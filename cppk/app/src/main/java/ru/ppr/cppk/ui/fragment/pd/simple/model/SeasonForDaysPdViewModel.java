package ru.ppr.cppk.ui.fragment.pd.simple.model;

import java.util.Date;
import java.util.List;

/**
 * Абонемент на даты, отображаемый во View.
 *
 * @author Aleksandr Brazhkin
 */
public class SeasonForDaysPdViewModel extends TicketPdViewModel {
    /**
     * Даты действия ПД
     */
    private List<Date> validityDates;
    /**
     * Флаг наличия ошибки
     * "Билет не действует на текущую дату"
     */
    private boolean validityDatesError;

    public List<Date> getValidityDates() {
        return validityDates;
    }

    public void setValidityDates(List<Date> validityDates) {
        this.validityDates = validityDates;
    }

    public boolean isValidityDatesError() {
        return validityDatesError;
    }

    public void setValidityDatesError(boolean validityDatesError) {
        this.validityDatesError = validityDatesError;
    }
}
