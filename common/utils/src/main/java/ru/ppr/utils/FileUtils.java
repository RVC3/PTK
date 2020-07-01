package ru.ppr.utils;

import android.content.Context;
import android.support.annotation.Nullable;

import java.io.File;
import java.io.IOException;

import ru.ppr.logger.Logger;

/**
 * Класс с функциями работы с файлами.
 *
 * @author G.Kashka
 * @deprecated Use {@link FileUtils2} instead.
 */
@Deprecated
public class FileUtils {

    private static final String TAG = Logger.makeLogTag(FileUtils.class);

    /**
     * переименовывает и переносит файл from - sourse File, to - destination
     *
     * @param from
     * @param to
     * @return state of operation
     */
    @Deprecated
    public static boolean renameFile(Context g, File from, File to) {
        return FileUtils2.renameFile(from, to, g);
    }

    /**
     * удаляет папку и все содержимое
     *
     * @param fileOrDirectory
     */
    @Deprecated
    public static void deleteRecursive(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory()) {
            FileUtils2.deleteDir(fileOrDirectory, null);
        }
        FileUtils2.deleteFile(fileOrDirectory, null);
    }

    /**
     * Возвращает содержимое файла в виде строки
     *
     * @param filePath
     * @return
     * @throws Exception
     */
    @Deprecated
    public static String getStringFromFile(String filePath) {

        try {
            return new String(FileUtils2.readFileContent(new File(filePath)));
        } catch (IOException e) {
            Logger.error(TAG, e);
            return "";
        }
    }

    /**
     * Вернет содержимое файла строкой
     *
     * @param filePath
     */
    @Deprecated
    public static String getFileContent(String filePath) {
        return "File " + (new File(filePath).getName()) + " : " + getStringFromFile(filePath);
    }

    /**
     * Копирует файлы из src в dst.
     * НЕ производит отправку интента о том что данный файл доступен
     *
     * @param src октуда копируем
     * @param dst куда копируем
     */
    @Deprecated
    public static boolean copyWithOutMtp(File src, File dst) {
        try {
            return FileUtils2.copyFile(src, dst, null);
        } catch (IOException e) {
            Logger.error(TAG, e);
            return false;
        }
    }

    /**
     * Копирует файл
     *
     * @param context
     * @param src
     * @param dst
     */
    @Deprecated
    public static boolean copy(Context context, File src, File dst) {
        try {
            return FileUtils2.copyFile(src, dst, context);
        } catch (IOException e) {
            Logger.error(TAG, e);
            return false;
        }
    }

    /**
     * Сообщает всем заинтересованным, что создан новый файл. Необходимо
     * вызывать чтобы созданный файл был доступет по mtp-протоколу
     *
     * @param g
     * @param file
     */
    @Deprecated
    public static void makeFileVisibleMtp(Context g, File file) {
        if (g != null && file != null) {
            MtpUtils.notifyFileCreated(g, file);
        }
    }

    /**
     * Посылает уведомление MTP клиентам что файл удален
     *
     * @param g
     * @param file
     */
    @Deprecated
    public static void sendFileDeletedMtp(Context g, File file) {
        if (g != null && file != null) {
            MtpUtils.notifyFileDeleted(g, file);
        }
    }

    /**
     * Удаляет файл с оповещением MTP клиентов
     *
     * @param g
     * @param file
     */
    @Deprecated
    public static void deleteFileMtp(@Nullable Context g, File file) {
        if (file != null) {
            FileUtils2.deleteFile(file, g);
        }
    }

    @Deprecated
    public static void clearFolder(String folderPath) {
        FileUtils2.clearDir(new File(folderPath), null);
    }

    /**
     * Очищает папку с оповещением MTP клиентов об этом.
     *
     * @param context
     * @param folderPath
     */
    @Deprecated
    public static void clearFolderMtp(@Nullable Context context, String folderPath) {
        FileUtils2.clearDir(new File(folderPath), context);
    }


    /**
     * Обновляет папку по mtp протоколу. Делает уже несуществующие файлики
     * невидимыми, все вновь созданные - видимыми. Также обрабатывает вложенные
     * папки.
     *
     * @param g
     * @param folderPath
     */
    @Deprecated
    public static void updateFolderMtp(Context g, String folderPath) {
        if (folderPath == null)
            return;
        MtpUtils.refreshDir(g, new File(folderPath));
    }

    /**
     * Получает данные из файла. Используется для чтение образов карт
     */
    @Deprecated
    public static byte[] getDataFromFile(File dataFile) {
        try {
            return FileUtils2.readFileContent(dataFile);
        } catch (IOException e) {
            Logger.error(TAG, e);
            return null;
        }
    }

    /**
     * Копирует файл из asses в директорию приложения
     *
     * @param context  контекст приложения
     * @param filename имя файла в папке ассет
     * @param dstFile  файл, куда необходимо скопировать
     */
    @Deprecated
    public static void copyFileFromAsset(Context context, String filename, File dstFile) throws IOException {
        FileUtils2.copyFileFromAssets(context, filename, dstFile);
    }

}