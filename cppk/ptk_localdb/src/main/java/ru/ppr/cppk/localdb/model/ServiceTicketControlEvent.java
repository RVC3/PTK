package ru.ppr.cppk.localdb.model;

import java.util.Date;

import ru.ppr.cppk.localdb.model.base.LocalModelWithId;
import ru.ppr.nsi.entity.TicketStorageType;

/**
 * Событие "Проход по служебной авторизационной карте".
 *
 * @author Aleksandr Brazhkin
 */
public class ServiceTicketControlEvent implements LocalModelWithId<Long> {
    /**
     * Primary key
     */
    private Long id;
    /**
     * Дата и время контроля
     */
    private Date controlDateTime;
    /**
     * Номер ключа ЭЦП
     */
    private long edsKeyNumber;
    /**
     * Код отказа по стоп-листу : 0 — нет отказа; >0 — код стоп-листа
     */
    private long stopListId;
    /**
     * Результат прохода по ПД / контроля ПД
     * Согласно справочнику НСИ «Описание результата прохода по ПД (контроля ПД)»
     */
    private ServiceTicketPassageResult validationResult;
    /**
     * Внешний номер БСК
     */
    private String cardNumber;
    /**
     * Номер кристалла (UID) БСК
     */
    private String cardCristalId;
    /**
     * Тпа БСК
     */
    private TicketStorageType ticketStorageType;
    /**
     * Дата начала действия
     */
    private Date validFrom;
    /**
     * Дата окончания действия
     */
    private Date validTo;
    /**
     * Вид зоны действия
     */
    private ServiceZoneType zoneType;
    /**
     * Код зоны действия
     */
    private int zoneValue;
    /**
     * True - разрешён проход и проезд
     * False - разрешён только проход
     */
    private boolean canTravel;
    /**
     * Персонифицированная
     */
    private boolean requirePersonification;
    /**
     * Требовать проверки документов
     */
    private boolean requireCheckDocument;
    /**
     * Порядковый номер СТУ
     */
    private int ticketNumber;
    /**
     * Дата и время инициализации БСК
     */
    private Date ticketWriteDateTime;
    /**
     * Счетчик использования карты
     */
    private int smartCardUsageCount;
    /**
     * Для ПТК - Результат контроля (валидная/невалидная служебная карта)
     * Для турникета - Признак успешного прохода
     */
    private boolean passageSign;
    /**
     * Статус события
     */
    private Status status;
    /**
     * Id связанной сущности из таблицы Event
     */
    private long eventId;
    /**
     * Id связанной сущности из таблицы CashRegisterWorkingShift
     */
    private long cashRegisterWorkingShiftId;
    /**
     * Идентификатор оборудования, которое записало СТУ
     */
    private long ticketDeviceId;

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public Date getControlDateTime() {
        return controlDateTime;
    }

    public void setControlDateTime(Date controlDateTime) {
        this.controlDateTime = controlDateTime;
    }

    public long getEdsKeyNumber() {
        return edsKeyNumber;
    }

    public void setEdsKeyNumber(long edsKeyNumber) {
        this.edsKeyNumber = edsKeyNumber;
    }

    public long getStopListId() {
        return stopListId;
    }

    public void setStopListId(long stopListId) {
        this.stopListId = stopListId;
    }

    public ServiceTicketPassageResult getValidationResult() {
        return validationResult;
    }

    public void setValidationResult(ServiceTicketPassageResult validationResult) {
        this.validationResult = validationResult;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public String getCardCristalId() {
        return cardCristalId;
    }

    public void setCardCristalId(String cardCristalId) {
        this.cardCristalId = cardCristalId;
    }

    public TicketStorageType getTicketStorageType() {
        return ticketStorageType;
    }

    public void setTicketStorageType(TicketStorageType ticketStorageType) {
        this.ticketStorageType = ticketStorageType;
    }

    public Date getValidFrom() {
        return validFrom;
    }

    public void setValidFrom(Date validFrom) {
        this.validFrom = validFrom;
    }

    public Date getValidTo() {
        return validTo;
    }

    public void setValidTo(Date validTo) {
        this.validTo = validTo;
    }

    public ServiceZoneType getZoneType() {
        return zoneType;
    }

    public void setZoneType(ServiceZoneType zoneType) {
        this.zoneType = zoneType;
    }

    public int getZoneValue() {
        return zoneValue;
    }

    public void setZoneValue(int zoneValue) {
        this.zoneValue = zoneValue;
    }

    public boolean isCanTravel() {
        return canTravel;
    }

    public void setCanTravel(boolean canTravel) {
        this.canTravel = canTravel;
    }

    public boolean isRequirePersonification() {
        return requirePersonification;
    }

    public void setRequirePersonification(boolean requirePersonification) {
        this.requirePersonification = requirePersonification;
    }

    public boolean isRequireCheckDocument() {
        return requireCheckDocument;
    }

    public void setRequireCheckDocument(boolean requireCheckDocument) {
        this.requireCheckDocument = requireCheckDocument;
    }

    public int getTicketNumber() {
        return ticketNumber;
    }

    public void setTicketNumber(int ticketNumber) {
        this.ticketNumber = ticketNumber;
    }

    public Date getTicketWriteDateTime() {
        return ticketWriteDateTime;
    }

    public void setTicketWriteDateTime(Date ticketWriteDateTime) {
        this.ticketWriteDateTime = ticketWriteDateTime;
    }

    public int getSmartCardUsageCount() {
        return smartCardUsageCount;
    }

    public void setSmartCardUsageCount(int smartCardUsageCount) {
        this.smartCardUsageCount = smartCardUsageCount;
    }

    public boolean isPassageSign() {
        return passageSign;
    }

    public void setPassageSign(boolean passageSign) {
        this.passageSign = passageSign;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public long getEventId() {
        return eventId;
    }

    public void setEventId(long eventId) {
        this.eventId = eventId;
    }

    public long getCashRegisterWorkingShiftId() {
        return cashRegisterWorkingShiftId;
    }

    public void setCashRegisterWorkingShiftId(long cashRegisterWorkingShiftId) {
        this.cashRegisterWorkingShiftId = cashRegisterWorkingShiftId;
    }

    public long getTicketDeviceId() {
        return ticketDeviceId;
    }

    public void setTicketDeviceId(long ticketDeviceId) {
        this.ticketDeviceId = ticketDeviceId;
    }

    /**
     * Статус события
     */
    public enum Status {
        /**
         * Создано в БД, может быть свободно удалено.
         */
        CREATED(0),
        /**
         * Событие полностью сформировано.
         */
        COMPLETED(10);

        private final int code;

        Status(int code) {
            this.code = code;
        }

        public int getCode() {
            return code;
        }

        public static Status valueOf(int code) {
            for (Status item : values()) {
                if (item.code == code) {
                    return item;
                }
            }
            throw new IllegalArgumentException("Unknown code = " + code);
        }
    }
}
