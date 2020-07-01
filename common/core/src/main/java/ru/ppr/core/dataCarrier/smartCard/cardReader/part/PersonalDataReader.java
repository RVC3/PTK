package ru.ppr.core.dataCarrier.smartCard.cardReader.part;

import android.support.annotation.NonNull;

import ru.ppr.core.dataCarrier.smartCard.cardReader.ReadCardResult;
import ru.ppr.core.dataCarrier.smartCard.cardReader.base.CardReader;
import ru.ppr.core.dataCarrier.smartCard.entity.PersonalData;

/**
 * Ридер песональных данных.
 *
 * @author Aleksandr Brazhkin
 */
public interface PersonalDataReader extends CardReader {
    /**
     * Считывает персональыне данные.
     *
     * @return Результат чтения карты
     */
    @NonNull
    ReadCardResult<PersonalData> readPersonalData();
}
