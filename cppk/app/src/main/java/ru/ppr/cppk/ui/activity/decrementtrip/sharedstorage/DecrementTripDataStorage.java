package ru.ppr.cppk.ui.activity.decrementtrip.sharedstorage;

import javax.inject.Inject;
import javax.inject.Singleton;

import ru.ppr.cppk.ui.activity.decrementtrip.model.DecrementTripData;

/**
 * In-memory хранилище данных, полученных в результате списания поездкиК.
 * Используется для передачи данных на экран с отображением информации.
 *
 * @author Aleksandr Brazhkin
 */
@Singleton
public class DecrementTripDataStorage {
    private DecrementTripData decrementTripData;

    @Inject
    DecrementTripDataStorage() {

    }

    public void putData(DecrementTripData decrementTripData) {
        this.decrementTripData = decrementTripData;
    }

    public void clearData() {
        this.decrementTripData = null;
    }

    public DecrementTripData getLastData() {
        return decrementTripData;
    }
}
