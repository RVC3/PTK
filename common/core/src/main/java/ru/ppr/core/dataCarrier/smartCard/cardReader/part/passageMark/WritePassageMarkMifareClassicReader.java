package ru.ppr.core.dataCarrier.smartCard.cardReader.part.passageMark;

import ru.ppr.core.dataCarrier.smartCard.cardReader.WriteCardResult;
import ru.ppr.core.dataCarrier.smartCard.cardReader.base.CardReader;
import ru.ppr.core.dataCarrier.smartCard.passageMark.base.PassageMark;

/**
 * Ридер, пишущий метку прохода на смарт-карту Mifare Classic.
 *
 * @author Aleksandr Brazhkin
 */
public interface WritePassageMarkMifareClassicReader extends CardReader {
    /**
     * Пишет метку прохода на карту.
     *
     * @param passageMark  Метка прохода
     * @param sectorNumber Номер сектора
     * @param blockNumber  Номер блока
     * @return Результат записи на карту
     */
    WriteCardResult writePassageMark(PassageMark passageMark, byte sectorNumber, byte blockNumber);
}
