package ru.ppr.core.dataCarrier.pd.v19;

import android.support.annotation.NonNull;

import java.nio.ByteOrder;
import java.util.Date;

import ru.ppr.core.dataCarrier.DataCarrierUtils;
import ru.ppr.core.dataCarrier.pd.PdDecoder;
import ru.ppr.core.dataCarrier.pd.base.PdWithPaymentType;

/**
 * Декодер ПД v.19.
 *
 * @author Aleksandr Brazhkin
 */
public class PdV19Decoder implements PdDecoder {

    @Override
    public PdV19 decode(@NonNull byte[] data) {

        if (data.length < PdV19Structure.PD_SIZE)
            return null;

        byte[] orderNumberData = DataCarrierUtils.subArray(data, PdV19Structure.ORDER_NUMBER_BYTE_INDEX, PdV19Structure.ORDER_NUMBER_BYTE_LENGTH);
        int orderNumber = DataCarrierUtils.bytesToInt(orderNumberData, PdV19Structure.ORDER_NUMBER_BIT_INDEX, PdV19Structure.ORDER_NUMBER_BIT_LENGTH, ByteOrder.LITTLE_ENDIAN);

        byte startDayOffsetData = data[PdV19Structure.START_DAY_OFFSET_BYTE_INDEX];
        int startDayOffset = DataCarrierUtils.byteToInt(startDayOffsetData, PdV19Structure.START_DAY_OFFSET_BIT_INDEX, PdV19Structure.START_DAY_OFFSET_BIT_LENGTH);

        byte paymentTypeData = data[PdV19Structure.PAYMENT_TYPE_BYTE_INDEX];
        boolean paymentType = DataCarrierUtils.byteToBoolean(paymentTypeData, PdV19Structure.PAYMENT_TYPE_BIT_INDEX);

        byte[] saleDateTimeData = DataCarrierUtils.subArray(data, PdV19Structure.SALE_DATE_TIME_BYTE_INDEX, PdV19Structure.SALE_DATE_TIME_BYTE_LENGTH);
        long saleDateTimeLong = DataCarrierUtils.bytesToLong(saleDateTimeData, ByteOrder.LITTLE_ENDIAN);
        Date saleDateTime = DataCarrierUtils.unixTimestampToDate(saleDateTimeLong);

        byte[] tariffCodeData = DataCarrierUtils.subArray(data, PdV19Structure.TARIFF_BYTE_INDEX, PdV19Structure.TARIFF_BYTE_LENGTH);
        long tariffCode = DataCarrierUtils.bytesToLong(tariffCodeData, ByteOrder.LITTLE_ENDIAN);

        byte[] edsKeyNumberData = DataCarrierUtils.subArray(data, PdV19Structure.EDS_KEY_NUMBER_BYTE_INDEX, PdV19Structure.EDS_KEY_NUMBER_BYTE_LENGTH);
        long edsKeyNumber = DataCarrierUtils.bytesToLong(edsKeyNumberData, ByteOrder.LITTLE_ENDIAN);

        PdV19Impl pdV19 = new PdV19Impl();
        pdV19.setOrderNumber(orderNumber);
        pdV19.setStartDayOffset(startDayOffset);
        pdV19.setPaymentType(paymentType ? PdWithPaymentType.PAYMENT_TYPE_CARD : PdWithPaymentType.PAYMENT_TYPE_CASH);
        pdV19.setSaleDateTime(saleDateTime);
        pdV19.setTariffCode(tariffCode);
        pdV19.setEdsKeyNumber(edsKeyNumber);

        return pdV19;
    }

}
