package ru.ppr.core.dataCarrier.pd.v19;

import android.support.annotation.NonNull;

import java.nio.ByteOrder;
import java.util.Date;

import ru.ppr.core.dataCarrier.DataCarrierUtils;
import ru.ppr.core.dataCarrier.pd.PdEncoder;
import ru.ppr.core.dataCarrier.pd.base.Pd;
import ru.ppr.core.dataCarrier.pd.base.PdWithPaymentType;

/**
 * Энкодер ПД v.19.
 *
 * @author Aleksandr Brazhkin
 */
public class PdV19Encoder implements PdEncoder {

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

        byte[] data = new byte[withEdsKeyNumber ? PdV19Structure.PD_SIZE : PdV19Structure.PD_DATA_SIZE];
        data[0] = (byte) pd.getVersion().getCode();

        PdV19 pdV19 = (PdV19) pd;

        int orderNumber = pdV19.getOrderNumber();
        DataCarrierUtils.writeInt(
                orderNumber,
                data,
                PdV19Structure.ORDER_NUMBER_BYTE_INDEX,
                PdV19Structure.ORDER_NUMBER_BIT_INDEX,
                PdV19Structure.ORDER_NUMBER_BIT_LENGTH,
                ByteOrder.LITTLE_ENDIAN
        );

        int startDayOffset = pdV19.getStartDayOffset();
        DataCarrierUtils.writeInt(
                startDayOffset,
                data,
                PdV19Structure.START_DAY_OFFSET_BYTE_INDEX,
                PdV19Structure.START_DAY_OFFSET_BIT_INDEX,
                PdV19Structure.START_DAY_OFFSET_BIT_LENGTH,
                ByteOrder.LITTLE_ENDIAN
        );

        boolean paymentType = pdV19.getPaymentType() == PdWithPaymentType.PAYMENT_TYPE_CARD;
        DataCarrierUtils.writeBoolean(
                paymentType,
                data,
                PdV19Structure.PAYMENT_TYPE_BYTE_INDEX,
                PdV19Structure.PAYMENT_TYPE_BIT_INDEX
        );

        Date saleDateTime = pdV19.getSaleDateTime();
        long saleDateTimeLong = DataCarrierUtils.dateToUnixTimestamp(saleDateTime);
        DataCarrierUtils.writeLong(
                saleDateTimeLong,
                data,
                PdV19Structure.SALE_DATE_TIME_BYTE_INDEX,
                PdV19Structure.SALE_DATE_TIME_BYTE_LENGTH,
                ByteOrder.LITTLE_ENDIAN
        );

        long tariffCode = pdV19.getTariffCode();
        DataCarrierUtils.writeLong(
                tariffCode,
                data,
                PdV19Structure.TARIFF_BYTE_INDEX,
                PdV19Structure.TARIFF_BYTE_LENGTH,
                ByteOrder.LITTLE_ENDIAN
        );

        if (!withEdsKeyNumber) {
            return data;
        }

        long edsKeyNumber = pdV19.getEdsKeyNumber();
        DataCarrierUtils.writeLong(
                edsKeyNumber,
                data,
                PdV19Structure.EDS_KEY_NUMBER_BYTE_INDEX,
                PdV19Structure.EDS_KEY_NUMBER_BYTE_LENGTH,
                ByteOrder.LITTLE_ENDIAN
        );

        return data;
    }
}
