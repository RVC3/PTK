package ru.ppr.cppk.devtools;

import javax.inject.Inject;

import ru.ppr.logger.Logger;

/**
 * Инструмент для стороннего подключения к БД приложения.
 *
 * @author Aleksandr Brazhkin
 */
public class SQLiteConnectionService {

    private static final String TAG = Logger.makeLogTag(SQLiteConnectionService.class);

    @Inject
    SQLiteConnectionService() {

    }

    public void start(int port) {
        // nop
    }

    public void stop(){
        // nop
    }
}
