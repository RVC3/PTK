package ru.ppr.chit.helpers;

import javax.inject.Inject;
import javax.inject.Singleton;

import ru.ppr.core.dataCarrier.pd.base.PdWithPlace;

/**
 * In-memory хранилище данных, считанных с ШК при контроле.
 * Используется для передачи данных на экран с отображением информации.
 *
 * @author Grigoriy Kashka
 */
@Singleton
public class PdWithPlaceBarcodeStorage {
    private PdWithPlace pdWithPlace;

    @Inject
    PdWithPlaceBarcodeStorage() {

    }

    public void putData(PdWithPlace pdWithPlace) {
        this.pdWithPlace = pdWithPlace;
    }

    public void clearData() {
        this.pdWithPlace = null;
    }

    public PdWithPlace getLastData() {
        return pdWithPlace;
    }
}
