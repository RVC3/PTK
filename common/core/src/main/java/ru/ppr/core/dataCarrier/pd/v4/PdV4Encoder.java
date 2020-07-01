package ru.ppr.core.dataCarrier.pd.v4;

import android.support.annotation.NonNull;

import java.nio.ByteOrder;
import java.util.Date;

import ru.ppr.core.dataCarrier.DataCarrierUtils;
import ru.ppr.core.dataCarrier.pd.PdEncoder;
import ru.ppr.core.dataCarrier.pd.base.Pd;
import ru.ppr.core.dataCarrier.pd.base.PdWithTicketType;

/**
 * Энкодер ПД v.4.
 *
 * @author Aleksandr Brazhkin
 */
public class PdV4Encoder implements PdEncoder {

    @NonNull
    @Override
    public byte[] encode(@NonNull Pd pd) {
        return internalEncode(pd, true);
    }

    @NonNull
    @Override
    public byte[] encodeWithoutEdsKeyNumber(@NonNull Pd pd) {
        return internalEncode(pd, false);
    }

    private byte[] internalEncode(@NonNull Pd pd, boolean withEdsKeyNumber) {

        byte[] data = new byte[withEdsKeyNumber ? PdV4Structure.PD_SIZE : PdV4Structure.PD_DATA_SIZE];
        data[0] = (byte) pd.getVersion().getCode();

        PdV4 pdV4 = (PdV4) pd;

        int orderNumber = pdV4.getOrderNumber();
        DataCarrierUtils.writeInt(
                orderNumber,
                data,
                PdV4Structure.ORDER_NUMBER_BYTE_INDEX,
                PdV4Structure.ORDER_NUMBER_BIT_INDEX,
                PdV4Structure.ORDER_NUMBER_BIT_LENGTH,
                ByteOrder.LITTLE_ENDIAN
        );

        int startDayOffset = pdV4.getStartDayOffset();
        DataCarrierUtils.writeInt(
                startDayOffset,
                data,
                PdV4Structure.START_DAY_OFFSET_BYTE_INDEX,
                PdV4Structure.START_DAY_OFFSET_BIT_INDEX,
                PdV4Structure.START_DAY_OFFSET_BIT_LENGTH,
                ByteOrder.BIG_ENDIAN
        );

        boolean ticketType = pdV4.getTicketType() == PdWithTicketType.TICKET_TYPE_WITH_EXEMPTION;
        DataCarrierUtils.writeBoolean(
                ticketType,
                data,
                PdV4Structure.TICKET_TYPE_BYTE_INDEX,
                PdV4Structure.TICKET_TYPE_BIT_INDEX
        );

        Date saleDateTime = pdV4.getSaleDateTime();
        long saleDateTimeLong = DataCarrierUtils.dateToUnixTimestamp(saleDateTime);
        DataCarrierUtils.writeLong(
                saleDateTimeLong,
                data,
                PdV4Structure.SALE_DATE_TIME_BYTE_INDEX,
                PdV4Structure.SALE_DATE_TIME_BYTE_LENGTH,
                ByteOrder.LITTLE_ENDIAN
        );

        long tariffCode = pdV4.getTariffCode();
        DataCarrierUtils.writeLong(
                tariffCode,
                data,
                PdV4Structure.TARIFF_BYTE_INDEX,
                PdV4Structure.TARIFF_BYTE_LENGTH,
                ByteOrder.LITTLE_ENDIAN
        );

        if (!withEdsKeyNumber) {
            return data;
        }

        long edsKeyNumber = pdV4.getEdsKeyNumber();
        DataCarrierUtils.writeLong(
                edsKeyNumber,
                data,
                PdV4Structure.EDS_KEY_NUMBER_BYTE_INDEX,
                PdV4Structure.EDS_KEY_NUMBER_BYTE_LENGTH,
                ByteOrder.LITTLE_ENDIAN
        );

        return data;
    }
}
