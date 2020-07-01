package ru.ppr.chit.domain.model.local;

import java.util.Date;

import ru.ppr.chit.domain.model.local.base.LocalModelWithId;
import ru.ppr.chit.domain.model.nsi.Exemption;
import ru.ppr.chit.domain.model.nsi.Station;
import ru.ppr.chit.domain.model.nsi.TicketType;
import ru.ppr.chit.domain.repository.local.PassengerPersonalDataRepository;
import ru.ppr.chit.domain.repository.local.PlaceLocationRepository;
import ru.ppr.chit.domain.repository.local.TicketIdRepository;
import ru.ppr.chit.domain.repository.nsi.ExemptionRepository;
import ru.ppr.chit.domain.repository.nsi.StationRepository;
import ru.ppr.chit.domain.repository.nsi.TicketTypeRepository;
import ru.ppr.utils.ObjectUtils;

/**
 * Билет.
 *
 * @author Aleksandr Brazhkin
 */
public class Ticket implements LocalModelWithId<Long> {

    /**
     * Идентификатор
     */
    private Long id;
    /**
     * Идентификатор ticketId
     */
    private Long ticketIdId;
    /**
     * Идентификатор билета
     */
    private TicketId ticketId;
    /**
     * Код нити поезда
     */
    private String trainThreadCode;
    /**
     * Номер поезда
     */
    private String trainNumber;
    /**
     * Код станции отправления
     */
    private Long departureStationCode;
    /**
     * Станция отправления
     */
    private Station departureStation;
    /**
     * Код станции назначения
     */
    private Long destinationStationCode;
    /**
     * Станция назначения
     */
    private Station destinationStation;
    /**
     * Время отправления
     */
    private Date departureDate;
    /**
     * Код типа билета
     */
    private Long ticketTypeCode;
    /**
     * Тип билета
     */
    private TicketType ticketType;
    /**
     * Код льготы или null
     */
    private Integer exemptionExpressCode;
    /**
     * Тип оформления билета
     */
    private TicketIssueType ticketIssueType;
    /**
     * Статус билета
     */
    private TicketState ticketState;
    /**
     * Дата и время последнего изменения статуса
     */
    private Date stateDate;
    /**
     * Идентификатор passenger
     */
    private Long passengerId;
    /**
     * Информация о пассажире
     */
    private PassengerPersonalData passenger;
    /**
     * Идентификатор placeLocation
     */
    private Long placeLocationId;
    /**
     * Место в поезде или null, если билет без места
     */
    private PlaceLocation placeLocation;
    /**
     * Идентификатор oldPlaceLocation
     */
    private Long oldPlaceLocationId;
    /**
     * Старое место в поезде, если была смена места или null, если смены не было или билет без места
     */
    private PlaceLocation oldPlaceLocation;
    /**
     * Идентификатор версии НСИ
     */
    private int nsiVersion;

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    //region TicketId getters and setters
    public Long getTicketIdId() {
        return ticketIdId;
    }

    public void setTicketIdId(Long ticketIdId) {
        this.ticketIdId = ticketIdId;
        if (this.ticketId != null && !ObjectUtils.equals(this.ticketId.getId(), ticketIdId)) {
            this.ticketId = null;
        }
    }

    public TicketId getTicketId(TicketIdRepository ticketIdRepository) {
        TicketId local = ticketId;
        if (local == null && ticketIdId != null) {
            synchronized (this) {
                if (ticketId == null) {
                    ticketId = ticketIdRepository.load(ticketIdId);
                }
            }
            return ticketId;
        }
        return local;
    }

    public void setTicketId(TicketId ticketId) {
        this.ticketId = ticketId;
        this.ticketIdId = ticketId != null ? ticketId.getId() : null;
    }
    //endregion


    public String getTrainThreadCode() {
        return trainThreadCode;
    }

    public void setTrainThreadCode(String trainThreadCode) {
        this.trainThreadCode = trainThreadCode;
    }

    public String getTrainNumber() {
        return trainNumber;
    }

    public void setTrainNumber(String trainNumber) {
        this.trainNumber = trainNumber;
    }

    //region DepartureStation getters and setters
    public Long getDepartureStationCode() {
        return departureStationCode;
    }

    public void setDepartureStationCode(Long departureStationCode) {
        this.departureStationCode = departureStationCode;
        if (this.departureStation != null && !ObjectUtils.equals(this.departureStation.getCode(), departureStationCode)) {
            this.departureStation = null;
        }
    }

    public Station getDepartureStation(StationRepository stationRepository, int versionId) {
        Station local = departureStation;
        if (local == null && departureStationCode != null) {
            synchronized (this) {
                if (departureStation == null) {
                    departureStation = stationRepository.load(departureStationCode, versionId);
                }
            }
            return departureStation;
        }
        return local;
    }

    public void setDepartureStation(Station departureStation) {
        this.departureStation = departureStation;
        this.departureStationCode = departureStation != null ? departureStation.getCode() : null;
    }
    //endregion

    //region DestinationStation getters and setters
    public Long getDestinationStationCode() {
        return destinationStationCode;
    }

    public void setDestinationStationCode(Long destinationStationCode) {
        this.destinationStationCode = destinationStationCode;
        if (this.destinationStation != null && !ObjectUtils.equals(this.destinationStation.getCode(), destinationStationCode)) {
            this.destinationStation = null;
        }
    }

