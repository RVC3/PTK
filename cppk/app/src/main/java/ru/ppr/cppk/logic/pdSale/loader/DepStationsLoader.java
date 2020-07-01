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
 * Лоадер станций отправления при оформлении ПД.
 *
 * @author Aleksandr Brazhkin
 */
public class DepStationsLoader extends BaseStationsLoader {

    private static String TAG = Logger.makeLogTag(DepStationsLoader.class);

    private final TariffRepository tariffRepository;

    public DepStationsLoader(PdSaleRestrictions pdSaleRestrictions,
                             RecentStationsStatistics recentStationsStatistics,
                             StationRepository stationRepository,
                             TariffRepository tariffRepository) {
        super(pdSaleRestrictions, recentStationsStatistics, stationRepository);

        this.tariffRepository = tariffRepository;
    }

    @Override
    protected List<Integer> getRecentStationCodes() {
        return getRecentStationsStatistics().getRecentDepartureStationsCodes();
    }

    @Override
    protected Set<Long> loadDirectStationsInternal(@Nullable Long fromStationCode, @Nullable Long toStationCode, @Nullable String likeQuery) {
        // Станция отправления ограничивается:
        // - Списком из CommonSettings
        // - Конретной выбранной станцией назначения в случае, если таковая указана
        // - Списком станций, до которых есть маршруты с участка работы ПТК
        // - Введенным поисковым запросом
        Set<Long> allowedDepStationCodes;
        if (toStationCode != null && !getPdSaleRestrictions().belongsToCurrentProductionSection(toStationCode)) {
            allowedDepStationCodes = CollectionUtils.innerJoin(getPdSaleRestrictions().getStationCodesForProductionSection(), fromStationCode);
        } else {
            allowedDepStationCodes = CollectionUtils.innerJoin(getPdSaleRestrictions().getAllowedStationCodesBySettingsAndProdSectionInProdSection(), fromStationCode);
        }
        Set<Long> allowedFilteredByNameDepStationCodes = filterByName(allowedDepStationCodes, likeQuery);

        Collection<TariffDescription> entries = tariffRepository.loadTariffsForSaleQuery(
                allowedFilteredByNameDepStationCodes,
                // Станция назначения ограничивается:
                // - Списком из CommonSettings
                // - Конретной выбранной станцией отправления в случае, если таковая указана
                // - Списком станций, до которых есть маршруты с участка работы ПТК
                CollectionUtils.innerJoin(getPdSaleRestrictions().getAllowedStationCodesBySettingsAndProdSection(), toStationCode),
                // Из списка станций отправления исключаются:
                // - Список запрещенных к оформлению станций
                getPdSaleRestrictions().getDeniedStationCodes(),
                // Из списка станций назначения исключаются:
                // - Список запрещенных к оформлению станций
                getPdSaleRestrictions().getDeniedStationCodes(),
                getPdSaleRestrictions().getAllowedTariffPlanCodes(),
                getPdSaleRestrictions().getAllowedTicketTypeCodes(),
                EnumSet.of(TariffDescription.Field.DEP_STATION_CODE),
                getVersionId());

        return CollectionUtils.asSet(entries, TariffDescription::getDepStationCode);
    }

