package ru.ppr.core.dataCarrier.pd.v2;

import java.util.Date;

import ru.ppr.core.dataCarrier.pd.PdVersion;
import ru.ppr.core.dataCarrier.pd.base.BasePdWithoutPlace;
import ru.ppr.core.dataCarrier.pd.base.PdWithDirection;
import ru.ppr.core.dataCarrier.pd.base.PdWithPaymentType;

/**
 * ПД v.2.
 *
 * @author Aleksandr Brazhkin
 */
public class PdV2Impl extends BasePdWithoutPlace implements PdV2 {

    /**
     * Тип оплаты
     */
    @PdWithPaymentType.PaymentType
    private int paymentType;
    /**
     * Направление
     */
    @PdWithDirection.Direction
    private int direction;
    /**
     * Порядковый номер исходного ПД
     */
    private int sourceOrderNumber;
    /**
     * Дата и время продажи исходного ПД
     */
    private Date sourceSaleDateTime;
    /**
     * ID оборудования, сформировавшего исходный ПД
     */
    private long sourceDeviceId;
    /**
     * ЭЦП
     */
    private byte[] eds;

    public PdV2Impl() {
        super(PdVersion.V2, PdV2Structure.PD_SIZE);
    }

    @Override
    public int getDirection() {
        return direction;
    }

    /**
     * Метод для установки направления движения.
     *
     * @param direction направление движения.
     * @see PdWithDirection.Direction
     */
    public void setDirection(@PdWithDirection.Direction final int direction) {
        this.direction = direction;
    }

    @Override
    public byte[] getEds() {
        return eds;
    }

    /**
     * Метод для установки ЭЦП.
     *
     * @param eds ЭЦП
     */
    public void setEds(final byte[] eds) {
        this.eds = eds;
    }

    @Override
    public int getSourceOrderNumber() {
        return sourceOrderNumber;
    }

    /**
     * Метод для установки порядкового номера исходного ПД.
     *
     * @param sourceOrderNumber порядковый номер исходного ПД.
     */
    public void setSourceOrderNumber(final int sourceOrderNumber) {
        this.sourceOrderNumber = sourceOrderNumber;
    }

    @Override
    public Date getSourceSaleDateTime() {
        return sourceSaleDateTime;
    }

    /**
     * Метод для установки даты и времени исходного ПД.
     *
     * @param sourceSaleDateTime дата и время исходного ПД.
     */
    public void setSourceSaleDateTime(final Date sourceSaleDateTime) {
        this.sourceSaleDateTime = sourceSaleDateTime;
    }

    @Override
    public long getSourceDeviceId() {
        return sourceDeviceId;
    }

    /**
     * Метод для уставноки ID устройства исходного ПД.
     *
     * @param deviceId ID устройства исходного ПД.
     */
    public void setSourceDeviceId(final long deviceId) {
        sourceDeviceId = deviceId;
    }

    @Override
    public int getPaymentType() {
        return paymentType;
    }

    /**
     * Метод для установки тип оплаты.
     *
     * @param paymentType тип оплаты.
     * @see PdWithPaymentType.PaymentType
     */
    public void setPaymentType(@PdWithPaymentType.PaymentType final int paymentType) {
        this.paymentType = paymentType;
    }

}
