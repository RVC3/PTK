package ru.ppr.core.dataCarrier.smartCard.cardReader.top;

import ru.ppr.core.dataCarrier.smartCard.cardReader.base.TroykaReader;
import ru.ppr.core.dataCarrier.smartCard.cardReader.part.WithMaxPdCountReader;
import ru.ppr.core.dataCarrier.smartCard.cardReader.part.pd.ReadPdReader;
import ru.ppr.core.dataCarrier.smartCard.cardReader.part.pd.WritePdReader;

/**
 * Ридер карт Тройка.
 *
 * @author Aleksandr Brazhkin
 */
public interface TroykaWithPdReader extends TroykaReader,
        ReadPdReader,
        WritePdReader,
        WithMaxPdCountReader {
}
