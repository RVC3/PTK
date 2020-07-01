package ru.ppr.core.dataCarrier.smartCard.cardReader.top;

import android.support.annotation.NonNull;

import java.util.List;

import ru.ppr.core.dataCarrier.smartCard.cardReader.ReadCardResult;
import ru.ppr.core.dataCarrier.smartCard.cardReader.base.CardReader;
import ru.ppr.core.dataCarrier.smartCard.cardReader.base.MifareClassicReader;
import ru.ppr.core.dataCarrier.smartCard.cardReader.part.OuterNumberReader;
import ru.ppr.core.dataCarrier.smartCard.cardReader.part.ServiceCardReader;
import ru.ppr.core.dataCarrier.smartCard.cardReader.part.passageMark.ReadPassageMarkReader;
import ru.ppr.core.dataCarrier.smartCard.cardReader.part.passageMark.WritePassageMarkReader;
import ru.ppr.core.dataCarrier.smartCard.coverageArea.base.CoverageArea;
import ru.ppr.core.dataCarrier.smartCard.entity.AuthCardData;
import ru.ppr.core.dataCarrier.smartCard.serviceData.base.ServiceData;

/**
 * Ридер авторизационных карт.
 *
 * @author Aleksandr Brazhkin
 */
public interface AuthCardReader extends CardReader, MifareClassicReader, OuterNumberReader, ServiceCardReader, ReadPassageMarkReader, WritePassageMarkReader {
    /**
     * Читает информацию об авторизационной карте.
     *
     * @return Результат чтения карты
     */
    @NonNull
    ReadCardResult<AuthCardData> readAuthCardData();

    /**
     * Читает служебные данные.
     *
     * @return Результат чтения карты
     */
    @NonNull
    ReadCardResult<ServiceData> readAuthServiceData();

    /**
     * Читает служебные данные (unparsed)
     * Для обратной совместимости.
     *
     * @return Результат чтения карты
     */
    @Deprecated
    @NonNull
    ReadCardResult<byte[]> readRawAuthServiceData();

    /**
     * Считывает список зон в сыром формате (часть 1).
     *
     * @return Результат чтения карты
     */
    @NonNull
    ReadCardResult<byte[]> readRawCoverageAreaListPart1();

    /**
     * Считывает список зон (часть 1).
     *
     * @return Результат чтения карты
     */
    @NonNull
    ReadCardResult<List<CoverageArea>> readCoverageAreaListPart1();

    /**
     * Считывает список зон в сыром формате (часть 2).
     *
     * @return Результат чтения карты
     */
    @NonNull
    ReadCardResult<byte[]> readRawCoverageAreaListPart2();

    /**
     * Считывает список зон (часть 2).
     *
     * @return Результат чтения карты
     */
    @NonNull
    ReadCardResult<List<CoverageArea>> readCoverageAreaListPart2();

}
