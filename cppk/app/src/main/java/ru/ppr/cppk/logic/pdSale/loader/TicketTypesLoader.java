package ru.ppr.cppk.logic.pdSale.loader;

import android.support.annotation.Nullable;

import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import ru.ppr.cppk.logic.pdSale.PdSaleRestrictions;
import ru.ppr.nsi.entity.TicketType;
import ru.ppr.nsi.model.TariffDescription;
import ru.ppr.nsi.repository.TariffRepository;
import ru.ppr.nsi.repository.TicketTypeRepository;
import ru.ppr.utils.CollectionUtils;

/**
 * Лоадер типов ПД при оформлении ПД.
 *
 * @author Aleksandr Brazhkin
 */
public class TicketTypesLoader extends BaseLoader {

    private final TicketTypeRepository ticketTypeRepository;
    private final TariffRepository tariffRepository;

    public TicketTypesLoader(PdSaleRestrictions pdSaleRestrictions,
                             TicketTypeRepository ticketTypeRepository,
                             TariffRepository tariffRepository) {
        super(pdSaleRestrictions);
        this.ticketTypeRepository = ticketTypeRepository;
        this.tariffRepository = tariffRepository;
    }

    /**
     * Возвращает список типов ПД между двумя станциями, для которых есть прямые тарифы.
     *
     * @param fromStationCode Код станции отправления
     * @param toStationCode   Код станции назначения
     * @param tariffPlanCode  Код тарифного плана
     * @return Список список типов ПД
     */
    public List<TicketType> loadDirectTicketTypes(@Nullable Long fromStationCode, @Nullable Long toStationCode, @Nullable Long tariffPlanCode) {
        return loadDirectTicketTypes(fromStationCode, toStationCode, CollectionUtils.asSet(tariffPlanCode));
    }

    /**
     * Возвращает список типов ПД между двумя станциями, для которых есть прямые тарифы.
     *
     * @param fromStationCode Код станции отправления
     * @param toStationCode   Код станции назначения
     * @param tariffPlanCodes Коды тарифных планов
     * @return Список список типов ПД
     */
    public List<TicketType> loadDirectTicketTypes(@Nullable Long fromStationCode, @Nullable Long toStationCode, @Nullable Set<Long> tariffPlanCodes) {
        Set<Long> ticketTypeCodes = loadDirectTicketTypesInternal(fromStationCode, toStationCode, tariffPlanCodes);
        return handleResult(ticketTypeCodes);
    }

    /**
     * Возвращает список типов ПД между двумя станциями, для которых есть транзитные тарифы.
     *
     * @param fromStationCode Код станции отправления
     * @param toStationCode   Код станции назначения
     * @param tariffPlanCode  Код тарифного плана
     * @return Список список типов ПД
     */
    public List<TicketType> loadTransitTicketTypes(@Nullable Long fromStationCode, @Nullable Long toStationCode, @Nullable Long tariffPlanCode) {
        Set<Long> ticketTypeCodes = loadTransitTicketTypesInternal(fromStationCode, toStationCode, tariffPlanCode);
        return handleResult(ticketTypeCodes);
    }

    /**
     * Возвращает список типов ПД между двумя станциями, для которых есть прямые или транзитные тарифы.
     *
     * @param fromStationCode Код станции отправления
     * @param toStationCode   Код станции назначения
     * @param tariffPlanCode  Код тарифного плана
     * @return Список список типов ПД
     */
    public List<TicketType> loadAllTicketTypes(@Nullable Long fromStationCode, @Nullable Long toStationCode, @Nullable Long tariffPlanCode) {
        return loadAllTicketTypes(fromStationCode, toStationCode, CollectionUtils.asSet(tariffPlanCode));
    }

    /**
     * Возвращает список типов ПД между двумя станциями, для которых есть прямые или транзитные тарифы.
     *
     * @param fromStationCode Код станции отправления
     * @param toStationCode   Код станции назначения
     * @param tariffPlanCodes Коды тарифных планов
     * @return Список список типов ПД
     */
    public List<TicketType> loadAllTicketTypes(@Nullable Long fromStationCode, @Nullable Long toStationCode, @Nullable Set<Long> tariffPlanCodes) {
        Set<Long> directTicketTypeCodes = loadDirectTicketTypesInternal(fromStationCode, toStationCode, tariffPlanCodes);
        Set<Long> transitTicketTypeCodes = loadTransitTicketTypesInternal(fromStationCode, toStationCode, tariffPlanCodes);
        Set<Long> ticketTypeCodes = CollectionUtils.fullOuterJoin(directTicketTypeCodes, transitTicketTypeCodes);
        return handleResult(ticketTypeCodes);
    }

    private List<TicketType> handleResult(Set<Long> ticketTypeCodes) {
        return ticketTypeRepository.loadAll(ticketTypeCodes, getVersionId());
    }

    private Set<Long> loadDirectTicketTypesInternal(@Nullable Long fromStationCode, @Nullable Long toStationCode, @Nullable Set<Long> tariffPlanCodes) {
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
                CollectionUtils.innerJoin(getPdSaleRestrictions().getAllowedTariffPlanCodes(), tariffPlanCodes),
                getPdSaleRestrictions().getAllowedTicketTypeCodes(),
                EnumSet.of(TariffDescription.Field.TICKET_TYPE_CODE),
                getVersionId());

        return CollectionUtils.asSet(entries, TariffDescription::getTicketTypeCode);
    }

    private Set<Long> loadTransitTicketTypesInternal(@Nullable Long fromStationCode, @Nullable Long toStationCode, @Nullable Long tariffPlanCode) {
        return loadTransitTicketTypesInternal(fromStationCode, toStationCode, CollectionUtils.asSet(tariffPlanCode));
    }

    private Set<Long> loadTransitTicketTypesInternal(@Nullable Long fromStationCode, @Nullable Long toStationCode, @Nullable Set<Long> tariffPlanCodes) {
        // Тарифы от станции отправления до транзитных станций
        Collection<TariffDescription> depToTransitEntries = findTariffsFromDepToTransit(fromStationCode, tariffPlanCodes);

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

        return CollectionUtils.asSet(transitToDestEntries, TariffDescription::getTicketTypeCode);
    }

    private Collection<TariffDescription> findTariffsFromDepToTransit(@Nullable Long fromStationCode, @Nullable Set<Long> tariffPlanCodes) {
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
                CollectionUtils.innerJoin(getPdSaleRestrictions().getAllowedTariffPlanCodes(), tariffPlanCodes),
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
                        TariffDescription.Field.TICKET_TYPE_CODE
                ),
                getVersionId());
    }

}
