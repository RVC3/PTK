package ru.ppr.core.dataCarrier.pd.v22;

import android.support.annotation.NonNull;

import java.nio.ByteOrder;
import java.util.Date;

import ru.ppr.core.dataCarrier.DataCarrierUtils;
import ru.ppr.core.dataCarrier.pd.PdDecoder;
import ru.ppr.core.dataCarrier.pd.base.PdWithDirection;
import ru.ppr.core.dataCarrier.pd.base.PdWithPaymentType;

/**
 * Декодер ПД v.22.
 *
 * @author Aleksandr Brazhkin
 */
public class PdV22Decoder implements PdDecoder {

    @Override
    public PdV22 decode(@NonNull byte[] data) {
        if (data.length < PdV22Structure.PD_SIZE)
            return null;

        byte[] orderNumberData = DataCarrierUtils.subArray(data, PdV22Structure.ORDER_NUMBER_BYTE_INDEX, PdV22Structure.ORDER_NUMBER_BYTE_LENGTH);
        int orderNumber = DataCarrierUtils.bytesToInt(orderNumberData, PdV22Structure.ORDER_NUMBER_BIT_INDEX, PdV22Structure.ORDER_NUMBER_BIT_LENGTH, ByteOrder.LITTLE_ENDIAN);

        byte directionData = data[PdV22Structure.DIRECTION_BYTE_INDEX];
        boolean direction = DataCarrierUtils.byteToBoolean(directionData, PdV22Structure.DIRECTION_BIT_INDEX);

        byte paymentTypeData = data[PdV22Structure.PAYMENT_TYPE_BYTE_INDEX];
        boolean paymentType = DataCarrierUtils.byteToBoolean(paymentTypeData, PdV22Structure.PAYMENT_TYPE_BIT_INDEX);

        byte passageToStationCheckRequiredData = data[PdV22Structure.PASSAGE_TO_STATION_CHECK_REQUIRED_BYTE_INDEX];
        boolean passageToStationCheckRequired = DataCarrierUtils.byteToBoolean(passageToStationCheckRequiredData, PdV22Structure.PASSAGE_TO_STATION_CHECK_REQUIRED_BIT_INDEX);

        byte activationRequiredData = data[PdV22Structure.ACTIVATION_REQUIRED_BYTE_INDEX];
        boolean activationRequired = DataCarrierUtils.byteToBoolean(activationRequiredData, PdV22Structure.ACTIVATION_REQUIRED_BIT_INDEX);

        byte[] phoneNumberData = DataCarrierUtils.subArray(data, PdV22Structure.PHONE_NUMBER_BYTE_INDEX, PdV22Structure.PHONE_NUMBER_BYTE_LENGTH);
        long phoneNumber = DataCarrierUtils.bytesToLong(phoneNumberData, ByteOrder.LITTLE_ENDIAN);

        byte[] startDayOffsetData = DataCarrierUtils.subArray(data, PdV22Structure.START_DAY_OFFSET_BYTE_INDEX, PdV22Structure.START_DAY_OFFSET_BYTE_LENGTH);
        int startDayOffset = DataCarrierUtils.bytesToInt(startDayOffsetData, ByteOrder.BIG_ENDIAN);

        byte[] exemptionCodeData = DataCarrierUtils.subArray(data, PdV22Structure.EXEMPTION_BYTE_INDEX, PdV22Structure.EXEMPTION_BYTE_LENGTH);
        int exemptionCode = DataCarrierUtils.bytesToInt(exemptionCodeData, ByteOrder.LITTLE_ENDIAN);

        byte[] saleDateTimeData = DataCarrierUtils.subArray(data, PdV22Structure.SALE_DATE_TIME_BYTE_INDEX, PdV22Structure.SALE_DATE_TIME_BYTE_LENGTH);
        long saleDateTimeLong = DataCarrierUtils.bytesToLong(saleDateTimeData, ByteOrder.LITTLE_ENDIAN);
        Date saleDateTime = DataCarrierUtils.unixTimestampToDate(saleDateTimeLong);

        byte[] tariffCodeData = DataCarrierUtils.subArray(data, PdV22Structure.TARIFF_BYTE_INDEX, PdV22Structure.TARIFF_BYTE_LENGTH);
        long tariffCode = DataCarrierUtils.bytesToLong(tariffCodeData, ByteOrder.LITTLE_ENDIAN);

        byte[] edsKeyNumberData = DataCarrierUtils.subArray(data, PdV22Structure.EDS_KEY_NUMBER_BYTE_INDEX, PdV22Structure.EDS_KEY_NUMBER_BYTE_LENGTH);
        long edsKeyNumber = DataCarrierUtils.bytesToLong(edsKeyNumberData, ByteOrder.LITTLE_ENDIAN);

        byte[] eds = DataCarrierUtils.subArray(data, PdV22Structure.EDS_BYTE_INDEX, PdV22Structure.EDS_BYTE_LENGTH);

        PdV22Impl pdV22 = new PdV22Impl();
        pdV22.setOrderNumber(orderNumber);
        pdV22.setDirection(direction ? PdWithDirection.DIRECTION_BACK : PdWithDirection.DIRECTION_THERE);
        pdV22.setPaymentType(paymentType ? PdWithPaymentType.PAYMENT_TYPE_CARD : PdWithPaymentType.PAYMENT_TYPE_CASH);
        pdV22.setPassageToStationCheckRequired(passageToStationCheckRequired);
        pdV22.setActivationRequired(activationRequired);
        pdV22.setPhoneNumber(phoneNumber);
        pdV22.setStartDayOffset(startDayOffset);
        pdV22.setExemptionCode(exemptionCode);
        pdV22.setSaleDateTime(saleDateTime);
        pdV22.setTariffCode(tariffCode);
        pdV22.setEdsKeyNumber(edsKeyNumber);
        pdV22.setEds(eds);

        return pdV22;
    }

}
