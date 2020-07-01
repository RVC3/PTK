package ru.ppr.chit.domain.model.local;

import ru.ppr.chit.domain.model.local.base.LocalModelWithId;

/**
 * @author Dmitry Nevolin
 */
public class Location implements LocalModelWithId<Long> {

    /**
     * Идентификатор
     */
    private Long id;
    /**
     * Номер вагона
     */
    private String carNumber;
    /**
     * Номер места
     */
    private String placeNumber;

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

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
