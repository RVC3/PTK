package ru.ppr.core.dataCarrier.pd.v15;

import android.support.annotation.NonNull;

import java.nio.ByteOrder;
import java.util.Date;

import ru.ppr.core.dataCarrier.DataCarrierUtils;
import ru.ppr.core.dataCarrier.pd.PdDecoder;
import ru.ppr.core.dataCarrier.pd.base.PdWithTicketType;

/**
 * Декодер ПД v.15.
 *
 * @author Aleksandr Brazhkin
 */
public class PdV15Decoder implements PdDecoder {

    @Override
    public PdV15 decode(@NonNull byte[] data) {

        if (data.length < PdV15Structure.PD_SIZE)
            return null;

        byte[] orderNumberData = DataCarrierUtils.subArray(data, PdV15Structure.ORDER_NUMBER_BYTE_INDEX, PdV15Structure.ORDER_NUMBER_BYTE_LENGTH);
        int orderNumber = DataCarrierUtils.bytesToInt(orderNumberData, PdV15Structure.ORDER_NUMBER_BIT_INDEX, PdV15Structure.ORDER_NUMBER_BIT_LENGTH, ByteOrder.LITTLE_ENDIAN);

        byte startDayOffsetData = data[PdV15Structure.START_DAY_OFFSET_BYTE_INDEX];
        int startDayOffset = DataCarrierUtils.byteToInt(startDayOffsetData, PdV15Structure.START_DAY_OFFSET_BIT_INDEX, PdV15Structure.START_DAY_OFFSET_BIT_LENGTH);

        byte ticketTypeData = data[PdV15Structure.TICKET_TYPE_BYTE_INDEX];
        boolean ticketType = DataCarrierUtils.byteToBoolean(ticketTypeData, PdV15Structure.TICKET_TYPE_BIT_INDEX);

        byte[] saleDateTimeData = DataCarrierUtils.subArray(data, PdV15Structure.SALE_DATE_TIME_BYTE_INDEX, PdV15Structure.SALE_DATE_TIME_BYTE_LENGTH);
        long saleDateTimeLong = DataCarrierUtils.bytesToLong(saleDateTimeData, ByteOrder.LITTLE_ENDIAN);
        Date saleDateTime = DataCarrierUtils.unixTimestampToDate(saleDateTimeLong);

        byte[] tariffCodeData = DataCarrierUtils.subArray(data, PdV15Structure.TARIFF_BYTE_INDEX, PdV15Structure.TARIFF_BYTE_LENGTH);
        long tariffCode = DataCarrierUtils.bytesToLong(tariffCodeData, ByteOrder.LITTLE_ENDIAN);

        byte[] edsKeyNumberData = DataCarrierUtils.subArray(data, PdV15Structure.EDS_KEY_NUMBER_BYTE_INDEX, PdV15Structure.EDS_KEY_NUMBER_BYTE_LENGTH);
        long edsKeyNumber = DataCarrierUtils.bytesToLong(edsKeyNumberData, ByteOrder.LITTLE_ENDIAN);

        PdV15Impl pdV15 = new PdV15Impl();
        pdV15.setOrderNumber(orderNumber);
        pdV15.setStartDayOffset(startDayOffset);
        pdV15.setTicketType(ticketType ? PdWithTicketType.TICKET_TYPE_WITH_EXEMPTION : PdWithTicketType.TICKET_TYPE_FULL);
        pdV15.setSaleDateTime(saleDateTime);
        pdV15.setTariffCode(tariffCode);
        pdV15.setEdsKeyNumber(edsKeyNumber);

        return pdV15;
    }

}
