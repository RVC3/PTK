package ru.ppr.core.dataCarrier.pd.v18;

import android.support.annotation.NonNull;

import java.nio.ByteOrder;
import java.util.Date;

import ru.ppr.core.dataCarrier.DataCarrierUtils;
import ru.ppr.core.dataCarrier.pd.PdEncoder;
import ru.ppr.core.dataCarrier.pd.base.Pd;
import ru.ppr.core.dataCarrier.pd.base.PdWithPaymentType;

/**
 * Энкодер ПД v.18.
 *
 * @author Aleksandr Brazhkin
 */
public class PdV18Encoder implements PdEncoder {

    @NonNull
    @Override
    public byte[] encode(@NonNull Pd pd) {
        return internalEncode(pd, true, true);
    }

    @NonNull
    @Override
    public byte[] encodeWithoutEdsKeyNumber(@NonNull Pd pd) {
        return internalEncode(pd, false, false);
    }

    /**
     * Кодирует ПД.
     *
     * @param pd      ПД
     * @param withCrc {@code true}, если нужно кодировать CRC, {@code false} - иначе
     * @return Данные ПД
     */
    @NonNull
    public byte[] encode(@NonNull Pd pd, boolean withCrc) {
        return internalEncode(pd, true, withCrc);
    }

    private byte[] internalEncode(@NonNull Pd pd, boolean withEdsKeyNumber, boolean withCrc) {

        int size = PdV18Structure.PD_DATA_SIZE;
        if (withEdsKeyNumber) size = size + PdV18Structure.EDS_KEY_NUMBER_BYTE_LENGTH;
        if (withCrc) size = size + PdV18Structure.CRC_BYTE_LENGTH;

        byte[] data = new byte[size];
        data[0] = (byte) pd.getVersion().getCode();

        PdV18 pdV18 = (PdV18) pd;

        int orderNumber = pdV18.getOrderNumber();
        DataCarrierUtils.writeInt(
                orderNumber,
                data,
                PdV18Structure.ORDER_NUMBER_BYTE_INDEX,
                PdV18Structure.ORDER_NUMBER_BIT_INDEX,
                PdV18Structure.ORDER_NUMBER_BIT_LENGTH,
                ByteOrder.LITTLE_ENDIAN
        );

        int startDayOffset = pdV18.getStartDayOffset();
        DataCarrierUtils.writeInt(
                startDayOffset,
                data,
                PdV18Structure.START_DAY_OFFSET_BYTE_INDEX,
                PdV18Structure.START_DAY_OFFSET_BIT_INDEX,
                PdV18Structure.START_DAY_OFFSET_BIT_LENGTH,
                ByteOrder.BIG_ENDIAN
        );

        boolean paymentType = pdV18.getPaymentType() == PdWithPaymentType.PAYMENT_TYPE_CARD;
        DataCarrierUtils.writeBoolean(
                paymentType,
                data,
                PdV18Structure.PAYMENT_TYPE_BYTE_INDEX,
                PdV18Structure.PAYMENT_TYPE_BIT_INDEX
        );

        Date saleDateTime = pdV18.getSaleDateTime();
        long saleDateTimeLong = DataCarrierUtils.dateToUnixTimestamp(saleDateTime);
        DataCarrierUtils.writeLong(
                saleDateTimeLong,
                data,
                PdV18Structure.SALE_DATE_TIME_BYTE_INDEX,
                PdV18Structure.SALE_DATE_TIME_BYTE_LENGTH,
                ByteOrder.LITTLE_ENDIAN
        );

        long tariffCode = pdV18.getTariffCode();
        DataCarrierUtils.writeLong(
                tariffCode,
                data,
                PdV18Structure.TARIFF_BYTE_INDEX,
                PdV18Structure.TARIFF_BYTE_LENGTH,
                ByteOrder.LITTLE_ENDIAN
        );

        int startCounterValue = pdV18.getStartCounterValue();
        DataCarrierUtils.writeInt(
                startCounterValue,
                data,
                PdV18Structure.START_COUNTER_VALUE_BYTE_INDEX,
                PdV18Structure.START_COUNTER_VALUE_BYTE_LENGTH,
                ByteOrder.LITTLE_ENDIAN
        );

        int endCounterValue = pdV18.getEndCounterValue();
        DataCarrierUtils.writeInt(
                endCounterValue,
                data,
                PdV18Structure.END_COUNTER_VALUE_BYTE_INDEX,
                PdV18Structure.END_COUNTER_VALUE_BYTE_LENGTH,
                ByteOrder.LITTLE_ENDIAN
        );

        if (!withEdsKeyNumber) {
            return data;
        }

        long edsKeyNumber = pdV18.getEdsKeyNumber();
        DataCarrierUtils.writeLong(
                edsKeyNumber,
                data,
                PdV18Structure.EDS_KEY_NUMBER_BYTE_INDEX,
                PdV18Structure.EDS_KEY_NUMBER_BYTE_LENGTH,
                ByteOrder.LITTLE_ENDIAN
        );

        if (!withCrc) {
            return data;
        }

        byte[] crc = pdV18.getCrc();
        DataCarrierUtils.writeBytes(crc, data, PdV18Structure.CRC_BYTE_INDEX);

        return data;
    }
}
