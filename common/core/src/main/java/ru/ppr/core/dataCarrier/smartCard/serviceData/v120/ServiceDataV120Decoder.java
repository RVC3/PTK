package ru.ppr.core.dataCarrier.smartCard.serviceData.v120;

import android.support.annotation.Nullable;

import java.nio.ByteOrder;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import ru.ppr.core.dataCarrier.DataCarrierUtils;
import ru.ppr.core.dataCarrier.smartCard.serviceData.ServiceDataDecoder;
import ru.ppr.core.dataCarrier.smartCard.serviceData.base.ServiceData;
import ru.ppr.core.dataCarrier.smartCard.serviceData.base.ServiceDataWithFlags;

/**
 * Декодер служебных данных v.120.
 *
 * @author Aleksandr Brazhkin
 */
public class ServiceDataV120Decoder implements ServiceDataDecoder {

    @Nullable
    @Override
    public ServiceData decode(byte[] data) {

        if (data.length < ServiceDataV120Structure.SERVICE_DATA_SIZE)
            return null;

        byte mandatoryOfDocVerificationData = data[ServiceDataV120Structure.MANDATORY_OF_DOC_VERIFICATION_BYTE_INDEX];
        boolean mandatoryOfDocVerification = DataCarrierUtils.byteToBoolean(mandatoryOfDocVerificationData, ServiceDataV120Structure.MANDATORY_OF_DOC_VERIFICATION_BIT_INDEX);

        byte postExistingFlagData = data[ServiceDataV120Structure.POST_EXISTING_FLAG_BYTE_INDEX];
        boolean postExistingFlag = DataCarrierUtils.byteToBoolean(postExistingFlagData, ServiceDataV120Structure.POST_EXISTING_FLAG_BIT_INDEX);

        byte personalizedFlagData = data[ServiceDataV120Structure.PERSONALIZED_FLAG_BYTE_INDEX];
        boolean personalizedFlag = DataCarrierUtils.byteToBoolean(personalizedFlagData, ServiceDataV120Structure.PERSONALIZED_FLAG_BIT_INDEX);

        byte cardTypeData = data[ServiceDataV120Structure.CARD_TYPE_BYTE_INDEX];
        boolean cardType = DataCarrierUtils.byteToBoolean(cardTypeData, ServiceDataV120Structure.CARD_TYPE_BIT_INDEX);

        byte[] orderNumberData = DataCarrierUtils.subArray(data, ServiceDataV120Structure.ORDER_NUMBER_BYTE_INDEX, ServiceDataV120Structure.ORDER_NUMBER_BYTE_LENGTH);
        int orderNumber = DataCarrierUtils.bytesToInt(orderNumberData, ByteOrder.LITTLE_ENDIAN);

        byte[] initDateData = DataCarrierUtils.subArray(data, ServiceDataV120Structure.INIT_DATE_BYTE_INDEX, ServiceDataV120Structure.INIT_DATE_BYTE_LENGTH);
        long initDateLong = DataCarrierUtils.bytesToLong(initDateData, ByteOrder.LITTLE_ENDIAN);
        Date initDate = new Date(TimeUnit.SECONDS.toMillis(initDateLong));

        byte[] validityTimeData = DataCarrierUtils.subArray(data, ServiceDataV120Structure.VALIDITY_TIME_BYTE_INDEX, ServiceDataV120Structure.VALIDITY_TIME_BYTE_LENGTH);
        int validityTime = DataCarrierUtils.bytesToInt(validityTimeData, ByteOrder.LITTLE_ENDIAN);

        byte[] serviceDataEdsKeyNumberData = DataCarrierUtils.subArray(data, ServiceDataV120Structure.EDS_KEY_NUMBER_BYTE_INDEX, ServiceDataV120Structure.EDS_KEY_NUMBER_BYTE_LENGTH);
        long edsKeyNumber = DataCarrierUtils.bytesToLong(serviceDataEdsKeyNumberData, ByteOrder.LITTLE_ENDIAN);

        ServiceDataV120Impl serviceDataV120 = new ServiceDataV120Impl();
        serviceDataV120.setMandatoryOfDocVerification(mandatoryOfDocVerification ?
                ServiceDataWithFlags.MandatoryOfDocVerification.REQUIRED : ServiceDataWithFlags.MandatoryOfDocVerification.NOT_REQUIRED);
        serviceDataV120.setPostExistingFlag(postExistingFlag ?
                ServiceDataWithFlags.PostExistingFlag.EXISTS : ServiceDataWithFlags.PostExistingFlag.NOT_EXISTS);
        serviceDataV120.setPersonalizedFlag(personalizedFlag ?
                ServiceDataWithFlags.PersonalizedFlag.PERSONALIZED : ServiceDataWithFlags.PersonalizedFlag.NOT_PERSONALIZED);
        serviceDataV120.setCardType(cardType ?
                ServiceDataWithFlags.CardType.TRIP : ServiceDataWithFlags.CardType.TURNSTILE);
        serviceDataV120.setOrderNumber(orderNumber);
        serviceDataV120.setInitDateTime(initDate);
        serviceDataV120.setValidityTime(validityTime);
        serviceDataV120.setEdsKeyNumber(edsKeyNumber);

        return serviceDataV120;
    }

}
