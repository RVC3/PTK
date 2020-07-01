package ru.ppr.core.dataCarrier.smartCard.cardReader.part;

import android.support.annotation.NonNull;

import ru.ppr.core.dataCarrier.smartCard.cardReader.ReadCardResult;
import ru.ppr.core.dataCarrier.smartCard.cardReader.base.CardReader;

/**
 * Ридер, читающий ЭЦП с карты.
 *
 * @author Aleksandr Brazhkin
 */
public interface ReadEdsReader extends CardReader {
    /**
     * Считывает ЭЦП.
     *
     * @return Результат чтения карты
     */
    @NonNull
    ReadCardResult<byte[]> readEds();
}
