package ru.ppr.chit.api.entity;

/**
 * Билет
 *
 * @author Dmitry Nevolin
 */
public class TicketEntity {

    /**
     * Идентификатор билета
     */
    private TicketIdEntity id;
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
     * Код станции назначения
     */
    private Long destinationStationCode;
    /**
     * Время отправления
     */
    private String departureDateTimeUtc;
    /**
     * Код типа билета
     */
    private Long ticketTypeCode;
    /**
     * Код льготы или null
     */
    private Integer exemptionExpressCode;
    /**
     * Тип оформления билета
     */
    private TicketIssueTypeEntity issueType;
    /**
     * Статус билета
     */
    private TicketStateEntity ticketState;
    /**
     * Дата и время последнего изменения статуса в UTC
     */
    private String stateDateTimeUtc;
    /**
     * Информация о пассажире
     */
    private PassengerPersonalDataEntity passenger;
    /**
     * Место в поезде или null, если билет без места
     */
    private PlaceLocationEntity placeLocation;
    /**
     * Старое место в поезде, если была смена места или null, если смены не было или билет без места
     */
    private PlaceLocationEntity oldPlaceLocation;
    /**
     * Идентификатор версии НСИ
     */
    private int rdsVersion;

    public TicketIdEntity getId() {
        return id;
    }

    public void setId(TicketIdEntity id) {
        this.id = id;
    }

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

    public Long getTicketTypeCode() {
        return ticketTypeCode;
    }

    public void setTicketTypeCode(Long ticketTypeCode) {
        this.ticketTypeCode = ticketTypeCode;
    }

    public Integer getExemptionExpressCode() {
        return exemptionExpressCode;
    }

    public void setExemptionExpressCode(Integer exemptionExpressCode) {
        this.exemptionExpressCode = exemptionExpressCode;
    }

    public TicketIssueTypeEntity getIssueType() {
        return issueType;
    }

    public void setIssueType(TicketIssueTypeEntity issueType) {
        this.issueType = issueType;
    }

    public TicketStateEntity getTicketState() {
        return ticketState;
    }

    public void setTicketState(TicketStateEntity ticketState) {
        this.ticketState = ticketState;
    }

    public String getStateDateTimeUtc() {
        return stateDateTimeUtc;
    }

    public void setStateDateTimeUtc(String stateDateTimeUtc) {
        this.stateDateTimeUtc = stateDateTimeUtc;
    }

    public PassengerPersonalDataEntity getPassenger() {
        return passenger;
    }

    public void setPassenger(PassengerPersonalDataEntity passenger) {
        this.passenger = passenger;
    }

    public PlaceLocationEntity getPlaceLocation() {
        return placeLocation;
    }

    public void setPlaceLocation(PlaceLocationEntity placeLocation) {
        this.placeLocation = placeLocation;
    }

    public PlaceLocationEntity getOldPlaceLocation() {
        return oldPlaceLocation;
    }

    public void setOldPlaceLocation(PlaceLocationEntity oldPlaceLocation) {
        this.oldPlaceLocation = oldPlaceLocation;
    }

    public int getRdsVersion() {
        return rdsVersion;
    }

    public void setRdsVersion(int rdsVersion) {
        this.rdsVersion = rdsVersion;
    }

}
