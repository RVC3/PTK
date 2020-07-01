package ru.ppr.cppk.logic.pdSale.loader;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Pair;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ru.ppr.cppk.logic.pdSale.PdSaleRestrictions;
import ru.ppr.cppk.model.TariffsChain;
import ru.ppr.logger.Logger;
import ru.ppr.nsi.entity.Tariff;
import ru.ppr.nsi.model.TariffDescription;
import ru.ppr.nsi.repository.TariffRepository;
import ru.ppr.utils.CollectionUtils;

/**
 * Лоадер тарифов при оформлении ПД.
 *
 * @author Aleksandr Brazhkin
 */
public class TariffsLoader extends BaseLoader {

    private static String TAG = Logger.makeLogTag(TariffsLoader.class);

    private final TariffRepository tariffRepository;

    public TariffsLoader(PdSaleRestrictions pdSaleRestrictions, TariffRepository tariffRepository) {
        super(pdSaleRestrictions);

        this.tariffRepository = tariffRepository;
    }

    /**
     * Возвращает список цепочек прямых тарифов туда между двумя станциями.
     *
     * @param fromStationCode Код станции отправления
     * @param toStationCode   Код станции назначения
     * @param tariffPlanCode  Код тарифного плана
     * @param ticketTypeCode  Код типа ПД
     * @return Список цепочек тарифов
     */
    @NonNull
    public List<TariffsChain> loadDirectTariffsThere(@Nullable Long fromStationCode, @Nullable Long toStationCode, @Nullable Long tariffPlanCode, @Nullable Long ticketTypeCode) {
        return loadDirectTariffsThere(fromStationCode, toStationCode, CollectionUtils.asSet(tariffPlanCode), ticketTypeCode);
    }

    /**
     * Возвращает список цепочек прямых тарифов туда между двумя станциями.
     *
     * @param fromStationCode Код станции отправления
     * @param toStationCode   Код станции назначения
     * @param tariffPlanCodes Коды тарифных планов
     * @param ticketTypeCode  Код типа ПД
     * @return Список цепочек тарифов
     */
    @NonNull
    public List<TariffsChain> loadDirectTariffsThere(@Nullable Long fromStationCode, @Nullable Long toStationCode, @Nullable Set<Long> tariffPlanCodes, @Nullable Long ticketTypeCode) {
        Set<TariffCodesChain> tariffCodesChains = loadDirectTariffsInternal(fromStationCode, toStationCode, tariffPlanCodes, ticketTypeCode);
        return handleResult(tariffCodesChains);
    }

    /**
     * Возвращает список цепочек транзитных тарифов туда между двумя станциями.
     *
     * @param fromStationCode Код станции отправления
     * @param toStationCode   Код станции назначения
     * @param tariffPlanCode  Код тарифного плана
     * @param ticketTypeCode  Код типа ПД
     * @return Список цепочек тарифов
     */
    @NonNull
    public List<TariffsChain> loadTransitTariffsThere(@Nullable Long fromStationCode, @Nullable Long toStationCode, @Nullable Long tariffPlanCode, @Nullable Long ticketTypeCode) {
        Set<TariffCodesChain> tariffCodesChains = loadTransitTariffsInternal(fromStationCode, toStationCode, tariffPlanCode, ticketTypeCode);
        return handleResult(tariffCodesChains);
    }

    /**
     * Возвращает список цепочек прямых тарифов туда и обратно между двумя станциями.
     *
     * @param fromStationCode Код станции отправления
     * @param toStationCode   Код станции назначения
     * @param tariffPlanCode  Код тарифного плана
     * @param ticketTypeCode  Код типа ПД
     * @return Список цепочек тарифов
     */
    @NonNull
    public Pair<List<TariffsChain>, List<TariffsChain>> loadDirectTariffsThereAndBack(@Nullable Long fromStationCode, @Nullable Long toStationCode, @Nullable Long tariffPlanCode, @Nullable Long ticketTypeCode) {
        return loadDirectTariffsThereAndBack(fromStationCode, toStationCode, CollectionUtils.asSet(tariffPlanCode), ticketTypeCode);
    }

    /**
     * Возвращает список цепочек прямых тарифов туда и обратно между двумя станциями.
     *
     * @param fromStationCode Код станции отправления
     * @param toStationCode   Код станции назначения
     * @param tariffPlanCodes Коды тарифных планов
     * @param ticketTypeCode  Код типа ПД
     * @return Список цепочек тарифов
     */
    @NonNull
    public Pair<List<TariffsChain>, List<TariffsChain>> loadDirectTariffsThereAndBack(@Nullable Long fromStationCode, @Nullable Long toStationCode, @Nullable Set<Long> tariffPlanCodes, @Nullable Long ticketTypeCode) {
        Set<TariffCodesChain> directTariffsThere = loadDirectTariffsInternal(fromStationCode, toStationCode, tariffPlanCodes, ticketTypeCode);
        Set<TariffCodesChain> directTariffsBack = loadDirectTariffsInternal(toStationCode, fromStationCode, tariffPlanCodes, ticketTypeCode);
        return Pair.create(handleResult(directTariffsThere), handleResult(directTariffsBack));
    }

