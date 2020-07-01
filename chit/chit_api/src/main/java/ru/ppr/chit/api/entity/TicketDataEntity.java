package ru.ppr.chit.api.entity;

/**
 * Данные билета
 *
 * @author Dmitry Nevolin
 */
public class TicketDataEntity {

    /**
     * Вид ПД
     */
    private Long ticketTypeCode;
    /**
     * Информация о пассажире
     */
    private PassengerEntity passenger;
    /**
     * Место в поезде
     */
    private LocationEntity location;
    /**
     * Время отправления
     */
    private String departureDateTimeUtc;
    /**
     * Код станции отправления
     */
    private Long departureStationCode;
    /**
     * Код станции назначения
     */
    private Long destinationStationCode;
    /**
     * Тариф
     */
    private Long tariffId;
    /**
     * Код льготы или <c>null</c>.
     */
    private Integer exemptionExpressCode;
    /**
     * Информация по БСК
     */
    private SmartCardEntity smartCard;
    /**
     * Версия НСИ
     */
    private int rdsVersionId;

    public Long getTicketTypeCode() {
        return ticketTypeCode;
    }

    public void setTicketTypeCode(Long ticketTypeCode) {
        this.ticketTypeCode = ticketTypeCode;
    }

    public PassengerEntity getPassenger() {
        return passenger;
    }

    public void setPassenger(PassengerEntity passenger) {
        this.passenger = passenger;
    }

    public LocationEntity getLocation() {
        return location;
    }

    public void setLocation(LocationEntity location) {
        this.location = location;
    }

    public String getDepartureDateTimeUtc() {
        return departureDateTimeUtc;
    }

    public void setDepartureDateTimeUtc(String departureDateTimeUtc) {
        this.departureDateTimeUtc = departureDateTimeUtc;
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

    public SmartCardEntity getSmartCard() {
        return smartCard;
    }

    public void setSmartCard(SmartCardEntity smartCard) {
        this.smartCard = smartCard;
    }

    public int getRdsVersionId() {
        return rdsVersionId;
    }

    public void setRdsVersionId(int rdsVersionId) {
        this.rdsVersionId = rdsVersionId;
    }

}
