package ru.ppr.inpas.lib.utils;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import ru.ppr.inpas.lib.logger.InpasLogger;

/**
 * Класс для представления даты согласно протоколу SA.
 *
 * @see ru.ppr.inpas.lib.protocol.SaPacket
 * @see ru.ppr.inpas.lib.protocol.model.SaField
 */
public class DateFormatter {

    private static final String TAG = InpasLogger.makeTag(DateFormatter.class);
    private static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("yyyyMMddHHmmss");

    /**
     * Метод возвращает дату отформатированную согласно протоколу SA.
     *
     * @param date дата для форматирования.
     * @return дата отформатированная согласно протоколу SA.
     */
    private static String getDate(@NonNull final Date date) {
        return DATE_FORMATTER.format(date);
    }

    /**
     * Метод возвращает дату из строки содержащей дату согласно протоколу SA.
     *
     * @param textDate строка содержащая дату согласно протоколу SA.
     * @return дата из передаваемой строки.
     */
    @Nullable
    public static Date getDate(@NonNull final String textDate) {
        Date date = null;

        try {
            date = DATE_FORMATTER.parse(textDate);
        } catch (ParseException ex) {
            InpasLogger.error(TAG, ex);
        }

        return date;
    }

    /**
     * Метод возвращает текущую дату отформатированную согласно протоколу SA.
     *
     * @return значение текущей даты отформатированное согласно протоколу SA.
     */
    public static String now() {
        return getDate(new Date());
    }

}
