package ru.ppr.nsi.dao;

import android.database.Cursor;

import ru.ppr.database.cache.QueryCache;
import ru.ppr.nsi.NsiDaoSession;
import ru.ppr.nsi.entity.ExemptionGroup;

/**
 * DAO для таблицы НСИ <i>ExemptionGroups</i>.
 *
 * @author Aleksandr Brazhkin
 */
public class ExemptionGroupDao extends BaseEntityDao<ExemptionGroup, Integer> {

    public static final String TABLE_NAME = "ExemptionGroups";

    public static class Properties {
        public static final String Name = "Name";
        public static final String Code = "Code";
    }

    public ExemptionGroupDao(NsiDaoSession nsiDaoSession, QueryCache queryCache) {
        super(nsiDaoSession, queryCache);
    }

    @Override
    String getTableName() {
        return TABLE_NAME;
    }

    @Override
    public ExemptionGroup fromCursor(Cursor cursor) {

        ExemptionGroup exemptionGroup = new ExemptionGroup();

        int index = cursor.getColumnIndex(ExemptionGroupDao.Properties.Code);
        if (index != -1)
            exemptionGroup.setGroupCode(cursor.getInt(index));

        index = cursor.getColumnIndex(ExemptionGroupDao.Properties.Name);
        if (index != -1)
            exemptionGroup.setGroupName(cursor.getString(index));

        return exemptionGroup;
    }
}
