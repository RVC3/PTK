package ru.ppr.core.dataCarrier.smartCard.cardReader.part;

import android.support.annotation.NonNull;

import ru.ppr.core.dataCarrier.smartCard.cardReader.ReadCardResult;
import ru.ppr.core.dataCarrier.smartCard.cardReader.base.CardReader;
import ru.ppr.core.dataCarrier.smartCard.entity.outerNumber.OuterNumber;

/**
 * Ридер внешнего номера карты.
 *
 * @author Aleksandr Brazhkin
 */
public interface OuterNumberReader extends CardReader {
    /**
     * Считывает внешний номер карты.
     *
     * @return Результат чтения карты
     */
    @NonNull
    ReadCardResult<OuterNumber> readOuterNumber();
}
