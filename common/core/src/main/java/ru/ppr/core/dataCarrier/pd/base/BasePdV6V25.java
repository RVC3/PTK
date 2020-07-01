package ru.ppr.core.dataCarrier.pd.base;

import ru.ppr.core.dataCarrier.pd.PdVersion;

/**
 * Базоый класс для ПД v.6 и v.25.
 *
 * @author Grigoriy Kashka
 */
public abstract class BasePdV6V25 extends BasePdWithoutPlace implements PdWithPaymentType, PdWithExemption, PdForDays {

    /**
     * Тип оплаты
     */
    @PdWithPaymentType.PaymentType
    private int paymentType;
    /**
     * Код льготы
     */
    private int exemptionCode;
    /**
     * Даты действия ПД. Каждому биту соответствует один из дней.
     */
    private int forDays;

    public BasePdV6V25(PdVersion version, int size) {
        super(version, size);
    }

    @PdWithPaymentType.PaymentType
    @Override
    public int getPaymentType() {
        return paymentType;
    }

    public void setPaymentType(@PdWithPaymentType.PaymentType int paymentType) {
        this.paymentType = paymentType;
    }

    @Override
    public int getExemptionCode() {
        return exemptionCode;
    }

    public void setExemptionCode(int exemptionCode) {
        this.exemptionCode = exemptionCode;
    }

    @Override
    public int getForDays() {
        return forDays;
    }

    public void setForDays(int forDays) {
        this.forDays = forDays;
    }
}
