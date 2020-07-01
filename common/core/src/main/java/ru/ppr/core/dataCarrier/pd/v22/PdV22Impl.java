package ru.ppr.core.dataCarrier.pd.v22;

import ru.ppr.core.dataCarrier.pd.PdVersion;
import ru.ppr.core.dataCarrier.pd.base.BasePdWithoutPlace;

/**
 * ПД v.22.
 *
 * @author Aleksandr Brazhkin
 */
public class PdV22Impl extends BasePdWithoutPlace implements PdV22 {

    /**
     * Тип оплаты
     */
    @PaymentType
    private int paymentType;
    /**
     * Направление
     */
    @Direction
    private int direction;
    /**
     * Код льготы
     */
    private int exemptionCode;
    /**
     * ЭЦП
     */
    private byte[] eds;
    /**
     * Признак - требуется проверка входа на другой станции при выходе на текущей станции
     */
    private boolean passageToStationCheckRequired;
    /**
     * Признак - требуется активация билета (с помощью считывания специального ШК на ИТ на станции отправления)
     */
    private boolean activationRequired;
    /**
     * Номер телефона
     */
    private long phoneNumber;

    public PdV22Impl() {
        super(PdVersion.V22, PdV22Structure.PD_SIZE);
    }

    @Override
    public int getExemptionCode() {
        return exemptionCode;
    }

    public void setExemptionCode(int exemptionCode) {
        this.exemptionCode = exemptionCode;
    }

    @Override
    public byte[] getEds() {
        return eds;
    }

    public void setEds(byte[] eds) {
        this.eds = eds;
    }

    @PaymentType
    @Override
    public int getPaymentType() {
        return paymentType;
    }

    public void setPaymentType(@PaymentType int paymentType) {
        this.paymentType = paymentType;
    }

    @Direction
    @Override
    public int getDirection() {
        return direction;
    }

    public void setDirection(@Direction int direction) {
        this.direction = direction;
    }

    @Override
    public boolean isPassageToStationCheckRequired() {
        return passageToStationCheckRequired;
    }

    @Override
    public void setPassageToStationCheckRequired(boolean passageToStationCheckRequired) {
        this.passageToStationCheckRequired = passageToStationCheckRequired;
    }

    @Override
    public boolean isActivationRequired() {
        return activationRequired;
    }

    @Override
    public void setActivationRequired(boolean activationRequired) {
        this.activationRequired = activationRequired;
    }

    @Override
    public long getPhoneNumber() {
        return phoneNumber;
    }

    @Override
    public void setPhoneNumber(long phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

}
