package ru.ppr.core.dataCarrier.smartCard.coverageArea;

import android.support.annotation.NonNull;

/**
 * Фабрика декодеров списка зон действия.
 *
 * @author Aleksandr Brazhkin
 */
public interface CoverageAreaListDecoderFactory {
    /**
     * Создает декодер для списка зон действия.
     *
     * @return Декодер списка зон действия
     */
    @NonNull
    CoverageAreaListDecoder create();
}
