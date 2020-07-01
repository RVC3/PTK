package ru.ppr.core.dataCarrier.pd.base;

import ru.ppr.core.dataCarrier.pd.PdVersion;

/**
 * Базоый класс для ПД v.19, v.20, v.23, v.24.
 *
 * @author Aleksandr Brazhkin
 */
public abstract class BasePdV19V20V23V24 extends BasePdWithoutPlace implements PdWithPaymentType {

    /**
     * Тип оплаты
     */
    @PdWithPaymentType.PaymentType
    private int paymentType;

    public BasePdV19V20V23V24(PdVersion version, int size) {
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
}
