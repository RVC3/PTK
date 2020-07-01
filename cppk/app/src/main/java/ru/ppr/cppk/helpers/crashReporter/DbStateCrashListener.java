package ru.ppr.cppk.helpers.crashReporter;

import org.sqlite.database.sqlite.SQLiteException;

import ru.ppr.core.ui.helper.crashreporter.CrashReport;
import ru.ppr.core.ui.helper.crashreporter.CrashReporter;
import ru.ppr.cppk.Globals;
import ru.ppr.cppk.di.Dagger;
import ru.ppr.cppk.di.Di;
import ru.ppr.database.SqLiteUtils;
import ru.ppr.logger.Logger;

/**
 * Логирует информацию о БД при краше.
 *
 * @author Aleksandr Brazhkin
 */
public class DbStateCrashListener implements CrashReporter.CrashListener {

    private static final String TAG = Logger.makeLogTag(DbStateCrashListener.class);

    private final Globals globals;

    public DbStateCrashListener(Globals globals) {
        this.globals = globals;
    }

    @Override
    public void onCrash(CrashReport crashReport) {

        Logger.trace(TAG, "onCrash");

        String securityDbVersion = globals.getSecurityDaoSession().getSecurityDataVersionDao().getSecurityVersion();
        Logger.error(TAG, "Security db version: " + securityDbVersion);

        int nsiDbVersion = Di.INSTANCE.nsiVersionManager().getCurrentNsiVersionId();
        Logger.error(TAG, "Nsi db version: " + nsiDbVersion);

        int localDbVersion = Dagger.appComponent().localDbVersionRepository().getCurrentVersion();
        Logger.error(TAG, "Local db version: " + localDbVersion);

        if (crashReport.toString().contains(SQLiteException.class.getSimpleName())) {
            Logger.trace(TAG, "LocalDb tables");
            SqLiteUtils.logTablesInDatabase(globals.getLocalDb());
            Logger.trace(TAG, "NsiDb tables");
            SqLiteUtils.logTablesInDatabase(globals.getNsiDb());
            Logger.trace(TAG, "SecurityDb tables");
            SqLiteUtils.logTablesInDatabase(globals.getSecurityDb());
            Logger.flushQueueSync();
        }
    }
}
