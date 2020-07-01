package ru.ppr.core.dataCarrier.smartCard.serviceData.v119;

import android.support.annotation.Nullable;

import java.nio.ByteOrder;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import ru.ppr.core.dataCarrier.DataCarrierUtils;
import ru.ppr.core.dataCarrier.smartCard.serviceData.ServiceDataDecoder;
import ru.ppr.core.dataCarrier.smartCard.serviceData.base.ServiceData;

/**
 * Декодер служебных данных v.119.
 *
 * @author Aleksandr Brazhkin
 */
public class ServiceDataV119Decoder implements ServiceDataDecoder {

    @Nullable
    @Override
    public ServiceData decode(byte[] data) {

        if (data.length < ServiceDataV119Structure.SERVICE_DATA_SIZE)
            return null;

        byte[] serviceDataEdsKeyNumberData = DataCarrierUtils.subArray(data, ServiceDataV119Structure.EDS_KEY_NUMBER_INDEX, ServiceDataV119Structure.EDS_KEY_NUMBER_LENGTH);
        long edsKeyNumber = DataCarrierUtils.bytesToLong(serviceDataEdsKeyNumberData, ByteOrder.LITTLE_ENDIAN);

        byte[] initDateData = DataCarrierUtils.subArray(data, ServiceDataV119Structure.INIT_DATE_INDEX, ServiceDataV119Structure.INIT_DATE_LENGTH);
        long initDateLong = DataCarrierUtils.bytesToLong(initDateData, ByteOrder.LITTLE_ENDIAN);
        Date initDate = new Date(TimeUnit.SECONDS.toMillis(initDateLong));

        ServiceDataV119Impl serviceDataV119 = new ServiceDataV119Impl();
        serviceDataV119.setEdsKeyNumber(edsKeyNumber);
        serviceDataV119.setInitDateTime(initDate);

        return serviceDataV119;
    }

}
