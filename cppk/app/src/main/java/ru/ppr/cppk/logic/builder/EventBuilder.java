package ru.ppr.cppk.logic.builder;

import android.support.annotation.NonNull;

import java.util.Date;
import java.util.UUID;

import ru.ppr.cppk.db.LocalDaoSession;
import ru.ppr.cppk.di.Dagger;
import ru.ppr.cppk.entity.event.base34.Event;
import ru.ppr.cppk.entity.settings.PrivateSettings;
import ru.ppr.cppk.localdb.model.UpdateEvent;
import ru.ppr.cppk.localdb.model.UpdateEventType;
import ru.ppr.cppk.localdb.repository.UpdateEventRepository;
import ru.ppr.cppk.managers.NsiVersionManager;

/**
 * Билдер сущности {@link Event}.
 *
 * @author Aleksandr Brazhkin
 */
public class EventBuilder {

    private final PrivateSettings privateSettings;
    private final LocalDaoSession localDaoSession;
    private final NsiVersionManager nsiVersionManager;
    private final UpdateEventRepository updateEventRepository;
    private long deviceId = -1;

    public EventBuilder(PrivateSettings privateSettings, LocalDaoSession localDaoSession, NsiVersionManager nsiVersionManager) {
        this.privateSettings = privateSettings;
        this.localDaoSession = localDaoSession;
        this.nsiVersionManager = nsiVersionManager;
        this.updateEventRepository = Dagger.appComponent().updateEventRepository();
    }

    public EventBuilder setDeviceId(long deviceId) {
        this.deviceId = deviceId;
        return this;
    }

    @NonNull
    public Event build() {
        Event event = new Event();
        event.setCreationTime(new Date());
        event.setUuid(UUID.randomUUID());
        event.setStationCode((long) privateSettings.getSaleStationCode());
        UpdateEvent updateEvent = updateEventRepository.getLastUpdateEvent(UpdateEventType.SW, false);
        event.setSoftwareUpdateEventId(updateEvent.getId());
        event.setDeviceId(deviceId);
        event.setVersionId(nsiVersionManager.getCurrentNsiVersionId());
        return event;
    }
}
