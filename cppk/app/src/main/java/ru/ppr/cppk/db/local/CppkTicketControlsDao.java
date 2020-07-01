package ru.ppr.cppk.db.local;

import android.content.ContentValues;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;

import ru.ppr.cppk.dataCarrier.pd.check.control.PassageResult;
import ru.ppr.cppk.db.LocalDaoSession;
import ru.ppr.cppk.db.SqlQueryBuilder;
import ru.ppr.cppk.entity.event.base34.CPPKTicketControl;
import ru.ppr.cppk.localdb.model.ShiftEvent;
import ru.ppr.database.SqLiteUtils;
import ru.ppr.database.references.ReferenceInfo;
import ru.ppr.logger.Logger;

/**
 * DAO для таблицы локальной БД <i>CPPKTicketControl</i>.
 */
public class CppkTicketControlsDao extends BaseEntityDao<CPPKTicketControl, Long> {

    private static final String TAG = Logger.makeLogTag(CppkTicketControlsDao.class);

    public static final String TABLE_NAME = "CPPKTicketControl";

    public static class Properties {
        public static final String EventId = "EventId";
        public static final String TicketEventBaseId = "TicketEventBaseId";
        public static final String ControlDateTime = "ControlDateTime";
        public static final String EdsKeyNumber = "EdsKeyNumber";
        public static final String IsRevokedEds = "IsRevokedEds";
        public static final String StopListId = "StopListId";
        public static final String ExemptionCode = "ExemptionCode";
        public static final String ParentTicketId = "ParentTicketId";
        public static final String ValidationResult = "ValidationResult";
        public static final String TripsSpend = "TripsSpend";
        public static final String TicketNumber = "TicketNumber";
        public static final String Trips7000Spend = "Trips7000Spend";
        public static final String TripsCount = "TripsCount";
        public static final String Trips7000Count = "Trips7000Count";
        public static final String SellTicketDeviceId = "SellTicketDeviceId";
        public static final String IsRestoredTicket = "IsRestoredTicket";
        public static final String TransferDeparturePoint = "TransferDeparturePoint";
        public static final String TransferDestinationPoint = "TransferDestinationPoint";
        public static final String TransferDepartureDateTime = "TransferDepartureDateTime";
    }

    public CppkTicketControlsDao(@NonNull LocalDaoSession localDaoSession) {
        super(localDaoSession);

        // Регистрируем ссылки таблицы
        registerReference(Properties.EventId, EventDao.TABLE_NAME, ReferenceInfo.ReferencesType.CASCADE);
        registerReference(Properties.TicketEventBaseId, TicketEventBaseDao.TABLE_NAME, ReferenceInfo.ReferencesType.NO_ACTION);
        registerReference(Properties.ParentTicketId, ParentTicketInfoDao.TABLE_NAME, ReferenceInfo.ReferencesType.NO_ACTION);
    }

    @Override
    public String getTableName() {
        return TABLE_NAME;
    }

    @Override
    public CPPKTicketControl fromCursor(Cursor cursor) {
        CPPKTicketControl out = new CPPKTicketControl();
        out.setId(cursor.getLong(cursor.getColumnIndex(BaseEntityDao.Properties.Id)));
        out.setEventId(cursor.getLong(cursor.getColumnIndex(Properties.EventId)));
        out.setTicketEventBaseId(cursor.getLong(cursor.getColumnIndex(Properties.TicketEventBaseId)));
        out.setControlTimestamp(new Date(cursor.getLong(cursor.getColumnIndex(Properties.ControlDateTime))));
        out.setEdsKeyNumber(cursor.getLong(cursor.getColumnIndex(Properties.EdsKeyNumber)));
        out.setRevokedEds(cursor.getInt(cursor.getColumnIndex(Properties.IsRevokedEds)) > 0);
        out.setStopListId(cursor.getInt(cursor.getColumnIndex(Properties.StopListId)));
        out.setExemptionCode(cursor.getInt(cursor.getColumnIndex(Properties.ExemptionCode)));
        out.setPassageResult(PassageResult.getPassageResultByCode(cursor.getInt(cursor.getColumnIndex(Properties.ValidationResult))));
        out.setTripsSpend(cursor.getInt(cursor.getColumnIndex(Properties.TripsSpend)));
        int index = cursor.getColumnIndex(Properties.Trips7000Spend);
        if (!cursor.isNull(index)) {
            out.setTrips7000Spend(cursor.getInt(index));
        }
        index = cursor.getColumnIndex(Properties.Trips7000Count);
        if (!cursor.isNull(index)) {
            out.setTrips7000Count(cursor.getInt(index));
        }
        index = cursor.getColumnIndex(Properties.TripsCount);
        if (!cursor.isNull(index)) {
            out.setTripsCount(cursor.getInt(index));
        }
        out.setSellTicketDeviceId(cursor.getLong(cursor.getColumnIndex(Properties.SellTicketDeviceId)));
        out.setTicketNumber(cursor.getInt(cursor.getColumnIndex(Properties.TicketNumber)));
        out.setRestoredTicket(cursor.getInt(cursor.getColumnIndex(Properties.IsRestoredTicket)) > 0);
        index = cursor.getColumnIndex(Properties.ParentTicketId);
        if (!cursor.isNull(index)) {
            out.setParentTicketInfoId(cursor.getInt(index));
        }
        index = cursor.getColumnIndex(Properties.TransferDeparturePoint);
        out.setTransferDeparturePoint(cursor.isNull(index) ? null : cursor.getLong(index));
        index = cursor.getColumnIndex(Properties.TransferDestinationPoint);
        out.setTransferDestinationPoint(cursor.isNull(index) ? null : cursor.getLong(index));
        index = cursor.getColumnIndex(Properties.TransferDepartureDateTime);
        out.setTransferDepartureDateTime(cursor.isNull(index) ? null : new Date(cursor.getLong(index)));
        return out;
    }

