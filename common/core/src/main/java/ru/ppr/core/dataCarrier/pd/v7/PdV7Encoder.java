package ru.ppr.core.dataCarrier.pd.v7;

import android.support.annotation.NonNull;

import java.nio.ByteOrder;
import java.util.Date;

import ru.ppr.core.dataCarrier.DataCarrierUtils;
import ru.ppr.core.dataCarrier.pd.PdEncoder;
import ru.ppr.core.dataCarrier.pd.base.Pd;
import ru.ppr.core.dataCarrier.pd.base.PdWithPaymentType;

/**
 * Энкодер ПД v.7.
 *
 * @author Aleksandr Brazhkin
 */
public class PdV7Encoder implements PdEncoder {

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

        int size = PdV7Structure.PD_DATA_SIZE;
        if (withEdsKeyNumber) size = size + PdV7Structure.EDS_KEY_NUMBER_BYTE_LENGTH;
        if (withCrc) size = size + PdV7Structure.CRC_BYTE_LENGTH;

        byte[] data = new byte[size];
        data[0] = (byte) pd.getVersion().getCode();

        PdV7 pdV7 = (PdV7) pd;

        int orderNumber = pdV7.getOrderNumber();
        DataCarrierUtils.writeInt(
                orderNumber,
                data,
                PdV7Structure.ORDER_NUMBER_BYTE_INDEX,
                PdV7Structure.ORDER_NUMBER_BIT_INDEX,
                PdV7Structure.ORDER_NUMBER_BIT_LENGTH,
                ByteOrder.LITTLE_ENDIAN
        );

        int startDayOffset = pdV7.getStartDayOffset();
        DataCarrierUtils.writeInt(
                startDayOffset,
                data,
                PdV7Structure.START_DAY_OFFSET_BYTE_INDEX,
                PdV7Structure.START_DAY_OFFSET_BIT_INDEX,
                PdV7Structure.START_DAY_OFFSET_BIT_LENGTH,
                ByteOrder.BIG_ENDIAN
        );

        boolean paymentType = pdV7.getPaymentType() == PdWithPaymentType.PAYMENT_TYPE_CARD;
        DataCarrierUtils.writeBoolean(
                paymentType,
                data,
                PdV7Structure.PAYMENT_TYPE_BYTE_INDEX,
                PdV7Structure.PAYMENT_TYPE_BIT_INDEX
        );

        Date saleDateTime = pdV7.getSaleDateTime();
        long saleDateTimeLong = DataCarrierUtils.dateToUnixTimestamp(saleDateTime);
        DataCarrierUtils.writeLong(
                saleDateTimeLong,
                data,
                PdV7Structure.SALE_DATE_TIME_BYTE_INDEX,
                PdV7Structure.SALE_DATE_TIME_BYTE_LENGTH,
                ByteOrder.LITTLE_ENDIAN
        );

        long tariffCode = pdV7.getTariffCode();
        DataCarrierUtils.writeLong(
                tariffCode,
                data,
                PdV7Structure.TARIFF_BYTE_INDEX,
                PdV7Structure.TARIFF_BYTE_LENGTH,
                ByteOrder.LITTLE_ENDIAN
        );

        int startCounterValue = pdV7.getStartCounterValue();
        DataCarrierUtils.writeInt(
                startCounterValue,
                data,
                PdV7Structure.START_COUNTER_VALUE_BYTE_INDEX,
                PdV7Structure.START_COUNTER_VALUE_BYTE_LENGTH,
                ByteOrder.LITTLE_ENDIAN
        );

        int endCounterValue = pdV7.getEndCounterValue();
        DataCarrierUtils.writeInt(
                endCounterValue,
                data,
                PdV7Structure.END_COUNTER_VALUE_BYTE_INDEX,
                PdV7Structure.END_COUNTER_VALUE_BYTE_LENGTH,
                ByteOrder.LITTLE_ENDIAN
        );

        if (!withEdsKeyNumber) {
            return data;
        }

        long edsKeyNumber = pdV7.getEdsKeyNumber();
        DataCarrierUtils.writeLong(
                edsKeyNumber,
                data,
                PdV7Structure.EDS_KEY_NUMBER_BYTE_INDEX,
                PdV7Structure.EDS_KEY_NUMBER_BYTE_LENGTH,
                ByteOrder.LITTLE_ENDIAN
        );

        if (!withCrc) {
            return data;
        }

        byte[] crc = pdV7.getCrc();
        DataCarrierUtils.writeBytes(crc, data, PdV7Structure.CRC_BYTE_INDEX);

        return data;
    }
}
