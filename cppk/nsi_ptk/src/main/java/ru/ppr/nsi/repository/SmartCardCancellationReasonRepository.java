package ru.ppr.nsi.repository;

import android.database.Cursor;

import javax.inject.Inject;
import javax.inject.Singleton;

import ru.ppr.nsi.NsiDbSessionManager;
import ru.ppr.nsi.dao.BaseEntityDao;
import ru.ppr.nsi.dao.SmartCardStopListReasonDao;
import ru.ppr.nsi.repository.base.BaseRepository;

/**
 * @author Dmitry Nevolin
 */
@Singleton
public class SmartCardCancellationReasonRepository extends BaseRepository<String, Integer> {

    @Inject
    SmartCardCancellationReasonRepository(NsiDbSessionManager nsiDbSessionManager) {
        super(nsiDbSessionManager);
    }

    @Override
    protected BaseEntityDao<String, Integer> selfDao() {
        return daoSession().getSmartCardStopListReasonDao();
    }

    /**
     * Возвращает причину постановки БСК в стоп-лист
     *
     * @param code    код причины
     * @param version версия бд
     * @return текстовая причина постановки БСК в стоп-лист
     */
    public String getReasonForCode(int code, int version) {
        String reason = null;

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("select ").append(SmartCardStopListReasonDao.Properties.Name)
                .append(" from ").append(SmartCardStopListReasonDao.TABLE_NAME)
                .append(" where ").append(BaseEntityDao.Properties.Code).append(" = ").append(code)
                .append(" AND ").append(BaseEntityDao.Properties.VersionId).append(" <= ").append(version)
                .append(" AND ").append("(").append(BaseEntityDao.Properties.DeleteInVersionId).append(" > ").append(version)
                .append(" OR ").append(BaseEntityDao.Properties.DeleteInVersionId).append(" is NULL)");
        Cursor cursor = null;
        try {
            cursor = daoSession().getNsiDb().rawQuery(stringBuilder.toString(), null);
            if (cursor.moveToFirst() && cursor.getCount() == 1) {
                reason = cursor.getString(0);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return reason;
    }

}
