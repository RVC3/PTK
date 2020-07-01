package ru.ppr.chit.ui.activity.passengerlist.model;

/**
 * Информация о пассажаре.
 *
 * @author Aleksandr Brazhkin
 */
public class PassengerInfo {
    /**
     * Фамилия
     */
    private String fio;
    /**
     * Номер документа
     */
    private String documentNumber;
    /**
     * Тип документа
     */
    private String documentType;
    /**
     * Id билета
     */
    private long ticketId;
    /**
     * Станция посадки
     */
    private Long departureStationCode;
    /**
     * Признак посадки
     */
    private boolean wasBoarded;
    /**
     * Признак что станция посадки совпадает с текущей
     */
    private boolean isCurrentStationBoarding;

    public String getFio() {
        return fio;
    }

    public void setFio(String fio) {
        this.fio = fio;
    }

    public String getDocumentNumber() {
        return documentNumber;
    }

    public void setDocumentNumber(String documentNumber) {
        this.documentNumber = documentNumber;
    }

    public String getDocumentType() {
        return documentType;
    }

    public void setDocumentType(String documentType) {
        this.documentType = documentType;
    }

    public long getTicketId() {
        return ticketId;
    }

    public void setTicketId(long ticketId) {
        this.ticketId = ticketId;
    }

    public void setDepartureStationCode(Long departureStationCode){
        this.departureStationCode = departureStationCode;
    }

    public Long getDepartureStationCode(){
        return departureStationCode;
    }

    public void setWasBoarded(boolean wasBoarded){
        this.wasBoarded = wasBoarded;
    }

    public void setIsCurrentStationBoarding(boolean value){
        this.isCurrentStationBoarding = value;
    }

    public boolean getIsCurrentStationBoarding(){
        return isCurrentStationBoarding;
    }

    public boolean getWasBoarded(){
        return wasBoarded;
    }
}
