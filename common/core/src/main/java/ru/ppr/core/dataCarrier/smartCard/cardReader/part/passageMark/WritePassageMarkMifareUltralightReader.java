package ru.ppr.core.dataCarrier.smartCard.cardReader.part.passageMark;

import ru.ppr.core.dataCarrier.smartCard.cardReader.WriteCardResult;
import ru.ppr.core.dataCarrier.smartCard.cardReader.base.CardReader;
import ru.ppr.core.dataCarrier.smartCard.passageMark.base.PassageMark;

/**
 * Ридер, пишущий метку прохода на смарт-карту Mifare Ultralight.
 *
 * @author Aleksandr Brazhkin
 */
public interface WritePassageMarkMifareUltralightReader extends CardReader {
    /**
     * Пишет метку прохода на карту.
     *
     * @param passageMark Метка прохода
     * @param pageNumber  Номер страницы
     * @return Результат записи на карту
     */
    WriteCardResult writePassageMark(PassageMark passageMark, byte pageNumber);
}
