package ru.ppr.core.dataCarrier.smartCard.wallet;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import ru.ppr.core.dataCarrier.DataCarrierUtils;
import ru.ppr.core.dataCarrier.smartCard.passageMark.PassageMarkDecoder;
import ru.ppr.core.dataCarrier.smartCard.passageMark.base.PassageMark;

import static ru.ppr.core.dataCarrier.smartCard.wallet.TroykaWalletStructure.*;

public class TroykaWalletDecoder implements PassageMarkDecoder {

    @Nullable
    @Override
    public PassageMark decode(@NonNull byte[] data) {
        if (data.length < TroykaWalletStructure.SECTOR_SIZE) return null;
        TroykaWalletImpl troykaWallet = new TroykaWalletImpl();
        troykaWallet.setUnits(DataCarrierUtils.getValue(data, UNITS_LEFT_BLOCK, UNITS_LEFT_SIZE));
        troykaWallet.setDaysEnd(DataCarrierUtils.getValue(data, END_TIME_BLOCK, END_TIME_SIZE));
        final int codeFormat = DataCarrierUtils.getValue(data, CODING_FORMAT_BLOCK, CODING_FORMAT_SIZE);
        final int extendNumFormat = DataCarrierUtils.getValue(data, EXTEND_NUM_FORMAT_BLOCK, EXTEND_NUM_FORMAT_SIZE);
        troykaWallet.setFormatData(codeFormat,extendNumFormat);
        return troykaWallet;
    }

}