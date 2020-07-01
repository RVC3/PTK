package ru.ppr.cppk.db.local;

import android.content.ContentValues;
import android.database.Cursor;
import android.support.annotation.NonNull;

import java.util.Date;

import ru.ppr.cppk.localdb.impl.mapper.LongDateMapper;
import ru.ppr.cppk.localdb.impl.mapper.UpdateEventTypeMapper;
import ru.ppr.cppk.db.LocalDaoSession;
import ru.ppr.cppk.localdb.model.UpdateEvent;
import ru.ppr.cppk.localdb.model.UpdateEventType;
import ru.ppr.database.Database;
import ru.ppr.database.garbage.DBGarbageCollector;
import ru.ppr.database.garbage.base.GCDeletedMarkSupported;
import ru.ppr.database.garbage.base.GCNoLinkRemovable;
import ru.ppr.database.garbage.base.GCOldDataRemovable;
import ru.ppr.logger.Logger;

/**
 * DAO для таблицы локальной БД <i>UpdateEvent</i>.
 *
 * @author Aleksandr Brazhkin
 */
public class UpdateEventDao extends BaseEntityDao<UpdateEvent, Long> implements GCNoLinkRemovable, GCOldDataRemovable, GCDeletedMarkSupported {

    public static final String TABLE_NAME = "UpdateEvent";

    public static class Properties {
        public static final String OperationTime = "OperationTime";
        public static final String UpdateSubject = "UpdateSubject";
        public static final String Version = "Version";
        public static final String DataContractVersion = "DataContractVersion";
        public static final String DeletedMark = "DeletedMark";
    }

    private final LongDateMapper longDateMapper = LongDateMapper.INSTANCE;
    private final UpdateEventTypeMapper updateEventTypeMapper = UpdateEventTypeMapper.INSTANCE;

    public UpdateEventDao(@NonNull LocalDaoSession localDaoSession) {
        super(localDaoSession);
    }

    @Override
    public String getTableName() {
        return TABLE_NAME;
    }

    @Override
    public UpdateEvent fromCursor(Cursor cursor) {
        UpdateEvent entity = new UpdateEvent();
        entity.setId(cursor.getLong(cursor.getColumnIndex(BaseEntityDao.Properties.Id)));
        entity.setOperationTime(longDateMapper.entityToModel(cursor.getLong(cursor.getColumnIndex(Properties.OperationTime))));
        entity.setType(updateEventTypeMapper.entityToModel(cursor.getInt(cursor.getColumnIndex(Properties.UpdateSubject))));
        entity.setVersion(cursor.getString(cursor.getColumnIndex(Properties.Version)));
        entity.setDataContractVersion(cursor.getInt(cursor.getColumnIndex(Properties.DataContractVersion)));
        return entity;
    }

    @Override
    public ContentValues toContentValues(UpdateEvent entity) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(Properties.OperationTime, longDateMapper.modelToEntity(entity.getOperationTime()));
        contentValues.put(Properties.UpdateSubject, updateEventTypeMapper.modelToEntity(entity.getType()));
        contentValues.put(Properties.Version, entity.getVersion());
        contentValues.put(Properties.DataContractVersion, entity.getDataContractVersion());
        return contentValues;
    }

    @Override
    public Long getKey(UpdateEvent entity) {
        return entity.getId();
    }

    @Override
    public long insertOrThrow(@NonNull UpdateEvent entity) {
        long id = super.insertOrThrow(entity);
        entity.setId(id);
        return id;
    }

    @Override
    public void gcRemoveOldData(Database database, Date dateBefore) {
        // Удаляем, если дата операции меньше dateBefore и тип обновления НЕ "ПО"
        // события обновления ПО могут висеть очень долго и их можно удалять, только когда на них больше нет ссылок в таблице Events
        StringBuilder sql = new StringBuilder();
        sql.append("update ").append(getTableName()).append(" set ").append(Properties.DeletedMark).append(" = 1 ")
                .append("where ").append(Properties.OperationTime).append(" < ").append(dateBefore.getTime())
                .append(" and ").append(Properties.UpdateSubject).append(" != ").append(UpdateEventType.SW.getCode());

        Logger.info(DBGarbageCollector.TAG, this.getClass().getSimpleName() + ".gcRemoveOldData(): execute sql" + "\n" + sql.toString());
        database.execSQL(sql.toString());
    }

    @Override
    public String getDeletedMarkField() {
        return Properties.DeletedMark;
    }

    @Override
    public boolean gcHandleNoLinkRemoveData(Database database) {
        // Переопределяем стандартный алгоритм
        // Удаляем события обновления ПО, на которые уже нет ссылок в событиях (EventDao)
        String masterFieldAlis = getIdWithTableName();
        String referenceFieldAlias = EventDao.TABLE_NAME + "." + EventDao.Properties.SoftwareUpdateEventId;

        StringBuilder sql = new StringBuilder();
        sql.append("delete from ").append(getTableName()).append("\n")
                .append(" where ").append(Properties.UpdateSubject).append(" = ").append(UpdateEventType.SW.getCode())
                .append(" and not exists( select ").append(referenceFieldAlias).append(" from ").append(EventDao.TABLE_NAME)
                .append(" where ").append(referenceFieldAlias).append(" = ").append(masterFieldAlis).append(" )");

        Logger.info(DBGarbageCollector.TAG, this.getClass().getSimpleName() + ".gcHandleNoLinkRemoveData(): execute sql" + "\n" + sql.toString());
        database.execSQL(sql.toString());

        return true;
    }

}
