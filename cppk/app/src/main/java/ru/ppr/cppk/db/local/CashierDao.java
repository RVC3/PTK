package ru.ppr.cppk.db.local;

import android.content.ContentValues;
import android.database.Cursor;
import android.support.annotation.NonNull;

import ru.ppr.cppk.db.LocalDaoSession;
import ru.ppr.cppk.entity.event.model.Cashier;
import ru.ppr.logger.Logger;

/**
 * DAO для таблицы локальной БД <i>Cashier</i>.
 *
 * @author Aleksandr Brazhkin
 */
public class CashierDao extends BaseEntityDao<Cashier, Long> {

    private static final String TAG = Logger.makeLogTag(CashierDao.class);

    public static final String TABLE_NAME = "Cashier";

    public static class Properties {
        public static final String Id = "_id";
        public static final String UserLogin = "UserLogin";
        public static final String OfficialCode = "OfficialCode";
        public static final String Fio = "Fio";
    }

    public CashierDao(LocalDaoSession localDaoSession) {
        super(localDaoSession);
    }

    @Override
    public String getTableName() {
        return TABLE_NAME;
    }

    @Override
    public Cashier fromCursor(Cursor cursor) {
        Cashier cashier = new Cashier();
        cashier.setId(cursor.getLong(cursor.getColumnIndex(Properties.Id)));
        cashier.setFio(cursor.getString(cursor.getColumnIndex(Properties.Fio)));
        cashier.setLogin(cursor.getString(cursor.getColumnIndex(Properties.UserLogin)));
        if (cashier.getLogin() != null) cashier.setLogin(cashier.getLogin().trim());
        cashier.setOfficialCode(cursor.getString(cursor.getColumnIndex(Properties.OfficialCode)));
        return cashier;
    }

    @Override
    public ContentValues toContentValues(Cashier entity) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(Properties.Fio, entity.getFio());
        String login = (entity.getLogin() == null) ? null : entity.getLogin().trim();
        contentValues.put(Properties.UserLogin, login);
        contentValues.put(Properties.OfficialCode, entity.getOfficialCode());
        return contentValues;
    }

    @Override
    public Long getKey(Cashier entity) {
        return entity.getId();
    }

    @Override
    public long insertOrThrow(@NonNull Cashier entity) {
        long id = super.insertOrThrow(entity);
        entity.setId(id);
        return id;
    }
}
