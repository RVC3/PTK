package ru.ppr.core.dataCarrier.smartCard.pdTroyka;

import android.support.annotation.NonNull;

import javax.inject.Inject;

import ru.ppr.core.dataCarrier.pd.PdDecoderFactory;


public class PdTroykaDecoderFactory implements PdDecoderFactory {

    @Inject
    public PdTroykaDecoderFactory(){

    }

    @NonNull
    @Override
    public PdTroykaDecoder create(@NonNull byte[] data) {
                return new PdTroykaDecoder();
        }
}
