package ru.ppr.chit.domain.model.local;

import java.util.Date;

import ru.ppr.chit.domain.model.local.base.LocalModelWithId;
import ru.ppr.chit.domain.repository.local.TicketDataRepository;
import ru.ppr.chit.domain.repository.local.TicketIdRepository;
import ru.ppr.utils.ObjectUtils;

/**
 * Посадка пассажира по билету
 *
 * @author Dmitry Nevolin
 */
public class TicketBoarding implements LocalModelWithId<Long> {

    /**
     * Идентификатор
     */
    private Long id;
    /**
     * Идентификатор ticketId
     */
    private Long ticketIdId;
    /**
     * Ключ билета
     */
    private TicketId ticketId;
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
    private boolean edsValid;
    /**
     * Признак "ПД в белом списке"
     */
    private boolean inWhiteList;
    /**
     * Признак посадки по списку
     */
    private boolean boardingByList;
    /**
     * Код отказа по стоп-листу
     */
    private Long stopListRefusalCode;
    /**
     * Дата и время прохода/проверки в UTC
     */
    private Date checkDate;
    /**
     * Признак «Посадка пассажира»
     */
    private boolean wasBoarded;
    /**
     * Идентификатор данных билета
     */
    private Long ticketDataId;
    /**
     * Данные билета
     */
    private TicketData ticketData;

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
        return edsValid;
    }

    public void setEdsValid(boolean edsValid) {
        this.edsValid = edsValid;
    }

    public boolean isInWhiteList() {
        return inWhiteList;
    }

    public void setInWhiteList(boolean inWhiteList) {
        this.inWhiteList = inWhiteList;
    }

    public boolean isBoardingByList() {
        return boardingByList;
    }

    public void setBoardingByList(boolean boardingByList) {
        this.boardingByList = boardingByList;
    }

    public Long getStopListRefusalCode() {
        return stopListRefusalCode;
    }

    public void setStopListRefusalCode(Long stopListRefusalCode) {
        this.stopListRefusalCode = stopListRefusalCode;
    }

    public Date getCheckDate() {
        return checkDate;
    }

    public void setCheckDate(Date checkDate) {
        this.checkDate = checkDate;
    }

    public boolean isWasBoarded() {
        return wasBoarded;
    }

    public void setWasBoarded(boolean wasBoarded) {
        this.wasBoarded = wasBoarded;
    }

    //region Device getters and setters
    public Long getTicketDataId() {
        return ticketDataId;
    }

    public void setTicketDataId(Long ticketDataId) {
        this.ticketDataId = ticketDataId;
        if (this.ticketData != null && !ObjectUtils.equals(this.ticketData.getId(), ticketDataId)) {
            this.ticketData = null;
        }
    }

    public TicketData getTicketData(TicketDataRepository ticketDataRepository) {
        TicketData local = ticketData;
        if (local == null && ticketDataId != null) {
            synchronized (this) {
                if (ticketData == null) {
                    ticketData = ticketDataRepository.load(ticketDataId);
                }
            }
            return ticketData;
        }
        return local;
    }

    public void setTicketData(TicketData ticketData) {
        this.ticketData = ticketData;
        this.ticketDataId = ticketData != null ? ticketData.getId() : null;
    }
    //endregion

}
