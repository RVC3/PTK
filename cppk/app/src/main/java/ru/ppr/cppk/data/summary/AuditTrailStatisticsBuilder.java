package ru.ppr.cppk.data.summary;

import com.google.common.base.Preconditions;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ru.ppr.cppk.Globals;
import ru.ppr.cppk.db.LocalDaoSession;
import ru.ppr.cppk.entity.event.base34.CPPKTicketReturn;
import ru.ppr.cppk.entity.event.base34.CPPKTicketSales;
import ru.ppr.cppk.entity.event.base34.TestTicketEvent;
import ru.ppr.cppk.entity.event.model34.ProgressStatus;
import ru.ppr.cppk.localdb.model.AuditTrailEvent;
import ru.ppr.cppk.localdb.model.FineSaleEvent;
import ru.ppr.cppk.localdb.model.MonthEvent;
import ru.ppr.cppk.localdb.model.ShiftEvent;

/**
 * Билдер статистики для КЖ.
 *
 * @author Aleksandr Brazhkin
 */
public class AuditTrailStatisticsBuilder {

    private String shiftId;
    private String monthId;
    private Date fromDate;
    private Date toDate;
    private boolean buildForLastShift;
    private boolean buildForLastMonth;
    private boolean buildForPeriod;
    ////////////////////////////////////////

    public AuditTrailStatisticsBuilder() {
    }

    /**
     * Задать id смены
     */
    public AuditTrailStatisticsBuilder setShiftId(String shiftId) {
        this.shiftId = shiftId;
        return this;
    }

    public AuditTrailStatisticsBuilder setBuildForLastShift(boolean buildForLastShift) {
        this.buildForLastShift = buildForLastShift;
        return this;
    }

    /**
     * Задать id месяца
     */
    public AuditTrailStatisticsBuilder setMonthId(String monthId) {
        this.monthId = monthId;
        return this;
    }

    public AuditTrailStatisticsBuilder setBuildForLastMonth(boolean buildForLastMonth) {
        this.buildForLastMonth = buildForLastMonth;
        return this;
    }

    /**
     * Задать произвольный период
     */
    public AuditTrailStatisticsBuilder setPeriod(Date fromDate, Date toDate) {
        this.fromDate = fromDate;
        this.toDate = toDate;
        return this;
    }

    public AuditTrailStatisticsBuilder setBuildForPeriod(boolean buildForPeriod) {
        this.buildForPeriod = buildForPeriod;
        return this;
    }

    public Statistics build() throws Exception {

        LocalDaoSession localDaoSession = Globals.getInstance().getLocalDaoSession();

        boolean forShiftMode = true;
        boolean forPeriodMode = buildForPeriod;
        ShiftEvent workingShifts = null;
        MonthEvent month = null;
        Date fromTimeStamp = null;
        Date toTimeStamp = null;

        if (buildForLastShift && buildForLastMonth) {
            throw new IllegalArgumentException("buildForLastShift && buildForLastMonth");
        }

        if (shiftId != null && monthId != null) {
            throw new IllegalArgumentException("shiftId != null && monthId != null");
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
        }

        if (buildForLastMonth) {
            // Для последнего месяца
            month = localDaoSession.getMonthEventDao().getLastMonthEvent();
            if (month == null) {
                throw new IllegalStateException("last month is null");
            }
            forShiftMode = false;
        }

        if (shiftId != null) {
            // Для конкретной смены
            workingShifts = localDaoSession.getShiftEventDao().getLastCashRegisterWorkingShiftByShiftId(shiftId, ShiftEvent.ShiftProgressStatus.FINISHED_STATUSES);
            if (workingShifts == null) {
                throw new IllegalStateException("shift is null");
            }
            forShiftMode = true;
        }

        if (monthId != null) {
            // Для конкретного месяца
            month = localDaoSession.getMonthEventDao().getLastMonthByMonthId(monthId);
            if (month == null) {
                throw new IllegalStateException("month is null");
            }
            forShiftMode = false;
        }

        if (forShiftMode) {
            // Для смены получаем месяц
            month = localDaoSession.getMonthEventDao().getMonthEventById(workingShifts.getMonthEventId());
            if (month == null) {
                throw new IllegalStateException("month is null");
            }
        }

        if (fromDate != null || toDate != null) {
            forPeriodMode = true;
        }

        /*
         * Время начала/окончания
         */
        if (forShiftMode) {
            ShiftEvent firstShiftEvent = localDaoSession.getShiftEventDao().getFirstShiftEventByShiftId(workingShifts.getShiftId(), ShiftEvent.ShiftProgressStatus.FINISHED_STATUSES);
            Preconditions.checkNotNull(firstShiftEvent);
            fromTimeStamp = localDaoSession.getEventDao().load(firstShiftEvent.getEventId()).getCreationTimestamp();
            toTimeStamp = (workingShifts.getStatus() == ShiftEvent.Status.ENDED) ? localDaoSession.getEventDao().load(workingShifts.getEventId()).getCreationTimestamp() : null;
        } else {
            fromTimeStamp = localDaoSession.getEventDao().load(localDaoSession.getMonthEventDao().getFirstMonthByMonthId(month.getMonthId()).getEventId()).getCreationTimestamp();
            toTimeStamp = (month.getStatus() == MonthEvent.Status.CLOSED) ? localDaoSession.getEventDao().load(month.getEventId()).getCreationTimestamp() : null;
        }
        if (forPeriodMode) {
            if (fromDate != null) {
                if (fromTimeStamp == null || fromDate.after(fromTimeStamp)) {
                    fromTimeStamp = fromDate;
                }
            }
            if (toDate != null) {
                if (toTimeStamp == null || toDate.before(toTimeStamp)) {
                    toTimeStamp = toDate;
                }
            }
        }

        /**
         * ID смены/месяца; Даты начала, оконачания
         */
        Statistics statistics = new Statistics();
        statistics.shiftId = forShiftMode ? workingShifts.getShiftId() : null;
        statistics.monthId = month.getMonthId();
        statistics.fromDate = fromTimeStamp;
        statistics.toDate = toTimeStamp;

        /**
         * События контрольного журнала
         */
        List<AuditTrailEvent> allAuditTrailEvents;
        if (forShiftMode) {
            if (forPeriodMode) {
                allAuditTrailEvents = localDaoSession.getAuditTrailEventDao().getAuditTrailEventsForShiftOrMonth(workingShifts.getShiftId(), null, fromTimeStamp, toTimeStamp);
            } else {
                allAuditTrailEvents = localDaoSession.getAuditTrailEventDao().getAuditTrailEventsForShiftOrMonth(workingShifts.getShiftId(), null, null, null);
            }
        } else {
            if (forPeriodMode) {
                allAuditTrailEvents = localDaoSession.getAuditTrailEventDao().getAuditTrailEventsForShiftOrMonth(null, month.getMonthId(), fromTimeStamp, toTimeStamp);
            } else {
                allAuditTrailEvents = localDaoSession.getAuditTrailEventDao().getAuditTrailEventsForShiftOrMonth(null, month.getMonthId(), null, null);
            }
        }
        statistics.auditTrailEvents = filterAuditTrailEvents(allAuditTrailEvents);

        return statistics;

    }

