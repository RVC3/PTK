package ru.ppr.cppk.db.local;

import android.content.ContentValues;
import android.database.Cursor;
import android.support.annotation.NonNull;

import java.util.Date;

import ru.ppr.cppk.db.LocalDaoSession;
import ru.ppr.cppk.entity.event.model.CouponReadEvent;
import ru.ppr.database.references.ReferenceInfo;


/**
 * Класс для работы с сущность CouponReadEvent.
 *
 * @author Grigoriy Kashka
 */
public class CouponReadEventDao extends BaseEntityDao<CouponReadEvent, Long> {

    public static final String TABLE_NAME = "CouponReadEvent";

    public static class Properties {
        public static final String EventId = "EventId";
        public static final String CashRegisterWorkingShiftId = "CashRegisterWorkingShiftId";
        public static final String PreTicketNumber = "PreTicketNumber";
        public static final String PrintDateTime = "PrintDateTime";
        public static final String DeviceId = "DeviceId";
        public static final String StationCode = "StationCode";
        public static final String PtsKeyId = "PtsKeyId";
        public static final String Status = "Status";
    }

    public CouponReadEventDao(LocalDaoSession localDaoSession) {
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
    public CouponReadEvent fromCursor(Cursor cursor) {
        CouponReadEvent couponReadEvent = new CouponReadEvent();
        couponReadEvent.setId(cursor.getLong(cursor.getColumnIndex(BaseEntityDao.Properties.Id)));
        couponReadEvent.setEventId(cursor.getLong(cursor.getColumnIndex(Properties.EventId)));
        couponReadEvent.setShiftEventId(cursor.getLong(cursor.getColumnIndex(Properties.CashRegisterWorkingShiftId)));
        couponReadEvent.setPreTicketNumber(cursor.getLong(cursor.getColumnIndex(Properties.PreTicketNumber)));
        couponReadEvent.setPrintDateTime(new Date(cursor.getLong(cursor.getColumnIndex(Properties.PrintDateTime))));
        couponReadEvent.setDeviceId(cursor.getString(cursor.getColumnIndex(Properties.DeviceId)));
        couponReadEvent.setStationCode(cursor.getLong(cursor.getColumnIndex(Properties.StationCode)));
        couponReadEvent.setPtsKeyId(cursor.getString(cursor.getColumnIndex(Properties.PtsKeyId)));
        couponReadEvent.setStatus(CouponReadEvent.Status.fromCode(cursor.getInt(cursor.getColumnIndex(Properties.Status))));
        return couponReadEvent;
    }

    @Override
    public ContentValues toContentValues(@NonNull final CouponReadEvent couponReadEvent) {
        final ContentValues contentValues = new ContentValues();

        contentValues.put(Properties.EventId, couponReadEvent.getEventId());
        contentValues.put(Properties.CashRegisterWorkingShiftId, couponReadEvent.getShiftEventId());
        contentValues.put(Properties.PreTicketNumber, couponReadEvent.getPreTicketNumber());
        contentValues.put(Properties.PrintDateTime, couponReadEvent.getPrintDateTime().getTime());
        contentValues.put(Properties.DeviceId, couponReadEvent.getDeviceId());
        contentValues.put(Properties.StationCode, couponReadEvent.getStationCode());
        contentValues.put(Properties.PtsKeyId, couponReadEvent.getPtsKeyId());
        contentValues.put(Properties.Status, couponReadEvent.getStatus().getCode());

        return contentValues;
    }

    @Override
    public Long getKey(CouponReadEvent entity) {
        return entity.getId();
    }

    @Override
    public long insertOrThrow(@NonNull CouponReadEvent entity) {
        long id = super.insertOrThrow(entity);
        entity.setId(id);
        return id;
    }


}
