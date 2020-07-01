package ru.ppr.cppk.sync.loader.model;

import android.database.Cursor;

import ru.ppr.cppk.db.LocalDaoSession;
import ru.ppr.cppk.db.local.BaseEntityDao;
import ru.ppr.cppk.db.local.SmartCardDao;
import ru.ppr.cppk.sync.kpp.model.SmartCard;
import ru.ppr.cppk.sync.loader.base.BaseLoader;
import ru.ppr.nsi.NsiDaoSession;

/**
 * @author Aleksandr Brazhkin
 */
public class SmartCardLoader extends BaseLoader {

    private final ParentTicketInfoLoader parentTicketInfoLoader;
    private final String loadSmartCardQuery;

    public SmartCardLoader(
            LocalDaoSession localDaoSession,
            NsiDaoSession nsiDaoSession,
            ParentTicketInfoLoader parentTicketInfoLoader) {
        super(localDaoSession, nsiDaoSession);
        this.parentTicketInfoLoader = parentTicketInfoLoader;
        loadSmartCardQuery = buildLoadSmartCardQuery();
    }

    public SmartCard load(long smartCardId) {

        String[] selectionArgs = new String[]{String.valueOf(smartCardId)};

        SmartCard smartCard = null;
        Cursor cursor = null;
        try {
            cursor = localDaoSession.getLocalDb().rawQuery(loadSmartCardQuery, selectionArgs);
            if (cursor.moveToFirst()) {
                smartCard = new SmartCard();
                smartCard.OuterNumber = cursor.getString(0);
                smartCard.CrystalSerialNumber = cursor.getString(1);
                smartCard.Type = cursor.getInt(2);
                smartCard.Issuer = cursor.getString(3);
                smartCard.UsageCount = cursor.getInt(4);
                smartCard.Track = cursor.getInt(5);
                if (!cursor.isNull(6)) {
                    smartCard.PresentTicket1 = parentTicketInfoLoader.load(cursor.getLong(6));
                }
                if (!cursor.isNull(7)) {
                    smartCard.PresentTicket2 = parentTicketInfoLoader.load(cursor.getLong(7));
                }
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return smartCard;
    }

    private String buildLoadSmartCardQuery() {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT ");
        sb.append(SmartCardDao.Properties.OuterNumber).append(", ");
        sb.append(SmartCardDao.Properties.CrystalSerialNumber).append(", ");
        sb.append(SmartCardDao.Properties.TypeCode).append(", ");
        sb.append(SmartCardDao.Properties.Issuer).append(", ");
        sb.append(SmartCardDao.Properties.UsageCount).append(", ");
        sb.append(SmartCardDao.Properties.Track).append(", ");
        sb.append(SmartCardDao.Properties.PresentTicket1).append(", ");
        sb.append(SmartCardDao.Properties.PresentTicket2);
        sb.append(" FROM ");
        sb.append(SmartCardDao.TABLE_NAME);
        sb.append(" WHERE ");
        sb.append(BaseEntityDao.Properties.Id).append(" = ").append("?");
        return sb.toString();
    }
}
