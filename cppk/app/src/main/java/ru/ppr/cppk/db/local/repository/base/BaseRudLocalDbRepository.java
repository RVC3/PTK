package ru.ppr.cppk.db.local.repository.base;

import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import ru.ppr.cppk.db.local.BaseEntityDao;
import ru.ppr.cppk.db.local.LocalDbSessionManager;
import ru.ppr.cppk.localdb.model.base.LocalModelWithId;
import ru.ppr.cppk.localdb.repository.base.RudLocalDbRepository;
import ru.ppr.database.Query;
import ru.ppr.database.QueryBuilder;

/**
 * Репозиторий для доступа к данным локальной БД с поддержкой следующих базовых операций:
 * - Выборка
 * - Обновление
 * - Удаление
 *
 * @author Aleksandr Brazhkin
 */
public abstract class BaseRudLocalDbRepository<M extends LocalModelWithId<PK>, PK> extends BaseLocalDbRepository<M> implements RudLocalDbRepository<M, PK> {

    public BaseRudLocalDbRepository(LocalDbSessionManager localDbSessionManager) {
        super(localDbSessionManager);
    }

    @Override
    protected abstract BaseEntityDao<M, PK> dao();

    @Nullable
    @Override
    public M load(@Nullable PK key) {
        return dao().load(key);
    }

    @NonNull
    @Override
    public List<M> loadAll() {
        return dao().loadAll();
    }

    @NonNull
    @Override
    public List<M> loadAll(@NonNull List<PK> keyList) {
        Query query = new QueryBuilder()
                .selectAll().from(dao().getTableName())
                .where().field(BaseEntityDao.Properties.Id).in(keyList)
                .build();

        List<M> models = new ArrayList<>();
        Cursor cursor = query.run(db());
        try {
            while (cursor.moveToNext()) {
                models.add(dao().fromCursor(cursor));
            }
        } finally {
            cursor.close();
        }
        return models;
    }

    @Override
    public List<M> loadAllNotIn(@NonNull List<PK> keyList) {
        Query query = new QueryBuilder()
                .selectAll().from(dao().getTableName())
                .where().field(BaseEntityDao.Properties.Id).notIn(keyList)
                .build();

        List<M> models = new ArrayList<>();
        Cursor cursor = query.run(db());
        try {
            while (cursor.moveToNext()) {
                models.add(dao().fromCursor(cursor));
            }
        } finally {
            cursor.close();
        }
        return models;
    }

    @Override
    public void update(@NonNull M model) {
        dao().update(model);
    }

    @Override
    public void delete(@NonNull M model) {
        dao().delete(model);
    }

}
