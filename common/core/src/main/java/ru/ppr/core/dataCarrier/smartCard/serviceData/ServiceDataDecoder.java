package ru.ppr.core.dataCarrier.smartCard.serviceData;

import android.support.annotation.Nullable;

import ru.ppr.core.dataCarrier.smartCard.serviceData.base.ServiceData;

/**
 * Декодер служебных данных.
 *
 * @author Aleksandr Brazhkin
 */
public interface ServiceDataDecoder {
    /**
     * Декодирует служебные данные.
     *
     * @param data Данные служебных данных
     * @return Служебные данные
     */
    @Nullable
    ServiceData decode(byte[] data);
}
