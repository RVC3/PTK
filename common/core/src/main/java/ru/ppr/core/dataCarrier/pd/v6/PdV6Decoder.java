package ru.ppr.core.dataCarrier.pd.v6;

import android.support.annotation.NonNull;

import java.nio.ByteOrder;
import java.util.Date;

import ru.ppr.core.dataCarrier.DataCarrierUtils;
import ru.ppr.core.dataCarrier.pd.PdDecoder;
import ru.ppr.core.dataCarrier.pd.base.PdWithPaymentType;

/**
 * Декодер ПД v.6.
 *
 * @author Aleksandr Brazhkin
 */
public class PdV6Decoder implements PdDecoder {

    @Override
    public PdV6 decode(@NonNull byte[] data) {

        if (data.length < PdV6Structure.PD_SIZE)
            return null;

        byte[] orderNumberData = DataCarrierUtils.subArray(data, PdV6Structure.ORDER_NUMBER_BYTE_INDEX, PdV6Structure.ORDER_NUMBER_BYTE_LENGTH);
        int orderNumber = DataCarrierUtils.bytesToInt(orderNumberData, PdV6Structure.ORDER_NUMBER_BIT_INDEX, PdV6Structure.ORDER_NUMBER_BIT_LENGTH, ByteOrder.LITTLE_ENDIAN);

        byte startDayOffsetData = data[PdV6Structure.START_DAY_OFFSET_BYTE_INDEX];
        int startDayOffset = DataCarrierUtils.byteToInt(startDayOffsetData, PdV6Structure.START_DAY_OFFSET_BIT_INDEX, PdV6Structure.START_DAY_OFFSET_BIT_LENGTH);

        byte paymentTypeData = data[PdV6Structure.PAYMENT_TYPE_BYTE_INDEX];
        boolean paymentType = DataCarrierUtils.byteToBoolean(paymentTypeData, PdV6Structure.PAYMENT_TYPE_BIT_INDEX);

        byte[] saleDateTimeData = DataCarrierUtils.subArray(data, PdV6Structure.SALE_DATE_TIME_BYTE_INDEX, PdV6Structure.SALE_DATE_TIME_BYTE_LENGTH);
        long saleDateTimeLong = DataCarrierUtils.bytesToLong(saleDateTimeData, ByteOrder.LITTLE_ENDIAN);
        Date saleDateTime = DataCarrierUtils.unixTimestampToDate(saleDateTimeLong);

        byte[] tariffCodeData = DataCarrierUtils.subArray(data, PdV6Structure.TARIFF_BYTE_INDEX, PdV6Structure.TARIFF_BYTE_LENGTH);
        long tariffCode = DataCarrierUtils.bytesToLong(tariffCodeData, ByteOrder.LITTLE_ENDIAN);

        byte[] exemptionCodeData = DataCarrierUtils.subArray(data, PdV6Structure.EXEMPTION_BYTE_INDEX, PdV6Structure.EXEMPTION_BYTE_LENGTH);
        int exemptionCode = DataCarrierUtils.bytesToInt(exemptionCodeData, ByteOrder.LITTLE_ENDIAN);

        byte[] forDaysData = DataCarrierUtils.subArray(data, PdV6Structure.FOR_DAYS_BYTE_INDEX, PdV6Structure.FOR_DAYS_BYTE_LENGTH);
        int forDays = DataCarrierUtils.bytesToInt(forDaysData, ByteOrder.LITTLE_ENDIAN);

        byte[] edsKeyNumberData = DataCarrierUtils.subArray(data, PdV6Structure.EDS_KEY_NUMBER_BYTE_INDEX, PdV6Structure.EDS_KEY_NUMBER_BYTE_LENGTH);
        long edsKeyNumber = DataCarrierUtils.bytesToLong(edsKeyNumberData, ByteOrder.LITTLE_ENDIAN);

        PdV6Impl pdV6 = new PdV6Impl();
        pdV6.setOrderNumber(orderNumber);
        pdV6.setStartDayOffset(startDayOffset);
        pdV6.setPaymentType(paymentType ? PdWithPaymentType.PAYMENT_TYPE_CARD : PdWithPaymentType.PAYMENT_TYPE_CASH);
        pdV6.setSaleDateTime(saleDateTime);
        pdV6.setTariffCode(tariffCode);
        pdV6.setExemptionCode(exemptionCode);
        pdV6.setForDays(forDays);
        pdV6.setEdsKeyNumber(edsKeyNumber);

        return pdV6;
    }

}
