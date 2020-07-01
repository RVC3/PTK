package ru.ppr.core.dataCarrier.smartCard.serviceData;

import android.support.annotation.NonNull;

import ru.ppr.core.dataCarrier.smartCard.serviceData.base.ServiceData;

/**
 * Фабрика энкодеров служебных данных.
 *
 * @author Aleksandr Brazhkin
 */
public interface ServiceDataEncoderFactory {
    /**
     * Создает энкодер для служебных данных.
     *
     * @param serviceData Служебные данные
     * @return Энкодер служебных данных
     */
    @NonNull
    ServiceDataEncoder create(ServiceData serviceData);
}
