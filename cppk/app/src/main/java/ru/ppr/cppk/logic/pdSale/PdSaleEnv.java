package ru.ppr.cppk.logic.pdSale;

import android.support.annotation.NonNull;

import ru.ppr.cppk.data.summary.RecentStationsStatistics;
import ru.ppr.cppk.helpers.TicketTypeChecker;
import ru.ppr.cppk.logic.interactor.PdValidityPeriodCalculator;
import ru.ppr.cppk.logic.pdSale.loader.DepStationsLoader;
import ru.ppr.cppk.logic.pdSale.loader.DestStationsLoader;
import ru.ppr.cppk.logic.pdSale.loader.TariffPlansLoader;
import ru.ppr.cppk.logic.pdSale.loader.TariffsLoader;
import ru.ppr.cppk.logic.pdSale.loader.TicketTypesLoader;
import ru.ppr.cppk.managers.NsiVersionManager;
import ru.ppr.nsi.NsiDaoSession;
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
 * "Окружение" для процесса оформления ПД.
 * Предоставляет все ограничения, установленные для оформления ПД
 * и лоадеры, позволяющие получать возможные значения стацний, тарифов и т.п.
 *
 * @author Aleksandr Brazhkin
 */
public class PdSaleEnv {

    private final PdSaleRestrictions pdSaleRestrictions;
    private final DepStationsLoader depStationsLoader;
    private final DestStationsLoader destStationsLoader;
    private final TariffPlansLoader tariffPlansLoader;
    private final TicketTypesLoader ticketTypesLoader;
    private final TariffsLoader tariffsLoader;
    private final PdSaleEnvType pdSaleEnvType;

    PdSaleEnv(@NonNull NsiDaoSession nsiDaoSession,
              @NonNull RecentStationsStatistics recentStationsStatistics,
              @NonNull TicketTypesValidityTimesRepository ticketTypesValidityTimesRepository,
              @NonNull TicketTypeRepository ticketTypeRepository,
              @NonNull TariffRepository tariffRepository,
              @NonNull TariffPlanRepository tariffPlanRepository,
              @NonNull StationRepository stationRepository,
              @NonNull StationToTariffZoneRepository stationToTariffZoneRepository,
              @NonNull TrainCategoryRepository trainCategoryRepository,
              @NonNull StationTransferRouteRepository stationTransferRouteRepository,
              @NonNull TrainTicketTypeForTransferRegistrationRepository trainTicketTypeForTransferRegistrationRepository,
              @NonNull NsiVersionManager nsiVersionManager,
              @NonNull PdValidityPeriodCalculator pdValidityPeriodCalculator,
              @NonNull PdSaleEnvType pdSaleEnvType,
              @NonNull TicketTypeChecker ticketTypeChecker) {
        this.pdSaleRestrictions = new PdSaleRestrictions(
                nsiDaoSession,
                ticketTypesValidityTimesRepository,
                ticketTypeRepository,
                stationRepository,
                tariffPlanRepository,
                stationToTariffZoneRepository,
                tariffRepository,
                trainCategoryRepository,
                stationTransferRouteRepository,
                trainTicketTypeForTransferRegistrationRepository,
                nsiVersionManager,
                pdValidityPeriodCalculator,
                pdSaleEnvType, ticketTypeChecker);
        this.depStationsLoader = new DepStationsLoader(pdSaleRestrictions, recentStationsStatistics, stationRepository, tariffRepository);
        this.destStationsLoader = new DestStationsLoader(pdSaleRestrictions, recentStationsStatistics, stationRepository, tariffRepository);
        this.tariffPlansLoader = new TariffPlansLoader(pdSaleRestrictions, tariffPlanRepository, tariffRepository);
        this.ticketTypesLoader = new TicketTypesLoader(pdSaleRestrictions, ticketTypeRepository, tariffRepository);
        this.tariffsLoader = new TariffsLoader(pdSaleRestrictions, tariffRepository);
        this.pdSaleEnvType = pdSaleEnvType;
    }

    public PdSaleRestrictions pdSaleRestrictions() {
        return pdSaleRestrictions;
    }

    public DepStationsLoader depStationsLoader() {
        return depStationsLoader;
    }

    public DestStationsLoader destStationsLoader() {
        return destStationsLoader;
    }

    public TariffPlansLoader tariffPlansLoader() {
        return tariffPlansLoader;
    }

    public TicketTypesLoader ticketTypesLoader() {
        return ticketTypesLoader;
    }

    public TariffsLoader tariffsLoader() {
        return tariffsLoader;
    }

    public PdSaleEnvType getType() {
        return pdSaleEnvType;
    }
}
