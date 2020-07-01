package ru.ppr.cppk.utils;

import android.text.TextUtils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import ru.ppr.logger.Logger;

/**
 * Created by Александр on 07.04.2016.
 */
public class DateTimeUtils {

    private static final String TAG = Logger.makeLogTag(DateTimeUtils.class);

    private static final TimeZone MOSCOW_TIME_ZONE = TimeZone.getTimeZone("Europe/Moscow");

    public static final SimpleDateFormat format_yyyy_MM_dd_HH_mm_ss = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());


    private static final ThreadLocal<SimpleDateFormat> SQLITE_DATETIME_FORMAT = new ThreadLocal<>();
    private static final ThreadLocal<SimpleDateFormat> SQLITE_DATE_FORMAT = new ThreadLocal<>();
    private static final ThreadLocal<SimpleDateFormat> SQLITE_TIME_FORMAT = new ThreadLocal<>();

    private static SimpleDateFormat getSQLiteDateFormat() {
        SimpleDateFormat format = SQLITE_DATETIME_FORMAT.get();
        if (format == null) {
            format = new SimpleDateFormat("yyyy-MM-dd");
            format.setTimeZone(TimeZone.getTimeZone("UTC"));
            SQLITE_DATETIME_FORMAT.set(format);
        }
        return format;
    }

    private static SimpleDateFormat getSQLiteDateTimeFormat() {
        SimpleDateFormat format = SQLITE_DATETIME_FORMAT.get();
        if (format == null) {
            format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            format.setTimeZone(TimeZone.getTimeZone("UTC"));
            SQLITE_DATETIME_FORMAT.set(format);
        }
        return format;
    }

    private static SimpleDateFormat getSQLiteTimeFormat() {
        SimpleDateFormat format = SQLITE_DATETIME_FORMAT.get();
        if (format == null) {
            format = new SimpleDateFormat("HH:mm:ss");
            format.setTimeZone(TimeZone.getTimeZone("UTC"));
            SQLITE_DATETIME_FORMAT.set(format);
        }
        return format;
    }

    public static Date getDateFromSQLite(String dateSQLite) {
        return getDateFromSQLite(dateSQLite, getSQLiteDateFormat());
    }

    public static String formatDateForSQLite(Date date) {
        return formatDateForSQLite(date, getSQLiteDateFormat());
    }

    public static Date getDateTimeFromSQLite(String dateSQLite) {
        return getDateFromSQLite(dateSQLite, getSQLiteDateTimeFormat());
    }

    public static String formatDateTimeForSQLite(Date date) {
        return formatDateForSQLite(date, getSQLiteDateTimeFormat());
    }

    public static Date getTimeFromSQLite(String dateSQLite) {
        return getDateFromSQLite(dateSQLite, getSQLiteTimeFormat());
    }

    public static String formatTimeForSQLite(Date date) {
        return formatDateForSQLite(date, getSQLiteTimeFormat());
    }

    private static Date getDateFromSQLite(String dateSQLite, DateFormat dateFormat) {
        Date date = null;

        if (TextUtils.isEmpty(dateSQLite)) {
            return date;
        }

        try {
            date = dateFormat.parse(dateSQLite);
        } catch (ParseException e) {
            Logger.error(TAG, e);
        }

        return date;
    }

    private static String formatDateForSQLite(Date date, DateFormat dateFormat) {
        String dateSQLite = dateFormat.format(date);
        return dateSQLite;
    }


    public static synchronized Date parseDate(String dateString, SimpleDateFormat simpleDateFormat, TimeZone timeZone) {

        Date date = null;

        if (TextUtils.isEmpty(dateString)) {
            return date;
        }

        try {
            simpleDateFormat.setTimeZone(timeZone);
            date = simpleDateFormat.parse(dateString);
        } catch (ParseException e) {
            Logger.error(TAG, e);
        }

        return date;
    }

    public static Date parseDate(String dateString, SimpleDateFormat simpleDateFormat) {
        return parseDate(dateString, simpleDateFormat, TimeZone.getTimeZone("UTC"));
    }

    public static String formatDate(Date date, SimpleDateFormat simpleDateFormat, TimeZone timeZone) {
        simpleDateFormat.setTimeZone(timeZone);
        return simpleDateFormat.format(date);
    }

    public static String formatDate(Date date, SimpleDateFormat simpleDateFormat) {
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        return simpleDateFormat.format(date);
    }

}
