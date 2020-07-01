package ru.ppr.core.dataCarrier.pd.v6;

import android.support.annotation.NonNull;

import java.nio.ByteOrder;
import java.util.Date;

import ru.ppr.core.dataCarrier.DataCarrierUtils;
import ru.ppr.core.dataCarrier.pd.PdEncoder;
import ru.ppr.core.dataCarrier.pd.base.Pd;
import ru.ppr.core.dataCarrier.pd.base.PdWithPaymentType;

/**
 * Энкодер ПД v.6.
 *
 * @author Aleksandr Brazhkin
 */
public class PdV6Encoder implements PdEncoder {

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

        byte[] data = new byte[withEdsKeyNumber ? PdV6Structure.PD_SIZE : PdV6Structure.PD_DATA_SIZE];
        data[0] = (byte) pd.getVersion().getCode();

        PdV6 pdV6 = (PdV6) pd;

        int orderNumber = pdV6.getOrderNumber();
        DataCarrierUtils.writeInt(
                orderNumber,
                data,
                PdV6Structure.ORDER_NUMBER_BYTE_INDEX,
                PdV6Structure.ORDER_NUMBER_BIT_INDEX,
                PdV6Structure.ORDER_NUMBER_BIT_LENGTH,
                ByteOrder.LITTLE_ENDIAN
        );

        int startDayOffset = pdV6.getStartDayOffset();
        DataCarrierUtils.writeInt(
                startDayOffset,
                data,
                PdV6Structure.START_DAY_OFFSET_BYTE_INDEX,
                PdV6Structure.START_DAY_OFFSET_BIT_INDEX,
                PdV6Structure.START_DAY_OFFSET_BIT_LENGTH,
                ByteOrder.BIG_ENDIAN
        );

        boolean paymentType = pdV6.getPaymentType() == PdWithPaymentType.PAYMENT_TYPE_CARD;
        DataCarrierUtils.writeBoolean(
                paymentType,
                data,
                PdV6Structure.PAYMENT_TYPE_BYTE_INDEX,
                PdV6Structure.PAYMENT_TYPE_BIT_INDEX
        );

        Date saleDateTime = pdV6.getSaleDateTime();
        long saleDateTimeLong = DataCarrierUtils.dateToUnixTimestamp(saleDateTime);
        DataCarrierUtils.writeLong(
                saleDateTimeLong,
                data,
                PdV6Structure.SALE_DATE_TIME_BYTE_INDEX,
                PdV6Structure.SALE_DATE_TIME_BYTE_LENGTH,
                ByteOrder.LITTLE_ENDIAN
        );

        long tariffCode = pdV6.getTariffCode();
        DataCarrierUtils.writeLong(
                tariffCode,
                data,
                PdV6Structure.TARIFF_BYTE_INDEX,
                PdV6Structure.TARIFF_BYTE_LENGTH,
                ByteOrder.LITTLE_ENDIAN
        );

        int exemptionCode = pdV6.getExemptionCode();
        DataCarrierUtils.writeInt(
                exemptionCode,
                data,
                PdV6Structure.EXEMPTION_BYTE_INDEX,
                PdV6Structure.EXEMPTION_BYTE_LENGTH,
                ByteOrder.LITTLE_ENDIAN
        );

        int forDays = pdV6.getForDays();
        DataCarrierUtils.writeInt(
                forDays,
                data,
                PdV6Structure.FOR_DAYS_BYTE_INDEX,
                PdV6Structure.FOR_DAYS_BYTE_LENGTH,
                ByteOrder.LITTLE_ENDIAN
        );

        if (!withEdsKeyNumber) {
            return data;
        }

        long edsKeyNumber = pdV6.getEdsKeyNumber();
        DataCarrierUtils.writeLong(
                edsKeyNumber,
                data,
                PdV6Structure.EDS_KEY_NUMBER_BYTE_INDEX,
                PdV6Structure.EDS_KEY_NUMBER_BYTE_LENGTH,
                ByteOrder.LITTLE_ENDIAN
        );

        return data;
    }
}
