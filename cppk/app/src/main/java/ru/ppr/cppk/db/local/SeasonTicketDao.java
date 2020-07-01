package ru.ppr.cppk.db.local;

import android.content.ContentValues;
import android.database.Cursor;
import android.support.annotation.NonNull;

import ru.ppr.cppk.db.LocalDaoSession;
import ru.ppr.cppk.entity.event.model34.SeasonTicket;
import ru.ppr.database.Database;
import ru.ppr.database.garbage.base.GCNoLinkRemovable;
import ru.ppr.logger.Logger;

/**
 * DAO для таблицы локальной БД <i>SeasonTicket</i>.
 *
 * @author Aleksandr Brazhkin
 */
public class SeasonTicketDao extends BaseEntityDao<SeasonTicket, Long> implements GCNoLinkRemovable {

    private static final String TAG = Logger.makeLogTag(SeasonTicketDao.class);

    public static final String TABLE_NAME = "SeasonTicket";

    public static class Properties {
        public static final String Id = "_id";
        public static final String PassCount = "PassCount";
        public static final String PassLeftCount = "PassLeftCount";
        public static final String MonthDays = "MonthDays";
    }

    public SeasonTicketDao(LocalDaoSession localDaoSession) {
        super(localDaoSession);
    }

    @Override
    public String getTableName() {
        return TABLE_NAME;
    }

    @Override
    public SeasonTicket fromCursor(Cursor cursor) {
        SeasonTicket out = new SeasonTicket();
        out.setId(cursor.getInt(cursor.getColumnIndex(Properties.Id)));
        out.setPassCount(cursor.getInt(cursor.getColumnIndex(Properties.PassCount)));
        out.setPassLeftCount(cursor.getInt(cursor.getColumnIndex(Properties.PassLeftCount)));
        out.setMonthDays(cursor.getString(cursor.getColumnIndex(Properties.MonthDays)));
        return out;
    }

    @Override
    public ContentValues toContentValues(SeasonTicket entity) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(Properties.MonthDays, entity.getMonthDays());
        contentValues.put(Properties.PassCount, entity.getPassCount());
        contentValues.put(Properties.PassLeftCount, entity.getPassLeftCount());
        return contentValues;
    }

    @Override
    public Long getKey(SeasonTicket entity) {
        return entity.getId();
    }

    @Override
    public long insertOrThrow(@NonNull SeasonTicket entity) {
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
