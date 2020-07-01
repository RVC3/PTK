package ru.ppr.cppk.db.local.repository;

import android.database.Cursor;

import javax.inject.Inject;

import ru.ppr.cppk.db.local.BaseEntityDao;
import ru.ppr.cppk.db.local.LocalDbSessionManager;
import ru.ppr.cppk.db.local.LocalDbVersionDao;
import ru.ppr.cppk.db.local.repository.base.BaseCrudLocalDbRepository;
import ru.ppr.cppk.localdb.model.LocalDbVersion;
import ru.ppr.cppk.localdb.repository.LocalDbVersionRepository;
import ru.ppr.database.Query;
import ru.ppr.database.QueryBuilder;

/**
 * @author Aleksandr Brazhkin
 */
public class LocalDbVersionRepositoryImpl extends BaseCrudLocalDbRepository<LocalDbVersion, Long> implements LocalDbVersionRepository {

    @Inject
    LocalDbVersionRepositoryImpl(LocalDbSessionManager localDbSessionManager) {
        super(localDbSessionManager);
    }

    @Override
    protected BaseEntityDao<LocalDbVersion, Long> dao() {
        return daoSession().localDbVersionDao();
    }

    @Override
    public int getCurrentVersion() {
        Query query = new QueryBuilder().select()
                .max(LocalDbVersionDao.Properties.VersionId)
                .from(LocalDbVersionDao.TABLE_NAME)
                .build();

        Cursor cursor = query.run(db());
        try {
            if (cursor.moveToFirst()) {
                return cursor.getInt(0);
            }
        } finally {
            cursor.close();
        }
        return 0;
    }
}
