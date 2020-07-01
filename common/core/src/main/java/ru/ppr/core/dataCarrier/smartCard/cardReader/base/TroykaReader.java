package ru.ppr.core.dataCarrier.smartCard.cardReader.base;

import android.support.annotation.NonNull;

import ru.ppr.core.dataCarrier.smartCard.cardReader.ReadCardResult;
import ru.ppr.core.dataCarrier.smartCard.cardReader.part.OuterNumberReader;
import ru.ppr.core.dataCarrier.smartCard.cardReader.part.ReadEdsReader;
import ru.ppr.core.dataCarrier.smartCard.cardReader.part.WriteEdsReader;
import ru.ppr.core.dataCarrier.smartCard.cardReader.part.passageMark.ReadPassageMarkReader;
import ru.ppr.core.dataCarrier.smartCard.pdTrip.TicketMetroPd;
import ru.ppr.core.dataCarrier.smartCard.pdTroyka.MetroPd;
import ru.ppr.core.dataCarrier.smartCard.wallet.MetroWallet;

/**
 * Ридер карт Тройка.
 *
 * @author Aleksandr Brazhkin
 */
public interface TroykaReader extends CardReader,
        MifareClassicReader,
        OuterNumberReader,
        ReadPassageMarkReader,
        ReadEdsReader,
        WriteEdsReader {

    /**
     * Считывает первый блок с ПД/служебными данными.
     *
     * @return Результат чтения карты
     */
    @NonNull
    ReadCardResult<byte[]> readFirstPayloadBlock();

    /**
     * Считывает данные кошелька
     * @return
     */
    @NonNull
    ReadCardResult<MetroWallet> readWalletData();

    /**
     * Запись данные кошелька
     * @return
     */
    @NonNull
    ReadCardResult<MetroWallet> writeWalletData();

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
