package ru.ppr.cppk.backup;

import android.util.Pair;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import ru.ppr.core.backup.BackupArchiveUnpacker;
import ru.ppr.core.backup.BaseBackupRestorer;
import ru.ppr.cppk.Globals;
import ru.ppr.cppk.di.Dagger;
import ru.ppr.cppk.entity.event.model.StationDevice;
import ru.ppr.cppk.entity.settings.PrivateSettings;
import ru.ppr.cppk.export.Exchange;
import ru.ppr.cppk.helpers.DeviceSessionInfo;
import ru.ppr.cppk.helpers.FilePathProvider;
import ru.ppr.cppk.helpers.PrivateSettingsHolder;
import ru.ppr.cppk.settings.SharedPreferencesUtils;
import ru.ppr.logger.Logger;
import ru.ppr.utils.FileUtils2;

/**
 * Восстанавливает полную резервную копию.
 * Поддерживает нвоый механизм восстановления, с шаблонами.
 *
 * @author Dmitry Nevolin
 */
public class FullBackupRestorer extends BaseBackupRestorer {

    private static final String TAG = Logger.makeLogTag(FullBackupRestorer.class);

    private static final String SHARED_PREFS_DIR_NAME = "shared_prefs";

    private final LocalDbBackupRestorer localDbBackupRestorer;
    private final NsiBackupRestorer nsiBackupRestorer;
    private final SecurityBackupRestorer securityBackupRestorer;
    private final PrinterDbBackupRestorer printerDbBackupRestorer;
    private final PrivateSettingsHolder privateSettingsHolder;
    private final DeviceSessionInfo deviceSessionInfo;
    private final FilePathProvider filePathProvider;
    private final Globals globals;

    @Inject
    FullBackupRestorer(BackupArchiveUnpacker backupArchiveUnpacker,
                       LocalDbBackupRestorer localDbBackupRestorer,
                       NsiBackupRestorer nsiBackupRestorer,
                       SecurityBackupRestorer securityBackupRestorer,
                       PrinterDbBackupRestorer printerDbBackupRestorer,
                       PrivateSettingsHolder privateSettingsHolder,
                       DeviceSessionInfo deviceSessionInfo,
                       FilePathProvider filePathProvider,
                       Globals globals) {
        super(backupArchiveUnpacker);
        this.localDbBackupRestorer = localDbBackupRestorer;
        this.nsiBackupRestorer = nsiBackupRestorer;
        this.securityBackupRestorer = securityBackupRestorer;
        this.printerDbBackupRestorer = printerDbBackupRestorer;
        this.privateSettingsHolder = privateSettingsHolder;
        this.deviceSessionInfo = deviceSessionInfo;
        this.filePathProvider = filePathProvider;
        this.globals = globals;
    }

    /**
     * Начинает восстановление полной резервной копии.
     *
     * @return true если успешно, false в противном случае.
     */
    public Pair<Boolean, String> start() {
        File backupDir = filePathProvider.getBackupsRestoreDir();
        try {
            Config config = new Config.Builder()
                    .setBackupDir(filePathProvider.getBackupsRestoreDir())
                    .setUnpackDir(filePathProvider.getBackupsTempDir())
                    .setTemplateFileNameList(provideTemplateFileNameList())
                    .build();
            File restoredBackup = restore(config);
            Logger.trace(TAG, "instead of restore #CPPKConnect just delete current");
            boolean deleteCppkConnect = deleteCppkConnect();
            // Восстанавливаем базы
            long terminalNumber = privateSettingsHolder.get().getTerminalNumber();
            boolean restoreLocal = localDbBackupRestorer.restore(restoredBackup);
            if (restoreLocal) {
                setTerminalNumber(terminalNumber);
            }
            boolean restoreNsi = nsiBackupRestorer.restore(restoredBackup);
            boolean restoreSecurity = securityBackupRestorer.restore(restoredBackup);
            boolean restorePrinterState = printerDbBackupRestorer.restore(restoredBackup);
            Logger.trace(TAG, "skip restore Feedback");
            Logger.trace(TAG, "skip restore Log");
            Logger.trace(TAG, "skip restore sft IN");
            Logger.trace(TAG, "skip restore sft OUT");
            Logger.trace(TAG, "skip restore sft WORKING");
            boolean restoreSharedPrefs = restoreSharedPrefs(restoredBackup);
            // Удаляем более ненужную папку с бекапом
            boolean clearGarbage = FileUtils2.deleteDir(restoredBackup, null);
            boolean result = deleteCppkConnect &&
                    restoreLocal && restoreNsi && restoreSecurity && restorePrinterState &&
                    restoreSharedPrefs &&
                    clearGarbage;
            return Pair.create(result, getLastUnpackArchiveFile().getAbsolutePath());
        } catch (Exception exception) {
            Logger.error(TAG, exception);
            return Pair.create(false, getLastUnpackArchiveFile() != null ?
                    getLastUnpackArchiveFile().getAbsolutePath() : backupDir.getAbsolutePath());
        }
    }

