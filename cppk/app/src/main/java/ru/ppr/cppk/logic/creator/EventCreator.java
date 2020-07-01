package ru.ppr.cppk.logic.creator;

import android.support.annotation.NonNull;

import java.util.Date;
import java.util.UUID;

import javax.inject.Inject;

import ru.ppr.cppk.db.LocalDaoSession;
import ru.ppr.cppk.di.Dagger;
import ru.ppr.cppk.entity.event.base34.Event;
import ru.ppr.cppk.entity.event.model.StationDevice;
import ru.ppr.cppk.entity.settings.PrivateSettings;
import ru.ppr.cppk.localdb.model.UpdateEvent;
import ru.ppr.cppk.localdb.model.UpdateEventType;
import ru.ppr.cppk.localdb.repository.UpdateEventRepository;
import ru.ppr.cppk.localdb.repository.base.LocalDbTransaction;
import ru.ppr.cppk.managers.NsiVersionManager;

/**
 * Класс, выполняющий сборку {@link Event} и запись его в БД.
 *
 * @author Aleksandr Brazhkin
 */
public class EventCreator {

    private final PrivateSettings privateSettings;
    private final LocalDaoSession localDaoSession;
    private final NsiVersionManager nsiVersionManager;
    private final UpdateEventRepository updateEventRepository;
    private final LocalDbTransaction localDbTransaction;
    private final StationDeviceCreator stationDeviceCreator;

    @Inject
    EventCreator(PrivateSettings privateSettings,
                 LocalDaoSession localDaoSession,
                 NsiVersionManager nsiVersionManager,
                 LocalDbTransaction localDbTransaction,
                 StationDeviceCreator stationDeviceCreator) {
        this.privateSettings = privateSettings;
        this.localDaoSession = localDaoSession;
        this.nsiVersionManager = nsiVersionManager;
        this.localDbTransaction = localDbTransaction;
        this.stationDeviceCreator = stationDeviceCreator;
        this.updateEventRepository = Dagger.appComponent().updateEventRepository();
    }

    /**
     * Выполнят сборку {@link Event} и запись его в БД.
     *
     * @return Сформированный {@link Event}
     */
    @NonNull
    public Event create() {
        return localDbTransaction.runInTx(this::createInternal);
    }

    @NonNull
    private Event createInternal() {
        UpdateEvent updateEvent = updateEventRepository.getLastUpdateEvent(UpdateEventType.SW, false);

        // Пишем в БД StationDevice
        StationDevice stationDevice = stationDeviceCreator.create();

        Event event = new Event();
        event.setCreationTime(new Date());
        event.setUuid(UUID.randomUUID());
        event.setStationCode((long) privateSettings.getSaleStationCode());
        event.setSoftwareUpdateEventId(updateEvent.getId());
        event.setDeviceId(stationDevice.getId());
        event.setVersionId(nsiVersionManager.getCurrentNsiVersionId());

        // Пишем в БД Event
        localDaoSession.getEventDao().insertOrThrow(event);
        return event;
    }
}
