package ru.ppr.core.dataCarrier.smartCard.passageMark;

import android.support.annotation.NonNull;

import ru.ppr.core.dataCarrier.smartCard.passageMark.base.PassageMark;

/**
 * Энкодер метки прохода.
 *
 * @author Aleksandr Brazhkin
 */
public interface PassageMarkEncoder {
    /**
     * Кодирует метку прохода.
     *
     * @param passageMark Метка прохода
     * @return Данные метки прохода
     */
    @NonNull
    byte[] encode(PassageMark passageMark);
}
