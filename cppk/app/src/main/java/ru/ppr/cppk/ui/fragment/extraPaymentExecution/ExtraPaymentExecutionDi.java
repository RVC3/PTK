package ru.ppr.cppk.ui.fragment.extraPaymentExecution;

import ru.ppr.cppk.db.LocalDaoSession;
import ru.ppr.cppk.di.Dagger;
import ru.ppr.cppk.di.Di;
import ru.ppr.cppk.entity.settings.CommonSettings;
import ru.ppr.cppk.entity.settings.PrivateSettings;
import ru.ppr.cppk.helpers.UiThread;
import ru.ppr.cppk.logic.CriticalNsiChecker;
import ru.ppr.cppk.logic.pdSale.PdSaleEnvFactory;
import ru.ppr.cppk.logic.pdSale.PdSaleRestrictionsParams;
import ru.ppr.cppk.logic.pdSale.PdSaleRestrictionsParamsBuilder;
import ru.ppr.cppk.managers.NsiVersionManager;
import ru.ppr.nsi.NsiDaoSession;
import ru.ppr.nsi.repository.ExemptionGroupRepository;
import ru.ppr.nsi.repository.ExemptionRepository;
import ru.ppr.nsi.repository.StationRepository;
import ru.ppr.nsi.repository.StationToTariffZoneRepository;
import ru.ppr.nsi.repository.TariffPlanRepository;
import ru.ppr.nsi.repository.TariffRepository;
import ru.ppr.nsi.repository.TicketTypeRepository;
import ru.ppr.nsi.repository.TrainCategoryRepository;

/**
 * @author Aleksandr Brazhkin
 */
class ExtraPaymentExecutionDi {

    private final Di di;

    ExtraPaymentExecutionDi(Di di) {
        this.di = di;
    }

    UiThread uiThread() {
        return di.uiThread();
    }

    LocalDaoSession localDaoSession() {
        return di.getDbManager().getLocalDaoSession().get();
    }

    NsiDaoSession nsiDaoSession() {
        return di.getDbManager().getNsiDaoSession().get();
    }

    PrivateSettings privateSettings() {
        return di.getPrivateSettings().get();
    }

    CommonSettings commonSettings() {
        return Dagger.appComponent().commonSettingsStorage().get();
    }

    NsiVersionManager nsiVersionManager() {
        return di.nsiVersionManager();
    }

    CriticalNsiChecker criticalNsiVersionChecker() {
        return new CriticalNsiChecker(di.nsiVersionManager(), di.getUserSessionInfo(), di.permissionChecker());
    }

    TariffRepository tariffRepository() {
        return Dagger.appComponent().tariffRepository();
    }

    TariffPlanRepository tariffPlanRepository() {
        return Dagger.appComponent().tariffPlanRepository();
    }

    TrainCategoryRepository trainCategoryRepository() {
        return Dagger.appComponent().trainCategoryRepository();
    }

    TicketTypeRepository ticketTypeRepository() {
        return Dagger.appComponent().ticketTypeRepository();
    }

    ExemptionRepository exemptionRepository() {
        return Dagger.appComponent().exemptionRepository();
    }

    ExemptionGroupRepository exemptionGroupRepository() {
        return Dagger.appComponent().exemptionGroupRepository();
    }

    PdSaleEnvFactory pdSaleEnvFactory() {
        return Dagger.appComponent().pdSaleEnvFactory();
    }

    PdSaleRestrictionsParamsBuilder pdSaleRestrictionsParamsBuilder() {
        return Dagger.appComponent().pdSaleRestrictionsParamsBuilder();
    }

}
