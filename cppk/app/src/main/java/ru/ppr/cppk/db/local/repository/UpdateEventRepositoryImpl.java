package ru.ppr.cppk.db.local.repository;

import android.database.Cursor;

import com.google.common.base.Preconditions;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import ru.ppr.cppk.GlobalConstants;
import ru.ppr.cppk.db.local.BaseEntityDao;
import ru.ppr.cppk.db.local.LocalDbSessionManager;
import ru.ppr.cppk.db.local.UpdateEventDao;
import ru.ppr.cppk.db.local.repository.base.BaseCrudLocalDbRepository;
import ru.ppr.cppk.localdb.model.MonthEvent;
import ru.ppr.cppk.localdb.model.ShiftEvent;
import ru.ppr.cppk.localdb.model.UpdateEvent;
import ru.ppr.cppk.localdb.model.UpdateEventType;
import ru.ppr.cppk.localdb.repository.UpdateEventRepository;

/**
 * @author Aleksandr Brazhkin
 */
public class UpdateEventRepositoryImpl extends BaseCrudLocalDbRepository<UpdateEvent, Long> implements UpdateEventRepository {

    @Inject
    UpdateEventRepositoryImpl(LocalDbSessionManager localDbSessionManager) {
        super(localDbSessionManager);
    }

    @Override
    protected BaseEntityDao<UpdateEvent, Long> dao() {
        return daoSession().getUpdateEventDao();
    }

    @Override
    public List<UpdateEvent> getUpdateEventsForShiftOrMonth(String shiftId, String monthId, UpdateEventType updateEventType) {
        Long firstUpdateEventId = null;
        Long lastUpdateEventId = null;

        if (shiftId != null) {
            ShiftEvent workingShiftStart = daoSession().getShiftEventDao().getFirstShiftEventByShiftId(shiftId, ShiftEvent.ShiftProgressStatus.FINISHED_STATUSES);
            ShiftEvent workingShiftEnd = daoSession().getShiftEventDao().getLastCashRegisterWorkingShiftByShiftId(shiftId, ShiftEvent.ShiftProgressStatus.FINISHED_STATUSES);
            if (workingShiftStart == null) {
                throw new IllegalStateException("Shift not found");
            }
            firstUpdateEventId = daoSession().getEventDao().load(workingShiftStart.getEventId()).getSoftwareUpdateEventId();
            Preconditions.checkNotNull(workingShiftEnd);
            if (workingShiftEnd.getStatus() == ShiftEvent.Status.ENDED) {
                lastUpdateEventId = daoSession().getEventDao().load(workingShiftEnd.getEventId()).getSoftwareUpdateEventId();
            }
        }

        if (monthId != null) {
            MonthEvent monthStart = daoSession().getMonthEventDao().getFirstMonthByMonthId(monthId);
            MonthEvent monthEnd = daoSession().getMonthEventDao().getLastMonthByMonthId(monthId);
            if (monthStart == null) {
                throw new IllegalStateException("Month not found");
            }
            firstUpdateEventId = daoSession().getEventDao().load(monthStart.getEventId()).getSoftwareUpdateEventId();
            if (monthEnd.getStatus() == MonthEvent.Status.CLOSED) {
                lastUpdateEventId = daoSession().getEventDao().load(monthEnd.getEventId()).getSoftwareUpdateEventId();
            }
        }

        StringBuilder builder = new StringBuilder();
        List<String> selectionArgsList = new ArrayList<>();

        builder.append("SELECT ");
        ////////////////////////////////////////////////////////////////////////////////
        builder.append(UpdateEventDao.TABLE_NAME).append(".*");
        ////////////////////////////////////////////////////////////////////////////////
        builder.append(" FROM ");
        builder.append(UpdateEventDao.TABLE_NAME);
        ////////////////////////////////////////////////////////////////////////////////
        builder.append(" WHERE ");
        builder.append(" 1 = 1 ");
        if (updateEventType != null) {
            builder.append(" AND ");
            builder.append(UpdateEventDao.Properties.UpdateSubject).append(" = ").append("?");
            selectionArgsList.add(String.valueOf(updateEventType.getCode()));
        }
        if (firstUpdateEventId != null) {
            builder.append(" AND ");
            builder.append(BaseEntityDao.Properties.Id).append(" >= ").append("?");
            selectionArgsList.add(String.valueOf(firstUpdateEventId));
        }
        if (lastUpdateEventId != null) {
            builder.append(" AND ");
            builder.append(BaseEntityDao.Properties.Id).append(" <= ").append("?");
            selectionArgsList.add(String.valueOf("lastUpdateEventId"));
        }
        ////////////////////////////////////////////////////////////////////////////////
        builder.append(" ORDER BY ").append(UpdateEventDao.Properties.OperationTime).append(" ASC ");

        String[] selectionArgs = new String[selectionArgsList.size()];
        selectionArgsList.toArray(selectionArgs);

        List<UpdateEvent> list = new ArrayList<>();
        Cursor cursor = db().rawQuery(builder.toString(), selectionArgs);
        try {
            while (cursor.moveToNext()) {
                list.add(dao().fromCursor(cursor));
            }
        } finally {
            cursor.close();
        }
        return list;
    }

    @Override
    public UpdateEvent getLastUpdateEvent(UpdateEventType updateEventType, boolean includeSubjectAll) {
        StringBuilder builder = new StringBuilder();

        builder.append("SELECT ");
        // /////////////////////////////////////////////////////////////////////////////
        builder.append(UpdateEventDao.TABLE_NAME).append(".*");
        // /////////////////////////////////////////////////////////////////////////////
        builder.append(" FROM ");
        builder.append(UpdateEventDao.TABLE_NAME);
        builder.append(" WHERE ");
        builder.append(UpdateEventDao.Properties.UpdateSubject + " = " + Integer.toString(updateEventType.getCode()));
        if (includeSubjectAll) {
            builder.append(" OR ");
            builder.append(UpdateEventDao.Properties.UpdateSubject + " = " + Integer.toString(UpdateEventType.ALL.getCode()));
        }
        builder.append(" ORDER BY " + UpdateEventDao.Properties.OperationTime + " DESC");
        builder.append(" LIMIT 1");

        Cursor cursor = db().rawQuery(builder.toString(), null);
        try {
            if (cursor.moveToFirst()) {
                return dao().fromCursor(cursor);
            }
        } finally {
            cursor.close();
        }
        return null;
    }

    @Override
    public boolean isStopListVersionValid(int actualDaysCount) {
        UpdateEvent updateEvent = getLastUpdateEvent(UpdateEventType.STOP_LISTS, true);
        if (updateEvent == null)
            return false;

        long dayInMillis = actualDaysCount * GlobalConstants.MILLISECOND_IN_DAY;
        long millisFromLastUpdate = System.currentTimeMillis() - updateEvent.getOperationTime().getTime();
        if (millisFromLastUpdate < dayInMillis)// && millisFromLastUpdate > 0)
            // https://aj.srvdev.ru/browse/CPPKPP-24784
            // ПТК: изменение даты на день назад приводит к истечению срока действия стоп-листов
            return true;
        else
            return false;
    }
}
