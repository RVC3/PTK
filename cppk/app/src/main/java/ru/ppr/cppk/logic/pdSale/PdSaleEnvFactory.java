package ru.ppr.cppk.logic.pdSale;

import android.support.annotation.NonNull;

import javax.inject.Inject;
import javax.inject.Singleton;

import ru.ppr.cppk.data.summary.RecentStationsStatistics;
import ru.ppr.cppk.di.Dagger;
import ru.ppr.cppk.helpers.TicketTypeChecker;
import ru.ppr.cppk.managers.NsiVersionManager;
import ru.ppr.logger.Logger;
import ru.ppr.nsi.NsiDbSessionManager;
import ru.ppr.nsi.repository.StationRepository;
import ru.ppr.nsi.repository.StationToTariffZoneRepository;
import ru.ppr.nsi.repository.StationTransferRouteRepository;
import ru.ppr.nsi.repository.TariffPlanRepository;
import ru.ppr.nsi.repository.TariffRepository;
import ru.ppr.nsi.repository.TicketTypeRepository;
import ru.ppr.nsi.repository.TicketTypesValidityTimesRepository;
import ru.ppr.nsi.repository.TrainCategoryRepository;
import ru.ppr.nsi.repository.TrainTicketTypeForTransferRegistrationRepository;

/**
 * Фабрика для {@link PdSaleEnv}.
 *
 * @author Aleksandr Brazhkin
 */
@Singleton
public class PdSaleEnvFactory {

    private static final String TAG = Logger.makeLogTag(PdSaleEnvFactory.class);

    private final NsiDbSessionManager nsiDbSessionManager;
    private final RecentStationsStatistics recentStationsStatistics;
    private final TicketTypesValidityTimesRepository ticketTypesValidityTimesRepository;
    private final TicketTypeRepository ticketTypeRepository;
    private final TariffRepository tariffRepository;
    private final TariffPlanRepository tariffPlanRepository;
    private final StationRepository stationRepository;
    private final TrainCategoryRepository trainCategoryRepository;
    private final StationTransferRouteRepository stationTransferRouteRepository;
    private final TrainTicketTypeForTransferRegistrationRepository trainTicketTypeForTransferRegistrationRepository;
    private final StationToTariffZoneRepository stationToTariffZoneRepository;
    private final NsiVersionManager nsiVersionManager;
    private final TicketTypeChecker ticketTypeChecker;

    private PdSaleEnv pdSaleEnvForSinglePd;
    private PdSaleEnv pdSaleEnvForBaggage;
    private PdSaleEnv pdSaleEnvForTariffsInfo;
    private PdSaleEnv pdSaleEnvForTransfer;
    private PdSaleEnv pdSaleEnvForExtraPayment;

    @Inject
    PdSaleEnvFactory(NsiDbSessionManager nsiDbSessionManager,
                     RecentStationsStatistics recentStationsStatistics,
                     TicketTypesValidityTimesRepository ticketTypesValidityTimesRepository,
                     TicketTypeRepository ticketTypeRepository,
                     TariffRepository tariffRepository,
                     TariffPlanRepository tariffPlanRepository,
                     StationRepository stationRepository,
                     TrainCategoryRepository trainCategoryRepository,
                     StationTransferRouteRepository stationTransferRouteRepository,
                     TrainTicketTypeForTransferRegistrationRepository trainTicketTypeForTransferRegistrationRepository,
                     StationToTariffZoneRepository stationToTariffZoneRepository,
                     NsiVersionManager nsiVersionManager,
                     TicketTypeChecker ticketTypeChecker) {
        this.nsiDbSessionManager = nsiDbSessionManager;
        this.recentStationsStatistics = recentStationsStatistics;
        this.ticketTypesValidityTimesRepository = ticketTypesValidityTimesRepository;
        this.ticketTypeRepository = ticketTypeRepository;
        this.tariffRepository = tariffRepository;
        this.tariffPlanRepository = tariffPlanRepository;
        this.stationRepository = stationRepository;
        this.trainCategoryRepository = trainCategoryRepository;
        this.stationTransferRouteRepository = stationTransferRouteRepository;
        this.trainTicketTypeForTransferRegistrationRepository = trainTicketTypeForTransferRegistrationRepository;
        this.stationToTariffZoneRepository = stationToTariffZoneRepository;
        this.nsiVersionManager = nsiVersionManager;
        this.ticketTypeChecker = ticketTypeChecker;
    }

    @NonNull
    public PdSaleEnv pdSaleEnvForTariffsInfo() {
        return pdSaleEnvForTariffsInfo;
    }

    @NonNull
    public PdSaleEnv pdSaleEnvForSinglePd() {
        return pdSaleEnvForSinglePd;
    }

