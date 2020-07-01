package ru.ppr.cppk.db.local;

import android.content.ContentValues;
import android.database.Cursor;
import android.support.annotation.NonNull;

import ru.ppr.cppk.db.LocalDaoSession;
import ru.ppr.cppk.localdb.model.PaperUsage;
import ru.ppr.logger.Logger;

/**
 * DAO для таблицы локальной БД <i>PaperUsage</i>.
 *
 * @author Aleksandr Brazhkin
 */
public class PaperUsageDao extends BaseEntityDao<PaperUsage, Long> {

    private static String TAG = Logger.makeLogTag(PaperUsageDao.class);

    public static final String TABLE_NAME = "PaperUsage";

    public static class Properties {
        public static final String Id = "_id";
        public static final String PrevOdometerValue = "PrevOdometerValue";
        public static final String PaperLength = "PaperLength";
        public static final String IsRestarted = "IsRestarted";
    }

    public PaperUsageDao(@NonNull LocalDaoSession localDaoSession) {
        super(localDaoSession);
    }

    @Override
    public String getTableName() {
        return TABLE_NAME;
    }

    @Override
    public PaperUsage fromCursor(Cursor cursor) {
        PaperUsage paperUsage = new PaperUsage();
        paperUsage.setId(cursor.getLong(cursor.getColumnIndex(BaseEntityDao.Properties.Id)));
        paperUsage.setPrevOdometerValue(cursor.getLong(cursor.getColumnIndex(Properties.PrevOdometerValue)));
        paperUsage.setPaperLength(cursor.getLong(cursor.getColumnIndex(Properties.PaperLength)));
        paperUsage.setRestarted(cursor.getInt(cursor.getColumnIndex(Properties.IsRestarted)) == 1);
        return paperUsage;
    }

    @Override
    public ContentValues toContentValues(PaperUsage entity) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(Properties.Id, entity.getId());
        contentValues.put(Properties.PrevOdometerValue, entity.getPrevOdometerValue());
        contentValues.put(Properties.PaperLength, entity.getPaperLength());
        contentValues.put(Properties.IsRestarted, entity.isRestarted());
        return contentValues;
    }

    @Override
    public Long getKey(PaperUsage entity) {
        return entity.getId();
    }

    @Override
    public long insertOrThrow(@NonNull PaperUsage entity) {
        long id = super.insertOrThrow(entity);
        entity.setId(id);
        return id;
    }
}
