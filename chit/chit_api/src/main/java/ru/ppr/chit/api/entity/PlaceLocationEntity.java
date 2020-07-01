package ru.ppr.chit.api.entity;

/**
 * @author Dmitry Nevolin
 */
public class PlaceLocationEntity {

    /**
     * Номер вагона
     */
    private String carNumber;
    /**
     * Номер места
     */
    private String placeNumber;

    public String getCarNumber() {
        return carNumber;
    }

    public void setCarNumber(String carNumber) {
        this.carNumber = carNumber;
    }

    public String getPlaceNumber() {
        return placeNumber;
    }

    public void setPlaceNumber(String placeNumber) {
        this.placeNumber = placeNumber;
    }

}
