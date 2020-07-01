package ru.ppr.cppk.ui.activity.sqlitetransactiontest;

import android.content.ContentValues;
import android.support.annotation.NonNull;

import javax.inject.Inject;

import ru.ppr.core.ui.mvp.presenter.BaseMvpViewStatePresenter;
import ru.ppr.cppk.db.LocalDaoSession;
import ru.ppr.cppk.helpers.SchedulersCPPK;
import ru.ppr.database.Database;
import ru.ppr.logger.Logger;
import rx.Completable;

/**
 * @author Dmitry Nevolin
 */
class SqliteTransactionTestPresenter extends BaseMvpViewStatePresenter<SqliteTransactionTestView, SqliteTransactionTestViewState> {

    private static final String TAG = Logger.makeLogTag(SqliteTransactionTestPresenter.class);

    private static final String COLUMN_ID = "_id";
    private static final String COLUMN_DATA = "Data";
    private static final String COLUMN_BASE_TABLE_ID = "BaseTableId";
    private static final String CREATE_STATEMENT_ID = COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT";
    private static final String CREATE_STATEMENT_FOREIGN_BASE = COLUMN_BASE_TABLE_ID + " INTEGER, FOREIGN KEY (" + COLUMN_BASE_TABLE_ID + ") REFERENCES BaseTable(" + COLUMN_ID + ")";
    private static final String TABLE_BASE = "BaseTable";
    private static final String TABLE_LEFT = "LeftTable";
    private static final String TABLE_RIGHT = "RightTable";
    private static final String CREATE_BASE_TABLE = "CREATE TABLE " + TABLE_BASE + " (" + CREATE_STATEMENT_ID + ", " + COLUMN_DATA + " INTEGER)";
    private static final String CREATE_LEFT_TABLE = "CREATE TABLE " + TABLE_LEFT + " (" + CREATE_STATEMENT_ID + ", " + CREATE_STATEMENT_FOREIGN_BASE + ")";
    private static final String CREATE_RIGHT_TABLE = "CREATE TABLE " + TABLE_RIGHT + " (" + CREATE_STATEMENT_ID + ", " + CREATE_STATEMENT_FOREIGN_BASE + ")";
    private static final String DROP_BASE_TABLE = "DROP TABLE IF EXISTS " + TABLE_BASE;
    private static final String DROP_LEFT_TABLE = "DROP TABLE IF EXISTS " + TABLE_LEFT;
    private static final String DROP_RIGHT_TABLE = "DROP TABLE IF EXISTS " + TABLE_RIGHT;

    private boolean initialized = false;

    private final LocalDaoSession localDaoSession;

    @Inject
    SqliteTransactionTestPresenter(@NonNull SqliteTransactionTestViewState sqliteTransactionTestViewState,
                                   LocalDaoSession localDaoSession) {
        super(sqliteTransactionTestViewState);
        this.localDaoSession = localDaoSession;
    }

    void initialize() {
        if (!initialized) {
            initialized = true;

            onInitialize();
        }
    }

    private void onInitialize() {
        // Удаляем таблицы (возможно остались с прошлого раза)
        dropTables();
    }

    /**
     * Запускает тест под номером 1-1.
     * Симулирует ситуацию одной вложенной транзакции:
     * Все транзакции выполняются успешно.
     */
    void test11() {
        Logger.trace(TAG, "test 1-1 (one nested, all successful) STARTED");
        Completable
                .fromAction(() -> test(() -> {
                    Logger.trace(TAG, "outer tx STARTED");
                    wrapInTx(() -> {
                        Logger.trace(TAG, "insert base table");
                        insertBaseTable();
                        Logger.trace(TAG, "nested tx 1 STARTED");
                        wrapInTx(() -> {
                            Logger.trace(TAG, "insert left table");
                            insertLeftTable();
                        });
                        Logger.trace(TAG, "nested tx 1 ENDED");
                        Logger.trace(TAG, "insert right table");
                        insertRightTable();
                    });
                    Logger.trace(TAG, "outer tx ENDED");
                }))
                .subscribeOn(SchedulersCPPK.background())
                .subscribe(() -> Logger.trace(TAG, "test 1-1 ENDED"));
    }

    /**
     * Запускает тест под номером 1-2.
     * Симулирует ситуацию одной вложенной транзакции:
     * Вложенная транзакция выполняется с ошибкой.
     */
    void test12() {
        Logger.trace(TAG, "test 1-2 (one nested, nested error) STARTED");
        Completable
                .fromAction(() -> test(() -> {
                    Logger.trace(TAG, "outer tx STARTED");
                    wrapInTx(() -> {
                        Logger.trace(TAG, "insert base table");
                        insertBaseTable();
                        Logger.trace(TAG, "nested tx 1 STARTED");
                        wrapInTx(() -> {
                            throw new RuntimeException("insert left table");
                        });
                        Logger.trace(TAG, "nested tx 1 ENDED");
                        Logger.trace(TAG, "insert right table");
                        insertRightTable();
                    });
                    Logger.trace(TAG, "outer tx ENDED");
                }))
                .subscribeOn(SchedulersCPPK.background())
                .subscribe(() -> Logger.trace(TAG, "test 1-2 ENDED"));
    }

