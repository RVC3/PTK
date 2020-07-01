package ru.ppr.core.dataCarrier.pd.v21;

import android.support.annotation.NonNull;

import java.nio.ByteOrder;
import java.util.Date;

import ru.ppr.core.dataCarrier.DataCarrierUtils;
import ru.ppr.core.dataCarrier.pd.PdEncoder;
import ru.ppr.core.dataCarrier.pd.base.Pd;
import ru.ppr.core.dataCarrier.pd.base.PdWithTicketType;

/**
 * Энкодер ПД v.12.
 *
 * @author Aleksandr Brazhkin
 */
public class PdV21Encoder implements PdEncoder {

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

        byte[] data = new byte[withEdsKeyNumber ? PdV21Structure.PD_SIZE : PdV21Structure.PD_DATA_SIZE];
        data[0] = (byte) pd.getVersion().getCode();

        PdV21 pdV21 = (PdV21) pd;

        int orderNumber = pdV21.getOrderNumber();
        DataCarrierUtils.writeInt(
                orderNumber,
                data,
                PdV21Structure.ORDER_NUMBER_BYTE_INDEX,
                PdV21Structure.ORDER_NUMBER_BIT_INDEX,
                PdV21Structure.ORDER_NUMBER_BIT_LENGTH,
                ByteOrder.LITTLE_ENDIAN
        );

        int startDayOffset = pdV21.getStartDayOffset();
        DataCarrierUtils.writeInt(
                startDayOffset,
                data,
                PdV21Structure.START_DAY_OFFSET_BYTE_INDEX,
                PdV21Structure.START_DAY_OFFSET_BIT_INDEX,
                PdV21Structure.START_DAY_OFFSET_BIT_LENGTH,
                ByteOrder.BIG_ENDIAN
        );

        boolean ticketType = pdV21.getTicketType() == PdWithTicketType.TICKET_TYPE_WITH_EXEMPTION;
        DataCarrierUtils.writeBoolean(
                ticketType,
                data,
                PdV21Structure.TICKET_TYPE_BYTE_INDEX,
                PdV21Structure.TICKET_TYPE_BIT_INDEX
        );

        Date saleDateTime = pdV21.getSaleDateTime();
        long saleDateTimeLong = DataCarrierUtils.dateToUnixTimestamp(saleDateTime);
        DataCarrierUtils.writeLong(
                saleDateTimeLong,
                data,
                PdV21Structure.SALE_DATE_TIME_BYTE_INDEX,
                PdV21Structure.SALE_DATE_TIME_BYTE_LENGTH,
                ByteOrder.LITTLE_ENDIAN
        );

        long serviceId = pdV21.getServiceId();
        DataCarrierUtils.writeLong(
                serviceId,
                data,
                PdV21Structure.SERVICE_ID_BYTE_INDEX,
                PdV21Structure.SERVICE_ID_BYTE_LENGTH,
                ByteOrder.LITTLE_ENDIAN
        );

        if (!withEdsKeyNumber) {
            return data;
        }

        long edsKeyNumber = pdV21.getEdsKeyNumber();
        DataCarrierUtils.writeLong(
                edsKeyNumber,
                data,
                PdV21Structure.EDS_KEY_NUMBER_BYTE_INDEX,
                PdV21Structure.EDS_KEY_NUMBER_BYTE_LENGTH,
                ByteOrder.LITTLE_ENDIAN
        );

        return data;
    }
}