    @Override
    public ContentValues toContentValues(CPPKTicketControl entity) {
        ContentValues contentValues = new ContentValues();
        if (entity.getParentTicketInfoId() != -1)
            contentValues.put(Properties.ParentTicketId, entity.getParentTicketInfoId());
        contentValues.put(Properties.EdsKeyNumber, entity.getEdsKeyNumber());
        contentValues.put(Properties.ControlDateTime, entity.getControlTime().getTime());
        contentValues.put(Properties.ExemptionCode, entity.getExemptionCode());
        contentValues.put(Properties.IsRevokedEds, entity.isRevokedEds());
        contentValues.put(Properties.StopListId, entity.getStopListId());
        contentValues.put(Properties.TripsSpend, entity.getTripsSpend());
        contentValues.put(Properties.ValidationResult, entity.getPassgeResult().getCode());
        contentValues.put(Properties.TicketEventBaseId, entity.getTicketEventBaseId());
        contentValues.put(Properties.EventId, entity.getEventId());
        contentValues.put(Properties.Trips7000Spend, entity.getTrips7000Spend());
        contentValues.put(Properties.Trips7000Count, entity.getTrips7000Count());
        contentValues.put(Properties.TripsCount, entity.getTripsCount());
        contentValues.put(Properties.SellTicketDeviceId, entity.getSellTicketDeviceId());
        contentValues.put(Properties.TicketNumber, entity.getTicketNumber());
        contentValues.put(Properties.IsRestoredTicket, entity.isRestoredTicket());
        contentValues.put(Properties.TransferDeparturePoint, entity.getTransferDeparturePoint());
        contentValues.put(Properties.TransferDestinationPoint, entity.getTransferDestinationPoint());
        Date transferDepartureDateTime = entity.getTransferDepartureDateTime();
        if (transferDepartureDateTime != null) {
            contentValues.put(Properties.TransferDepartureDateTime, transferDepartureDateTime.getTime());
        }
        return contentValues;
    }

    @Override
    public Long getKey(CPPKTicketControl entity) {
        return entity.getId();
    }

    @Override
    public long insertOrThrow(@NonNull CPPKTicketControl entity) {
        long id = super.insertOrThrow(entity);
        entity.setId(id);
        return id;
    }

    @NonNull
    public List<CPPKTicketControl> getControlEventsForShift(@NonNull String shiftUid) {
        return getControlEventsByParams(shiftUid, null, null);
    }

    @NonNull
    public List<CPPKTicketControl> getControlEventsForMonth(@NonNull String monthUid,
                                                            @Nullable EnumSet<ShiftEvent.Status> shiftStatuses) {
        return getControlEventsByParams(null, monthUid, shiftStatuses);
    }

