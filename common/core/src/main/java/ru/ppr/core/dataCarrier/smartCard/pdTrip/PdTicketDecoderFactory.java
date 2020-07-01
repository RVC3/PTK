package ru.ppr.core.dataCarrier.smartCard.pdTrip;

import android.support.annotation.NonNull;

import javax.inject.Inject;

import ru.ppr.core.dataCarrier.pd.PdDecoderFactory;


public class PdTicketDecoderFactory implements PdDecoderFactory {

    @Inject
    public PdTicketDecoderFactory(){

    }

    @NonNull
    @Override
    public PdTicketDecoder create(@NonNull byte[] data) {
                return new PdTicketDecoder();
        }
}
