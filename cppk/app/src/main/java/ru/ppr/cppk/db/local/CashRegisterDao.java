package ru.ppr.cppk.db.local;

import android.content.ContentValues;
import android.database.Cursor;
import android.provider.BaseColumns;
import android.support.annotation.NonNull;

import ru.ppr.cppk.db.LocalDaoSession;
import ru.ppr.cppk.localdb.model.CashRegister;
import ru.ppr.logger.Logger;

/**
 * DAO для таблицы локальной БД <i>CashRegister</i>.
 *
 * @author Aleksandr Brazhkin
 */
public class CashRegisterDao extends BaseEntityDao<CashRegister, Long> {

    private static final String TAG = Logger.makeLogTag(CashRegisterDao.class);

    public static final String TABLE_NAME = "CashRegister";

    public static class Properties {
        public static final String Id = BaseColumns._ID;
        public static final String Inn = "INN";
        public static final String EklzNumber = "EKLZNumber";
        public static final String FnSerial = "FNSerial";
        public static final String SerialNumber = "SerialNumber";
        public static final String Model = "Model";
    }

    public CashRegisterDao(LocalDaoSession localDaoSession) {
        super(localDaoSession);
    }

    @Override
    public String getTableName() {
        return TABLE_NAME;
    }

    @Override
    public CashRegister fromCursor(Cursor cursor) {
        CashRegister out = new CashRegister();
        out.setId(cursor.getLong(cursor.getColumnIndex(Properties.Id)));
        out.setINN(cursor.getString(cursor.getColumnIndex(Properties.Inn)));
        out.setEKLZNumber(cursor.getString(cursor.getColumnIndex(Properties.EklzNumber)));
        out.setFNSerial(cursor.getString(cursor.getColumnIndex(Properties.FnSerial)));
        out.setModel(cursor.getString(cursor.getColumnIndex(Properties.Model)));
        out.setSerialNumber(cursor.getString(cursor.getColumnIndex(Properties.SerialNumber)));
        return out;
    }

    @Override
    public ContentValues toContentValues(CashRegister entity) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(Properties.Inn, entity.getINN());
        contentValues.put(Properties.EklzNumber, entity.getEKLZNumber());
        contentValues.put(Properties.FnSerial, entity.getFNSerial());
        contentValues.put(Properties.Model, entity.getModel());
        contentValues.put(Properties.SerialNumber, entity.getSerialNumber());
        return contentValues;
    }

    @Override
    public Long getKey(CashRegister entity) {
        return entity.getId();
    }

    @Override
    public long insertOrThrow(@NonNull CashRegister entity) {
        long id = super.insertOrThrow(entity);
        entity.setId(id);
        return id;
    }
}
