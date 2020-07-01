package ru.ppr.cppk.db.local;

import android.content.ContentValues;
import android.database.Cursor;
import android.support.annotation.NonNull;

import ru.ppr.cppk.db.LocalDaoSession;
import ru.ppr.cppk.entity.event.model.StationDevice;
import ru.ppr.database.Database;
import ru.ppr.database.garbage.base.GCNoLinkRemovable;
import ru.ppr.nsi.entity.DeviceType;

/**
 * DAO для таблицы локальной БД <i>StationDevice</i>.
 *
 * @author Aleksandr Brazhkin
 */
public class StationDeviceDao extends BaseEntityDao<StationDevice, Long> implements GCNoLinkRemovable {

    public static final String TABLE_NAME = "StationDevice";

    public static class Properties {
        public static final String DeviceId = "DeviceId";
        public static final String Model = "Model";
        public static final String SerialNumber = "SerialNumber";
        public static final String Type = "Type";
        public static final String ProductionSectionCode = "ProductionSectionCode";
    }

    public StationDeviceDao(LocalDaoSession localDaoSession) {
        super(localDaoSession);
    }

    @Override
    public String getTableName() {
        return TABLE_NAME;
    }

    @Override
    public StationDevice fromCursor(@NonNull final Cursor cursor) {
        StationDevice stationDevice = new StationDevice();
        stationDevice.setId(cursor.getLong(cursor.getColumnIndex(BaseEntityDao.Properties.Id)));
        stationDevice.setDeviceId(cursor.getLong(cursor.getColumnIndex(Properties.DeviceId)));
        stationDevice.setModel(cursor.getString(cursor.getColumnIndex(Properties.Model)));
        stationDevice.setSerialNumber(cursor.getString(cursor.getColumnIndex(Properties.SerialNumber)));
        stationDevice.setType(DeviceType.getType(cursor.getInt(cursor.getColumnIndex(Properties.Type))));
        stationDevice.setProductionSectionCode(cursor.getInt(cursor.getColumnIndex(Properties.ProductionSectionCode)));
        return stationDevice;
    }

    @Override
    public ContentValues toContentValues(@NonNull final StationDevice entity) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(StationDeviceDao.Properties.DeviceId, entity.getDeviceId());
        contentValues.put(StationDeviceDao.Properties.Model, entity.getModel());
        contentValues.put(StationDeviceDao.Properties.SerialNumber, entity.getSerialNumber());
        contentValues.put(StationDeviceDao.Properties.Type, entity.getType().getCode());
        contentValues.put(StationDeviceDao.Properties.ProductionSectionCode, entity.getProductionSectionCode());
        return contentValues;
    }

    @Override
    public Long getKey(StationDevice entity) {
        return entity.getId();
    }

    @Override
    public long insertOrThrow(@NonNull StationDevice entity) {
        long id = super.insertOrThrow(entity);
        entity.setId(id);
        return id;
    }

    @Override
    public boolean gcHandleNoLinkRemoveData(Database database) {
        // оставляем стандартный алгоритм сборщика мусора, удаляющий записи, на кторые нет ссылок
        return false;
    }

}
