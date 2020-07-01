package ru.ppr.core.dataCarrier.smartCard.cardReader.top;

import ru.ppr.core.dataCarrier.smartCard.cardReader.base.CardReader;
import ru.ppr.core.dataCarrier.smartCard.cardReader.base.MifareClassicReader;
import ru.ppr.core.dataCarrier.smartCard.cardReader.base.SkmSkmoIpkReader;
import ru.ppr.core.dataCarrier.smartCard.cardReader.part.WithMaxPdCountReader;

/**
 * Интерфейс ридера СКМ карт с ошибкой чтения ПД или ЭЦП
 * Эмулируем ситуацию что на карте нет места
 * http://agile.srvdev.ru/browse/CPPKPP-34076
 *
 * @author Grigoriy Kashka
 */
public interface SkmNoPdPlaceReader extends CardReader,
        MifareClassicReader,
        SkmSkmoIpkReader,
        WithMaxPdCountReader {
}
