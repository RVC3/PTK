package ru.ppr.core.ui.helper.crashreporter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;

import ru.ppr.logger.Logger;

/**
 * Сохраняет в файл информацию о краше.
 *
 * @author Aleksandr Brazhkin
 */
public class FileLoggerCrashListener implements CrashReporter.CrashListener {

    private static final String TAG = Logger.makeLogTag(FileLoggerCrashListener.class);

    public final static int DEFAULT_MAX_FILE_COUNT = 10;

    private final File logDir;
    private final int maxFileCount;

    public FileLoggerCrashListener(File logDir, int maxFileCount) {
        this.logDir = logDir;
        this.maxFileCount = maxFileCount;
    }

    @Override
    public void onCrash(CrashReport crashReport) {

        Logger.trace(TAG, "onCrash");

        saveContentToFile(crashReport.toString());
        Logger.flushQueueSync();
    }

    /**
     * Сохраняет описание краша в файл.
     *
     * @param fileContent Описание краша
     */
    private void saveContentToFile(String fileContent) {
        deleteOldFiles();

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault());
        String formattedDate = simpleDateFormat.format(new Date());
        String fileName = "CrashReport_" + formattedDate + ".stacktrace";

        File newFile = new File(logDir, fileName);

        FileOutputStream fos = null;
        try {
            Logger.trace(TAG, "Creating new crash report: " + newFile.getAbsolutePath());
            fos = new FileOutputStream(newFile);
            fos.write(fileContent.getBytes());
        } catch (IOException e) {
            Logger.trace(TAG, e);
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    Logger.trace(TAG, e);
                }
            }
        }
    }

    /**
     * Удаляет устаревшие логи, если требуется.
     * При достижении масксимального количества файлов удаляется половина файлов.
     */
    private void deleteOldFiles() {
        FilenameFilter filenameFilter = (dir1, name) -> name.endsWith(".stacktrace");
        File[] files = logDir.listFiles(filenameFilter);
        if (files != null) {
            if (files.length < maxFileCount) {
                return;
            }
            Arrays.sort(files, (lhs, rhs) -> (int) (lhs.lastModified() - rhs.lastModified()));
            for (int i = 0; i < maxFileCount / 2; i++) {
                Logger.trace(TAG, "Deleting old file: " + files[i].getAbsolutePath());
                if (!files[i].delete()) {
                    Logger.error(TAG, "Smth went wrong: could not delete file: " + files[i].getAbsolutePath());
                }
            }
        }
    }
}
