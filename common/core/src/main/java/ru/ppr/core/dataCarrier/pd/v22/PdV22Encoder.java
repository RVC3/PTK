package ru.ppr.core.dataCarrier.pd.v22;

import android.support.annotation.NonNull;

import java.nio.ByteOrder;
import java.util.Date;

import ru.ppr.core.dataCarrier.DataCarrierUtils;
import ru.ppr.core.dataCarrier.pd.PdEncoder;
import ru.ppr.core.dataCarrier.pd.base.Pd;
import ru.ppr.core.dataCarrier.pd.base.PdWithDirection;
import ru.ppr.core.dataCarrier.pd.base.PdWithPaymentType;

/**
 * Энкодер ПД v.22.
 *
 * @author Aleksandr Brazhkin
 */
public class PdV22Encoder implements PdEncoder {

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
        byte[] data = new byte[withEdsKeyNumber ? PdV22Structure.PD_SIZE : PdV22Structure.PD_DATA_SIZE];
        data[0] = (byte) pd.getVersion().getCode();

        PdV22 pdV22 = (PdV22) pd;

        int orderNumber = pdV22.getOrderNumber();
        DataCarrierUtils.writeInt(
                orderNumber,
                data,
                PdV22Structure.ORDER_NUMBER_BYTE_INDEX,
                PdV22Structure.ORDER_NUMBER_BIT_INDEX,
                PdV22Structure.ORDER_NUMBER_BIT_LENGTH,
                ByteOrder.LITTLE_ENDIAN
        );


        DataCarrierUtils.writeInt(
                orderNumber >> 16 << 6,
                data,
                3,
                1,
                ByteOrder.LITTLE_ENDIAN
        );


        boolean direction = pdV22.getDirection() == PdWithDirection.DIRECTION_BACK;
        DataCarrierUtils.writeBoolean(
                direction,
                data,
                PdV22Structure.DIRECTION_BYTE_INDEX,
                PdV22Structure.DIRECTION_BIT_INDEX
        );

        boolean paymentType = pdV22.getPaymentType() == PdWithPaymentType.PAYMENT_TYPE_CARD;
        DataCarrierUtils.writeBoolean(
                paymentType,
                data,
                PdV22Structure.PAYMENT_TYPE_BYTE_INDEX,
                PdV22Structure.PAYMENT_TYPE_BIT_INDEX
        );

        boolean passageToStationCheckRequired = pdV22.isPassageToStationCheckRequired();
        DataCarrierUtils.writeBoolean(
                passageToStationCheckRequired,
                data,
                PdV22Structure.PASSAGE_TO_STATION_CHECK_REQUIRED_BYTE_INDEX,
                PdV22Structure.PASSAGE_TO_STATION_CHECK_REQUIRED_BIT_INDEX
        );

        boolean activationRequired = pdV22.isActivationRequired();
        DataCarrierUtils.writeBoolean(
                activationRequired,
                data,
                PdV22Structure.ACTIVATION_REQUIRED_BYTE_INDEX,
                PdV22Structure.ACTIVATION_REQUIRED_BIT_INDEX
        );

        long phoneNumber = pdV22.getPhoneNumber();
        DataCarrierUtils.writeLong(
                phoneNumber,
                data,
                PdV22Structure.PHONE_NUMBER_BYTE_INDEX,
                PdV22Structure.PHONE_NUMBER_BYTE_LENGTH,
                ByteOrder.LITTLE_ENDIAN
        );

        int startDayOffset = pdV22.getStartDayOffset();
        DataCarrierUtils.writeInt(
                startDayOffset,
                data,
                PdV22Structure.START_DAY_OFFSET_BYTE_INDEX,
                PdV22Structure.START_DAY_OFFSET_BYTE_LENGTH,
                ByteOrder.BIG_ENDIAN
        );

        int exemptionCode = pdV22.getExemptionCode();
        DataCarrierUtils.writeInt(
                exemptionCode,
                data,
                PdV22Structure.EXEMPTION_BYTE_INDEX,
                PdV22Structure.EXEMPTION_BYTE_LENGTH,
                ByteOrder.LITTLE_ENDIAN
        );

        Date saleDateTime = pdV22.getSaleDateTime();
        long saleDateTimeLong = DataCarrierUtils.dateToUnixTimestamp(saleDateTime);
        DataCarrierUtils.writeLong(
                saleDateTimeLong,
                data,
                PdV22Structure.SALE_DATE_TIME_BYTE_INDEX,
                PdV22Structure.SALE_DATE_TIME_BYTE_LENGTH,
                ByteOrder.LITTLE_ENDIAN
        );

        long tariffCode = pdV22.getTariffCode();
        DataCarrierUtils.writeLong(
                tariffCode,
                data,
                PdV22Structure.TARIFF_BYTE_INDEX,
                PdV22Structure.TARIFF_BYTE_LENGTH,
                ByteOrder.LITTLE_ENDIAN
        );

        if (!withEdsKeyNumber) {
            return data;
        }

        long edsKeyNumber = pdV22.getEdsKeyNumber();
        DataCarrierUtils.writeLong(
                edsKeyNumber,
                data,
                PdV22Structure.EDS_KEY_NUMBER_BYTE_INDEX,
                PdV22Structure.EDS_KEY_NUMBER_BYTE_LENGTH,
                ByteOrder.LITTLE_ENDIAN
        );

        byte[] eds = pdV22.getEds();
        DataCarrierUtils.writeBytes(eds, data, PdV22Structure.EDS_BYTE_INDEX);

        return data;
    }
}
