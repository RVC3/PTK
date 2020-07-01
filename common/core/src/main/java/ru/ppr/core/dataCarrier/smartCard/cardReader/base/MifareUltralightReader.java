package ru.ppr.core.dataCarrier.smartCard.cardReader.base;

import android.support.annotation.NonNull;

import ru.ppr.core.dataCarrier.smartCard.cardReader.ReadCardResult;
import ru.ppr.core.dataCarrier.smartCard.cardReader.WriteCardResult;
import ru.ppr.core.dataCarrier.smartCard.cardReader.part.BscTypeReader;

/**
 * Ридер смарт-карт Mifare Ultralight.
 *
 * @author Aleksandr Brazhkin
 */
public interface MifareUltralightReader extends CardReader, BscTypeReader {

    /**
     * Считывает байты с карты.
     *
     * @param startPageNumber Номер стартовой страницы
     * @param startByteNumber Номер стартового байта на странице
     * @param byteCount       Количество считываемых байт
     * @return Результат чтения карты
     */
    @NonNull
    ReadCardResult<byte[]> readBytes(byte startPageNumber, byte startByteNumber, byte byteCount);

    /**
     * Считывает байты, содержащие тип карты.
     *
     * @return Результат чтения карты
     */
    @NonNull
    ReadCardResult<byte[]> readBytesWithCardType();

    /**
     * Пишет данные на карту.
     *
     * @param data            Данные
     * @param startPageNumber Номер стартовой страницы
     * @return Результат записи на карту
     */
    @NonNull
    WriteCardResult writeBytes(byte data[], byte startPageNumber);
}
