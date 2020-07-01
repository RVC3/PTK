package ru.ppr.core.dataCarrier.smartCard.cardReader.part.passageMark;

import ru.ppr.core.dataCarrier.smartCard.cardReader.ReadCardResult;
import ru.ppr.core.dataCarrier.smartCard.cardReader.base.CardReader;
import ru.ppr.core.dataCarrier.smartCard.passageMark.base.PassageMark;

/**
 * Ридер, читающий метку прохода со смарт-карт Mifare Classic.
 *
 * @author Aleksandr Brazhkin
 */
public interface ReadPassageMarkMifareClassicReader extends CardReader {
    /**
     * Считывает метку прохода с карты.
     *
     * @param sectorNumber Номер сектора
     * @param blockNumber  Номер блока
     * @return Результат чтения карты
     */
    ReadCardResult<PassageMark> readPassageMark(byte sectorNumber, byte blockNumber);
}
