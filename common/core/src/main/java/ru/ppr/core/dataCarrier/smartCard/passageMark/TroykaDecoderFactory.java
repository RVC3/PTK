package ru.ppr.core.dataCarrier.smartCard.passageMark;

import android.support.annotation.NonNull;

import ru.ppr.core.dataCarrier.smartCard.passageMark.troyka.PassageMarkTroykaDecoder;
import ru.ppr.core.dataCarrier.smartCard.wallet.TroykaWalletDecoder;

public class TroykaDecoderFactory implements PassageMarkDecoderFactory {
    private static final int SECTOR_PASS_MARK = 9;//метка прохода
    private static final int SECTOR_WALLET = 8;//тип кошелек
    private int sector;

    public TroykaDecoderFactory(int sector) {
        this.sector = sector;
    }
    @NonNull
    @Override
    public PassageMarkDecoder create(byte[] data) {
        switch (sector) {
            case SECTOR_PASS_MARK: return new PassageMarkTroykaDecoder();
            case SECTOR_WALLET: return new TroykaWalletDecoder();
            default:  return new DefaultPassageMarkDecoder();
        }

    }
}
