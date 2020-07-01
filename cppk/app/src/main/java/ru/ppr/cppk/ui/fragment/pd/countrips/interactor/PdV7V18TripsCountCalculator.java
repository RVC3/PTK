package ru.ppr.cppk.ui.fragment.pd.countrips.interactor;

import android.support.annotation.NonNull;

import javax.inject.Inject;

import ru.ppr.core.dataCarrier.pd.base.Pd;
import ru.ppr.core.dataCarrier.pd.base.PdWithCounter;
import ru.ppr.core.dataCarrier.pd.v18.PdV18;
import ru.ppr.core.dataCarrier.pd.v7.PdV7;

/**
 * Калькулятор количества доступных поездок по {@link PdV7}, {@link PdV18}.
 *
 * @author Aleksandr Brazhkin
 */
public class PdV7V18TripsCountCalculator {

    @Inject
    PdV7V18TripsCountCalculator() {

    }

    public int calcTripsCount(@NonNull Pd pd, int hwCounterValue) {
        if (!(pd instanceof PdV7 || pd instanceof PdV18)) {
            throw new IllegalArgumentException("Incorrect pd version: " + pd.getVersion());
        }
        PdWithCounter pdWithCounter = (PdWithCounter) pd;
        if (pdWithCounter.getStartCounterValue() > hwCounterValue) {
            // Если ПД ещё не активен, то осталось полное количество поездок
            int totalTripsCount = pdWithCounter.getEndCounterValue() - pdWithCounter.getStartCounterValue() + 1; // Включительно
            // Защищаемся от отрицательных значений
            totalTripsCount = Math.max(totalTripsCount, 0);
            return totalTripsCount;
        } else {
            int availableTripsCount = pdWithCounter.getEndCounterValue() - hwCounterValue + 1; // Включительно
            // Защищаемся от отрицательных значений
            availableTripsCount = Math.max(availableTripsCount, 0);
            return availableTripsCount;
        }
    }
}
