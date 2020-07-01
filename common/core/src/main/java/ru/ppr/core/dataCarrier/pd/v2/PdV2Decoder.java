package ru.ppr.core.dataCarrier.pd.v2;

import android.support.annotation.NonNull;

import java.nio.ByteOrder;
import java.util.Date;

import ru.ppr.core.dataCarrier.DataCarrierUtils;
import ru.ppr.core.dataCarrier.pd.PdDecoder;
import ru.ppr.core.dataCarrier.pd.base.PdWithDirection;
import ru.ppr.core.dataCarrier.pd.base.PdWithPaymentType;

/**
 * Декодер ПД версии 2.
 *
 * @see PdV2Impl
 */
public class PdV2Decoder implements PdDecoder {

    @Override
    public PdV2Impl decode(@NonNull byte[] data) {

        if (data.length < PdV2Structure.PD_SIZE)
            return null;

        final byte[] orderNumberData = DataCarrierUtils.subArray(data, PdV2Structure.ORDER_NUMBER_BYTE_INDEX, PdV2Structure.ORDER_NUMBER_BYTE_LENGTH);
        final int orderNumber = DataCarrierUtils.bytesToInt(orderNumberData, PdV2Structure.ORDER_NUMBER_BIT_INDEX, PdV2Structure.ORDER_NUMBER_BIT_LENGTH, ByteOrder.LITTLE_ENDIAN);

        final byte startDayOffsetData = data[PdV2Structure.START_DAY_OFFSET_BYTE_INDEX];
        final int startDayOffset = DataCarrierUtils.byteToInt(startDayOffsetData, PdV2Structure.START_DAY_OFFSET_BIT_INDEX, PdV2Structure.START_DAY_OFFSET_BIT_LENGTH);

        final byte directionData = data[PdV2Structure.DIRECTION_BYTE_INDEX];
        final boolean direction = DataCarrierUtils.byteToBoolean(directionData, PdV2Structure.DIRECTION_BIT_INDEX);

        final byte paymentTypeData = data[PdV2Structure.PAYMENT_TYPE_BYTE_INDEX];
        final boolean paymentType = DataCarrierUtils.byteToBoolean(paymentTypeData, PdV2Structure.PAYMENT_TYPE_BIT_INDEX);

        final byte[] sourceOrderNumberData = DataCarrierUtils.subArray(data, PdV2Structure.SOURCE_ORDER_NUMBER_BYTE_INDEX, PdV2Structure.SOURCE_ORDER_NUMBER_BYTE_LENGTH);
        final int sourceOrderNumber = DataCarrierUtils.bytesToInt(sourceOrderNumberData, PdV2Structure.SOURCE_ORDER_NUMBER_BIT_INDEX, PdV2Structure.SOURCE_ORDER_NUMBER_BIT_LENGTH, ByteOrder.LITTLE_ENDIAN);

        final byte[] saleDateTimeData = DataCarrierUtils.subArray(data, PdV2Structure.SALE_DATE_TIME_BYTE_INDEX, PdV2Structure.SALE_DATE_TIME_BYTE_LENGTH);
        final long saleDateTimeLong = DataCarrierUtils.bytesToLong(saleDateTimeData, ByteOrder.LITTLE_ENDIAN);
        final Date saleDateTime = DataCarrierUtils.unixTimestampToDate(saleDateTimeLong);

        final byte[] sourceSaleDateTimeData = DataCarrierUtils.subArray(data, PdV2Structure.SOURCE_SALE_DATE_TIME_BYTE_INDEX, PdV2Structure.SOURCE_SALE_DATE_TIME_BYTE_LENGTH);
        final long sourceSaleDateTimeLong = DataCarrierUtils.bytesToLong(sourceSaleDateTimeData, ByteOrder.LITTLE_ENDIAN);
        final Date sourceSaleDateTime = DataCarrierUtils.unixTimestampToDate(sourceSaleDateTimeLong);

        final byte[] tariffCodeData = DataCarrierUtils.subArray(data, PdV2Structure.TARIFF_BYTE_INDEX, PdV2Structure.TARIFF_BYTE_LENGTH);
        final long tariffCode = DataCarrierUtils.bytesToLong(tariffCodeData, ByteOrder.LITTLE_ENDIAN);

        final byte[] sourcePdDeviceIdData = DataCarrierUtils.subArray(data, PdV2Structure.SOURCE_PD_DEVICE_ID_BYTE_INDEX, PdV2Structure.SOURCE_PD_DEVICE_ID_BYTE_LENGTH);
        final long sourcePdDeviceId = DataCarrierUtils.bytesToLong(sourcePdDeviceIdData, ByteOrder.LITTLE_ENDIAN);

        final byte[] edsKeyNumberData = DataCarrierUtils.subArray(data, PdV2Structure.EDS_KEY_NUMBER_BYTE_INDEX, PdV2Structure.EDS_KEY_NUMBER_BYTE_LENGTH);
        final long edsKeyNumber = DataCarrierUtils.bytesToLong(edsKeyNumberData, ByteOrder.LITTLE_ENDIAN);

        final byte[] eds = DataCarrierUtils.subArray(data, PdV2Structure.EDS_BYTE_INDEX, PdV2Structure.EDS_BYTE_LENGTH);

        PdV2Impl pdV2 = new PdV2Impl();
        pdV2.setOrderNumber(orderNumber);
        pdV2.setStartDayOffset(startDayOffset);
        pdV2.setDirection(direction ? PdWithDirection.DIRECTION_BACK : PdWithDirection.DIRECTION_THERE);
        pdV2.setPaymentType(paymentType ? PdWithPaymentType.PAYMENT_TYPE_CARD : PdWithPaymentType.PAYMENT_TYPE_CASH);
        pdV2.setSourceOrderNumber(sourceOrderNumber);
        pdV2.setSaleDateTime(saleDateTime);
        pdV2.setSourceSaleDateTime(sourceSaleDateTime);
        pdV2.setTariffCode(tariffCode);
        pdV2.setSourceDeviceId(sourcePdDeviceId);
        pdV2.setEdsKeyNumber(edsKeyNumber);
        pdV2.setEds(eds);

        return pdV2;
    }

}
