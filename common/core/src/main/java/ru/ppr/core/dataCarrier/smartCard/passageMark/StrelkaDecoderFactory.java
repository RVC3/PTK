package ru.ppr.core.dataCarrier.smartCard.passageMark;

import android.support.annotation.NonNull;

import ru.ppr.core.dataCarrier.smartCard.passageMark.troyka.PassageMarkTroykaDecoder;

public class StrelkaDecoderFactory implements PassageMarkDecoderFactory {
    private static final int SECTOR_PASS_MARK = 9;//метка прохода
    private int sector;

    public StrelkaDecoderFactory(int sector) {
        this.sector = sector;
    }
    @NonNull
    @Override
    public PassageMarkDecoder create(byte[] data) {
        switch (sector) {
            case SECTOR_PASS_MARK: return new PassageMarkTroykaDecoder();
            default:  return new DefaultPassageMarkDecoder();
        }

    }
}
