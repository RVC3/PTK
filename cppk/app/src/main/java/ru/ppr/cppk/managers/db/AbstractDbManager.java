package ru.ppr.cppk.managers.db;

import android.content.Context;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import ru.ppr.core.database.AppDbOpenHelper;
import ru.ppr.database.Database;
import ru.ppr.utils.FileUtils;

/**
 * @author Aleksandr Brazhkin
 */
abstract class AbstractDbManager<DS> {
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
    private boolean sqlLogsEnabled = false;

    private List<DaoSessionResetListener<DS>> daoSessionResetListeners = new ArrayList<>();


    AbstractDbManager(Context context) {
        changeDatabase(context, getDbName());
        appDbOpenHelper = new AppDbOpenHelper(context, getDbName(), isExternalDatabase());
        resetDaoSession();
    }

    protected abstract void resetDaoSession();

    protected abstract String getDbName();

    // Определяет, что база данных внешняя и мы не управляем ее структурой и версией (не создаем, не обновляем)
    protected abstract boolean isExternalDatabase();

    AppDbOpenHelper getAppDbOpenHelper() {
        return appDbOpenHelper;
    }

    void addDaoSessionResetListener(DaoSessionResetListener<DS> daoSessionResetListener) {
        daoSessionResetListeners.add(daoSessionResetListener);
        daoSessionResetListener.onDaoSessionReset(daoSession);
    }

    public DS getDaoSession() {
        return daoSession;
    }

    void setDaoSession(DS daoSession) {
        this.daoSession = daoSession;
        for (DaoSessionResetListener<DS> daoSessionResetListener : daoSessionResetListeners) {
            daoSessionResetListener.onDaoSessionReset(daoSession);
        }
    }

    boolean isSqlLogsEnabled() {
        return sqlLogsEnabled;
    }

    void setLogEnabled(boolean logEnabled) {
        sqlLogsEnabled = logEnabled;
        appDbOpenHelper.getWritableDatabase().setLogEnabled(logEnabled);
    }

    public void closeConnection() {
        appDbOpenHelper.close();
    }

    private void changeDatabase(Context context, String name) {
        File dbInRightPlace = context.getDatabasePath(name); // тут должна быть БД
        File oldDb = new File(context.getCacheDir() + name); // отсюда будем копировать старую БД

        // изначально в папке database есть какая то версия БД, ее размер 12288 байт, поэтому
        // если версия в папке database меньше 13000 байт, то будем считать что это некорректная версия
        if (oldDb.exists() && (!dbInRightPlace.exists()
                || (dbInRightPlace.exists() && dbInRightPlace.length() < 13000))) {
            //копируем БД
            FileUtils.copy(context, oldDb, dbInRightPlace);
        }
    }

    public interface DaoSessionResetListener<DS> {
        void onDaoSessionReset(DS daoSession);
    }
}
