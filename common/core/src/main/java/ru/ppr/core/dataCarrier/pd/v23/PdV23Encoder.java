package ru.ppr.core.dataCarrier.pd.v23;

import android.support.annotation.NonNull;

import java.nio.ByteOrder;
import java.util.Date;

import ru.ppr.core.dataCarrier.DataCarrierUtils;
import ru.ppr.core.dataCarrier.pd.PdEncoder;
import ru.ppr.core.dataCarrier.pd.base.Pd;
import ru.ppr.core.dataCarrier.pd.base.PdWithPaymentType;

/**
 * Энкодер ПД v.23.
 *
 * @author Aleksandr Brazhkin
 */
public class PdV23Encoder implements PdEncoder {

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

        byte[] data = new byte[withEdsKeyNumber ? PdV23Structure.PD_SIZE : PdV23Structure.PD_DATA_SIZE];
        data[0] = (byte) pd.getVersion().getCode();

        PdV23 pdV23 = (PdV23) pd;

        int orderNumber = pdV23.getOrderNumber();
        DataCarrierUtils.writeInt(
                orderNumber,
                data,
                PdV23Structure.ORDER_NUMBER_BYTE_INDEX,
                PdV23Structure.ORDER_NUMBER_BIT_INDEX,
                PdV23Structure.ORDER_NUMBER_BIT_LENGTH,
                ByteOrder.LITTLE_ENDIAN
        );

        int startDayOffset = pdV23.getStartDayOffset();
        DataCarrierUtils.writeInt(
                startDayOffset,
                data,
                PdV23Structure.START_DAY_OFFSET_BYTE_INDEX,
                PdV23Structure.START_DAY_OFFSET_BIT_INDEX,
                PdV23Structure.START_DAY_OFFSET_BIT_LENGTH,
                ByteOrder.LITTLE_ENDIAN
        );

        boolean paymentType = pdV23.getPaymentType() == PdWithPaymentType.PAYMENT_TYPE_CARD;
        DataCarrierUtils.writeBoolean(
                paymentType,
                data,
                PdV23Structure.PAYMENT_TYPE_BYTE_INDEX,
                PdV23Structure.PAYMENT_TYPE_BIT_INDEX
        );

        Date saleDateTime = pdV23.getSaleDateTime();
        long saleDateTimeLong = DataCarrierUtils.dateToUnixTimestamp(saleDateTime);
        DataCarrierUtils.writeLong(
                saleDateTimeLong,
                data,
                PdV23Structure.SALE_DATE_TIME_BYTE_INDEX,
                PdV23Structure.SALE_DATE_TIME_BYTE_LENGTH,
                ByteOrder.LITTLE_ENDIAN
        );

        long tariffCode = pdV23.getTariffCode();
        DataCarrierUtils.writeLong(
                tariffCode,
                data,
                PdV23Structure.TARIFF_BYTE_INDEX,
                PdV23Structure.TARIFF_BYTE_LENGTH,
                ByteOrder.LITTLE_ENDIAN
        );

        if (!withEdsKeyNumber) {
            return data;
        }

        long edsKeyNumber = pdV23.getEdsKeyNumber();
        DataCarrierUtils.writeLong(
                edsKeyNumber,
                data,
                PdV23Structure.EDS_KEY_NUMBER_BYTE_INDEX,
                PdV23Structure.EDS_KEY_NUMBER_BYTE_LENGTH,
                ByteOrder.LITTLE_ENDIAN
        );

        return data;
    }
}
