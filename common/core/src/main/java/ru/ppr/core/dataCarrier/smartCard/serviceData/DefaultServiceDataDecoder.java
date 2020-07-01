package ru.ppr.core.dataCarrier.smartCard.serviceData;

import android.support.annotation.Nullable;

import ru.ppr.core.dataCarrier.smartCard.serviceData.base.ServiceData;

/**
 * Декодер служебных данных по умолчанию.
 * Следует использовать если не найден подходящий декодер для конкретных служебных данных.
 *
 * @author Aleksandr Brazhkin
 */
public class DefaultServiceDataDecoder implements ServiceDataDecoder {

    @Nullable
    @Override
    public ServiceData decode(byte[] data) {
        return null;
    }

}
