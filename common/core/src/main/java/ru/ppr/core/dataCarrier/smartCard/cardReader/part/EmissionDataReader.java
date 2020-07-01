package ru.ppr.core.dataCarrier.smartCard.cardReader.part;

import android.support.annotation.NonNull;

import ru.ppr.core.dataCarrier.smartCard.cardReader.ReadCardResult;
import ru.ppr.core.dataCarrier.smartCard.cardReader.base.CardReader;
import ru.ppr.core.dataCarrier.smartCard.entity.EmissionData;

/**
 * Ридер эмисионных данных.
 *
 * @author Aleksandr Brazhkin
 */
public interface EmissionDataReader extends CardReader {
    /**
     * Читает эмисионные данные.
     *
     * @return Результат чтения карты
     */
    @NonNull
    ReadCardResult<EmissionData> readEmissionData();
}
