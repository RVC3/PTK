package ru.ppr.core.dataCarrier.pd.v25;

import android.support.annotation.NonNull;

import java.nio.ByteOrder;
import java.util.Date;

import ru.ppr.core.dataCarrier.DataCarrierUtils;
import ru.ppr.core.dataCarrier.pd.PdDecoder;
import ru.ppr.core.dataCarrier.pd.base.PdWithPaymentType;

/**
 * Декодер ПД v.25.
 *
 * @author Grigoriy Kashka
 */
public class PdV25Decoder implements PdDecoder {

    @Override
    public PdV25 decode(@NonNull byte[] data) {

        if (data.length < PdV25Structure.PD_SIZE)
            return null;

        byte[] orderNumberData = DataCarrierUtils.subArray(data, PdV25Structure.ORDER_NUMBER_BYTE_INDEX, PdV25Structure.ORDER_NUMBER_BYTE_LENGTH);
        int orderNumber = DataCarrierUtils.bytesToInt(orderNumberData, PdV25Structure.ORDER_NUMBER_BIT_INDEX, PdV25Structure.ORDER_NUMBER_BIT_LENGTH, ByteOrder.LITTLE_ENDIAN);

        byte startDayOffsetData = data[PdV25Structure.START_DAY_OFFSET_BYTE_INDEX];
        int startDayOffset = DataCarrierUtils.byteToInt(startDayOffsetData, PdV25Structure.START_DAY_OFFSET_BIT_INDEX, PdV25Structure.START_DAY_OFFSET_BIT_LENGTH);

        byte paymentTypeData = data[PdV25Structure.PAYMENT_TYPE_BYTE_INDEX];
        boolean paymentType = DataCarrierUtils.byteToBoolean(paymentTypeData, PdV25Structure.PAYMENT_TYPE_BIT_INDEX);

        byte[] saleDateTimeData = DataCarrierUtils.subArray(data, PdV25Structure.SALE_DATE_TIME_BYTE_INDEX, PdV25Structure.SALE_DATE_TIME_BYTE_LENGTH);
        long saleDateTimeLong = DataCarrierUtils.bytesToLong(saleDateTimeData, ByteOrder.LITTLE_ENDIAN);
        Date saleDateTime = DataCarrierUtils.unixTimestampToDate(saleDateTimeLong);

        byte[] tariffCodeData = DataCarrierUtils.subArray(data, PdV25Structure.TARIFF_BYTE_INDEX, PdV25Structure.TARIFF_BYTE_LENGTH);
        long tariffCode = DataCarrierUtils.bytesToLong(tariffCodeData, ByteOrder.LITTLE_ENDIAN);

        byte[] exemptionCodeData = DataCarrierUtils.subArray(data, PdV25Structure.EXEMPTION_BYTE_INDEX, PdV25Structure.EXEMPTION_BYTE_LENGTH);
        int exemptionCode = DataCarrierUtils.bytesToInt(exemptionCodeData, ByteOrder.LITTLE_ENDIAN);

        byte[] forDaysData = DataCarrierUtils.subArray(data, PdV25Structure.FOR_DAYS_BYTE_INDEX, PdV25Structure.FOR_DAYS_BYTE_LENGTH);
        int forDays = DataCarrierUtils.bytesToInt(forDaysData, ByteOrder.LITTLE_ENDIAN);

        byte[] edsKeyNumberData = DataCarrierUtils.subArray(data, PdV25Structure.EDS_KEY_NUMBER_BYTE_INDEX, PdV25Structure.EDS_KEY_NUMBER_BYTE_LENGTH);
        long edsKeyNumber = DataCarrierUtils.bytesToLong(edsKeyNumberData, ByteOrder.LITTLE_ENDIAN);

        PdV25Impl pdV25 = new PdV25Impl();
        pdV25.setOrderNumber(orderNumber);
        pdV25.setStartDayOffset(startDayOffset);
        pdV25.setPaymentType(paymentType ? PdWithPaymentType.PAYMENT_TYPE_CARD : PdWithPaymentType.PAYMENT_TYPE_CASH);
        pdV25.setSaleDateTime(saleDateTime);
        pdV25.setTariffCode(tariffCode);
        pdV25.setExemptionCode(exemptionCode);
        pdV25.setForDays(forDays);
        pdV25.setEdsKeyNumber(edsKeyNumber);

        return pdV25;
    }

}
