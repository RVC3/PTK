package ru.ppr.cppk.db.local;

import android.content.ContentValues;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Date;

import ru.ppr.cppk.db.LocalDaoSession;
import ru.ppr.cppk.entity.event.base34.TerminalDay;
import ru.ppr.database.references.ReferenceInfo;
import ru.ppr.logger.Logger;

/**
 * DAO для таблицы локальной БД <i>TerminalDay</i>.
 *
 * @author Aleksandr Brazhkin
 */
public class TerminalDayDao extends BaseEntityDao<TerminalDay, Long> {

    private static final String TAG = Logger.makeLogTag(TerminalDayDao.class);

    public static final String TABLE_NAME = "TerminalDay";

    public static class Properties {
        public static final String Id = "_id";
        public static final String TerminalDayId = "TerminalDayId";
        public static final String EventId = "EventId";
        public static final String StartCashRegisterWorkingShiftId = "StartCashRegisterWorkingShiftId";
        public static final String EndCashRegisterWorkingShiftId = "EndCashRegisterWorkingShiftId";
        public static final String StartDateTime = "StartDateTime";
        public static final String EndDateTime = "EndDateTime";
        public static final String Report = "Report";
        public static final String CurrentSaleTransactionId = "CurrentSaleTransactionId";
        public static final String TerminalNumber = "TerminalNumber";
    }

    public TerminalDayDao(@NonNull LocalDaoSession localDaoSession) {
        super(localDaoSession);

        // Регистрируем ссылки таблицы
        registerReference(Properties.EventId, EventDao.TABLE_NAME, ReferenceInfo.ReferencesType.CASCADE);
        registerReference(Properties.StartCashRegisterWorkingShiftId, ShiftEventDao.TABLE_NAME, ReferenceInfo.ReferencesType.NO_ACTION);
        registerReference(Properties.EndCashRegisterWorkingShiftId, ShiftEventDao.TABLE_NAME, ReferenceInfo.ReferencesType.NO_ACTION);
    }

    @Override
    public String getTableName() {
        return TABLE_NAME;
    }

    @Override
    public TerminalDay fromCursor(Cursor cursor) {
        int index;

        TerminalDay terminalDay = new TerminalDay();

        terminalDay.setId(cursor.getLong(cursor.getColumnIndex(Properties.Id)));
        terminalDay.setTerminalDayId(cursor.getLong(cursor.getColumnIndex(Properties.TerminalDayId)));
        terminalDay.setEventId(cursor.getLong(cursor.getColumnIndex(Properties.EventId)));
        terminalDay.setStartShiftEventId(cursor.getLong(cursor.getColumnIndex(Properties.StartCashRegisterWorkingShiftId)));

        //NULLABLE
        if (!cursor.isNull(index = cursor.getColumnIndex(Properties.EndCashRegisterWorkingShiftId))) {
            terminalDay.setEndShiftEventId(cursor.getLong(index));
        }

        terminalDay.setStartDateTime(new Date(cursor.getLong(cursor.getColumnIndex(Properties.StartDateTime))));

        //NULLABLE
        if (!cursor.isNull(index = cursor.getColumnIndex(Properties.EndDateTime))) {
            terminalDay.setEndDateTime(new Date(cursor.getLong(index)));
        }

        //NULLABLE
        if (!cursor.isNull(index = cursor.getColumnIndex(Properties.Report))) {
            terminalDay.setReport(cursor.getString(index));
        }

        terminalDay.setCurrentSaleTransactionId(cursor.getInt(cursor.getColumnIndex(Properties.CurrentSaleTransactionId)));
        terminalDay.setTerminalNumber(cursor.getString(cursor.getColumnIndex(Properties.TerminalNumber)));

        return terminalDay;
    }

    @Override
    public ContentValues toContentValues(TerminalDay entity) {
        ContentValues contentValues = new ContentValues();

        contentValues.put(Properties.TerminalDayId, entity.getTerminalDayId());
        contentValues.put(Properties.EventId, entity.getEventId());
        contentValues.put(Properties.StartCashRegisterWorkingShiftId, entity.getStartShiftEventId());

        //NULLABLE
        if (entity.getEndShiftEventId() > 0)
            contentValues.put(Properties.EndCashRegisterWorkingShiftId, entity.getEndShiftEventId());

        contentValues.put(Properties.StartDateTime, entity.getStartDateTime().getTime());

        //NULLABLE
        if (entity.getEndDateTime() != null)
            contentValues.put(Properties.EndDateTime, entity.getEndDateTime().getTime());

        //NULLABLE
        if (entity.getReport() != null)
            contentValues.put(Properties.Report, entity.getReport());

        contentValues.put(Properties.CurrentSaleTransactionId, entity.getCurrentSaleTransactionId());
        contentValues.put(Properties.TerminalNumber, entity.getTerminalNumber());

        return contentValues;
    }

    @Override
    public Long getKey(TerminalDay entity) {
        return entity.getId();
    }

    /**
     * Метод для сохранения дня в БД.
     *
     * @param terminalDay день для сохранения.
     * @return id дня, добавленного в БД.
     */
    @Override
    public long insertOrThrow(@NonNull TerminalDay terminalDay) {
        long id = super.insertOrThrow(terminalDay);
        terminalDay.setId(id);
        return id;
    }

    /**
     * Метод для получения дня по id смены.
     *
     * @param shiftId id смены.
     * @return результат выполнения операции.
     */
    @Nullable
    public TerminalDay getTerminalDayForShiftId(long shiftId) {
        final String query = "select * from TerminalDay " +
                "where StartCashRegisterWorkingShiftId in " +
                "(select _id from CashRegisterWorkingShift " +
                "where ShiftId in (" +
                "select ShiftId from CashRegisterWorkingShift " +
                "where _id = " + shiftId + ")) " +
                "and EndCashRegisterWorkingShiftId not null";

        Cursor cursor = db().rawQuery(query, null);
        try {
            if (cursor.moveToFirst()) {
                return fromCursor(cursor);
            }
        } finally {
            cursor.close();
        }

        return null;
    }

    /**
     * Метод для получения последнего для.
     *
     * @return день.
     */
    @Nullable
    public TerminalDay getLastTerminalDay() {
        Cursor cursor = db().query(TABLE_NAME, null, null, null, null, null,
                Properties.Id + " DESC", "1");
        try {
            if (cursor.moveToFirst()) {
                return fromCursor(cursor);
            }
        } finally {
            cursor.close();
        }

        return null;
    }

    /**
     * Метод для получения последнего дня в месяце.
     *
     * @param month_id id месяца.
     * @return день.
     */
    @Nullable
    public TerminalDay getLastTerminalDayInMonth(long month_id) {
        final String query = "select * from " + TABLE_NAME +
                " where " + Properties.StartCashRegisterWorkingShiftId + " in (" +
                "select _id from " + ShiftEventDao.TABLE_NAME +
                " where " + ShiftEventDao.Properties.MonthEventId + " in (" +
                " select _id from " + MonthEventDao.TABLE_NAME + " where _id = " + month_id +
                " ) " +
                ") order by _id desc limit 1";

        Cursor cursor = db().rawQuery(query, null);
        try {
            if (cursor.moveToFirst()) {
                return fromCursor(cursor);
            }
        } finally {
            cursor.close();
        }

        return null;
    }

}
