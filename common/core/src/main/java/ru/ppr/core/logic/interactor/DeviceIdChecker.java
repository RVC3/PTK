package ru.ppr.core.logic.interactor;

import javax.inject.Inject;

/**
 * Валидатор рассчитанного id устройства, записавшего СТУ.
 *
 * @author Aleksandr Brazhkin
 */
public class DeviceIdChecker {

    @Inject
    public DeviceIdChecker() {
    }

    public boolean isDeviceIdValid(long deviceId) {
        return deviceId > 0;
    }
}
