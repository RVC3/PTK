package ru.ppr.cppk.data.summary;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ru.ppr.cppk.db.LocalDaoSession;
import ru.ppr.cppk.di.Dagger;
import ru.ppr.cppk.localdb.model.UpdateEvent;
import ru.ppr.cppk.localdb.model.UpdateEventType;
import ru.ppr.cppk.localdb.repository.UpdateEventRepository;
import ru.ppr.cppk.localdb.model.ShiftEvent;
import ru.ppr.cppk.localdb.model.MonthEvent;

/**
 * Билдер статистики по обновлениям ПО.
 *
 * @author Aleksandr Brazhkin
 */
public class UpdateStatisticsBuilder {

    private String shiftId;
    private String monthId;
    private boolean buildForLastShift;
    private boolean buildForLastMonth;
    private UpdateEventType updateEventType;
    ////////////////////////////////////////

    public UpdateStatisticsBuilder() {
    }

    /**
     * Задать id смены
     */
    public UpdateStatisticsBuilder setShiftId(String shiftId) {
        this.shiftId = shiftId;
        return this;
    }

    public UpdateStatisticsBuilder setBuildForLastShift(boolean buildForLastShift) {
        this.buildForLastShift = buildForLastShift;
        return this;
    }

    /**
     * Задать id месяца
     */
    public UpdateStatisticsBuilder setMonthId(String monthId) {
        this.monthId = monthId;
        return this;
    }

    public UpdateStatisticsBuilder setBuildForLastMonth(boolean buildForLastMonth) {
        this.buildForLastMonth = buildForLastMonth;
        return this;
    }


    /**
     * Задать тип обновляемого субъекта
     */
    public UpdateStatisticsBuilder setUpdateEventType(UpdateEventType updateEventType) {
        this.updateEventType = updateEventType;
        return this;
    }

    public Statistics build() throws Exception {

        LocalDaoSession localDaoSession = Dagger.appComponent().localDaoSession();
        UpdateEventRepository updateEventRepository = Dagger.appComponent().updateEventRepository();

        boolean forShiftMode = false;
        boolean forMonthMode = false;
        boolean forAllMode = true;
        ShiftEvent workingShifts = null;
        MonthEvent month = null;
        Date fromTimeStamp = null;
        Date toTimeStamp = null;

        if (buildForLastShift && buildForLastMonth) {
            throw new IllegalArgumentException("buildForLastShift && buildForLastMonth");
        }

        if ((buildForLastShift || buildForLastMonth) && (shiftId != null || monthId != null)) {
            throw new IllegalArgumentException("(buildForLastShift || buildForLastMonth) && (shiftId != null || monthId != null)");
        }

        if (buildForLastShift) {
            // Для последней смены
            workingShifts = localDaoSession.getShiftEventDao().getLastShiftEvent(ShiftEvent.ShiftProgressStatus.FINISHED_STATUSES);
            if (workingShifts == null) {
                throw new IllegalStateException("last shift is null");
            }
            forShiftMode = true;
            forMonthMode = forAllMode = !forShiftMode;
        }

        if (buildForLastMonth) {
            // Для последнего месяца
            month = localDaoSession.getMonthEventDao().getLastMonthEvent();
            if (month == null) {
                throw new IllegalStateException("last month is null");
            }
            forMonthMode = true;
            forShiftMode = forAllMode = !forMonthMode;
        }

        if (shiftId != null) {
            // Для конкретной смены
            workingShifts = localDaoSession.getShiftEventDao().getLastCashRegisterWorkingShiftByShiftId(shiftId, ShiftEvent.ShiftProgressStatus.FINISHED_STATUSES);
            if (workingShifts == null) {
                throw new IllegalStateException("shift is null");
            }
            forShiftMode = true;
            forMonthMode = forAllMode = !forShiftMode;
        }

        if (monthId != null) {
            // Для конкретного месяца
            month = localDaoSession.getMonthEventDao().getLastMonthByMonthId(monthId);
            if (month == null) {
                throw new IllegalStateException("month is null");
            }
            forMonthMode = true;
            forShiftMode = forAllMode = !forMonthMode;
        }

        if (workingShifts == null && month == null) {
            forAllMode = true;
            forShiftMode = forMonthMode = !forAllMode;
        }

        if (forShiftMode) {
            // Для смены получаем месяц
            month = localDaoSession.getMonthEventDao().getMonthEventById(workingShifts.getMonthEventId());
            if (month == null) {
                throw new IllegalStateException("month is null");
            }
        }

        /**
         * Время начала/окончания
         */
        if (forShiftMode) {
            fromTimeStamp = workingShifts.getStartTime();
            toTimeStamp = workingShifts.getCloseTime();
        } else if (forMonthMode) {
            fromTimeStamp = month.getOpenDate();
            toTimeStamp = month.getCloseDate();
        } else if (forAllMode) {
            fromTimeStamp = null;
            toTimeStamp = null;
        }

        /**
         * ID смены/месяца; Даты начала, оконачания
         */
        Statistics statistics = new Statistics();
        statistics.shiftId = forShiftMode ? workingShifts.getShiftId() : null;
        statistics.monthId = (forMonthMode || forMonthMode) ? month.getMonthId() : null;
        statistics.fromDate = fromTimeStamp;
        statistics.toDate = toTimeStamp;

        /**
         * Обновления
         */
        if (forShiftMode) {
            statistics.updateEvents = updateEventRepository.getUpdateEventsForShiftOrMonth(workingShifts.getShiftId(), null, updateEventType);
        } else if (forMonthMode) {
            statistics.updateEvents = updateEventRepository.getUpdateEventsForShiftOrMonth(null, month.getMonthId(), updateEventType);
        } else if (forAllMode) {
            statistics.updateEvents = updateEventRepository.getUpdateEventsForShiftOrMonth(null, null, updateEventType);
        }

        return statistics;

    }

    public static class Statistics {
        public String shiftId;
        public String monthId;
        public Date fromDate;
        public Date toDate;
        public List<UpdateEvent> updateEvents = new ArrayList<>();
    }
}
