package ru.ppr.cppk.helpers;

import android.content.Context;
import android.support.annotation.NonNull;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;

import ru.ppr.logger.Logger;
import ru.ppr.utils.FileUtils2;
import ru.ppr.utils.MtpUtils;
import ru.ppr.utils.ZipUtils;

/**
 * Пишет логи из {@value #DROPBOX_DIR_PATH} по указанному пути.
 * Не имеет ничего общего с http://www.dropbox.com.
 * На основании времени изменения папки {@value #DROPBOX_DIR_PATH} архивирует её
 * если требуется. Управляет количеством скопленных архивов.
 *
 * @author Dmitry Nevolin
 */
public class DropBoxLogWriter {

    private static final String TAG = Logger.makeLogTag(DropBoxLogWriter.class);

    public static final int MAX_LOG_COUNT = 10;
    private static final String DROPBOX_DIR_PATH = "/data/system/dropbox";
    private static final File DROPBOX_DIR = new File(DROPBOX_DIR_PATH);
    private static final SimpleDateFormat TIMESTAMP_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH_mm_ss", Locale.getDefault());

    private final File destLogDir;
    private final Context context;

    /**
     * Конструктор
     *
     * @param context Context
     * @param destLogDir Папка, в котрой следует складывать логи
     */
    public DropBoxLogWriter(@NonNull Context context, @NonNull File destLogDir) {
        this.context = context;
        this.destLogDir = destLogDir;
    }

    /**
     * Создает свежий архив папки {@value #DROPBOX_DIR_PATH}, если такового пока ещё нет.
     */
    public void createLog() {
        // Проверяем наличие папок и файлов
        if (!DROPBOX_DIR.exists()) {
            Logger.trace(TAG, "dropbox dir does not exists");

            return;
        }

        File[] filesInDir = DROPBOX_DIR.listFiles();

        if (filesInDir == null || filesInDir.length == 0) {
            Logger.trace(TAG, "dropbox dir is empty");

            return;
        }

        if (!destLogDir.exists() && !destLogDir.mkdirs()) {
            Logger.trace(TAG, "could not create dest log dir");

            return;
        }

        // Проверяем, есть ли новые логи
        long lastDropBoxTimestamp = DROPBOX_DIR.lastModified();
        File lastDropBoxLog = new File(destLogDir, getDropBoxLogFileName(lastDropBoxTimestamp));
        
        if (lastDropBoxLog.exists()) {
            Logger.trace(TAG, "last dropbox log already exists, lastDropBoxTimestamp = " + lastDropBoxTimestamp);
            
            return;
        }
        
        // Чистим старые логи ради освобождения места
        File[] existingLogs = destLogDir.listFiles();
        
        if (existingLogs != null && existingLogs.length >= MAX_LOG_COUNT) {
            Arrays.sort(existingLogs, (lhs, rhs) -> (int) (rhs.lastModified() - lhs.lastModified()));
            
            for (int i = MAX_LOG_COUNT; i < existingLogs.length; i++) {
                if (FileUtils2.deleteFile(existingLogs[i], context)) {
                    Logger.trace(TAG, "existing log " + existingLogs[i].getName() + " deleted");
                } else {
                    Logger.trace(TAG, "could not delete existing log " + existingLogs[i].getName());
                }
            }
        }
        // Копируем свежие логи
        archiveDropBoxDir(lastDropBoxLog);
    }

    /**
     * Возвращает имя архива с указанным таймштампом
     */
    private String getDropBoxLogFileName(long dropboxTimestamp) {
        String formattedDate = TIMESTAMP_FORMAT.format(new Date(dropboxTimestamp));
        
        return "dropbox_" + formattedDate + ".zip";
    }

    /**
     * Архивирует папку {@value #DROPBOX_DIR_PATH}
     * @param destArchive Архив, в который пишется папка
     */
    private boolean archiveDropBoxDir(File destArchive) {
        try {
            boolean res = ZipUtils.zipDir(DROPBOX_DIR, destArchive, false);
            
            if (res) {
                Logger.trace(TAG, "dropbox archive created " + destArchive.getName());
            } else {
                Logger.trace(TAG, "could not archive dropbox dir");
            }

            MtpUtils.notifyFileCreated(context, destArchive);
            
            return true;
        } catch (IOException e) {
            Logger.error(TAG, "could not archive dropbox dir", e);
        }

        return false;
    }

}
