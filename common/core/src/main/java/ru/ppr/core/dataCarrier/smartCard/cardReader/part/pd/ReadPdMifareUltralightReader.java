package ru.ppr.core.dataCarrier.smartCard.cardReader.part.pd;

import java.util.List;

import ru.ppr.core.dataCarrier.smartCard.cardReader.ReadCardResult;
import ru.ppr.core.dataCarrier.smartCard.cardReader.base.CardReader;
import ru.ppr.core.dataCarrier.pd.base.Pd;

/**
 * Ридер, читающий ПД со смарт-карт Mifare Ultralight.
 *
 * @author Aleksandr Brazhkin
 */
public interface ReadPdMifareUltralightReader extends CardReader {
    /**
     * Считывает ПД с карты.
     *
     * @param startPageNumber Номер стартовой страницы
     * @param startByteNumber Номер стартового блока на странице
     * @param byteCount       Количество считываемых байт
     * @param pdCount         Количество ПД на карте по схеме данных
     * @return Результат чтения карты
     */
    ReadCardResult<List<Pd>> readPdList(byte startPageNumber, byte startByteNumber, byte byteCount, int pdCount);
}
