package ru.ppr.core.dataCarrier.pd.v7;

import android.support.annotation.NonNull;

import java.nio.ByteOrder;
import java.util.Date;

import ru.ppr.core.dataCarrier.DataCarrierUtils;
import ru.ppr.core.dataCarrier.pd.PdDecoder;
import ru.ppr.core.dataCarrier.pd.base.PdWithPaymentType;

/**
 * Декодер ПД v.7.
 *
 * @author Aleksandr Brazhkin
 */
public class PdV7Decoder implements PdDecoder {

    @Override
    public PdV7 decode(@NonNull byte[] data) {

        if (data.length < PdV7Structure.PD_SIZE)
            return null;

        byte[] orderNumberData = DataCarrierUtils.subArray(data, PdV7Structure.ORDER_NUMBER_BYTE_INDEX, PdV7Structure.ORDER_NUMBER_BYTE_LENGTH);
        int orderNumber = DataCarrierUtils.bytesToInt(orderNumberData, PdV7Structure.ORDER_NUMBER_BIT_INDEX, PdV7Structure.ORDER_NUMBER_BIT_LENGTH, ByteOrder.LITTLE_ENDIAN);

        byte startDayOffsetData = data[PdV7Structure.START_DAY_OFFSET_BYTE_INDEX];
        int startDayOffset = DataCarrierUtils.byteToInt(startDayOffsetData, PdV7Structure.START_DAY_OFFSET_BIT_INDEX, PdV7Structure.START_DAY_OFFSET_BIT_LENGTH);

        byte paymentTypeData = data[PdV7Structure.PAYMENT_TYPE_BYTE_INDEX];
        boolean paymentType = DataCarrierUtils.byteToBoolean(paymentTypeData, PdV7Structure.PAYMENT_TYPE_BIT_INDEX);

        byte[] saleDateTimeData = DataCarrierUtils.subArray(data, PdV7Structure.SALE_DATE_TIME_BYTE_INDEX, PdV7Structure.SALE_DATE_TIME_BYTE_LENGTH);
        long saleDateTimeLong = DataCarrierUtils.bytesToLong(saleDateTimeData, ByteOrder.LITTLE_ENDIAN);
        Date saleDateTime = DataCarrierUtils.unixTimestampToDate(saleDateTimeLong);

        byte[] tariffCodeData = DataCarrierUtils.subArray(data, PdV7Structure.TARIFF_BYTE_INDEX, PdV7Structure.TARIFF_BYTE_LENGTH);
        long tariffCode = DataCarrierUtils.bytesToLong(tariffCodeData, ByteOrder.LITTLE_ENDIAN);

        byte[] startCounterValueData = DataCarrierUtils.subArray(data, PdV7Structure.START_COUNTER_VALUE_BYTE_INDEX, PdV7Structure.START_COUNTER_VALUE_BYTE_LENGTH);
        int startCounterValue = DataCarrierUtils.bytesToInt(startCounterValueData, ByteOrder.LITTLE_ENDIAN);

        byte[] endCounterValueData = DataCarrierUtils.subArray(data, PdV7Structure.END_COUNTER_VALUE_BYTE_INDEX, PdV7Structure.END_COUNTER_VALUE_BYTE_LENGTH);
        int endCounterValue = DataCarrierUtils.bytesToInt(endCounterValueData, ByteOrder.LITTLE_ENDIAN);

        byte[] edsKeyNumberData = DataCarrierUtils.subArray(data, PdV7Structure.EDS_KEY_NUMBER_BYTE_INDEX, PdV7Structure.EDS_KEY_NUMBER_BYTE_LENGTH);
        long edsKeyNumber = DataCarrierUtils.bytesToLong(edsKeyNumberData, ByteOrder.LITTLE_ENDIAN);

        byte[] crcData = DataCarrierUtils.subArray(data, PdV7Structure.CRC_BYTE_INDEX, PdV7Structure.CRC_BYTE_LENGTH);

        PdV7Impl pdV7 = new PdV7Impl();
        pdV7.setOrderNumber(orderNumber);
        pdV7.setStartDayOffset(startDayOffset);
        pdV7.setPaymentType(paymentType ? PdWithPaymentType.PAYMENT_TYPE_CARD : PdWithPaymentType.PAYMENT_TYPE_CASH);
        pdV7.setSaleDateTime(saleDateTime);
        pdV7.setTariffCode(tariffCode);
        pdV7.setStartCounterValue(startCounterValue);
        pdV7.setEndCounterValue(endCounterValue);
        pdV7.setEdsKeyNumber(edsKeyNumber);
        pdV7.setCrc(crcData);

        return pdV7;
    }

}
