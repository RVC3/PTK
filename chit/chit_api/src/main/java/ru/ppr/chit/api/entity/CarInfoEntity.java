package ru.ppr.chit.api.entity;

/**
 * @author Dmitry Nevolin
 */
public class CarInfoEntity {

    /**
     * Номер вагона
     */
    private String number;
    /**
     * Схема вагона
     */
    private CarSchemeEntity scheme;

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public CarSchemeEntity getScheme() {
        return scheme;
    }

    public void setScheme(CarSchemeEntity scheme) {
        this.scheme = scheme;
    }

}
