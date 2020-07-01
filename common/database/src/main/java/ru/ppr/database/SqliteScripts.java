package ru.ppr.database;

import android.content.Context;
import android.support.annotation.NonNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import ru.ppr.logger.Logger;

/**
 * Функции работы с sql скриптами
 *
 * @author m.sidorov
 */

public class SqliteScripts {

    private static final String TAG = Logger.makeLogTag(SqliteScripts.class);

    /**
     * Считыват sql крипт из asset файла
     *
     * @return String тело скрипта
     */
    public static String readAssetsSqlScript(Context context, String assetsFileName) throws IOException {

        BufferedReader bufferedReader = null;

        String script;
        try {
            String newLine = System.getProperty("line.separator");

            StringBuilder builder = new StringBuilder();
            bufferedReader = new BufferedReader(
                    new InputStreamReader(context.getAssets().open(assetsFileName)));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                builder.append(line).append(newLine);
            }
            script = builder.toString();
        } finally {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException e) {
                    Logger.error(TAG, e);
                }
            }
        }

        return script;
    }

    /**
     * Разбивает скрипт по разделителю ";" на список sql команд
     * Android не может выполнить несколько запросов, разделенных ";",
     * поэтому делаем из одного запроса список последовательных команд
     *
     * @return List<String> список sql команд из скрипта
     */
    public static List<String> splitScript(@NonNull String script) {

        if (script.isEmpty()) return Collections.emptyList();

        String[] array = script.split("\\s*;\\s*");

        return Arrays.asList(array);
    }

    /**
     * Выполняет последовательность sql команд
     *
     * @return
     */
    public static void exec(Database db, String sqlStatements) throws Exception {
        exec(db, splitScript(sqlStatements));
    }

    /**
     * Выполняет последовательность sql команд
     *
     * @return
     */
    public static void exec(Database db, List<String> sqlStatements) throws Exception {
        if (!sqlStatements.isEmpty()) {
            for (String statement : sqlStatements) {
                db.execSQL(statement);
            }
        }
    }

}
