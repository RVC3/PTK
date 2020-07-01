package ru.ppr.cppk.ui.activity.controlreadbsc.interactor;

import javax.inject.Inject;

import ru.ppr.core.dataCarrier.smartCard.cardReader.ReadCardResult;
import ru.ppr.core.dataCarrier.smartCard.cardReader.base.CardReader;
import ru.ppr.core.dataCarrier.smartCard.cardReader.base.TroykaReader;
import ru.ppr.core.dataCarrier.smartCard.cardReader.top.StrelkaTroykaReader;
import ru.ppr.core.dataCarrier.smartCard.wallet.MetroWallet;

/**
 * Операция чтения данных кошелька
 *
 * @author isedoi
 */
public class ReadWalletInteractor {


    @Inject
    ReadWalletInteractor() {

    }

    public ReadCardResult<MetroWallet> readWalletData( CardReader cardReader) {
        return (cardReader instanceof TroykaReader)? ((TroykaReader)cardReader).readWalletData(): ((StrelkaTroykaReader)cardReader).readWalletData();
    }
}
