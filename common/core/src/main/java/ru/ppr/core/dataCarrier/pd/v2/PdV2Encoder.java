package ru.ppr.core.dataCarrier.pd.v2;

import android.support.annotation.NonNull;

import java.nio.ByteOrder;
import java.util.Date;

import ru.ppr.core.dataCarrier.DataCarrierUtils;
import ru.ppr.core.dataCarrier.pd.PdEncoder;
import ru.ppr.core.dataCarrier.pd.base.Pd;
import ru.ppr.core.dataCarrier.pd.base.PdWithDirection;
import ru.ppr.core.dataCarrier.pd.base.PdWithPaymentType;

/**
 * Энкодер ПД v.2.
 *
 * @author Aleksandr Brazhkin
 */
public class PdV2Encoder implements PdEncoder {

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

        byte[] data = new byte[withEdsKeyNumber ? PdV2Structure.PD_SIZE : PdV2Structure.PD_DATA_SIZE];
        data[0] = (byte) pd.getVersion().getCode();

        PdV2 pdV2 = (PdV2) pd;

        int orderNumber = pdV2.getOrderNumber();
        DataCarrierUtils.writeInt(
                orderNumber,
                data,
                PdV2Structure.ORDER_NUMBER_BYTE_INDEX,
                PdV2Structure.ORDER_NUMBER_BIT_INDEX,
                PdV2Structure.ORDER_NUMBER_BIT_LENGTH,
                ByteOrder.LITTLE_ENDIAN
        );

        int startDayOffset = pdV2.getStartDayOffset();
        DataCarrierUtils.writeInt(
                startDayOffset,
                data,
                PdV2Structure.START_DAY_OFFSET_BYTE_INDEX,
                PdV2Structure.START_DAY_OFFSET_BIT_INDEX,
                PdV2Structure.START_DAY_OFFSET_BIT_LENGTH,
                ByteOrder.BIG_ENDIAN
        );

        boolean direction = pdV2.getDirection() == PdWithDirection.DIRECTION_BACK;
        DataCarrierUtils.writeBoolean(
                direction,
                data,
                PdV2Structure.DIRECTION_BYTE_INDEX,
                PdV2Structure.DIRECTION_BIT_INDEX
        );

        boolean paymentType = pdV2.getPaymentType() == PdWithPaymentType.PAYMENT_TYPE_CARD;
        DataCarrierUtils.writeBoolean(
                paymentType,
                data,
                PdV2Structure.PAYMENT_TYPE_BYTE_INDEX,
                PdV2Structure.PAYMENT_TYPE_BIT_INDEX
        );

        int sourceOrderNumber = pdV2.getSourceOrderNumber();
        DataCarrierUtils.writeInt(
                sourceOrderNumber,
                data,
                PdV2Structure.SOURCE_ORDER_NUMBER_BYTE_INDEX,
                PdV2Structure.SOURCE_ORDER_NUMBER_BIT_INDEX,
                PdV2Structure.SOURCE_ORDER_NUMBER_BIT_LENGTH,
                ByteOrder.LITTLE_ENDIAN
        );

        Date saleDateTime = pdV2.getSaleDateTime();
        long saleDateTimeLong = DataCarrierUtils.dateToUnixTimestamp(saleDateTime);
        DataCarrierUtils.writeLong(
                saleDateTimeLong,
                data,
                PdV2Structure.SALE_DATE_TIME_BYTE_INDEX,
                PdV2Structure.SALE_DATE_TIME_BYTE_LENGTH,
                ByteOrder.LITTLE_ENDIAN
        );

        Date sourceSaleDateTime = pdV2.getSourceSaleDateTime();
        long sourceSaleDateTimeLong = DataCarrierUtils.dateToUnixTimestamp(sourceSaleDateTime);
        DataCarrierUtils.writeLong(
                sourceSaleDateTimeLong,
                data,
                PdV2Structure.SOURCE_SALE_DATE_TIME_BYTE_INDEX,
                PdV2Structure.SOURCE_SALE_DATE_TIME_BYTE_LENGTH,
                ByteOrder.LITTLE_ENDIAN
        );

        long tariffCode = pdV2.getTariffCode();
        DataCarrierUtils.writeLong(
                tariffCode,
                data,
                PdV2Structure.TARIFF_BYTE_INDEX,
                PdV2Structure.TARIFF_BYTE_LENGTH,
                ByteOrder.LITTLE_ENDIAN
        );

        long sourcePdDeviceId = pdV2.getSourceDeviceId();
        DataCarrierUtils.writeLong(
                sourcePdDeviceId,
                data,
                PdV2Structure.SOURCE_PD_DEVICE_ID_BYTE_INDEX,
                PdV2Structure.SOURCE_PD_DEVICE_ID_BYTE_LENGTH,
                ByteOrder.LITTLE_ENDIAN
        );

        if (!withEdsKeyNumber) {
            return data;
        }

        long edsKeyNumber = pdV2.getEdsKeyNumber();
        DataCarrierUtils.writeLong(
                edsKeyNumber,
                data,
                PdV2Structure.EDS_KEY_NUMBER_BYTE_INDEX,
                PdV2Structure.EDS_KEY_NUMBER_BYTE_LENGTH,
                ByteOrder.LITTLE_ENDIAN
        );

        byte[] eds = pdV2.getEds();
        DataCarrierUtils.writeBytes(eds, data, PdV2Structure.EDS_BYTE_INDEX);

        return data;
    }
}