    private List<AuditTrailEvent> filterAuditTrailEvents(List<AuditTrailEvent> source) {

        LocalDaoSession localDaoSession = Globals.getInstance().getLocalDaoSession();

        List<AuditTrailEvent> filteredList = new ArrayList<>();
        for (AuditTrailEvent auditTrailEvent : source) {
            boolean filtered = true;
            switch (auditTrailEvent.getType()) {
                case PRINT_TEST_PD: {
                    TestTicketEvent testTicketEvent = localDaoSession.getTestTicketDao().load(auditTrailEvent.getExtEventId());
                    filtered = testTicketEvent.getStatus() == TestTicketEvent.Status.CHECK_PRINTED
                            || testTicketEvent.getStatus() == TestTicketEvent.Status.COMPLETED;
                    break;
                }
                case SALE:
                case SALE_WITH_ADD_PAYMENT: {
                    CPPKTicketSales cppkTicketSales = localDaoSession.getCppkTicketSaleDao().load(auditTrailEvent.getExtEventId());
                    filtered = cppkTicketSales.getProgressStatus() == ProgressStatus.CheckPrinted
                            || cppkTicketSales.getProgressStatus() == ProgressStatus.Completed;
                    break;
                }
                case RETURN: {
                    CPPKTicketReturn cppkTicketReturn = localDaoSession.getCppkTicketReturnDao().load(auditTrailEvent.getExtEventId());
                    filtered = cppkTicketReturn.getProgressStatus() == ProgressStatus.CheckPrinted
                            || cppkTicketReturn.getProgressStatus() == ProgressStatus.Completed;
                    break;
                }
                case PRINT_REPORT: {
                    filtered = true;
                    break;
                }
                case SERVICE_SALE: {
                    filtered = true;
                    break;
                }
                case FINE_SALE: {
                    FineSaleEvent fineSaleEvent = localDaoSession.getFineSaleEventDao().load(auditTrailEvent.getExtEventId());
                    filtered = fineSaleEvent.getStatus() == FineSaleEvent.Status.CHECK_PRINTED
                            || fineSaleEvent.getStatus() == FineSaleEvent.Status.COMPLETED;
                    break;
                }
            }
            if (filtered) {
                filteredList.add(auditTrailEvent);
            }
        }
        return filteredList;
    }

    public static class Statistics {
        public String shiftId;
        public String monthId;
        public Date fromDate;
        public Date toDate;
        public List<AuditTrailEvent> auditTrailEvents = new ArrayList<>();
    }
}
