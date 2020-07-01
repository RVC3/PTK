package ru.ppr.cppk.logic.pdSale.loader;

import android.support.annotation.Nullable;

import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import ru.ppr.cppk.logic.pdSale.PdSaleRestrictions;
import ru.ppr.logger.Logger;
import ru.ppr.nsi.entity.TariffPlan;
import ru.ppr.nsi.model.TariffDescription;
import ru.ppr.nsi.repository.TariffPlanRepository;
import ru.ppr.nsi.repository.TariffRepository;
import ru.ppr.utils.CollectionUtils;

/**
 * Лоадер тарифных планов при оформлении ПД.
 *
 * @author Aleksandr Brazhkin
 */
public class TariffPlansLoader extends BaseLoader {

    private static String TAG = Logger.makeLogTag(TariffPlansLoader.class);

    private final TariffPlanRepository tariffPlanRepository;
    private final TariffRepository tariffRepository;

    public TariffPlansLoader(PdSaleRestrictions pdSaleRestrictions, TariffPlanRepository tariffPlanRepository, TariffRepository tariffRepository) {
        super(pdSaleRestrictions);

        this.tariffPlanRepository = tariffPlanRepository;
        this.tariffRepository = tariffRepository;
    }

    /**
     * Возвращает список тарифных планов между двумя станциями, для которых есть прямые тарифы.
     *
     * @param fromStationCode Код станции отправления
     * @param toStationCode   Код станции назначения
     * @return Список тарифных планов
     */
    public List<TariffPlan> loadDirectTariffPlans(@Nullable Long fromStationCode, @Nullable Long toStationCode) {
        Set<Long> tariffPlanCodes = loadDirectTariffPlansInternal(fromStationCode, toStationCode);
        return handleResult(tariffPlanCodes);
    }

    /**
     * Возвращает список тарифных планов между двумя станциями, для которых есть транзитные тарифы.
     *
     * @param fromStationCode Код станции отправления
     * @param toStationCode   Код станции назначения
     * @return Список тарифных планов
     */
    public List<TariffPlan> loadTransitTariffPlans(@Nullable Long fromStationCode, @Nullable Long toStationCode) {
        Set<Long> tariffPlanCodes = loadTransitTariffPlansInternal(fromStationCode, toStationCode);
        return handleResult(tariffPlanCodes);
    }

    /**
     * Возвращает список тарифных планов между двумя станциями, для которых есть прямые или транзитные тарифы.
     *
     * @param fromStationCode Код станции отправления
     * @param toStationCode   Код станции назначения
     * @return Список тарифных планов
     */
    public List<TariffPlan> loadAllTariffPlans(@Nullable Long fromStationCode, @Nullable Long toStationCode) {
        Set<Long> directTariffPlanCodes = loadDirectTariffPlansInternal(fromStationCode, toStationCode);
        Set<Long> transitTariffPlanCodes = loadTransitTariffPlansInternal(fromStationCode, toStationCode);
        Set<Long> tariffPlanCodes = CollectionUtils.fullOuterJoin(directTariffPlanCodes, transitTariffPlanCodes);
        return handleResult(tariffPlanCodes);
    }

    private List<TariffPlan> handleResult(Set<Long> tariffPlanCodes) {
        return tariffPlanRepository.loadAll(tariffPlanCodes, getVersionId());
    }

    private Set<Long> loadDirectTariffPlansInternal(@Nullable Long fromStationCode, @Nullable Long toStationCode) {
        Collection<TariffDescription> entries = tariffRepository.loadTariffsForSaleQuery(
                // Станция отправления ограничивается:
                // - Списком из CommonSettings
                // - Конретной выбранной станцией отправления в случае, если таковая указана
                // - Списком станций, до которых есть маршруты с участка работы ПТК
                CollectionUtils.innerJoin(getPdSaleRestrictions().getAllowedStationCodesBySettingsAndProdSection(), fromStationCode),
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
                EnumSet.of(TariffDescription.Field.TARIFF_PLAN_CODE),
                getVersionId());

        return CollectionUtils.asSet(entries, TariffDescription::getTariffPlanCode);
    }

    private Set<Long> loadTransitTariffPlansInternal(@Nullable Long fromStationCode, @Nullable Long toStationCode) {
        // Тарифы от станции отправления до транзитных станций
        Collection<TariffDescription> depToTransitEntries = findTariffsFromDepToTransit(fromStationCode);

        Set<Long> depToTransitTariffPlanCodes = CollectionUtils.asSet(depToTransitEntries, TariffDescription::getTariffPlanCode);
        Set<Long> depToTransitTicketTypeCodes = CollectionUtils.asSet(depToTransitEntries, TariffDescription::getTicketTypeCode);
        Set<Long> depToTransitDestStationCodes = CollectionUtils.asSet(depToTransitEntries, TariffDescription::getDestStationCode);

        // Тарифы от транзитных станций до станции назначения
        Collection<TariffDescription> transitToDestEntries = findTariffsFromTransitToDest(
                fromStationCode,
                toStationCode,
                depToTransitDestStationCodes,
                depToTransitTariffPlanCodes,
                depToTransitTicketTypeCodes
        );

        return CollectionUtils.asSet(transitToDestEntries, TariffDescription::getTariffPlanCode);
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
                                                                       @Nullable Collection<Long> allowedDepStationCodes,
                                                                       @Nullable Collection<Long> allowedTariffPlanCodes,
                                                                       @Nullable Collection<Long> allowedTicketTypeCodes) {
        return tariffRepository.loadTariffsForSaleQuery(
                // Станция отправления (транзитная) ограничивается:
                // - Списком, найденным на другом этапе (Предполагается, что он уже отфильтрован)
                allowedDepStationCodes,
                // Станция назначения ограничивается:
                // - Списком из CommonSettings
                // - Конретной выбранной станцией назначения в случае, если таковая указана
                // - Списком станций, до которых есть маршруты с участка работы ПТК
                CollectionUtils.innerJoin(getPdSaleRestrictions().getAllowedStationCodesBySettingsAndProdSection(), toStationCode),
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
                        TariffDescription.Field.TARIFF_PLAN_CODE
                ),
                getVersionId());
    }

}
