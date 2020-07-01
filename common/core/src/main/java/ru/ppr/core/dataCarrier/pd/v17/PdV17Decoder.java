package ru.ppr.core.dataCarrier.pd.v17;

import android.support.annotation.NonNull;

import java.nio.ByteOrder;
import java.util.Date;

import ru.ppr.core.dataCarrier.DataCarrierUtils;
import ru.ppr.core.dataCarrier.pd.PdDecoder;
import ru.ppr.core.dataCarrier.pd.base.PdWithDirection;

/**
 * Декодер ПД v.17.
 *
 * @author Aleksandr Brazhkin
 */
public class PdV17Decoder implements PdDecoder {

    @Override
    public PdV17 decode(@NonNull byte[] data) {

        if (data.length < PdV17Structure.PD_SIZE)
            return null;

        byte[] orderNumberData = DataCarrierUtils.subArray(data, PdV17Structure.ORDER_NUMBER_BYTE_INDEX, PdV17Structure.ORDER_NUMBER_BYTE_LENGTH);
        int orderNumber = DataCarrierUtils.bytesToInt(orderNumberData, PdV17Structure.ORDER_NUMBER_BIT_INDEX, PdV17Structure.ORDER_NUMBER_BIT_LENGTH, ByteOrder.LITTLE_ENDIAN);

        byte startDayOffsetData = data[PdV17Structure.START_DAY_OFFSET_BYTE_INDEX];
        int startDayOffset = DataCarrierUtils.byteToInt(startDayOffsetData, PdV17Structure.START_DAY_OFFSET_BIT_INDEX, PdV17Structure.START_DAY_OFFSET_BIT_LENGTH);

        byte directionData = data[PdV17Structure.DIRECTION_BYTE_INDEX];
        boolean direction = DataCarrierUtils.byteToBoolean(directionData, PdV17Structure.DIRECTION_BIT_INDEX);

        byte[] saleDateTimeData = DataCarrierUtils.subArray(data, PdV17Structure.SALE_DATE_TIME_BYTE_INDEX, PdV17Structure.SALE_DATE_TIME_BYTE_LENGTH);
        long saleDateTimeLong = DataCarrierUtils.bytesToLong(saleDateTimeData, ByteOrder.LITTLE_ENDIAN);
        Date saleDateTime = DataCarrierUtils.unixTimestampToDate(saleDateTimeLong);

        byte[] tariffCodeData = DataCarrierUtils.subArray(data, PdV17Structure.TARIFF_BYTE_INDEX, PdV17Structure.TARIFF_BYTE_LENGTH);
        long tariffCode = DataCarrierUtils.bytesToLong(tariffCodeData, ByteOrder.LITTLE_ENDIAN);

        byte[] exemptionCodeData = DataCarrierUtils.subArray(data, PdV17Structure.EXEMPTION_BYTE_INDEX, PdV17Structure.EXEMPTION_BYTE_LENGTH);
        int exemptionCode = DataCarrierUtils.bytesToInt(exemptionCodeData, ByteOrder.LITTLE_ENDIAN);

        byte[] edsKeyNumberData = DataCarrierUtils.subArray(data, PdV17Structure.EDS_KEY_NUMBER_BYTE_INDEX, PdV17Structure.EDS_KEY_NUMBER_BYTE_LENGTH);
        long edsKeyNumber = DataCarrierUtils.bytesToLong(edsKeyNumberData, ByteOrder.LITTLE_ENDIAN);

        PdV17Impl pdV17 = new PdV17Impl();
        pdV17.setOrderNumber(orderNumber);
        pdV17.setStartDayOffset(startDayOffset);
        pdV17.setDirection(direction ? PdWithDirection.DIRECTION_BACK : PdWithDirection.DIRECTION_THERE);
        pdV17.setSaleDateTime(saleDateTime);
        pdV17.setTariffCode(tariffCode);
        pdV17.setExemptionCode(exemptionCode);
        pdV17.setEdsKeyNumber(edsKeyNumber);

        return pdV17;
    }

}
