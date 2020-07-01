package ru.ppr.cppk.db.local;

import android.content.ContentValues;
import android.database.Cursor;
import android.support.annotation.NonNull;

import java.util.Date;

import ru.ppr.cppk.db.LocalDaoSession;
import ru.ppr.cppk.localdb.model.LogActionType;
import ru.ppr.cppk.localdb.model.LogEvent;
import ru.ppr.cppk.localdb.model.LogMessageType;
import ru.ppr.database.Database;
import ru.ppr.database.garbage.DBGarbageCollector;
import ru.ppr.database.garbage.base.GCOldDataRemovable;
import ru.ppr.logger.Logger;

/**
 * DAO для таблицы локальной БД <i>Log</i>.
 *
 * @author Aleksandr Brazhkin
 */
public class LogEventDao extends BaseEntityDao<LogEvent, Long> implements GCOldDataRemovable {

    private static final String TAG = Logger.makeLogTag(LogEventDao.class);

    public static final String TABLE_NAME = "Log";

    public static class Properties {
        public static final String Id = "_id";
        public static final String CreationTimestamp = "CreationTimestamp";
        public static final String MessageTypeCode = "MessageTypeCode";
        public static final String UserName = "UserName";
        public static final String ActionTypeCode = "ActionTypeCode";
        public static final String SecurityCardUID = "SecurityCardUID";
        public static final String Message = "Message";
    }

    public LogEventDao(LocalDaoSession localDaoSession) {
        super(localDaoSession);
    }

    @Override
    public String getTableName() {
        return TABLE_NAME;
    }

    @Override
    public LogEvent fromCursor(Cursor cursor) {
        LogEvent logEvent = new LogEvent();
        logEvent.setId(cursor.getLong(cursor.getColumnIndex(Properties.Id)));
        logEvent.setCreationTimestamp(new Date(cursor.getLong(cursor.getColumnIndex(Properties.CreationTimestamp))));
        logEvent.setActionType(LogActionType.valueOf(cursor.getInt(cursor.getColumnIndex(Properties.ActionTypeCode))));
        logEvent.setMessageType(LogMessageType.valueOf(cursor.getInt(cursor.getColumnIndex(Properties.MessageTypeCode))));
        logEvent.setMessage(cursor.getString(cursor.getColumnIndex(Properties.Message)));
        logEvent.setSecurityCardUid(cursor.getString(cursor.getColumnIndex(Properties.SecurityCardUID)));
        logEvent.setUserName(cursor.getString(cursor.getColumnIndex(Properties.UserName)));
        return logEvent;
    }

    @Override
    public ContentValues toContentValues(LogEvent entity) {
        ContentValues cv = new ContentValues();
        cv.put(Properties.CreationTimestamp, entity.getCreationTimestamp().getTime());
        cv.put(Properties.ActionTypeCode, entity.getActionType().getCode());
        cv.put(Properties.Message, entity.getMessage());
        cv.put(Properties.MessageTypeCode, entity.getMessageType().getCode());
        cv.put(Properties.SecurityCardUID, entity.getSecurityCardUid());
        cv.put(Properties.UserName, entity.getUserName());
        return cv;
    }

    @Override
    public Long getKey(LogEvent entity) {
        return entity.getId();
    }

    @Override
    public long insertOrThrow(@NonNull LogEvent entity) {
        long id = super.insertOrThrow(entity);
        entity.setId(id);
        return id;
    }

    @Override
    public void gcRemoveOldData(Database database, Date dateBefore) {
        // Удаляем из лога записи с датой создания меньше dateBefore
        StringBuilder sql = new StringBuilder();
        sql.append("delete from ").append(getTableName()).append(" where ").append(Properties.CreationTimestamp).append(" < ").append(dateBefore.getTime());

        Logger.info(DBGarbageCollector.TAG, this.getClass().getSimpleName() + ".gcRemoveOldData(): execute sql" + "\n" + sql.toString());
        database.execSQL(sql.toString());
    }

}