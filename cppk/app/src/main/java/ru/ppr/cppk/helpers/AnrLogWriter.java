package ru.ppr.cppk.helpers;

import android.content.Context;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;

import ru.ppr.logger.Logger;
import ru.ppr.utils.FileUtils;
import ru.ppr.utils.ZipUtils;

/**
 * Пишет логи ANR по указанному пути.
 * На основании времени изменения папки {@value #ANR_DIR_PATH} архивирует её
 * если требуется. Управляет количеством скопленных архивов.
 * <p>
 * Created by Александр on 30.09.2016.
 */
public class AnrLogWriter {

    private static final String TAG = Logger.makeLogTag(AnrLogWriter.class);

    public static final int MAX_LOG_COUNT = 10;
    private static final String ANR_DIR_PATH = "/data/anr";
    private static final File ANR_DIR = new File(ANR_DIR_PATH);
    private static final SimpleDateFormat TIMESTAMP_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

    private final File destLogDir;
    private final Context context;

    /**
     * Конструктор
     *
     * @param context    Context
     * @param destLogDir Папка, в котрой следует складывать логи
     */
    public AnrLogWriter(Context context, File destLogDir) {
        if (destLogDir == null) {
            throw new NullPointerException("destArchiveDir shouldn't be null");
        }
        this.context = context;
        this.destLogDir = destLogDir;
    }

    /**
     * Создает свежий архив папки {@value #ANR_DIR_PATH}, если такового пока ещё нет.
     */
    public void createLog() {
        // Проверяем наличие папок и файлов
        if (!ANR_DIR.exists()) {
            Logger.trace(TAG, "anr dir does not exists");
            return;
        }
        File[] filesInDir = ANR_DIR.listFiles();
        if (filesInDir == null || filesInDir.length == 0) {
            Logger.trace(TAG, "anr dir is empty");
            return;
        }
        if (!destLogDir.exists()) {
            if (!destLogDir.mkdirs()) {
                Logger.trace(TAG, "could not create dest log dir");
                return;
            }
        }
        // Проверяем, есть ли новые логи
        long lastAnrTimestamp = ANR_DIR.lastModified();
        File lastAnrLog = new File(destLogDir, getAnrLogFileName(lastAnrTimestamp));
        if (lastAnrLog.exists()) {
            Logger.trace(TAG, "last anr log already exists, lastAnrTimestamp = " + lastAnrTimestamp);
            return;
        }
        // Чистим старые логи ради освобождения места
        File[] existingLogs = destLogDir.listFiles();
        if (existingLogs != null && existingLogs.length >= MAX_LOG_COUNT) {
            Arrays.sort(existingLogs, (lhs, rhs) -> (int) (rhs.lastModified() - lhs.lastModified()));
            for (int i = MAX_LOG_COUNT; i < existingLogs.length; i++) {
                if (existingLogs[i].delete()) {
                    Logger.trace(TAG, "existing log " + existingLogs[i].getName() + " deleted");
                    FileUtils.sendFileDeletedMtp(context, existingLogs[i]);
                } else {
                    Logger.trace(TAG, "could not delete existing log " + existingLogs[i].getName());
                }
            }
        }
        // Копируем свежие логи
        archiveAnrDir(lastAnrLog);
    }

    /**
     * Возвращает имя архива с указанным таймштампом
     *
     * @param anrTimestamp
     * @return
     */
    private String getAnrLogFileName(long anrTimestamp) {
        String formattedDate = TIMESTAMP_FORMAT.format(new Date(anrTimestamp));
        return "anr_" + formattedDate + ".zip";
    }

    /**
     * Архивирует папку {@value #ANR_DIR_PATH}
     *
     * @param destArchive Архив, в который пишется папка
     * @return
     */
    private boolean archiveAnrDir(File destArchive) {
        try {
            boolean res = ZipUtils.zipDir(ANR_DIR, destArchive, false);
            if (res) {
                Logger.trace(TAG, "anr archive created " + destArchive.getName());
            } else {
                Logger.trace(TAG, "could not archive anr dir");
            }
            FileUtils.makeFileVisibleMtp(context, destArchive);
            return true;
        } catch (IOException e) {
            Logger.error(TAG, "could not archive anr dir", e);
        }

        return false;
    }
}
