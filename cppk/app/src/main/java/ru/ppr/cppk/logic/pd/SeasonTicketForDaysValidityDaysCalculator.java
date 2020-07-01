package ru.ppr.cppk.logic.pd;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

/**
 * Калькулятор дат действия абонемента на определенные даты.
 *
 * @author Grigoriy Kashka
 */
public class SeasonTicketForDaysValidityDaysCalculator {

    @Inject
    public SeasonTicketForDaysValidityDaysCalculator() {
    }

    /**
     * Возвращает список дат, в которые действует ПД.
     *
     * @param pdStartDate Дата начала действия ПД
     * @param days        Дни действия ПД (Битовое представление)
     * @return Список дат, в которые действует ПД.
     */
    @NonNull
    public List<Date> getValidityDays(Date pdStartDate, int days) {
        ArrayList<Date> dates = new ArrayList<>();
        for (int i = 0; i < 32; i++) {
            if (getBit(days, i) == 1) {
                Date date = new Date(pdStartDate.getTime() + TimeUnit.DAYS.toMillis(i));
                dates.add(date);
            }
        }
        return dates;
    }

    /**
     * Вернет последнюю дату действия ПД
     *
     * @param pdStartDate Дата начала действия ПД
     * @param days        Дни действия ПД (Битовое представление)
     * @return последняя дата действия ПД
     */
    @Nullable
    public Date getLastValidityDay(Date pdStartDate, int days) {
        for (int i = 31; i >= 0; i--) {
            if (getBit(days, i) == 1) {
                return new Date(pdStartDate.getTime() + TimeUnit.DAYS.toMillis(i));
            }
        }
        return null;
    }

    /**
     * Возвращает значение бита по индексу bitIndex из числа value.
     *
     * @param value    - число
     * @param bitIndex - индекс бита
     * @return Значение бита
     */
    private int getBit(int value, int bitIndex) {
        return (value >> bitIndex) & 1;
    }

}
