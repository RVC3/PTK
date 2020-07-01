package ru.ppr.cppk.data.summary;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import ru.ppr.cppk.Globals;
import ru.ppr.cppk.db.LocalDaoSession;
import ru.ppr.cppk.entity.event.base34.CPPKTicketSales;
import ru.ppr.cppk.entity.event.base34.TicketSaleReturnEventBase;
import ru.ppr.cppk.entity.event.model.ExemptionForEvent;
import ru.ppr.cppk.entity.event.model34.ProgressStatus;
import ru.ppr.cppk.localdb.model.ShiftEvent;

/**
 * Билдер статистики по продажам на ЭТТ.
 *
 * @author Aleksandr Brazhkin
 */
public class SalesForEttStatisticsBuilder {

    private String shiftId;
    private boolean buildForLastShift;
    ////////////////////////////////////////

    public SalesForEttStatisticsBuilder() {

    }

    /**
     * Задать id смены
     */
    public SalesForEttStatisticsBuilder setShiftId(String shiftId) {
        this.shiftId = shiftId;
        return this;
    }

    public SalesForEttStatisticsBuilder setBuildForLastShift(boolean buildForLastShift) {
        this.buildForLastShift = buildForLastShift;
        return this;
    }

    public Statistics build() throws Exception {

        LocalDaoSession localDaoSession = Globals.getInstance().getLocalDaoSession();

        ShiftEvent workingShifts = null;

        if (buildForLastShift && shiftId != null) {
            throw new IllegalArgumentException("buildForLastShift && shiftId != null");
        }

        if (buildForLastShift) {
            // Для последней смены
            workingShifts = localDaoSession.getShiftEventDao().getLastShiftEvent(ShiftEvent.ShiftProgressStatus.FINISHED_STATUSES);
            if (workingShifts == null) {
                throw new IllegalStateException("last shift is null");
            }
        }

        if (shiftId != null) {
            // Для конкретной смены
            workingShifts = localDaoSession.getShiftEventDao().getLastCashRegisterWorkingShiftByShiftId(shiftId, ShiftEvent.ShiftProgressStatus.FINISHED_STATUSES);
            if (workingShifts == null) {
                throw new IllegalStateException("shift is null");
            }
        }

        /**
         * Продажи по ЭТТ
         */
        Statistics statistics = new Statistics();

        List<ProgressStatus> statuses = Arrays.asList(ProgressStatus.Completed, ProgressStatus.CheckPrinted);
        List<CPPKTicketSales> cppkTicketSalesList = localDaoSession.getCppkTicketSaleDao().getSaleEventsForEtt(workingShifts.getShiftId(), statuses);

        statistics.ettPdCount = cppkTicketSalesList.size();
        statistics.cppkTicketSalesList = cppkTicketSalesList;
        for (CPPKTicketSales cppkTicketSales : cppkTicketSalesList) {
            TicketSaleReturnEventBase ticketSaleReturnEventBase = localDaoSession.getTicketSaleReturnEventBaseDao().load(cppkTicketSales.getTicketSaleReturnEventBaseId());
            ExemptionForEvent exemptionForEvent = localDaoSession.exemptionDao().load(ticketSaleReturnEventBase.getExemptionForEventId());
            if (exemptionForEvent != null) {
                statistics.lossSum = statistics.lossSum.add(exemptionForEvent.getLossSumm());
            }
        }

        return statistics;

    }

    public static class Statistics {
        /**
         * Количество билетов по ЭТТ
         */
        public int ettPdCount;
        /**
         * Выпадающий доход
         */
        public BigDecimal lossSum = BigDecimal.ZERO;
        /**
         * Продажи по ЭТТ
         */
        public List<CPPKTicketSales> cppkTicketSalesList;
    }
}
