package ru.ppr.cppk.data.summary;

import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;

import ru.ppr.cppk.Globals;
import ru.ppr.cppk.db.LocalDaoSession;
import ru.ppr.cppk.entity.settings.ReportType;
import ru.ppr.cppk.localdb.model.MonthEvent;
import ru.ppr.cppk.localdb.model.ShiftEvent;

/**
 * Билдер статистики по печати отчетов.
 *
 * @author Aleksandr Brazhkin
 */
public class ReportCountStatisticsBuilder {

    private String shiftId;
    private String monthId;
    private boolean buildForLastShift;
    private boolean buildForLastMonth;
    private boolean buildForClosedShiftsOnly;
    private boolean buildForOutOfShiftEvents;
    ////////////////////////////////////////

    public ReportCountStatisticsBuilder() {
    }

    /**
     * Задать id смены
     */
    public ReportCountStatisticsBuilder setShiftId(String shiftId) {
        this.shiftId = shiftId;
        return this;
    }

    public ReportCountStatisticsBuilder setBuildForLastShift(boolean buildForLastShift) {
        this.buildForLastShift = buildForLastShift;
        return this;
    }

    /**
     * Задать id месяца
     */
    public ReportCountStatisticsBuilder setMonthId(String monthId) {
        this.monthId = monthId;
        return this;
    }

    public ReportCountStatisticsBuilder setBuildForLastMonth(boolean buildForLastMonth) {
        this.buildForLastMonth = buildForLastMonth;
        return this;
    }

    /**
     * Устанавливает флаг необходимости сбора сведений только для закрытых смен
     *
     * @param buildForClosedShiftsOnly {@code true} - только для закрытых смен, {@code false} - иначе.
     * @return {@code this}
     */
    public ReportCountStatisticsBuilder setBuildForClosedShiftsOnly(boolean buildForClosedShiftsOnly) {
        this.buildForClosedShiftsOnly = buildForClosedShiftsOnly;
        return this;
    }

    /**
     * Устанавливает флаг необходимости сбора сведений вне смен
     *
     * @param buildForOutOfShiftEvents {@code true} - учитывать события вне смен, {@code false} - иначе.
     * @return {@code this}
     */
    public ReportCountStatisticsBuilder setBuildForOutOfShiftEvents(boolean buildForOutOfShiftEvents) {
        this.buildForOutOfShiftEvents = buildForOutOfShiftEvents;
        return this;
    }

    public Statistics build() throws Exception {

        LocalDaoSession localDaoSession = Globals.getInstance().getLocalDaoSession();

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
         * Количество отчетов за смену/месяц
         */
        HashMap<ReportType, Integer> reportsCount = null;
        if (forShiftMode) {
            reportsCount = localDaoSession.getPrintReportEventDao().getPrintReportEventsCountForShift(workingShifts.getShiftId());
        } else if (forMonthMode) {
            reportsCount = localDaoSession.getPrintReportEventDao().getPrintReportEventsCountForMonth(
                    month.getMonthId(),
                    // https://aj.srvdev.ru/browse/CPPKPP-32090
                    // Для месячной ведомости учитываем только закрытые смены
                    buildForClosedShiftsOnly ? EnumSet.of(ShiftEvent.Status.ENDED) : null,
                    buildForOutOfShiftEvents
            );
        } else if (forAllMode) {
            reportsCount = localDaoSession.getPrintReportEventDao().getPrintReportEventsCount();
        }

        int auditTrailCount = reportsCount.containsKey(ReportType.AuditTrail) ? reportsCount.get(ReportType.AuditTrail) : 0;
        int testShiftSheetCount = reportsCount.containsKey(ReportType.TestShiftShit) ? reportsCount.get(ReportType.TestShiftShit) : 0;
        int shiftSheetCount = reportsCount.containsKey(ReportType.ShiftShit) ? reportsCount.get(ReportType.ShiftShit) : 0;
        int shiftDiscountSheetCount = reportsCount.containsKey(ReportType.DiscountedShiftShit) ? reportsCount.get(ReportType.DiscountedShiftShit) : 0;
        int shiftClearingSheetCount = reportsCount.containsKey(ReportType.SheetShiftBlanking) ? reportsCount.get(ReportType.SheetShiftBlanking) : 0;
        int testMonthSheetCount = reportsCount.containsKey(ReportType.TestMonthShit) ? reportsCount.get(ReportType.TestMonthShit) : 0;
        int monthSheetCount = reportsCount.containsKey(ReportType.MonthlySheet) ? reportsCount.get(ReportType.MonthlySheet) : 0;
        int monthDiscountSheetCount = reportsCount.containsKey(ReportType.DiscountedMonthlySheet) ? reportsCount.get(ReportType.DiscountedMonthlySheet) : 0;
        int monthClearingSheetCount = reportsCount.containsKey(ReportType.SheetBlankingMonth) ? reportsCount.get(ReportType.SheetBlankingMonth) : 0;
        int salesForEttLogCount = reportsCount.containsKey(ReportType.SalesForEttLog) ? reportsCount.get(ReportType.SalesForEttLog) : 0;

        statistics.reportsCount.auditTrailCount = auditTrailCount;
        statistics.reportsCount.testShiftSheetCount = testShiftSheetCount;
        statistics.reportsCount.shiftSheetCount = shiftSheetCount;
        statistics.reportsCount.shiftDiscountSheetCount = shiftDiscountSheetCount;
        statistics.reportsCount.shiftClearingSheetCount = shiftClearingSheetCount;
        statistics.reportsCount.testMonthSheetCount = testMonthSheetCount;
        statistics.reportsCount.monthSheetCount = monthSheetCount;
        statistics.reportsCount.monthDiscountSheetCount = monthDiscountSheetCount;
        statistics.reportsCount.monthClearingSheetCount = monthClearingSheetCount;
        statistics.reportsCount.salesForEttLogCount = salesForEttLogCount;

        statistics.reportsCount.totalCount = auditTrailCount
                + testShiftSheetCount
                + shiftSheetCount
                + shiftDiscountSheetCount
                + shiftClearingSheetCount
                + monthSheetCount
                + monthDiscountSheetCount
                + monthClearingSheetCount
                + testMonthSheetCount
                + salesForEttLogCount;

        return statistics;
    }

    public static class Statistics {
        public String shiftId;
        public String monthId;
        public Date fromDate;
        public Date toDate;
        public ReportsCount reportsCount = new ReportsCount();
    }


    public static class ReportsCount {
        public int totalCount;
        public int auditTrailCount;
        public int testShiftSheetCount;
        public int shiftSheetCount;
        public int shiftDiscountSheetCount;
        public int shiftClearingSheetCount;
        public int testMonthSheetCount;
        public int salesForEttLogCount;
        public int monthSheetCount;
        public int monthDiscountSheetCount;
        public int monthClearingSheetCount;
    }
}
