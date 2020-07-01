package ru.ppr.core.dataCarrier.pd.v12;

import android.support.annotation.NonNull;

import java.nio.ByteOrder;
import java.util.Date;

import ru.ppr.core.dataCarrier.DataCarrierUtils;
import ru.ppr.core.dataCarrier.pd.PdDecoder;
import ru.ppr.core.dataCarrier.pd.base.PdWithTicketType;

/**
 * Декодер ПД v.12.
 *
 * @author Aleksandr Brazhkin
 */
public class PdV12Decoder implements PdDecoder {

    @Override
    public PdV12 decode(@NonNull byte[] data) {

        if (data.length < PdV12Structure.PD_SIZE)
            return null;

        byte[] orderNumberData = DataCarrierUtils.subArray(data, PdV12Structure.ORDER_NUMBER_BYTE_INDEX, PdV12Structure.ORDER_NUMBER_BYTE_LENGTH);
        int orderNumber = DataCarrierUtils.bytesToInt(orderNumberData, PdV12Structure.ORDER_NUMBER_BIT_INDEX, PdV12Structure.ORDER_NUMBER_BIT_LENGTH, ByteOrder.LITTLE_ENDIAN);

        byte startDayOffsetData = data[PdV12Structure.START_DAY_OFFSET_BYTE_INDEX];
        int startDayOffset = DataCarrierUtils.byteToInt(startDayOffsetData, PdV12Structure.START_DAY_OFFSET_BIT_INDEX, PdV12Structure.START_DAY_OFFSET_BIT_LENGTH);

        byte ticketTypeData = data[PdV12Structure.TICKET_TYPE_BYTE_INDEX];
        boolean ticketType = DataCarrierUtils.byteToBoolean(ticketTypeData, PdV12Structure.TICKET_TYPE_BIT_INDEX);

        byte[] saleDateTimeData = DataCarrierUtils.subArray(data, PdV12Structure.SALE_DATE_TIME_BYTE_INDEX, PdV12Structure.SALE_DATE_TIME_BYTE_LENGTH);
        long saleDateTimeLong = DataCarrierUtils.bytesToLong(saleDateTimeData, ByteOrder.LITTLE_ENDIAN);
        Date saleDateTime = DataCarrierUtils.unixTimestampToDate(saleDateTimeLong);

        byte[] tariffCodeData = DataCarrierUtils.subArray(data, PdV12Structure.TARIFF_BYTE_INDEX, PdV12Structure.TARIFF_BYTE_LENGTH);
        long tariffCode = DataCarrierUtils.bytesToLong(tariffCodeData, ByteOrder.LITTLE_ENDIAN);

        byte[] edsKeyNumberData = DataCarrierUtils.subArray(data, PdV12Structure.EDS_KEY_NUMBER_BYTE_INDEX, PdV12Structure.EDS_KEY_NUMBER_BYTE_LENGTH);
        long edsKeyNumber = DataCarrierUtils.bytesToLong(edsKeyNumberData, ByteOrder.LITTLE_ENDIAN);

        PdV12Impl pdV12 = new PdV12Impl();
        pdV12.setOrderNumber(orderNumber);
        pdV12.setStartDayOffset(startDayOffset);
        pdV12.setTicketType(ticketType ? PdWithTicketType.TICKET_TYPE_WITH_EXEMPTION : PdWithTicketType.TICKET_TYPE_FULL);
        pdV12.setSaleDateTime(saleDateTime);
        pdV12.setTariffCode(tariffCode);
        pdV12.setEdsKeyNumber(edsKeyNumber);

        return pdV12;
    }

}
