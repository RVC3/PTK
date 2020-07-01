package ru.ppr.cppk.sync.loader.baseEntities;

import android.database.Cursor;

import ru.ppr.cppk.db.LocalDaoSession;
import ru.ppr.cppk.db.local.EventDao;
import ru.ppr.cppk.sync.kpp.baseEntities.Event;
import ru.ppr.cppk.sync.loader.base.BaseLoader;
import ru.ppr.cppk.sync.loader.base.Column;
import ru.ppr.cppk.sync.loader.base.Offset;
import ru.ppr.cppk.sync.loader.model.local.SoftwareVersionLoader;
import ru.ppr.cppk.sync.loader.model.StationDeviceLoader;
import ru.ppr.cppk.sync.loader.model.StationLoader;
import ru.ppr.nsi.NsiDaoSession;

/**
 * @author Aleksandr Brazhkin
 */
public class EventLoader extends BaseLoader {

    private final StationLoader stationLoader;
    private final StationDeviceLoader stationDeviceLoader;
    private final SoftwareVersionLoader softwareVersionLoader;

    public EventLoader(LocalDaoSession localDaoSession,
                       NsiDaoSession nsiDaoSession,
                       StationLoader stationLoader,
                       StationDeviceLoader stationDeviceLoader,
                       SoftwareVersionLoader softwareVersionLoader) {
        super(localDaoSession, nsiDaoSession);
        this.stationLoader = stationLoader;
        this.stationDeviceLoader = stationDeviceLoader;
        this.softwareVersionLoader = softwareVersionLoader;
    }

    public static class Columns {
        static final Column GUID = new Column(0, EventDao.Properties.Guid);
        static final Column CREATION_TIMESTAMP = new Column(1, EventDao.Properties.CreationTimestamp);
        static final Column VERSION_ID = new Column(2, EventDao.Properties.VersionId);
        static final Column SOFTWARE_UPDATE_EVENT_ID = new Column(3, EventDao.Properties.SoftwareUpdateEventId);
        static final Column STATION_DEVICE_ID = new Column(4, EventDao.Properties.StationDeviceId);
        static final Column STATION_CODE = new Column(5, EventDao.Properties.StationCode);

        public static Column[] all = new Column[]{
                GUID,
                CREATION_TIMESTAMP,
                VERSION_ID,
                SOFTWARE_UPDATE_EVENT_ID,
                STATION_DEVICE_ID,
                STATION_CODE
        };
    }

    public void fill(Event event, Cursor cursor, Offset offset) {
        event.Id = cursor.getString(offset.value + Columns.GUID.index);
        event.CreationTimestamp = cursor.getLong(offset.value + Columns.CREATION_TIMESTAMP.index);
        event.VersionId = cursor.getInt(offset.value + Columns.VERSION_ID.index);

        long stationCode = cursor.getLong(offset.value + Columns.STATION_CODE.index);
        long updateEventId = cursor.getLong(offset.value + Columns.SOFTWARE_UPDATE_EVENT_ID.index);

        event.SoftwareVersion = softwareVersionLoader.load(updateEventId);
        event.Station = stationLoader.loadStation(stationCode, event.VersionId);

        offset.value += Columns.all.length;
        event.Device = stationDeviceLoader.load(cursor, offset);
    }
}
