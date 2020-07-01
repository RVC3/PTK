package ru.ppr.core.dataCarrier.pd.v18;

import android.support.annotation.NonNull;

import java.nio.ByteOrder;
import java.util.Date;

import ru.ppr.core.dataCarrier.DataCarrierUtils;
import ru.ppr.core.dataCarrier.pd.PdDecoder;
import ru.ppr.core.dataCarrier.pd.base.PdWithPaymentType;

/**
 * Декодер ПД v.18.
 *
 * @author Aleksandr Brazhkin
 */
public class PdV18Decoder implements PdDecoder {

    @Override
    public PdV18 decode(@NonNull byte[] data) {

        if (data.length < PdV18Structure.PD_SIZE)
            return null;

        byte[] orderNumberData = DataCarrierUtils.subArray(data, PdV18Structure.ORDER_NUMBER_BYTE_INDEX, PdV18Structure.ORDER_NUMBER_BYTE_LENGTH);
        int orderNumber = DataCarrierUtils.bytesToInt(orderNumberData, PdV18Structure.ORDER_NUMBER_BIT_INDEX, PdV18Structure.ORDER_NUMBER_BIT_LENGTH, ByteOrder.LITTLE_ENDIAN);

        byte startDayOffsetData = data[PdV18Structure.START_DAY_OFFSET_BYTE_INDEX];
        int startDayOffset = DataCarrierUtils.byteToInt(startDayOffsetData, PdV18Structure.START_DAY_OFFSET_BIT_INDEX, PdV18Structure.START_DAY_OFFSET_BIT_LENGTH);

        byte paymentTypeData = data[PdV18Structure.PAYMENT_TYPE_BYTE_INDEX];
        boolean paymentType = DataCarrierUtils.byteToBoolean(paymentTypeData, PdV18Structure.PAYMENT_TYPE_BIT_INDEX);

        byte[] saleDateTimeData = DataCarrierUtils.subArray(data, PdV18Structure.SALE_DATE_TIME_BYTE_INDEX, PdV18Structure.SALE_DATE_TIME_BYTE_LENGTH);
        long saleDateTimeLong = DataCarrierUtils.bytesToLong(saleDateTimeData, ByteOrder.LITTLE_ENDIAN);
        Date saleDateTime = DataCarrierUtils.unixTimestampToDate(saleDateTimeLong);

        byte[] tariffCodeData = DataCarrierUtils.subArray(data, PdV18Structure.TARIFF_BYTE_INDEX, PdV18Structure.TARIFF_BYTE_LENGTH);
        long tariffCode = DataCarrierUtils.bytesToLong(tariffCodeData, ByteOrder.LITTLE_ENDIAN);

        byte[] startCounterValueData = DataCarrierUtils.subArray(data, PdV18Structure.START_COUNTER_VALUE_BYTE_INDEX, PdV18Structure.START_COUNTER_VALUE_BYTE_LENGTH);
        int startCounterValue = DataCarrierUtils.bytesToInt(startCounterValueData, ByteOrder.LITTLE_ENDIAN);

        byte[] endCounterValueData = DataCarrierUtils.subArray(data, PdV18Structure.END_COUNTER_VALUE_BYTE_INDEX, PdV18Structure.END_COUNTER_VALUE_BYTE_LENGTH);
        int endCounterValue = DataCarrierUtils.bytesToInt(endCounterValueData, ByteOrder.LITTLE_ENDIAN);

        byte[] edsKeyNumberData = DataCarrierUtils.subArray(data, PdV18Structure.EDS_KEY_NUMBER_BYTE_INDEX, PdV18Structure.EDS_KEY_NUMBER_BYTE_LENGTH);
        long edsKeyNumber = DataCarrierUtils.bytesToLong(edsKeyNumberData, ByteOrder.LITTLE_ENDIAN);

        byte[] crcData = DataCarrierUtils.subArray(data, PdV18Structure.CRC_BYTE_INDEX, PdV18Structure.CRC_BYTE_LENGTH);

        PdV18Impl pdV18 = new PdV18Impl();
        pdV18.setOrderNumber(orderNumber);
        pdV18.setStartDayOffset(startDayOffset);
        pdV18.setPaymentType(paymentType ? PdWithPaymentType.PAYMENT_TYPE_CARD : PdWithPaymentType.PAYMENT_TYPE_CASH);
        pdV18.setSaleDateTime(saleDateTime);
        pdV18.setTariffCode(tariffCode);
        pdV18.setStartCounterValue(startCounterValue);
        pdV18.setEndCounterValue(endCounterValue);
        pdV18.setEdsKeyNumber(edsKeyNumber);
        pdV18.setCrc(crcData);

        return pdV18;
    }

}
