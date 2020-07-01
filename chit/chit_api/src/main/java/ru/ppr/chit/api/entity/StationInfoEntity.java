package ru.ppr.chit.api.entity;

/**
 * @author Dmitry Nevolin
 */
public class StationInfoEntity {

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
    private StationStateEntity stationState;
    /**
     * Время прибытия на станцию в UTC или null, если это начальная станция
     */
    private String arrivalDateTimeUtc;
    /**
     * Время отправления со станции в UTC или null, если это конечная станция
     */
    private String departureDateTimeUtc;

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

    public StationStateEntity getStationState() {
        return stationState;
    }

    public void setStationState(StationStateEntity stationState) {
        this.stationState = stationState;
    }

    public String getArrivalDateTimeUtc() {
        return arrivalDateTimeUtc;
    }

    public void setArrivalDateTimeUtc(String arrivalDateTimeUtc) {
        this.arrivalDateTimeUtc = arrivalDateTimeUtc;
    }

    public String getDepartureDateTimeUtc() {
        return departureDateTimeUtc;
    }

    public void setDepartureDateTimeUtc(String departureDateTimeUtc) {
        this.departureDateTimeUtc = departureDateTimeUtc;
    }

}
