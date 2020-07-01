package ru.ppr.chit.data.repository.nsi.base;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.greenrobot.greendao.query.WhereCondition;

import java.util.List;

import ru.ppr.chit.data.db.NsiDbManager;
import ru.ppr.chit.domain.model.nsi.base.NsiModelWithCV;
import ru.ppr.chit.domain.repository.nsi.base.CvNsiDbRepository;
import ru.ppr.chit.nsidb.entity.base.NsiEntityWithCV;
import ru.ppr.chit.nsidb.entity.base.NsiEntityWithCode;
import ru.ppr.chit.nsidb.entity.base.NsiEntityWithVersionId;

/**
 * Базовый класс репозитория сущностей НСИ с полями Code и VersionId.
 *
 * @param <M> Тип модели слоя логики
 * @param <E> Тип сущности слоя БД
 * @param <C> Тип поля Code модели слоя логики
 * @author Aleksandr Brazhkin
 */
public abstract class BaseCvNsiDbRepository<M extends NsiModelWithCV<C>, E extends NsiEntityWithCV<C>, C>
        extends BaseNsiDbRepository<M, E, Void>
        implements CvNsiDbRepository<M, C> {

    public BaseCvNsiDbRepository(NsiDbManager nsiDbManager) {
        super(nsiDbManager);
    }

    @Nullable
    @Override
    public M load(@NonNull C code, int versionId) {
        E entity = dao().queryBuilder()
                .where(codeEqCondition(code), versionIdEqCondition(versionId))
                .unique();
        return mapper().entityToModel(entity);
    }

    @NonNull
    @Override
    public List<M> loadAll(int versionId) {
        List<E> entities = dao().queryBuilder()
                .where(versionIdEqCondition(versionId))
                .list();
        return mapper().entityListToModelList(entities);
    }

    @NonNull
    @Override
    public List<M> loadAll(@NonNull List<C> codeList, int versionId) {
        List<E> entities = dao().queryBuilder()
                .where(codeInCondition(codeList), versionIdEqCondition(versionId))
                .list();
        return mapper().entityListToModelList(entities);
    }

    private WhereCondition codeEqCondition(@NonNull C code) {
        return new WhereCondition.StringCondition(NsiEntityWithCode.Property + "=" + code);
    }

    private WhereCondition codeInCondition(@NonNull List<C> codeList) {
        StringBuilder sb = new StringBuilder("(");
        for (int i = 0; i < codeList.size(); i++) {
            sb.append(codeList.get(i));
            if (i == codeList.size() - 1) {
                sb.append(")");
            } else {
                sb.append(",");
            }
        }
        return new WhereCondition.StringCondition(NsiEntityWithCode.Property + " in " + sb);
    }

    protected WhereCondition versionIdEqCondition(int versionId) {
        return new WhereCondition.StringCondition(NsiEntityWithVersionId.Property + " <= " + versionId);
    }

}
