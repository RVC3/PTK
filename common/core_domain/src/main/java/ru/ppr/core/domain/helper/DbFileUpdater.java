package ru.ppr.core.domain.helper;

import android.support.annotation.NonNull;

import java.io.File;
import java.io.IOException;

import ru.ppr.logger.Logger;
import ru.ppr.utils.FileUtils2;

/**
 * Класс, выполняющий обновление файла БД приложения.
 *
 * @author Aleksandr Brazhkin
 */
public class DbFileUpdater {

    private static final String TAG = Logger.makeLogTag(DbFileUpdater.class);

    private static final String TEMP_FILE_SUFFIX = ".temp.db";
    private static final String BACKUP_FILE_SUFFIX = ".backup.db";

    /**
     * Выполняет обновление файла базы данных.
     *
     * @param src       Новый файл БД
     * @param dst       Место расположения БД приложения
     * @param deleteSrc {@code true} если исходный файл можно удалить, {@code false} иначе.
     * @return {@code true} при успешном выполнении операции, {@code false} иначе
     * @throws IOException В случае возникновения ошибки при работе с файлами
     */
    public boolean updateFromFile(@NonNull File src, @NonNull File dst, boolean deleteSrc) throws IOException {
        Logger.trace(TAG, "updateFromFile started");

        File temp;
        if (deleteSrc) {
            // Будем переименовывать исходный файл
            // Пока исходим из предположения, что файлы находятся на одном диске
            temp = src;
        } else {
            // Удаляем старый временный файл, если таковой вдруг остался
            temp = new File(dst.getParentFile(), TEMP_FILE_SUFFIX);
            if (temp.exists()) {
                Logger.error(TAG, "Temp file already exists: " + temp.getAbsolutePath());
                if (!temp.delete()) {
                    Logger.error(TAG, "Couldn't delete temp file: " + temp.getAbsolutePath());
                    return false;
                }
            }
            // Копируем файл в папку с БД
            if (!FileUtils2.copyFile(src, temp, null)) {
                Logger.trace(TAG, "Couldn't copy file " + src.getAbsolutePath() + " to " + temp.getAbsolutePath());
                return false;
            }
        }

        File backup = new File(dst.getParentFile(), BACKUP_FILE_SUFFIX);

        if (dst.exists()) {
            // Удаляем старый бекап, если таковой вдруг остался
            if (backup.exists()) {
                Logger.error(TAG, "Backup file already exists: " + backup.getAbsolutePath());
                if (!backup.delete()) {
                    Logger.error(TAG, "Couldn't delete backup file: " + backup.getAbsolutePath());
                    return false;
                }
            }
            // Делаем бекап существующего файла БД
            if (!FileUtils2.renameFile(dst, backup, null)) {
                Logger.error(TAG, "Couldn't rename file " + dst.getAbsolutePath() + " to " + backup.getAbsolutePath());
            }
        }

        if (!FileUtils2.renameFile(temp, dst, null)) {
            Logger.error(TAG, "Couldn't rename file " + temp.getAbsolutePath() + " to " + dst.getAbsolutePath());
            return false;
        }

        if (backup.exists() && !backup.delete()) {
            // Считаем операцию успешной несмотря на то, что не удалось удалить бекап
            Logger.error(TAG, "Couldn't delete backup file: " + backup.getAbsolutePath());
        }

        Logger.trace(TAG, "updateFromFile completed successfully");

        return true;
    }

}