    public Station getDestinationStation(StationRepository stationRepository, int versionId) {
        Station local = destinationStation;
        if (local == null && destinationStationCode != null) {
            synchronized (this) {
                if (destinationStation == null) {
                    destinationStation = stationRepository.load(destinationStationCode, versionId);
                }
            }
            return destinationStation;
        }
        return local;
    }

    public void setDestinationStation(Station destinationStation) {
        this.destinationStation = destinationStation;
        this.destinationStationCode = destinationStation != null ? destinationStation.getCode() : null;
    }
    //endregion

    public Date getDepartureDate() {
        return departureDate;
    }

    public void setDepartureDate(Date departureDate) {
        this.departureDate = departureDate;
    }

    //region TicketType getters and setters
    public Long getTicketTypeCode() {
        return ticketTypeCode;
    }

    public void setTicketTypeCode(Long ticketTypeCode) {
        this.ticketTypeCode = ticketTypeCode;
        if (this.ticketType != null && !ObjectUtils.equals(this.ticketType.getCode(), ticketTypeCode)) {
            this.ticketType = null;
        }
    }

    public TicketType getTicketType(TicketTypeRepository ticketTypeRepository, int versionId) {
        TicketType local = ticketType;
        if (local == null && ticketTypeCode != null) {
            synchronized (this) {
                if (ticketType == null) {
                    ticketType = ticketTypeRepository.load(ticketTypeCode, versionId);
                }
            }
            return ticketType;
        }
        return local;
    }

    public void setTicketType(TicketType ticketType) {
        this.ticketType = ticketType;
        this.ticketTypeCode = ticketType != null ? ticketType.getCode() : null;
    }
    //endregion

    //region Exemption getters and setters
    public Integer getExemptionExpressCode() {
        return exemptionExpressCode;
    }

    public void setExemptionExpressCode(Integer exemptionExpressCode) {
        this.exemptionExpressCode = exemptionExpressCode;
    }
    //endregion

    public TicketIssueType getTicketIssueType() {
        return ticketIssueType;
    }

    public void setTicketIssueType(TicketIssueType ticketIssueType) {
        this.ticketIssueType = ticketIssueType;
    }

    public TicketState getTicketState() {
        return ticketState;
    }

    public void setTicketState(TicketState ticketState) {
        this.ticketState = ticketState;
    }

    public Date getStateDate() {
        return stateDate;
    }

    public void setStateDate(Date stateDate) {
        this.stateDate = stateDate;
    }

    //region Passenger getters and setters
    public Long getPassengerId() {
        return passengerId;
    }

    public void setPassengerId(Long passengerId) {
        this.passengerId = passengerId;
        if (this.passenger != null && !ObjectUtils.equals(this.passenger.getId(), passengerId)) {
            this.passenger = null;
        }
    }

    public PassengerPersonalData getPassenger(PassengerPersonalDataRepository passengerPersonalDataRepository) {
        PassengerPersonalData local = passenger;
        if (local == null && passengerId != null) {
            synchronized (this) {
                if (passenger == null) {
                    passenger = passengerPersonalDataRepository.load(passengerId);
                }
            }
            return passenger;
        }
        return local;
    }

    public void setPassenger(PassengerPersonalData passenger) {
        this.passenger = passenger;
        this.passengerId = passenger != null ? passenger.getId() : null;
    }
    //endregion

    //region PlaceLocation getters and setters
    public Long getPlaceLocationId() {
        return placeLocationId;
    }

    public void setPlaceLocationId(Long placeLocationId) {
        this.placeLocationId = placeLocationId;
        if (this.placeLocation != null && !ObjectUtils.equals(this.placeLocation.getId(), placeLocationId)) {
            this.placeLocation = null;
        }
    }

    public PlaceLocation getPlaceLocation(PlaceLocationRepository placeLocationRepository) {
        PlaceLocation local = placeLocation;
        if (local == null && placeLocationId != null) {
            synchronized (this) {
                if (placeLocation == null) {
                    placeLocation = placeLocationRepository.load(placeLocationId);
                }
            }
            return placeLocation;
        }
        return local;
    }

    public void setPlaceLocation(PlaceLocation placeLocation) {
        this.placeLocation = placeLocation;
        this.placeLocationId = placeLocation != null ? placeLocation.getId() : null;
    }
    //endregion

    //region OldPlaceLocation getters and setters
    public Long getOldPlaceLocationId() {
        return oldPlaceLocationId;
    }

    public void setOldPlaceLocationId(Long oldPlaceLocationId) {
        this.oldPlaceLocationId = oldPlaceLocationId;
        if (this.placeLocation != null && !ObjectUtils.equals(this.placeLocation.getId(), oldPlaceLocationId)) {
            this.placeLocation = null;
        }
    }

    public PlaceLocation getOldPlaceLocation(PlaceLocationRepository placeLocationRepository) {
        PlaceLocation local = oldPlaceLocation;
        if (local == null && oldPlaceLocationId != null) {
            synchronized (this) {
                if (oldPlaceLocation == null) {
                    oldPlaceLocation = placeLocationRepository.load(oldPlaceLocationId);
                }
            }
            return oldPlaceLocation;
        }
        return local;
    }

    public void setOldPlaceLocation(PlaceLocation oldPlaceLocation) {
        this.oldPlaceLocation = oldPlaceLocation;
        this.oldPlaceLocationId = oldPlaceLocation != null ? oldPlaceLocation.getId() : null;
    }
    //endregion

    public int getNsiVersion() {
        return nsiVersion;
    }

    public void setNsiVersion(int nsiVersion) {
        this.nsiVersion = nsiVersion;
    }

}
