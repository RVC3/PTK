package ru.ppr.cppk.logic.pd.checker;

import java.util.Date;

import javax.inject.Inject;

import ru.ppr.nsi.entity.TypeOfDay;

/**
 * Чекер абонемента на рабочие дни
 *
 * @author Grigoriy Kashka
 */
public class SeasonTicketWorkingDaysChecker {

    private final TypeOfDayCalculator typeOfDayCalculator;
    private final TypeOfDayChecker typeOfDayChecker;

    @Inject
    public SeasonTicketWorkingDaysChecker(TypeOfDayCalculator typeOfDayCalculator,
                                          TypeOfDayChecker typeOfDayChecker) {
        this.typeOfDayCalculator = typeOfDayCalculator;
        this.typeOfDayChecker = typeOfDayChecker;
    }

    /**
     * Вернет true если абонемент на рабочие дни сегодня валиден
     *
     * @param nsiVersion
     * @return
     */
    public boolean check(int nsiVersion) {
        Date currentDate = new Date();
        TypeOfDay typeOfDay = typeOfDayCalculator.getTypeOfDay(currentDate, nsiVersion);
        return typeOfDayChecker.isWorkingValid(typeOfDay);
    }
}
