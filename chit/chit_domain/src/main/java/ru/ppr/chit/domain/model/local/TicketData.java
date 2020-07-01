package ru.ppr.chit.domain.model.local;

import android.support.annotation.Nullable;

import java.util.Date;

import ru.ppr.chit.domain.model.local.base.LocalModelWithId;
import ru.ppr.chit.domain.model.nsi.Station;
import ru.ppr.chit.domain.repository.local.LocationRepository;
import ru.ppr.chit.domain.repository.local.PassengerRepository;
import ru.ppr.chit.domain.repository.local.SmartCardRepository;
import ru.ppr.chit.domain.repository.nsi.StationRepository;
import ru.ppr.utils.ObjectUtils;

/**
 * Данные билета
 *
 * @author Dmitry Nevolin
 */
public class TicketData implements LocalModelWithId<Long> {

    /**
     * Идентификатор
     */
    private Long id;
    /**
     * Вид ПД
     */
    private Long ticketTypeCode;
    /**
     * Идентификатор информации о пассажире
     */
    private Long passengerId;
    /**
     * Информация о пассажире
     */
    private Passenger passenger;
    /**
     * Идентификатор места в поезде
     */
    private Long locationId;
    /**
     * Место в поезде
     */
    private Location location;
    /**
     * Время отправления
     */
    private Date departureDate;
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
     * Тариф
     */
    private Long tariffId;
    /**
     * Код льготы или <c>null</c>.
     */
    private Integer exemptionExpressCode;
    /**
     * Идентификатор информации по БСК
     */
    private Long smartCardId;
    /**
     * Информация по БСК
     */
    private SmartCard smartCard;
    /**
     * Версия НСИ
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

    public Long getTicketTypeCode() {
        return ticketTypeCode;
    }

    public void setTicketTypeCode(Long ticketTypeCode) {
        this.ticketTypeCode = ticketTypeCode;
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

    public Passenger getPassenger(PassengerRepository passengerRepository) {
        Passenger local = passenger;
        if (local == null && passengerId != null) {
            synchronized (this) {
                if (passenger == null) {
                    passenger = passengerRepository.load(passengerId);
                }
            }
            return passenger;
        }
        return local;
    }

    public void setPassenger(Passenger passenger) {
        this.passenger = passenger;
        this.passengerId = passenger != null ? passenger.getId() : null;
    }
    //endregion

    //region Location getters and setters
    public Long getLocationId() {
        return locationId;
    }

    public void setLocationId(Long locationId) {
        this.locationId = locationId;
        if (this.location != null && !ObjectUtils.equals(this.location.getId(), locationId)) {
            this.location = null;
        }
    }

    @Nullable
    public Location getLocation(LocationRepository locationRepository) {
        Location local = location;
        if (local == null && locationId != null) {
            synchronized (this) {
                if (location == null) {
                    location = locationRepository.load(locationId);
                }
            }
            return location;
        }
        return local;
    }

    public void setLocation(Location location) {
        this.location = location;
        this.locationId = location != null ? location.getId() : null;
    }
    //endregion

    public Date getDepartureDate() {
        return departureDate;
    }

    public void setDepartureDate(Date departureDate) {
        this.departureDate = departureDate;
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

    public Long getTariffId() {
        return tariffId;
    }

    public void setTariffId(Long tariffId) {
        this.tariffId = tariffId;
    }

    public Integer getExemptionExpressCode() {
        return exemptionExpressCode;
    }

    public void setExemptionExpressCode(Integer exemptionExpressCode) {
        this.exemptionExpressCode = exemptionExpressCode;
    }

    //region SmartCard getters and setters
    public Long getSmartCardId() {
        return smartCardId;
    }

    public void setSmartCardId(Long smartCardId) {
        this.smartCardId = smartCardId;
        if (this.smartCard != null && !ObjectUtils.equals(this.smartCard.getId(), smartCardId)) {
            this.smartCard = null;
        }
    }

    @Nullable
    public SmartCard getSmartCard(SmartCardRepository smartCardRepository) {
        SmartCard local = smartCard;
        if (local == null && smartCardId != null) {
            synchronized (this) {
                if (smartCard == null) {
                    smartCard = smartCardRepository.load(smartCardId);
                }
            }
            return smartCard;
        }
        return local;
    }

    public void setSmartCard(SmartCard smartCard) {
        this.smartCard = smartCard;
        this.smartCardId = smartCard != null ? smartCard.getId() : null;
    }
    //endregion

    public int getNsiVersion() {
        return nsiVersion;
    }

    public void setNsiVersion(int nsiVersion) {
        this.nsiVersion = nsiVersion;
    }

}
