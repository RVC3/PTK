package ru.ppr.core.dataCarrier.smartCard.cardReader.part.passageMark;

import android.support.annotation.NonNull;

import ru.ppr.core.dataCarrier.smartCard.cardReader.ReadCardResult;
import ru.ppr.core.dataCarrier.smartCard.cardReader.base.CardReader;
import ru.ppr.core.dataCarrier.smartCard.passageMark.base.PassageMark;

/**
 * Ридер, читающий метку прохода.
 *
 * @author Aleksandr Brazhkin
 */
public interface ReadPassageMarkReader extends CardReader {
    /**
     * Считывает метку прохода с карты
     *
     * @return Результат чтения карты
     */
    @NonNull
    ReadCardResult<PassageMark> readPassageMark();
}
