package ru.ppr.database;

import android.database.Cursor;
import android.database.DatabaseUtils;
import android.util.Log;

import ru.ppr.logger.Logger;

/**
 * Вспомогательный функции для работы с SQLite.
 *
 * @author Aleksandr Brazhkin
 */
public class SqLiteUtils {

    private static final String TAG = Logger.makeLogTag(SqLiteUtils.class);
    private static final int LOG_LINE_SYMBOLS = 1024;

    /**
     * Возвращает строку с плейсхолдерами.
     *
     * @param len Количество плейсхолдеров
     * @return Строка с плейсхолдерами
     */
    public static String makePlaceholders(int len) {
        if (len < 1) {
            return "";
        } else {
            StringBuilder sb = new StringBuilder(len * 2 - 1);
            sb.append("?");
            for (int i = 1; i < len; i++) {
                sb.append(",?");
            }
            return sb.toString();
        }
    }

    /**
     * Логирует план запроса.
     *
     * @param database      БД
     * @param query         Запрос
     * @param selectionArgs Параметры запроса
     */
    public static void logExplainQueryPlan(Database database, String query, String[] selectionArgs) {
        Cursor cursor = null;
        try {
            cursor = database.rawQuery("EXPLAIN QUERY PLAN " + query, selectionArgs);
            Logger.trace(TAG, DatabaseUtils.dumpCursorToString(cursor));
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    /**
     * Логирует тело запроса.
     *
     * @param query         Запрос
     * @param selectionArgs Параметры запроса
     */
    public static void logQuery(String query, Object[] selectionArgs) {
        StringBuilder sb = new StringBuilder();
        sb.append("Query:\n");
        int fromIndex = 0;
        int argIndex = 0;
        while (true) {
            int newFromIndex = query.indexOf("?", fromIndex);
            if (newFromIndex == -1) {
                sb.append(query.substring(fromIndex, query.length()));
                break;
            }
            sb.append(query.substring(fromIndex, newFromIndex));
            Object arg = selectionArgs[argIndex];
            if (arg instanceof String) {
                sb.append('\'').append(arg).append('\'');
            } else {
                sb.append(arg);
            }
            fromIndex = newFromIndex + 1;
            argIndex++;
        }

        // слишком длинные запросы одной строкой в logcat выводятся не полностью
        for (int i = LOG_LINE_SYMBOLS; i < sb.length(); i += LOG_LINE_SYMBOLS) {
            sb.insert(i, '\n');
        }

        Logger.trace(TAG, sb.toString());
    }

    /**
     * Логирует список таблиц в БД.
     *
     * @param database БД
     */
    public static void logTablesInDatabase(Database database) {
        Cursor cursor = null;
        try {
            cursor = database.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);
            StringBuilder stringBuilder = new StringBuilder("Tables in database:\n");
            while (cursor.moveToNext()) {
                String tableName = cursor.getString(0);
                stringBuilder.append(tableName).append("\n");
            }
            Logger.trace(TAG, stringBuilder.toString());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }
}
