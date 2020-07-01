package ru.ppr.core.dataCarrier.pd.v25;

import android.support.annotation.NonNull;

import java.nio.ByteOrder;
import java.util.Date;

import ru.ppr.core.dataCarrier.DataCarrierUtils;
import ru.ppr.core.dataCarrier.pd.PdEncoder;
import ru.ppr.core.dataCarrier.pd.base.Pd;
import ru.ppr.core.dataCarrier.pd.base.PdWithPaymentType;

/**
 * Энкодер ПД v.25.
 *
 * @author Grigoriy Kashka
 */
public class PdV25Encoder implements PdEncoder {

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

        byte[] data = new byte[withEdsKeyNumber ? PdV25Structure.PD_SIZE : PdV25Structure.PD_DATA_SIZE];
        data[0] = (byte) pd.getVersion().getCode();

        PdV25 pdV25 = (PdV25) pd;

        int orderNumber = pdV25.getOrderNumber();
        DataCarrierUtils.writeInt(
                orderNumber,
                data,
                PdV25Structure.ORDER_NUMBER_BYTE_INDEX,
                PdV25Structure.ORDER_NUMBER_BIT_INDEX,
                PdV25Structure.ORDER_NUMBER_BIT_LENGTH,
                ByteOrder.LITTLE_ENDIAN
        );

        int startDayOffset = pdV25.getStartDayOffset();
        DataCarrierUtils.writeInt(
                startDayOffset,
                data,
                PdV25Structure.START_DAY_OFFSET_BYTE_INDEX,
                PdV25Structure.START_DAY_OFFSET_BIT_INDEX,
                PdV25Structure.START_DAY_OFFSET_BIT_LENGTH,
                ByteOrder.BIG_ENDIAN
        );

        boolean paymentType = pdV25.getPaymentType() == PdWithPaymentType.PAYMENT_TYPE_CARD;
        DataCarrierUtils.writeBoolean(
                paymentType,
                data,
                PdV25Structure.PAYMENT_TYPE_BYTE_INDEX,
                PdV25Structure.PAYMENT_TYPE_BIT_INDEX
        );

        Date saleDateTime = pdV25.getSaleDateTime();
        long saleDateTimeLong = DataCarrierUtils.dateToUnixTimestamp(saleDateTime);
        DataCarrierUtils.writeLong(
                saleDateTimeLong,
                data,
                PdV25Structure.SALE_DATE_TIME_BYTE_INDEX,
                PdV25Structure.SALE_DATE_TIME_BYTE_LENGTH,
                ByteOrder.LITTLE_ENDIAN
        );

        long tariffCode = pdV25.getTariffCode();
        DataCarrierUtils.writeLong(
                tariffCode,
                data,
                PdV25Structure.TARIFF_BYTE_INDEX,
                PdV25Structure.TARIFF_BYTE_LENGTH,
                ByteOrder.LITTLE_ENDIAN
        );

        int exemptionCode = pdV25.getExemptionCode();
        DataCarrierUtils.writeInt(
                exemptionCode,
                data,
                PdV25Structure.EXEMPTION_BYTE_INDEX,
                PdV25Structure.EXEMPTION_BYTE_LENGTH,
                ByteOrder.LITTLE_ENDIAN
        );

        int forDays = pdV25.getForDays();
        DataCarrierUtils.writeInt(
                forDays,
                data,
                PdV25Structure.FOR_DAYS_BYTE_INDEX,
                PdV25Structure.FOR_DAYS_BYTE_LENGTH,
                ByteOrder.LITTLE_ENDIAN
        );

        if (!withEdsKeyNumber) {
            return data;
        }

        long edsKeyNumber = pdV25.getEdsKeyNumber();
        DataCarrierUtils.writeLong(
                edsKeyNumber,
                data,
                PdV25Structure.EDS_KEY_NUMBER_BYTE_INDEX,
                PdV25Structure.EDS_KEY_NUMBER_BYTE_LENGTH,
                ByteOrder.LITTLE_ENDIAN
        );

        return data;
    }
}
