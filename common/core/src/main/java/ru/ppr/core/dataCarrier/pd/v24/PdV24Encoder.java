package ru.ppr.core.dataCarrier.pd.v24;

import android.support.annotation.NonNull;

import java.nio.ByteOrder;
import java.util.Date;

import ru.ppr.core.dataCarrier.DataCarrierUtils;
import ru.ppr.core.dataCarrier.pd.PdEncoder;
import ru.ppr.core.dataCarrier.pd.base.Pd;
import ru.ppr.core.dataCarrier.pd.base.PdWithPaymentType;

/**
 * Энкодер ПД v.24.
 *
 * @author Aleksandr Brazhkin
 */
public class PdV24Encoder implements PdEncoder {

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

        byte[] data = new byte[withEdsKeyNumber ? PdV24Structure.PD_SIZE : PdV24Structure.PD_DATA_SIZE];
        data[0] = (byte) pd.getVersion().getCode();

        PdV24 pdV24 = (PdV24) pd;

        int orderNumber = pdV24.getOrderNumber();
        DataCarrierUtils.writeInt(
                orderNumber,
                data,
                PdV24Structure.ORDER_NUMBER_BYTE_INDEX,
                PdV24Structure.ORDER_NUMBER_BIT_INDEX,
                PdV24Structure.ORDER_NUMBER_BIT_LENGTH,
                ByteOrder.LITTLE_ENDIAN
        );

        int startDayOffset = pdV24.getStartDayOffset();
        DataCarrierUtils.writeInt(
                startDayOffset,
                data,
                PdV24Structure.START_DAY_OFFSET_BYTE_INDEX,
                PdV24Structure.START_DAY_OFFSET_BIT_INDEX,
                PdV24Structure.START_DAY_OFFSET_BIT_LENGTH,
                ByteOrder.LITTLE_ENDIAN
        );

        boolean paymentType = pdV24.getPaymentType() == PdWithPaymentType.PAYMENT_TYPE_CARD;
        DataCarrierUtils.writeBoolean(
                paymentType,
                data,
                PdV24Structure.PAYMENT_TYPE_BYTE_INDEX,
                PdV24Structure.PAYMENT_TYPE_BIT_INDEX
        );

        Date saleDateTime = pdV24.getSaleDateTime();
        long saleDateTimeLong = DataCarrierUtils.dateToUnixTimestamp(saleDateTime);
        DataCarrierUtils.writeLong(
                saleDateTimeLong,
                data,
                PdV24Structure.SALE_DATE_TIME_BYTE_INDEX,
                PdV24Structure.SALE_DATE_TIME_BYTE_LENGTH,
                ByteOrder.LITTLE_ENDIAN
        );

        long tariffCode = pdV24.getTariffCode();
        DataCarrierUtils.writeLong(
                tariffCode,
                data,
                PdV24Structure.TARIFF_BYTE_INDEX,
                PdV24Structure.TARIFF_BYTE_LENGTH,
                ByteOrder.LITTLE_ENDIAN
        );

        if (!withEdsKeyNumber) {
            return data;
        }

        long edsKeyNumber = pdV24.getEdsKeyNumber();
        DataCarrierUtils.writeLong(
                edsKeyNumber,
                data,
                PdV24Structure.EDS_KEY_NUMBER_BYTE_INDEX,
                PdV24Structure.EDS_KEY_NUMBER_BYTE_LENGTH,
                ByteOrder.LITTLE_ENDIAN
        );

        return data;
    }
}
