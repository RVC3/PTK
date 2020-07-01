package ru.ppr.cppk.ui.fragment.pd.simple.model;

import java.util.Date;

/**
 * Услуга, отображаемая во View.
 *
 * @author Aleksandr Brazhkin
 */
public class ServicePdViewModel extends BasePdViewModel {
    /**
     * Дата начала действия услуги
     */
    private Date validityFromDate;
    /**
     * Дата окончания действия услуги
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
}
