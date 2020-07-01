package ru.ppr.chit.data.db;

import android.content.Context;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.PublishSubject;
import ru.ppr.chit.data.assets.AssetsStore;
import ru.ppr.database.Database;
import ru.ppr.database.greendao.GreenDaoLoggableDatabase;
import ru.ppr.logger.Logger;
import ru.ppr.utils.Executors;

/**
 * Базовый класс для DbManager'ов.
 *
 * @author Aleksandr Brazhkin
 */
public abstract class AbstractDbManager<DS> implements DbManager<DS> {

    private static final String TAG = Logger.makeLogTag(AbstractDbManager.class);

    /**
     * Объект блокировки
     */
    private final Object LOCK = new Object();
    /**
     * Объект, управляющий подключением к БД.
     * Получать эземпляр {@link Database} и закрывать соединение
     * следует через методы этого объекта
     */
    private final AppDbOpenHelper appDbOpenHelper;
    /**
     * DaoSession
     */
    private DS daoSession;
    /**
     * Флаг, включено ли логирование SQL
     */
    private boolean logEnabled = true;
    /**
     * Оповещатель состояния подключения к БД
     */
    private BehaviorSubject<Boolean> connectionState = BehaviorSubject.createDefault(Boolean.FALSE);
    /**
     * Оповещатель завершения транзакций в БД
     */
    private PublishSubject<Boolean> endsOfTransactions = PublishSubject.create();

    AbstractDbManager(Context context, AssetsStore assetsStore, String dbName, AssetsStore.Entry assetsDbEntry) {
        appDbOpenHelper = new AppDbOpenHelper(context, assetsStore, dbName, assetsDbEntry);
    }

    public String getDatabaseName() {
        return appDbOpenHelper.getDatabaseName();
    }

    public File getDatabasePath() {
        return appDbOpenHelper.getDatabasePath();
    }

    @Override
    public DS daoSession() {
        return daoSession;
    }

    @Override
    public Observable<Boolean> connectionState() {
        return connectionState;
    }

    @Override
    public Observable<Boolean> endsOfTransactions() {
        return endsOfTransactions;
    }

    public void setLogEnabled(boolean logEnabled) {
        synchronized (LOCK) {
            this.logEnabled = logEnabled;
            if (daoSession != null) {
                // Получаем объект БД, только если подключение уже установлено
                appDbOpenHelper.getWritableDatabase().setLogEnabled(logEnabled);
            }
        }
    }

    public void openConnection() {
        synchronized (LOCK) {
            Logger.trace(TAG, "openConnection");
            GreenDaoLoggableDatabase db = appDbOpenHelper.getWritableDatabase();
            db.setTransactionListener(this::notifyTransactionFinished);
            db.setLogEnabled(logEnabled);
            prepareDatabase(db);
            daoSession = createDaoSession(db);
            notifyConnectionStateChanged(true);
        }
    }

    public void closeConnection() {
        synchronized (LOCK) {
            Logger.trace(TAG, "closeConnection");
            appDbOpenHelper.close();
            daoSession = null;
            notifyConnectionStateChanged(false);
        }
    }

    private void notifyConnectionStateChanged(boolean connected) {
        Logger.trace(TAG, "notifyConnectionStateChanged, connected = " + connected);
        notificationThread.execute(() -> connectionState.onNext(connected));
    }

    private void notifyTransactionFinished() {
        Logger.trace(TAG, "notifyTransactionFinished");
        notificationThread.execute(() -> endsOfTransactions.onNext(Boolean.TRUE));
    }

    protected abstract DS createDaoSession(GreenDaoLoggableDatabase database);

    protected void prepareDatabase(GreenDaoLoggableDatabase database) {

    }

    private final ExecutorService notificationThread = new ThreadPoolExecutor(1, 1,
            0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<>(),
            r -> new Thread(r, getDatabaseName() + "NotificationThread")) {
        @Override
        protected void afterExecute(Runnable r, Throwable t) {
            super.afterExecute(r, t);
            Executors.logErrorAfterExecute(TAG, r, t);
        }
    };
}
