package ru.ppr.nsi.dao;

import android.database.Cursor;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import ru.ppr.database.cache.QueryCache;
import ru.ppr.nsi.NsiDaoSession;
import ru.ppr.nsi.entity.TrainCategory;
import ru.ppr.nsi.entity.TrainCategoryPrefix;

/**
 * DAO для таблицы НСИ <i>TrainCategories</i>.
 *
 * @author Aleksandr Brazhkin
 */
public class TrainCategoryDao extends BaseEntityDao<TrainCategory, Integer> {

    public static final String TABLE_NAME = "TrainCategories";

    public static class Properties {
        public static final String Description = "Description";
        public static final String Category = "Category";
        public static final String Name = "Name";
        public static final String Code = BaseEntityDao.Properties.Code;
        public static final String DataChecksum = "DataChecksum";
        public static final String ChangedDateTime = "ChangedDateTime";
        public static final String Prefix = "Prefix";
    }

    public TrainCategoryDao(NsiDaoSession nsiDaoSession, QueryCache queryCache) {
        super(nsiDaoSession, queryCache);
    }

    @Override
    public TrainCategory load(Integer code, int versionId) {
        TrainCategory loaded = super.load(code, versionId);

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
    public TrainCategory fromCursor(Cursor cursor) {
        TrainCategory out = new TrainCategory();

        int index = cursor.getColumnIndex(TrainCategoryDao.Properties.Description);
        if (index != -1)
            out.description = cursor.getString(index);

        index = cursor.getColumnIndex(TrainCategoryDao.Properties.Category);
        if (index != -1)
            out.category = cursor.getString(index);

        index = cursor.getColumnIndex(TrainCategoryDao.Properties.Name);
        if (index != -1)
            out.name = cursor.getString(index);

        index = cursor.getColumnIndex(TrainCategoryDao.Properties.Code);
        if (index != -1)
            out.code = cursor.getInt(index);

        try {
            index = cursor.getColumnIndex(TrainCategoryDao.Properties.ChangedDateTime);
            if (index != -1)
                out.changedDateTime = (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").parse(cursor.getString(index))).getTime() / 1000;
        } catch (ParseException e) {
            e.printStackTrace();
        }

        index = cursor.getColumnIndex(TrainCategoryDao.Properties.Prefix);
        if (index != -1)
            out.prefix = TrainCategoryPrefix.valueOf(cursor.getInt(index));

        return out;
    }

}
