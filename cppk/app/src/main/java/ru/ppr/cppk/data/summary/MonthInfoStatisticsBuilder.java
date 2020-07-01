package ru.ppr.cppk.data.summary;

import android.support.annotation.Nullable;

import com.google.common.base.Preconditions;

import java.util.Date;
import java.util.EnumSet;

import ru.ppr.cppk.Globals;
import ru.ppr.cppk.db.LocalDaoSession;
import ru.ppr.cppk.entity.event.base34.CPPKServiceSale;
import ru.ppr.cppk.entity.event.base34.CPPKTicketReturn;
import ru.ppr.cppk.entity.event.base34.CPPKTicketSales;
import ru.ppr.cppk.entity.event.base34.CashRegisterEvent;
import ru.ppr.cppk.entity.event.base34.TestTicketEvent;
import ru.ppr.cppk.entity.event.base34.TicketEventBase;
import ru.ppr.cppk.entity.event.base34.TicketSaleReturnEventBase;
import ru.ppr.cppk.entity.event.model.Cashier;
import ru.ppr.cppk.entity.event.model.Check;
import ru.ppr.cppk.entity.event.model34.ProgressStatus;
import ru.ppr.cppk.localdb.model.FineSaleEvent;
import ru.ppr.cppk.localdb.model.MonthEvent;
import ru.ppr.cppk.localdb.model.ShiftEvent;

/**
 * Билдер статистики с основной информацией о месяце.
 *
 * @author Aleksandr Brazhkin
 */
public class MonthInfoStatisticsBuilder {

    private String monthId;
    private boolean buildForLastMonth;
    private boolean buildForClosedShiftsOnly;
    ////////////////////////////////////////

    public MonthInfoStatisticsBuilder() {
    }

    /**
     * Задать id месяца
     */
    public MonthInfoStatisticsBuilder setMonthId(String monthId) {
        this.monthId = monthId;
        return this;
    }

    public MonthInfoStatisticsBuilder setBuildForLastMonth(boolean buildForLastMonth) {
        this.buildForLastMonth = buildForLastMonth;
        return this;
    }

    /**
     * Устанавливает флаг необходимости сбора сведений только для закрытых смен
     *
     * @param buildForClosedShiftsOnly {@code true} - только для закрытых смен, {@code false} - иначе.
     * @return {@code this}
     */
    public MonthInfoStatisticsBuilder setBuildForClosedShiftsOnly(boolean buildForClosedShiftsOnly) {
        this.buildForClosedShiftsOnly = buildForClosedShiftsOnly;
        return this;
    }


