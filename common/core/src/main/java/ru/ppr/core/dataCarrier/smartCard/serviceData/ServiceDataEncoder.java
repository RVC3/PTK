package ru.ppr.core.dataCarrier.smartCard.serviceData;

import android.support.annotation.NonNull;

import ru.ppr.core.dataCarrier.smartCard.serviceData.base.ServiceData;

/**
 * Энкодер служебных данных.
 *
 * @author Aleksandr Brazhkin
 */
public interface ServiceDataEncoder {
    /**
     * Кодирует служебные данные.
     *
     * @param serviceData Служебные данные
     * @return Данные служебных данных
     */
    @NonNull
    byte[] encode(ServiceData serviceData);
}
