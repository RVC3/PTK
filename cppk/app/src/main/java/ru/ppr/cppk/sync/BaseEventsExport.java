package ru.ppr.cppk.sync;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import ru.ppr.cppk.db.LocalDaoSession;
import ru.ppr.cppk.sync.loader.base.Column;
import ru.ppr.logger.Logger;
import ru.ppr.nsi.NsiDaoSession;
import ru.ppr.utils.Executors;

/**
 * Базовый класс экспорта данных при синхронизации
 *
 * @author Grigoriy Kashka
 */
abstract class BaseEventsExport {

    private static final String TAG = Logger.makeLogTag(BaseEventsExport.class);

    protected final LocalDaoSession localDaoSession;
    protected final NsiDaoSession nsiDaoSession;
    final File outputFile;

    BaseEventsExport(LocalDaoSession localDaoSession, NsiDaoSession nsiDaoSession, File outputFile) {
        this.localDaoSession = localDaoSession;
        this.nsiDaoSession = nsiDaoSession;
        this.outputFile = outputFile;
    }

    final ExecutorService writeToFileExecutor = new ThreadPoolExecutor(1, 1,
            0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<>(),
            r -> new Thread(r, "ExportWriteToFile")) {
        @Override
        protected void afterExecute(Runnable r, Throwable t) {
            super.afterExecute(r, t);
            Executors.logErrorAfterExecute(TAG, r, t);
        }
    };

    String createColumnsForSelect(String prefix, Column[] columns) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < columns.length; i++) {
            if (i != 0) sb.append(", ");
            sb.append(prefix).append(".").append(columns[i].name);
        }
        return sb.toString();
    }
}
