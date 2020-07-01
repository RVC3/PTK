package ru.ppr.cppk.db.local;

import android.content.ContentValues;
import android.database.Cursor;
import android.support.annotation.NonNull;

import ru.ppr.cppk.db.LocalDaoSession;
import ru.ppr.cppk.entity.event.model.LegalEntity;
import ru.ppr.database.Database;
import ru.ppr.database.garbage.base.GCNoLinkRemovable;
import ru.ppr.logger.Logger;

/**
 * DAO для таблицы локальной БД <i>LegalEntity</i>.
 *
 * @author Aleksandr Brazhkin
 */
public class LegalEntityDao extends BaseEntityDao<LegalEntity, Long> implements GCNoLinkRemovable {

    private static final String TAG = Logger.makeLogTag(LegalEntityDao.class);

    public static final String TABLE_NAME = "LegalEntity";

    public static class Properties {
        public static final String Id = "_id";
        public static final String Code = "Code";
        public static final String INN = "INN";
        public static final String Name = "Name";
    }

    public LegalEntityDao(LocalDaoSession localDaoSession) {
        super(localDaoSession);
    }

    @Override
    public String getTableName() {
        return TABLE_NAME;
    }

    @Override
    public LegalEntity fromCursor(Cursor cursor) {
        LegalEntity legalEntity = new LegalEntity();
        legalEntity.setId(cursor.getLong(cursor.getColumnIndex(Properties.Id)));
        legalEntity.setCode(cursor.getString(cursor.getColumnIndex(Properties.Code)));
        legalEntity.setInn(cursor.getString(cursor.getColumnIndex(Properties.INN)));
        legalEntity.setName(cursor.getString(cursor.getColumnIndex(Properties.Name)));
        return legalEntity;
    }

    @Override
    public ContentValues toContentValues(LegalEntity entity) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(Properties.INN, entity.getInn());
        contentValues.put(Properties.Code, entity.getCode());
        contentValues.put(Properties.Name, entity.getName());
        return contentValues;
    }

    @Override
    public Long getKey(LegalEntity entity) {
        return entity.getId();
    }

    @Override
    public long insertOrThrow(@NonNull LegalEntity entity) {
        long id = super.insertOrThrow(entity);
        entity.setId(id);
        return id;
    }

    @Override
    public boolean gcHandleNoLinkRemoveData(Database database) {
        // реализация сборщика мусора по умолчани.
        return false;
    }

}
