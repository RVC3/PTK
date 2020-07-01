package ru.ppr.cppk.ui.activity.commonSettingsManagement;

import android.support.annotation.NonNull;

import ru.ppr.cppk.db.LocalDaoSession;
import ru.ppr.cppk.di.Dagger;
import ru.ppr.cppk.di.Di;
import ru.ppr.cppk.entity.settings.CommonSettings;

/**
 * @author Dmitry Nevolin
 */
class CommonSettingsManagementDi {

    private final Di di;

    CommonSettingsManagementDi(@NonNull Di di) {
        this.di = di;
    }

    @NonNull
    CommonSettings commonSettings() {
        return Dagger.appComponent().commonSettingsStorage().get();
    }

    @NonNull
    LocalDaoSession localDaoSession() {
        return di.getDbManager().getLocalDaoSession().get();
    }

}
