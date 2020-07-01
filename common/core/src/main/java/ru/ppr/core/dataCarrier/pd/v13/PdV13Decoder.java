package ru.ppr.core.dataCarrier.pd.v13;

import android.support.annotation.NonNull;

import java.nio.ByteOrder;
import java.util.Date;

import ru.ppr.core.dataCarrier.DataCarrierUtils;
import ru.ppr.core.dataCarrier.pd.PdDecoder;
import ru.ppr.core.dataCarrier.pd.base.PdWithDirection;

/**
 * Декодер ПД v.13.
 *
 * @author Aleksandr Brazhkin
 */
public class PdV13Decoder implements PdDecoder {

    @Override
    public PdV13 decode(@NonNull byte[] data) {

        if (data.length < PdV13Structure.PD_SIZE)
            return null;

        byte[] orderNumberData = DataCarrierUtils.subArray(data, PdV13Structure.ORDER_NUMBER_BYTE_INDEX, PdV13Structure.ORDER_NUMBER_BYTE_LENGTH);
        int orderNumber = DataCarrierUtils.bytesToInt(orderNumberData, PdV13Structure.ORDER_NUMBER_BIT_INDEX, PdV13Structure.ORDER_NUMBER_BIT_LENGTH, ByteOrder.LITTLE_ENDIAN);

        byte startDayOffsetData = data[PdV13Structure.START_DAY_OFFSET_BYTE_INDEX];
        int startDayOffset = DataCarrierUtils.byteToInt(startDayOffsetData, PdV13Structure.START_DAY_OFFSET_BIT_INDEX, PdV13Structure.START_DAY_OFFSET_BIT_LENGTH);

        byte directionData = data[PdV13Structure.DIRECTION_BYTE_INDEX];
        boolean direction = DataCarrierUtils.byteToBoolean(directionData, PdV13Structure.DIRECTION_BIT_INDEX);

        byte[] saleDateTimeData = DataCarrierUtils.subArray(data, PdV13Structure.SALE_DATE_TIME_BYTE_INDEX, PdV13Structure.SALE_DATE_TIME_BYTE_LENGTH);
        long saleDateTimeLong = DataCarrierUtils.bytesToLong(saleDateTimeData, ByteOrder.LITTLE_ENDIAN);
        Date saleDateTime = DataCarrierUtils.unixTimestampToDate(saleDateTimeLong);

        byte[] tariffCodeData = DataCarrierUtils.subArray(data, PdV13Structure.TARIFF_BYTE_INDEX, PdV13Structure.TARIFF_BYTE_LENGTH);
        long tariffCode = DataCarrierUtils.bytesToLong(tariffCodeData, ByteOrder.LITTLE_ENDIAN);

        byte[] exemptionCodeData = DataCarrierUtils.subArray(data, PdV13Structure.EXEMPTION_BYTE_INDEX, PdV13Structure.EXEMPTION_BYTE_LENGTH);
        int exemptionCode = DataCarrierUtils.bytesToInt(exemptionCodeData, ByteOrder.LITTLE_ENDIAN);

        byte[] edsKeyNumberData = DataCarrierUtils.subArray(data, PdV13Structure.EDS_KEY_NUMBER_BYTE_INDEX, PdV13Structure.EDS_KEY_NUMBER_BYTE_LENGTH);
        long edsKeyNumber = DataCarrierUtils.bytesToLong(edsKeyNumberData, ByteOrder.LITTLE_ENDIAN);

        PdV13Impl pdV13 = new PdV13Impl();
        pdV13.setOrderNumber(orderNumber);
        pdV13.setStartDayOffset(startDayOffset);
        pdV13.setDirection(direction ? PdWithDirection.DIRECTION_BACK : PdWithDirection.DIRECTION_THERE);
        pdV13.setSaleDateTime(saleDateTime);
        pdV13.setTariffCode(tariffCode);
        pdV13.setExemptionCode(exemptionCode);
        pdV13.setEdsKeyNumber(edsKeyNumber);

        return pdV13;
    }

}
