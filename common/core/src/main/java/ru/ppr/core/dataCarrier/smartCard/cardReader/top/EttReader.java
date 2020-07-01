package ru.ppr.core.dataCarrier.smartCard.cardReader.top;

import android.support.annotation.NonNull;

import ru.ppr.core.dataCarrier.smartCard.cardReader.ReadCardResult;
import ru.ppr.core.dataCarrier.smartCard.cardReader.base.CardReader;
import ru.ppr.core.dataCarrier.smartCard.cardReader.base.MifareClassicReader;
import ru.ppr.core.dataCarrier.smartCard.cardReader.part.BscInformationReader;
import ru.ppr.core.dataCarrier.smartCard.cardReader.part.ReadEdsReader;
import ru.ppr.core.dataCarrier.smartCard.cardReader.part.WithMaxPdCountReader;
import ru.ppr.core.dataCarrier.smartCard.cardReader.part.WriteEdsReader;
import ru.ppr.core.dataCarrier.smartCard.cardReader.part.pd.ClearPdReader;
import ru.ppr.core.dataCarrier.smartCard.cardReader.part.pd.ReadPdReader;
import ru.ppr.core.dataCarrier.smartCard.cardReader.part.pd.WritePdReader;
import ru.ppr.core.dataCarrier.smartCard.entity.EttData;

/**
 * Ридер карт ЭТТ.
 *
 * @author Aleksandr Brazhkin
 */
public interface EttReader extends CardReader,
        MifareClassicReader,
        BscInformationReader,
        ReadEdsReader,
        WriteEdsReader,
        ReadPdReader,
        WritePdReader,
        ClearPdReader,
        WithMaxPdCountReader {
    /**
     * Читает даныне ЭТТ.
     *
     * @return Результат чтения карты
     */
    @NonNull
    ReadCardResult<EttData> readEttData();
}
