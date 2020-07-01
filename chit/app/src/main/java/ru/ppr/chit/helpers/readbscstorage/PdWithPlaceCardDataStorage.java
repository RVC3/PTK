package ru.ppr.chit.helpers.readbscstorage;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * In-memory хранилище данных, считанных с карты при контроле БСК.
 * Используется для передачи данных на экран с отображением информации.
 *
 * @author Aleksandr Brazhkin
 */
@Singleton
public class PdWithPlaceCardDataStorage {

    private PdWithPlaceCardData pdWithPlaceCardData;

    @Inject
    PdWithPlaceCardDataStorage() {

    }

    public void putCardData(PdWithPlaceCardData pdWithPlaceCardData) {
        this.pdWithPlaceCardData = pdWithPlaceCardData;
    }

    public void clearCardData() {
        this.pdWithPlaceCardData = null;
    }

    public PdWithPlaceCardData getLastCardData() {
        return pdWithPlaceCardData;
    }

}
