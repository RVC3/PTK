package ru.ppr.core.dataCarrier.smartCard.coverageArea;

import android.support.annotation.NonNull;

import java.util.List;

import ru.ppr.core.dataCarrier.smartCard.coverageArea.base.CoverageArea;

/**
 * Декодер списка зон действия.
 *
 * @author Aleksandr Brazhkin
 */
public interface CoverageAreaListDecoder {
    /**
     * Декодирует список зон действия.
     *
     * @param data Данные зон действия
     * @return Зона действия
     */
    @NonNull
    List<CoverageArea> decode(@NonNull byte[] data);
}
