package ru.ppr.core.dataCarrier.smartCard.cardReader.part.pd;

import java.util.List;

import ru.ppr.core.dataCarrier.smartCard.cardReader.ReadCardResult;
import ru.ppr.core.dataCarrier.smartCard.cardReader.base.CardReader;
import ru.ppr.core.dataCarrier.pd.base.Pd;

/**
 * Ридер, читающий ПД со смарт-карт Mifare Classic.
 *
 * @author Aleksandr Brazhkin
 */
public interface ReadPdMifareClassicReader extends CardReader {
    /**
     * Считывает ПД с карты.
     *
     * @param startSectorNumber Номер стартового сектора
     * @param startBlockNumber  Номер стартового блока
     * @param blockCount        Количество считываемых блоков
     * @param pdCount           Количество ПД на карте по схеме данных
     * @return Результат чтения карты
     */
    ReadCardResult<List<Pd>> readPdList(byte startSectorNumber, byte startBlockNumber, byte blockCount, int pdCount);
}
