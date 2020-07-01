package ru.ppr.chit.domain.model.local;

import java.util.Date;
import java.util.List;

import ru.ppr.chit.domain.model.local.base.LocalModelWithId;
import ru.ppr.chit.domain.repository.local.CarInfoRepository;
import ru.ppr.chit.domain.repository.local.StationInfoRepository;

/**
 * Информация о нити поезда
 *
 * @author Dmitry Nevolin
 */
public class TrainInfo implements LocalModelWithId<Long> {

    /**
     * Идентификатор
     */
    private Long id;
    /**
     * Код нити поезда
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
    private Date departureDate;
    /**
     * Дата/время прибытия
     */
    private Date destinationDate;
    /**
     * Перечень станций
     */
    private List<StationInfo> stations;
    /**
     * Перечень вагонов
     */
    private List<CarInfo> cars;
    /**
     * Признак неактуальности информации о поезде
     */
    private boolean legacy;

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

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

    public Date getDepartureDate() {
        return departureDate;
    }

    public void setDepartureDate(Date departureDate) {
        this.departureDate = departureDate;
    }

    public Date getDestinationDate() {
        return destinationDate;
    }

    public void setDestinationDate(Date destinationDate) {
        this.destinationDate = destinationDate;
    }

    public List<StationInfo> getStations(StationInfoRepository stationInfoRepository) {
        List<StationInfo> local = stations;
        if (local == null) {
            synchronized (this) {
                if (stations == null) {
                    stations = stationInfoRepository.loadAllByTrainInfo(id);
                }
            }
            return stations;
        }
        return local;
    }

    public void setStations(List<StationInfo> stations) {
        this.stations = stations;
    }

    public List<CarInfo> getCars(CarInfoRepository carInfoRepository) {
        List<CarInfo> local = cars;
        if (local == null) {
            synchronized (this) {
                if (cars == null) {
                    cars = carInfoRepository.loadAllByTrainInfo(id);
                }
            }
            return cars;
        }
        return local;
    }

    public void setCars(List<CarInfo> cars) {
        this.cars = cars;
    }

    public boolean isLegacy() {
        return legacy;
    }

    public void setLegacy(boolean legacy) {
        this.legacy = legacy;
    }

}
