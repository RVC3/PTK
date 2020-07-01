package ru.ppr.core.dataCarrier.smartCard.cardReader.part;

import android.support.annotation.NonNull;

import java.util.List;

import ru.ppr.core.dataCarrier.smartCard.cardReader.ReadCardResult;
import ru.ppr.core.dataCarrier.smartCard.cardReader.base.CardReader;
import ru.ppr.core.dataCarrier.smartCard.coverageArea.base.CoverageArea;

/**
 * Ридер зон действия БСК.
 *
 * @author Aleksandr Brazhkin
 */
public interface CoverageAreaReader extends CardReader {

    /**
     * Считывает зоны действия БСК.
     *
     * @param startSectorNumber Номер стартового сектора
     * @param startBlockNumber  Номер стартового блока
     * @param blockCount        Количество считываемых блоков
     * @return Результат чтения карты
     */
    @NonNull
    ReadCardResult<List<CoverageArea>> readCoverageAreaList(byte startSectorNumber, byte startBlockNumber, byte blockCount);

    /**
     * Считывает зоны действия БСК в сыром виде.
     *
     * @param startSectorNumber Номер стартового сектора
     * @param startBlockNumber  Номер стартового блока
     * @param blockCount        Количество считываемых блоков
     * @return Результат чтения карты
     */
    @NonNull
    ReadCardResult<byte[]> readRawCoverageAreaList(byte startSectorNumber, byte startBlockNumber, byte blockCount);
}
