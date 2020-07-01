package ru.ppr.core.dataCarrier.pd.v12;

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
public class PdV12Encoder implements PdEncoder {

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

        byte[] data = new byte[withEdsKeyNumber ? PdV12Structure.PD_SIZE : PdV12Structure.PD_DATA_SIZE];
        data[0] = (byte) pd.getVersion().getCode();

        PdV12 pdV12 = (PdV12) pd;

        int orderNumber = pdV12.getOrderNumber();
        DataCarrierUtils.writeInt(
                orderNumber,
                data,
                PdV12Structure.ORDER_NUMBER_BYTE_INDEX,
                PdV12Structure.ORDER_NUMBER_BIT_INDEX,
                PdV12Structure.ORDER_NUMBER_BIT_LENGTH,
                ByteOrder.LITTLE_ENDIAN
        );

        int startDayOffset = pdV12.getStartDayOffset();
        DataCarrierUtils.writeInt(
                startDayOffset,
                data,
                PdV12Structure.START_DAY_OFFSET_BYTE_INDEX,
                PdV12Structure.START_DAY_OFFSET_BIT_INDEX,
                PdV12Structure.START_DAY_OFFSET_BIT_LENGTH,
                ByteOrder.BIG_ENDIAN
        );

        boolean ticketType = pdV12.getTicketType() == PdWithTicketType.TICKET_TYPE_WITH_EXEMPTION;
        DataCarrierUtils.writeBoolean(
                ticketType,
                data,
                PdV12Structure.TICKET_TYPE_BYTE_INDEX,
                PdV12Structure.TICKET_TYPE_BIT_INDEX
        );

        Date saleDateTime = pdV12.getSaleDateTime();
        long saleDateTimeLong = DataCarrierUtils.dateToUnixTimestamp(saleDateTime);
        DataCarrierUtils.writeLong(
                saleDateTimeLong,
                data,
                PdV12Structure.SALE_DATE_TIME_BYTE_INDEX,
                PdV12Structure.SALE_DATE_TIME_BYTE_LENGTH,
                ByteOrder.LITTLE_ENDIAN
        );

        long tariffCode = pdV12.getTariffCode();
        DataCarrierUtils.writeLong(
                tariffCode,
                data,
                PdV12Structure.TARIFF_BYTE_INDEX,
                PdV12Structure.TARIFF_BYTE_LENGTH,
                ByteOrder.LITTLE_ENDIAN
        );

        if (!withEdsKeyNumber) {
            return data;
        }

        long edsKeyNumber = pdV12.getEdsKeyNumber();
        DataCarrierUtils.writeLong(
                edsKeyNumber,
                data,
                PdV12Structure.EDS_KEY_NUMBER_BYTE_INDEX,
                PdV12Structure.EDS_KEY_NUMBER_BYTE_LENGTH,
                ByteOrder.LITTLE_ENDIAN
        );

        return data;
    }
}
