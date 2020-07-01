package ru.ppr.cppk.logic.interactor;

import android.support.annotation.NonNull;

import java.util.Calendar;
import java.util.Date;

import javax.inject.Inject;

import ru.ppr.nsi.entity.TicketType;

/**
 * Калькулятор срока действия ПД.
 *
 * @author Aleksandr Brazhkin
 */
public class TicketTypeValidityPeriodCalculator {

    @Inject
    TicketTypeValidityPeriodCalculator() {

    }

    /**
     * Возвращает количество дней, на протяжении которых действует указанный тип ПД.
     *
     * @param ticketType Тип ПД
     * @param startDate  Дата начала действия ПД
     */
    public int getValidityPeriod(@NonNull TicketType ticketType, @NonNull Date startDate) {
        if (ticketType.getValidityPeriod() == null) {
            // Если период действия неизвестен, считаем его равным 1
            return 1;
        }

        switch (ticketType.getValidityPeriod()) {
            case DAY: {
                return getDurationOfValidity(ticketType);
            }
            case MONTH: {
                return calculateDaysInMonth(getDurationOfValidity(ticketType), startDate);
            }
            case DATE: {
                return getDurationOfValidity(ticketType);
            }
            default: {
                return 1;
            }
        }
    }

    /**
     * Возвращает срок действия ПД
     */
    private int getDurationOfValidity(@NonNull TicketType ticketType) {
        return ticketType.getDurationOfValidity() == null ? 1 : ticketType.getDurationOfValidity();
    }

    /**
     * Вычисляет фактическое количество дней в месяцах
     *
     * @param monthsCount Количество месяцев
     * @param startDate   Дата начала действия ПД
     */
    private int calculateDaysInMonth(int monthsCount, @NonNull Date startDate) {
        int daysTotalCount = 0;

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(startDate);

        for (int i = 0; i < monthsCount; i++) {
            // Получаем количество дней в месяце
            int dayInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
            daysTotalCount += dayInMonth;
            // Переходим на следующий месяц
            calendar.add(Calendar.MONTH, 1);
        }

        return daysTotalCount;
    }
}