    /**
     * Запускает тест под номером 1-3.
     * Симулирует ситуацию одной вложенной транзакции:
     * Внешняя транзакция выполняется с ошибкой после успешного выполнения вложенной.
     */
    void test13() {
        Logger.trace(TAG, "test 1-3 (one nested, outer error) STARTED");
        Completable
                .fromAction(() -> test(() -> {
                    Logger.trace(TAG, "outer tx STARTED");
                    wrapInTx(() -> {
                        Logger.trace(TAG, "insert base table");
                        insertBaseTable();
                        Logger.trace(TAG, "nested tx 1 STARTED");
                        wrapInTx(() -> {
                            Logger.trace(TAG, "insert left table");
                            insertLeftTable();
                        });
                        Logger.trace(TAG, "nested tx 1 ENDED");
                        throw new RuntimeException("insert right table");
                    });
                    Logger.trace(TAG, "outer tx ENDED");
                }))
                .subscribeOn(SchedulersCPPK.background())
                .subscribe(() -> Logger.trace(TAG, "test 1-3 ENDED"));
    }

    /**
     * Запускает тест под номером 2-1.
     * Симулирует ситуацию двух вложенных транзакций в одну общую:
     * Первая внутренняя транзакция завершается с ошибкой.
     */
    void test21() {
        Logger.trace(TAG, "test 2-1 (two nested, first nested error) STARTED");
        Completable
                .fromAction(() -> test(() -> {
                    Logger.trace(TAG, "outer tx STARTED");
                    wrapInTx(() -> {
                        Logger.trace(TAG, "nested tx 1 STARTED");
                        wrapInTx(() -> {
                            throw new RuntimeException("insert left table");
                        });
                        Logger.trace(TAG, "nested tx 1 ENDED");
                        Logger.trace(TAG, "nested tx 2 STARTED");
                        wrapInTx(() -> {
                            Logger.trace(TAG, "insert right table");
                            insertRightTable();
                        });
                        Logger.trace(TAG, "nested tx 2 ENDED");
                        Logger.trace(TAG, "insert base table");
                        insertBaseTable();
                    });
                    Logger.trace(TAG, "outer tx ENDED");
                }))
                .subscribeOn(SchedulersCPPK.background())
                .subscribe(() -> Logger.trace(TAG, "test 2-1 ENDED"));
    }

    /**
     * Запускает тест под номером 2-2.
     * Симулирует ситуацию двух вложенных транзакций в одну общую:
     * Вторая внутренняя транзакция завершается с ошибкой.
     */
    void test22() {
        Logger.trace(TAG, "test 2-2 (two nested, second nested error) STARTED");
        Completable
                .fromAction(() -> test(() -> {
                    Logger.trace(TAG, "outer tx STARTED");
                    wrapInTx(() -> {
                        Logger.trace(TAG, "nested tx 1 STARTED");
                        wrapInTx(() -> {
                            Logger.trace(TAG, "insert left table");
                            insertLeftTable();
                        });
                        Logger.trace(TAG, "nested tx 1 ENDED");
                        Logger.trace(TAG, "nested tx 2 STARTED");
                        wrapInTx(() -> {
                            throw new RuntimeException("insert right table");
                        });
                        Logger.trace(TAG, "nested tx 2 ENDED");
                        Logger.trace(TAG, "insert base table");
                        insertBaseTable();
                    });
                    Logger.trace(TAG, "outer tx ENDED");
                }))
                .subscribeOn(SchedulersCPPK.background())
                .subscribe(() -> Logger.trace(TAG, "test 2-2 ENDED"));
    }

    /**
     * Готовит БД для теста и запускает тест.
     */
    private void test(Runnable runnable) {
        // Удаляем таблицы (возможно остались с прошлого теста)
        dropTables();
        // Создаём таблицы для тестов
        Database localDb = localDaoSession.getLocalDb();
        localDb.execSQL(CREATE_BASE_TABLE);
        localDb.execSQL(CREATE_LEFT_TABLE);
        localDb.execSQL(CREATE_RIGHT_TABLE);

        runnable.run();
    }

    /**
     * Удаляет тестовые таблицы
     */
    private void dropTables() {
        Database localDb = localDaoSession.getLocalDb();
        localDb.execSQL(DROP_RIGHT_TABLE);
        localDb.execSQL(DROP_LEFT_TABLE);
        localDb.execSQL(DROP_BASE_TABLE);
    }

    /**
     * Вставляет данные базовой таблицы, возвращает id вставленной записи
     */
    private long insertBaseTable() {
        Database localDb = localDaoSession.getLocalDb();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_DATA, 0);
        return localDb.insertOrThrow(TABLE_BASE, null, contentValues);
    }

    /**
     * Вставляет данные левой таблицы (сам довставляет себе базовую запись)
     */
    private void insertLeftTable() {
        Database localDb = localDaoSession.getLocalDb();
        long baseTableId = insertBaseTable();
        localDb.insertOrThrow(TABLE_LEFT, null, createBaseTableIdContentValues(baseTableId));
    }

    /**
     * Вставляет данные правой таблицы (сам довставляет себе базовую запись)
     */
    private void insertRightTable() {
        Database localDb = localDaoSession.getLocalDb();
        long baseTableId = insertBaseTable();
        localDb.insertOrThrow(TABLE_RIGHT, null, createBaseTableIdContentValues(baseTableId));
    }

    /**
     * Создаёт данные для вставки в дочерние для базовой таблицы таблицы
     */
    private ContentValues createBaseTableIdContentValues(long baseTableId) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_BASE_TABLE_ID, baseTableId);
        return contentValues;
    }

    /**
     * Оборачивает переданный код в транзакцию
     */
    private void wrapInTx(Runnable runnable) {
        localDaoSession.beginTransaction();
        try {
            runnable.run();
            localDaoSession.setTransactionSuccessful();
        } catch (RuntimeException error) {
            Logger.error(TAG, error);
        } finally {
            localDaoSession.endTransaction();
        }
    }

    @Override
    public void destroy() {
        // Удаляем таблицы при выходе с экрана
        dropTables();
        super.destroy();
    }

}
