package ru.ppr.core.dataCarrier.pd.v13;

import android.support.annotation.NonNull;

import java.nio.ByteOrder;
import java.util.Date;

import ru.ppr.core.dataCarrier.DataCarrierUtils;
import ru.ppr.core.dataCarrier.pd.PdEncoder;
import ru.ppr.core.dataCarrier.pd.base.Pd;
import ru.ppr.core.dataCarrier.pd.base.PdWithDirection;

/**
 * Энкодер ПД v.13.
 *
 * @author Aleksandr Brazhkin
 */
public class PdV13Encoder implements PdEncoder {

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

        byte[] data = new byte[withEdsKeyNumber ? PdV13Structure.PD_SIZE : PdV13Structure.PD_DATA_SIZE];
        data[0] = (byte) pd.getVersion().getCode();

        PdV13 pdV13 = (PdV13) pd;

        int orderNumber = pdV13.getOrderNumber();
        DataCarrierUtils.writeInt(
                orderNumber,
                data,
                PdV13Structure.ORDER_NUMBER_BYTE_INDEX,
                PdV13Structure.ORDER_NUMBER_BIT_INDEX,
                PdV13Structure.ORDER_NUMBER_BIT_LENGTH,
                ByteOrder.LITTLE_ENDIAN
        );

        int startDayOffset = pdV13.getStartDayOffset();
        DataCarrierUtils.writeInt(
                startDayOffset,
                data,
                PdV13Structure.START_DAY_OFFSET_BYTE_INDEX,
                PdV13Structure.START_DAY_OFFSET_BIT_INDEX,
                PdV13Structure.START_DAY_OFFSET_BIT_LENGTH,
                ByteOrder.BIG_ENDIAN
        );

        boolean direction = pdV13.getDirection() == PdWithDirection.DIRECTION_BACK;
        DataCarrierUtils.writeBoolean(
                direction,
                data,
                PdV13Structure.DIRECTION_BYTE_INDEX,
                PdV13Structure.DIRECTION_BIT_INDEX
        );

        Date saleDateTime = pdV13.getSaleDateTime();
        long saleDateTimeLong = DataCarrierUtils.dateToUnixTimestamp(saleDateTime);
        DataCarrierUtils.writeLong(
                saleDateTimeLong,
                data,
                PdV13Structure.SALE_DATE_TIME_BYTE_INDEX,
                PdV13Structure.SALE_DATE_TIME_BYTE_LENGTH,
                ByteOrder.LITTLE_ENDIAN
        );

        long tariffCode = pdV13.getTariffCode();
        DataCarrierUtils.writeLong(
                tariffCode,
                data,
                PdV13Structure.TARIFF_BYTE_INDEX,
                PdV13Structure.TARIFF_BYTE_LENGTH,
                ByteOrder.LITTLE_ENDIAN
        );

        int exemptionCode = pdV13.getExemptionCode();
        DataCarrierUtils.writeInt(
                exemptionCode,
                data,
                PdV13Structure.EXEMPTION_BYTE_INDEX,
                PdV13Structure.EXEMPTION_BYTE_LENGTH,
                ByteOrder.LITTLE_ENDIAN
        );

        if (!withEdsKeyNumber) {
            return data;
        }

        long edsKeyNumber = pdV13.getEdsKeyNumber();
        DataCarrierUtils.writeLong(
                edsKeyNumber,
                data,
                PdV13Structure.EDS_KEY_NUMBER_BYTE_INDEX,
                PdV13Structure.EDS_KEY_NUMBER_BYTE_LENGTH,
                ByteOrder.LITTLE_ENDIAN
        );

        return data;
    }
}
