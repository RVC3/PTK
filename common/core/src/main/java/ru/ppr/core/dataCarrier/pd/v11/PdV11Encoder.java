package ru.ppr.core.dataCarrier.pd.v11;

import android.support.annotation.NonNull;

import java.nio.ByteOrder;
import java.util.Date;

import ru.ppr.core.dataCarrier.DataCarrierUtils;
import ru.ppr.core.dataCarrier.pd.PdEncoder;
import ru.ppr.core.dataCarrier.pd.base.Pd;
import ru.ppr.core.dataCarrier.pd.base.PdWithDirection;
import ru.ppr.core.dataCarrier.pd.base.PdWithTicketType;

/**
 * Энкодер ПД v.11.
 *
 * @author Aleksandr Brazhkin
 */
public class PdV11Encoder implements PdEncoder {

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

        byte[] data = new byte[withEdsKeyNumber ? PdV11Structure.PD_SIZE : PdV11Structure.PD_DATA_SIZE];
        data[0] = (byte) pd.getVersion().getCode();

        PdV11 pdV11 = (PdV11) pd;

        int orderNumber = pdV11.getOrderNumber();
        DataCarrierUtils.writeInt(
                orderNumber,
                data,
                PdV11Structure.ORDER_NUMBER_BYTE_INDEX,
                PdV11Structure.ORDER_NUMBER_BIT_INDEX,
                PdV11Structure.ORDER_NUMBER_BIT_LENGTH,
                ByteOrder.LITTLE_ENDIAN
        );

        int startDayOffset = pdV11.getStartDayOffset();
        DataCarrierUtils.writeInt(
                startDayOffset,
                data,
                PdV11Structure.START_DAY_OFFSET_BYTE_INDEX,
                PdV11Structure.START_DAY_OFFSET_BIT_INDEX,
                PdV11Structure.START_DAY_OFFSET_BIT_LENGTH,
                ByteOrder.BIG_ENDIAN
        );

        boolean direction = pdV11.getDirection() == PdWithDirection.DIRECTION_BACK;
        DataCarrierUtils.writeBoolean(
                direction,
                data,
                PdV11Structure.DIRECTION_BYTE_INDEX,
                PdV11Structure.DIRECTION_BIT_INDEX
        );

        boolean ticketType = pdV11.getTicketType() == PdWithTicketType.TICKET_TYPE_WITH_EXEMPTION;
        DataCarrierUtils.writeBoolean(
                ticketType,
                data,
                PdV11Structure.TICKET_TYPE_BYTE_INDEX,
                PdV11Structure.TICKET_TYPE_BIT_INDEX
        );

        Date saleDateTime = pdV11.getSaleDateTime();
        long saleDateTimeLong = DataCarrierUtils.dateToUnixTimestamp(saleDateTime);
        DataCarrierUtils.writeLong(
                saleDateTimeLong,
                data,
                PdV11Structure.SALE_DATE_TIME_BYTE_INDEX,
                PdV11Structure.SALE_DATE_TIME_BYTE_LENGTH,
                ByteOrder.LITTLE_ENDIAN
        );

        long tariffCode = pdV11.getTariffCode();
        DataCarrierUtils.writeLong(
                tariffCode,
                data,
                PdV11Structure.TARIFF_BYTE_INDEX,
                PdV11Structure.TARIFF_BYTE_LENGTH,
                ByteOrder.LITTLE_ENDIAN
        );

        if (!withEdsKeyNumber) {
            return data;
        }

        long edsKeyNumber = pdV11.getEdsKeyNumber();
        DataCarrierUtils.writeLong(
                edsKeyNumber,
                data,
                PdV11Structure.EDS_KEY_NUMBER_BYTE_INDEX,
                PdV11Structure.EDS_KEY_NUMBER_BYTE_LENGTH,
                ByteOrder.LITTLE_ENDIAN
        );

        return data;
    }

}
