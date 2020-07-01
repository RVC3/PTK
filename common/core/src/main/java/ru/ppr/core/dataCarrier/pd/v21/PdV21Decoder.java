package ru.ppr.core.dataCarrier.pd.v21;

import android.support.annotation.NonNull;

import java.nio.ByteOrder;
import java.util.Date;

import ru.ppr.core.dataCarrier.DataCarrierUtils;
import ru.ppr.core.dataCarrier.pd.PdDecoder;
import ru.ppr.core.dataCarrier.pd.base.PdWithTicketType;

/**
 * Декодер ПД v.21.
 *
 * @author Aleksandr Brazhkin
 */
public class PdV21Decoder implements PdDecoder {

    @Override
    public PdV21 decode(@NonNull byte[] data) {

        if (data.length < PdV21Structure.PD_SIZE)
            return null;

        byte[] orderNumberData = DataCarrierUtils.subArray(data, PdV21Structure.ORDER_NUMBER_BYTE_INDEX, PdV21Structure.ORDER_NUMBER_BYTE_LENGTH);
        int orderNumber = DataCarrierUtils.bytesToInt(orderNumberData, PdV21Structure.ORDER_NUMBER_BIT_INDEX, PdV21Structure.ORDER_NUMBER_BIT_LENGTH, ByteOrder.LITTLE_ENDIAN);

        byte startDayOffsetData = data[PdV21Structure.START_DAY_OFFSET_BYTE_INDEX];
        int startDayOffset = DataCarrierUtils.byteToInt(startDayOffsetData, PdV21Structure.START_DAY_OFFSET_BIT_INDEX, PdV21Structure.START_DAY_OFFSET_BIT_LENGTH);

        byte ticketTypeData = data[PdV21Structure.TICKET_TYPE_BYTE_INDEX];
        boolean ticketType = DataCarrierUtils.byteToBoolean(ticketTypeData, PdV21Structure.TICKET_TYPE_BIT_INDEX);

        byte[] saleDateTimeData = DataCarrierUtils.subArray(data, PdV21Structure.SALE_DATE_TIME_BYTE_INDEX, PdV21Structure.SALE_DATE_TIME_BYTE_LENGTH);
        long saleDateTimeLong = DataCarrierUtils.bytesToLong(saleDateTimeData, ByteOrder.LITTLE_ENDIAN);
        Date saleDateTime = DataCarrierUtils.unixTimestampToDate(saleDateTimeLong);

        byte[] serviceIdData = DataCarrierUtils.subArray(data, PdV21Structure.SERVICE_ID_BYTE_INDEX, PdV21Structure.SERVICE_ID_BYTE_LENGTH);
        long serviceId = DataCarrierUtils.bytesToLong(serviceIdData, ByteOrder.LITTLE_ENDIAN);

        byte[] edsKeyNumberData = DataCarrierUtils.subArray(data, PdV21Structure.EDS_KEY_NUMBER_BYTE_INDEX, PdV21Structure.EDS_KEY_NUMBER_BYTE_LENGTH);
        long edsKeyNumber = DataCarrierUtils.bytesToLong(edsKeyNumberData, ByteOrder.LITTLE_ENDIAN);

        PdV21Impl pdV21 = new PdV21Impl();
        pdV21.setOrderNumber(orderNumber);
        pdV21.setStartDayOffset(startDayOffset);
        pdV21.setTicketType(ticketType ? PdWithTicketType.TICKET_TYPE_WITH_EXEMPTION : PdWithTicketType.TICKET_TYPE_FULL);
        pdV21.setSaleDateTime(saleDateTime);
        pdV21.setServiceId(serviceId);
        pdV21.setEdsKeyNumber(edsKeyNumber);

        return pdV21;
    }

}
