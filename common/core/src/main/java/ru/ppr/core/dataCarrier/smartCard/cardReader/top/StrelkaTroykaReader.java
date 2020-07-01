package ru.ppr.core.dataCarrier.smartCard.cardReader.top;

import android.support.annotation.NonNull;

import ru.ppr.core.dataCarrier.smartCard.cardReader.ReadCardResult;
import ru.ppr.core.dataCarrier.smartCard.cardReader.base.CardReader;
import ru.ppr.core.dataCarrier.smartCard.cardReader.base.MifareClassicReader;
import ru.ppr.core.dataCarrier.smartCard.cardReader.part.ReadEdsReader;
import ru.ppr.core.dataCarrier.smartCard.cardReader.part.OuterNumberReader;
import ru.ppr.core.dataCarrier.smartCard.cardReader.part.WithMaxPdCountReader;
import ru.ppr.core.dataCarrier.smartCard.cardReader.part.WriteEdsReader;
import ru.ppr.core.dataCarrier.smartCard.cardReader.part.passageMark.ReadPassageMarkReader;
import ru.ppr.core.dataCarrier.smartCard.cardReader.part.pd.ReadPdReader;
import ru.ppr.core.dataCarrier.smartCard.cardReader.part.pd.WritePdReader;
import ru.ppr.core.dataCarrier.smartCard.pdTrip.TicketMetroPd;
import ru.ppr.core.dataCarrier.smartCard.pdTroyka.MetroPd;
import ru.ppr.core.dataCarrier.smartCard.wallet.MetroWallet;

/**
 * Ридер карт Стрелка.
 *
 * @author Aleksandr Brazhkin
 */
public interface StrelkaTroykaReader extends CardReader,
        MifareClassicReader,
        OuterNumberReader,
        ReadEdsReader,
        WriteEdsReader,
        ReadPassageMarkReader,
        ReadPdReader,
        WritePdReader,
        WithMaxPdCountReader {

    /**
     * Считывает данные кошелька
     * @return
     */
    @NonNull
    ReadCardResult<MetroWallet> readWalletData();

    /**
     * Считывает основную информацию комплексного и единого билета
     */

    @NonNull
    ReadCardResult<MetroPd> readTicketPd();

    /**
     *  Считываем 1, либо 2 блок Комплексного и единого билета
     */

    @NonNull
    ReadCardResult<TicketMetroPd> readInformationPd(byte block);
}
