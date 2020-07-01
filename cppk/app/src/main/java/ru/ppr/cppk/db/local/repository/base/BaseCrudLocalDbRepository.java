package ru.ppr.cppk.db.local.repository.base;

import android.support.annotation.NonNull;

import java.util.List;

import ru.ppr.cppk.db.local.LocalDbSessionManager;
import ru.ppr.cppk.localdb.model.base.LocalModelWithId;
import ru.ppr.cppk.localdb.repository.base.CrudLocalDbRepository;

/**
 * Репозиторий для доступа к данным локальной БД с поддержкой следующих базовых операций:
 * - Выборка
 * - Обновление
 * - Удаление
 * - Вставка
 *
 * @author Aleksandr Brazhkin
 */
public abstract class BaseCrudLocalDbRepository<M extends LocalModelWithId<K>, K> extends BaseRudLocalDbRepository<M, K> implements CrudLocalDbRepository<M, K> {

    public BaseCrudLocalDbRepository(LocalDbSessionManager localDbSessionManager) {
        super(localDbSessionManager);
    }

    @Override
    public long insert(@NonNull M model) {
        long rowId = dao().insertOrThrow(model);
        model.setId(model.getId());
        return rowId;
    }

    @Override
    public void insertAll(@NonNull List<M> modelList) {
        beginTransaction();
        try {
            for (M model : modelList) {
                insert(model);
            }
            setTransactionSuccessful();
        } finally {
            endTransaction();
        }
    }

}
