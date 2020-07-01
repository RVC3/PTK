package ru.ppr.cppk.ui.activity.fineListManagement;

import android.support.annotation.NonNull;

import ru.ppr.cppk.db.LocalDaoSession;
import ru.ppr.cppk.di.Dagger;
import ru.ppr.cppk.di.Di;
import ru.ppr.cppk.entity.settings.PrivateSettings;
import ru.ppr.cppk.logic.PermissionChecker;
import ru.ppr.cppk.managers.NsiVersionManager;
import ru.ppr.nsi.NsiDaoSession;
import ru.ppr.nsi.repository.FineRepository;

/**
 * @author Dmitry Nevolin
 */
class FineListManagementDi {

    private final Di di;

    FineListManagementDi(@NonNull Di di) {
        this.di = di;
    }

    @NonNull
    LocalDaoSession localDaoSession() {
        return di.getDbManager().getLocalDaoSession().get();
    }

    @NonNull
    NsiDaoSession nsiDaoSession() {
        return di.getDbManager().getNsiDaoSession().get();
    }

    @NonNull
    PrivateSettings privateSettings() {
        return di.getPrivateSettings().get();
    }

    @NonNull
    PermissionChecker permissionChecker() {
        return di.permissionChecker();
    }

    @NonNull
    NsiVersionManager nsiVersionManager() {
        return di.nsiVersionManager();
    }

    @NonNull
    FineRepository fineRepository() {
        return Dagger.appComponent().fineRepository();
    }

}
