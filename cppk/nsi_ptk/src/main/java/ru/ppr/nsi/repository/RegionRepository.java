package ru.ppr.nsi.repository;

import android.database.Cursor;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import ru.ppr.nsi.NsiDbSessionManager;
import ru.ppr.nsi.NsiUtils;
import ru.ppr.nsi.dao.BaseEntityDao;
import ru.ppr.nsi.dao.ProductionSectionForUkkDao;
import ru.ppr.nsi.dao.RegionDao;
import ru.ppr.nsi.dao.StationDao;
import ru.ppr.nsi.dao.StationForProductionSectionDao;
import ru.ppr.nsi.entity.Region;
import ru.ppr.nsi.repository.base.BaseRepository;

/**
 * @author Aleksandr Brazhkin
 */
@Singleton
public class RegionRepository extends BaseRepository<Region, Integer> {

    @Inject
    RegionRepository(NsiDbSessionManager nsiDbSessionManager) {
        super(nsiDbSessionManager);
    }

    @Override
    protected BaseEntityDao<Region, Integer> selfDao() {
        return daoSession().getRegionDao();
    }

    /**
     * Возвращает регион для участка.
     *
     * @param productionSectionCode Код участка
     * @param nsiVersion            Версия НСИ
     * @return Регион
     */
    public Region getRegionForProductionSection(int productionSectionCode, int nsiVersion) {
        List<String> selectionArgsList = new ArrayList<>();

        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("SELECT ");
        stringBuilder.append(RegionDao.TABLE_NAME).append(".").append("*");
        stringBuilder.append(" FROM ");
        stringBuilder.append(RegionDao.TABLE_NAME);
        stringBuilder.append(" JOIN ");
        {
            stringBuilder.append("(");
            stringBuilder.append("SELECT ");
            stringBuilder.append(StationDao.TABLE_NAME).append(".").append("*");
            stringBuilder.append(" FROM ");
            stringBuilder.append(StationForProductionSectionDao.TABLE_NAME);
            stringBuilder.append(" JOIN ");
            stringBuilder.append(StationDao.TABLE_NAME);
            stringBuilder.append(" ON ");
            stringBuilder.append(StationForProductionSectionDao.TABLE_NAME).append(".").append(StationForProductionSectionDao.Properties.StationCode);
            stringBuilder.append(" = ");
            stringBuilder.append(StationDao.TABLE_NAME).append(".").append(StationDao.Properties.Code);
            stringBuilder.append(" WHERE ");
            {
                stringBuilder.append(" ( ");
                stringBuilder.append(StationForProductionSectionDao.Properties.ProductionSectionCode).append(" = ").append("?");
                selectionArgsList.add(String.valueOf(productionSectionCode));
                stringBuilder.append(" OR ");
                stringBuilder.append(StationForProductionSectionDao.Properties.ProductionSectionCode).append(" IN ");
                {
                    stringBuilder.append(" ( ");
                    stringBuilder.append("SELECT ");
                    stringBuilder.append(ProductionSectionForUkkDao.Properties.ProductSectionCode);
                    stringBuilder.append(" FROM ");
                    stringBuilder.append(ProductionSectionForUkkDao.TABLE_NAME);
                    stringBuilder.append(" WHERE ");
                    stringBuilder.append(ProductionSectionForUkkDao.Properties.UkkCode).append(" = ").append("?");
                    selectionArgsList.add(String.valueOf(productionSectionCode));
                    stringBuilder.append(" AND ");
                    stringBuilder.append(NsiUtils.checkVersion(ProductionSectionForUkkDao.TABLE_NAME, nsiVersion));
                    stringBuilder.append(" ) ");
                }
                stringBuilder.append(" ) ");
            }
            stringBuilder.append(" AND ");
            stringBuilder.append(NsiUtils.checkVersion(StationDao.TABLE_NAME, nsiVersion));
            stringBuilder.append(" AND ");
            stringBuilder.append(NsiUtils.checkVersion(StationForProductionSectionDao.TABLE_NAME, nsiVersion));
            stringBuilder.append(")").append(" AS ").append(StationDao.TABLE_NAME);
        }
        stringBuilder.append(" ON ");
        stringBuilder.append(RegionDao.TABLE_NAME).append(".").append(RegionDao.Properties.Code);
        stringBuilder.append(" = ");
        stringBuilder.append(StationDao.TABLE_NAME).append(".").append(StationDao.Properties.RegionCode);
        stringBuilder.append(" WHERE ");
        stringBuilder.append(NsiUtils.checkVersion(RegionDao.TABLE_NAME, nsiVersion));
        stringBuilder.append(" LIMIT ").append(1);

        String[] selectionArgs = new String[selectionArgsList.size()];
        selectionArgsList.toArray(selectionArgs);

        Region region = null;
        Cursor cursor = null;
        try {
            cursor = daoSession().getNsiDb().rawQuery(stringBuilder.toString(), selectionArgs);
            if (cursor.moveToFirst()) {
                region = daoSession().getRegionDao().fromCursor(cursor);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return region;
    }
}
