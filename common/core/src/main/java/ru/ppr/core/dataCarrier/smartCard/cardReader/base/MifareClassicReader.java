package ru.ppr.core.dataCarrier.smartCard.cardReader.base;

import android.support.annotation.NonNull;

import ru.ppr.core.dataCarrier.smartCard.cardReader.ReadCardResult;
import ru.ppr.core.dataCarrier.smartCard.cardReader.WriteCardResult;
import ru.ppr.core.dataCarrier.smartCard.cardReader.part.BscTypeReader;

/**
 * Ридер смарт-карт Mifare Classic.
 *
 * @author Aleksandr Brazhkin
 */
public interface MifareClassicReader extends CardReader, BscTypeReader {

    /**
     * Считывает блок данных.
     *
     * @param sectorNumber Номер сектора
     * @param blockNumber  Номер блока
     * @return Результат чтения карты
     */
    @NonNull
    ReadCardResult<byte[]> readBlock(int sectorNumber, int blockNumber);

    /**
     * Считывает несколько подряд идущих блоков данных.
     *
     * @param startSectorNumber Номер сектора с первым блоком
     * @param startBlockNumber  Номер первого блока
     * @param blockCount        Количество считываемых блоков
     * @return Результат чтения карты
     */
    @NonNull
    ReadCardResult<byte[]> readBlocks(int startSectorNumber, int startBlockNumber, int blockCount);

    /**
     * Считывает блок, содержащий тип карты.
     *
     * @return Результат чтения карты
     */
    @NonNull
    ReadCardResult<byte[]> readBlockWithCardType();

    /**
     * Пишет данные в блок на карте.
     *
     * @param data         Данные
     * @param sectorNumber Номер сектора
     * @param blockNumber  Номер блока
     * @return Результат записи на карту
     */
    @NonNull
    WriteCardResult writeBlock(byte[] data, int sectorNumber, int blockNumber);

    /**
     * Пишет данные в несколько подряд идущих блоков на карте.
     *
     * @param data              Данные
     * @param startSectorNumber Номер сектора с первым блоком
     * @param startBlockNumber  Номер первого блока
     * @return Результат записи на карту
     */
    @NonNull
    WriteCardResult writeBlocks(byte[] data, int startSectorNumber, int startBlockNumber);
}
