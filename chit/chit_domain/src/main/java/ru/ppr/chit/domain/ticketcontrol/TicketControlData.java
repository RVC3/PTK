package ru.ppr.chit.domain.ticketcontrol;

import java.util.Date;
import java.util.List;

import ru.ppr.chit.domain.model.nsi.TicketStorageType;

/**
 * Информация о контроле ПД.
 *
 * @author Aleksandr Brazhkin
 */
public class TicketControlData {
    /**
     * Порядковый номер документа
     */
    private long ticketNumber;
    /**
     * Код типа ПД
     */
    private long ticketTypeCode;
    /**
     * Тип источника данных
     */
    private DataCarrierType dataCarrierType;
    /**
     * Дата продажи ПД
     */
    private Date saleDateTime;
    /**
     * Даты отправления
     */
    private List<Date> departureDates;
    /**
     * Номер поезда
     */
    private String trainNumber;
    /**
     * Код станции отправления
     */
    private long departureStationCode;
    /**
     * Код станции прибытия
     */
    private long destinationStationCode;
    /**
     * 4-х значный код льготы
     */
    private Integer exemptionExpressCode;
    /**
     * Номер вагона
     */
    private String carNumber;
    /**
     * Номер места
     */
    private String seatNumber;
    /**
     * Фамилия пассажира
     */
    private String lastName;
    /**
     * Имя пассажира
     */
    private String firstName;
    /**
     * Отчество пассажира
     */
    private String secondName;
    /**
     * Код типа документа, удостовреяющиего личность
     */
    private long documentTypeCode;
    /**
     * Номер документа, удостовреяющиего личность
     */
    private String documentNumber;
    /**
     * Номер ключа ЭЦП
     */
    private long edsKeyNumber;
    /**
     * Внешний номер БСК
     */
    private String cardOuterNumber;
    /**
     * Номер кристалла БСК
     */
    private String cardCrystalSerialNumber;
    /**
     * Тип носителя данных
     */
    private TicketStorageType ticketStorageType;
    /**
     * Id устройства продажи ПД
     */
    private long deviceId;
    /**
     * Флаг наличия ПД в белом списке
     */
    private boolean inWhiteList;
    /**
     * Признак валидности ЭЦП
     */
    private boolean edsValid;

    public long getTicketNumber() {
        return ticketNumber;
    }

    public void setTicketNumber(long ticketNumber) {
        this.ticketNumber = ticketNumber;
    }

    public long getTicketTypeCode() {
        return ticketTypeCode;
    }

    public void setTicketTypeCode(long ticketTypeCode) {
        this.ticketTypeCode = ticketTypeCode;
    }

    public DataCarrierType getDataCarrierType() {
        return dataCarrierType;
    }

    public void setDataCarrierType(DataCarrierType dataCarrierType) {
        this.dataCarrierType = dataCarrierType;
    }

    public Date getSaleDateTime() {
        return saleDateTime;
    }

    public void setSaleDateTime(Date saleDateTime) {
        this.saleDateTime = saleDateTime;
    }

    public List<Date> getDepartureDates() {
        return departureDates;
    }

    public void setDepartureDates(List<Date> departureDates) {
        this.departureDates = departureDates;
    }

    public String getTrainNumber() {
        return trainNumber;
    }

    public void setTrainNumber(String trainNumber) {
        this.trainNumber = trainNumber;
    }

    public long getDepartureStationCode() {
        return departureStationCode;
    }

    public void setDepartureStationCode(long departureStationCode) {
        this.departureStationCode = departureStationCode;
    }

    public long getDestinationStationCode() {
        return destinationStationCode;
    }

    public void setDestinationStationCode(long destinationStationCode) {
        this.destinationStationCode = destinationStationCode;
    }

    public Integer getExemptionExpressCode() {
        return exemptionExpressCode;
    }

    public void setExemptionExpressCode(Integer exemptionExpressCode) {
        this.exemptionExpressCode = exemptionExpressCode;
    }

    public String getCarNumber() {
        return carNumber;
    }

    public void setCarNumber(String carNumber) {
        this.carNumber = carNumber;
    }

    public String getSeatNumber() {
        return seatNumber;
    }

    public void setSeatNumber(String seatNumber) {
        this.seatNumber = seatNumber;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getSecondName() {
        return secondName;
    }

    public void setSecondName(String secondName) {
        this.secondName = secondName;
    }

    public long getDocumentTypeCode() {
        return documentTypeCode;
    }

    public void setDocumentTypeCode(long documentTypeCode) {
        this.documentTypeCode = documentTypeCode;
    }

    public String getDocumentNumber() {
        return documentNumber;
    }

    public void setDocumentNumber(String documentNumber) {
        this.documentNumber = documentNumber;
    }

    public long getEdsKeyNumber() {
        return edsKeyNumber;
    }

    public void setEdsKeyNumber(long edsKeyNumber) {
        this.edsKeyNumber = edsKeyNumber;
    }

    public String getCardOuterNumber() {
        return cardOuterNumber;
    }

    public void setCardOuterNumber(String cardOuterNumber) {
        this.cardOuterNumber = cardOuterNumber;
    }

    public String getCardCrystalSerialNumber() {
        return cardCrystalSerialNumber;
    }

    public void setCardCrystalSerialNumber(String cardCrystalSerialNumber) {
        this.cardCrystalSerialNumber = cardCrystalSerialNumber;
    }

    public TicketStorageType getTicketStorageType() {
        return ticketStorageType;
    }

    public void setTicketStorageType(TicketStorageType ticketStorageType) {
        this.ticketStorageType = ticketStorageType;
    }

    public long getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(long deviceId) {
        this.deviceId = deviceId;
    }

    public boolean isInWhiteList() {
        return inWhiteList;
    }

    public void setInWhiteList(boolean inWhiteList) {
        this.inWhiteList = inWhiteList;
    }

    public boolean isEdsValid() {
        return edsValid;
    }

    public void setEdsValid(boolean edsValid) {
        this.edsValid = edsValid;
    }
}
