package ru.ppr.cppk.logic.pd.checker;

import java.util.Date;

import javax.inject.Inject;

import ru.ppr.nsi.entity.TypeOfDay;

/**
 * Чекер абонемента выходного дня
 *
 * @author Grigoriy Kashka
 */
public class SeasonTicketWeekendDaysChecker {

    private final TypeOfDayCalculator typeOfDayCalculator;
    private final TypeOfDayChecker typeOfDayChecker;

    @Inject
    public SeasonTicketWeekendDaysChecker(TypeOfDayCalculator typeOfDayCalculator,
                                          TypeOfDayChecker typeOfDayChecker) {
        this.typeOfDayCalculator = typeOfDayCalculator;
        this.typeOfDayChecker = typeOfDayChecker;
    }

    /**
     * Вернет true если абонемент на выходные дни сегодня валиден
     *
     * @param nsiVersion
     * @return
     */
    public boolean check(int nsiVersion) {
        Date currentDate = new Date();
        TypeOfDay typeOfDay = typeOfDayCalculator.getTypeOfDay(currentDate, nsiVersion);
        return typeOfDayChecker.isHolidayValid(typeOfDay);
    }

}
