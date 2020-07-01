package ru.ppr.core.dataCarrier.smartCard.cardReader.part;

import android.support.annotation.NonNull;

import ru.ppr.core.dataCarrier.smartCard.cardReader.WriteCardResult;
import ru.ppr.core.dataCarrier.smartCard.cardReader.base.CardReader;

/**
 * Ридер, пишущий ЭЦП на карту.
 *
 * @author Aleksandr Brazhkin
 */
public interface WriteEdsReader extends CardReader {

    /**
     * Пишет ЭЦП на карту.
     *
     * @param eds ЭЦП
     * @return Результат записи на карту
     */
    @NonNull
    WriteCardResult writeEds(byte[] eds);
}