    @NonNull
    private List<CPPKTicketControl> getControlEventsByParams(@Nullable String shiftId,
                                                             @Nullable String monthId,
                                                             @Nullable EnumSet<ShiftEvent.Status> shiftStatuses) {
        StringBuilder builder = new StringBuilder();
        List<String> selectionArgsList = new ArrayList<>();

        builder.append("SELECT ");
        ////////////////////////////////////////////////////////////////////////////////
        builder.append(CppkTicketControlsDao.TABLE_NAME).append(".*");
        ////////////////////////////////////////////////////////////////////////////////
        builder.append(" FROM ");
        builder.append(CppkTicketControlsDao.TABLE_NAME);
        builder.append(" JOIN ");
        builder.append(EventDao.TABLE_NAME);
        builder.append(" ON ");
        builder.append(CppkTicketControlsDao.TABLE_NAME).append(".").append(Properties.EventId);
        builder.append(" = ");
        builder.append(getLocalDaoSession().getEventDao().getIdWithTableName());
        if (shiftId != null || monthId != null) {
            builder.append(" JOIN ");
            builder.append(TicketEventBaseDao.TABLE_NAME);
            builder.append(" ON ");
            builder.append(CppkTicketControlsDao.TABLE_NAME).append(".").append(Properties.TicketEventBaseId);
            builder.append(" = ");
            builder.append(TicketEventBaseDao.TABLE_NAME + "." + BaseEntityDao.Properties.Id);
            builder.append(" JOIN ");
            builder.append(ShiftEventDao.TABLE_NAME);
            builder.append(" ON ");
            builder.append(TicketEventBaseDao.TABLE_NAME).append(".").append(TicketEventBaseDao.Properties.CashRegisterWorkingShiftId);
            builder.append(" = ");
            builder.append(ShiftEventDao.TABLE_NAME + "." + BaseEntityDao.Properties.Id);
            if (shiftId != null) {
                builder.append(" AND ");
                builder.append(ShiftEventDao.Properties.ShiftId).append(" = ").append("?");
                selectionArgsList.add(shiftId);
            }
            if (monthId != null) {
                builder.append(" JOIN ");
                builder.append(MonthEventDao.TABLE_NAME);
                builder.append(" ON ");
                builder.append(ShiftEventDao.TABLE_NAME).append(".").append(ShiftEventDao.Properties.MonthEventId);
                builder.append(" = ");
                builder.append(MonthEventDao.TABLE_NAME + "." + BaseEntityDao.Properties.Id);
                builder.append(" AND ");
                builder.append(MonthEventDao.Properties.MonthId).append(" = ").append("?");
                selectionArgsList.add(monthId);
            }
        }
        if (shiftStatuses != null) {
            builder.append(" WHERE ");
            builder.append(" EXISTS ");
            builder.append(" ( ");
            {
                builder.append("SELECT ");
                builder.append(ShiftEventDao.TABLE_NAME + "." + BaseEntityDao.Properties.Id);
                builder.append(" FROM ");
                builder.append(ShiftEventDao.TABLE_NAME).append(" AS ").append("SHIFTS");
                builder.append(" WHERE ");
                builder.append("SHIFTS").append(".").append(ShiftEventDao.Properties.ShiftId);
                builder.append(" = ");
                builder.append(ShiftEventDao.TABLE_NAME).append(".").append(ShiftEventDao.Properties.ShiftId);
                builder.append(" AND ");
                builder.append("SHIFTS").append(".").append(ShiftEventDao.Properties.ShiftStatus);
                builder.append(" IN ");
                builder.append(" ( ");
                builder.append(SqLiteUtils.makePlaceholders(shiftStatuses.size()));
                for (ShiftEvent.Status shiftStatus : shiftStatuses) {
                    selectionArgsList.add(String.valueOf(shiftStatus.getCode()));
                }
                builder.append(" ) ");
            }
            builder.append(" ) ");
        }
        ////////////////////////////////////////////////////////////////////////////////
        builder.append(" ORDER BY " + BaseEntityDao.Properties.Id + " DESC");
        builder.append(", ");
        builder.append(EventDao.Properties.CreationTimestamp + " DESC");

        String[] selectionArgs = new String[selectionArgsList.size()];
        selectionArgsList.toArray(selectionArgs);

        List<CPPKTicketControl> list = new ArrayList<>();

        Cursor cursor = null;
        try {
            cursor = db().rawQuery(builder.toString(), selectionArgs);
            while (cursor.moveToNext()) {
                CPPKTicketControl item = fromCursor(cursor);
                list.add(item);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return list;
    }

    /**
     * Возвращает список события контроля билетов по времени отсортированных по времени контроля
     *
     * @param time время, с которого необходимо выбрать события контроля(ms)
     * @return
     */
    public List<CPPKTicketControl> loadEventByTime(Date time) {
        List<CPPKTicketControl> contolList = new ArrayList<>();
        /*
         * формируем запрос
         *
         * select * from CPPKTicketControls where CPPKTicketControls.EventId IN
         * ( Select Event._id from Event where Event.CreationTimestamp >
         * 1424451904120)
         */

        String query = "Select * from " + CppkTicketControlsDao.TABLE_NAME +
                " where " + Properties.EventId + " IN " +
                "(select " + BaseEntityDao.Properties.Id +
                " from " + EventDao.TABLE_NAME +
                " where " + EventDao.Properties.CreationTimestamp +
                " > " + time.getTime() + ")" +
                " ORDER BY " + Properties.ControlDateTime;

        Cursor cursor = null;
        try {
            cursor = db().rawQuery(query, null);
            while (cursor.moveToNext()) {
                CPPKTicketControl cppkTicketControls = fromCursor(cursor);
                if (cppkTicketControls != null) {
                    contolList.add(cppkTicketControls);
                }
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return contolList;
    }

    /**
     * Возвращает timestamp создания последнего события контроля
     *
     * @return
     */
    public long getLastControlEventCreationTimeStamp() {
        long lastCreationTimeStamp = 0;

        /*
         * формируем запрос
         *
         * SELECT max(CreationTimestamp) FROM CPPKTicketControl
         * JOIN Event ON CPPKTicketControl.EventId = Event._id
         */

        StringBuilder sql = new StringBuilder();

        sql.append("SELECT max(").append(EventDao.Properties.CreationTimestamp).append(") FROM ").append(CppkTicketControlsDao.TABLE_NAME);
        sql.append(" JOIN ").append(EventDao.TABLE_NAME).append(" ON ").append(Properties.EventId).append(" = ").append(getLocalDaoSession().getEventDao().getIdWithTableName());

        Cursor cursor = null;
        try {
            cursor = db().rawQuery(sql.toString(), null);
            if (cursor.moveToFirst()) {
                if (!cursor.isNull(0)) {
                    lastCreationTimeStamp = cursor.getLong(0);
                }
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return lastCreationTimeStamp;
    }

    /**
     * Возвращает общее количество контролей для смены
     *
     * @param shiftId смены, для которой нужно посчитать количество контролей
     * @return
     */
    public int getControlsCountForShift(@NonNull String shiftId) {
        /*
        select count() from CPPKTicketControl JOIN TicketEventBase on CPPKTicketControl.TicketEventBaseId = TicketEventBase._id
        join CashRegisterWorkingShift on TicketEventBase.CashRegisterWorkingShiftId = CashRegisterWorkingShift._id
        where CashRegisterWorkingShift.ShiftId = '7b2c399d-fb95-46d1-8406-01c5878e96d1'
         */

        final SqlQueryBuilder sqlQueryBuilder = SqlQueryBuilder.newBuilder();
        sqlQueryBuilder.select("count()").from(CppkTicketControlsDao.TABLE_NAME)
                .join(TicketEventBaseDao.TABLE_NAME)
                .onEquals(Properties.TicketEventBaseId,
                        TicketEventBaseDao.TABLE_NAME + "." + BaseEntityDao.Properties.Id)
                .join(ShiftEventDao.TABLE_NAME)
                .onEquals(TicketEventBaseDao.Properties.CashRegisterWorkingShiftId,
                        ShiftEventDao.TABLE_NAME + "." + BaseEntityDao.Properties.Id)
                .whereEquals(ShiftEventDao.Properties.ShiftId, shiftId);

        int count = 0;

        Cursor cursor = null;
        try {
            cursor = db().rawQuery(sqlQueryBuilder.buildQuery(), null);
            if (cursor.moveToFirst()) {
                count = cursor.getInt(0);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return count;
    }

    /**
     * Возвращает количество уникальных проверенных билетов для смены
     *
     * @param shift смена, для которой ищем уникальные проверки
     * @return
     */
    public int getUniqueCheckPd(@NonNull ShiftEvent shift) {
        /*
         * Формируем запрос
         *
         *   select count(*) from
         *   (SELECT DISTINCT CPPKTicketControl.SellTicketDeviceId,
         *                    CPPKTicketControl.TicketNumber,
         *                    TicketEventBase.SaleDateTime
         *     from CPPKTicketControl
         *     JOIN TicketEventBase
         *     ON CPPKTicketControl.TicketEventBaseId = TicketEventBase._id
         *     WHERE TicketEventBase.CashRegisterWorkingShiftId = 3)
         */

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("select count(*) from (select DISTINCT ")
                .append(Properties.SellTicketDeviceId).append(", ")
                .append(Properties.TicketNumber).append(", ")
                .append(TicketEventBaseDao.Properties.SaleDateTime)
                .append(" from ").append(CppkTicketControlsDao.TABLE_NAME)
                .append(" join ").append(TicketEventBaseDao.TABLE_NAME)
                .append(" on ").append(Properties.TicketEventBaseId)
                .append(" = ").append(TicketEventBaseDao.TABLE_NAME + "." + BaseEntityDao.Properties.Id)
                .append(" where ").append(TicketEventBaseDao.Properties.CashRegisterWorkingShiftId)
                .append(" = ").append(shift.getId()).append(")");

        int count = 0;

        Cursor cursor = null;
        try {
            cursor = db().rawQuery(stringBuilder.toString(), null);
            if (cursor.moveToFirst()) {
                count = cursor.getInt(0);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return count;
    }
}
