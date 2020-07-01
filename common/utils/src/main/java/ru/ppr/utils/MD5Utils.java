package ru.ppr.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import ru.ppr.logger.Logger;

/**
 * Набор утилит для работы с MD5
 *
 * @author Artem Ushakov
 */
public class MD5Utils {

    private static final String TAG = Logger.makeLogTag(MD5Utils.class);

    /**
     * Считает MD5 хеш файла
     *
     * @param src файл для расчёта хеша
     * @return хеш
     * @throws IOException              в случае ошибки работы с файлом
     * @throws NoSuchAlgorithmException если алгоритм MD5 не предоставлется платформой
     */
    public static byte[] from(File src) throws IOException, NoSuchAlgorithmException {
        InputStream srcStream = null;

        try {
            srcStream = new FileInputStream(src);
            MessageDigest digest = MessageDigest.getInstance("MD5");

            int len;
            byte[] buf = new byte[8192];
            while ((len = srcStream.read(buf)) > 0) {
                digest.update(buf, 0, len);
            }

            return digest.digest();
        } catch (IOException | NoSuchAlgorithmException exception) {
            Logger.error(TAG, exception);
            throw exception;
        } finally {
            if (srcStream != null) {
                try {
                    srcStream.close();
                } catch (IOException exception) {
                    Logger.error(TAG, exception);
                }
            }
        }
    }

    /**
     * @deprecated use {@link MD5Utils#from}
     */
    @Deprecated
    public static byte[] generateMD5(File file) {
        byte[] md5;
        InputStream inputStream = null;

        try {
            inputStream = new FileInputStream(file);
            byte[] buffer = new byte[4096];
            MessageDigest digest = MessageDigest.getInstance("MD5");
            int numRead = 0;
            while (numRead != -1) {
                numRead = inputStream.read(buffer);
                if (numRead > 0)
                    digest.update(buffer, 0, numRead);
            }
            md5 = digest.digest();
        } catch (Exception exception) {

            return null;
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (Exception e) {
                }
            }
        }
        return md5;
    }

    public static String convertHashToString(byte[] src) {
        if (src == null) {
            return null;
        }
        StringBuilder stringBuilder = new StringBuilder();
        for (byte b : src) {
            stringBuilder.append(Integer.toString((b & 0xff) + 0x100, 16).substring(1));
        }
        return stringBuilder.toString();
    }
}
