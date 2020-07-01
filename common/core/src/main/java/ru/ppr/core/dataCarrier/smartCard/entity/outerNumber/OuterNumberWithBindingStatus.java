package ru.ppr.core.dataCarrier.smartCard.entity.outerNumber;

/**
 * Внешний номер c флагом привязки БСК к пассажиру.
 *
 * @author Aleksandr Brazhkin
 */
public class OuterNumberWithBindingStatus extends OuterNumber {

    /**
     * Признак привязки БСК к пассажиру.
     */
    private boolean boundToPassenger;

    public boolean isBoundToPassenger() {
        return boundToPassenger;
    }

    public void setBoundToPassenger(boolean boundToPassenger) {
        this.boundToPassenger = boundToPassenger;
    }
}