    public Statistics build() throws Exception {

        LocalDaoSession localDaoSession = Globals.getInstance().getLocalDaoSession();

        MonthEvent month = null;
        Date fromTimeStamp = null;
        Date toTimeStamp = null;

        if (buildForLastMonth && monthId != null) {
            throw new IllegalArgumentException("buildForLastMonth && monthId != null");
        }

        if (buildForLastMonth) {
            // Для последнего месяца
            month = localDaoSession.getMonthEventDao().getLastMonthEvent();
            if (month == null) {
                throw new IllegalStateException("last month is null");
            }
        }

        if (monthId != null) {
            // Для конкретного месяца
            month = localDaoSession.getMonthEventDao().getLastMonthByMonthId(monthId);
            if (month == null) {
                throw new IllegalStateException("month is null");
            }
        }

        /**
         * Время начала/окончания
         */
        toTimeStamp = month.getOpenDate();
        fromTimeStamp = month.getCloseDate();

        /**
         * ID месяца; Даты начала, оконачания
         */
        Statistics statistics = new Statistics();
        statistics.monthId = month.getMonthId();
        statistics.fromDate = fromTimeStamp;
        statistics.toDate = toTimeStamp;
        statistics.monthNum = month.getMonthNumber();
        statistics.isClosed = toTimeStamp != null;

        /**
         * Первый/последний документы за месяц
         */

        // https://aj.srvdev.ru/browse/CPPKPP-32090
        // Для месячной ведомости учитываем только закрытые смены
        EnumSet<ShiftEvent.Status> shiftStatuses = buildForClosedShiftsOnly ? EnumSet.of(ShiftEvent.Status.ENDED) : null;
        statistics.firstDocument = getFirstOrLastDocumentForMonth(month, true, shiftStatuses);
        statistics.lastDocument = getFirstOrLastDocumentForMonth(month, false, shiftStatuses);

        /**
         * Первая/последняя смены за месяц
         */
        EnumSet<ShiftEvent.Status> allShiftStatuses = EnumSet.of(ShiftEvent.Status.STARTED, ShiftEvent.Status.TRANSFERRED, ShiftEvent.Status.ENDED);
        EnumSet<ShiftEvent.ShiftProgressStatus> allProgressStatuses = ShiftEvent.ShiftProgressStatus.FINISHED_STATUSES;

        ShiftEvent shiftEventFirst;
        ShiftEvent shiftEventLast;

        if (buildForClosedShiftsOnly) {
            // https://aj.srvdev.ru/browse/CPPKPP-32090
            // Для месячной ведомости учитываем только закрытые смены
            shiftEventFirst = localDaoSession.getShiftEventDao().getFirstShiftEventForMonth(month.getMonthId(), EnumSet.of(ShiftEvent.Status.ENDED), allProgressStatuses);
            if (shiftEventFirst == null) {
                // Первая смена месяца не была закрыта
                // Посмотрим, открывалась ли смена в месяце вообще
                shiftEventFirst = localDaoSession.getShiftEventDao().getFirstShiftEventForMonth(month.getMonthId(), allShiftStatuses, allProgressStatuses);
                if (shiftEventFirst != null) {
                    // В месяце есть одна незакрытая смена
                    shiftEventLast = localDaoSession.getShiftEventDao().getLastShiftEventForMonth(month.getMonthId(), allShiftStatuses, allProgressStatuses);
                } else {
                    // В месяце нет смен вообще
                    shiftEventLast = null;
                }
            } else {
                shiftEventLast = localDaoSession.getShiftEventDao().getLastShiftEventForMonth(month.getMonthId(), EnumSet.of(ShiftEvent.Status.ENDED), allProgressStatuses);
            }
        } else {
            shiftEventFirst = localDaoSession.getShiftEventDao().getFirstShiftEventForMonth(month.getMonthId(), allShiftStatuses, allProgressStatuses);
            shiftEventLast = localDaoSession.getShiftEventDao().getLastShiftEventForMonth(month.getMonthId(), allShiftStatuses, allProgressStatuses);
        }
        if (shiftEventFirst != null) {
            statistics.firstShiftStatistics = new ShiftInfoStatisticsBuilder().setShiftId(shiftEventFirst.getShiftId()).build();
        }
        if (shiftEventLast != null) {
            statistics.lastShiftStatistics = new ShiftInfoStatisticsBuilder().setShiftId(shiftEventLast.getShiftId()).build();
        }

        return statistics;
    }