    @NonNull
    public PdSaleEnv pdSaleEnvForBaggage() {
        return pdSaleEnvForBaggage;
    }

    /**
     * Подумать перед использованием.
     * Создан как быстрое решение проблем расшаривания состояния на разные {@link PdSaleRestrictions} оформления трансфера.
     */
    @NonNull
    public PdSaleEnv newPdSaleEnvForTransfer() {
        return new PdSaleEnv(nsiDbSessionManager.getDaoSession(),
                recentStationsStatistics,
                ticketTypesValidityTimesRepository,
                ticketTypeRepository,
                tariffRepository,
                tariffPlanRepository,
                stationRepository,
                stationToTariffZoneRepository,
                trainCategoryRepository,
                stationTransferRouteRepository,
                trainTicketTypeForTransferRegistrationRepository,
                nsiVersionManager,
                Dagger.appComponent().pdValidityPeriodCalculator(),
                PdSaleEnvType.TRANSFER,
                ticketTypeChecker);
    }

    @NonNull
    public PdSaleEnv pdSaleEnvForTransfer() {
        return pdSaleEnvForTransfer;
    }

    @NonNull
    public PdSaleEnv pdSaleEnvForExtraPayment() {
        return pdSaleEnvForExtraPayment;
    }

    /**
     * Выполняет сброс закешированных окружений.
     * Вызывать после обновления БД и иных подобных оперций
     */
    public void reset() {
        pdSaleEnvForSinglePd = new PdSaleEnv(
                nsiDbSessionManager.getDaoSession(),
                recentStationsStatistics,
                ticketTypesValidityTimesRepository,
                ticketTypeRepository,
                tariffRepository,
                tariffPlanRepository,
                stationRepository,
                stationToTariffZoneRepository,
                trainCategoryRepository,
                stationTransferRouteRepository,
                trainTicketTypeForTransferRegistrationRepository,
                nsiVersionManager,
                Dagger.appComponent().pdValidityPeriodCalculator(),
                PdSaleEnvType.SINGLE_PD,
                ticketTypeChecker);
        pdSaleEnvForBaggage = new PdSaleEnv(nsiDbSessionManager.getDaoSession(),
                recentStationsStatistics,
                ticketTypesValidityTimesRepository,
                ticketTypeRepository,
                tariffRepository,
                tariffPlanRepository,
                stationRepository,
                stationToTariffZoneRepository,
                trainCategoryRepository,
                stationTransferRouteRepository,
                trainTicketTypeForTransferRegistrationRepository,
                nsiVersionManager,
                Dagger.appComponent().pdValidityPeriodCalculator(),
                PdSaleEnvType.BAGGAGE,
                ticketTypeChecker);
        pdSaleEnvForTariffsInfo = new PdSaleEnv(nsiDbSessionManager.getDaoSession(),
                recentStationsStatistics,
                ticketTypesValidityTimesRepository,
                ticketTypeRepository,
                tariffRepository,
                tariffPlanRepository,
                stationRepository,
                stationToTariffZoneRepository,
                trainCategoryRepository,
                stationTransferRouteRepository,
                trainTicketTypeForTransferRegistrationRepository,
                nsiVersionManager,
                Dagger.appComponent().pdValidityPeriodCalculator(),
                PdSaleEnvType.TARIFFS_INFO,
                ticketTypeChecker);
        pdSaleEnvForTransfer = new PdSaleEnv(nsiDbSessionManager.getDaoSession(),
                recentStationsStatistics,
                ticketTypesValidityTimesRepository,
                ticketTypeRepository,
                tariffRepository,
                tariffPlanRepository,
                stationRepository,
                stationToTariffZoneRepository,
                trainCategoryRepository,
                stationTransferRouteRepository,
                trainTicketTypeForTransferRegistrationRepository,
                nsiVersionManager,
                Dagger.appComponent().pdValidityPeriodCalculator(),
                PdSaleEnvType.TRANSFER,
                ticketTypeChecker);
        pdSaleEnvForExtraPayment = new PdSaleEnv(nsiDbSessionManager.getDaoSession(),
                recentStationsStatistics,
                ticketTypesValidityTimesRepository,
                ticketTypeRepository,
                tariffRepository,
                tariffPlanRepository,
                stationRepository,
                stationToTariffZoneRepository,
                trainCategoryRepository,
                stationTransferRouteRepository,
                trainTicketTypeForTransferRegistrationRepository,
                nsiVersionManager,
                Dagger.appComponent().pdValidityPeriodCalculator(),
                PdSaleEnvType.EXTRA_PAYMENT,
                ticketTypeChecker);
    }

}
