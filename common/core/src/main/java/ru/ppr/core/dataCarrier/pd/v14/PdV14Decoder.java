package ru.ppr.core.dataCarrier.pd.v14;

import android.support.annotation.NonNull;

import java.nio.ByteOrder;
import java.util.Date;

import ru.ppr.core.dataCarrier.DataCarrierUtils;
import ru.ppr.core.dataCarrier.pd.PdDecoder;
import ru.ppr.core.dataCarrier.pd.base.PdWithTicketType;

/**
 * Декодер ПД v.14.
 *
 * @author Aleksandr Brazhkin
 */
public class PdV14Decoder implements PdDecoder {

    @Override
    public PdV14 decode(@NonNull byte[] data) {

        if (data.length < PdV14Structure.PD_SIZE)
            return null;

        byte[] orderNumberData = DataCarrierUtils.subArray(data, PdV14Structure.ORDER_NUMBER_BYTE_INDEX, PdV14Structure.ORDER_NUMBER_BYTE_LENGTH);
        int orderNumber = DataCarrierUtils.bytesToInt(orderNumberData, PdV14Structure.ORDER_NUMBER_BIT_INDEX, PdV14Structure.ORDER_NUMBER_BIT_LENGTH, ByteOrder.LITTLE_ENDIAN);

        byte startDayOffsetData = data[PdV14Structure.START_DAY_OFFSET_BYTE_INDEX];
        int startDayOffset = DataCarrierUtils.byteToInt(startDayOffsetData, PdV14Structure.START_DAY_OFFSET_BIT_INDEX, PdV14Structure.START_DAY_OFFSET_BIT_LENGTH);

        byte ticketTypeData = data[PdV14Structure.TICKET_TYPE_BYTE_INDEX];
        boolean ticketType = DataCarrierUtils.byteToBoolean(ticketTypeData, PdV14Structure.TICKET_TYPE_BIT_INDEX);

        byte[] saleDateTimeData = DataCarrierUtils.subArray(data, PdV14Structure.SALE_DATE_TIME_BYTE_INDEX, PdV14Structure.SALE_DATE_TIME_BYTE_LENGTH);
        long saleDateTimeLong = DataCarrierUtils.bytesToLong(saleDateTimeData, ByteOrder.LITTLE_ENDIAN);
        Date saleDateTime = DataCarrierUtils.unixTimestampToDate(saleDateTimeLong);

        byte[] tariffCodeData = DataCarrierUtils.subArray(data, PdV14Structure.TARIFF_BYTE_INDEX, PdV14Structure.TARIFF_BYTE_LENGTH);
        long tariffCode = DataCarrierUtils.bytesToLong(tariffCodeData, ByteOrder.LITTLE_ENDIAN);

        byte[] edsKeyNumberData = DataCarrierUtils.subArray(data, PdV14Structure.EDS_KEY_NUMBER_BYTE_INDEX, PdV14Structure.EDS_KEY_NUMBER_BYTE_LENGTH);
        long edsKeyNumber = DataCarrierUtils.bytesToLong(edsKeyNumberData, ByteOrder.LITTLE_ENDIAN);

        PdV14Impl pdV14 = new PdV14Impl();
        pdV14.setOrderNumber(orderNumber);
        pdV14.setStartDayOffset(startDayOffset);
        pdV14.setTicketType(ticketType ? PdWithTicketType.TICKET_TYPE_WITH_EXEMPTION : PdWithTicketType.TICKET_TYPE_FULL);
        pdV14.setSaleDateTime(saleDateTime);
        pdV14.setTariffCode(tariffCode);
        pdV14.setEdsKeyNumber(edsKeyNumber);

        return pdV14;
    }

}
