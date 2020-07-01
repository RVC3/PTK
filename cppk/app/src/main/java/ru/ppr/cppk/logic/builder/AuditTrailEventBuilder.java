package ru.ppr.cppk.logic.builder;

import android.support.annotation.NonNull;

import java.util.Date;

import ru.ppr.cppk.localdb.model.AuditTrailEventType;
import ru.ppr.cppk.localdb.model.AuditTrailEvent;

/**
 * Билдер сущности {@link AuditTrailEvent}.
 *
 * @author Aleksandr Brazhkin
 */
public class AuditTrailEventBuilder {
    private AuditTrailEventType type;
    private long extEventId;
    private Date operationTime;
    private long shiftEventId;
    private long monthEventId;

    public AuditTrailEventBuilder setType(AuditTrailEventType type) {
        this.type = type;
        return this;
    }

    public AuditTrailEventBuilder setExtEventId(long extEventId) {
        this.extEventId = extEventId;
        return this;
    }

    public AuditTrailEventBuilder setOperationTime(Date operationTime) {
        this.operationTime = operationTime;
        return this;
    }

    public AuditTrailEventBuilder setShiftEventId(long shiftEventId) {
        this.shiftEventId = shiftEventId;
        return this;
    }

    public AuditTrailEventBuilder setMonthEventId(long monthEventId) {
        this.monthEventId = monthEventId;
        return this;
    }

    @NonNull
    public AuditTrailEvent build() {
        if (type == null) {
            throw new NullPointerException("Type is null");
        }
        if (extEventId < 1) {
            throw new IllegalArgumentException("ExtEventId = 0");
        }
        if (operationTime == null) {
            throw new NullPointerException("OperationTime is null");
        }
        if (monthEventId < 1) {
            throw new NullPointerException("MonthEventId = 0");
        }

        AuditTrailEvent auditTrailEvent = new AuditTrailEvent();
        auditTrailEvent.setType(type);
        auditTrailEvent.setExtEventId(extEventId);
        auditTrailEvent.setOperationTime(operationTime);
        auditTrailEvent.setCashRegisterWorkingShiftId(shiftEventId);
        auditTrailEvent.setMonthEventId(monthEventId);

        return auditTrailEvent;
    }
}
