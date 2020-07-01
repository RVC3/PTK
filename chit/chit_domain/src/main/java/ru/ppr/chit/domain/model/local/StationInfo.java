package ru.ppr.chit.domain.model.local;

import java.util.Date;

import ru.ppr.chit.domain.model.local.base.LocalModelWithId;

/**
 * Информация о станции в нити поезда
 *
 * @author Dmitry Nevolin
 */
public class StationInfo implements LocalModelWithId<Long> {

    /**
     * Идентификатор
     */
    private Long id;
    /**
     * Код станции
     */
    private Long code;
    /**
     * Порядковый номер станции
     */
    private int number;
    /**
     * Статус станции
     */
    private StationState stationState;
    /**
     * Время прибытия на станцию в UTC или null, если это начальная станция
     */
    private Date arrivalDate;
    /**
     * Время отправления со станции в UTC или null, если это конечная станция
     */
    private Date departureDate;

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public Long getCode() {
        return code;
    }

    public void setCode(Long code) {
        this.code = code;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public StationState getStationState() {
        return stationState;
    }

    public void setStationState(StationState stationState) {
        this.stationState = stationState;
    }

    public Date getArrivalDate() {
        return arrivalDate;
    }

    public void setArrivalDate(Date arrivalDate) {
        this.arrivalDate = arrivalDate;
    }

    public Date getDepartureDate() {
        return departureDate;
    }

    public void setDepartureDate(Date departureDate) {
        this.departureDate = departureDate;
    }

}
