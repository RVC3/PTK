package ru.ppr.core.dataCarrier.smartCard.cardReader.part;

import android.support.annotation.NonNull;

import ru.ppr.core.dataCarrier.smartCard.cardReader.ReadCardResult;
import ru.ppr.core.dataCarrier.smartCard.cardReader.base.CardReader;
import ru.ppr.core.dataCarrier.smartCard.entity.BscInformation;

/**
 * Ридер информации о БСК.
 *
 * @author Aleksandr Brazhkin
 */
public interface BscInformationReader extends CardReader {
    /**
     * Считывает информацию о БСК.
     *
     * @return Результат чтения карты
     */
    @NonNull
    ReadCardResult<BscInformation> readBscInformation();
}
