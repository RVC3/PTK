package ru.ppr.nsi.repository;

import android.database.Cursor;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import ru.ppr.database.Query;
import ru.ppr.database.QueryBuilder;
import ru.ppr.nsi.NsiDbSessionManager;
import ru.ppr.nsi.NsiUtils;
import ru.ppr.nsi.dao.BaseEntityDao;
import ru.ppr.nsi.dao.FineDao;
import ru.ppr.nsi.entity.Fine;
import ru.ppr.nsi.repository.base.BaseRepository;

/**
 * @author Dmitry Nevolin
 */
@Singleton
public class FineRepository extends BaseRepository<Fine, Long> {

    @Inject
    FineRepository(NsiDbSessionManager nsiDbSessionManager) {
        super(nsiDbSessionManager);
    }

    @Override
    protected BaseEntityDao<Fine, Long> selfDao() {
        return daoSession().getFineDao();
    }

    /**
     * Загружает список всех штрафов.
     *
     * @param versionId Версия НСИ
     * @return Список штрафов
     */
    public List<Fine> loadAll(int versionId) {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT * FROM ").append(FineDao.TABLE_NAME);
        sb.append(" WHERE ").append(NsiUtils.checkVersion(FineDao.TABLE_NAME, versionId));

        List<Fine> fines = new ArrayList<>();
        Cursor cursor = null;
        try {
            cursor = daoSession().getNsiDb().rawQuery(sb.toString(), null);
            while (cursor.moveToNext()) {
                Fine fine = daoSession().getFineDao().fromCursor(cursor);
                fines.add(fine);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return fines;
    }

    /**
     * Загружает список всех штрафов со списком кодов.
     *
     * @param versionId Версия НСИ
     * @return Список штрафов
     */
    public List<Fine> loadAllWithCodes(int versionId, long[] codes) {
        if (codes == null || codes.length == 0) {
            return new ArrayList<>();
        }

        List<Long> codeList = new ArrayList<>();

        for (long code : codes) {
            codeList.add(code);
        }

        QueryBuilder qb = new QueryBuilder();
        qb.selectAll().from(FineDao.TABLE_NAME).where()
                .appendRaw(NsiUtils.checkVersion(FineDao.TABLE_NAME, versionId)).and()
                .field(FineDao.Properties.Code).in(codeList);

        Query query = qb.build();
        List<Fine> fines = new ArrayList<>();
        Cursor cursor = null;

        try {
            cursor = query.run(daoSession().getNsiDb());

            while (cursor.moveToNext()) {
                fines.add(daoSession().getFineDao().fromCursor(cursor));
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return fines;
    }

}
