package ru.ppr.cppk.managers.db;

import ru.ppr.cppk.Holder;
import ru.ppr.cppk.db.LocalDaoSession;
import ru.ppr.cppk.di.Dagger;
import ru.ppr.cppk.di.Di;
import ru.ppr.cppk.entity.event.model.StationDevice;
import ru.ppr.cppk.entity.settings.PrivateSettings;
import ru.ppr.cppk.helpers.CommonSettingsStorage;
import ru.ppr.cppk.logic.ShiftManager;
import ru.ppr.cppk.logic.pdSale.PdSaleEnvFactory;
import ru.ppr.cppk.managers.PosManager;

/**
 * Слушатель событий обновления {@link LocalDaoSession}.
 * Пока этот слушатель один и делает все операции сам.
 * Данный факт - наследие прошлого.
 * Возможно, в будущем это будет организовано лучше.
 *
 * @author Aleksandr Brazhkin
 */
public class LocalDaoSessionUpdatesListener {

    public LocalDaoSessionUpdatesListener(
            LocalDbManager localDbManager,
            NsiDbManager nsiDbManager,
            SecurityDbManager securityDbManager,
            Holder<LocalDaoSession> localDaoSession,
            Holder<PrivateSettings> privateSettings,
            CommonSettingsStorage commonSettingsStorage,
            ShiftManager shiftManager,
            PosManager posManager,
            PdSaleEnvFactory pdSaleEnvFactory) {
        localDbManager
                .addDaoSessionResetListener(daoSession -> {
                    localDaoSession.set(daoSession);
                    privateSettings.set(Dagger.appComponent().privateSettingsRepository().getPrivateSettings());
                    commonSettingsStorage.clearCache();

                    pdSaleEnvFactory.reset();

                    boolean sqlLogsEnabled = commonSettingsStorage.get().isLogFullSQL();

                    localDbManager.setLogEnabled(sqlLogsEnabled);
                    nsiDbManager.setLogEnabled(sqlLogsEnabled);
                    securityDbManager.setLogEnabled(sqlLogsEnabled);

                    Di.INSTANCE.getDeviceSessionInfo().setCurrentStationDevice(StationDevice.getThisDevice());

                    shiftManager.refreshState();
                    posManager.refreshState();
                });
    }
}