    private void setTerminalNumber(long terminalNumber) {
        PrivateSettings privateSettings = new PrivateSettings(privateSettingsHolder.get());
        privateSettings.setTerminalNumber(terminalNumber);
        Dagger.appComponent().privateSettingsRepository().savePrivateSettings(privateSettings);
        privateSettingsHolder.set(privateSettings);
        deviceSessionInfo.setCurrentStationDevice(StationDevice.getThisDevice());
        Logger.trace(TAG, "unstash terminalNumber to: " + terminalNumber);
    }

    /**
     * Удаляет папку #CPPKConnect.
     *
     * @return true если успешно, false в противном случае.
     */
    private boolean deleteCppkConnect() {
        Logger.trace(TAG, "start delete #CPPKConnect");
        File cppkConnectDir = new File(Exchange.DIR);
        boolean deleteCppkConnectDir = !cppkConnectDir.exists() || FileUtils2.deleteDir(cppkConnectDir, null);
        Logger.trace(TAG, "complete delete #CPPKConnect: " + deleteCppkConnectDir);

        return deleteCppkConnectDir;
    }

    /**
     * Восстанавливает SharedPreferences.
     * Если понадобится где-то еще вынести в хелпер
     *
     * @param restoredBackup архив с бекапом.
     * @return true если успешно, false в противном случае.
     */
    private boolean restoreSharedPrefs(File restoredBackup) throws IOException {
        Logger.trace(TAG, "start restore shared prefs");
        String terminalSerial = SharedPreferencesUtils.getSerialNumber(globals);
        Logger.trace(TAG, "stash terminalSerial: " + terminalSerial);
        Logger.trace(TAG, "replace shared prefs");
        File sharedPrefs = new File(globals.getApplicationInfo().dataDir, SHARED_PREFS_DIR_NAME);
        File restoredSharedPrefs = new File(restoredBackup, SHARED_PREFS_DIR_NAME);
        boolean copyRestoredSharedPrefs = FileUtils2.copyDir(restoredSharedPrefs, sharedPrefs, null);
        if (terminalSerial != null) {
            SharedPreferencesUtils.setSerialNumber(globals, terminalSerial);
            deviceSessionInfo.setCurrentStationDevice(StationDevice.getThisDevice());
            Logger.trace(TAG, "unstash terminalSerial: " + terminalSerial);
        }
        Logger.trace(TAG, "complete restore shared prefs: " + copyRestoredSharedPrefs);

        return copyRestoredSharedPrefs;
    }

    private List<String> provideTemplateFileNameList() {
        List<String> templateFileNameList = new ArrayList<>();
        templateFileNameList.add(localDbBackupRestorer.getDbTemplateFileName());
        templateFileNameList.add(nsiBackupRestorer.getDbTemplateFileName());
        templateFileNameList.add(securityBackupRestorer.getDbTemplateFileName());
        templateFileNameList.add(printerDbBackupRestorer.getDbTemplateFileName());
        return templateFileNameList;
    }

}
