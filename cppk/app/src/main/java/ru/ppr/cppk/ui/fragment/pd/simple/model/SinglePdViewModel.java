package ru.ppr.cppk.ui.fragment.pd.simple.model;

import java.util.Date;

/**
 * Разовый ПД, отображаемый во View.
 *
 * @author Aleksandr Brazhkin
 */
public class SinglePdViewModel extends TicketPdViewModel {
    /**
     * Флаг, что ПД отображается в режиме контроля.
     */
    private boolean controlMode;
    /**
     * Дата действия ПД
     */
    private Date validityDate;
    /**
     * Направление билета туда-обратно
     * {@code true} = направление "Туда-обратно"
     * {@code false} = направление "Туда"
     */
    private boolean twoWay;
    /**
     * Флаг наличия ошибки
     * "Билет не действует на текущую дату"
     */
    private boolean validityDateError;
    /**
     * Флаг, что билет был сейчас продан
     */
    private boolean soldNow;

    public boolean isControlMode() {
        return controlMode;
    }

    public void setControlMode(boolean controlMode) {
        this.controlMode = controlMode;
    }

    public Date getValidityDate() {
        return validityDate;
    }

    public void setValidityDate(Date validityDate) {
        this.validityDate = validityDate;
    }

    public boolean isTwoWay() {
        return twoWay;
    }

    public void setTwoWay(boolean twoWay) {
        this.twoWay = twoWay;
    }

    public boolean isValidityDateError() {
        return validityDateError;
    }

    public void setValidityDateError(boolean validityDateError) {
        this.validityDateError = validityDateError;
    }

    public boolean isSoldNow() {
        return soldNow;
    }

    public void setSoldNow(boolean soldNow) {
        this.soldNow = soldNow;
    }
}
