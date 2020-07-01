package ru.ppr.core.dataCarrier.pd.v14;

import android.support.annotation.NonNull;

import java.nio.ByteOrder;
import java.util.Date;

import ru.ppr.core.dataCarrier.DataCarrierUtils;
import ru.ppr.core.dataCarrier.pd.PdEncoder;
import ru.ppr.core.dataCarrier.pd.base.Pd;
import ru.ppr.core.dataCarrier.pd.base.PdWithTicketType;

/**
 * Энкодер ПД v.14.
 *
 * @author Aleksandr Brazhkin
 */
public class PdV14Encoder implements PdEncoder {

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

        byte[] data = new byte[withEdsKeyNumber ? PdV14Structure.PD_SIZE : PdV14Structure.PD_DATA_SIZE];
        data[0] = (byte) pd.getVersion().getCode();

        PdV14 pdV14 = (PdV14) pd;

        int orderNumber = pdV14.getOrderNumber();
        DataCarrierUtils.writeInt(
                orderNumber,
                data,
                PdV14Structure.ORDER_NUMBER_BYTE_INDEX,
                PdV14Structure.ORDER_NUMBER_BIT_INDEX,
                PdV14Structure.ORDER_NUMBER_BIT_LENGTH,
                ByteOrder.LITTLE_ENDIAN
        );

        int startDayOffset = pdV14.getStartDayOffset();
        DataCarrierUtils.writeInt(
                startDayOffset,
                data,
                PdV14Structure.START_DAY_OFFSET_BYTE_INDEX,
                PdV14Structure.START_DAY_OFFSET_BIT_INDEX,
                PdV14Structure.START_DAY_OFFSET_BIT_LENGTH,
                ByteOrder.BIG_ENDIAN
        );

        boolean ticketType = pdV14.getTicketType() == PdWithTicketType.TICKET_TYPE_WITH_EXEMPTION;
        DataCarrierUtils.writeBoolean(
                ticketType,
                data,
                PdV14Structure.TICKET_TYPE_BYTE_INDEX,
                PdV14Structure.TICKET_TYPE_BIT_INDEX
        );

        Date saleDateTime = pdV14.getSaleDateTime();
        long saleDateTimeLong = DataCarrierUtils.dateToUnixTimestamp(saleDateTime);
        DataCarrierUtils.writeLong(
                saleDateTimeLong,
                data,
                PdV14Structure.SALE_DATE_TIME_BYTE_INDEX,
                PdV14Structure.SALE_DATE_TIME_BYTE_LENGTH,
                ByteOrder.LITTLE_ENDIAN
        );

        long tariffCode = pdV14.getTariffCode();
        DataCarrierUtils.writeLong(
                tariffCode,
                data,
                PdV14Structure.TARIFF_BYTE_INDEX,
                PdV14Structure.TARIFF_BYTE_LENGTH,
                ByteOrder.LITTLE_ENDIAN
        );

        if (!withEdsKeyNumber) {
            return data;
        }

        long edsKeyNumber = pdV14.getEdsKeyNumber();
        DataCarrierUtils.writeLong(
                edsKeyNumber,
                data,
                PdV14Structure.EDS_KEY_NUMBER_BYTE_INDEX,
                PdV14Structure.EDS_KEY_NUMBER_BYTE_LENGTH,
                ByteOrder.LITTLE_ENDIAN
        );

        return data;
    }
}
