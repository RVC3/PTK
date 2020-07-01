package ru.ppr.core.ui.helper.crashreporter;

/**
 * Информация о состоянии устройства.
 *
 * @author Aleksandr Brazhkin
 */
final class DeviceStateInfo {

    private final long availableInternalMemorySize;
    private final long totalInternalMemorySize;

    DeviceStateInfo(long availableInternalMemorySize, long totalInternalMemorySize) {
        this.availableInternalMemorySize = availableInternalMemorySize;
        this.totalInternalMemorySize = totalInternalMemorySize;
    }

    long getAvailableInternalMemorySize() {
        return availableInternalMemorySize;
    }

    long getTotalInternalMemorySize() {
        return totalInternalMemorySize;
    }
}
