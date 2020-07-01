package ru.ppr.cppk.sync.loader.model;

import android.database.Cursor;

import java.math.BigDecimal;

import ru.ppr.cppk.db.LocalDaoSession;
import ru.ppr.cppk.db.local.BaseEntityDao;
import ru.ppr.cppk.db.local.ExemptionDao;
import ru.ppr.cppk.sync.kpp.model.Exemption;
import ru.ppr.cppk.sync.loader.base.BaseLoader;
import ru.ppr.nsi.NsiDaoSession;

/**
 * @author Grigoriy Kashka
 */
public class ExemptionLoader extends BaseLoader {

    private final String loadExemptionQuery;
    private final SmartCardLoader smartCardLoader;

    public ExemptionLoader(LocalDaoSession localDaoSession, NsiDaoSession nsiDaoSession, SmartCardLoader smartCardLoader) {
        super(localDaoSession, nsiDaoSession);
        this.smartCardLoader = smartCardLoader;
        loadExemptionQuery = buildLoadParentTicketInfoQuery();
    }

    public Exemption load(long exemptionId) {

        String[] selectionArgs = new String[]{String.valueOf(exemptionId)};

        Exemption exemption = null;
        Cursor cursor = null;
        try {
            cursor = localDaoSession.getLocalDb().rawQuery(loadExemptionQuery, selectionArgs);
            if (cursor.moveToFirst()) {
                exemption = new Exemption();
                exemption.Fio = cursor.getString(0);
                exemption.RegionOkatoCode = cursor.getString(1);
                exemption.LossSum = new BigDecimal(cursor.getString(2));
                long smartCardId = cursor.getLong(3);
                exemption.SmartCardFromWhichWasReadAboutExemption = smartCardId > 0 ? smartCardLoader.load(smartCardId) : null;
                exemption.TypeOfDocumentWhichApproveExemption = cursor.getString(4);
                exemption.NumberOfDocumentWhichApproveExemption = cursor.getString(5);
                exemption.Organization = cursor.getString(6);
                exemption.IsSnilsUsed = cursor.getInt(7) == 1;
                exemption.RequireSocialCard = cursor.getInt(8) == 1;
                exemption.Code = cursor.getString(9);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return exemption;
    }

    private String buildLoadParentTicketInfoQuery() {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT ");
        sb.append(ExemptionDao.Properties.Fio).append(", ");
        sb.append(ExemptionDao.Properties.RegionOkatoCode).append(", ");
        sb.append(ExemptionDao.Properties.LossSum).append(", ");
        sb.append(ExemptionDao.Properties.SmartCardId).append(", ");
        sb.append(ExemptionDao.Properties.TypeOfDocument).append(", ");
        sb.append(ExemptionDao.Properties.NumberOfDocument).append(", ");
        sb.append(ExemptionDao.Properties.Organization).append(", ");
        sb.append(ExemptionDao.Properties.IsSnilsUsed).append(", ");
        sb.append(ExemptionDao.Properties.RequireSocialCard).append(", ");
        sb.append(ExemptionDao.Properties.Express_Code);
        sb.append(" FROM ");
        sb.append(ExemptionDao.TABLE_NAME);
        sb.append(" WHERE ");
        sb.append(BaseEntityDao.Properties.Id).append(" = ").append("?");
        return sb.toString();
    }
}