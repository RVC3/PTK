package ru.ppr.chit.data.repository.nsi.base;

import org.greenrobot.greendao.query.WhereCondition;

import ru.ppr.chit.data.db.NsiDbManager;
import ru.ppr.chit.domain.model.nsi.base.NsiModelWithCV;
import ru.ppr.chit.nsidb.entity.base.NsiEntityWithCV;
import ru.ppr.chit.nsidb.entity.base.NsiEntityWithDeleteInVersionId;
import ru.ppr.chit.nsidb.entity.base.NsiEntityWithVersionId;

/**
 * Базовый класс репозитория сущностей НСИ с полями Code, VersionId и DeleteInVersionId.
 *
 * @param <M> Тип модели слоя логики
 * @param <E> Тип сущности слоя БД
 * @param <C> Тип поля Code модели слоя логики
 * @author Aleksandr Brazhkin
 */
public abstract class BaseCvdNsiDbRepository<M extends NsiModelWithCV<C>, E extends NsiEntityWithCV<C>, C>
        extends BaseCvNsiDbRepository<M, E, C> {

    public BaseCvdNsiDbRepository(NsiDbManager nsiDbManager) {
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
