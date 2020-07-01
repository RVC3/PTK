package ru.ppr.core.dataCarrier.smartCard.passageMark;

import android.support.annotation.NonNull;

/**
 * Фабрика декодеров метки прохода.
 *
 * @author Aleksandr Brazhkin
 */
public interface PassageMarkDecoderFactory {
    /**
     * Создает декодер для метки прохода.
     *
     * @param data Данные метки прохода
     * @return Декодер метки прохода
     */
    @NonNull
    PassageMarkDecoder create(byte[] data);
}