    @Override
    protected Set<Long> loadTransitStationsInternal(@Nullable Long fromStationCode, @Nullable Long toStationCode, @Nullable String likeQuery) {
        // Тарифы от транзитных станций до станции назначения
        Collection<TariffDescription> transitToDestTariffs = findTariffsFromTransitToDest(toStationCode);

        // Складываем в Map все возможные TariffType от транзитных станций
        @SuppressLint("UseSparseArrays")
        Map<Long, Set<TariffType>> transitToDestTariffsMap = new HashMap<>();
        for (TariffDescription tariff : transitToDestTariffs) {
            TariffType tariffType = new TariffType(tariff.getTicketTypeCode(), tariff.getTariffPlanCode());
            Set<TariffType> tariffTypes = transitToDestTariffsMap.get(tariff.getDepStationCode());
            if (tariffTypes == null) {
                tariffTypes = new HashSet<>();
                transitToDestTariffsMap.put(tariff.getDepStationCode(), tariffTypes);
            }
            tariffTypes.add(tariffType);
        }

        Set<Long> transitToDestTariffPlanCodes = CollectionUtils.asSet(transitToDestTariffs, TariffDescription::getTariffPlanCode);
        Set<Long> transitToDestTypeCodes = CollectionUtils.asSet(transitToDestTariffs, TariffDescription::getTicketTypeCode);
        Set<Long> transitToDestStationCodes = CollectionUtils.asSet(transitToDestTariffs, TariffDescription::getDepStationCode);

        // Тарифы от станции отправления до транзитных станций
        Collection<TariffDescription> depToTransitTariffs = findTariffsFromDepToTransit(
                fromStationCode,
                toStationCode,
                likeQuery,
                transitToDestStationCodes,
                transitToDestTariffPlanCodes,
                transitToDestTypeCodes
        );

        // Выполняем фильтрацию тарифов да станции назначения так, чтобы исключить варианты,
        // когда разные части транзитного маршрута проходят по разным TariffType
        Collection<TariffDescription> filteredDepToTransitTariffs = new ArrayList<>();
        for (TariffDescription tariff : depToTransitTariffs) {
            Set<TariffType> tariffTypes = transitToDestTariffsMap.get(tariff.getDestStationCode());
            TariffType tariffType = new TariffType(tariff.getTicketTypeCode(), tariff.getTariffPlanCode());
            if (tariffTypes.contains(tariffType)) {
                filteredDepToTransitTariffs.add(tariff);
            }
        }

        return CollectionUtils.asSet(filteredDepToTransitTariffs, TariffDescription::getDepStationCode);
    }

    private Collection<TariffDescription> findTariffsFromTransitToDest(@Nullable Long toStationCode) {
        return tariffRepository.loadTariffsForSaleQuery(
                // Транзитная станция ограничивается:
                // - Списком станций, которые являются транзитными
                // - Списком из CommonSettings
                // - Списком станций, до которых есть маршруты с участка работы ПТК
                // - Списком станций, находящихся на участке работы ПТК (ВНЕ режима мобильной кассы) (http://agile.srvdev.ru/browse/CPPKPP-34719)
                getPdSaleRestrictions().getTransitStationCodes(),
                // Станция назначения ограничивается:
                // - Списком из CommonSettings
                // - Конретной выбранной станцией назначения в случае, если таковая указана
                // - Списком станций, до которых есть маршруты с участка работы ПТК
                CollectionUtils.innerJoin(getPdSaleRestrictions().getAllowedStationCodesBySettingsAndProdSection(), toStationCode),
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
                        TariffDescription.Field.DEP_STATION_CODE
                ),
                getVersionId());
    }

    private Collection<TariffDescription> findTariffsFromDepToTransit(@Nullable Long fromStationCode,
                                                                      @Nullable Long toStationCode,
                                                                      @Nullable String likeQuery,
                                                                      @Nullable Collection<Long> allowedDestStationCodes,
                                                                      @Nullable Collection<Long> allowedTariffPlanCodes,
                                                                      @Nullable Collection<Long> allowedTicketTypeCodes) {
        // Станция отправления ограничивается:
        // - Списком из CommonSettings
        // - Конретной выбранной станцией отправления в случае, если таковая указана
        // - Списком станций, до которых есть маршруты с участка работы ПТК
        // - Введенным поисковым запросом
        Set<Long> allowedDepStationCodes = CollectionUtils.innerJoin(getPdSaleRestrictions().getAllowedStationCodesBySettingsAndProdSection(), fromStationCode);
        Set<Long> allowedFilteredByNameDepStationCodes = filterByName(allowedDepStationCodes, likeQuery);


        return tariffRepository.loadTariffsForSaleQuery(
                allowedFilteredByNameDepStationCodes,
                // Станция назначения (транзитная) ограничивается:
                // - Списком, найденным на другом этапе (Предполагается, что он уже отфильтрован)
                allowedDestStationCodes,
                // Из списка станций отправления исключаются:
                // - Станция назначения, если таковая указана
                // - Список запрещенных к оформлению станций
                CollectionUtils.fullOuterJoin(getPdSaleRestrictions().getDeniedStationCodes(), toStationCode),
                // Из списка станций назначения исключаются:
                // - Список запрещенных к оформлению станций
                getPdSaleRestrictions().getDeniedStationCodes(),
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
