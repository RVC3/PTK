package ru.ppr.cppk.devtools;

import android.content.Context;

import javax.inject.Inject;

import pl.com.salsoft.sqlitestudioremote.SQLiteStudioService;
import ru.ppr.logger.Logger;

/**
 * Инструмент для стороннего подключения к БД приложения.
 *
 * @author Aleksandr Brazhkin
 */
public class SQLiteConnectionService {

    private static final String TAG = Logger.makeLogTag(SQLiteConnectionService.class);

    private final Context context;

    @Inject
    SQLiteConnectionService(Context context) {
        this.context = context;
    }

    public void start(int port) {
        Logger.trace(TAG, "start, port = " + port);
        SQLiteStudioService.instance().setPort(port);
        SQLiteStudioService.instance().start(context);
    }

    public void stop() {
        SQLiteStudioService.instance().stop();
    }
}
