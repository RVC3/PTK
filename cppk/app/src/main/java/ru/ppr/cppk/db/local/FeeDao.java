package ru.ppr.cppk.db.local;

import android.content.ContentValues;
import android.database.Cursor;
import android.support.annotation.NonNull;

import java.math.BigDecimal;

import ru.ppr.cppk.db.LocalDaoSession;
import ru.ppr.cppk.localdb.model.Fee;
import ru.ppr.database.Database;
import ru.ppr.database.garbage.base.GCNoLinkRemovable;
import ru.ppr.logger.Logger;
import ru.ppr.nsi.entity.FeeType;

/**
 * DAO для таблицы локальной БД <i>Fee</i>.
 *
 * @author Aleksandr Brazhkin
 */
public class FeeDao extends BaseEntityDao<Fee, Long> implements GCNoLinkRemovable {

    private static final String TAG = Logger.makeLogTag(FeeDao.class);

    public static final String TABLE_NAME = "Fee";

    public static class Properties {
        public static final String Id = "_id";
        public static final String Total = "Total";
        public static final String Nds = "Nds";
        public static final String FeeType = "FeeType";
    }

    public FeeDao(LocalDaoSession localDaoSession) {
        super(localDaoSession);
    }

    @Override
    public String getTableName() {
        return TABLE_NAME;
    }

    @Override
    public Fee fromCursor(Cursor cursor) {
        Fee fee = new Fee();
        fee.setId(cursor.getLong(cursor.getColumnIndex(Properties.Id)));
        fee.setTotal(new BigDecimal(cursor.getString(cursor.getColumnIndex(Properties.Total))));
        fee.setNds(new BigDecimal(cursor.getString(cursor.getColumnIndex(Properties.Nds))));
        int index = cursor.getColumnIndex(Properties.FeeType);
        fee.setFeeType(cursor.isNull(index) ? null : FeeType.valueOf(cursor.getInt(index)));
        return fee;
    }

    @Override
    public ContentValues toContentValues(Fee entity) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(Properties.Nds, entity.getNds().toString());
        contentValues.put(Properties.Total, entity.getTotal().toString());
        contentValues.put(Properties.FeeType, entity.getFeeType() == null ? null : entity.getFeeType().getCode());
        return contentValues;
    }

    @Override
    public Long getKey(Fee entity) {
        return entity.getId();
    }

    @Override
    public long insertOrThrow(@NonNull Fee entity) {
        long id = super.insertOrThrow(entity);
        entity.setId(id);
        return id;
    }

    @Override
    public boolean gcHandleNoLinkRemoveData(Database database) {
        // оставляем реализацию сборщика мусора по умолчанию
        return false;
    }

}
