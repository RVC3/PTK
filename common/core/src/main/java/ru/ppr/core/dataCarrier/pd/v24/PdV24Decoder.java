package ru.ppr.core.dataCarrier.pd.v24;

import android.support.annotation.NonNull;

import java.nio.ByteOrder;
import java.util.Date;

import ru.ppr.core.dataCarrier.DataCarrierUtils;
import ru.ppr.core.dataCarrier.pd.PdDecoder;
import ru.ppr.core.dataCarrier.pd.base.PdWithPaymentType;

/**
 * Декодер ПД v.24.
 *
 * @author Aleksandr Brazhkin
 */
public class PdV24Decoder implements PdDecoder {

    @Override
    public PdV24 decode(@NonNull byte[] data) {

        if (data.length < PdV24Structure.PD_SIZE)
            return null;

        byte[] orderNumberData = DataCarrierUtils.subArray(data, PdV24Structure.ORDER_NUMBER_BYTE_INDEX, PdV24Structure.ORDER_NUMBER_BYTE_LENGTH);
        int orderNumber = DataCarrierUtils.bytesToInt(orderNumberData, PdV24Structure.ORDER_NUMBER_BIT_INDEX, PdV24Structure.ORDER_NUMBER_BIT_LENGTH, ByteOrder.LITTLE_ENDIAN);

        byte startDayOffsetData = data[PdV24Structure.START_DAY_OFFSET_BYTE_INDEX];
        int startDayOffset = DataCarrierUtils.byteToInt(startDayOffsetData, PdV24Structure.START_DAY_OFFSET_BIT_INDEX, PdV24Structure.START_DAY_OFFSET_BIT_LENGTH);

        byte paymentTypeData = data[PdV24Structure.PAYMENT_TYPE_BYTE_INDEX];
        boolean paymentType = DataCarrierUtils.byteToBoolean(paymentTypeData, PdV24Structure.PAYMENT_TYPE_BIT_INDEX);

        byte[] saleDateTimeData = DataCarrierUtils.subArray(data, PdV24Structure.SALE_DATE_TIME_BYTE_INDEX, PdV24Structure.SALE_DATE_TIME_BYTE_LENGTH);
        long saleDateTimeLong = DataCarrierUtils.bytesToLong(saleDateTimeData, ByteOrder.LITTLE_ENDIAN);
        Date saleDateTime = DataCarrierUtils.unixTimestampToDate(saleDateTimeLong);

        byte[] tariffCodeData = DataCarrierUtils.subArray(data, PdV24Structure.TARIFF_BYTE_INDEX, PdV24Structure.TARIFF_BYTE_LENGTH);
        long tariffCode = DataCarrierUtils.bytesToLong(tariffCodeData, ByteOrder.LITTLE_ENDIAN);

        byte[] edsKeyNumberData = DataCarrierUtils.subArray(data, PdV24Structure.EDS_KEY_NUMBER_BYTE_INDEX, PdV24Structure.EDS_KEY_NUMBER_BYTE_LENGTH);
        long edsKeyNumber = DataCarrierUtils.bytesToLong(edsKeyNumberData, ByteOrder.LITTLE_ENDIAN);

        PdV24Impl pdV24 = new PdV24Impl();
        pdV24.setOrderNumber(orderNumber);
        pdV24.setStartDayOffset(startDayOffset);
        pdV24.setPaymentType(paymentType ? PdWithPaymentType.PAYMENT_TYPE_CARD : PdWithPaymentType.PAYMENT_TYPE_CASH);
        pdV24.setSaleDateTime(saleDateTime);
        pdV24.setTariffCode(tariffCode);
        pdV24.setEdsKeyNumber(edsKeyNumber);

        return pdV24;
    }

}
