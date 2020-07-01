package ru.ppr.core.dataCarrier.smartCard.serviceData;

import android.support.annotation.NonNull;

import ru.ppr.core.dataCarrier.smartCard.serviceData.base.ServiceData;

/**
 * Кодер служебных данных по умолчанию.
 * Следует использовать если не найден подходящий кодер для конкретных служебных данных.
 *
 * @author Aleksandr Brazhkin
 */
public class DefaultServiceDataEncoder implements ServiceDataEncoder {

    @NonNull
    @Override
    public byte[] encode(ServiceData serviceData) {
        return new byte[0];
    }

}
