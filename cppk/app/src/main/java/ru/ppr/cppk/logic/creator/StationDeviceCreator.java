package ru.ppr.cppk.logic.creator;

import android.support.annotation.NonNull;

import com.google.common.base.Preconditions;

import javax.inject.Inject;

import ru.ppr.cppk.db.LocalDaoSession;
import ru.ppr.cppk.entity.event.model.StationDevice;
import ru.ppr.cppk.helpers.DeviceSessionInfo;
import ru.ppr.cppk.localdb.repository.base.LocalDbTransaction;

/**
 * Класс, выполняющий сборку {@link StationDevice} и запись его в БД.
 *
 * @author Aleksandr Brazhkin
 */
public class StationDeviceCreator {

    private final DeviceSessionInfo deviceSessionInfo;
    private final LocalDaoSession localDaoSession;
    private final LocalDbTransaction localDbTransaction;

    @Inject
    StationDeviceCreator(DeviceSessionInfo deviceSessionInfo, LocalDaoSession localDaoSession, LocalDbTransaction localDbTransaction) {
        this.deviceSessionInfo = deviceSessionInfo;
        this.localDaoSession = localDaoSession;
        this.localDbTransaction = localDbTransaction;
    }

    /**
     * Выполнят сборку {@link StationDevice} и запись его в БД.
     *
     * @return Сформированный {@link StationDevice}
     */
    @NonNull
    public StationDevice create() {
        return localDbTransaction.runInTx(this::createInternal);
    }

    @NonNull
    private StationDevice createInternal() {
        StationDevice stationDevice = deviceSessionInfo.getCurrentStationDevice();
        Preconditions.checkNotNull(stationDevice);
        // Пишем в БД StationDevice
        localDaoSession.getStationDeviceDao().insertOrThrow(stationDevice);
        return stationDevice;
    }
}
