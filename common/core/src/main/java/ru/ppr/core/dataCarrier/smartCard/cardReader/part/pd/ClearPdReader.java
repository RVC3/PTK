package ru.ppr.core.dataCarrier.smartCard.cardReader.part.pd;

import android.support.annotation.NonNull;

import ru.ppr.core.dataCarrier.smartCard.cardReader.WriteCardResult;
import ru.ppr.core.dataCarrier.smartCard.cardReader.base.CardReader;

/**
 * Ридер, стирающий ПД со смарт-карты.
 *
 * @author Grigoriy Kashka
 */
public interface ClearPdReader extends CardReader {
    /**
     * Затереть все билеты нулями.
     *
     * @return Результат записи на карту
     */
    @NonNull
    WriteCardResult clearPdList();
}
