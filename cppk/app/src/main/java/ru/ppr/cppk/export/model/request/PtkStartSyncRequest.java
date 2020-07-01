package ru.ppr.cppk.export.model.request;

import ru.ppr.cppk.export.model.StationDevice;

/**
 * Запрос на начало синхронизации с ПТК
 *
 * @author Grigoriy Kashka
 */
public class PtkStartSyncRequest {

    /**
     * Информация о терминале
     */
    public StationDevice device;

    @Override
    public String toString() {
        return "PtkStartSyncRequest{" +
                "device=" + device +
                '}';
    }
}
