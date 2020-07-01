package ru.ppr.core.dataCarrier.pd.v23;

import android.support.annotation.NonNull;

import java.nio.ByteOrder;
import java.util.Date;

import ru.ppr.core.dataCarrier.DataCarrierUtils;
import ru.ppr.core.dataCarrier.pd.PdDecoder;
import ru.ppr.core.dataCarrier.pd.base.PdWithPaymentType;

/**
 * Декодер ПД v.23.
 *
 * @author Aleksandr Brazhkin
 */
public class PdV23Decoder implements PdDecoder {

    @Override
    public PdV23 decode(@NonNull byte[] data) {

        if (data.length < PdV23Structure.PD_SIZE)
            return null;

        byte[] orderNumberData = DataCarrierUtils.subArray(data, PdV23Structure.ORDER_NUMBER_BYTE_INDEX, PdV23Structure.ORDER_NUMBER_BYTE_LENGTH);
        int orderNumber = DataCarrierUtils.bytesToInt(orderNumberData, PdV23Structure.ORDER_NUMBER_BIT_INDEX, PdV23Structure.ORDER_NUMBER_BIT_LENGTH, ByteOrder.LITTLE_ENDIAN);

        byte startDayOffsetData = data[PdV23Structure.START_DAY_OFFSET_BYTE_INDEX];
        int startDayOffset = DataCarrierUtils.byteToInt(startDayOffsetData, PdV23Structure.START_DAY_OFFSET_BIT_INDEX, PdV23Structure.START_DAY_OFFSET_BIT_LENGTH);

        byte paymentTypeData = data[PdV23Structure.PAYMENT_TYPE_BYTE_INDEX];
        boolean paymentType = DataCarrierUtils.byteToBoolean(paymentTypeData, PdV23Structure.PAYMENT_TYPE_BIT_INDEX);

        byte[] saleDateTimeData = DataCarrierUtils.subArray(data, PdV23Structure.SALE_DATE_TIME_BYTE_INDEX, PdV23Structure.SALE_DATE_TIME_BYTE_LENGTH);
        long saleDateTimeLong = DataCarrierUtils.bytesToLong(saleDateTimeData, ByteOrder.LITTLE_ENDIAN);
        Date saleDateTime = DataCarrierUtils.unixTimestampToDate(saleDateTimeLong);

        byte[] tariffCodeData = DataCarrierUtils.subArray(data, PdV23Structure.TARIFF_BYTE_INDEX, PdV23Structure.TARIFF_BYTE_LENGTH);
        long tariffCode = DataCarrierUtils.bytesToLong(tariffCodeData, ByteOrder.LITTLE_ENDIAN);

        byte[] edsKeyNumberData = DataCarrierUtils.subArray(data, PdV23Structure.EDS_KEY_NUMBER_BYTE_INDEX, PdV23Structure.EDS_KEY_NUMBER_BYTE_LENGTH);
        long edsKeyNumber = DataCarrierUtils.bytesToLong(edsKeyNumberData, ByteOrder.LITTLE_ENDIAN);

        PdV23Impl pdV23 = new PdV23Impl();
        pdV23.setOrderNumber(orderNumber);
        pdV23.setStartDayOffset(startDayOffset);
        pdV23.setPaymentType(paymentType ? PdWithPaymentType.PAYMENT_TYPE_CARD : PdWithPaymentType.PAYMENT_TYPE_CASH);
        pdV23.setSaleDateTime(saleDateTime);
        pdV23.setTariffCode(tariffCode);
        pdV23.setEdsKeyNumber(edsKeyNumber);

        return pdV23;
    }

}
