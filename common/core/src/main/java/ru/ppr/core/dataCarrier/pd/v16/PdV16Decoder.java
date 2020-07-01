package ru.ppr.core.dataCarrier.pd.v16;

import android.support.annotation.NonNull;

import java.nio.ByteOrder;
import java.util.Date;

import ru.ppr.core.dataCarrier.DataCarrierUtils;
import ru.ppr.core.dataCarrier.pd.PdDecoder;
import ru.ppr.core.dataCarrier.pd.base.PdWithDirection;

/**
 * Декодер ПД v.16.
 *
 * @author Aleksandr Brazhkin
 */
public class PdV16Decoder implements PdDecoder {

    @Override
    public PdV16 decode(@NonNull byte[] data) {

        if (data.length < PdV16Structure.PD_SIZE)
            return null;

        byte[] orderNumberData = DataCarrierUtils.subArray(data, PdV16Structure.ORDER_NUMBER_BYTE_INDEX, PdV16Structure.ORDER_NUMBER_BYTE_LENGTH);
        int orderNumber = DataCarrierUtils.bytesToInt(orderNumberData, PdV16Structure.ORDER_NUMBER_BIT_INDEX, PdV16Structure.ORDER_NUMBER_BIT_LENGTH, ByteOrder.LITTLE_ENDIAN);

        byte startDayOffsetData = data[PdV16Structure.START_DAY_OFFSET_BYTE_INDEX];
        int startDayOffset = DataCarrierUtils.byteToInt(startDayOffsetData, PdV16Structure.START_DAY_OFFSET_BIT_INDEX, PdV16Structure.START_DAY_OFFSET_BIT_LENGTH);

        byte directionData = data[PdV16Structure.DIRECTION_BYTE_INDEX];
        boolean direction = DataCarrierUtils.byteToBoolean(directionData, PdV16Structure.DIRECTION_BIT_INDEX);

        byte[] saleDateTimeData = DataCarrierUtils.subArray(data, PdV16Structure.SALE_DATE_TIME_BYTE_INDEX, PdV16Structure.SALE_DATE_TIME_BYTE_LENGTH);
        long saleDateTimeLong = DataCarrierUtils.bytesToLong(saleDateTimeData, ByteOrder.LITTLE_ENDIAN);
        Date saleDateTime = DataCarrierUtils.unixTimestampToDate(saleDateTimeLong);

        byte[] tariffCodeData = DataCarrierUtils.subArray(data, PdV16Structure.TARIFF_BYTE_INDEX, PdV16Structure.TARIFF_BYTE_LENGTH);
        long tariffCode = DataCarrierUtils.bytesToLong(tariffCodeData, ByteOrder.LITTLE_ENDIAN);

        byte[] exemptionCodeData = DataCarrierUtils.subArray(data, PdV16Structure.EXEMPTION_BYTE_INDEX, PdV16Structure.EXEMPTION_BYTE_LENGTH);
        int exemptionCode = DataCarrierUtils.bytesToInt(exemptionCodeData, ByteOrder.LITTLE_ENDIAN);

        byte[] edsKeyNumberData = DataCarrierUtils.subArray(data, PdV16Structure.EDS_KEY_NUMBER_BYTE_INDEX, PdV16Structure.EDS_KEY_NUMBER_BYTE_LENGTH);
        long edsKeyNumber = DataCarrierUtils.bytesToLong(edsKeyNumberData, ByteOrder.LITTLE_ENDIAN);

        PdV16Impl pdV16 = new PdV16Impl();
        pdV16.setOrderNumber(orderNumber);
        pdV16.setStartDayOffset(startDayOffset);
        pdV16.setDirection(direction ? PdWithDirection.DIRECTION_BACK : PdWithDirection.DIRECTION_THERE);
        pdV16.setSaleDateTime(saleDateTime);
        pdV16.setTariffCode(tariffCode);
        pdV16.setExemptionCode(exemptionCode);
        pdV16.setEdsKeyNumber(edsKeyNumber);

        return pdV16;
    }

}
