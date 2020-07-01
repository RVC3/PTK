package ru.ppr.utils;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;

import ru.ppr.logger.Logger;

/**
 * Набор функций для работы с файлами.
 *
 * @author Aleksandr Brazhkin
 */
public class FileUtils2 {

    private static final String TAG = Logger.makeLogTag(FileUtils.class);

    /**
     * Удаляет файл с оповещением по MTP.
     *
     * @param file    Удаляемый файл
     * @param context Контекст, {@code null}, если не требуется уведомление по MTP
     * @return {@code true} при успешном выполнении операции, {@code false} иначе
     */
    public static boolean deleteFile(@NonNull File file, @Nullable Context context) {
        if (!file.delete()) {
            return false;
        }
        if (context != null) {
            MtpUtils.notifyFileDeleted(context, file);
        }
        return true;
    }

    /**
     * Удаляет папку с оповещением по MTP.
     *
     * @param dir     Удаляемая папка
     * @param context Контекст, {@code null}, если не требуется уведомление по MTP
     * @return {@code true} при успешном выполнении операции, {@code false} иначе
     */
    public static boolean deleteDir(@NonNull File dir, @Nullable Context context) {
        if (!dir.exists()) {
            Logger.error(TAG, "deleteDir: dir not found: " + dir.getAbsolutePath());
            return false;
        }

        // list all the directory contents
        File files[] = dir.listFiles();

        if (files != null) {
            for (File file : files) {
                boolean result;
                // recursive deleting 
                if (file.isDirectory()) {
                    result = deleteDir(file, context);
                } else {
                    result = deleteFile(file, context);
                }
                if (!result) {
                    return false;
                }
            }
        }

        if (dir.delete()) {
            if (context != null) {
                MtpUtils.notifyFileDeleted(context, dir);
            }
            return true;
        } else {
            return false;
        }
    }

