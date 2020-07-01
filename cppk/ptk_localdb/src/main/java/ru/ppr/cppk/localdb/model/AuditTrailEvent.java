package ru.ppr.cppk.localdb.model;

import java.util.Date;

import ru.ppr.cppk.localdb.model.base.LocalModelWithId;

/**
 * Событие контрольного журнала.
 *
 * @author Aleksandr Brazhkin
 */
public class AuditTrailEvent implements LocalModelWithId<Long> {
    /**
     * Идентификатор события
     */
    private Long id;
    /**
     * Тип события
     */
    private AuditTrailEventType type;
    /**
     * Идентификатор самого события (продажи, печати отчета, ...) во внешней таблице
     */
    private long extEventId;
    /**
     * Время выполнения операции
     */
    private Date operationTime;
    /**
     * Идентификатор смены
     */
    private long cashRegisterWorkingShiftId;
    /**
     * Идентификатор месяца
     */
    private long monthEventId;

    public AuditTrailEvent() {

    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public AuditTrailEventType getType() {
        return type;
    }

    public void setType(AuditTrailEventType type) {
        this.type = type;
    }

    public long getExtEventId() {
        return extEventId;
    }

    public void setExtEventId(long extEventId) {
        this.extEventId = extEventId;
    }

    public Date getOperationTime() {
        return operationTime;
    }

    public void setOperationTime(Date operationTime) {
        this.operationTime = operationTime;
    }

    public long getCashRegisterWorkingShiftId() {
        return cashRegisterWorkingShiftId;
    }

    public void setCashRegisterWorkingShiftId(long cashRegisterWorkingShiftId) {
        this.cashRegisterWorkingShiftId = cashRegisterWorkingShiftId;
    }

    public long getMonthEventId() {
        return monthEventId;
    }

    public void setMonthEventId(long monthEventId) {
        this.monthEventId = monthEventId;
    }
}
