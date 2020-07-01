package ru.ppr.core.dataCarrier.smartCard.coverageArea;

import android.support.annotation.Nullable;

import ru.ppr.core.dataCarrier.smartCard.coverageArea.base.CoverageArea;

/**
 * Декодер зоны действия.
 *
 * @author Aleksandr Brazhkin
 */
public interface CoverageAreaDecoder {
    /**
     * Декодирует зону действия.
     *
     * @param data Данные зоны действия
     * @return Зона действия
     */
    @Nullable
    CoverageArea decode(byte[] data);
}
