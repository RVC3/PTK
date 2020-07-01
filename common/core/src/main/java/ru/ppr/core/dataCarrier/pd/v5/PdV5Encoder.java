package ru.ppr.core.dataCarrier.pd.v5;

import android.support.annotation.NonNull;

import java.nio.ByteOrder;
import java.util.Date;

import ru.ppr.core.dataCarrier.DataCarrierUtils;
import ru.ppr.core.dataCarrier.pd.PdEncoder;
import ru.ppr.core.dataCarrier.pd.base.Pd;
import ru.ppr.core.dataCarrier.pd.base.PdWithDirection;

/**
 * Энкодер ПД v.5.
 *
 * @author Aleksandr Brazhkin
 */
public class PdV5Encoder implements PdEncoder {

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

        byte[] data = new byte[withEdsKeyNumber ? PdV5Structure.PD_SIZE : PdV5Structure.PD_DATA_SIZE];
        data[0] = (byte) pd.getVersion().getCode();

        PdV5 pdV5 = (PdV5) pd;

        int orderNumber = pdV5.getOrderNumber();
        DataCarrierUtils.writeInt(
                orderNumber,
                data,
                PdV5Structure.ORDER_NUMBER_BYTE_INDEX,
                PdV5Structure.ORDER_NUMBER_BIT_INDEX,
                PdV5Structure.ORDER_NUMBER_BIT_LENGTH,
                ByteOrder.LITTLE_ENDIAN
        );

        int startDayOffset = pdV5.getStartDayOffset();
        DataCarrierUtils.writeInt(
                startDayOffset,
                data,
                PdV5Structure.START_DAY_OFFSET_BYTE_INDEX,
                PdV5Structure.START_DAY_OFFSET_BIT_INDEX,
                PdV5Structure.START_DAY_OFFSET_BIT_LENGTH,
                ByteOrder.BIG_ENDIAN
        );

        boolean direction = pdV5.getDirection() == PdWithDirection.DIRECTION_BACK;
        DataCarrierUtils.writeBoolean(
                direction,
                data,
                PdV5Structure.DIRECTION_BYTE_INDEX,
                PdV5Structure.DIRECTION_BIT_INDEX
        );

        Date saleDateTime = pdV5.getSaleDateTime();
        long saleDateTimeLong = DataCarrierUtils.dateToUnixTimestamp(saleDateTime);
        DataCarrierUtils.writeLong(
                saleDateTimeLong,
                data,
                PdV5Structure.SALE_DATE_TIME_BYTE_INDEX,
                PdV5Structure.SALE_DATE_TIME_BYTE_LENGTH,
                ByteOrder.LITTLE_ENDIAN
        );

        long tariffCode = pdV5.getTariffCode();
        DataCarrierUtils.writeLong(
                tariffCode,
                data,
                PdV5Structure.TARIFF_BYTE_INDEX,
                PdV5Structure.TARIFF_BYTE_LENGTH,
                ByteOrder.LITTLE_ENDIAN
        );

        int exemptionCode = pdV5.getExemptionCode();
        DataCarrierUtils.writeInt(
                exemptionCode,
                data,
                PdV5Structure.EXEMPTION_BYTE_INDEX,
                PdV5Structure.EXEMPTION_BYTE_LENGTH,
                ByteOrder.LITTLE_ENDIAN
        );

        if (!withEdsKeyNumber) {
            return data;
        }

        long edsKeyNumber = pdV5.getEdsKeyNumber();
        DataCarrierUtils.writeLong(
                edsKeyNumber,
                data,
                PdV5Structure.EDS_KEY_NUMBER_BYTE_INDEX,
                PdV5Structure.EDS_KEY_NUMBER_BYTE_LENGTH,
                ByteOrder.LITTLE_ENDIAN
        );

        return data;
    }
}
