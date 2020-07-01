package ru.ppr.cppk.sync.writer.base;

import android.support.annotation.Nullable;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * @author Aleksandr Brazhkin
 */
public class DateFormatter {

    private final Calendar calendar;
    private final StringBuilder sb;

    public DateFormatter() {
        calendar = Calendar.getInstance(Locale.getDefault());
        calendar.setTimeZone(TimeZone.getTimeZone("UTC"));
        sb = new StringBuilder(24);
    }

    /**
     * Форматирует дату для экспорта.
     *
     * @param date Дата
     * @return Строковое представление даты
     */
    public String formatDateForExport(@Nullable Date date) {
        if (date == null) {
            return null;
        }

        calendar.setTime(date);

        sb.setLength(0);
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        int second = calendar.get(Calendar.SECOND);
        int millisecond = calendar.get(Calendar.MILLISECOND);
        sb.append(year);
        sb.append('-');
        if (month < 10) {
            sb.append('0');
        }
        sb.append(month);
        sb.append('-');
        if (dayOfMonth < 10) {
            sb.append('0');
        }
        sb.append(dayOfMonth);
        sb.append('T');
        if (hour < 10) {
            sb.append('0');
        }
        sb.append(hour);
        sb.append(':');
        if (minute < 10) {
            sb.append('0');
        }
        sb.append(minute);
        sb.append(':');
        if (second < 10) {
            sb.append('0');
        }
        sb.append(second);
        sb.append('.');
        if (millisecond < 10) {
            sb.append('0');
            sb.append('0');
        } else if (millisecond < 100) {
            sb.append('0');
        }
        sb.append(millisecond);
        sb.append('Z');
        return sb.toString();
    }
}
