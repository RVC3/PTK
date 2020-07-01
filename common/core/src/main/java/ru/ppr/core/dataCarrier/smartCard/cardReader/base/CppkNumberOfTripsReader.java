package ru.ppr.core.dataCarrier.smartCard.cardReader.base;

import android.support.annotation.NonNull;

import ru.ppr.core.dataCarrier.smartCard.cardReader.ReadCardResult;
import ru.ppr.core.dataCarrier.smartCard.cardReader.WriteCardResult;
import ru.ppr.core.dataCarrier.smartCard.cardReader.part.OuterNumberReader;
import ru.ppr.core.dataCarrier.smartCard.cardReader.part.ReadEdsReader;
import ru.ppr.core.dataCarrier.smartCard.cardReader.part.WithMaxPdCountReader;
import ru.ppr.core.dataCarrier.smartCard.cardReader.part.WriteEdsReader;
import ru.ppr.core.dataCarrier.smartCard.cardReader.part.passageMark.ReadPassageMarkReader;
import ru.ppr.core.dataCarrier.smartCard.cardReader.part.passageMark.WritePassageMarkReader;
import ru.ppr.core.dataCarrier.smartCard.cardReader.part.pd.ReadPdReader;
import ru.ppr.core.dataCarrier.smartCard.cardReader.part.pd.WritePdReader;

/**
 * Ридер карт ЦППК на количество поездок.
 *
 * @author Aleksandr Brazhkin
 */
public interface CppkNumberOfTripsReader extends MifareUltralightReader, CardReader,
        OuterNumberReader,
        ReadEdsReader,
        WriteEdsReader,
        ReadPdReader,
        WritePdReader,
        ReadPassageMarkReader,
        WritePassageMarkReader,
        WithMaxPdCountReader {

    /**
     * Читает показания хардварного счетчика для ПД.
     *
     * @param pdIndex Номер ассоциированного ПД
     * @return Результат чтения карты
     */
    @NonNull
    ReadCardResult<Integer> readHardwareCounter(int pdIndex);

    /**
     * Инкрементирует хардварный счетчик для ПД.
     *
     * @param pdIndex        Номер ассоциированного ПД
     * @param incrementValue Значение, на которое увеличаются показания
     * @return Результат записи на карту
     */
    @NonNull
    WriteCardResult incrementHardwareCounter(int pdIndex, int incrementValue);
}
