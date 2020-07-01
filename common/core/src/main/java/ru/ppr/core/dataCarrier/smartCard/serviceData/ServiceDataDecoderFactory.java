package ru.ppr.core.dataCarrier.smartCard.serviceData;

import android.support.annotation.NonNull;

/**
 * Фабрика декодеров служебных данных.
 *
 * @author Aleksandr Brazhkin
 */
public interface ServiceDataDecoderFactory {
    /**
     * Создает декодер для служебных данных.
     *
     * @param data Данные служебных данных
     * @return Декодер служебных данных
     */
    @NonNull
    ServiceDataDecoder create(byte[] data);
}
