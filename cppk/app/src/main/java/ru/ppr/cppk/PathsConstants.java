package ru.ppr.cppk;

import android.os.Environment;

/**
 * Created by Кашка Григорий on 21.04.2016.
 * Пути к директориям
 */
public class PathsConstants {

    /* путь до центральной папки */
    public static final String DIR = Environment.getExternalStorageDirectory().getAbsolutePath() + "/CPPKInternal";


    /* путь до папки с логами */
    public static final String LOG = DIR + "/Log";

    /**
     * Путь до папки с логом критических ошибок
     */
    public static final String LOG_FATALS = LOG + "/Fatals";

    /**
     * Путь до папки с логом приложения ошибок
     */
    public static final String LOG_APP = LOG + "/AppLog";

    /**
     * Путь до папки с логом Zebra
     */
    public static final String LOG_ZEBRA = LOG + "/Zebra";
    /**
     * Путь до папки с логом InternalPrinter
     */
    public static final String LOG_i9000S_print = LOG + "/i9000S_LOG";

    /**
     * Путь до папки с логом Теста аккумулятора
     */
    public static final String LOG_BATTERY_TEST = LOG + "/BatteryTest";

    /**
     * Путь до папки с логами ANR
     */
    public static final String LOG_ANR = LOG + "/ANR";

    /**
     * Путь до папки с логами dropbox
     */
    public static final String LOG_DROPBOX = LOG + "/dropbox";

    /**
     * Путь до папки с отчетами об ошибках (скриншоты)
     */
    public static final String FEEDBACK = DIR + "/Feedback";

    /**
     * Путь до temp директории
     */
    public static final String TEMP = DIR + "/Temp";

    /**
     * Путь до папки с образами. Для Debug режима.
     */
    public static final String IMAGE = DIR + "/Image";
    public static final String IMAGE_RFID = IMAGE + "/rfid";
    public static final String IMAGE_BARCODE = IMAGE + "/barcode";

    /**
     * Путь до папки, с которой будет работать файловый принтер
     */
    public static final String PRINTER = DIR + "/Printer";


}
