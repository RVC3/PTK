package ru.ppr.core.dataCarrier.pd.base;

import ru.ppr.core.dataCarrier.pd.PdVersion;

/**
 * Базоый класс для ПД v.7 и v.18.
 *
 * @author Dmitry Nevolin
 */
public abstract class BasePdV7V18 extends BasePdWithoutPlace implements PdWithPaymentType, PdWithCounter, PdWithCrc {

    /**
     * Тип оплаты
     */
    @PdWithPaymentType.PaymentType
    private int paymentType;
    /**
     * Стартовое значение счетчика.
     * Значение счетчика, при котором начинает действовать абонемент, включительно.
     */
    private int startCounterValue;
    /**
     * Конечное значение счетчика.
     * Значение счетчика, при котором завершает действовать абонемент, включительно.
     */
    private int endCounterValue;
    /**
     * Контрольная сумма CRC-16 всех полей ПД, включая номер ключа ЭЦП. Контрольная сумма не подписывается ЭЦП.
     */
    private byte[] crc;

    public BasePdV7V18(PdVersion version, int size) {
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
    public int getStartCounterValue() {
        return startCounterValue;
    }

    public void setStartCounterValue(int startCounterValue) {
        this.startCounterValue = startCounterValue;
    }

    @Override
    public int getEndCounterValue() {
        return endCounterValue;
    }

    public void setEndCounterValue(int endCounterValue) {
        this.endCounterValue = endCounterValue;
    }

    @Override
    public byte[] getCrc() {
        return crc;
    }

    public void setCrc(byte[] crc) {
        this.crc = crc;
    }

}
