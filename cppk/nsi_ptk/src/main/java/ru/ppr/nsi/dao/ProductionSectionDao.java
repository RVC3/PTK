package ru.ppr.nsi.dao;

import android.database.Cursor;

import ru.ppr.database.cache.QueryCache;
import ru.ppr.nsi.NsiDaoSession;
import ru.ppr.nsi.entity.ProductionSection;

/**
 * DAO для таблицы НСИ <i>ProductionSections</i>.
 *
 * @author Aleksandr Brazhkin
 */
public class ProductionSectionDao extends BaseEntityDao<ProductionSection, Long> {

    public static final String TABLE_NAME = "ProductionSections";

    public static class Properties {
        public static final String Name = "Name";
        public static final String CppkSubdivisionCode = "CppkSubdivisionCode";
        public static final String DirectionCode = "DirectionCode";
        public static final String IsUkk = "IsUkk";
        public static final String Code = "Code";
        public static final String VersionId = "VersionId";
        public static final String DeleteInVersionId = "DeleteInVersionId";
    }

    public ProductionSectionDao(NsiDaoSession nsiDaoSession, QueryCache queryCache) {
        super(nsiDaoSession, queryCache);
    }

    @Override
    String getTableName() {
        return TABLE_NAME;
    }

    @Override
    public ProductionSection fromCursor(Cursor cursor) {
        ProductionSection out = new ProductionSection();

        int index = cursor.getColumnIndex(BaseEntityDao.Properties.VersionId);
        if (index != -1)
            out.setVersionId(cursor.getInt(index));

        index = cursor.getColumnIndex(ProductionSectionDao.Properties.Name);
        if (index != -1)
            out.setName(cursor.getString(index));

        index = cursor.getColumnIndex(ProductionSectionDao.Properties.Code);
        if (index != -1)
            out.setCode(cursor.getInt(index));

        index = cursor.getColumnIndex(ProductionSectionDao.Properties.IsUkk);
        if (index != -1)
            out.setUkk(cursor.getInt(index) == 1);

        return out;
    }

}
