package ru.ppr.core.dataCarrier.smartCard.cardReader.part.pd;

import android.support.annotation.NonNull;

import java.util.List;

import ru.ppr.core.dataCarrier.smartCard.cardReader.ReadCardResult;
import ru.ppr.core.dataCarrier.smartCard.cardReader.base.CardReader;
import ru.ppr.core.dataCarrier.pd.base.Pd;

/**
 * Ридер, читающий ПД со смарт-карт.
 *
 * @author Aleksandr Brazhkin
 */
public interface ReadPdReader extends CardReader {
    /**
     * Считывает список ПД с карты.
     *
     * @return Результат чтения карты
     */
    @NonNull
    ReadCardResult<List<Pd>> readPdList();
}
