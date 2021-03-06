package ru.ppr.core.dataCarrier.smartCard.cardReader.part.pd;

import ru.ppr.core.dataCarrier.smartCard.cardReader.WriteCardResult;
import ru.ppr.core.dataCarrier.smartCard.cardReader.base.CardReader;
import ru.ppr.core.dataCarrier.pd.base.Pd;

/**
 * Ридер, пишущий ПД на смарт-карту Mifare Ultralight.
 *
 * @author Aleksandr Brazhkin
 */
public interface WritePdMifareUltralightReader extends CardReader {
    /**
     * Пишет ПД на карту.
     *
     * @param pd              ПД
     * @param startPageNumber Номер стартовой страницы
     * @return Результат записи на карту
     */
    WriteCardResult writePd(Pd pd, byte startPageNumber);
}
