package ru.ppr.core.database;

import android.content.Context;

import java.io.File;
import java.io.IOException;

import ru.ppr.database.Database;
import ru.ppr.database.LoggableDatabase;
import ru.ppr.database.NewSQLiteDbOpenHelper;
import ru.ppr.database.SqLiteUtils;
import ru.ppr.database.StubDatabase;
import ru.ppr.logger.Logger;
import ru.ppr.utils.FileUtils2;

/**
 * Дефолтный класс для работы с localDb, rdsDb, securityDb.
 *
 * @author Aleksandr Brazhkin
 */
public class AppDbOpenHelper extends NewSQLiteDbOpenHelper {

    private static String TAG = Logger.makeLogTag(AppDbOpenHelper.class);
    private static final Database STUB_DATABASE = new StubDatabase();

    private final Context context;
    private final boolean isExternalDatabase;
    private LoggableDatabase loggableDatabase = null;
    private Database prevDelegateDatabase = null;

    /**
     * Конструктор
     *
     * @param name               имя базы данных
     * @param isExternalDatabase флаг, определяющий, что база данных внешняя и мы не создаем ее и не управляем ее структурой и версией
     *                           В случае, если внешней базы не существует, вместо нее будет создана безопасная stub заглушка
     */
    public AppDbOpenHelper(Context context, String name, boolean isExternalDatabase) {
        super(context, name, 1);
        this.context = context;
        this.isExternalDatabase = isExternalDatabase;
    }

    @Override
    public void onCreate(Database db) {
        Logger.trace(TAG, "onCreate: " + getDatabaseName());
    }

    @Override
    public void onUpgrade(Database db, int oldVersion, int newVersion) {
        Logger.trace(TAG, "onUpgrade: " + getDatabaseName() + " from " + oldVersion + " to " + newVersion);
    }

    @Override
    public void onOpen(Database db) {
        Logger.trace(TAG, "onOpen: " + getDatabaseName());
    }

    @Override
    public LoggableDatabase getWritableDatabase() {
        Logger.trace(TAG, "getWritableDatabase: " + getDatabaseName());
        synchronized (this) {
            wrap(prepareDatabase(getDatabaseName(), false));
        }
        SqLiteUtils.logTablesInDatabase(loggableDatabase);
        return loggableDatabase;
    }


    @Override
    public LoggableDatabase getReadableDatabase() {
        Logger.trace(TAG, "getReadableDatabase: " + getDatabaseName());
        synchronized (this) {
            wrap(prepareDatabase(getDatabaseName(), true));
        }
        SqLiteUtils.logTablesInDatabase(loggableDatabase);
        return loggableDatabase;
    }

    // Оборачивает базу данных в логгируемую базу данных
    private LoggableDatabase wrap(Database database) {
        if (prevDelegateDatabase != database) {
            if (database == null) {
                loggableDatabase = null;
                prevDelegateDatabase = null;
            } else {
                loggableDatabase = new LoggableDatabase(database, getDatabaseName());
                prevDelegateDatabase = database;
            }
        }

        return loggableDatabase;
    }

    // Подготавливает реальную базу данных и возвращает ее (создает/открывает)
    private Database prepareDatabase(String name, boolean readOnlyMode) {
        File file = context.getDatabasePath(name);
        if (file.exists()) {
            Logger.info(TAG, "Database already exists: " + name);
            return readOnlyMode ? super.getReadableDatabase() : super.getWritableDatabase();
        } else {
            Logger.info(TAG, "Database not exists: " + name);
            if (isExternalDatabase) {
                // Если файла внешней базы не существует, то просто создаем безопасную StubDatabase заглушку
                Logger.info(TAG, "create Database stub object: " + file.getName());
                return STUB_DATABASE;
            } else {
                // Для собственных баз копируем файл базы данных из ресурсов
                try {
                    File databasesDir = file.getParentFile();
                    if ((databasesDir.exists() || databasesDir.mkdirs()) && FileUtils2.copyFileFromAssets(context, file.getName(), file)) {
                        Logger.info(TAG, "Database copied from assets: " + file.getName());
                        return readOnlyMode ? super.getReadableDatabase() : super.getWritableDatabase();
                    } else {
                        throw new RuntimeException("Could not copy database from assets: " + file.getName());
                    }
                } catch (IOException e) {
                    throw new RuntimeException("Could not copy database from assets: " + file.getName(), e);
                }
            }
        }
    }
}