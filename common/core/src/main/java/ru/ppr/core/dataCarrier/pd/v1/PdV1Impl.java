package ru.ppr.core.dataCarrier.pd.v1;

import ru.ppr.core.dataCarrier.pd.PdVersion;
import ru.ppr.core.dataCarrier.pd.base.BasePdWithoutPlace;

/**
 * ПД v.1.
 *
 * @author Aleksandr Brazhkin
 */
public class PdV1Impl extends BasePdWithoutPlace implements PdV1 {

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

    public PdV1Impl() {
        super(PdVersion.V1, PdV1Structure.PD_SIZE);
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
}
