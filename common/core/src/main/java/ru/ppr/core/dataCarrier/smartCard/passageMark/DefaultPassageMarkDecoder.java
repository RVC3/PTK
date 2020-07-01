package ru.ppr.core.dataCarrier.smartCard.passageMark;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import ru.ppr.core.dataCarrier.smartCard.passageMark.base.PassageMark;

/**
 * Декодер метки прохода по умолчанию.
 * Следует использовать если не найден подходящий декодер для конкретной метки прохода.
 */
public class DefaultPassageMarkDecoder implements PassageMarkDecoder {

    @Nullable
    @Override
    public PassageMark decode(@NonNull byte[] data) {

        return null;
    }

}