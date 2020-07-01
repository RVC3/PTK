package ru.ppr.core.dataCarrier.smartCard.cardReader.part;

import android.support.annotation.NonNull;

import java.util.List;

import ru.ppr.core.dataCarrier.smartCard.cardReader.ReadCardResult;
import ru.ppr.core.dataCarrier.smartCard.cardReader.base.CardReader;
import ru.ppr.core.dataCarrier.smartCard.cardReader.base.MifareClassicReader;
import ru.ppr.core.dataCarrier.smartCard.coverageArea.base.CoverageArea;
import ru.ppr.core.dataCarrier.smartCard.serviceData.base.ServiceData;

/**
 * Ридер служебных карт.
 *
 * @author Aleksandr Brazhkin
 */
public interface ServiceCardReader extends CardReader,
        MifareClassicReader,
        OuterNumberReader,
        ReadEdsReader {

    /**
     * Считывает служебные данные в сыром формате.
     *
     * @return Результат чтения карты
     */
    @NonNull
    ReadCardResult<byte[]> readRawServiceData();

    /**
     * Считывает служебные данные.
     *
     * @return Результат чтения карты
     */
    @NonNull
    ReadCardResult<ServiceData> readServiceData();

    /**
     * Считывает список зон в сыром формате.
     *
     * @return Результат чтения карты
     */
    @NonNull
    ReadCardResult<byte[]> readRawCoverageAreaList();

    /**
     * Считывает список зон.
     *
     * @return Результат чтения карты
     */
    @NonNull
    ReadCardResult<List<CoverageArea>> readCoverageAreaList();

}
