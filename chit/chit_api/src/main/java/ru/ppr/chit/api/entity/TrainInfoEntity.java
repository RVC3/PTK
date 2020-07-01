package ru.ppr.chit.api.entity;

import java.util.List;

/**
 * @author Dmitry Nevolin
 */
public class TrainInfoEntity {
    /**
     * Идентификатор нити поезда
     */
    private String trainThreadId;
    /**
     * Номер поезда
     */
    private String trainNumber;
    /**
     * Код станции отправления
     */
    private Long departureStationCode;
    /**
     * Код станции назначения
     */
    private Long destinationStationCode;
    /**
     * Дата/время отправления
     */
    private String departureDateTimeUtc;
    /**
     * Дата/время прибытия
     */
    private String destinationDateTimeUtc;
    /**
     * Перечень станций
     */
    private List<StationInfoEntity> stations;
    /**
     * Перечень вагонов
     */
    private List<CarInfoEntity> cars;

    public String getTrainThreadId() {
        return trainThreadId;
    }

    public void setTrainThreadId(String trainThreadId) {
        this.trainThreadId = trainThreadId;
    }

    public String getTrainNumber() {
        return trainNumber;
    }

    public void setTrainNumber(String trainNumber) {
        this.trainNumber = trainNumber;
    }

    public Long getDepartureStationCode() {
        return departureStationCode;
    }

    public void setDepartureStationCode(Long departureStationCode) {
        this.departureStationCode = departureStationCode;
    }

    public Long getDestinationStationCode() {
        return destinationStationCode;
    }

    public void setDestinationStationCode(Long destinationStationCode) {
        this.destinationStationCode = destinationStationCode;
    }

    public String getDepartureDateTimeUtc() {
        return departureDateTimeUtc;
    }

    public void setDepartureDateTimeUtc(String departureDateTimeUtc) {
        this.departureDateTimeUtc = departureDateTimeUtc;
    }

    public String getDestinationDateTimeUtc() {
        return destinationDateTimeUtc;
    }

    public void setDestinationDateTimeUtc(String destinationDateTimeUtc) {
        this.destinationDateTimeUtc = destinationDateTimeUtc;
    }

    public List<StationInfoEntity> getStations() {
        return stations;
    }

    public void setStations(List<StationInfoEntity> stations) {
        this.stations = stations;
    }

    public List<CarInfoEntity> getCars() {
        return cars;
    }

    public void setCars(List<CarInfoEntity> cars) {
        this.cars = cars;
    }

}
