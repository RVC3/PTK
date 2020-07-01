package ru.ppr.chit.ui.activity.ticketcontrol.interactor;

import android.support.annotation.NonNull;

import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import javax.inject.Inject;

/**
 * Калькулятор дат отправления для ПД с местом.
 *
 * @author Aleksandr Brazhkin
 */
public class DepartureDatesCalculator {

    @Inject
    DepartureDatesCalculator() {

    }

    /**
     * @param saleDateTime       Дата продажи ПД
     * @param departureDayOffset Дата отправления: количество дней с даты продажи. Значение 0: дата отправления соответствует дате продажи.
     * @param departureTime      Время отправления с точностью до минуты - количество минут с полуночи. Минимум: 0 - 00:00, максимум: 1439 - 23:59
     * @param forDays            Даты действия ПД. Каждому биту соответствует один из дней.
     *                           Нулевой бит - это дата первого отправления, он всегда = 1. остальные биты - последующие дни. Для разового ПД в единицу установлен только нулевой бит.
     * @return Даты отправления
     */
    @NonNull
    public List<Date> calc(Date saleDateTime, int departureDayOffset, int departureTime, int forDays) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeZone(TimeZone.getTimeZone("UTC"));
        // Отталкиваемся от даты продажи
        calendar.setTime(saleDateTime);
        // Смещаем дату отправления на нужное количество дней
        calendar.add(Calendar.DAY_OF_MONTH, departureDayOffset);
        // Сбрасываем время
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        // Устанавливаем нужное время в минутах
        calendar.add(Calendar.MINUTE, departureTime);
        return Collections.singletonList(calendar.getTime());
    }
}
