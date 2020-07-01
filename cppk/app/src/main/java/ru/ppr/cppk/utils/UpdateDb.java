package ru.ppr.cppk.utils;

import android.content.res.AssetManager;

import com.google.common.io.Files;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import ru.ppr.cppk.Globals;
import ru.ppr.logger.Logger;

/**
 * Данный клас предоставляет функции для получения версий текущей и новой базы
 * данных, а так же функции для копирования новой бд в рабочий каталог
 * приложения
 *
 * @author Artem U.
 */
public class UpdateDb {

    private static final String TAG = "UpdateDB";

    /**
     * Производит копирование файлов
     *
     * @param src исходный файл
     * @param dst файл назначения
     */
    public static boolean copy(File src, File dst) {
        boolean copyIsSuccess = false;
        try {
            dst.delete();
            Logger.trace(TAG, "Copy from " + src.getAbsolutePath() + " to " + dst.getAbsolutePath());
            Files.copy(src, dst);
            copyIsSuccess = true;
        } catch (IOException e) {
            Logger.error(TAG, "Error while copy database file. " + e.getMessage(), e);
        }
        return copyIsSuccess;
    }

    /**
     * Копирует папку из Asses в директорию приложения
     *
     * @param g
     * @param assessName
     */
    public static void copyFileOrDirFromAssets(Globals g, String assessName) {
        AssetManager assetManager = g.getAssets();
        String assets[];
        try {
            assets = assetManager.list(assessName);
            if (assets.length == 0) {
                copyFile(g, assessName);
            } else {
                String fullPath = "/data/data/" + g.getPackageName() + "/" + assessName;
                File dir = new File(fullPath);
                if (!dir.exists())
                    dir.mkdir();
                for (int i = 0; i < assets.length; ++i) {
                    copyFileOrDirFromAssets(g, assessName + "/" + assets[i]);
                }
            }
        } catch (IOException ex) {
            Logger.error("tag", "I/O Exception", ex);
        }
    }

    /**
     * Копирует файл из asses в директорию приложения
     *
     * @param g
     * @param filename
     */
    public static void copyFile(Globals g, String filename) {
        AssetManager assetManager = g.getAssets();

        InputStream in = null;
        OutputStream out = null;
        try {
            in = assetManager.open(filename);
            String newFileName = "/data/data/" + g.getPackageName() + "/" + filename;
            (new File(newFileName)).delete();
            out = new FileOutputStream(newFileName);

            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            out.flush();
        } catch (Exception e) {
            Logger.error("tag", e.getMessage());
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    Logger.error(TAG, "Error close InputStream", e);
                }
            }

            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    Logger.error(TAG, "Error close OutputStream", e);
                }
            }
        }

    }
}
