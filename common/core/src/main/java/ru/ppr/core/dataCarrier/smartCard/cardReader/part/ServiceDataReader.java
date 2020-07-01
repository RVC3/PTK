package ru.ppr.core.dataCarrier.smartCard.cardReader.part;

import android.support.annotation.NonNull;

import ru.ppr.core.dataCarrier.smartCard.cardReader.ReadCardResult;
import ru.ppr.core.dataCarrier.smartCard.cardReader.base.CardReader;
import ru.ppr.core.dataCarrier.smartCard.serviceData.base.ServiceData;

/**
 * Ридер служебных данных.
 *
 * @author Aleksandr Brazhkin
 */
public interface ServiceDataReader extends CardReader {

    /**
     * Считывает служебные данные.
     *
     * @return Результат чтения карты
     */
    @NonNull
    ReadCardResult<ServiceData> readServiceData(byte sectorNumber, byte blockNumber);

    /**
     * Считывает служебные данные в сыром виде.
     *
     * @return Результат чтения карты
     */
    ReadCardResult<byte[]> readRawServiceData(byte sectorNumber, byte blockNumber);
}
