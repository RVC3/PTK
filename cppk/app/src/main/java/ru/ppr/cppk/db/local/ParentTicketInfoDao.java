package ru.ppr.cppk.db.local;

import android.content.ContentValues;
import android.database.Cursor;
import android.support.annotation.NonNull;

import java.util.Date;

import ru.ppr.cppk.localdb.model.ParentTicketInfo;
import ru.ppr.cppk.localdb.model.TicketWayType;
import ru.ppr.cppk.db.LocalDaoSession;
import ru.ppr.database.Database;
import ru.ppr.database.garbage.base.GCNoLinkRemovable;

/**
 * DAO для таблицы локальной БД <i>ParentTicketInfo</i>.
 *
 * @author Dmitry Nevolin
 */
public class ParentTicketInfoDao extends BaseEntityDao<ParentTicketInfo, Long> implements GCNoLinkRemovable {

    public static final String TABLE_NAME = "ParentTicketInfo";

    public static class Properties {
        public static final String Id = "_id";
        public static final String SaleDateTime = "SaleDateTime";
        public static final String TicketNumber = "TicketNumber";
        public static final String CashRegisterNumber = "CashRegisterNumber";
        public static final String WayType = "WayType";
    }

    public ParentTicketInfoDao(@NonNull LocalDaoSession localDaoSession) {
        super(localDaoSession);
    }

    @Override
    public String getTableName() {
        return TABLE_NAME;
    }

    @Override
    public ParentTicketInfo fromCursor(Cursor cursor) {
        ParentTicketInfo out = new ParentTicketInfo();
        out.setId(cursor.getLong(cursor.getColumnIndex(Properties.Id)));
        out.setSaleDateTime(new Date(cursor.getLong(cursor.getColumnIndex(ParentTicketInfoDao.Properties.SaleDateTime))));
        out.setTicketNumber(cursor.getInt(cursor.getColumnIndex(Properties.TicketNumber)));
        out.setCashRegisterNumber(cursor.getLong(cursor.getColumnIndex(Properties.CashRegisterNumber)));
        out.setWayType(TicketWayType.valueOf(Integer.parseInt(cursor.getString(cursor.getColumnIndex(Properties.WayType)))));
        return out;
    }

    @Override
    public ContentValues toContentValues(ParentTicketInfo entity) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(Properties.CashRegisterNumber, entity.getCashRegisterNumber());
        contentValues.put(Properties.SaleDateTime, entity.getSaleDateTime().getTime());
        contentValues.put(Properties.TicketNumber, entity.getTicketNumber());
        contentValues.put(Properties.WayType, entity.getWayType().getCode());
        return contentValues;
    }

    @Override
    public Long getKey(ParentTicketInfo entity) {
        return entity.getId();
    }

    @Override
    public long insertOrThrow(@NonNull ParentTicketInfo entity) {
        long id = super.insertOrThrow(entity);
        entity.setId(id);
        return id;
    }

    @Override
    public boolean gcHandleNoLinkRemoveData(Database database) {
        return false;
    }

}
