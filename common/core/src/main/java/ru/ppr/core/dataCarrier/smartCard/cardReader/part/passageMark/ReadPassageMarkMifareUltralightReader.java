package ru.ppr.core.dataCarrier.smartCard.cardReader.part.passageMark;

import ru.ppr.core.dataCarrier.smartCard.cardReader.ReadCardResult;
import ru.ppr.core.dataCarrier.smartCard.cardReader.base.CardReader;
import ru.ppr.core.dataCarrier.smartCard.passageMark.base.PassageMark;

/**
 * Ридер, читающий метку прохода со смарт-карт Mifare Ultralight.
 *
 * @author Aleksandr Brazhkin
 */
public interface ReadPassageMarkMifareUltralightReader extends CardReader {
    /**
     * Считывает метку прохода с карты.
     *
     * @param startPageNumber Номер стартовой страницы.
     * @param startByteNumber Номер стартового байта на странице.
     * @return Результат чтения карты
     */
    ReadCardResult<PassageMark> readPassageMark(byte startPageNumber, byte startByteNumber);
}
