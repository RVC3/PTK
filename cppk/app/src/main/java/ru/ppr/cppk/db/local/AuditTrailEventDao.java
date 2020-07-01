package ru.ppr.cppk.db.local;

import android.content.ContentValues;
import android.database.Cursor;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ru.ppr.cppk.db.LocalDaoSession;
import ru.ppr.cppk.localdb.model.AuditTrailEvent;
import ru.ppr.cppk.localdb.model.AuditTrailEventType;
import ru.ppr.database.references.ReferenceInfo;
import ru.ppr.logger.Logger;

/**
 * DAO для таблицы локальной БД <i>AuditTrailEvent</i>.
 *
 * @author Aleksandr Brazhkin
 */
public class AuditTrailEventDao extends BaseEntityDao<AuditTrailEvent, Long> {

    private static final String TAG = Logger.makeLogTag(AuditTrailEventDao.class);

    public static final String TABLE_NAME = "AuditTrailEvent";

    public static class Properties {
        public static final String ExtEventId = "ExtEventId";
        public static final String Type = "Type";
        public static final String OperationTime = "OperationTime";
        public static final String CashRegisterWorkingShiftId = "CashRegisterWorkingShiftId";
        public static final String MonthEventId = "MonthEventId";
    }

    public AuditTrailEventDao(@NonNull LocalDaoSession localDaoSession) {
        super(localDaoSession);

        // Регистрируем ссылки таблицы
        registerReference(Properties.MonthEventId, MonthEventDao.TABLE_NAME, ReferenceInfo.ReferencesType.CASCADE);
        registerReference(Properties.CashRegisterWorkingShiftId, ShiftEventDao.TABLE_NAME, ReferenceInfo.ReferencesType.NO_ACTION);
    }

    @Override
    public String getTableName() {
        return TABLE_NAME;
    }

    @Override
    public AuditTrailEvent fromCursor(@NonNull final Cursor cursor) {
        AuditTrailEvent auditTrailEvent = new AuditTrailEvent();
        auditTrailEvent.setId(cursor.getLong(cursor.getColumnIndex(BaseEntityDao.Properties.Id)));
        auditTrailEvent.setExtEventId(cursor.getLong(cursor.getColumnIndex(Properties.ExtEventId)));
        auditTrailEvent.setType(AuditTrailEventType.valueOf(cursor.getInt(cursor.getColumnIndex(Properties.Type))));
        auditTrailEvent.setOperationTime(new Date(cursor.getLong(cursor.getColumnIndex(Properties.OperationTime))));
        int cashRegisterWorkingShiftIdIndex = cursor.getColumnIndex(Properties.CashRegisterWorkingShiftId);
        if (!cursor.isNull(cashRegisterWorkingShiftIdIndex)) {
            auditTrailEvent.setCashRegisterWorkingShiftId(cursor.getLong(cashRegisterWorkingShiftIdIndex));
        }
        auditTrailEvent.setMonthEventId(cursor.getColumnIndex(Properties.MonthEventId));
        return auditTrailEvent;
    }

    @Override
    public ContentValues toContentValues(@NonNull final AuditTrailEvent entity) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(Properties.ExtEventId, entity.getExtEventId());
        contentValues.put(Properties.Type, entity.getType().getCode());
        contentValues.put(Properties.OperationTime, entity.getOperationTime().getTime());
        contentValues.put(Properties.MonthEventId, entity.getMonthEventId());
        if (entity.getCashRegisterWorkingShiftId() != -1) {
            contentValues.put(Properties.CashRegisterWorkingShiftId, entity.getCashRegisterWorkingShiftId());
        }
        return contentValues;
    }

    @Override
    public Long getKey(AuditTrailEvent entity) {
        return entity.getId();
    }

    @Override
    public long insertOrThrow(@NonNull AuditTrailEvent entity) {
        long id = super.insertOrThrow(entity);
        entity.setId(id);
        return id;
    }

    public List<AuditTrailEvent> getAuditTrailEventsForShiftOrMonth(String shiftId, String monthId, Date fromDate, Date toDate) {

        List<String> selectionArgsList = new ArrayList<>();
        StringBuilder builder = new StringBuilder();

        builder.append("SELECT ");
        ////////////////////////////////////////////////////////////////////////////////
        builder.append(TABLE_NAME).append(".*");
        ////////////////////////////////////////////////////////////////////////////////
        builder.append(" FROM ");
        builder.append(TABLE_NAME);
        if (shiftId != null) {
            builder.append(" JOIN ");
            builder.append(ShiftEventDao.TABLE_NAME);
            builder.append(" ON ");
            builder.append(TABLE_NAME).append(".").append(Properties.CashRegisterWorkingShiftId);
            builder.append(" = ");
            builder.append(ShiftEventDao.TABLE_NAME + "." + BaseEntityDao.Properties.Id);
            builder.append(" AND ");
            builder.append(ShiftEventDao.Properties.ShiftId).append(" = ").append("?");
            selectionArgsList.add(shiftId);
            builder.append(" AND ");
            builder.append(" ( ");
            builder.append(ShiftEventDao.TABLE_NAME).append(".").append(ShiftEventDao.Properties.ShiftStatus).append(" = ").append("0");
            builder.append(" OR ");
            builder.append(ShiftEventDao.TABLE_NAME).append(".").append(ShiftEventDao.Properties.ShiftStatus).append(" = ").append("5");
            builder.append(" ) ");
        }
        if (monthId != null) {
            builder.append(" JOIN ");
            builder.append(MonthEventDao.TABLE_NAME);
            builder.append(" ON ");
            builder.append(TABLE_NAME).append(".").append(Properties.MonthEventId);
            builder.append(" = ");
            builder.append(MonthEventDao.TABLE_NAME + "." + BaseEntityDao.Properties.Id);
            builder.append(" AND ");
            builder.append(MonthEventDao.Properties.MonthId).append(" = ").append("?");
            selectionArgsList.add(monthId);
            builder.append(" AND ");
            builder.append(MonthEventDao.TABLE_NAME).append(".").append(MonthEventDao.Properties.Status).append(" = ").append("0");
        }
        /////////////////////////////////////////////////////////////////////////////////
        builder.append(" WHERE ");
        builder.append(" 1 = 1 ");
        if (fromDate != null) {
            builder.append(" AND ");
            builder.append(Properties.OperationTime).append(" >= ").append("?");
            selectionArgsList.add(String.valueOf(fromDate.getTime()));
        }
        if (toDate != null) {
            builder.append(" AND ");
            builder.append(Properties.OperationTime).append(" < ").append("?");
            selectionArgsList.add(String.valueOf(toDate.getTime()));
        }
        /////////////////////////////////////////////////////////////////////////////////
        builder.append(" ORDER BY ").append(getIdWithTableName()).append(" ASC ");

        String[] selectionArgs = new String[selectionArgsList.size()];
        selectionArgsList.toArray(selectionArgs);
        List<AuditTrailEvent> list = new ArrayList<>();
        Cursor cursor = null;
        try {
            cursor = db().rawQuery(builder.toString(), selectionArgs);

            while (cursor.moveToNext()) {
                AuditTrailEvent auditTrailEvent = fromCursor(cursor);
                list.add(auditTrailEvent);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return list;
    }

}
