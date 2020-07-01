package ru.ppr.cppk.ui.helper;

import android.support.annotation.NonNull;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

/**
 * Преобразователь в строку списка дат.
 *
 * @author Aleksandr Brazhkin
 */
public class DateListStringifier {

    private final SimpleDateFormat dayOnlyFormat = new SimpleDateFormat("dd");
    private final SimpleDateFormat withMonthFormat = new SimpleDateFormat("dd MMMM");
    private final SimpleDateFormat withYearFormat = new SimpleDateFormat("dd MMMM yyyy");

    @Inject
    public DateListStringifier() {

    }

    /**
     * Преобразует список дата в строку.
     *
     * @param dates Список дат
     * @return Строковое представление
     */
    @NonNull
    public String stringify(@NonNull List<Date> dates) {
        StringBuilder sb = new StringBuilder();
        int size = dates.size();
        for (int i = 0; i < size; i++) {
            Date currentDate = dates.get(i);
            if (i != 0) {
                // Если это не первая запись в списке, лобаляем запятую
                sb.append(", ");
            }
            if (i < size - 1) {
                // Если есть следующая дата
                Calendar currentCalendar = Calendar.getInstance();
                currentCalendar.setTime(currentDate);
                Calendar nextCalendar = Calendar.getInstance();
                nextCalendar.setTime(dates.get(i + 1));
                if (currentCalendar.get(Calendar.YEAR) == nextCalendar.get(Calendar.YEAR)) {
                    // Если совпадает год
                    if (currentCalendar.get(Calendar.MONTH) == nextCalendar.get(Calendar.MONTH)) {
                        // Если совпадает месяц
                        sb.append(dayOnlyFormat.format(currentDate));
                    } else {
                        // Если не совпадает месяц
                        sb.append(withMonthFormat.format(currentDate));
                    }
                } else {
                    // Если не совпадает год
                    sb.append(withYearFormat.format(currentDate));
                }
            } else {
                // Если нет следующей даты
                sb.append(withYearFormat.format(currentDate));
            }
        }
        return sb.toString();
    }
}
