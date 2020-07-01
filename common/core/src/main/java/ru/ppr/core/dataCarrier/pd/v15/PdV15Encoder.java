package ru.ppr.core.dataCarrier.pd.v15;

import android.support.annotation.NonNull;

import java.nio.ByteOrder;
import java.util.Date;

import ru.ppr.core.dataCarrier.DataCarrierUtils;
import ru.ppr.core.dataCarrier.pd.PdEncoder;
import ru.ppr.core.dataCarrier.pd.base.Pd;
import ru.ppr.core.dataCarrier.pd.base.PdWithTicketType;

/**
 * Энкодер ПД v.15.
 *
 * @author Aleksandr Brazhkin
 */
public class PdV15Encoder implements PdEncoder {

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

        byte[] data = new byte[withEdsKeyNumber ? PdV15Structure.PD_SIZE : PdV15Structure.PD_DATA_SIZE];
        data[0] = (byte) pd.getVersion().getCode();

        PdV15 pdV15 = (PdV15) pd;

        int orderNumber = pdV15.getOrderNumber();
        DataCarrierUtils.writeInt(
                orderNumber,
                data,
                PdV15Structure.ORDER_NUMBER_BYTE_INDEX,
                PdV15Structure.ORDER_NUMBER_BIT_INDEX,
                PdV15Structure.ORDER_NUMBER_BIT_LENGTH,
                ByteOrder.LITTLE_ENDIAN
        );

        int startDayOffset = pdV15.getStartDayOffset();
        DataCarrierUtils.writeInt(
                startDayOffset,
                data,
                PdV15Structure.START_DAY_OFFSET_BYTE_INDEX,
                PdV15Structure.START_DAY_OFFSET_BIT_INDEX,
                PdV15Structure.START_DAY_OFFSET_BIT_LENGTH,
                ByteOrder.BIG_ENDIAN
        );

        boolean ticketType = pdV15.getTicketType() == PdWithTicketType.TICKET_TYPE_WITH_EXEMPTION;
        DataCarrierUtils.writeBoolean(
                ticketType,
                data,
                PdV15Structure.TICKET_TYPE_BYTE_INDEX,
                PdV15Structure.TICKET_TYPE_BIT_INDEX
        );

        Date saleDateTime = pdV15.getSaleDateTime();
        long saleDateTimeLong = DataCarrierUtils.dateToUnixTimestamp(saleDateTime);
        DataCarrierUtils.writeLong(
                saleDateTimeLong,
                data,
                PdV15Structure.SALE_DATE_TIME_BYTE_INDEX,
                PdV15Structure.SALE_DATE_TIME_BYTE_LENGTH,
                ByteOrder.LITTLE_ENDIAN
        );

        long tariffCode = pdV15.getTariffCode();
        DataCarrierUtils.writeLong(
                tariffCode,
                data,
                PdV15Structure.TARIFF_BYTE_INDEX,
                PdV15Structure.TARIFF_BYTE_LENGTH,
                ByteOrder.LITTLE_ENDIAN
        );

        if (!withEdsKeyNumber) {
            return data;
        }

        long edsKeyNumber = pdV15.getEdsKeyNumber();
        DataCarrierUtils.writeLong(
                edsKeyNumber,
                data,
                PdV15Structure.EDS_KEY_NUMBER_BYTE_INDEX,
                PdV15Structure.EDS_KEY_NUMBER_BYTE_LENGTH,
                ByteOrder.LITTLE_ENDIAN
        );

        return data;
    }
}
