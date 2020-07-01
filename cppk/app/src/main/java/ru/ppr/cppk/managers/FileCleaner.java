package ru.ppr.cppk.managers;

import android.content.Context;
import android.support.annotation.NonNull;

import java.io.File;
import java.util.Arrays;
import java.util.Date;

import javax.inject.Inject;

import ru.ppr.logger.Logger;
import ru.ppr.utils.MtpUtils;
import ru.ppr.utils.NumberUtils;

/**
 * Менеджер очистки файлов
 *
 * @author Grigoriy Kashka
 */
public class FileCleaner {

    private final String TAG = Logger.makeLogTag(FileCleaner.class);

    private final Context context;

    @Inject
    FileCleaner(Context context) {
        this.context = context;
    }

    /**
     * Выполняет рекурсивное удаление файлов из директории, созданные более {@code maxFileAge} миллисекунд назад.
     *
     * @param dir        Директория
     * @param maxFileAge Максимальное допустимое время жизни файла в миллисекундах
     */
    public void clearDirRecursive(@NonNull File dir, long maxFileAge) {
        if (dir.exists()) {
            if (dir.isFile()) {
                deleteFile(dir, maxFileAge);
            } else {
                File[] list = dir.listFiles();
                if (list != null && list.length > 0) {
                    for (File item : list) {
                        clearDirRecursive(item, maxFileAge);
                    }
                }
            }
        }
    }

    /**
     * Выполняет удаление файла, если он создан более {@code maxFileAge} миллисекунд назад.
     *
     * @param file       Директория
     * @param maxFileAge Максимальное допустимое время жизни файла в миллисекундах
     */
    public void deleteFile(@NonNull File file, long maxFileAge) {
        long lastModifyTime = file.lastModified();
        if (lastModifyTime < System.currentTimeMillis() - maxFileAge) {
            if (file.delete()) {
                Logger.trace(TAG, "Удален файл: " + file.getAbsolutePath() + " lastModifyTime:" + (new Date(lastModifyTime)).toString());
                MtpUtils.notifyFileDeleted(context, file);
            }
        }
    }

    /**
     * Выполняет удаление старых файлов из директории так, чтобы итоговое количество файлов не превышало {@code maxAllowedFilesCount}.
     * Метод работает НЕрекусивно.
     * Директории в подсчете не участвуют.
     *
     * @param dir                  Директория
     * @param maxAllowedFilesCount Максимальное допустимое количество файлов в директории
     */
    public void clearDir(@NonNull File dir, int maxAllowedFilesCount) {
        if (!dir.exists()) {
            return;
        }
        if (dir.isFile()) {
            return;
        }
        File[] filesInDir = dir.listFiles(File::isFile);
        if (filesInDir == null || filesInDir.length <= maxAllowedFilesCount) {
            return;
        }
        // Сортируем так, чтобы самые свежие были сверху списка
        Arrays.sort(filesInDir, (f1, f2) -> NumberUtils.compare(f2.lastModified(), f1.lastModified()));
        for (int i = maxAllowedFilesCount; i < filesInDir.length; i++) {
            File file = filesInDir[i];
            if (file.delete()) {
                Logger.trace(TAG, "Удален файл: " + file.getAbsolutePath() + " index:" + i);
                MtpUtils.notifyFileDeleted(context, file);
            } else {
                Logger.error(TAG, "Не удалось удалить файл: " + file.getAbsolutePath() + " index:" + i);
            }
        }
    }
}
