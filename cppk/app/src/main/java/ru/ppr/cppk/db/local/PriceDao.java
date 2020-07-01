package ru.ppr.cppk.db.local;

import android.content.ContentValues;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.util.Log;

import java.math.BigDecimal;

import ru.ppr.cppk.db.LocalDaoSession;
import ru.ppr.cppk.localdb.model.Price;
import ru.ppr.database.Database;
import ru.ppr.database.garbage.base.GCNoLinkRemovable;
import ru.ppr.logger.Logger;

/**
 * DAO для таблицы локальной БД <i>Price</i>.
 *
 * @author Aleksandr Brazhkin
 */
public class PriceDao extends BaseEntityDao<Price, Long> implements GCNoLinkRemovable {

    private static String TAG = Logger.makeLogTag(PriceDao.class);

    public static final String TABLE_NAME = "Price";

    public static class Properties {
        public static final String Full = "Full";
        public static final String Nds = "Nds";
        public static final String Payed = "Payed";
        public static final String SummForReturn = "SummForReturn";
    }

    public PriceDao(@NonNull LocalDaoSession localDaoSession) {
        super(localDaoSession);
    }

    @Override
    public String getTableName() {
        return TABLE_NAME;
    }

    @Override
    public Price fromCursor(Cursor cursor) {
        Price price = new Price();
        price.setId(cursor.getLong(cursor.getColumnIndex(BaseEntityDao.Properties.Id)));
        price.setFull(new BigDecimal(cursor.getString(cursor.getColumnIndex(Properties.Full))));
        price.setPayed(new BigDecimal(cursor.getString(cursor.getColumnIndex(Properties.Payed))));
        price.setSumForReturn(new BigDecimal(cursor.getString(cursor.getColumnIndex(Properties.SummForReturn))));
        price.setNds(new BigDecimal(cursor.getString(cursor.getColumnIndex(Properties.Nds))));
        return price;
    }

    @Override
    public ContentValues toContentValues(Price entity) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(Properties.Full, entity.getFull().toPlainString());
        contentValues.put(Properties.Payed, entity.getPayed().toPlainString());
        contentValues.put(Properties.SummForReturn, entity.getSumForReturn().toPlainString());
        contentValues.put(Properties.Nds, entity.getNds().toPlainString());
        return contentValues;
    }

    @Override
    public Long getKey(Price entity) {
        return entity.getId();
    }

    @Override
    public long insertOrThrow(@NonNull Price entity) {
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
