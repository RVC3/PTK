package ru.ppr.core.dataCarrier.pd.v1;

import android.support.annotation.NonNull;

import java.nio.ByteOrder;
import java.util.Date;

import ru.ppr.core.dataCarrier.DataCarrierUtils;
import ru.ppr.core.dataCarrier.pd.PdDecoder;
import ru.ppr.core.dataCarrier.pd.base.PdWithDirection;
import ru.ppr.core.dataCarrier.pd.base.PdWithPaymentType;

/**
 * Декодер ПД v.1.
 *
 * @author Aleksandr Brazhkin
 */
public class PdV1Decoder implements PdDecoder {

    @Override
    public PdV1 decode(@NonNull byte[] data) {

        if (data.length < PdV1Structure.PD_SIZE)
            return null;

        byte[] orderNumberData = DataCarrierUtils.subArray(data, PdV1Structure.ORDER_NUMBER_BYTE_INDEX, PdV1Structure.ORDER_NUMBER_BYTE_LENGTH);
        int orderNumber = DataCarrierUtils.bytesToInt(orderNumberData, PdV1Structure.ORDER_NUMBER_BIT_INDEX, PdV1Structure.ORDER_NUMBER_BIT_LENGTH, ByteOrder.LITTLE_ENDIAN);

        byte startDayOffsetData = data[PdV1Structure.START_DAY_OFFSET_BYTE_INDEX];
        int startDayOffset = DataCarrierUtils.byteToInt(startDayOffsetData, PdV1Structure.START_DAY_OFFSET_BIT_INDEX, PdV1Structure.START_DAY_OFFSET_BIT_LENGTH);

        byte directionData = data[PdV1Structure.DIRECTION_BYTE_INDEX];
        boolean direction = DataCarrierUtils.byteToBoolean(directionData, PdV1Structure.DIRECTION_BIT_INDEX);

        byte paymentTypeData = data[PdV1Structure.PAYMENT_TYPE_BYTE_INDEX];
        boolean paymentType = DataCarrierUtils.byteToBoolean(paymentTypeData, PdV1Structure.PAYMENT_TYPE_BIT_INDEX);

        byte[] saleDateTimeData = DataCarrierUtils.subArray(data, PdV1Structure.SALE_DATE_TIME_BYTE_INDEX, PdV1Structure.SALE_DATE_TIME_BYTE_LENGTH);
        long saleDateTimeLong = DataCarrierUtils.bytesToLong(saleDateTimeData, ByteOrder.LITTLE_ENDIAN);
        Date saleDateTime = DataCarrierUtils.unixTimestampToDate(saleDateTimeLong);

        byte[] tariffCodeData = DataCarrierUtils.subArray(data, PdV1Structure.TARIFF_BYTE_INDEX, PdV1Structure.TARIFF_BYTE_LENGTH);
        long tariffCode = DataCarrierUtils.bytesToLong(tariffCodeData, ByteOrder.LITTLE_ENDIAN);

        byte[] exemptionCodeData = DataCarrierUtils.subArray(data, PdV1Structure.EXEMPTION_BYTE_INDEX, PdV1Structure.EXEMPTION_BYTE_LENGTH);
        int exemptionCode = DataCarrierUtils.bytesToInt(exemptionCodeData, ByteOrder.LITTLE_ENDIAN);

        byte[] edsKeyNumberData = DataCarrierUtils.subArray(data, PdV1Structure.EDS_KEY_NUMBER_BYTE_INDEX, PdV1Structure.EDS_KEY_NUMBER_BYTE_LENGTH);
        long edsKeyNumber = DataCarrierUtils.bytesToLong(edsKeyNumberData, ByteOrder.LITTLE_ENDIAN);

        byte[] eds = DataCarrierUtils.subArray(data, PdV1Structure.EDS_BYTE_INDEX, PdV1Structure.EDS_BYTE_LENGTH);

        PdV1Impl pdV1 = new PdV1Impl();
        pdV1.setOrderNumber(orderNumber);
        pdV1.setStartDayOffset(startDayOffset);
        pdV1.setDirection(direction ? PdWithDirection.DIRECTION_BACK : PdWithDirection.DIRECTION_THERE);
        pdV1.setPaymentType(paymentType ? PdWithPaymentType.PAYMENT_TYPE_CARD : PdWithPaymentType.PAYMENT_TYPE_CASH);
        pdV1.setSaleDateTime(saleDateTime);
        pdV1.setTariffCode(tariffCode);
        pdV1.setExemptionCode(exemptionCode);
        pdV1.setEdsKeyNumber(edsKeyNumber);
        pdV1.setEds(eds);

        return pdV1;
    }

}
