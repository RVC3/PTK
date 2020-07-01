package ru.ppr.core.dataCarrier.pd.v20;

import android.support.annotation.NonNull;

import java.nio.ByteOrder;
import java.util.Date;

import ru.ppr.core.dataCarrier.DataCarrierUtils;
import ru.ppr.core.dataCarrier.pd.PdEncoder;
import ru.ppr.core.dataCarrier.pd.base.Pd;
import ru.ppr.core.dataCarrier.pd.base.PdWithPaymentType;

/**
 * Энкодер ПД v.20.
 *
 * @author Aleksandr Brazhkin
 */
public class PdV20Encoder implements PdEncoder {

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

        byte[] data = new byte[withEdsKeyNumber ? PdV20Structure.PD_SIZE : PdV20Structure.PD_DATA_SIZE];
        data[0] = (byte) pd.getVersion().getCode();

        PdV20 pdV20 = (PdV20) pd;

        int orderNumber = pdV20.getOrderNumber();
        DataCarrierUtils.writeInt(
                orderNumber,
                data,
                PdV20Structure.ORDER_NUMBER_BYTE_INDEX,
                PdV20Structure.ORDER_NUMBER_BIT_INDEX,
                PdV20Structure.ORDER_NUMBER_BIT_LENGTH,
                ByteOrder.LITTLE_ENDIAN
        );

        int startDayOffset = pdV20.getStartDayOffset();
        DataCarrierUtils.writeInt(
                startDayOffset,
                data,
                PdV20Structure.START_DAY_OFFSET_BYTE_INDEX,
                PdV20Structure.START_DAY_OFFSET_BIT_INDEX,
                PdV20Structure.START_DAY_OFFSET_BIT_LENGTH,
                ByteOrder.LITTLE_ENDIAN
        );

        boolean paymentType = pdV20.getPaymentType() == PdWithPaymentType.PAYMENT_TYPE_CARD;
        DataCarrierUtils.writeBoolean(
                paymentType,
                data,
                PdV20Structure.PAYMENT_TYPE_BYTE_INDEX,
                PdV20Structure.PAYMENT_TYPE_BIT_INDEX
        );

        Date saleDateTime = pdV20.getSaleDateTime();
        long saleDateTimeLong = DataCarrierUtils.dateToUnixTimestamp(saleDateTime);
        DataCarrierUtils.writeLong(
                saleDateTimeLong,
                data,
                PdV20Structure.SALE_DATE_TIME_BYTE_INDEX,
                PdV20Structure.SALE_DATE_TIME_BYTE_LENGTH,
                ByteOrder.LITTLE_ENDIAN
        );

        long tariffCode = pdV20.getTariffCode();
        DataCarrierUtils.writeLong(
                tariffCode,
                data,
                PdV20Structure.TARIFF_BYTE_INDEX,
                PdV20Structure.TARIFF_BYTE_LENGTH,
                ByteOrder.LITTLE_ENDIAN
        );

        if (!withEdsKeyNumber) {
            return data;
        }

        long edsKeyNumber = pdV20.getEdsKeyNumber();
        DataCarrierUtils.writeLong(
                edsKeyNumber,
                data,
                PdV20Structure.EDS_KEY_NUMBER_BYTE_INDEX,
                PdV20Structure.EDS_KEY_NUMBER_BYTE_LENGTH,
                ByteOrder.LITTLE_ENDIAN
        );

        return data;
    }
}
