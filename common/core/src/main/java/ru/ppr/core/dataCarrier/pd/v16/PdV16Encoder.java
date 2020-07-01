package ru.ppr.core.dataCarrier.pd.v16;

import android.support.annotation.NonNull;

import java.nio.ByteOrder;
import java.util.Date;

import ru.ppr.core.dataCarrier.DataCarrierUtils;
import ru.ppr.core.dataCarrier.pd.PdEncoder;
import ru.ppr.core.dataCarrier.pd.base.Pd;
import ru.ppr.core.dataCarrier.pd.base.PdWithDirection;

/**
 * Энкодер ПД v.16.
 *
 * @author Aleksandr Brazhkin
 */
public class PdV16Encoder implements PdEncoder {

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

        byte[] data = new byte[withEdsKeyNumber ? PdV16Structure.PD_SIZE : PdV16Structure.PD_DATA_SIZE];
        data[0] = (byte) pd.getVersion().getCode();

        PdV16 pdV16 = (PdV16) pd;

        int orderNumber = pdV16.getOrderNumber();
        DataCarrierUtils.writeInt(
                orderNumber,
                data,
                PdV16Structure.ORDER_NUMBER_BYTE_INDEX,
                PdV16Structure.ORDER_NUMBER_BIT_INDEX,
                PdV16Structure.ORDER_NUMBER_BIT_LENGTH,
                ByteOrder.LITTLE_ENDIAN
        );

        int startDayOffset = pdV16.getStartDayOffset();
        DataCarrierUtils.writeInt(
                startDayOffset,
                data,
                PdV16Structure.START_DAY_OFFSET_BYTE_INDEX,
                PdV16Structure.START_DAY_OFFSET_BIT_INDEX,
                PdV16Structure.START_DAY_OFFSET_BIT_LENGTH,
                ByteOrder.BIG_ENDIAN
        );

        boolean direction = pdV16.getDirection() == PdWithDirection.DIRECTION_BACK;
        DataCarrierUtils.writeBoolean(
                direction,
                data,
                PdV16Structure.DIRECTION_BYTE_INDEX,
                PdV16Structure.DIRECTION_BIT_INDEX
        );

        Date saleDateTime = pdV16.getSaleDateTime();
        long saleDateTimeLong = DataCarrierUtils.dateToUnixTimestamp(saleDateTime);
        DataCarrierUtils.writeLong(
                saleDateTimeLong,
                data,
                PdV16Structure.SALE_DATE_TIME_BYTE_INDEX,
                PdV16Structure.SALE_DATE_TIME_BYTE_LENGTH,
                ByteOrder.LITTLE_ENDIAN
        );

        long tariffCode = pdV16.getTariffCode();
        DataCarrierUtils.writeLong(
                tariffCode,
                data,
                PdV16Structure.TARIFF_BYTE_INDEX,
                PdV16Structure.TARIFF_BYTE_LENGTH,
                ByteOrder.LITTLE_ENDIAN
        );

        int exemptionCode = pdV16.getExemptionCode();
        DataCarrierUtils.writeInt(
                exemptionCode,
                data,
                PdV16Structure.EXEMPTION_BYTE_INDEX,
                PdV16Structure.EXEMPTION_BYTE_LENGTH,
                ByteOrder.LITTLE_ENDIAN
        );

        if (!withEdsKeyNumber) {
            return data;
        }

        long edsKeyNumber = pdV16.getEdsKeyNumber();
        DataCarrierUtils.writeLong(
                edsKeyNumber,
                data,
                PdV16Structure.EDS_KEY_NUMBER_BYTE_INDEX,
                PdV16Structure.EDS_KEY_NUMBER_BYTE_LENGTH,
                ByteOrder.LITTLE_ENDIAN
        );

        return data;
    }
}
