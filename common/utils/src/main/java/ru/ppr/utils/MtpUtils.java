package ru.ppr.utils;

import android.content.Context;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;

import java.io.File;

import ru.ppr.logger.Logger;

/**
 * Набор функций для работы с MTP.
 *
 * @author Aleksandr Brazhkin
 */
public class MtpUtils {

    private static final String TAG = Logger.makeLogTag(MtpUtils.class);

    /**
     * Сообщает всем заинтересованным, что создан новый файл.
     * Необходимо вызывать чтобы созданный файл был доступет по mtp-протоколу.
     *
     * @param context Контекст
     * @param file    Созданный файл
     */
    public static void notifyFileCreated(@NonNull Context context, @NonNull File file) {
        //запретим оповещение для несуществующих файлов, папок и файлов из недоступного по mtp места
        if (file.exists() && file.isFile() && file.getAbsolutePath().indexOf("/data/data/") != 0) {
            Logger.trace(TAG, "notifyFileCreated: " + file.getAbsolutePath());
            MediaScannerConnection.scanFile(context, new String[]{file.getAbsolutePath()}, null, null);
            context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file)));
        }
    }

    /**
     * Посылает уведомление MTP клиентам что файл удален.
     *
     * @param context Контекст
     * @param file    Удаленный файл
     */
    public static void notifyFileDeleted(@NonNull Context context, @NonNull File file) {
        //запретим оповещение для файлов удаленных из недоступного по mtp места
        if (Build.VERSION.SDK_INT == 17 && file.getAbsolutePath().indexOf("/data/data/") != 0) {
            Logger.trace(TAG, "notifyFileDeleted: " + file.getAbsolutePath());
            context.getApplicationContext().sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.fromFile(file)));
        }
    }

    /**
     * Обновляет содержимое папки по mtp протоколу.
     * Работает рекурсивно.
     *
     * @param context Контекст
     * @param dir     Обновляемая папка
     */
    public static void refreshDir(@NonNull Context context, @NonNull File dir) {

        notifyFileDeleted(context, dir);

        File[] files = dir.listFiles();

        if (files != null) {
            for (File file : files) {
                if (file.isDirectory())
                    refreshDir(context, file);
                else
                    notifyFileCreated(context, file);
            }
        }
    }
}
