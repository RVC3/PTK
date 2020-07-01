package ru.ppr.core.dataCarrier.smartCard.serviceData;

import android.support.annotation.NonNull;

import javax.inject.Inject;

import ru.ppr.core.dataCarrier.smartCard.serviceData.base.ServiceData;

/**
 * Фабрика кодеров служебных данных, используемая по умолчанию.
 *
 * @author Aleksandr Brazhkin
 */
public class DefaultServiceDataEncoderFactory implements ServiceDataEncoderFactory {

    @Inject
    DefaultServiceDataEncoderFactory(){

    }

    @NonNull
    @Override
    public ServiceDataEncoder create(ServiceData serviceData) {
        ServiceDataVersion version = serviceData.getVersion();
        switch (version) {
            case V119:
            case V120:
            case V121:
            default:
                return new DefaultServiceDataEncoder();
        }
    }

}
