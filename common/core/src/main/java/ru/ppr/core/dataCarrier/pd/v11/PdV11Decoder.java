package ru.ppr.core.dataCarrier.pd.v11;

import android.support.annotation.NonNull;

import java.nio.ByteOrder;
import java.util.Date;

import ru.ppr.core.dataCarrier.DataCarrierUtils;
import ru.ppr.core.dataCarrier.pd.PdDecoder;
import ru.ppr.core.dataCarrier.pd.base.PdWithDirection;
import ru.ppr.core.dataCarrier.pd.base.PdWithTicketType;

/**
 * Декодер ПД v.11.
 *
 * @author Aleksandr Brazhkin
 */
public class PdV11Decoder implements PdDecoder {

    @Override
    public PdV11 decode(@NonNull byte[] data) {

        if (data.length < PdV11Structure.PD_SIZE)
            return null;

        byte[] orderNumberData = DataCarrierUtils.subArray(data, PdV11Structure.ORDER_NUMBER_BYTE_INDEX, PdV11Structure.ORDER_NUMBER_BYTE_LENGTH);
        int orderNumber = DataCarrierUtils.bytesToInt(orderNumberData, PdV11Structure.ORDER_NUMBER_BIT_INDEX, PdV11Structure.ORDER_NUMBER_BIT_LENGTH, ByteOrder.LITTLE_ENDIAN);

        byte startDayOffsetData = data[PdV11Structure.START_DAY_OFFSET_BYTE_INDEX];
        int startDayOffset = DataCarrierUtils.byteToInt(startDayOffsetData, PdV11Structure.START_DAY_OFFSET_BIT_INDEX, PdV11Structure.START_DAY_OFFSET_BIT_LENGTH);

        byte directionData = data[PdV11Structure.DIRECTION_BYTE_INDEX];
        boolean direction = DataCarrierUtils.byteToBoolean(directionData, PdV11Structure.DIRECTION_BIT_INDEX);

        byte ticketTypeData = data[PdV11Structure.TICKET_TYPE_BYTE_INDEX];
        boolean ticketType = DataCarrierUtils.byteToBoolean(ticketTypeData, PdV11Structure.TICKET_TYPE_BIT_INDEX);

        byte[] saleDateTimeData = DataCarrierUtils.subArray(data, PdV11Structure.SALE_DATE_TIME_BYTE_INDEX, PdV11Structure.SALE_DATE_TIME_BYTE_LENGTH);
        long saleDateTimeLong = DataCarrierUtils.bytesToLong(saleDateTimeData, ByteOrder.LITTLE_ENDIAN);
        Date saleDateTime = DataCarrierUtils.unixTimestampToDate(saleDateTimeLong);

        byte[] tariffCodeData = DataCarrierUtils.subArray(data, PdV11Structure.TARIFF_BYTE_INDEX, PdV11Structure.TARIFF_BYTE_LENGTH);
        long tariffCode = DataCarrierUtils.bytesToLong(tariffCodeData, ByteOrder.LITTLE_ENDIAN);

        byte[] edsKeyNumberData = DataCarrierUtils.subArray(data, PdV11Structure.EDS_KEY_NUMBER_BYTE_INDEX, PdV11Structure.EDS_KEY_NUMBER_BYTE_LENGTH);
        long edsKeyNumber = DataCarrierUtils.bytesToLong(edsKeyNumberData, ByteOrder.LITTLE_ENDIAN);

        PdV11Impl pdV11 = new PdV11Impl();
        pdV11.setOrderNumber(orderNumber);
        pdV11.setStartDayOffset(startDayOffset);
        pdV11.setDirection(direction ? PdWithDirection.DIRECTION_BACK : PdWithDirection.DIRECTION_THERE);
        pdV11.setTicketType(ticketType ? PdWithTicketType.TICKET_TYPE_WITH_EXEMPTION : PdWithTicketType.TICKET_TYPE_FULL);
        pdV11.setSaleDateTime(saleDateTime);
        pdV11.setTariffCode(tariffCode);
        pdV11.setEdsKeyNumber(edsKeyNumber);

        return pdV11;
    }

}
