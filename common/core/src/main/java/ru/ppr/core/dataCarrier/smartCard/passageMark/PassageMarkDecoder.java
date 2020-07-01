package ru.ppr.core.dataCarrier.smartCard.passageMark;

import android.support.annotation.Nullable;

import ru.ppr.core.dataCarrier.smartCard.passageMark.base.PassageMark;

/**
 * Декодер метки прохода.
 *
 * @author Aleksandr Brazhkin
 */
public interface PassageMarkDecoder {
    /**
     * Декодирует метку прохода.
     *
     * @param data Данные метки прохода
     * @return Метка прохода
     */
    @Nullable
    PassageMark decode(byte[] data);
}
