package ru.ppr.core.dataCarrier.pd.v1;

import android.support.annotation.NonNull;

import java.nio.ByteOrder;
import java.util.Date;

import ru.ppr.core.dataCarrier.DataCarrierUtils;
import ru.ppr.core.dataCarrier.pd.PdEncoder;
import ru.ppr.core.dataCarrier.pd.base.Pd;
import ru.ppr.core.dataCarrier.pd.base.PdWithDirection;
import ru.ppr.core.dataCarrier.pd.base.PdWithPaymentType;

/**
 * Энкодер ПД v.1.
 *
 * @author Aleksandr Brazhkin
 */
public class PdV1Encoder implements PdEncoder {

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

        byte[] data = new byte[withEdsKeyNumber ? PdV1Structure.PD_SIZE : PdV1Structure.PD_DATA_SIZE];
        data[0] = (byte) pd.getVersion().getCode();

        PdV1 pdV1 = (PdV1) pd;

        int orderNumber = pdV1.getOrderNumber();
        DataCarrierUtils.writeInt(
                orderNumber,
                data,
                PdV1Structure.ORDER_NUMBER_BYTE_INDEX,
                PdV1Structure.ORDER_NUMBER_BIT_INDEX,
                PdV1Structure.ORDER_NUMBER_BIT_LENGTH,
                ByteOrder.LITTLE_ENDIAN
        );

        int startDayOffset = pdV1.getStartDayOffset();
        DataCarrierUtils.writeInt(
                startDayOffset,
                data,
                PdV1Structure.START_DAY_OFFSET_BYTE_INDEX,
                PdV1Structure.START_DAY_OFFSET_BIT_INDEX,
                PdV1Structure.START_DAY_OFFSET_BIT_LENGTH,
                ByteOrder.BIG_ENDIAN
        );

        boolean direction = pdV1.getDirection() == PdWithDirection.DIRECTION_BACK;
        DataCarrierUtils.writeBoolean(
                direction,
                data,
                PdV1Structure.DIRECTION_BYTE_INDEX,
                PdV1Structure.DIRECTION_BIT_INDEX
        );

        boolean paymentType = pdV1.getPaymentType() == PdWithPaymentType.PAYMENT_TYPE_CARD;
        DataCarrierUtils.writeBoolean(
                paymentType,
                data,
                PdV1Structure.PAYMENT_TYPE_BYTE_INDEX,
                PdV1Structure.PAYMENT_TYPE_BIT_INDEX
        );

        Date saleDateTime = pdV1.getSaleDateTime();
        long saleDateTimeLong = DataCarrierUtils.dateToUnixTimestamp(saleDateTime);
        DataCarrierUtils.writeLong(
                saleDateTimeLong,
                data,
                PdV1Structure.SALE_DATE_TIME_BYTE_INDEX,
                PdV1Structure.SALE_DATE_TIME_BYTE_LENGTH,
                ByteOrder.LITTLE_ENDIAN
        );

        long tariffCode = pdV1.getTariffCode();
        DataCarrierUtils.writeLong(
                tariffCode,
                data,
                PdV1Structure.TARIFF_BYTE_INDEX,
                PdV1Structure.TARIFF_BYTE_LENGTH,
                ByteOrder.LITTLE_ENDIAN
        );

        int exemptionCode = pdV1.getExemptionCode();
        DataCarrierUtils.writeInt(
                exemptionCode,
                data,
                PdV1Structure.EXEMPTION_BYTE_INDEX,
                PdV1Structure.EXEMPTION_BYTE_LENGTH,
                ByteOrder.LITTLE_ENDIAN
        );

        if (!withEdsKeyNumber) {
            return data;
        }

        long edsKeyNumber = pdV1.getEdsKeyNumber();
        DataCarrierUtils.writeLong(
                edsKeyNumber,
                data,
                PdV1Structure.EDS_KEY_NUMBER_BYTE_INDEX,
                PdV1Structure.EDS_KEY_NUMBER_BYTE_LENGTH,
                ByteOrder.LITTLE_ENDIAN
        );

        byte[] eds = pdV1.getEds();
        DataCarrierUtils.writeBytes(eds, data, PdV1Structure.EDS_BYTE_INDEX);

        return data;
    }
}