    /**
     * Возвращает список цепочек транзитных тарифов туда и обратно между двумя станциями.
     *
     * @param fromStationCode Код станции отправления
     * @param toStationCode   Код станции назначения
     * @param tariffPlanCode  Код тарифного плана
     * @param ticketTypeCode  Код типа ПД
     * @return Список цепочек тарифов
     */
    @NonNull
    public Pair<List<TariffsChain>, List<TariffsChain>> loadTransitTariffsThereAndBack(@Nullable Long fromStationCode, @Nullable Long toStationCode, @Nullable Long tariffPlanCode, @Nullable Long ticketTypeCode) {
        Set<TariffCodesChain> transitTariffsThere = loadTransitTariffsInternal(fromStationCode, toStationCode, tariffPlanCode, ticketTypeCode);
        Set<TariffCodesChain> transitTariffsBack = loadTransitTariffsInternal(toStationCode, fromStationCode, tariffPlanCode, ticketTypeCode);
        return Pair.create(handleResult(transitTariffsThere), handleResult(transitTariffsBack));
    }

    @NonNull
    private List<TariffsChain> handleResult(@NonNull Set<TariffCodesChain> tariffCodesChains) {
        List<TariffsChain> tariffsChains = new ArrayList<>();
        for (TariffCodesChain tariffCodesChain : tariffCodesChains) {
            List<Tariff> tariffs = new ArrayList<>();
            for (Long tariffCode : tariffCodesChain.getTariffCodes()) {
                tariffs.add(tariffRepository.load(tariffCode, getVersionId()));
            }
            TariffsChain tariffsChain = new TariffsChain(tariffs);
            tariffsChains.add(tariffsChain);
        }
        return tariffsChains;
    }

    private Set<TariffCodesChain> loadDirectTariffsInternal(@Nullable Long fromStationCode, @Nullable Long toStationCode, @Nullable Set<Long> tariffPlanCodes, @Nullable Long ticketTypeCode) {
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
                CollectionUtils.innerJoin(getPdSaleRestrictions().getAllowedTicketTypeCodes(), ticketTypeCode),
                EnumSet.of(TariffDescription.Field.TARIFF_CODE),
                getVersionId());

        return CollectionUtils.asSet(entries, entry -> new TariffCodesChain(entry.getTariffCode()));
    }

    private Set<TariffCodesChain> loadTransitTariffsInternal(@Nullable Long fromStationCode, @Nullable Long toStationCode, @Nullable Long tariffPlanCode, @Nullable Long ticketTypeCode) {
        // Тарифы от станции отправления до транзитных станций
        Collection<TariffDescription> depToTransitEntries = findTariffsFromDepToTransit(fromStationCode, tariffPlanCode, ticketTypeCode);

        Set<TariffCodesChain> tariffCodesChains = new HashSet<>();

        for (TariffDescription depToTransitEntry : depToTransitEntries) {
            Set<Long> depToTransitTariffPlanCodes = Collections.singleton(depToTransitEntry.getTariffPlanCode());
            Set<Long> depToTransitTicketTypeCodes = Collections.singleton(depToTransitEntry.getTicketTypeCode());
            Set<Long> depToTransitDestStationCodes = Collections.singleton(depToTransitEntry.getDestStationCode());

            // Тарифы от транзитной станции до станции назначения
            Collection<TariffDescription> transitToDestEntries = findTariffsFromTransitToDest(
                    fromStationCode,
                    toStationCode,
                    depToTransitDestStationCodes,
                    depToTransitTariffPlanCodes,
                    depToTransitTicketTypeCodes
            );

            for (TariffDescription transitToDestEntry : transitToDestEntries) {
                List<Long> tariffCodes = new ArrayList<>(2);
                tariffCodes.add(depToTransitEntry.getTariffCode());
                tariffCodes.add(transitToDestEntry.getTariffCode());
                tariffCodesChains.add(new TariffCodesChain(tariffCodes));
            }
        }
        return tariffCodesChains;
    }

    private Collection<TariffDescription> findTariffsFromDepToTransit(@Nullable Long fromStationCode, @Nullable Long tariffPlanCode, @Nullable Long ticketTypeCode) {
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
                CollectionUtils.innerJoin(getPdSaleRestrictions().getAllowedTariffPlanCodes(), tariffPlanCode),
                CollectionUtils.innerJoin(getPdSaleRestrictions().getAllowedTicketTypeCodes(), ticketTypeCode),
                EnumSet.of(
                        TariffDescription.Field.TARIFF_PLAN_CODE,
                        TariffDescription.Field.TICKET_TYPE_CODE,
                        TariffDescription.Field.DEST_STATION_CODE,
                        TariffDescription.Field.TARIFF_CODE
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
                        TariffDescription.Field.TARIFF_CODE
                ),
                getVersionId());
    }

    /**
     * Цепочка кодов тарифов
     */
    private class TariffCodesChain {
        private final List<Long> tariffCodes;

        TariffCodesChain(List<Long> tariffCodes) {
            this.tariffCodes = tariffCodes;
        }

        TariffCodesChain(Long tariffCode) {
            this.tariffCodes = Collections.singletonList(tariffCode);
        }

        List<Long> getTariffCodes() {
            return tariffCodes;
        }
    }

}
