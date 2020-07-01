package ru.ppr.cppk.ui.fragment.fineSalePreparation;

import android.support.annotation.NonNull;

import ru.ppr.cppk.db.LocalDaoSession;
import ru.ppr.cppk.di.Dagger;
import ru.ppr.cppk.di.Di;
import ru.ppr.cppk.entity.settings.PrivateSettings;
import ru.ppr.cppk.helpers.UiThread;
import ru.ppr.cppk.logic.CriticalNsiChecker;
import ru.ppr.cppk.managers.NsiVersionManager;
import ru.ppr.cppk.managers.PrinterManager;
import ru.ppr.nsi.NsiDaoSession;
import ru.ppr.nsi.repository.FineRepository;

/**
 * @author Aleksandr Brazhkin
 */
class FineSalePreparationDi {

    private final Di di;

    FineSalePreparationDi(Di di) {
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

    PrinterManager printerManager() {
        return di.printerManager();
    }

    NsiVersionManager nsiVersionManager() {
        return di.nsiVersionManager();
    }

    CriticalNsiChecker criticalNsiVersionChecker() {
        return new CriticalNsiChecker(di.nsiVersionManager(), di.getUserSessionInfo(), di.permissionChecker());
    }

    @NonNull
    FineRepository fineRepository() {
        return Dagger.appComponent().fineRepository();
    }

}
