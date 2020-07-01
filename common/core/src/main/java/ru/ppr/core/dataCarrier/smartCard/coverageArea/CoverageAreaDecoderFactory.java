package ru.ppr.core.dataCarrier.smartCard.coverageArea;

import android.support.annotation.NonNull;

/**
 * Фабрика декодеров зоны действия.
 *
 * @author Aleksandr Brazhkin
 */
public interface CoverageAreaDecoderFactory {
    /**
     * Создает декодер для зоны действия.
     *
     * @param data Данные зоны действия
     * @return Декодер зоны действия
     */
    @NonNull
    CoverageAreaDecoder create(byte[] data);
}
