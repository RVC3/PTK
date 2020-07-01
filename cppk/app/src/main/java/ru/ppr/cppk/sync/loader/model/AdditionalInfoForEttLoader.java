package ru.ppr.cppk.sync.loader.model;

import android.database.Cursor;

import java.util.Date;

import ru.ppr.cppk.db.LocalDaoSession;
import ru.ppr.cppk.db.local.AdditionalInfoForEttDao;
import ru.ppr.cppk.db.local.BaseEntityDao;
import ru.ppr.cppk.sync.kpp.model.AdditionalInfoForEtt;
import ru.ppr.cppk.sync.loader.base.BaseLoader;
import ru.ppr.nsi.NsiDaoSession;

/**
 * @author Grigoriy Kashka
 */
public class AdditionalInfoForEttLoader extends BaseLoader {

    private final String loadExemptionQuery;

    public AdditionalInfoForEttLoader(LocalDaoSession localDaoSession, NsiDaoSession nsiDaoSession) {
        super(localDaoSession, nsiDaoSession);
        loadExemptionQuery = buildLoadParentTicketInfoQuery();
    }

    public AdditionalInfoForEtt load(long exemptionId) {

        String[] selectionArgs = new String[]{String.valueOf(exemptionId)};

        AdditionalInfoForEtt additionalInfoForEtt = null;
        Cursor cursor = null;
        try {
            cursor = localDaoSession.getLocalDb().rawQuery(loadExemptionQuery, selectionArgs);
            if (cursor.moveToFirst()) {
                additionalInfoForEtt = new AdditionalInfoForEtt();
                additionalInfoForEtt.IssueDataTime = cursor.isNull(0) ? null : new Date(cursor.getLong(0));
                additionalInfoForEtt.IssueUnitCode = cursor.getString(1);
                additionalInfoForEtt.OwnerOrganizationCode = cursor.getString(2);
                additionalInfoForEtt.PassengerFio = cursor.getString(3);
                additionalInfoForEtt.GuardianFio = cursor.getString(4);
                additionalInfoForEtt.SNILS = cursor.getString(5);
                additionalInfoForEtt.PassengerCategory = cursor.getString(6);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return additionalInfoForEtt;
    }

    private String buildLoadParentTicketInfoQuery() {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT ");
        sb.append(AdditionalInfoForEttDao.Properties.IssueDataTime).append(", ");
        sb.append(AdditionalInfoForEttDao.Properties.IssueUnitCode).append(", ");
        sb.append(AdditionalInfoForEttDao.Properties.OwnerOrganizationCode).append(", ");
        sb.append(AdditionalInfoForEttDao.Properties.PassengerFio).append(", ");
        sb.append(AdditionalInfoForEttDao.Properties.GuardianFio).append(", ");
        sb.append(AdditionalInfoForEttDao.Properties.Snils).append(", ");
        sb.append(AdditionalInfoForEttDao.Properties.PassengerCategory);
        sb.append(" FROM ");
        sb.append(AdditionalInfoForEttDao.TABLE_NAME);
        sb.append(" WHERE ");
        sb.append(BaseEntityDao.Properties.Id).append(" = ").append("?");
        return sb.toString();
    }
}