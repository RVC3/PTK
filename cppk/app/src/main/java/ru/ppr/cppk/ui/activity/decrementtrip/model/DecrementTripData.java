package ru.ppr.cppk.ui.activity.decrementtrip.model;

import android.support.annotation.NonNull;

import ru.ppr.core.dataCarrier.smartCard.passageMark.base.PassageMark;

/**
 * Данные, полученные в результате списания поездки.
 *
 * @author Aleksandr Brazhkin
 */
public class DecrementTripData {
    /**
     * Метка прохода после списания поездки
     */
    private final PassageMark passageMark;
    /**
     * Показания хардварного счетчика после списания поездки
     */
    private final int newHwCounterValue;

    public DecrementTripData(@NonNull PassageMark passageMark, int newHwCounterValue) {
        this.passageMark = passageMark;
        this.newHwCounterValue = newHwCounterValue;
    }

    @NonNull
    public PassageMark getPassageMark() {
        return passageMark;
    }

    public int getNewHwCounterValue() {
        return newHwCounterValue;
    }
}
