package ru.ppr.core.dataCarrier.pd.v20;

import android.support.annotation.NonNull;

import java.nio.ByteOrder;
import java.util.Date;

import ru.ppr.core.dataCarrier.DataCarrierUtils;
import ru.ppr.core.dataCarrier.pd.PdDecoder;
import ru.ppr.core.dataCarrier.pd.base.PdWithPaymentType;

/**
 * Декодер ПД v.20.
 *
 * @author Aleksandr Brazhkin
 */
public class PdV20Decoder implements PdDecoder {

    @Override
    public PdV20 decode(@NonNull byte[] data) {

        if (data.length < PdV20Structure.PD_SIZE)
            return null;

        byte[] orderNumberData = DataCarrierUtils.subArray(data, PdV20Structure.ORDER_NUMBER_BYTE_INDEX, PdV20Structure.ORDER_NUMBER_BYTE_LENGTH);
        int orderNumber = DataCarrierUtils.bytesToInt(orderNumberData, PdV20Structure.ORDER_NUMBER_BIT_INDEX, PdV20Structure.ORDER_NUMBER_BIT_LENGTH, ByteOrder.LITTLE_ENDIAN);

        byte startDayOffsetData = data[PdV20Structure.START_DAY_OFFSET_BYTE_INDEX];
        int startDayOffset = DataCarrierUtils.byteToInt(startDayOffsetData, PdV20Structure.START_DAY_OFFSET_BIT_INDEX, PdV20Structure.START_DAY_OFFSET_BIT_LENGTH);

        byte paymentTypeData = data[PdV20Structure.PAYMENT_TYPE_BYTE_INDEX];
        boolean paymentType = DataCarrierUtils.byteToBoolean(paymentTypeData, PdV20Structure.PAYMENT_TYPE_BIT_INDEX);

        byte[] saleDateTimeData = DataCarrierUtils.subArray(data, PdV20Structure.SALE_DATE_TIME_BYTE_INDEX, PdV20Structure.SALE_DATE_TIME_BYTE_LENGTH);
        long saleDateTimeLong = DataCarrierUtils.bytesToLong(saleDateTimeData, ByteOrder.LITTLE_ENDIAN);
        Date saleDateTime = DataCarrierUtils.unixTimestampToDate(saleDateTimeLong);

        byte[] tariffCodeData = DataCarrierUtils.subArray(data, PdV20Structure.TARIFF_BYTE_INDEX, PdV20Structure.TARIFF_BYTE_LENGTH);
        long tariffCode = DataCarrierUtils.bytesToLong(tariffCodeData, ByteOrder.LITTLE_ENDIAN);

        byte[] edsKeyNumberData = DataCarrierUtils.subArray(data, PdV20Structure.EDS_KEY_NUMBER_BYTE_INDEX, PdV20Structure.EDS_KEY_NUMBER_BYTE_LENGTH);
        long edsKeyNumber = DataCarrierUtils.bytesToLong(edsKeyNumberData, ByteOrder.LITTLE_ENDIAN);

        PdV20Impl pdV20 = new PdV20Impl();
        pdV20.setOrderNumber(orderNumber);
        pdV20.setStartDayOffset(startDayOffset);
        pdV20.setPaymentType(paymentType ? PdWithPaymentType.PAYMENT_TYPE_CARD : PdWithPaymentType.PAYMENT_TYPE_CASH);
        pdV20.setSaleDateTime(saleDateTime);
        pdV20.setTariffCode(tariffCode);
        pdV20.setEdsKeyNumber(edsKeyNumber);

        return pdV20;
    }

}
