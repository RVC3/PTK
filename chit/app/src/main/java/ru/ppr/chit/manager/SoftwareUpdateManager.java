package ru.ppr.chit.manager;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import java.io.File;

import javax.inject.Inject;
import javax.inject.Singleton;

import ru.ppr.chit.helpers.FilePathProvider;
import ru.ppr.logger.Logger;

/**
 * Менеджер обновления ПО
 *
 * @author Dmitry Nevolin
 */
@Singleton
public class SoftwareUpdateManager {

    private static final String TAG = Logger.makeLogTag(SoftwareUpdateManager.class);

    private static final String NEW_SOFTWARE_FILE_NAME = "chit.apk";

    private final Context context;
    private final FilePathProvider filePathProvider;

    @Inject
    SoftwareUpdateManager(Context context, FilePathProvider filePathProvider) {
        this.context = context;
        this.filePathProvider = filePathProvider;
    }

    public String getNewSoftwareFileName() {
        return NEW_SOFTWARE_FILE_NAME;
    }

    /**
     * Выполняет обновления софта, ищет файл chit.apk в папке для обновлений
     * и запускает его установку в случае обнаружения
     */
    public void updateSoftware() {
        Logger.info(TAG, "start updateSoftware");
        File[] softwareSyncDirFiles = filePathProvider.getSoftwareSyncDir().listFiles();
        if (softwareSyncDirFiles == null || softwareSyncDirFiles.length == 0) {
            Logger.info(TAG, "no new software found, skip update");
        } else {
            Logger.info(TAG, "start search new software file");
            for (File file : softwareSyncDirFiles) {
                String fileName = file.getName();
                Logger.info(TAG, fileName);
                if (NEW_SOFTWARE_FILE_NAME.equals(fileName)) {
                    Logger.info(TAG, "new software found: " + fileName);
                    startInstallApk(file);
                    return;
                }
            }
            Logger.info(TAG, "no new software found, skip update");
        }
    }

    private void startInstallApk(File apk) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setDataAndType(Uri.fromFile(apk), "application/vnd.android.package-archive");
        context.startActivity(intent);
    }

}
