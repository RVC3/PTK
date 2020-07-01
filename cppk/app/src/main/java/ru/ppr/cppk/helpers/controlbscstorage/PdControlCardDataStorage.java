package ru.ppr.cppk.helpers.controlbscstorage;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * In-memory хранилище данных, считанных с карты при контроле БСК.
 * Используется для передачи данных на экран с отображением информации.
 *
 * @author Aleksandr Brazhkin
 */
@Singleton
public class PdControlCardDataStorage {

    private PdControlCardData pdControlCardData;

    @Inject
    PdControlCardDataStorage() {

    }

    public void putCardData(PdControlCardData pdControlCardData) {
        this.pdControlCardData = pdControlCardData;
    }

    public void clearCardData() {
        this.pdControlCardData = null;
    }

    public PdControlCardData getLastCardData() {
        return pdControlCardData;
    }

}
