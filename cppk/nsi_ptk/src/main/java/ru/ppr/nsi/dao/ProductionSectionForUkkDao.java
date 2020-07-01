package ru.ppr.nsi.dao;

import android.database.Cursor;

import java.util.ArrayList;
import java.util.List;

import ru.ppr.database.cache.QueryCache;
import ru.ppr.nsi.NsiDaoSession;

/**
 * DAO для таблицы НСИ <i>ProductionSectionsForUkk</i>.
 *
 * @author Aleksandr Brazhkin
 */
public class ProductionSectionForUkkDao extends BaseEntityDao<String, Integer> {

    public static final String TABLE_NAME = "ProductionSectionsForUkk";

    public static class Properties {
        public static final String UkkCode = "UkkCode";
        public static final String ProductSectionCode = "ProductSectionCode";
    }

    public ProductionSectionForUkkDao(NsiDaoSession nsiDaoSession, QueryCache queryCache) {
        super(nsiDaoSession, queryCache);
    }

    @Override
    String getTableName() {
        return TABLE_NAME;
    }

    @Override
    public String fromCursor(Cursor cursor) {

        return null;
    }

    /**
     * Получает список кодов участков для указанного участка УКК.
     *
     * @param productionSectionWithUkkCode Участок УКК
     * @param addParent                    Включить productionSectionWithUkkCode в список
     * @param versionId                    Версия НСИ
     * @return Список кодов участков для указанного участка УКК
     */
    public List<Long> getProductionSectionCodesForUkk(int productionSectionWithUkkCode, boolean addParent, int versionId) {

        List<String> selectionArgsList = new ArrayList<>();

        StringBuilder sb = new StringBuilder();
        if (addParent) {
            sb.append("SELECT ").append(BaseEntityDao.Properties.Code);
            sb.append(" FROM ").append(ProductionSectionDao.TABLE_NAME);
            sb.append(" WHERE ").append(BaseEntityDao.Properties.Code).append(" = ").append("?");
            selectionArgsList.add(String.valueOf(productionSectionWithUkkCode));
            sb.append(" AND ");
            sb.append(checkVersion(ProductionSectionDao.TABLE_NAME, versionId));
            sb.append(" UNION ");
        }
        sb.append("SELECT ").append(Properties.ProductSectionCode);
        sb.append(" FROM ").append(TABLE_NAME);
        sb.append(" WHERE ").append(Properties.UkkCode).append(" = ").append("?");
        selectionArgsList.add(String.valueOf(productionSectionWithUkkCode));
        sb.append(" AND ");
        sb.append(checkVersion(TABLE_NAME, versionId));

        String[] selectionArgs = new String[selectionArgsList.size()];
        selectionArgsList.toArray(selectionArgs);

        List<Long> productionSectionCodes = new ArrayList<>();
        Cursor cursor = null;
        try {
            cursor = db().rawQuery(sb.toString(), selectionArgs);
            while (cursor.moveToNext()) {
                long productionSectionsCode = cursor.getLong(0);
                productionSectionCodes.add(productionSectionsCode);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return productionSectionCodes;
    }
}
