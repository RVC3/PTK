package ru.ppr.chit.data.repository.nsi.base;

import org.greenrobot.greendao.query.WhereCondition;

import ru.ppr.chit.data.db.NsiDbManager;
import ru.ppr.chit.domain.model.nsi.base.NsiModelWithVD;
import ru.ppr.chit.nsidb.entity.base.NsiEntityWithDeleteInVersionId;
import ru.ppr.chit.nsidb.entity.base.NsiEntityWithVD;
import ru.ppr.chit.nsidb.entity.base.NsiEntityWithVersionId;

/**
 * Базовый класс репозитория сущностей НСИ с полями VersionId и DeleteInVersionId.
 *
 * @param <M> Тип модели слоя логики
 * @param <E> Тип сущности слоя БД
 * @author Dmitry Nevolin
 */
public abstract class BaseVdNsiDbRepository<M extends NsiModelWithVD, E extends NsiEntityWithVD> extends BaseNsiDbRepository<M, E, Void> {

    public BaseVdNsiDbRepository(NsiDbManager nsiDbManager) {
        super(nsiDbManager);
    }

    protected WhereCondition versionIdEqCondition(int versionId) {
        return new WhereCondition.StringCondition(
                NsiEntityWithVersionId.Property + " <= " + versionId + " AND (" +
                        NsiEntityWithDeleteInVersionId.Property + " > " + versionId + " OR " +
                        NsiEntityWithDeleteInVersionId.Property + " IS NULL)"
        );
    }

    protected WhereCondition versionIdEqCondition(int versionId, String tablePrefix) {
        return new WhereCondition.StringCondition(
                tablePrefix + "." + NsiEntityWithVersionId.Property + " <= " + versionId + " AND (" +
                        tablePrefix + "." + NsiEntityWithDeleteInVersionId.Property + " > " + versionId + " OR " +
                        tablePrefix + "." + NsiEntityWithDeleteInVersionId.Property + " IS NULL)"
        );
    }

}
