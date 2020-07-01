package ru.ppr.cppk.ui.fragment.pdSalePreparation;

import ru.ppr.cppk.di.Dagger;
import ru.ppr.cppk.di.Di;
import ru.ppr.cppk.entity.settings.PrivateSettings;
import ru.ppr.cppk.helpers.UiThread;
import ru.ppr.cppk.logic.CriticalNsiChecker;
import ru.ppr.cppk.logic.TicketStorageTypeToTicketTypeChecker;
import ru.ppr.cppk.logic.pdSale.PdSaleEnvFactory;
import ru.ppr.cppk.logic.pdSale.PdSaleRestrictionsParamsBuilder;
import ru.ppr.cppk.managers.NsiVersionManager;
import ru.ppr.cppk.managers.PrinterManager;
import ru.ppr.nsi.NsiDaoSession;
import ru.ppr.nsi.repository.ExemptionGroupRepository;
import ru.ppr.nsi.repository.ExemptionRepository;
import ru.ppr.nsi.repository.StationRepository;
import ru.ppr.nsi.repository.TrainCategoryRepository;

/**
 * @author Aleksandr Brazhkin
 */
class PdSalePreparationDi {

    private final Di di;

    PdSalePreparationDi(Di di) {
        this.di = di;
    }

    UiThread uiThread() {
        return di.uiThread();
    }

    NsiDaoSession nsiDaoSession() {
        return di.getDbManager().getNsiDaoSession().get();
    }

    PrivateSettings privateSettings() {
        return di.getPrivateSettings().get();
    }

    PdSaleRestrictionsParamsBuilder pdSaleRestrictionsParamsBuilder(){
        return Dagger.appComponent().pdSaleRestrictionsParamsBuilder();
    }

    PrinterManager printerManager() {
        return di.printerManager();
    }

    TicketStorageTypeToTicketTypeChecker ticketStorageTypeToTicketTypeChecker() {
        return new TicketStorageTypeToTicketTypeChecker(di.nsiVersionManager(), Dagger.appComponent().ticketTypeRepository());
    }

    PdSaleEnvFactory pdSaleEnvFactory() {
        return Dagger.appComponent().pdSaleEnvFactory();
    }

    NsiVersionManager nsiVersionManager() {
        return di.nsiVersionManager();
    }

    CriticalNsiChecker criticalNsiVersionChecker() {
        return new CriticalNsiChecker(di.nsiVersionManager(), di.getUserSessionInfo(), di.permissionChecker());
    }

    TrainCategoryRepository trainCategoryRepository() {
        return Dagger.appComponent().trainCategoryRepository();
    }

    StationRepository stationRepository() {
        return Dagger.appComponent().stationRepository();
    }

    ExemptionRepository exemptionRepository(){
        return Dagger.appComponent().exemptionRepository();
    }

    ExemptionGroupRepository exemptionGroupRepository(){
        return Dagger.appComponent().exemptionGroupRepository();
    }

}
