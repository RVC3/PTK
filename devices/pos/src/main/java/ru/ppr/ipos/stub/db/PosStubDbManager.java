package ru.ppr.ipos.stub.db;

import android.content.Context;

import java.io.File;

import ru.ppr.database.Database;
import ru.ppr.logger.Logger;

/**
 * @author Dmitry Vinogradov
 */
public class PosStubDbManager {

    private static final String TAG = Logger.makeLogTag(PosStubDbManager.class);

    /**
     * Объект блокировки
     */
    private final Object LOCK = new Object();
    /**
     * Объект, управляющий подключением к БД.
     * Получать эземпляр {@link Database} и закрывать соединение
     * следует через методы этого объекта
     */
    private final PosStubSQLiteHelper posStubSQLiteHelper;
    /**
     * DaoSession
     */
    private PosStubDaoSession daoSession;

    PosStubDbManager(Context context) {
        posStubSQLiteHelper = new PosStubSQLiteHelper(context);
    }

    public String getDatabaseName() {
        return posStubSQLiteHelper.getDatabaseName();
    }

    public File getDatabasePath() {
        return posStubSQLiteHelper.getDatabasePath();
    }

    public PosStubDaoSession daoSession() {
        return daoSession;
    }

    public void openConnection() {
        synchronized (LOCK) {
            Logger.trace(TAG, "openConnection");
            Database db = posStubSQLiteHelper.getWritableDatabase();
            daoSession = createDaoSession(db);
        }
    }

    public void closeConnection() {
        synchronized (LOCK) {
            Logger.trace(TAG, "closeConnection");
            posStubSQLiteHelper.close();
            daoSession = null;
        }
    }

    protected PosStubDaoSession createDaoSession(Database database) {
        return new PosStubDaoSession(database);
    }
}
