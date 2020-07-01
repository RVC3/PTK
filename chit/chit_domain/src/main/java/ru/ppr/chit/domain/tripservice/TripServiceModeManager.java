package ru.ppr.chit.domain.tripservice;

import javax.inject.Inject;
import javax.inject.Singleton;

import ru.ppr.chit.domain.model.local.ControlStation;
import ru.ppr.logger.Logger;

/**
 * Менеджер режимов обслуживания поезда.
 * Пока что их два:
 * - онлайн
 * - оффлайн
 *
 * @author Dmitry Nevolin
 */
@Singleton
public class TripServiceModeManager {

    private static final String TAG = Logger.makeLogTag(TripServiceModeManager.class);

    private final TripServiceManager tripServiceManager;
    private final TripServiceInfoStorage tripServiceInfoStorage;

    @Inject
    TripServiceModeManager(TripServiceManager tripServiceManager,
                           TripServiceInfoStorage tripServiceInfoStorage) {
        this.tripServiceManager = tripServiceManager;
        this.tripServiceInfoStorage = tripServiceInfoStorage;
    }

    /**
     * Определяет режим обслуживания поезда
     *
     * @return режим обслуживания поезда.
     */
    public TripServiceMode detectTripServiceMode() {
        if (!tripServiceManager.isTripServiceStarted()) {
            throw new IllegalStateException("Can't detect trip service mode without started trip service");
        }
        ControlStation controlStation = tripServiceInfoStorage.getControlStation();
        // Пока что единственный более-менее достоверный признак определить в каком режиме
        // работает приложение - это проверить наличие станции контроля, т.к. если её нет,
        // мы не можем создавать события посадки, соответственно не можем работать так, как
        // требует того онлайн режим. По номеру поезда определять не рекомендуется, т.к. возможно
        // что позже будет добавлен функционал ручного его ввода, со станциями нити поезда
        // такого случиться не должно.
        if (controlStation == null) {
            Logger.trace(TAG, "detectTripServiceMode: " + TripServiceMode.OFFLINE);
            return TripServiceMode.OFFLINE;
        } else {
            Logger.trace(TAG, "detectTripServiceMode: " + TripServiceMode.ONLINE);
            return TripServiceMode.ONLINE;
        }
    }

}
