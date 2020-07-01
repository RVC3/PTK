package ru.ppr.cppk.db.local;

import android.content.ContentValues;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;

import ru.ppr.cppk.db.LocalDaoSession;
import ru.ppr.cppk.entity.settings.ReportType;
import ru.ppr.cppk.localdb.model.MonthEvent;
import ru.ppr.cppk.localdb.model.ShiftEvent;
import ru.ppr.cppk.model.PrintReportEvent;
import ru.ppr.database.SqLiteUtils;
import ru.ppr.database.references.ReferenceInfo;
import ru.ppr.logger.Logger;

/**
 * DAO для таблицы локальной БД <i>PrintReportEvent</i>.
 *
 * @author Aleksandr Brazhkin
 */
public class PrintReportEventDao extends BaseEntityDao<PrintReportEvent, Long> {

    private static String TAG = Logger.makeLogTag(PrintReportEvent.class);

    public static final String TABLE_NAME = "PrintReportEvent";

    public static class Properties {
        public static final String ReportType = "ReportType";
        public static final String EventId = "EventId";
        public static final String CashRegisterEventId = "CashRegisterEventId";
        public static final String TicketTapeEventId = "TicketTapeEventId";
        public static final String CashRegisterWorkingShiftId = "CashRegisterWorkingShiftId";
        public static final String MonthEventId = "MonthEventId";
        public static final String CashInFr = "CashInFR";
        public static final String OperationTime = "OperationTime";
    }

    public PrintReportEventDao(@NonNull LocalDaoSession localDaoSession) {
        super(localDaoSession);

        // Регистрируем ссылки таблицы
        registerReference(Properties.EventId, EventDao.TABLE_NAME, ReferenceInfo.ReferencesType.CASCADE);
        registerReference(Properties.CashRegisterEventId, CashRegisterEventDao.TABLE_NAME, ReferenceInfo.ReferencesType.NO_ACTION);
        registerReference(Properties.TicketTapeEventId, TicketTapeEventDao.TABLE_NAME, ReferenceInfo.ReferencesType.NO_ACTION);
        registerReference(Properties.CashRegisterWorkingShiftId, ShiftEventDao.TABLE_NAME, ReferenceInfo.ReferencesType.NO_ACTION);
        registerReference(Properties.MonthEventId, MonthEventDao.TABLE_NAME, ReferenceInfo.ReferencesType.NO_ACTION);
    }

    @Override
    public String getTableName() {
        return TABLE_NAME;
    }

    @Override
    public PrintReportEvent fromCursor(Cursor cursor) {
        PrintReportEvent printReportEvent = new PrintReportEvent();
        printReportEvent.setId(cursor.getLong(cursor.getColumnIndex(BaseEntityDao.Properties.Id)));
        printReportEvent.setEventId(cursor.getLong(cursor.getColumnIndex(Properties.EventId)));
        printReportEvent.setCashRegisterEventId(cursor.getLong(cursor.getColumnIndex(Properties.CashRegisterEventId)));
        printReportEvent.setCashInFR(new BigDecimal(cursor.getString(cursor.getColumnIndex(Properties.CashInFr))));
        printReportEvent.setOperationTime(new Date(cursor.getLong(cursor.getColumnIndex(Properties.OperationTime))));
        int reportTypeIndex = cursor.getColumnIndex(Properties.ReportType);
        if (!cursor.isNull(reportTypeIndex)) {
            printReportEvent.setReportType(ReportType.getByCode(cursor.getInt(reportTypeIndex)));
        }
        int cashRegisterWorkingShiftIdIndex = cursor.getColumnIndex(Properties.CashRegisterWorkingShiftId);
        if (!cursor.isNull(cashRegisterWorkingShiftIdIndex)) {
            printReportEvent.setShiftEventId(cursor.getLong(cashRegisterWorkingShiftIdIndex));
        }
        int monthEventIdIndex = cursor.getColumnIndex(Properties.MonthEventId);
        if (!cursor.isNull(monthEventIdIndex)) {
            printReportEvent.setMonthEventId(cursor.getLong(monthEventIdIndex));
        }
        return printReportEvent;
    }

