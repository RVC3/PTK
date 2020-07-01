package ru.ppr.core.dataCarrier.pd.base;

import ru.ppr.core.dataCarrier.pd.PdVersion;

/**
 * Базовый класс для ПД с местом.
 *
 * @author Aleksandr Brazhkin
 */
public abstract class BasePdWithPlace extends BaseRealPd implements PdWithPlace {
    /**
     * Код типа ПД для ПД с местом, с использованием которого сформирован ПД, в соответствии с НСИ
     */
    private long ticketTypeCode;
    /**
     * Код станции отправления, в соответствии с НСИ
     */
    private long departureStationCode;
    /**
     * Код станции прибытия, в соответствии с НСИ
     */
    private long destinationStationCode;
    /**
     * Номер поезда
     */
    private int trainNumber;
    /**
     * Литера поезда
     */
    private String trainLetter;
    /**
     * Номер места в вагоне. В текущих вагонах до 64 мест.
     */
    private int placeNumber;
    /**
     * Литера места
     */
    private String placeLetter;
    /**
     * Номер вагона. От 0 до 63.
     */
    private int wagonNumber;
    /**
     * Дата отправления: количество дней с даты продажи. Значение 0: дата отправления соответствует дате продажи.
     */
    private int departureDayOffset;
    /**
     * Время отправления с точностью до минуты - количество минут с полуночи. Минимум: 0 - 00:00, максимум: 1439 - 23:59
     */
    private int departureTime;
    /**
     * Код типа документа, удостовреяющиего личность. Наименование документа определяется по НСИ.
     */
    private int documentTypeCode;
    /**
     * Последние 4 цифры/символа номера документа.
     */
    private String documentNumber;
    /**
     * Фамилия, до 28 символов
     */
    private String lastName;
    /**
     * Первая буква имени
     */
    private String firstNameInitial;
    /**
     * Первая буква отчества
     */
    private String secondNameInitial;
    /**
     * Код льготы
     */
    private int exemptionCode;

    public BasePdWithPlace(PdVersion version, int size) {
        super(version, size);
    }

    @Override
    public long getTicketTypeCode() {
        return ticketTypeCode;
    }

    public void setTicketTypeCode(long ticketTypeCode) {
        this.ticketTypeCode = ticketTypeCode;
    }

    @Override
    public long getDepartureStationCode() {
        return departureStationCode;
    }

    public void setDepartureStationCode(long departureStationCode) {
        this.departureStationCode = departureStationCode;
    }

    @Override
    public long getDestinationStationCode() {
        return destinationStationCode;
    }

    public void setDestinationStationCode(long destinationStationCode) {
        this.destinationStationCode = destinationStationCode;
    }

    @Override
    public int getTrainNumber() {
        return trainNumber;
    }

    public void setTrainNumber(int trainNumber) {
        this.trainNumber = trainNumber;
    }

    @Override
    public String getTrainLetter() {
        return trainLetter;
    }

    public void setTrainLetter(String trainLetter) {
        this.trainLetter = trainLetter;
    }

    @Override
    public int getPlaceNumber() {
        return placeNumber;
    }

    public void setPlaceNumber(int placeNumber) {
        this.placeNumber = placeNumber;
    }

    @Override
    public String getPlaceLetter() {
        return placeLetter;
    }

    public void setPlaceLetter(String placeLetter) {
        this.placeLetter = placeLetter;
    }

    @Override
    public int getWagonNumber() {
        return wagonNumber;
    }

    public void setWagonNumber(int wagonNumber) {
        this.wagonNumber = wagonNumber;
    }

    @Override
    public int getDepartureDayOffset() {
        return departureDayOffset;
    }

    public void setDepartureDayOffset(int departureDayOffset) {
        this.departureDayOffset = departureDayOffset;
    }

    @Override
    public int getDepartureTime() {
        return departureTime;
    }

    public void setDepartureTime(int departureTime) {
        this.departureTime = departureTime;
    }

    @Override
    public int getDocumentTypeCode() {
        return documentTypeCode;
    }

    public void setDocumentTypeCode(int documentTypeCode) {
        this.documentTypeCode = documentTypeCode;
    }

    @Override
    public String getDocumentNumber() {
        return documentNumber;
    }

    public void setDocumentNumber(String documentNumber) {
        this.documentNumber = documentNumber;
    }

    @Override
    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    @Override
    public String getFirstNameInitial() {
        return firstNameInitial;
    }

    public void setFirstNameInitial(String firstNameInitial) {
        this.firstNameInitial = firstNameInitial;
    }

    @Override
    public String getSecondNameInitial() {
        return secondNameInitial;
    }

    public void setSecondNameInitial(String secondNameInitial) {
        this.secondNameInitial = secondNameInitial;
    }

    @Override
    public int getExemptionCode() {
        return exemptionCode;
    }

    public void setExemptionCode(int exemptionCode) {
        this.exemptionCode = exemptionCode;
    }
}
