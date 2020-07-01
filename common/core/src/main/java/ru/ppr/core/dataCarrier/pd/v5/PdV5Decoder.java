package ru.ppr.core.dataCarrier.pd.v5;

import android.support.annotation.NonNull;

import java.nio.ByteOrder;
import java.util.Date;

import ru.ppr.core.dataCarrier.DataCarrierUtils;
import ru.ppr.core.dataCarrier.pd.PdDecoder;
import ru.ppr.core.dataCarrier.pd.base.PdWithDirection;

/**
 * Декодер ПД v.5.
 *
 * @author Aleksandr Brazhkin
 */
public class PdV5Decoder implements PdDecoder {

    @Override
    public PdV5 decode(@NonNull byte[] data) {

        if (data.length < PdV5Structure.PD_SIZE)
            return null;

        byte[] orderNumberData = DataCarrierUtils.subArray(data, PdV5Structure.ORDER_NUMBER_BYTE_INDEX, PdV5Structure.ORDER_NUMBER_BYTE_LENGTH);
        int orderNumber = DataCarrierUtils.bytesToInt(orderNumberData, PdV5Structure.ORDER_NUMBER_BIT_INDEX, PdV5Structure.ORDER_NUMBER_BIT_LENGTH, ByteOrder.LITTLE_ENDIAN);

        byte startDayOffsetData = data[PdV5Structure.START_DAY_OFFSET_BYTE_INDEX];
        int startDayOffset = DataCarrierUtils.byteToInt(startDayOffsetData, PdV5Structure.START_DAY_OFFSET_BIT_INDEX, PdV5Structure.START_DAY_OFFSET_BIT_LENGTH);

        byte directionData = data[PdV5Structure.DIRECTION_BYTE_INDEX];
        boolean direction = DataCarrierUtils.byteToBoolean(directionData, PdV5Structure.DIRECTION_BIT_INDEX);

        byte[] saleDateTimeData = DataCarrierUtils.subArray(data, PdV5Structure.SALE_DATE_TIME_BYTE_INDEX, PdV5Structure.SALE_DATE_TIME_BYTE_LENGTH);
        long saleDateTimeLong = DataCarrierUtils.bytesToLong(saleDateTimeData, ByteOrder.LITTLE_ENDIAN);
        Date saleDateTime = DataCarrierUtils.unixTimestampToDate(saleDateTimeLong);

        byte[] tariffCodeData = DataCarrierUtils.subArray(data, PdV5Structure.TARIFF_BYTE_INDEX, PdV5Structure.TARIFF_BYTE_LENGTH);
        long tariffCode = DataCarrierUtils.bytesToLong(tariffCodeData, ByteOrder.LITTLE_ENDIAN);

        byte[] exemptionCodeData = DataCarrierUtils.subArray(data, PdV5Structure.EXEMPTION_BYTE_INDEX, PdV5Structure.EXEMPTION_BYTE_LENGTH);
        int exemptionCode = DataCarrierUtils.bytesToInt(exemptionCodeData, ByteOrder.LITTLE_ENDIAN);

        byte[] edsKeyNumberData = DataCarrierUtils.subArray(data, PdV5Structure.EDS_KEY_NUMBER_BYTE_INDEX, PdV5Structure.EDS_KEY_NUMBER_BYTE_LENGTH);
        long edsKeyNumber = DataCarrierUtils.bytesToLong(edsKeyNumberData, ByteOrder.LITTLE_ENDIAN);

        PdV5Impl pdV5 = new PdV5Impl();
        pdV5.setOrderNumber(orderNumber);
        pdV5.setStartDayOffset(startDayOffset);
        pdV5.setDirection(direction ? PdWithDirection.DIRECTION_BACK : PdWithDirection.DIRECTION_THERE);
        pdV5.setSaleDateTime(saleDateTime);
        pdV5.setTariffCode(tariffCode);
        pdV5.setExemptionCode(exemptionCode);
        pdV5.setEdsKeyNumber(edsKeyNumber);

        return pdV5;
    }

}