    @Override
    public ContentValues toContentValues(PrintReportEvent entity) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(Properties.EventId, entity.getEventId());
        contentValues.put(Properties.ReportType, entity.getReportType() == null ? null : entity.getReportType().getCode());
        contentValues.put(Properties.CashRegisterEventId, entity.getCashRegisterEventId());
        contentValues.put(Properties.TicketTapeEventId, entity.getTicketTapeEventId());
        contentValues.put(Properties.CashInFr, entity.getCashInFR().toString());
        contentValues.put(Properties.OperationTime, entity.getOperationTime().getTime());
        if (entity.getMonthEventId() > 0) {
            contentValues.put(Properties.MonthEventId, entity.getMonthEventId());
        }
        if (entity.getShiftEventId() > 0) {
            contentValues.put(Properties.CashRegisterWorkingShiftId, entity.getShiftEventId());
        }
        return contentValues;
    }

    @Override
    public Long getKey(PrintReportEvent entity) {
        return entity.getId();
    }

    @Override
    public long insertOrThrow(@NonNull PrintReportEvent entity) {
        long id = super.insertOrThrow(entity);
        entity.setId(id);
        return id;
    }

    /**
     * Возвращает количество напечатанных отчетов за всё время.
     *
     * @return Количество напечатанных отчетов
     */
    @NonNull
    public HashMap<ReportType, Integer> getPrintReportEventsCount() {
        return getPrintReportEventsCountByParams(null, null, null, false);
    }

    /**
     * Возвращает количество напечатанных отчетов за смену.
     *
     * @param shiftUid UUID смены
     * @return Количество напечатанных отчетов
     */
    @NonNull
    public HashMap<ReportType, Integer> getPrintReportEventsCountForShift(@NonNull String shiftUid) {
        return getPrintReportEventsCountByParams(shiftUid, null, null, false);
    }

    /**
     * Возвращает количество напечатанных отчетов за месяц.
     *
     * @param monthUid                UUID месяца
     * @param shiftStatuses           Список интересующих статусов смен, {@code null} - чтобы не использовать фильтрацию
     * @param includeOutOfShiftEvents {@code true} - учитывать события вне смен, {@code false} - иначе.
     * @return Количество напечатанных отчетов
     */
    @NonNull
    public HashMap<ReportType, Integer> getPrintReportEventsCountForMonth(@NonNull String monthUid,
                                                                          @Nullable EnumSet<ShiftEvent.Status> shiftStatuses,
                                                                          boolean includeOutOfShiftEvents) {
        return getPrintReportEventsCountByParams(null, monthUid, shiftStatuses, includeOutOfShiftEvents);
    }

    /**
     * Возвращает количество напечатанных отчетов по указанным параметрам.
     *
     * @param shiftId                 UUID смены
     * @param monthId                 UUID месяца
     * @param shiftStatuses           Список интересующих статусов смен, {@code null} - чтобы не использовать фильтрацию
     * @param includeOutOfShiftEvents {@code true} - учитывать события вне смен, {@code false} - иначе.
     * @return Количество напечатанных отчетов
     */
    @NonNull
    private HashMap<ReportType, Integer> getPrintReportEventsCountByParams(@Nullable String shiftId,
                                                                           @Nullable String monthId,
                                                                           @Nullable EnumSet<ShiftEvent.Status> shiftStatuses,
                                                                           boolean includeOutOfShiftEvents) {

        String countColumnName = "count";
        HashMap<ReportType, Integer> hashMap = new HashMap<>();

        List<String> selectionArgsList = new ArrayList<>();
        StringBuilder builder = new StringBuilder();

        builder.append("SELECT ");
        // /////////////////////////////////////////////////////////////////////////////
        builder.append(Properties.ReportType + ",");
        builder.append("COUNT(").append(Properties.ReportType).append(") AS ").append(countColumnName);
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
        }
        if (monthId != null) {
            if (shiftStatuses != null) {
                builder.append(" LEFT JOIN ");
                builder.append(ShiftEventDao.TABLE_NAME);
                builder.append(" ON ");
                builder.append(TABLE_NAME).append(".").append(Properties.CashRegisterWorkingShiftId);
                builder.append(" = ");
                builder.append(ShiftEventDao.TABLE_NAME + "." + BaseEntityDao.Properties.Id);
            }
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
            builder.append(MonthEventDao.TABLE_NAME).append(".").append(MonthEventDao.Properties.Status).append(" = ").append("?");
            selectionArgsList.add(String.valueOf(MonthEvent.Status.OPENED.getCode()));
        }
        ///////////////////////////////////////////////////////////////////////////////
        builder.append(" WHERE 1 = 1");
        if (monthId != null) {
            if (shiftStatuses != null) {
                builder.append(" AND ");
                builder.append(" ( ");
                {
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
                    if (includeOutOfShiftEvents) {
                        builder.append(" OR ");
                        builder.append(Properties.CashRegisterWorkingShiftId).append(" IS NULL ");
                    }
                    builder.append(" ) ");
                }
                builder.append(" ) ");
            } else {
                if (!includeOutOfShiftEvents) {
                    builder.append(" AND ");
                    builder.append(TicketTapeEventDao.Properties.CashRegisterWorkingShiftId).append(" IS NOT NULL ");
                }
            }
        }
        ///////////////////////////////////////////////////////////////////////////////
        builder.append(" GROUP BY ").append(Properties.ReportType);

        String[] selectionArgs = new String[selectionArgsList.size()];
        selectionArgsList.toArray(selectionArgs);

        Cursor cursor = null;
        try {
            cursor = db().rawQuery(builder.toString(), selectionArgs);
            while (cursor.moveToNext()) {
                ReportType reportType = null;
                int reportTypeIndex = cursor.getColumnIndex(Properties.ReportType);
                if (!cursor.isNull(reportTypeIndex)) {
                    reportType = ReportType.getByCode(cursor.getInt(reportTypeIndex));
                }
                int count = cursor.getInt(cursor.getColumnIndex(countColumnName));
                hashMap.put(reportType, count);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return hashMap;
    }
}
