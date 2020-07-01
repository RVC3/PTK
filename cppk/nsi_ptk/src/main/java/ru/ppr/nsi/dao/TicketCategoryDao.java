package ru.ppr.nsi.dao;

import android.database.Cursor;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import ru.ppr.database.cache.QueryCache;
import ru.ppr.logger.Logger;
import ru.ppr.nsi.NsiDaoSession;
import ru.ppr.nsi.entity.TicketCategory;

/**
 * DAO для таблицы НСИ <i>TicketCategories</i>.
 *
 * @author Aleksandr Brazhkin
 */
public class TicketCategoryDao extends BaseEntityDao<TicketCategory, Integer> {

    private static final String TAG = Logger.makeLogTag(TicketCategoryDao.class);

    public static final String TABLE_NAME = "TicketCategories";

    public static class Properties {
        public static final String Name = "Name";
        public static final String ExpressTicketCategoryCode = "ExpressTicketCategoryCode";
        public static final String Abbreviation = "Abbreviation";
        public static final String Presale = "Presale";
        public static final String DelayPassback = "DelayPassback";
        public static final String ChagedDateTime = "ChangedDateTime";
        public static final String Code = "Code";
        public static final String IsCompositeType = "IsCompositeType";
    }

    public TicketCategoryDao(NsiDaoSession nsiDaoSession, QueryCache queryCache) {
        super(nsiDaoSession, queryCache);
    }

    @Override
    public TicketCategory load(Integer code, int versionId) {
        TicketCategory loaded = super.load(code, versionId);

        if (loaded != null) {
            loaded.setVersionId(versionId);
        }

        return loaded;
    }

    @Override
    String getTableName() {
        return TABLE_NAME;
    }

    @Override
    public TicketCategory fromCursor(Cursor cursor) {
        TicketCategory out = new TicketCategory();

        int index = cursor.getColumnIndex(TicketCategoryDao.Properties.Name);
        if (index != -1)
            out.setName(cursor.getString(index));

        index = cursor.getColumnIndex(TicketCategoryDao.Properties.ExpressTicketCategoryCode);
        if (index != -1)
            out.setExpressTicketCategoryCode(cursor.getString(index));

        index = cursor.getColumnIndex(TicketCategoryDao.Properties.Abbreviation);
        if (index != -1)
            out.setAbbreviation(cursor.getString(index));

        index = cursor.getColumnIndex(TicketCategoryDao.Properties.Presale);
        if (index != -1)
            out.setPresale(cursor.getInt(index));

        index = cursor.getColumnIndex(TicketCategoryDao.Properties.DelayPassback);
        if (index != -1)
            out.setDelayPassback(cursor.getInt(index));

        index = cursor.getColumnIndex(TicketCategoryDao.Properties.ChagedDateTime);
        if (index != -1)
            out.setChagedDateTime(getDateFrom1970(cursor.getString(index)));

        index = cursor.getColumnIndex(TicketCategoryDao.Properties.Code);
        if (index != -1)
            out.setCode(cursor.getInt(index));

        index = cursor.getColumnIndex(TicketCategoryDao.Properties.IsCompositeType);
        if (index != -1)
            out.setCompositeType(cursor.getInt(index) != 0);

        // out.versionId =
        // cursor.getInt(cursor.getColumnIndex(ConstantsDB.versionId));
        // out.deleteInVersion =
        // cursor.getInt(cursor.getColumnIndex(ConstantsDB.deleteInVersion));
        return out;
    }

    public Date getDateFrom1970(String datetime) {
        if (datetime == null)
            return null;
        try {
            return (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").parse(datetime));
        } catch (ParseException e) {
            Logger.info(TAG, "Can not convert date " + datetime + " to timestamp with pattert yyyy-MM-dd HH:mm:ss.SSS - " + e.getMessage());
        }
        return null;
    }
}
