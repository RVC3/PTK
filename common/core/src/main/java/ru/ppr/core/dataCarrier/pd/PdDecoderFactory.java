package ru.ppr.core.dataCarrier.pd;

import android.support.annotation.NonNull;

/**
 * Фабрика декодеров ПД.
 *
 * @author Aleksandr Brazhkin
 */
public interface PdDecoderFactory {
    /**
     * Создает декодер для ПД.
     *
     * @param data Данные ПД
     * @return Декодер ПД
     */
    @NonNull
    PdDecoder create(@NonNull byte[] data);
}
