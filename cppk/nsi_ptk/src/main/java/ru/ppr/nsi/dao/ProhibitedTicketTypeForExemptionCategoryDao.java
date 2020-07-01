package ru.ppr.nsi.dao;

import android.database.Cursor;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import ru.ppr.database.cache.QueryCache;
import ru.ppr.logger.Logger;
import ru.ppr.nsi.NsiDaoSession;
import ru.ppr.nsi.entity.ProhibitedTicketTypeForExemptionCategory;

/**
 * DAO для таблицы НСИ <i>ProhibitedTicketTypeForExemptionsCategories</i>.
 *
 * @author Aleksandr Brazhkin
 */
public class ProhibitedTicketTypeForExemptionCategoryDao extends BaseEntityDao<ProhibitedTicketTypeForExemptionCategory, Long> {

    private static final String TAG = Logger.makeLogTag(ProhibitedTicketTypeForExemptionCategoryDao.class);

    public static final String TABLE_NAME = "ProhibitedTicketTypeForExemptionsCategories";

    public static class Properties {
        public static final String TicketStorageTypeCode = "TicketStorageTypeCode";
        public static final String Category = "Category";
        public static final String TicketTypeCode = "TicketTypeCode";
    }

    private final SimpleDateFormat changedDateTimeFormat;

    public ProhibitedTicketTypeForExemptionCategoryDao(NsiDaoSession nsiDaoSession, QueryCache queryCache) {
        super(nsiDaoSession, queryCache);
        changedDateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        changedDateTimeFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    @Override
    String getTableName() {
        return TABLE_NAME;
    }

    @Override
    public ProhibitedTicketTypeForExemptionCategory fromCursor(Cursor cursor) {
        ProhibitedTicketTypeForExemptionCategory entity = new ProhibitedTicketTypeForExemptionCategory();
        entity.setTicketStorageTypeCode(cursor.getInt(cursor.getColumnIndex(Properties.TicketStorageTypeCode)));
        entity.setCategory(cursor.getString(cursor.getColumnIndex(Properties.Category)));
        entity.setTicketTypeCode(cursor.getInt(cursor.getColumnIndex(Properties.TicketTypeCode)));
        //добавим базовые поля
        addBaseNSIData(entity, Long.class, cursor);

        return entity;
    }

}
