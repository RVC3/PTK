package ru.ppr.core.dataCarrier.pd.v3;

import android.support.annotation.NonNull;

import java.nio.ByteOrder;
import java.util.Date;

import ru.ppr.core.dataCarrier.DataCarrierUtils;
import ru.ppr.core.dataCarrier.pd.PdDecoder;
import ru.ppr.core.dataCarrier.pd.base.PdWithDirection;
import ru.ppr.core.dataCarrier.pd.base.PdWithTicketType;

/**
 * Декодер ПД v.3.
 *
 * @author Aleksandr Brazhkin
 */
public class PdV3Decoder implements PdDecoder {

    @Override
    public PdV3 decode(@NonNull byte[] data) {

        if (data.length < PdV3Structure.PD_SIZE)
            return null;

        byte[] orderNumberData = DataCarrierUtils.subArray(data, PdV3Structure.ORDER_NUMBER_BYTE_INDEX, PdV3Structure.ORDER_NUMBER_BYTE_LENGTH);
        int orderNumber = DataCarrierUtils.bytesToInt(orderNumberData, PdV3Structure.ORDER_NUMBER_BIT_INDEX, PdV3Structure.ORDER_NUMBER_BIT_LENGTH, ByteOrder.LITTLE_ENDIAN);

        byte startDayOffsetData = data[PdV3Structure.START_DAY_OFFSET_BYTE_INDEX];
        int startDayOffset = DataCarrierUtils.byteToInt(startDayOffsetData, PdV3Structure.START_DAY_OFFSET_BIT_INDEX, PdV3Structure.START_DAY_OFFSET_BIT_LENGTH);

        byte directionData = data[PdV3Structure.DIRECTION_BYTE_INDEX];
        boolean direction = DataCarrierUtils.byteToBoolean(directionData, PdV3Structure.DIRECTION_BIT_INDEX);

        byte ticketTypeData = data[PdV3Structure.TICKET_TYPE_BYTE_INDEX];
        boolean ticketType = DataCarrierUtils.byteToBoolean(ticketTypeData, PdV3Structure.TICKET_TYPE_BIT_INDEX);

        byte[] saleDateTimeData = DataCarrierUtils.subArray(data, PdV3Structure.SALE_DATE_TIME_BYTE_INDEX, PdV3Structure.SALE_DATE_TIME_BYTE_LENGTH);
        long saleDateTimeLong = DataCarrierUtils.bytesToLong(saleDateTimeData, ByteOrder.LITTLE_ENDIAN);
        Date saleDateTime = DataCarrierUtils.unixTimestampToDate(saleDateTimeLong);

        byte[] tariffCodeData = DataCarrierUtils.subArray(data, PdV3Structure.TARIFF_BYTE_INDEX, PdV3Structure.TARIFF_BYTE_LENGTH);
        long tariffCode = DataCarrierUtils.bytesToLong(tariffCodeData, ByteOrder.LITTLE_ENDIAN);

        byte[] edsKeyNumberData = DataCarrierUtils.subArray(data, PdV3Structure.EDS_KEY_NUMBER_BYTE_INDEX, PdV3Structure.EDS_KEY_NUMBER_BYTE_LENGTH);
        long edsKeyNumber = DataCarrierUtils.bytesToLong(edsKeyNumberData, ByteOrder.LITTLE_ENDIAN);

        PdV3Impl pdV3 = new PdV3Impl();
        pdV3.setOrderNumber(orderNumber);
        pdV3.setStartDayOffset(startDayOffset);
        pdV3.setDirection(direction ? PdWithDirection.DIRECTION_BACK : PdWithDirection.DIRECTION_THERE);
        pdV3.setTicketType(ticketType ? PdWithTicketType.TICKET_TYPE_WITH_EXEMPTION : PdWithTicketType.TICKET_TYPE_FULL);
        pdV3.setSaleDateTime(saleDateTime);
        pdV3.setTariffCode(tariffCode);
        pdV3.setEdsKeyNumber(edsKeyNumber);

        return pdV3;
    }

}
