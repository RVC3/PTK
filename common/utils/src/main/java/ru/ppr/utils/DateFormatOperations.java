package ru.ppr.utils;

import android.annotation.SuppressLint;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import ru.ppr.logger.Logger;

/**
 * Класс для форматирования дат
 */
// В будущем: DateFormatUtils and remove from 'app' module.
@SuppressLint("SimpleDateFormat")
public class DateFormatOperations {
    static String TAG = "DateFormatOperations";

    private static final SimpleDateFormat dd_MM_yyy_HH_mm = new SimpleDateFormat("dd.MM.yyy HH:mm");
    private static final SimpleDateFormat dd_MM_yyyy_HH_mm = new SimpleDateFormat("dd.MM.yyyy HH:mm");
    private static final SimpleDateFormat dd_MM_yyyy = new SimpleDateFormat("dd.MM.yyyy");
    private static final SimpleDateFormat yyyy_MM_dd_T_HH_mm_ss_SSS_Z;
    private static final SimpleDateFormat dd_MM_yyy_HH_mm_ss = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
    private static final SimpleDateFormat dd_MM_yyy_N_HH_mm_ss = new SimpleDateFormat("dd.MM.yyyy\nHH:mm:ss");
    private static final SimpleDateFormat dd_MM_yyy = new SimpleDateFormat("dd.MM.yyyy");
    private static final SimpleDateFormat dd_MMMM_yyyy = new SimpleDateFormat("dd MMMM yyyy");
    private static final SimpleDateFormat dd_MMM_yyyy = new SimpleDateFormat("dd MMM yyyy");
    private static final SimpleDateFormat HH_mm = new SimpleDateFormat("HH:mm");
    private static final SimpleDateFormat HH_mm_ss = new SimpleDateFormat("HH:mm:ss");

    static {
        yyyy_MM_dd_T_HH_mm_ss_SSS_Z = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        yyyy_MM_dd_T_HH_mm_ss_SSS_Z.setTimeZone(TimeZone.getTimeZone("UTC"));
        HH_mm_ss.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    /**
     * Возвращает время в UTC формате в виде строки.
     *
     * @param date
     * @return
     */
    public synchronized static String getUtcString(Date date) {
        return yyyy_MM_dd_T_HH_mm_ss_SSS_Z.format(date);
    }

    /**
     * Возвращает timestamp в человекопонятном виде; pattern - dd.MM.yyyy HH:mm
     *
     * @return
     */
    public synchronized static String getDateForOut(Date date) {
        String curStringDate = null;
        if (date!=null) {
            try {
                curStringDate = dd_MM_yyy_HH_mm.format(date);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return curStringDate;
    }

    public synchronized static String getDateddMMyyyyHHmm(Date datetime) {
        String curStringDate = null;
        try {
            curStringDate = dd_MM_yyyy_HH_mm.format(datetime);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return curStringDate;
    }

    public synchronized static String getDateddMMyyyy(Date datetime) {
        String curStringDate = null;
        try {
            curStringDate = dd_MM_yyyy.format(datetime);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return curStringDate;
    }

    /**
     * For last time go from terminal; pattern - dd.MM.yyyy HH:mm:ss
     *
     * @param date
     * @return
     */
    public synchronized static String getDateddMMyyyyHHmmss(Date date) {
        String curStringDate = null;
        try {
            curStringDate = dd_MM_yyy_HH_mm_ss.format(date);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return curStringDate;
    }

    /**
     * For last time go from terminal; pattern - dd.MM.yyyy HH:mm:ss
     *
     * @param date
     * @return
     */
    public synchronized static String getDateddMMyyyyNHHmmss(Date date) {
        String curStringDate = null;
        try {
            curStringDate = dd_MM_yyy_N_HH_mm_ss.format(date);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return curStringDate;
    }

    /**
     * For PD valid from and PD valid Till Date; pattern - dd.MM.yyyy
     *
     * @param datetime
     * @return
     */
    public synchronized static String getOutDate(Date datetime) {
        String curStringDate = null;
        try {
            curStringDate = dd_MM_yyy.format(datetime);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return curStringDate;
    }

    /**
     * Возвращает текущую дату в виде строки, pattern - dd MMMM yyyy
     *
     * @param datetime
     * @return
     */
    public static String getDate(Date datetime) {
        String curStringDate = null;
        try {
            synchronized (DateFormatOperations.class) {
                curStringDate = dd_MMMM_yyyy.format(datetime);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return curStringDate;
    }

    /**
     * Возвращает текущее время в виде строки; pattern HH:mm
     *
     * @param date
     * @return
     */
    public synchronized static String getTime(Date date) {
        String curStringDate = null;
        try {
            curStringDate = HH_mm.format(date);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return curStringDate;
    }

    /**
     * Пытается превести время, переданное в виде строки в timestamp, использует паттерны:
     * dd MMMM yyyy HH:mm:ss.SSS;
     * dd-MM-yyyy HH:mm:ss.SSS;
     * dd-MM-yyyy HH:mm:ss.SSS;
     * yyyy-MM-dd HH:mm:ss.SSS;
     * yyyy-MM-dd HH:mm:ss
     * Если не один паттерн не подошел, возвращает null
     *
     * @param datetime
     * @return
     */
    public static Date getDateFrom1970(String datetime) {
        if (datetime == null)
            return null;
        try {
            return (new SimpleDateFormat("dd MMMM yyyy HH:mm:ss.SSS").parse(datetime));
        } catch (ParseException e) {
            Logger.info(TAG, "Can not convert date " + datetime + " to timestamp with pattert dd MMMM yyyy HH:mm:ss.SSS - " + e.getMessage());
        }
        try {
            return (new SimpleDateFormat("dd-MM-yyyy HH:mm:ss.SSS").parse(datetime));
        } catch (ParseException e) {
            Logger.info(TAG, "Can not convert date " + datetime + " to timestamp with pattert dd-MM-yyyy HH:mm:ss.SSS - " + e.getMessage());
        }
        try {
            return (new SimpleDateFormat("dd-MM-yyyy HH:mm:ss.SSS").parse(datetime));
        } catch (ParseException e) {
            Logger.info(TAG, "Can not convert date " + datetime + " to timestamp with pattert dd-MM-yyyy HH:mm:ss.SSS - " + e.getMessage());
        }
        try {
            return (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").parse(datetime));
        } catch (ParseException e) {
            Logger.info(TAG, "Can not convert date " + datetime + " to timestamp with pattert yyyy-MM-dd HH:mm:ss.SSS - " + e.getMessage());
        }
        try {
            return (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(datetime));
        } catch (ParseException e) {
            Logger.info(TAG, "Can not convert date " + datetime + " to timestamp with pattert yyyy-MM-dd HH:mm:ss - " + e.getMessage());
        }
        return null;
    }

    public synchronized static Date convertToTime(String time) {
        Date date;
        try {
            date = HH_mm_ss.parse(time);
        } catch (ParseException e) {
            e.printStackTrace();
            date = new Date(0);
        }
        return date;
    }
}
