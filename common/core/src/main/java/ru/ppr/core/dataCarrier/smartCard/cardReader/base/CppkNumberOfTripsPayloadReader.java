package ru.ppr.core.dataCarrier.smartCard.cardReader.base;

import android.support.annotation.NonNull;

import ru.ppr.core.dataCarrier.smartCard.cardReader.ReadCardResult;
import ru.ppr.core.dataCarrier.smartCard.cardReader.part.OuterNumberReader;

/**
 * Ридер карт ЦППК на количество поездок.
 *
 * @author Aleksandr Brazhkin
 */
public interface CppkNumberOfTripsPayloadReader extends MifareUltralightReader, CardReader, OuterNumberReader {

    /**
     * Считывает первый байт с ПД.
     *
     * @return Результат чтения карты
     */
    @NonNull
    ReadCardResult<byte[]> readFirstPayloadBytes();
}
