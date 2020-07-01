package ru.ppr.core.dataCarrier.smartCard.cardReader.part.pd;

import android.support.annotation.NonNull;

import java.util.List;

import ru.ppr.core.dataCarrier.smartCard.cardReader.WriteCardResult;
import ru.ppr.core.dataCarrier.smartCard.cardReader.base.CardReader;
import ru.ppr.core.dataCarrier.pd.base.Pd;

/**
 * Ридер, пишущий ПД на смарт-карту.
 *
 * @author Aleksandr Brazhkin
 */
public interface WritePdReader extends CardReader {
    /**
     * Пишет список ПД на карту.
     *
     * @param pdList          Список ПД
     * @param forWriteIndexes Индексы ПД в списке, которые необходимо записать на карту.
     * @return Результат записи на карту
     */
    @NonNull
    WriteCardResult writePdList(List<Pd> pdList, boolean[] forWriteIndexes);
}
