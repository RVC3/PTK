package ru.ppr.core.dataCarrier.smartCard.cardReader.part.pd;

import ru.ppr.core.dataCarrier.smartCard.cardReader.WriteCardResult;
import ru.ppr.core.dataCarrier.smartCard.cardReader.base.CardReader;
import ru.ppr.core.dataCarrier.pd.base.Pd;

/**
 * Ридер, пишущий ПД на смарт-карту Mifare Classic.
 *
 * @author Aleksandr Brazhkin
 */
public interface WritePdMifareClassicReader extends CardReader {
    /**
     * Пишет ПД на карту.
     *
     * @param pd                ПД
     * @param startSectorNumber Номер стартового сектора
     * @param startBlockNumber  Номер стартового блока
     * @return Результат записи на карту
     */
    WriteCardResult writePd(Pd pd, byte startSectorNumber, byte startBlockNumber);
}
