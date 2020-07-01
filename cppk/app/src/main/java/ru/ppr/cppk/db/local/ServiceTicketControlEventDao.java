package ru.ppr.cppk.db.local;

import android.content.ContentValues;
import android.database.Cursor;
import android.support.annotation.NonNull;

import java.util.Date;

import ru.ppr.cppk.db.LocalDaoSession;
import ru.ppr.cppk.localdb.model.ServiceTicketControlEvent;
import ru.ppr.cppk.localdb.model.ServiceTicketPassageResult;
import ru.ppr.cppk.localdb.model.ServiceZoneType;
import ru.ppr.database.references.ReferenceInfo;
import ru.ppr.nsi.entity.TicketStorageType;

/**
 * Dao для сущности {@link ServiceTicketControlEvent} в локальной БД
 *
 * @author Grigoriy Kashka
 */
public class ServiceTicketControlEventDao extends BaseEntityDao<ServiceTicketControlEvent, Long> {

    public static final String TABLE_NAME = "ServiceTicketControlEvent";

    public static class Properties {
        public static final String EventId = "EventId";
        public static final String CashRegisterWorkingShiftId = "CashRegisterWorkingShiftId";
        public static final String ControlDateTime = "ControlDateTime";
        public static final String EdsKeyNumber = "EdsKeyNumber";
        public static final String StopListId = "StopListId";
        public static final String ValidationResult = "ValidationResult";
        public static final String CardNumber = "CardNumber";
        public static final String CardCristalId = "CardCristalId";
        public static final String TicketStorageType = "TicketStorageType";
        public static final String ValidFrom = "ValidFrom";
        public static final String ValidTo = "ValidTo";
        public static final String ZoneType = "ZoneType";
        public static final String ZoneValue = "ZoneValue";
        public static final String CanTravel = "CanTravel";
        public static final String RequirePersonification = "RequirePersonification";
        public static final String RequireCheckDocument = "RequireCheckDocument";
        public static final String TicketNumber = "TicketNumber";
        public static final String TicketWriteDateTime = "TicketWriteDateTime";
        public static final String SmartCardUsageCount = "SmartCardUsageCount";
        public static final String PassageSign = "PassageSign";
        public static final String TicketDeviceId = "TicketDeviceId";
        public static final String Status = "Status";
    }

    public ServiceTicketControlEventDao(LocalDaoSession localDaoSession) {
        super(localDaoSession);

        // Регистрируем ссылки таблицы
        registerReference(Properties.EventId, EventDao.TABLE_NAME, ReferenceInfo.ReferencesType.CASCADE);
        registerReference(Properties.CashRegisterWorkingShiftId, ShiftEventDao.TABLE_NAME, ReferenceInfo.ReferencesType.NO_ACTION);
    }

    @Override
    public String getTableName() {
        return TABLE_NAME;
    }

