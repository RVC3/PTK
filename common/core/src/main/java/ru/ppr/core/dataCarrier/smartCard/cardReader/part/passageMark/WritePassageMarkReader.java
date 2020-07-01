package ru.ppr.core.dataCarrier.smartCard.cardReader.part.passageMark;

import android.support.annotation.NonNull;

import ru.ppr.core.dataCarrier.smartCard.cardReader.WriteCardResult;
import ru.ppr.core.dataCarrier.smartCard.cardReader.base.CardReader;
import ru.ppr.core.dataCarrier.smartCard.passageMark.base.PassageMark;

/**
 * Ридер, пишущий метку прохода.
 *
 * @author Aleksandr Brazhkin
 */
public interface WritePassageMarkReader extends CardReader {
    /**
     * Пишет метку прохода на карту.
     *
     * @param passageMark Метка прохода
     * @return Результат записи на карту
     */
    @NonNull
    WriteCardResult writePassageMark(PassageMark passageMark);
}
