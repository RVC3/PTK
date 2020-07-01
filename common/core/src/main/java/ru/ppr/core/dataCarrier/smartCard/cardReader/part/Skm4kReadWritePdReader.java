package ru.ppr.core.dataCarrier.smartCard.cardReader.part;

import ru.ppr.core.dataCarrier.smartCard.cardReader.base.CardReader;
import ru.ppr.core.dataCarrier.smartCard.cardReader.base.MifareClassicReader;
import ru.ppr.core.dataCarrier.smartCard.cardReader.part.pd.ReadPdReader;
import ru.ppr.core.dataCarrier.smartCard.cardReader.part.pd.WritePdReader;

/**
 * Ридер ПД и ЭЦП для карт СКМ 4k.
 *
 * @author Grigoriy Kashka
 */
public interface Skm4kReadWritePdReader extends CardReader,
        MifareClassicReader,
        ReadEdsReader,
        WriteEdsReader,
        ReadPdReader,
        WritePdReader,
        WithMaxPdCountReader {
}
