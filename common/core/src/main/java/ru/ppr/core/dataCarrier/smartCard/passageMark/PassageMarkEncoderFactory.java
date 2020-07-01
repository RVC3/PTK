package ru.ppr.core.dataCarrier.smartCard.passageMark;

import android.support.annotation.NonNull;

import ru.ppr.core.dataCarrier.smartCard.passageMark.base.PassageMark;

/**
 * Фабрика энкодеров метки прохода.
 *
 * @author Aleksandr Brazhkin
 */
public interface PassageMarkEncoderFactory {
    /**
     * Создает энкодер для метки прохода.
     *
     * @param passageMark Метка прохода
     * @return Энкодер метки прохода
     */
    @NonNull
    PassageMarkEncoder create(PassageMark passageMark);
}
