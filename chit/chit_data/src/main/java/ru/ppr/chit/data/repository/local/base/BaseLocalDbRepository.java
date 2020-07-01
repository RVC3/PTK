package ru.ppr.chit.data.repository.local.base;

import org.greenrobot.greendao.AbstractDao;

import io.reactivex.Observable;
import ru.ppr.chit.data.db.LocalDbManager;
import ru.ppr.chit.data.mapper.local.LocalDbMapper;
import ru.ppr.chit.domain.repository.local.base.LocalDbRepository;
import ru.ppr.chit.localdb.daosession.LocalDaoSession;
import ru.ppr.database.greendao.GreenDaoDatabase;

/**
 * Базовый класс для репозитория локальной БД.
 *
 * @param <M>  Тип модели слоя логики
 * @param <E>  Тип сущности слоя БД
 * @param <PK> Тип поля PrimaryKey сущности слоя БД
 * @author Aleksandr Brazhkin
 */
public abstract class BaseLocalDbRepository<M, E, PK> implements LocalDbRepository {

    private final LocalDbManager localDbManager;

    public BaseLocalDbRepository(LocalDbManager localDbManager) {
        this.localDbManager = localDbManager;
    }

    protected abstract AbstractDao<E, PK> dao();

    protected abstract LocalDbMapper<M, E> mapper();

    protected LocalDaoSession daoSession() {
        return localDbManager.daoSession();
    }

    protected GreenDaoDatabase db() {
        return daoSession().getDatabase();
    }

    protected Observable<Boolean> connectionState() {
        return localDbManager.connectionState();
    }

    protected Observable<Boolean> endsOfTransactions() {
        return localDbManager.endsOfTransactions();
    }

    protected void beginTransaction(){
        localDbManager.daoSession().getDatabase().beginTransaction();
    }

    protected void endTransaction(){
        localDbManager.daoSession().getDatabase().endTransaction();
    }

    protected void setTransactionSuccessful(){
        localDbManager.daoSession().getDatabase().setTransactionSuccessful();
    }

}
