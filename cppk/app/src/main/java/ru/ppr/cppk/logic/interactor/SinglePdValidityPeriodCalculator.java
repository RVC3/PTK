package ru.ppr.cppk.logic.interactor;

import android.support.annotation.NonNull;

import java.util.Calendar;
import java.util.Date;

import javax.inject.Inject;

import ru.ppr.cppk.localdb.model.TicketWayType;
import ru.ppr.cppk.logic.pd.checker.TypeOfDayChecker;
import ru.ppr.cppk.logic.pd.checker.TypeOfDayCalculator;
import ru.ppr.nsi.entity.TypeOfDay;

/**
 * Калькулятор количества дней действия для разового билета.
 *
 * @author Grigoriy Kashka
 */
public class SinglePdValidityPeriodCalculator {

    private final TypeOfDayCalculator typeOfDayCalculator;
    private final TypeOfDayChecker typeOfDayChecker;

    @Inject
    SinglePdValidityPeriodCalculator(TypeOfDayCalculator typeOfDayCalculator,
                                     TypeOfDayChecker typeOfDayChecker) {
        this.typeOfDayCalculator = typeOfDayCalculator;
        this.typeOfDayChecker = typeOfDayChecker;
    }

    /**
     * Вычисляет количество дней действия для разового билета.
     *
     * @param pdStartDate Дата начала действия ПД
     * @param wayType     Направление ПД
     * @param nsiVersion  Версия НСИ
     * @return Количество дней действия ПД
     */
    public int calcValidityPeriod(@NonNull Date pdStartDate, @NonNull TicketWayType wayType, int nsiVersion) {
        /*
         * Если билет туда-обратно, то определяем тип следующего дня после
         * покупки билета. Если следующий день является выходным или
         * праздничным, то увеличиваем количество дней действия на 1, до тех
         * пор, пока не дойдем до рабочего дня.
         */

        int validityPeriod = 1;

        if (wayType == TicketWayType.OneWay) {
            // Разовый ПД в одном направлении действует 1 день
            return validityPeriod;
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(pdStartDate);
        while (true) {
            validityPeriod++;
            calendar.add(Calendar.DAY_OF_YEAR, 1);
            TypeOfDay typeOfDay = typeOfDayCalculator.getTypeOfDay(calendar.getTime(), nsiVersion);
            // Возможна ситуация, что в календаре нет такого дня из-за кривого НСИ
            // или еще какого-нибудь бага, поэтому добавляем доп. ограничения
            if (typeOfDayChecker.isWorkingValid(typeOfDay) || typeOfDay == TypeOfDay.UNKNOWN || validityPeriod > 30) {
                break;
            }
        }

        return validityPeriod;
    }
}
