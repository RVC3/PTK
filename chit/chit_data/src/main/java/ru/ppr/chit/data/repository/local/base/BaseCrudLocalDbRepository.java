package ru.ppr.chit.data.repository.local.base;

import android.support.annotation.NonNull;

import java.util.List;

import ru.ppr.chit.data.db.LocalDbManager;
import ru.ppr.chit.domain.model.local.base.LocalModelWithId;
import ru.ppr.chit.domain.repository.local.base.CrudLocalDbRepository;
import ru.ppr.chit.localdb.entity.base.LocalEntityWithId;

/**
 * @author Aleksandr Brazhkin
 */
public abstract class BaseCrudLocalDbRepository<M extends LocalModelWithId<K>, E extends LocalEntityWithId<K>, K> extends BaseRudLocalDbRepository<M, E, K> implements CrudLocalDbRepository<M, K> {

    public BaseCrudLocalDbRepository(LocalDbManager localDbManager) {
        super(localDbManager);
    }

    @Override
    public long insert(@NonNull M model) {
        E entity = mapper().modelToEntity(model);
        long rowId = dao().insert(entity);
        model.setId(entity.getId());
        return rowId;
    }

    @Override
    public void insertAll(List<M> modelList) {
        dao().insertInTx(mapper().modelListToEntityList(modelList));
    }

}
