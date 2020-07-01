package ru.ppr.core.dataCarrier.pd.v3;

import android.support.annotation.NonNull;

import java.nio.ByteOrder;
import java.util.Date;

import ru.ppr.core.dataCarrier.DataCarrierUtils;
import ru.ppr.core.dataCarrier.pd.PdEncoder;
import ru.ppr.core.dataCarrier.pd.base.Pd;
import ru.ppr.core.dataCarrier.pd.base.PdWithDirection;
import ru.ppr.core.dataCarrier.pd.base.PdWithTicketType;

/**
 * Энкодер ПД v.3.
 *
 * @author Aleksandr Brazhkin
 */
public class PdV3Encoder implements PdEncoder {

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

        byte[] data = new byte[withEdsKeyNumber ? PdV3Structure.PD_SIZE : PdV3Structure.PD_DATA_SIZE];
        data[0] = (byte) pd.getVersion().getCode();

        PdV3 pdV3 = (PdV3) pd;

        int orderNumber = pdV3.getOrderNumber();
        DataCarrierUtils.writeInt(
                orderNumber,
                data,
                PdV3Structure.ORDER_NUMBER_BYTE_INDEX,
                PdV3Structure.ORDER_NUMBER_BIT_INDEX,
                PdV3Structure.ORDER_NUMBER_BIT_LENGTH,
                ByteOrder.LITTLE_ENDIAN
        );

        int startDayOffset = pdV3.getStartDayOffset();
        DataCarrierUtils.writeInt(
                startDayOffset,
                data,
                PdV3Structure.START_DAY_OFFSET_BYTE_INDEX,
                PdV3Structure.START_DAY_OFFSET_BIT_INDEX,
                PdV3Structure.START_DAY_OFFSET_BIT_LENGTH,
                ByteOrder.BIG_ENDIAN
        );

        boolean direction = pdV3.getDirection() == PdWithDirection.DIRECTION_BACK;
        DataCarrierUtils.writeBoolean(
                direction,
                data,
                PdV3Structure.DIRECTION_BYTE_INDEX,
                PdV3Structure.DIRECTION_BIT_INDEX
        );

        boolean ticketType = pdV3.getTicketType() == PdWithTicketType.TICKET_TYPE_WITH_EXEMPTION;
        DataCarrierUtils.writeBoolean(
                ticketType,
                data,
                PdV3Structure.TICKET_TYPE_BYTE_INDEX,
                PdV3Structure.TICKET_TYPE_BIT_INDEX
        );

        Date saleDateTime = pdV3.getSaleDateTime();
        long saleDateTimeLong = DataCarrierUtils.dateToUnixTimestamp(saleDateTime);
        DataCarrierUtils.writeLong(
                saleDateTimeLong,
                data,
                PdV3Structure.SALE_DATE_TIME_BYTE_INDEX,
                PdV3Structure.SALE_DATE_TIME_BYTE_LENGTH,
                ByteOrder.LITTLE_ENDIAN
        );

        long tariffCode = pdV3.getTariffCode();
        DataCarrierUtils.writeLong(
                tariffCode,
                data,
                PdV3Structure.TARIFF_BYTE_INDEX,
                PdV3Structure.TARIFF_BYTE_LENGTH,
                ByteOrder.LITTLE_ENDIAN
        );

        if (!withEdsKeyNumber) {
            return data;
        }

        long edsKeyNumber = pdV3.getEdsKeyNumber();
        DataCarrierUtils.writeLong(
                edsKeyNumber,
                data,
                PdV3Structure.EDS_KEY_NUMBER_BYTE_INDEX,
                PdV3Structure.EDS_KEY_NUMBER_BYTE_LENGTH,
                ByteOrder.LITTLE_ENDIAN
        );

        return data;
    }

}
