package ru.ppr.chit.data.repository.local.base;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.greenrobot.greendao.query.WhereCondition;

import java.util.List;

import ru.ppr.chit.data.db.LocalDbManager;
import ru.ppr.chit.domain.model.local.base.LocalModelWithId;
import ru.ppr.chit.domain.repository.local.base.RudLocalDbRepository;
import ru.ppr.chit.localdb.entity.base.LocalEntityWithId;

/**
 * @author Dmitry Nevolin
 */
public abstract class BaseRudLocalDbRepository<M extends LocalModelWithId<PK>, E extends LocalEntityWithId<PK>, PK> extends BaseLocalDbRepository<M, E, PK> implements RudLocalDbRepository<M, PK> {

    public BaseRudLocalDbRepository(LocalDbManager localDbManager) {
        super(localDbManager);
    }

    @Nullable
    @Override
    public M load(@NonNull PK key) {
        return mapper().entityToModel(dao().load(key));
    }

    @NonNull
    @Override
    public List<M> loadAll() {
        return mapper().entityListToModelList(dao().loadAll());
    }

    @NonNull
    @Override
    public List<M> loadAll(@NonNull List<PK> keyList) {
        List<E> entityList = dao().queryBuilder()
                .where(keyInOrNotInCondition(keyList, false))
                .list();
        return mapper().entityListToModelList(entityList);
    }

    @Override
    public List<M> loadAllNotIn(@NonNull List<PK> keyList) {
        List<E> entityList = dao().queryBuilder()
                .where(keyInOrNotInCondition(keyList, true))
                .list();
        return mapper().entityListToModelList(entityList);
    }

    @Override
    public void update(@NonNull M model) {
        dao().update(mapper().modelToEntity(model));
    }

    @Override
    public void delete(@NonNull M model) {
        dao().delete(mapper().modelToEntity(model));
    }

    private WhereCondition keyInOrNotInCondition(@NonNull List<PK> keyList, boolean not) {
        StringBuilder sb = new StringBuilder("(");
        for (int i = 0; i < keyList.size(); i++) {
            sb.append(keyList.get(i));
            if (i == keyList.size() - 1) {
                sb.append(")");
            } else {
                sb.append(",");
            }
        }
        if (sb.toString().equals("(")) {
            sb.append(")");
        }
        return new WhereCondition.StringCondition(LocalEntityWithId.PropertyId + (not ? " not" : "") + " in " + sb);
    }

}
