package ru.ppr.nsi.repository;

import android.database.Cursor;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import ru.ppr.logger.Logger;
import ru.ppr.nsi.NsiDbSessionManager;
import ru.ppr.nsi.dao.BaseEntityDao;
import ru.ppr.nsi.dao.ProductionSectionDao;
import ru.ppr.nsi.entity.ProductionSection;
import ru.ppr.nsi.repository.base.BaseRepository;

/**
 * @author Dmitry Nevolin
 */
@Singleton
public class ProductionSectionRepository extends BaseRepository<ProductionSection, Long> {

    private static final String TAG = Logger.makeLogTag(ProductionSectionRepository.class);

    @Inject
    ProductionSectionRepository(NsiDbSessionManager nsiDbSessionManager) {
        super(nsiDbSessionManager);
    }

    @Override
    protected BaseEntityDao<ProductionSection, Long> selfDao() {
        return daoSession().getProductionSectionDao();
    }

    /**
     * Возвращает список участков
     */
    public List<ProductionSection> getAllProductionSections(int nsiVersion) {
        List<ProductionSection> sectionsList = new ArrayList<>();

        if (nsiVersion == -1) {
            Logger.trace(TAG, "Actual version database not found");
            return sectionsList;
        }

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder
                .append("Select * from ")
                .append(ProductionSectionDao.TABLE_NAME)
                .append(" where ")
                .append(BaseEntityDao.Properties.VersionId)
                .append(" <= ")
                .append(nsiVersion)
                .append(" AND ")
                .append("(")
                .append(BaseEntityDao.Properties.DeleteInVersionId)
                .append(" > ")
                .append(nsiVersion)
                .append(" OR ")
                .append(BaseEntityDao.Properties.DeleteInVersionId)
                .append(" is NULL)");

        Cursor cursor = daoSession().getNsiDb().rawQuery(stringBuilder.toString(), null);
        try {
            while (cursor.moveToNext()) {
                ProductionSection productionSections = selfDao().fromCursor(cursor);
                sectionsList.add(productionSections);
            }
        } finally {
            cursor.close();
        }

        return sectionsList;
    }

}
