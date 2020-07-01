package ru.ppr.cppk.helpers;

import ru.ppr.cppk.entity.event.model.StationDevice;

/**
 * In-memory хранилище информации о ПТК.
 *
 * @author Aleksandr Brazhkin
 */
public class DeviceSessionInfo {

    /**
     * Информация о ПТК
     */
    private StationDevice currentStationDevice;

    public StationDevice getCurrentStationDevice() {
        return currentStationDevice;
    }

    public void setCurrentStationDevice(StationDevice currentStationDevice) {
        this.currentStationDevice = currentStationDevice;
    }
}
