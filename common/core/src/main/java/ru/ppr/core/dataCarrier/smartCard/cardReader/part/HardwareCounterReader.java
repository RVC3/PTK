package ru.ppr.core.dataCarrier.smartCard.cardReader.part;

import android.support.annotation.NonNull;

import ru.ppr.core.dataCarrier.smartCard.cardReader.ReadCardResult;
import ru.ppr.core.dataCarrier.smartCard.cardReader.WriteCardResult;
import ru.ppr.core.dataCarrier.smartCard.cardReader.base.CardReader;

/**
 * Ридер, читающий и инкрементрирующий хардварный счетчик.
 *
 * @author Aleksandr Brazhkin
 */
public interface HardwareCounterReader extends CardReader {

    /**
     * Читает показания хардварного счетчика.
     *
     * @param counterIndex Номер счетчика
     * @return Результат чтения карты
     */
    @NonNull
    ReadCardResult<Integer> readHardwareCounter(int counterIndex);

    /**
     * Инкрементирует хардварный счетчик.
     *
     * @param counterIndex   Номер счетчика
     * @param incrementValue Значение, на которое увеличаются показания
     * @return Результат записи на карту
     */
    @NonNull
    WriteCardResult incrementHardwareCounter(int counterIndex, int incrementValue);
}
