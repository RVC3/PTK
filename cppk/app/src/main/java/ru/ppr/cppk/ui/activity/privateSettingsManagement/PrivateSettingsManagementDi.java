package ru.ppr.cppk.ui.activity.privateSettingsManagement;

import android.support.annotation.NonNull;

import ru.ppr.core.manager.network.NetworkManager;
import ru.ppr.cppk.db.LocalDaoSession;
import ru.ppr.cppk.di.Di;
import ru.ppr.cppk.entity.settings.PrivateSettings;
import ru.ppr.cppk.managers.AppNetworkManager;

/**
 * @author Dmitry Nevolin
 */
class PrivateSettingsManagementDi {

    private final Di di;

    PrivateSettingsManagementDi(@NonNull Di di) {
        this.di = di;
    }

    @NonNull
    PrivateSettings privateSettings() {
        return di.getPrivateSettings().get();
    }

    @NonNull
    LocalDaoSession localDaoSession() {
        return di.getDbManager().getLocalDaoSession().get();
    }

    AppNetworkManager networkManager() {
        return di.networkManager();
    }

}
