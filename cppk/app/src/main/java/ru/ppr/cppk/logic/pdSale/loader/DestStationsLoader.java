package ru.ppr.cppk.logic.pdSale.loader;

import android.annotation.SuppressLint;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ru.ppr.cppk.data.summary.RecentStationsStatistics;
import ru.ppr.cppk.logic.pdSale.PdSaleRestrictions;
import ru.ppr.logger.Logger;
import ru.ppr.nsi.model.TariffDescription;
import ru.ppr.nsi.repository.StationRepository;
import ru.ppr.nsi.repository.TariffRepository;
import ru.ppr.utils.CollectionUtils;

/**
 * Лоадер станций назначения при оформлении ПД.
 *
 * @author Aleksandr Brazhkin
 */
public class DestStationsLoader extends BaseStationsLoader {

    private static String TAG = Logger.makeLogTag(DestStationsLoader.class);

    private final TariffRepository tariffRepository;

    public DestStationsLoader(PdSaleRestrictions pdSaleRestrictions,
                              RecentStationsStatistics recentStationsStatistics,
                              StationRepository stationRepository,
                              TariffRepository tariffRepository) {
        super(pdSaleRestrictions, recentStationsStatistics, stationRepository);

        this.tariffRepository = tariffRepository;
    }

    @Override
    protected List<Integer> getRecentStationCodes() {
        return getRecentStationsStatistics().getRecentDestinationsStationsCodes();
    }

    @Override
    protected Set<Long> loadDirectStationsInternal(@Nullable Long fromStationCode, @Nullable Long toStationCode, @Nullable String likeQuery) {
        // Станция назначения ограничивается:
        // - Списком из CommonSettings
        // - Конретной выбранной станцией назначения в случае, если таковая указана
        // - Списком станций, до которых есть маршруты с участка работы ПТК
        // - Введенным поисковым запросом

        Set<Long> allowedDestStationCodes;
        if (fromStationCode != null && !getPdSaleRestrictions().belongsToCurrentProductionSection(fromStationCode)) {
            allowedDestStationCodes = CollectionUtils.innerJoin(getPdSaleRestrictions().getStationCodesForProductionSection(), toStationCode);
        } else {
            allowedDestStationCodes = CollectionUtils.innerJoin(getPdSaleRestrictions().getAllowedStationCodesBySettingsAndProdSectionInProdSection(), toStationCode);
        }
        Set<Long> allowedFilteredByNameDestStationCodes = filterByName(allowedDestStationCodes, likeQuery);


        Collection<TariffDescription> entries = tariffRepository.loadTariffsForSaleQuery(
                // Станция отправления ограничивается:
                // - Списком из CommonSettings
                // - Конретной выбранной станцией отправления в случае, если таковая указана
                // - Списком станций, до которых есть маршруты с участка работы ПТК
                CollectionUtils.innerJoin(getPdSaleRestrictions().getAllowedStationCodesBySettingsAndProdSection(), fromStationCode),
                allowedFilteredByNameDestStationCodes,
                // Из списка станций отправления исключаются:
                // - Список запрещенных к оформлению станций
                getPdSaleRestrictions().getDeniedStationCodes(),
                // Из списка станций назначения исключаются:
                // - Список запрещенных к оформлению станций
                getPdSaleRestrictions().getDeniedStationCodes(),
                getPdSaleRestrictions().getAllowedTariffPlanCodes(),
                getPdSaleRestrictions().getAllowedTicketTypeCodes(),
                EnumSet.of(TariffDescription.Field.DEST_STATION_CODE),
                getVersionId());

        return CollectionUtils.asSet(entries, TariffDescription::getDestStationCode);
    }

    @Override
    protected Set<Long> loadTransitStationsInternal(@Nullable Long fromStationCode, @Nullable Long toStationCode, @Nullable String likeQuery) {
        // Тарифы от станции отправления до транзитных станций
        Collection<TariffDescription> depToTransitTariffs = findTariffsFromDepToTransit(fromStationCode);

        // Складываем в Map все возможные TariffType до транзитных станций
        @SuppressLint("UseSparseArrays")
        Map<Long, Set<TariffType>> depToTransitTariffsMap = new HashMap<>();
        for (TariffDescription tariff : depToTransitTariffs) {
            TariffType tariffType = new TariffType(tariff.getTicketTypeCode(), tariff.getTariffPlanCode());
            Set<TariffType> tariffTypes = depToTransitTariffsMap.get(tariff.getDestStationCode());
            if (tariffTypes == null) {
                tariffTypes = new HashSet<>();
                depToTransitTariffsMap.put(tariff.getDestStationCode(), tariffTypes);
            }
            tariffTypes.add(tariffType);
        }

        Set<Long> depToTransitTariffPlanCodes = CollectionUtils.asSet(depToTransitTariffs, TariffDescription::getTariffPlanCode);
        Set<Long> depToTransitTicketTypeCodes = CollectionUtils.asSet(depToTransitTariffs, TariffDescription::getTicketTypeCode);
        Set<Long> depToTransitDestStationCodes = CollectionUtils.asSet(depToTransitTariffs, TariffDescription::getDestStationCode);

        // Тарифы от транзитных станций до станции назначения
        Collection<TariffDescription> transitToDestTariffs = findTariffsFromTransitToDest(
                fromStationCode,
                toStationCode,
                likeQuery,
                depToTransitDestStationCodes,
                depToTransitTariffPlanCodes,
                depToTransitTicketTypeCodes
        );

        // Выполняем фильтрацию тарифов да станции назначения так, чтобы исключить варианты,
        // когда разные части транзитного маршрута проходят по разным TariffType
        Collection<TariffDescription> filteredTransitToDestTariffs = new ArrayList<>();
        for (TariffDescription tariff : transitToDestTariffs) {
            Set<TariffType> tariffTypes = depToTransitTariffsMap.get(tariff.getDepStationCode());
            TariffType tariffType = new TariffType(tariff.getTicketTypeCode(), tariff.getTariffPlanCode());
            if (tariffTypes.contains(tariffType)) {
                filteredTransitToDestTariffs.add(tariff);
            }
        }

        return CollectionUtils.asSet(filteredTransitToDestTariffs, TariffDescription::getDestStationCode);
    }

