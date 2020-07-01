package ru.ppr.nsi.dao;

import android.database.Cursor;

import ru.ppr.database.cache.QueryCache;
import ru.ppr.nsi.NsiDaoSession;
import ru.ppr.nsi.entity.ExemptionOrganization;

/**
 * DAO для таблицы НСИ <i>ExemptionOrganizations</i>.
 *
 * @author Aleksandr Brazhkin
 */
public class ExemptionOrganizationDao extends BaseEntityDao<ExemptionOrganization, String> {

    public static final String TABLE_NAME = "ExemptionOrganizations";

    public static class Properties {
    }

    public ExemptionOrganizationDao(NsiDaoSession nsiDaoSession, QueryCache queryCache) {
        super(nsiDaoSession, queryCache);
    }

    @Override
    String getTableName() {
        return TABLE_NAME;
    }

    @Override
    public ExemptionOrganization fromCursor(Cursor cursor) {

        ExemptionOrganization exemptionOrganization = new ExemptionOrganization();

        int index = cursor.getColumnIndex(ExemptionGroupDao.Properties.Code);
        if (index != -1)
            exemptionOrganization.setOrganizationCode(cursor.getString(index));

        index = cursor.getColumnIndex(ExemptionGroupDao.Properties.Name);
        if (index != -1)
            exemptionOrganization.setOrganizationName(cursor.getString(index));

        return exemptionOrganization;
    }
}
