package ru.ppr.core.dataCarrier.smartCard.coverageArea;

import android.support.annotation.Nullable;

import ru.ppr.core.dataCarrier.smartCard.coverageArea.base.CoverageArea;

/**
 * Декодер зоны действия по умолчанию.
 * Следует использовать если не найден подходящий декодер для конкретной зоны действия.
 * @author Aleksandr Brazhkin
 */
public class DefaultCoverageAreaDecoder implements CoverageAreaDecoder {
    @Nullable
    @Override
    public CoverageArea decode(byte[] data) {
        return null;
    }
}