    private Collection<TariffDescription> findTariffsFromDepToTransit(@Nullable Long fromStationCode) {
        return tariffRepository.loadTariffsForSaleQuery(
                // Станция отправления ограничивается:
                // - Списком из CommonSettings
                // - Конретной выбранной станцией отправления в случае, если таковая указана
                // - Списком станций, до которых есть маршруты с участка работы ПТК
                CollectionUtils.innerJoin(getPdSaleRestrictions().getAllowedStationCodesBySettingsAndProdSection(), fromStationCode),
                // Транзитная станция ограничивается:
                // - Списком станций, которые являются транзитными
                // - Списком из CommonSettings
                // - Списком станций, до которых есть маршруты с участка работы ПТК
                // - Списком станций, находящихся на участке работы ПТК (ВНЕ режима мобильной кассы) (http://agile.srvdev.ru/browse/CPPKPP-34719)
                getPdSaleRestrictions().getTransitStationCodes(),
                // Из списка станций отправления исключаются:
                // - Список запрещенных к оформлению станций
                getPdSaleRestrictions().getDeniedStationCodes(),
                // Из списка станций назначения исключаются:
                // - Список запрещенных к оформлению станций
                getPdSaleRestrictions().getDeniedStationCodes(),
                getPdSaleRestrictions().getAllowedTariffPlanCodes(),
                getPdSaleRestrictions().getAllowedTicketTypeCodes(),
                EnumSet.of(
                        TariffDescription.Field.TARIFF_PLAN_CODE,
                        TariffDescription.Field.TICKET_TYPE_CODE,
                        TariffDescription.Field.DEST_STATION_CODE
                ),
                getVersionId());
    }

    private Collection<TariffDescription> findTariffsFromTransitToDest(@Nullable Long fromStationCode,
                                                                       @Nullable Long toStationCode,
                                                                       @Nullable String likeQuery,
                                                                       @Nullable Collection<Long> allowedDepStationCodes,
                                                                       @Nullable Collection<Long> allowedTariffPlanCodes,
                                                                       @Nullable Collection<Long> allowedTicketTypeCodes) {
        // Станция назначения ограничивается:
        // - Списком из CommonSettings
        // - Конретной выбранной станцией назначения в случае, если таковая указана
        // - Списком станций, до которых есть маршруты с участка работы ПТК
        // - Введенным поисковым запросом
        Set<Long> allowedDestStationCodes = CollectionUtils.innerJoin(getPdSaleRestrictions().getAllowedStationCodesBySettingsAndProdSection(), toStationCode);
        Set<Long> allowedFilteredByNameDestStationCodes = filterByName(allowedDestStationCodes, likeQuery);

        return tariffRepository.loadTariffsForSaleQuery(
                // Станция отправления (транзитная) ограничивается:
                // - Списком, найденным на другом этапе (Предполагается, что он уже отфильтрован)
                allowedDepStationCodes,
                allowedFilteredByNameDestStationCodes,
                // Из списка станций отправления исключаются:
                // - Список запрещенных к оформлению станций
                getPdSaleRestrictions().getDeniedStationCodes(),
                // Из списка станций назначения исключаются:
                // - Станция отправления, если таковая указана
                // - Список запрещенных к оформлению станций
                CollectionUtils.fullOuterJoin(getPdSaleRestrictions().getDeniedStationCodes(), fromStationCode),
                allowedTariffPlanCodes,
                allowedTicketTypeCodes,
                EnumSet.of(
                        TariffDescription.Field.TARIFF_PLAN_CODE,
                        TariffDescription.Field.TICKET_TYPE_CODE,
                        TariffDescription.Field.DEP_STATION_CODE,
                        TariffDescription.Field.DEST_STATION_CODE
                ),
                getVersionId());
    }
}
