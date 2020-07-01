package ru.ppr.cppk.sync.loader.model;

import android.database.Cursor;

import java.util.Date;

import ru.ppr.cppk.db.LocalDaoSession;
import ru.ppr.cppk.db.local.BaseEntityDao;
import ru.ppr.cppk.db.local.ParentTicketInfoDao;
import ru.ppr.cppk.sync.kpp.model.ParentTicketInfo;
import ru.ppr.cppk.sync.loader.base.BaseLoader;
import ru.ppr.nsi.NsiDaoSession;


/**
 * @author Aleksandr Brazhkin
 */
public class ParentTicketInfoLoader extends BaseLoader {

    private final String loadParentTicketInfoQuery;

    public ParentTicketInfoLoader(LocalDaoSession localDaoSession, NsiDaoSession nsiDaoSession) {
        super(localDaoSession, nsiDaoSession);
        loadParentTicketInfoQuery = buildLoadParentTicketInfoQuery();
    }

    public ParentTicketInfo load(long parentTicketInfoId) {

        String[] selectionArgs = new String[]{String.valueOf(parentTicketInfoId)};

        ParentTicketInfo parentTicketInfo = null;
        Cursor cursor = null;
        try {
            cursor = localDaoSession.getLocalDb().rawQuery(loadParentTicketInfoQuery, selectionArgs);
            if (cursor.moveToFirst()) {
                parentTicketInfo = new ParentTicketInfo();
                parentTicketInfo.SaleDateTime = new Date(cursor.getLong(0));
                parentTicketInfo.TicketNumber = cursor.getInt(1);
                parentTicketInfo.CashRegisterNumber = cursor.getString(2);
                parentTicketInfo.WayType = cursor.getInt(3);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return parentTicketInfo;
    }

    private String buildLoadParentTicketInfoQuery() {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT ");
        sb.append(ParentTicketInfoDao.Properties.SaleDateTime).append(", ");
        sb.append(ParentTicketInfoDao.Properties.TicketNumber).append(", ");
        sb.append(ParentTicketInfoDao.Properties.CashRegisterNumber).append(", ");
        sb.append(ParentTicketInfoDao.Properties.WayType);
        sb.append(" FROM ");
        sb.append(ParentTicketInfoDao.TABLE_NAME);
        sb.append(" WHERE ");
        sb.append(BaseEntityDao.Properties.Id).append(" = ").append("?");
        return sb.toString();
    }
}