    /**
     * Возвращает первый/последний документ за месяц
     *
     * @param monthEvent Месяц
     * @param first      {@code true}, если нужен первый документ, {@code false} - если последний
     * @return Первый/последний документ
     */
    @Nullable
    private Document getFirstOrLastDocumentForMonth(MonthEvent monthEvent, boolean first, EnumSet<ShiftEvent.Status> shiftStatuses) {

        LocalDaoSession localDaoSession = Globals.getInstance().getLocalDaoSession();

        CPPKTicketSales cppkTicketSales;
        long cppkTicketSalesEventId;
        CPPKTicketReturn cppkTicketReturn;
        long cppkTicketReturnEventId;
        TestTicketEvent testTicketEvent;
        long testTicketEventEventId;
        CPPKServiceSale cppkServiceSale;
        long cppkServiceSaleEventId;
        FineSaleEvent fineSaleEvent;
        long fineSaleEventId;

        EnumSet<ProgressStatus> progressStatuses = EnumSet.of(ProgressStatus.CheckPrinted, ProgressStatus.Completed);
        EnumSet<FineSaleEvent.Status> fineProgressStatuses = EnumSet.of(FineSaleEvent.Status.CHECK_PRINTED, FineSaleEvent.Status.COMPLETED);

        long documentId;
        EnumSet<TestTicketEvent.Status> testPdProgressStatuses = EnumSet.of(TestTicketEvent.Status.CHECK_PRINTED, TestTicketEvent.Status.COMPLETED);
        if (first) {
            cppkTicketSales = localDaoSession.getCppkTicketSaleDao().getFirstSaleForMonth(monthEvent.getMonthId(), progressStatuses, shiftStatuses);
            cppkTicketSalesEventId = cppkTicketSales == null ? Long.MAX_VALUE : cppkTicketSales.getEventId();
            cppkTicketReturn = localDaoSession.getCppkTicketReturnDao().getFirstReturnForMonth(monthEvent.getMonthId(), progressStatuses, shiftStatuses);
            cppkTicketReturnEventId = cppkTicketReturn == null ? Long.MAX_VALUE : cppkTicketReturn.getEventId();
            testTicketEvent = localDaoSession.getTestTicketDao().getFirstTestTicketForMonth(monthEvent.getMonthId(), shiftStatuses, testPdProgressStatuses);
            testTicketEventEventId = testTicketEvent == null ? Long.MAX_VALUE : testTicketEvent.getEventId();
            cppkServiceSale = localDaoSession.getCppkServiceSaleDao().getFirstServiceSaleForMonth(monthEvent.getMonthId(), shiftStatuses);
            cppkServiceSaleEventId = cppkServiceSale == null ? Long.MAX_VALUE : cppkServiceSale.getEventId();
            fineSaleEvent = localDaoSession.getFineSaleEventDao().getFirstFineSaleEventForMonth(monthEvent.getMonthId(), shiftStatuses, fineProgressStatuses);
            fineSaleEventId = fineSaleEvent == null ? Long.MAX_VALUE : fineSaleEvent.getEventId();

            documentId = Math.min(Math.min(Math.min(Math.min(cppkTicketSalesEventId, cppkTicketReturnEventId), testTicketEventEventId), cppkServiceSaleEventId), fineSaleEventId);
            if (documentId == Long.MAX_VALUE) {
                return null;
            }
        } else {
            cppkTicketSales = localDaoSession.getCppkTicketSaleDao().getLastSaleForMonth(monthEvent.getMonthId(), progressStatuses, shiftStatuses);
            cppkTicketSalesEventId = cppkTicketSales == null ? Long.MIN_VALUE : cppkTicketSales.getEventId();
            cppkTicketReturn = localDaoSession.getCppkTicketReturnDao().getLastReturnForMonth(monthEvent.getMonthId(), progressStatuses, shiftStatuses);
            cppkTicketReturnEventId = cppkTicketReturn == null ? Long.MIN_VALUE : cppkTicketReturn.getEventId();
            testTicketEvent = localDaoSession.getTestTicketDao().getLastTestTicketForMonth(monthEvent.getMonthId(), shiftStatuses, testPdProgressStatuses);
            testTicketEventEventId = testTicketEvent == null ? Long.MIN_VALUE : testTicketEvent.getEventId();
            cppkServiceSale = localDaoSession.getCppkServiceSaleDao().getLastServiceSaleForMonth(monthEvent.getMonthId(), shiftStatuses);
            cppkServiceSaleEventId = cppkServiceSale == null ? Long.MIN_VALUE : cppkServiceSale.getEventId();
            fineSaleEvent = localDaoSession.getFineSaleEventDao().getLastFineSaleEventForMonth(monthEvent.getMonthId(), shiftStatuses, fineProgressStatuses);
            fineSaleEventId = fineSaleEvent == null ? Long.MIN_VALUE : fineSaleEvent.getEventId();

            documentId = Math.max(Math.max(Math.max(Math.max(cppkTicketSalesEventId, cppkTicketReturnEventId), testTicketEventEventId), cppkServiceSaleEventId), fineSaleEventId);
            if (documentId == Long.MIN_VALUE) {
                return null;
            }
        }

        Document document;
        if (documentId == cppkTicketSalesEventId) {
            document = new Document();
            TicketSaleReturnEventBase ticketSaleReturnEventBase = localDaoSession.getTicketSaleReturnEventBaseDao().load(cppkTicketSales.getTicketSaleReturnEventBaseId());
            Check check = localDaoSession.getCheckDao().load(ticketSaleReturnEventBase.getCheckId());
            document.printTime = check.getPrintDatetime();
            document.number = check.getOrderNumber();
            TicketEventBase ticketEventBase = localDaoSession.getTicketEventBaseDao().load(ticketSaleReturnEventBase.getTicketEventBaseId());
            ShiftEvent shiftEvent = localDaoSession.getShiftEventDao().load(ticketEventBase.getShiftEventId());
            long cashRegisterEventId = shiftEvent.getCashRegisterEventId();
            CashRegisterEvent cashRegisterEvent = localDaoSession.getCashRegisterEventDao().load(cashRegisterEventId);
            document.cashier = localDaoSession.cashierDao().load(cashRegisterEvent.getCashierId());
            return document;
        } else if (documentId == cppkTicketReturnEventId) {
            document = new Document();
            Check returnCheck = localDaoSession.getCheckDao().load(cppkTicketReturn.getCheckId());
            document.printTime = returnCheck.getPrintDatetime();
            document.number = returnCheck.getOrderNumber();
            CPPKTicketSales pdSaleEvent = localDaoSession.getCppkTicketSaleDao().load(cppkTicketReturn.getPdSaleEventId());
            TicketSaleReturnEventBase ticketSaleReturnEventBase = localDaoSession.getTicketSaleReturnEventBaseDao().load(pdSaleEvent.getTicketSaleReturnEventBaseId());
            TicketEventBase ticketEventBase = localDaoSession.getTicketEventBaseDao().load(ticketSaleReturnEventBase.getTicketEventBaseId());
            ShiftEvent shiftEvent = localDaoSession.getShiftEventDao().load(ticketEventBase.getShiftEventId());
            long cashRegisterEventId = shiftEvent.getCashRegisterEventId();
            CashRegisterEvent cashRegisterEvent = localDaoSession.getCashRegisterEventDao().load(cashRegisterEventId);
            document.cashier = localDaoSession.cashierDao().load(cashRegisterEvent.getCashierId());
            return document;
        } else if (documentId == testTicketEventEventId) {
            document = new Document();
            Check check = localDaoSession.getCheckDao().load(testTicketEvent.getCheckId());
            document.printTime = check.getPrintDatetime();
            document.number = check.getOrderNumber();
            ShiftEvent shiftEvent = localDaoSession.getShiftEventDao().load(testTicketEvent.getShiftEventId());
            long cashRegisterEventId = shiftEvent.getCashRegisterEventId();
            CashRegisterEvent cashRegisterEvent = localDaoSession.getCashRegisterEventDao().load(cashRegisterEventId);
            document.cashier = localDaoSession.cashierDao().load(cashRegisterEvent.getCashierId());
            return document;
        } else if (documentId == cppkServiceSaleEventId) {
            document = new Document();
            Check check = localDaoSession.getCheckDao().load(cppkServiceSale.getCheckId());
            document.printTime = check.getPrintDatetime();
            document.number = check.getOrderNumber();
            ShiftEvent shiftEvent = localDaoSession.getShiftEventDao().load(cppkServiceSale.getShiftEventId());
            long cashRegisterEventId = shiftEvent.getCashRegisterEventId();
            CashRegisterEvent cashRegisterEvent = localDaoSession.getCashRegisterEventDao().load(cashRegisterEventId);
            document.cashier = localDaoSession.cashierDao().load(cashRegisterEvent.getCashierId());
            return document;
        } else if (documentId == fineSaleEventId) {
            document = new Document();
            Check check = localDaoSession.getCheckDao().load(fineSaleEvent.getCheckId());
            document.printTime = check.getPrintDatetime();
            document.number = check.getOrderNumber();
            ShiftEvent shiftEvent = localDaoSession.getShiftEventDao().load(fineSaleEvent.getShiftEventId());
            Preconditions.checkNotNull(shiftEvent);
            long cashRegisterEventId = shiftEvent.getCashRegisterEventId();
            CashRegisterEvent cashRegisterEvent = localDaoSession.getCashRegisterEventDao().load(cashRegisterEventId);
            document.cashier = localDaoSession.cashierDao().load(cashRegisterEvent.getCashierId());
            return document;
        }

        // В теории, сюда не доходим никогда
        return null;
    }

    public static class Statistics {
        public String monthId;
        public Date fromDate;
        public Date toDate;
        public int monthNum;
        public Document firstDocument = null;
        public Document lastDocument = null;
        public boolean isClosed;
        public ShiftInfoStatisticsBuilder.Statistics firstShiftStatistics;
        public ShiftInfoStatisticsBuilder.Statistics lastShiftStatistics;
    }

    /**
     * Первый/последний документ за месяц
     */
    public static class Document {
        public Date printTime;
        public int number;
        public Cashier cashier;
    }
}
