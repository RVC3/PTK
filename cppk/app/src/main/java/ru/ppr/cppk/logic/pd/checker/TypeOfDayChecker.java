package ru.ppr.cppk.logic.pd.checker;

import android.support.annotation.NonNull;

import javax.inject.Inject;

import ru.ppr.nsi.entity.TypeOfDay;

/**
 * Класс, выполняющий проверку типа дня.
 *
 * @author Aleksandr Brazhkin
 */
public class TypeOfDayChecker {

    @Inject
    TypeOfDayChecker() {

    }

    /**
     * Проверяет, валиден ли абонемент на выходные дни в день {@code typeOfDate}.
     * <p>
     * Абонемент выходного дня действует по:
     * выходным и праздничным дням,
     * в день перед выходным или праздником,
     * в день после выходного или праздника.
     */
    public boolean isHolidayValid(@NonNull TypeOfDay typeOfDay) {
        switch (typeOfDay) {
            case SATURDAY:
            case SUNDAY:
            case PRE_HOLIDAY:
            case HOLIDAY:
            case REGIONAL_HOLIDAY:
            case HOLIDAY_TRANSFER:
            case POST_HOLIDAY:
            case DAY_OFF:
                return true;
            default:
                return false;
        }
    }

    /**
     * Проверяет, валиден ли абонемент на рабочие дни в день {@code typeOfDate}.
     * <p/>
     * Абонемент рабочего дня действителен только по рабочим дням.
     * При переносе выходных/праздничных и рабочих дней такие билеты:
     * действительны в выходные дни, объявленные рабочими,
     * недействительны в рабочие дни, объявленные нерабочими.
     */
    public boolean isWorkingValid(@NonNull TypeOfDay typeOfDay) {
        switch (typeOfDay) {
            case WORKING_DAY:
            case PRE_HOLIDAY:
            case REGIONAL_BUSINESS_DAY:
            case WORKING_DAY_TRANSFER:
            case POST_HOLIDAY:
                return true;
            default:
                return false;
        }
    }

}
