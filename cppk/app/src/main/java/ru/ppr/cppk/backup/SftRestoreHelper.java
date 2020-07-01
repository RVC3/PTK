package ru.ppr.cppk.backup;

import java.io.File;
import java.io.IOException;

import javax.inject.Inject;

import ru.ppr.core.manager.eds.EdsManager;
import ru.ppr.logger.Logger;
import ru.ppr.utils.FileUtils2;

/**
 * Класс-помощник для восстановления бекапов в части SFT.
 *
 * @author Grigoriy Kashka
 */
public class SftRestoreHelper {

    private static final String TAG = Logger.makeLogTag(SftRestoreHelper.class);

    private final EdsManager edsManager;

    @Inject
    SftRestoreHelper(EdsManager edsManager) {
        this.edsManager = edsManager;
    }

    /**
     * Восстанавливает папку sft IN.
     *
     * @param restoredBackup архив с бекапом.
     * @return true если успешно, false в противном случае.
     */
    public boolean restoreSftIn(File restoredBackup) throws IOException {
        Logger.trace(TAG, "start restore sft IN");
        File edsTransportInDir = edsManager.getEdsDirs().getEdsTransportInDir();
        File restoredEdsTransportInDir = new File(restoredBackup, edsTransportInDir.getName());
        boolean deleteEdsTransportInDir = FileUtils2.deleteDir(edsTransportInDir, null);
        boolean copyRestoredEdsTransportInDir = FileUtils2.copyDir(restoredEdsTransportInDir, edsTransportInDir, null);
        boolean restoreSftIn = deleteEdsTransportInDir && copyRestoredEdsTransportInDir;
        Logger.trace(TAG, "complete restore sft IN: " + restoreSftIn);

        return restoreSftIn;
    }

    /**
     * Восстанавливает папку sft OUT.
     *
     * @param restoredBackup архив с бекапом.
     * @return true если успешно, false в противном случае.
     */
    public boolean restoreSftWorking(File restoredBackup) throws IOException {
        Logger.trace(TAG, "start restore sft WORKING");
        File edsWorkingDir = edsManager.getEdsDirs().getEdsWorkingDir();
        File restoredEdsWorkingDir = new File(restoredBackup, edsWorkingDir.getName());
        boolean deleteEdsWorkingDir = FileUtils2.deleteDir(edsWorkingDir, null);
        boolean copyRestoredEdsWorkingDir = FileUtils2.copyDir(restoredEdsWorkingDir, edsWorkingDir, null);
        boolean restoreSftWorking = deleteEdsWorkingDir && copyRestoredEdsWorkingDir;
        Logger.trace(TAG, "complete restore sft WORKING: " + restoreSftWorking);

        return restoreSftWorking;
    }
}
