package ru.ppr.core.dataCarrier.pd.v4;

import android.support.annotation.NonNull;

import java.nio.ByteOrder;
import java.util.Date;

import ru.ppr.core.dataCarrier.DataCarrierUtils;
import ru.ppr.core.dataCarrier.pd.PdDecoder;
import ru.ppr.core.dataCarrier.pd.base.PdWithTicketType;

/**
 * Декодер ПД v.4.
 *
 * @author Aleksandr Brazhkin
 */
public class PdV4Decoder implements PdDecoder {

    @Override
    public PdV4 decode(@NonNull byte[] data) {

        if (data.length < PdV4Structure.PD_SIZE)
            return null;

        byte[] orderNumberData = DataCarrierUtils.subArray(data, PdV4Structure.ORDER_NUMBER_BYTE_INDEX, PdV4Structure.ORDER_NUMBER_BYTE_LENGTH);
        int orderNumber = DataCarrierUtils.bytesToInt(orderNumberData, PdV4Structure.ORDER_NUMBER_BIT_INDEX, PdV4Structure.ORDER_NUMBER_BIT_LENGTH, ByteOrder.LITTLE_ENDIAN);

        byte startDayOffsetData = data[PdV4Structure.START_DAY_OFFSET_BYTE_INDEX];
        int startDayOffset = DataCarrierUtils.byteToInt(startDayOffsetData, PdV4Structure.START_DAY_OFFSET_BIT_INDEX, PdV4Structure.START_DAY_OFFSET_BIT_LENGTH);

        byte ticketTypeData = data[PdV4Structure.TICKET_TYPE_BYTE_INDEX];
        boolean ticketType = DataCarrierUtils.byteToBoolean(ticketTypeData, PdV4Structure.TICKET_TYPE_BIT_INDEX);

        byte[] saleDateTimeData = DataCarrierUtils.subArray(data, PdV4Structure.SALE_DATE_TIME_BYTE_INDEX, PdV4Structure.SALE_DATE_TIME_BYTE_LENGTH);
        long saleDateTimeLong = DataCarrierUtils.bytesToLong(saleDateTimeData, ByteOrder.LITTLE_ENDIAN);
        Date saleDateTime = DataCarrierUtils.unixTimestampToDate(saleDateTimeLong);

        byte[] tariffCodeData = DataCarrierUtils.subArray(data, PdV4Structure.TARIFF_BYTE_INDEX, PdV4Structure.TARIFF_BYTE_LENGTH);
        long tariffCode = DataCarrierUtils.bytesToLong(tariffCodeData, ByteOrder.LITTLE_ENDIAN);

        byte[] edsKeyNumberData = DataCarrierUtils.subArray(data, PdV4Structure.EDS_KEY_NUMBER_BYTE_INDEX, PdV4Structure.EDS_KEY_NUMBER_BYTE_LENGTH);
        long edsKeyNumber = DataCarrierUtils.bytesToLong(edsKeyNumberData, ByteOrder.LITTLE_ENDIAN);

        PdV4Impl pdV4 = new PdV4Impl();
        pdV4.setOrderNumber(orderNumber);
        pdV4.setStartDayOffset(startDayOffset);
        pdV4.setTicketType(ticketType ? PdWithTicketType.TICKET_TYPE_WITH_EXEMPTION : PdWithTicketType.TICKET_TYPE_FULL);
        pdV4.setSaleDateTime(saleDateTime);
        pdV4.setTariffCode(tariffCode);
        pdV4.setEdsKeyNumber(edsKeyNumber);

        return pdV4;
    }

}