    /**
     * Очищает папку с оповещением по MTP.
     *
     * @param dir     Очищаемая папка
     * @param context Контекст, {@code null}, если не требуется уведомление по MTP
     * @return {@code true} при успешном выполнении операции, {@code false} иначе
     */
    public static boolean clearDir(@NonNull File dir, @Nullable Context context) {
        if (!dir.exists()) {
            Logger.error(TAG, "clearDir: dir not found: " + dir.getAbsolutePath());
            return false;
        }

        // list all the directory contents
        File files[] = dir.listFiles();

        if (files != null) {
            for (File file : files) {
                boolean result;
                // recursive deleting
                if (file.isDirectory()) {
                    result = deleteDir(file, context);
                } else {
                    result = deleteFile(file, context);
                }
                if (!result) {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * Копирует файл с оповещением по MTP.
     *
     * @param src     Исходный файл
     * @param dst     Файл назначения
     * @param context Контекст, {@code null}, если не требуется уведомление по MTP
     * @return {@code true} при успешном выполнении операции, {@code false} иначе
     * @throws IOException
     */
    public static boolean copyFile(@NonNull File src, @NonNull File dst, @Nullable Context context) throws IOException {

        if (!src.exists()) {
            Logger.error(TAG, "copyFile: src not found: " + src.getAbsolutePath());
            return false;
        }

        // if directory for file doesn't exist, create it
        File dstFileDir = dst.getParentFile();
        if (!dstFileDir.exists()) {
            if (!dstFileDir.mkdirs()) {
                Logger.error(TAG, "copyFile: couldn't create directory: " + dstFileDir.getAbsolutePath());
                return false;
            }
        }
        FileInputStream srcStream = null;
        FileOutputStream dstStream = null;
        FileChannel srcChannel = null;
        FileChannel dstChannel = null;
        try {
            if (dst.exists()) {
                Logger.trace(TAG, "copyFile: dst file already exists, deleting: " + dst.getAbsolutePath());
                if (!deleteFile(dst, context)) {
                    Logger.error(TAG, "copyFile: couldn't delete existing file: " + dst.getAbsolutePath());
                }
            }
            if (!dst.exists()) {
                if (!dst.createNewFile()) {
                    Logger.error(TAG, "copyFile: couldn't create new file: " + dst.getAbsolutePath());
                    return false;
                }
            }
            srcStream = new FileInputStream(src);
            dstStream = new FileOutputStream(dst);
            srcChannel = srcStream.getChannel();
            dstChannel = dstStream.getChannel();
            dstChannel.transferFrom(srcChannel, 0, srcChannel.size());

            if (context != null) {
                MtpUtils.notifyFileCreated(context, dst);
            }

            return true;
        } catch (IOException e) {
            throw e;
        } finally {
            if (srcChannel != null) {
                try {
                    srcChannel.close();
                } catch (IOException e) {
                    Logger.error(TAG, e);
                }
            }
            if (dstChannel != null) {
                try {
                    dstChannel.close();
                } catch (IOException e) {
                    Logger.error(TAG, e);
                }
            }
            if (srcStream != null) {
                try {
                    srcStream.close();
                } catch (IOException e) {
                    Logger.error(TAG, e);
                }
            }
            if (dstStream != null) {
                try {
                    dstStream.close();
                } catch (IOException e) {
                    Logger.error(TAG, e);
                }
            }
        }
    }

    /**
     * Копирует директорию с оповещением по MTP.
     *
     * @param src     Исходная директория
     * @param dst     Директория назначения
     * @param context Контекст, {@code null}, если не требуется уведомление по MTP
     * @return {@code true} при успешном выполнении операции, {@code false} иначе
     * @throws IOException
     */

    public static boolean copyDir(@NonNull File src, @NonNull File dst, @Nullable Context context) throws IOException {
        if (!src.exists()) {
            Logger.error(TAG, "copyDir: src not found: " + src.getAbsolutePath());
            return false;
        }

        // if directory not exists, create it
        if (!dst.exists()) {
            if (!dst.mkdirs()) {
                Logger.error(TAG, "copyDir: couldn't create directory: " + dst.getAbsolutePath());
                return false;
            }
        }

        // list all the directory contents
        File files[] = src.listFiles();

        if (files != null) {
            for (File file : files) {
                boolean result;
                // recursive copy
                if (file.isDirectory()) {
                    result = copyDir(file, new File(dst, file.getName()), context);
                } else {
                    result = copyFile(file, new File(dst, file.getName()), context);
                }
                if (!result) {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * Переименовывает файл с оповещением по MTP.
     *
     * @param src     Исходный файл
     * @param dst     Файл назначения
     * @param context Контекст, {@code null}, если не требуется уведомление по MTP
     * @return {@code true} при успешном выполнении операции, {@code false} иначе
     * @throws IOException
     */
    public static boolean renameFile(@NonNull File src, @NonNull File dst, @Nullable Context context) {
        if (!src.exists()) {
            Logger.error(TAG, "copyFile: src not found: " + src.getAbsolutePath());
            return false;
        }

        // if directory for file doesn't exist, create it
        File dstFileDir = dst.getParentFile();
        if (!dstFileDir.exists()) {
            if (!dstFileDir.mkdirs()) {
                Logger.error(TAG, "copyFile: couldn't create directory: " + dstFileDir.getAbsolutePath());
                return false;
            }
        }

        if (src.renameTo(dst)) {
            if (context != null) {
                MtpUtils.notifyFileDeleted(context, src);
                MtpUtils.notifyFileCreated(context, dst);
            }
            return true;
        } else {
            return false;
        }
    }

    /**
     * Копирует файл из assets в указанное место.
     *
     * @param context  Контекст приложения
     * @param fileName Имя файла в папке assets
     * @param dstFile  Файл, куда необходимо скопировать
     * @return {@code true} при успешном выполнении операции, {@code false} иначе
     * @throws IOException
     */
    public static boolean copyFileFromAssets(Context context, String fileName, File dstFile) throws IOException {
        InputStream in = context.getAssets().open(fileName);
        return copyFileFromStream(in, dstFile);
    }

    /**
     * Копирует файл из {@link InputStream} в указанное место.
     *
     * @param in      Входящий поток
     * @param dstFile Файл, куда необходимо скопировать
     * @return {@code true} при успешном выполнении операции, {@code false} иначе
     * @throws IOException
     */
    public static boolean copyFileFromStream(@NonNull InputStream in, File dstFile) throws IOException {
        OutputStream out = null;
        try {
            if (dstFile.exists()) {
                Logger.trace(TAG, "copyFileFromAssets: dstFile already exists, deleting");
                if (!dstFile.delete()) {
                    Logger.error(TAG, "could not delete file");
                    return false;
                }
            }
            out = new FileOutputStream(dstFile);

            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            out.flush();

            return true;
        } finally {
            try {
                in.close();
            } catch (IOException e) {
                Logger.error(TAG, e);
            }
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    Logger.error(TAG, e);
                }
            }
        }
    }

    /**
     * Читает данные из файла.
     *
     * @param file Читаемый файл
     * @return Массив байтов, представляющий данные файла
     * @throws IOException
     */
    public static byte[] readFileContent(File file) throws IOException {
        if (!file.exists()) {
            throw new FileNotFoundException("File not found: " + file.getAbsolutePath());
        }
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(file);
            // Create byte array large enough to hold the content of the file.
            byte fileContent[] = new byte[(int) file.length()];
            int readSize = fis.read(fileContent);
            if (readSize != fileContent.length) {
                Logger.trace(TAG, "Something went wrong: fileSize = " + fileContent.length + ", readSize = " + readSize);
            }
            return fileContent;
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    Logger.error(TAG, e);
                }
            }
        }
    }
}
