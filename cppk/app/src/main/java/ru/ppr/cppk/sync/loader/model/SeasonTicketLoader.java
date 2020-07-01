package ru.ppr.cppk.sync.loader.model;

import android.database.Cursor;

import ru.ppr.cppk.db.LocalDaoSession;
import ru.ppr.cppk.db.local.BaseEntityDao;
import ru.ppr.cppk.db.local.SeasonTicketDao;
import ru.ppr.cppk.sync.kpp.model.SeasonTicket;
import ru.ppr.cppk.sync.loader.base.BaseLoader;
import ru.ppr.nsi.NsiDaoSession;

/**
 * @author Grigoriy Kashka
 */
public class SeasonTicketLoader extends BaseLoader {

    private final String loadExemptionQuery;

    public SeasonTicketLoader(LocalDaoSession localDaoSession, NsiDaoSession nsiDaoSession) {
        super(localDaoSession, nsiDaoSession);
        loadExemptionQuery = buildLoadParentTicketInfoQuery();
    }

    public SeasonTicket load(long exemptionId) {

        String[] selectionArgs = new String[]{String.valueOf(exemptionId)};

        SeasonTicket seasonTicket = null;
        Cursor cursor = null;
        try {
            cursor = localDaoSession.getLocalDb().rawQuery(loadExemptionQuery, selectionArgs);
            if (cursor.moveToFirst()) {
                seasonTicket = new SeasonTicket();
                seasonTicket.PassCount = cursor.getInt(0);
                seasonTicket.PassLeftCount = cursor.getInt(1);
                seasonTicket.MonthDays = cursor.getString(2);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return seasonTicket;
    }

    private String buildLoadParentTicketInfoQuery() {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT ");
        sb.append(SeasonTicketDao.Properties.PassCount).append(", ");
        sb.append(SeasonTicketDao.Properties.PassLeftCount).append(", ");
        sb.append(SeasonTicketDao.Properties.MonthDays);
        sb.append(" FROM ");
        sb.append(SeasonTicketDao.TABLE_NAME);
        sb.append(" WHERE ");
        sb.append(BaseEntityDao.Properties.Id).append(" = ").append("?");
        return sb.toString();
    }
}