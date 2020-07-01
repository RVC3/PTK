package ru.ppr.chit.data.repository.nsi.base;

import org.greenrobot.greendao.AbstractDao;

import io.reactivex.Observable;
import ru.ppr.chit.data.db.NsiDbManager;
import ru.ppr.chit.data.mapper.nsi.NsiDbMapper;
import ru.ppr.chit.domain.repository.nsi.base.NsiDbRepository;
import ru.ppr.chit.nsidb.daosession.NsiDaoSession;
import ru.ppr.database.greendao.GreenDaoDatabase;

/**
 * Базовый класс для репозитория НСИ.
 *
 * @param <M>  Тип модели слоя логики
 * @param <E>  Тип сущности слоя БД
 * @param <PK> Тип поля PrimaryKey сущности слоя БД
 * @author Dmitry Nevolin
 */
public abstract class BaseNsiDbRepository<M, E, PK> implements NsiDbRepository {

    private final NsiDbManager nsiDbManager;

    public BaseNsiDbRepository(NsiDbManager nsiDbManager) {
        this.nsiDbManager = nsiDbManager;
    }

    protected abstract AbstractDao<E, PK> dao();

    protected abstract NsiDbMapper<M, E> mapper();

    protected NsiDaoSession daoSession() {
        return nsiDbManager.daoSession();
    }

    protected GreenDaoDatabase db() {
        return daoSession().getDatabase();
    }

    protected Observable<Boolean> connectionState() {
        return nsiDbManager.connectionState();
    }

    protected Observable<Boolean> endsOfTransactions() {
        return nsiDbManager.endsOfTransactions();
    }

}
