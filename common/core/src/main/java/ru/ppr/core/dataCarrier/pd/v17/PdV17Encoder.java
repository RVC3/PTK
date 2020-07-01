package ru.ppr.core.dataCarrier.pd.v17;

import android.support.annotation.NonNull;

import java.nio.ByteOrder;
import java.util.Date;

import ru.ppr.core.dataCarrier.DataCarrierUtils;
import ru.ppr.core.dataCarrier.pd.PdEncoder;
import ru.ppr.core.dataCarrier.pd.base.Pd;
import ru.ppr.core.dataCarrier.pd.base.PdWithDirection;

/**
 * Энкодер ПД v.17.
 *
 * @author Aleksandr Brazhkin
 */
public class PdV17Encoder implements PdEncoder {

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

        byte[] data = new byte[withEdsKeyNumber ? PdV17Structure.PD_SIZE : PdV17Structure.PD_DATA_SIZE];
        data[0] = (byte) pd.getVersion().getCode();

        PdV17 pdV17 = (PdV17) pd;

        int orderNumber = pdV17.getOrderNumber();
        DataCarrierUtils.writeInt(
                orderNumber,
                data,
                PdV17Structure.ORDER_NUMBER_BYTE_INDEX,
                PdV17Structure.ORDER_NUMBER_BIT_INDEX,
                PdV17Structure.ORDER_NUMBER_BIT_LENGTH,
                ByteOrder.LITTLE_ENDIAN
        );

        int startDayOffset = pdV17.getStartDayOffset();
        DataCarrierUtils.writeInt(
                startDayOffset,
                data,
                PdV17Structure.START_DAY_OFFSET_BYTE_INDEX,
                PdV17Structure.START_DAY_OFFSET_BIT_INDEX,
                PdV17Structure.START_DAY_OFFSET_BIT_LENGTH,
                ByteOrder.BIG_ENDIAN
        );

        boolean direction = pdV17.getDirection() == PdWithDirection.DIRECTION_BACK;
        DataCarrierUtils.writeBoolean(
                direction,
                data,
                PdV17Structure.DIRECTION_BYTE_INDEX,
                PdV17Structure.DIRECTION_BIT_INDEX
        );

        Date saleDateTime = pdV17.getSaleDateTime();
        long saleDateTimeLong = DataCarrierUtils.dateToUnixTimestamp(saleDateTime);
        DataCarrierUtils.writeLong(
                saleDateTimeLong,
                data,
                PdV17Structure.SALE_DATE_TIME_BYTE_INDEX,
                PdV17Structure.SALE_DATE_TIME_BYTE_LENGTH,
                ByteOrder.LITTLE_ENDIAN
        );

        long tariffCode = pdV17.getTariffCode();
        DataCarrierUtils.writeLong(
                tariffCode,
                data,
                PdV17Structure.TARIFF_BYTE_INDEX,
                PdV17Structure.TARIFF_BYTE_LENGTH,
                ByteOrder.LITTLE_ENDIAN
        );

        int exemptionCode = pdV17.getExemptionCode();
        DataCarrierUtils.writeInt(
                exemptionCode,
                data,
                PdV17Structure.EXEMPTION_BYTE_INDEX,
                PdV17Structure.EXEMPTION_BYTE_LENGTH,
                ByteOrder.LITTLE_ENDIAN
        );

        if (!withEdsKeyNumber) {
            return data;
        }

        long edsKeyNumber = pdV17.getEdsKeyNumber();
        DataCarrierUtils.writeLong(
                edsKeyNumber,
                data,
                PdV17Structure.EDS_KEY_NUMBER_BYTE_INDEX,
                PdV17Structure.EDS_KEY_NUMBER_BYTE_LENGTH,
                ByteOrder.LITTLE_ENDIAN
        );

        return data;
    }
}
