package ru.ppr.core.dataCarrier.smartCard.serviceData.v121;

import android.support.annotation.Nullable;

import java.nio.ByteOrder;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import ru.ppr.core.dataCarrier.DataCarrierUtils;
import ru.ppr.core.dataCarrier.smartCard.serviceData.ServiceDataDecoder;
import ru.ppr.core.dataCarrier.smartCard.serviceData.base.ServiceData;
import ru.ppr.core.dataCarrier.smartCard.serviceData.base.ServiceDataWithFlags;

/**
 * Декодер служебных данных v.121.
 *
 * @author Aleksandr Brazhkin
 */
public class ServiceDataV121Decoder implements ServiceDataDecoder {

    @Nullable
    @Override
    public ServiceData decode(byte[] data) {

        if (data.length < ServiceDataV121Structure.SERVICE_DATA_SIZE)
            return null;

        byte mandatoryOfDocVerificationData = data[ServiceDataV121Structure.MANDATORY_OF_DOC_VERIFICATION_BYTE_INDEX];
        boolean mandatoryOfDocVerification = DataCarrierUtils.byteToBoolean(mandatoryOfDocVerificationData, ServiceDataV121Structure.MANDATORY_OF_DOC_VERIFICATION_BIT_INDEX);

        byte postExistingFlagData = data[ServiceDataV121Structure.POST_EXISTING_FLAG_BYTE_INDEX];
        boolean postExistingFlag = DataCarrierUtils.byteToBoolean(postExistingFlagData, ServiceDataV121Structure.POST_EXISTING_FLAG_BIT_INDEX);

        byte personalizedFlagData = data[ServiceDataV121Structure.PERSONALIZED_FLAG_BYTE_INDEX];
        boolean personalizedFlag = DataCarrierUtils.byteToBoolean(personalizedFlagData, ServiceDataV121Structure.PERSONALIZED_FLAG_BIT_INDEX);

        byte cardTypeData = data[ServiceDataV121Structure.CARD_TYPE_BYTE_INDEX];
        boolean cardType = DataCarrierUtils.byteToBoolean(cardTypeData, ServiceDataV121Structure.CARD_TYPE_BIT_INDEX);

        byte[] orderNumberData = DataCarrierUtils.subArray(data, ServiceDataV121Structure.ORDER_NUMBER_BYTE_INDEX, ServiceDataV121Structure.ORDER_NUMBER_BYTE_LENGTH);
        int orderNumber = DataCarrierUtils.bytesToInt(orderNumberData, ByteOrder.LITTLE_ENDIAN);

        byte[] initDateData = DataCarrierUtils.subArray(data, ServiceDataV121Structure.INIT_DATE_BYTE_INDEX, ServiceDataV121Structure.INIT_DATE_BYTE_LENGTH);
        long initDateLong = DataCarrierUtils.bytesToLong(initDateData, ByteOrder.LITTLE_ENDIAN);
        Date initDate = new Date(TimeUnit.SECONDS.toMillis(initDateLong));

        byte[] validityTimeData = DataCarrierUtils.subArray(data, ServiceDataV121Structure.VALIDITY_TIME_BYTE_INDEX, ServiceDataV121Structure.VALIDITY_TIME_BYTE_LENGTH);
        int validityTime = DataCarrierUtils.bytesToInt(validityTimeData, ByteOrder.LITTLE_ENDIAN);

        byte[] serviceDataEdsKeyNumberData = DataCarrierUtils.subArray(data, ServiceDataV121Structure.EDS_KEY_NUMBER_BYTE_INDEX, ServiceDataV121Structure.EDS_KEY_NUMBER_BYTE_LENGTH);
        long edsKeyNumber = DataCarrierUtils.bytesToLong(serviceDataEdsKeyNumberData, ByteOrder.LITTLE_ENDIAN);

        ServiceDataV121Impl serviceDataV121 = new ServiceDataV121Impl();
        serviceDataV121.setMandatoryOfDocVerification(mandatoryOfDocVerification ?
                ServiceDataWithFlags.MandatoryOfDocVerification.REQUIRED : ServiceDataWithFlags.MandatoryOfDocVerification.NOT_REQUIRED);
        serviceDataV121.setPostExistingFlag(postExistingFlag ?
                ServiceDataWithFlags.PostExistingFlag.EXISTS : ServiceDataWithFlags.PostExistingFlag.NOT_EXISTS);
        serviceDataV121.setPersonalizedFlag(personalizedFlag ?
                ServiceDataWithFlags.PersonalizedFlag.PERSONALIZED : ServiceDataWithFlags.PersonalizedFlag.NOT_PERSONALIZED);
        serviceDataV121.setCardType(cardType ?
                ServiceDataWithFlags.CardType.TRIP : ServiceDataWithFlags.CardType.TURNSTILE);
        serviceDataV121.setOrderNumber(orderNumber);
        serviceDataV121.setInitDateTime(initDate);
        serviceDataV121.setValidityTime(validityTime);
        serviceDataV121.setEdsKeyNumber(edsKeyNumber);

        return serviceDataV121;
    }

}
