package ru.ppr.cppk.ui.fragment.pd.countrips.model;

import android.support.annotation.Nullable;

import ru.ppr.core.dataCarrier.pd.v23.PdV23;
import ru.ppr.core.dataCarrier.pd.v24.PdV24;

/**
 * Информация о контроле ПД на количество поездок.
 *
 * @author Aleksandr Brazhkin
 */
public class CountTripsPdControlData {
    /**
     * Флаг списания поездки
     * {@code true} - если при контроле была списана поездка
     * {@code false} - иначе
     */
    private boolean tripsCountDecremented;
    /**
     * Флаг списания поездки на поезд 7000
     * {@code true} - если при контроле была списана поездка на поезд 7000
     * {@code false} - иначе
     */
    private boolean trips7000CountDecremented;
    /**
     * Количество оставшихся поездок после контроля
     */
    private int availableTripsCount;
    /**
     * Количество оставшихся поездок на поезд 7000 после контроля
     * (Заполняется только для {@link PdV23},{@link PdV24})
     */
    private Integer availableTrips7000Count;

    public boolean isTripsCountDecremented() {
        return tripsCountDecremented;
    }

    public void setTripsCountDecremented(boolean tripsCountDecremented) {
        this.tripsCountDecremented = tripsCountDecremented;
    }

    public boolean isTrips7000CountDecremented() {
        return trips7000CountDecremented;
    }

    public void setTrips7000CountDecremented(boolean trips7000CountDecremented) {
        this.trips7000CountDecremented = trips7000CountDecremented;
    }

    public int getAvailableTripsCount() {
        return availableTripsCount;
    }

    public void setAvailableTripsCount(int availableTripsCount) {
        this.availableTripsCount = availableTripsCount;
    }

    @Nullable
    public Integer getAvailableTrips7000Count() {
        return availableTrips7000Count;
    }

    public void setAvailableTrips7000Count(@Nullable Integer availableTrips7000Count) {
        this.availableTrips7000Count = availableTrips7000Count;
    }
}
