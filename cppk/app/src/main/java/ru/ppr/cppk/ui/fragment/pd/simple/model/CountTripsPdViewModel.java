package ru.ppr.cppk.ui.fragment.pd.simple.model;

import java.util.Date;

/**
 * Абонемент на количество поездок, отображаемый во View.
 *
 * @author Aleksandr Brazhkin
 */
public class CountTripsPdViewModel extends TicketPdViewModel {
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
     * "Дата действия еще не наступила"
     */
    private boolean validityFromDateError;
    /**
     * Флаг наличия ошибки
     * "Срок действия истек"
     */
    private boolean validityToDateError;
    /**
     * Количество оставшихся поездок
     */
    private int availableTripsCount;
    /**
     * Время последнего прохода
     * {@code null} если прохода не было
     */
    private Date lastPassageTime;
    /**
     * Максимальный интревал времени с момента последнего списания поездки
     */
    private Integer maxHoursAgo;
    /**
     * Флаг наличия ошибки во времени последнего прохода
     */
    private boolean lastPassageError;
    /**
     * Флаг отсутствия поездок для списания
     */
    private boolean noTripsError;

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

    public int getAvailableTripsCount() {
        return availableTripsCount;
    }

    public void setAvailableTripsCount(int availableTripsCount) {
        this.availableTripsCount = availableTripsCount;
    }

    public Date getLastPassageTime() {
        return lastPassageTime;
    }

    public void setLastPassageTime(Date lastPassageTime) {
        this.lastPassageTime = lastPassageTime;
    }

    public int getMaxHoursAgo() {
        return maxHoursAgo;
    }

    public void setMaxHoursAgo(int maxHoursAgo) {
        this.maxHoursAgo = maxHoursAgo;
    }

    public boolean isLastPassageError() {
        return lastPassageError;
    }

    public void setLastPassageError(boolean lastPassageError) {
        this.lastPassageError = lastPassageError;
    }

    public boolean isNoTripsError() {
        return noTripsError;
    }

    public void setNoTripsError(boolean noTripsError) {
        this.noTripsError = noTripsError;
    }
}
