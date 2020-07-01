package ru.ppr.chit.data.repository.security.base;

import org.greenrobot.greendao.AbstractDao;

import io.reactivex.Observable;
import ru.ppr.chit.data.db.SecurityDbManager;
import ru.ppr.chit.data.mapper.security.SecurityDbMapper;
import ru.ppr.chit.domain.repository.security.base.SecurityDbRepository;
import ru.ppr.chit.securitydb.daosession.SecurityDaoSession;
import ru.ppr.database.greendao.GreenDaoDatabase;


/**
 * Базовый класс для репозитория Security.
 *
 * @param <M>  Тип модели слоя логики
 * @param <E>  Тип сущности слоя БД
 * @param <PK> Тип поля PrimaryKey сущности слоя БД
 *
 * @author Aleksandr Brazhkin
 */
public abstract class BaseSecurityDbRepository<M, E, PK> implements SecurityDbRepository {

    private final SecurityDbManager securityDbManager;

    public BaseSecurityDbRepository(SecurityDbManager securityDbManager) {
        this.securityDbManager = securityDbManager;
    }

    protected abstract AbstractDao<E, PK> dao();

    protected abstract SecurityDbMapper<M, E> mapper();

    protected SecurityDaoSession daoSession() {
        return securityDbManager.daoSession();
    }

    protected GreenDaoDatabase db() {
        return daoSession().getDatabase();
    }

    protected Observable<Boolean> connectionState() {
        return securityDbManager.connectionState();
    }

    protected Observable<Boolean> endsOfTransactions() {
        return securityDbManager.endsOfTransactions();
    }

}
