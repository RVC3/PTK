package ru.ppr.core.dataCarrier.smartCard.serviceData;

import android.support.annotation.NonNull;

import javax.inject.Inject;

import ru.ppr.core.dataCarrier.smartCard.serviceData.v119.ServiceDataV119Decoder;
import ru.ppr.core.dataCarrier.smartCard.serviceData.v120.ServiceDataV120Decoder;
import ru.ppr.core.dataCarrier.smartCard.serviceData.v121.ServiceDataV121Decoder;

/**
 * Фабрика декодеров служебных данных, используемая по умолчанию.
 *
 * @author Aleksandr Brazhkin
 */
public class DefaultServiceDataDecoderFactory implements ServiceDataDecoderFactory {

    private static final int VERSION_INDEX = 0;

    @Inject
    DefaultServiceDataDecoderFactory(){

    }

    @NonNull
    @Override
    public ServiceDataDecoder create(byte[] data) {
        ServiceDataVersion version = ServiceDataVersion.getByCode(data[VERSION_INDEX]);

        if (version == null){
            return new DefaultServiceDataDecoder();
        }

        switch (version) {
            case V119:
                return new ServiceDataV119Decoder();
            case V120:
                return new ServiceDataV120Decoder();
            case V121:
                return new ServiceDataV121Decoder();
            default:
                return new DefaultServiceDataDecoder();
        }
    }

}
