package ru.ppr.core.dataCarrier.smartCard.cardReader.top;

import ru.ppr.core.dataCarrier.smartCard.cardReader.base.CardReader;
import ru.ppr.core.dataCarrier.smartCard.cardReader.base.MifareClassicReader;
import ru.ppr.core.dataCarrier.smartCard.cardReader.base.SkmSkmoIpkReader;
import ru.ppr.core.dataCarrier.smartCard.cardReader.part.ReadEdsReader;
import ru.ppr.core.dataCarrier.smartCard.cardReader.part.WithMaxPdCountReader;
import ru.ppr.core.dataCarrier.smartCard.cardReader.part.WriteEdsReader;
import ru.ppr.core.dataCarrier.smartCard.cardReader.part.passageMark.ReadPassageMarkReader;
import ru.ppr.core.dataCarrier.smartCard.cardReader.part.pd.ReadPdReader;
import ru.ppr.core.dataCarrier.smartCard.cardReader.part.passageMark.WritePassageMarkReader;
import ru.ppr.core.dataCarrier.smartCard.cardReader.part.pd.WritePdReader;

/**
 * Ридер карт ИПК.
 *
 * @author Aleksandr Brazhkin
 */
public interface IpkReader extends CardReader,
        MifareClassicReader,
        ReadEdsReader,
        WriteEdsReader,
        ReadPdReader,
        WritePdReader,
        SkmSkmoIpkReader,
        ReadPassageMarkReader,
        WritePassageMarkReader,
        WithMaxPdCountReader {
}
