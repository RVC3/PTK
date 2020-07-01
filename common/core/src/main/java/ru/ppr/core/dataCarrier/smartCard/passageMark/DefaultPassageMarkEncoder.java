package ru.ppr.core.dataCarrier.smartCard.passageMark;

import android.support.annotation.NonNull;

import ru.ppr.core.dataCarrier.smartCard.passageMark.base.PassageMark;

/**
 * Кодер метки прохода по умолчанию.
 * Следует использовать если не найден подходящий кодер для конкретной метки прохода.
 */
public class DefaultPassageMarkEncoder implements PassageMarkEncoder {

    @NonNull
    @Override
    public byte[] encode(PassageMark passageMark) {
        return new byte[0];
    }
}