    @Override
    public ServiceTicketControlEvent fromCursor(@NonNull final Cursor cursor) {
        final ServiceTicketControlEvent event = new ServiceTicketControlEvent();
        event.setId(cursor.getLong(cursor.getColumnIndex(BaseEntityDao.Properties.Id)));
        event.setEventId(cursor.getLong(cursor.getColumnIndex(Properties.EventId)));
        event.setCashRegisterWorkingShiftId(cursor.getLong(cursor.getColumnIndex(Properties.CashRegisterWorkingShiftId)));
        event.setControlDateTime(new Date(cursor.getLong(cursor.getColumnIndex(Properties.ControlDateTime))));
        event.setEdsKeyNumber(cursor.getLong(cursor.getColumnIndex(Properties.EdsKeyNumber)));
        event.setStopListId(cursor.getLong(cursor.getColumnIndex(Properties.StopListId)));
        int validationResultIndex = cursor.getColumnIndex(Properties.ValidationResult);
        if (!cursor.isNull(validationResultIndex)) {
            event.setValidationResult(ServiceTicketPassageResult.valueOf(cursor.getInt(validationResultIndex)));
        }
        event.setCardNumber(cursor.getString(cursor.getColumnIndex(Properties.CardNumber)));
        event.setCardCristalId(cursor.getString(cursor.getColumnIndex(Properties.CardCristalId)));
        event.setTicketStorageType(TicketStorageType.getTypeByDBCode(cursor.getInt(cursor.getColumnIndex(Properties.TicketStorageType))));
        event.setValidFrom(new Date(cursor.getLong(cursor.getColumnIndex(Properties.ValidFrom))));
        int validToIndex = cursor.getColumnIndex(Properties.ValidTo);
        if (!cursor.isNull(validToIndex)) {
            event.setValidTo(new Date(cursor.getLong(validToIndex)));
        }
        int zoneTypeIndex = cursor.getColumnIndex(Properties.ZoneType);
        if (!cursor.isNull(zoneTypeIndex)) {
            event.setZoneType(ServiceZoneType.valueOf(cursor.getInt(zoneTypeIndex)));
        }
        event.setZoneValue(cursor.getInt(cursor.getColumnIndex(Properties.ZoneValue)));
        event.setCanTravel(cursor.getInt(cursor.getColumnIndex(Properties.CanTravel)) == 1);
        event.setRequirePersonification(cursor.getInt(cursor.getColumnIndex(Properties.RequirePersonification)) == 1);
        event.setRequireCheckDocument(cursor.getInt(cursor.getColumnIndex(Properties.RequireCheckDocument)) == 1);
        event.setTicketNumber(cursor.getInt(cursor.getColumnIndex(Properties.TicketNumber)));
        int ticketWriteDateTimeIndex = cursor.getColumnIndex(Properties.TicketWriteDateTime);
        if (!cursor.isNull(ticketWriteDateTimeIndex)) {
            event.setTicketWriteDateTime(new Date(cursor.getLong(ticketWriteDateTimeIndex)));
        }
        event.setSmartCardUsageCount(cursor.getInt(cursor.getColumnIndex(Properties.SmartCardUsageCount)));
        event.setPassageSign(cursor.getInt(cursor.getColumnIndex(Properties.PassageSign)) == 1);
        event.setTicketDeviceId(cursor.getLong(cursor.getColumnIndex(Properties.TicketDeviceId)));
        event.setStatus(ServiceTicketControlEvent.Status.valueOf(cursor.getInt(cursor.getColumnIndex(Properties.Status))));
        return event;
    }

    @Override
    public ContentValues toContentValues(@NonNull final ServiceTicketControlEvent entity) {
        final ContentValues contentValues = new ContentValues();
        contentValues.put(Properties.EventId, entity.getEventId());
        contentValues.put(Properties.CashRegisterWorkingShiftId, entity.getCashRegisterWorkingShiftId());
        contentValues.put(Properties.ControlDateTime, entity.getControlDateTime().getTime());
        contentValues.put(Properties.EdsKeyNumber, entity.getEdsKeyNumber());
        contentValues.put(Properties.StopListId, entity.getStopListId());
        contentValues.put(Properties.ValidationResult, entity.getValidationResult() == null ? null : entity.getValidationResult().getCode());
        contentValues.put(Properties.CardNumber, entity.getCardNumber());
        contentValues.put(Properties.CardCristalId, entity.getCardCristalId());
        contentValues.put(Properties.TicketStorageType, entity.getTicketStorageType().getDBCode());
        contentValues.put(Properties.ValidFrom, entity.getValidFrom().getTime());
        contentValues.put(Properties.ValidTo, entity.getValidTo() == null ? null : entity.getValidTo().getTime());
        contentValues.put(Properties.ZoneType, entity.getZoneType() == null ? null : entity.getZoneType().getCode());
        contentValues.put(Properties.ZoneValue, entity.getZoneValue());
        contentValues.put(Properties.CanTravel, entity.isCanTravel() ? 1 : 0);
        contentValues.put(Properties.RequirePersonification, entity.isRequirePersonification() ? 1 : 0);
        contentValues.put(Properties.RequireCheckDocument, entity.isRequireCheckDocument() ? 1 : 0);
        contentValues.put(Properties.TicketNumber, entity.getTicketNumber());
        contentValues.put(Properties.TicketWriteDateTime, entity.getTicketWriteDateTime() == null ? null : entity.getTicketWriteDateTime().getTime());
        contentValues.put(Properties.SmartCardUsageCount, entity.getSmartCardUsageCount());
        contentValues.put(Properties.PassageSign, entity.isPassageSign() ? 1 : 0);
        contentValues.put(Properties.TicketDeviceId, entity.getTicketDeviceId());
        contentValues.put(Properties.Status, entity.getStatus().getCode());
        return contentValues;
    }

    @Override
    public Long getKey(ServiceTicketControlEvent entity) {
        return entity.getId();
    }

    @Override
    public long insertOrThrow(@NonNull ServiceTicketControlEvent entity) {
        long id = super.insertOrThrow(entity);
        entity.setId(id);
        return id;
    }

}
