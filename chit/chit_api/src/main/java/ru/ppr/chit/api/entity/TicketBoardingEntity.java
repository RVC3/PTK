package ru.ppr.chit.api.entity;

/**
 * Посадка пассажира по билету
 *
 * @author Dmitry Nevolin
 */
public class TicketBoardingEntity {

    /**
     * Идентификатор билета
     */
    private TicketIdEntity ticketId;
    /**
     * Номер поезда
     */
    private String trainNumber;
    /**
     * Идентификатор нити поезда
     */
    private String trainThreadId;
    /**
     * Идентификатор терминала посадки
     */
    private String terminalDeviceId;
    /**
     * Логин контролера
     */
    private String operatorName;
    /**
     * Станция контроля ПД
     */
    private Long controlStationCode;
    /**
     * Параметр необходим для формирования Белого списка при отзыве ключа
     */
    private long edsKeyNumber;
    /**
     * Признак валидности ЭЦП
     */
    private boolean isEdsValid;
    /**
     * Признак "ПД в белом списке"
     */
    private boolean isInWhiteList;
    /**
     * Признак посадки по списку
     */
    private boolean isBoardingByList;
    /**
     * Код отказа по стоп-листу
     */
    private Long stopListRefusalCode;
    /**
     * Дата и время прохода/проверки в UTC
     */
    private String checkDateTimeUtc;
    /**
     * Признак «Посадка пассажира»
     */
    private boolean wasBoarded;
    /**
     * Данные билета
     */
    private TicketDataEntity ticketData;

    public TicketIdEntity getTicketId() {
        return ticketId;
    }

    public void setTicketId(TicketIdEntity ticketId) {
        this.ticketId = ticketId;
    }

    public String getTrainNumber() {
        return trainNumber;
    }

    public void setTrainNumber(String trainNumber) {
        this.trainNumber = trainNumber;
    }

    public String getTrainThreadId() {
        return trainThreadId;
    }

    public void setTrainThreadId(String trainThreadId) {
        this.trainThreadId = trainThreadId;
    }

    public String getTerminalDeviceId() {
        return terminalDeviceId;
    }

    public void setTerminalDeviceId(String terminalDeviceId) {
        this.terminalDeviceId = terminalDeviceId;
    }

    public String getOperatorName() {
        return operatorName;
    }

    public void setOperatorName(String operatorName) {
        this.operatorName = operatorName;
    }

    public Long getControlStationCode() {
        return controlStationCode;
    }

    public void setControlStationCode(Long controlStationCode) {
        this.controlStationCode = controlStationCode;
    }

    public long getEdsKeyNumber() {
        return edsKeyNumber;
    }

    public void setEdsKeyNumber(long edsKeyNumber) {
        this.edsKeyNumber = edsKeyNumber;
    }

    public boolean isEdsValid() {
        return isEdsValid;
    }

    public void setEdsValid(boolean edsValid) {
        isEdsValid = edsValid;
    }

    public boolean isInWhiteList() {
        return isInWhiteList;
    }

    public void setInWhiteList(boolean inWhiteList) {
        isInWhiteList = inWhiteList;
    }

    public boolean isBoardingByList() {
        return isBoardingByList;
    }

    public void setBoardingByList(boolean boardingByList) {
        isBoardingByList = boardingByList;
    }

    public Long getStopListRefusalCode() {
        return stopListRefusalCode;
    }

    public void setStopListRefusalCode(Long stopListRefusalCode) {
        this.stopListRefusalCode = stopListRefusalCode;
    }

    public String getCheckDateTimeUtc() {
        return checkDateTimeUtc;
    }

    public void setCheckDateTimeUtc(String checkDateTimeUtc) {
        this.checkDateTimeUtc = checkDateTimeUtc;
    }

    public boolean isWasBoarded() {
        return wasBoarded;
    }

    public void setWasBoarded(boolean wasBoarded) {
        this.wasBoarded = wasBoarded;
    }

    public TicketDataEntity getTicketData() {
        return ticketData;
    }

    public void setTicketData(TicketDataEntity ticketData) {
        this.ticketData = ticketData;
    }

}
