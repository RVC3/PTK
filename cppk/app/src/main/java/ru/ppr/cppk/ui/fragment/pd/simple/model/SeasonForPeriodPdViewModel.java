package ru.ppr.cppk.ui.fragment.pd.simple.model;

import java.util.Date;

/**
 * Абонемент на период, отображаемый во View.
 *
 * @author Aleksandr Brazhkin
 */
public class SeasonForPeriodPdViewModel extends TicketPdViewModel {
    /**
     * Дата начала действия абонемента
     */
    private Date validityFromDate;
    /**
     * Дата окончания действия абонемента
     */
    private Date validityToDate;
    /**
     * Флаг наличия ошибки
     * "ПД действует только в выходные дни"
     */
    private boolean weekendOnlyError;
    /**
     * Флаг наличия ошибки
     * "ПД действует только в будни"
     */
    private boolean workingDayOnlyError;
    /**
     * Флаг наличия ошибки
     * "Дата действия еще не наступила"
     */
    private boolean validityFromDateError;
    /**
     * Флаг наличия ошибки
     * "Срок действия истек"
     */
    private boolean validityToDateError;

    public Date getValidityFromDate() {
        return validityFromDate;
    }

    public void setValidityFromDate(Date validityFromDate) {
        this.validityFromDate = validityFromDate;
    }

    public Date getValidityToDate() {
        return validityToDate;
    }

    public void setValidityToDate(Date validityToDate) {
        this.validityToDate = validityToDate;
    }

    public boolean isWeekendOnlyError() {
        return weekendOnlyError;
    }

    public void setWeekendOnlyError(boolean weekendOnlyError) {
        this.weekendOnlyError = weekendOnlyError;
    }

    public boolean isWorkingDayOnlyError() {
        return workingDayOnlyError;
    }

    public void setWorkingDayOnlyError(boolean workingDayOnlyError) {
        this.workingDayOnlyError = workingDayOnlyError;
    }

    public boolean isValidityFromDateError() {
        return validityFromDateError;
    }

    public void setValidityFromDateError(boolean validityFromDateError) {
        this.validityFromDateError = validityFromDateError;
    }

    public boolean isValidityToDateError() {
        return validityToDateError;
    }

    public void setValidityToDateError(boolean validityToDateError) {
        this.validityToDateError = validityToDateError;
    }
}
