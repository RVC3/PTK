package ru.ppr.cppk.db.local;

import android.content.ContentValues;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import ru.ppr.cppk.db.LocalDaoSession;
import ru.ppr.cppk.localdb.model.LocalDbVersion;
import ru.ppr.logger.Logger;

/**
 * DAO для таблицы локальной БД <i>Versions</i>.
 *
 * @author Aleksandr Brazhkin
 */
public class LocalDbVersionDao extends BaseEntityDao<LocalDbVersion, Long> {

    private static final String TAG = Logger.makeLogTag(LocalDbVersionDao.class);

    public static final String TABLE_NAME = "Versions";

    public static class Properties {
        public static final String VersionId = "VersionId";
        public static final String CreatedDateTime = "CreatedDateTime";
        public static final String Description = "Description";
    }

    public LocalDbVersionDao(LocalDaoSession localDaoSession) {
        super(localDaoSession);
    }

    @Override
    public String getTableName() {
        return TABLE_NAME;
    }

    @Override
    public LocalDbVersion fromCursor(Cursor cursor) {
        LocalDbVersion entity = new LocalDbVersion();
        entity.setId(cursor.getLong(cursor.getColumnIndex(Properties.VersionId)));
        entity.setCreatedDateTime(stringToDate(cursor.getString(cursor.getColumnIndex(Properties.CreatedDateTime))));
        entity.setDescription(cursor.getString(cursor.getColumnIndex(Properties.Description)));
        return entity;
    }

    @Override
    public ContentValues toContentValues(LocalDbVersion entity) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(Properties.VersionId, entity.getId());
        contentValues.put(Properties.CreatedDateTime, dateToString(entity.getCreatedDateTime()));
        contentValues.put(Properties.Description, entity.getDescription());
        return contentValues;
    }

    @Override
    public Long getKey(LocalDbVersion entity) {
        return entity.getId();
    }

    @Override
    public long insertOrThrow(@NonNull LocalDbVersion entity) {
        long id = super.insertOrThrow(entity);
        entity.setId(id);
        return id;
    }

    @Nullable
    Date stringToDate(String dateStr) {
        try {
            return dateStr == null ? null : dateFormatter.get().parse(dateStr);
        } catch (ParseException e) {
            Logger.error(TAG, e);
            return null;
        }
    }

    @Nullable
    String dateToString(@Nullable Date date) {
        return date == null ? null : dateFormatter.get().format(date);
    }

    private final ThreadLocal<SimpleDateFormat> dateFormatter = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault());
        }
    };
}
