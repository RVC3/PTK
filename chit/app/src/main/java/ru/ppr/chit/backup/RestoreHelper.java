package ru.ppr.chit.backup;

import java.io.File;
import java.io.IOException;

import javax.inject.Inject;

import io.reactivex.Completable;
import io.reactivex.Single;
import ru.ppr.chit.data.db.LocalDbManager;
import ru.ppr.chit.data.db.NsiDbManager;
import ru.ppr.chit.data.db.SecurityDbManager;
import ru.ppr.logger.Logger;
import ru.ppr.utils.FileUtils2;

/**
 * Класс-помощник для восстановления бекапов.
 * Объединяет в себе часто нужный функционал при восстановлении резервных копий.
 *
 * @author Dmitry Nevolin
 */
public class RestoreHelper {

    private static final String TAG = Logger.makeLogTag(RestoreHelper.class);

    private static final String LOCAL_FILE_NAME = "databases/localDb.db";
    private static final String NSI_FILE_NAME = "databases/nsiDb.db";
    private static final String SECURITY_TEMPLATE_FILE_NAME = "databases/securityDb.db";

    private final LocalDbManager localDbManager;
    private final NsiDbManager nsiDbManager;
    private final SecurityDbManager securityDbManager;

    @Inject
    public RestoreHelper(LocalDbManager localDbManager,
                         NsiDbManager nsiDbManager,
                         SecurityDbManager securityDbManager) {
        this.localDbManager = localDbManager;
        this.nsiDbManager = nsiDbManager;
        this.securityDbManager = securityDbManager;
    }

    /**
     * Восстанавливает из папки с бекапом локальную базу.
     *
     * @param restoredBackup папка с бекапом.
     * @return true в случае успеха, false в противном случае.
     */
    public boolean restoreLocal(File restoredBackup) {
        File local = new File(restoredBackup, LOCAL_FILE_NAME);
        Logger.trace(TAG, "start restore local: " + local.getAbsolutePath());
        if (!local.exists()) {
            throw new IllegalStateException("file not found in restored backup: " + local.getAbsolutePath());
        }
        try {
            localDbManager.closeConnection();
            FileUtils2.copyFile(local, localDbManager.getDatabasePath(), null);
            localDbManager.openConnection();
            Logger.trace(TAG, "complete restore local");
            return true;
        } catch (IOException exception) {
            Logger.error(TAG, exception);
            return false;
        }
    }

    public Completable rxRestoreLocal(File restoredBackup) {
        return Single
                .fromCallable(() -> restoreSecurity(restoredBackup))
                .flatMapCompletable(result -> {
                    if (result) {
                        return Completable.complete();
                    } else {
                        return Completable.error(new RuntimeException("restoring local is failed"));
                    }
                });
    }

    /**
     * Восстанавливает из папки с бекапом НСИ.
     *
     * @param restoredBackup папка с бекапом.
     * @return true в случае успеха, false в противном случае.
     */
    public boolean restoreNsi(File restoredBackup) {
        File nsi = new File(restoredBackup, NSI_FILE_NAME);
        Logger.trace(TAG, "start restore nsi: " + nsi.getAbsolutePath());
        if (!nsi.exists()) {
            throw new IllegalStateException("file not found in restored backup: " + nsi.getAbsolutePath());
        }
        try {
            nsiDbManager.closeConnection();
            FileUtils2.copyFile(nsi, nsiDbManager.getDatabasePath(), null);
            nsiDbManager.openConnection();
            Logger.trace(TAG, "complete restore nsi");
            return true;
        } catch (IOException exception) {
            Logger.error(TAG, exception);
            return false;
        }
    }

    public Completable rxRestoreNsi(File restoredBackup) {
        return Single
                .fromCallable(() -> restoreSecurity(restoredBackup))
                .flatMapCompletable(result -> {
                    if (result) {
                        return Completable.complete();
                    } else {
                        return Completable.error(new RuntimeException("restoring nsi is failed"));
                    }
                });
    }

    /**
     * Восстанавливает из папки с бекапом базу безопасности.
     *
     * @param restoredBackup папка с бекапом.
     * @return true в случае успеха, false в противном случае.
     */
    public boolean restoreSecurity(File restoredBackup) {
        File security = new File(restoredBackup, SECURITY_TEMPLATE_FILE_NAME);
        Logger.trace(TAG, "start restore security: " + security.getAbsolutePath());
        if (!security.exists()) {
            throw new IllegalStateException("file not found in restored backup: " + security.getAbsolutePath());
        }
        try {
            securityDbManager.closeConnection();
            FileUtils2.copyFile(security, securityDbManager.getDatabasePath(), null);
            securityDbManager.openConnection();
            Logger.trace(TAG, "complete restore security");
            return true;
        } catch (IOException exception) {
            Logger.error(TAG, exception);
            return false;
        }
    }

    public Completable rxRestoreSecurity(File restoredBackup) {
        return Single
                .fromCallable(() -> restoreSecurity(restoredBackup))
                .flatMapCompletable(result -> {
                    if (result) {
                        return Completable.complete();
                    } else {
                        return Completable.error(new RuntimeException("restoring security is failed"));
                    }
                });
    }

}
