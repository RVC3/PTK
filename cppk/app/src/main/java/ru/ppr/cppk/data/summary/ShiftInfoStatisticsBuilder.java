package ru.ppr.cppk.data.summary;

import android.support.annotation.Nullable;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;

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
 * Билдер статистики с основной информацией о смене.
 *
 * @author Aleksandr Brazhkin
 */
public class ShiftInfoStatisticsBuilder {

    /**
     * Идентификатор смены
     */
    private String shiftId;
    /**
     * Флаг, что строим за последнюю смену
     */
    private boolean buildForLastShift;
    ////////////////////////////////////////

    public ShiftInfoStatisticsBuilder() {
    }

    /**
     * Устанавливает id смены для сбора статистики
     *
     * @param shiftId Идентификатор смены
     * @return Билдер статистики
     */
    public ShiftInfoStatisticsBuilder setShiftId(String shiftId) {
        this.shiftId = shiftId;
        return this;
    }

    /**
     * Устанавливает флаг, что следует собирать статистику для последней смены
     *
     * @param buildForLastShift
     * @return Билдер статистики
     */
    public ShiftInfoStatisticsBuilder setBuildForLastShift(boolean buildForLastShift) {
        this.buildForLastShift = buildForLastShift;
        return this;
    }

    /**
     * Выполняет построение статистики.
     *
     * @return Статистика по смене
     * @throws IllegalStateException    Если невозможно построить статистику
     * @throws IllegalArgumentException Если некорректно указаны параметры
     */
    public Statistics build() throws IllegalStateException, IllegalArgumentException {

        LocalDaoSession localDaoSession = Globals.getInstance().getLocalDaoSession();


        ShiftEvent workingShifts = null;
        MonthEvent month = null;
        Date fromTimeStamp = null;
        Date toTimeStamp = null;

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

        // Для смены получаем месяц
        month = localDaoSession.getMonthEventDao().getMonthEventById(workingShifts.getMonthEventId());
        if (month == null) {
            throw new IllegalStateException("month is null");
        }

        /**
         * Время начала/окончания
         */
        fromTimeStamp = workingShifts.getStartTime();
        toTimeStamp = workingShifts.getCloseTime();

        /**
         * ID смены/месяца; Даты начала, оконачания
         */
        Statistics statistics = new Statistics();
        statistics.shiftId = workingShifts.getShiftId();
        statistics.monthId = month.getMonthId();
        statistics.fromDate = fromTimeStamp;
        statistics.toDate = toTimeStamp;
        statistics.shiftNum = workingShifts.getShiftNumber();

        /**
         * Первый/послдений документы за смену
         */
        statistics.firstDocument = getFirstOrLastDocumentForShift(workingShifts, true);
        statistics.lastDocument = getFirstOrLastDocumentForShift(workingShifts, false);

        /**
         * Сумма в ФР за смену
         */
        ShiftEvent workingShiftEnd = localDaoSession.getShiftEventDao().getLastCashRegisterWorkingShiftByShiftId(workingShifts.getShiftId(), ShiftEvent.ShiftProgressStatus.FINISHED_STATUSES);
        if (workingShiftEnd == null) {
            throw new IllegalStateException("workingShiftEnd is null");
        }
        if (workingShiftEnd.getStatus() == ShiftEvent.Status.ENDED) {
            // Берем сумму в ФР из БД, только если смена уже закрыта
            statistics.cashInFR = workingShiftEnd.getCashInFR();
        }

        /**
         * Список кассиров за смену
         */
        List<ShiftEvent> allShiftEvents = localDaoSession.getShiftEventDao().getAllEventsForShift(workingShifts.getShiftId(), ShiftEvent.ShiftProgressStatus.FINISHED_STATUSES);
        for (ShiftEvent shift : allShiftEvents) {
            CashRegisterEvent cashRegisterEvent = localDaoSession.getCashRegisterEventDao().load(shift.getCashRegisterEventId());
            Cashier cashier = localDaoSession.cashierDao().load(cashRegisterEvent.getCashierId());
            if (!statistics.cashiers.contains(cashier)) {
                statistics.cashiers.add(cashier);
            }
        }
        return statistics;

    }

    /**
     * Возвращает первый/последний документ для смены
     *
     * @param workingShift Смена
     * @param first        {@code true}, если нужен первый документ, {@code false} - если последний
     * @return Первый/последний документ
     */
    @Nullable
    private Document getFirstOrLastDocumentForShift(ShiftEvent workingShift, boolean first) {

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
        EnumSet<TestTicketEvent.Status> testPdProgressStatuses = EnumSet.of(TestTicketEvent.Status.CHECK_PRINTED, TestTicketEvent.Status.COMPLETED);

        long documentId;
        if (first) {
            cppkTicketSales = localDaoSession.getCppkTicketSaleDao().getFirstSaleForShift(workingShift.getShiftId(), progressStatuses);
            cppkTicketSalesEventId = cppkTicketSales == null ? Long.MAX_VALUE : cppkTicketSales.getEventId();
            cppkTicketReturn = localDaoSession.getCppkTicketReturnDao().getFirstReturnForShift(workingShift.getShiftId(), progressStatuses);
            cppkTicketReturnEventId = cppkTicketReturn == null ? Long.MAX_VALUE : cppkTicketReturn.getEventId();
            testTicketEvent = localDaoSession.getTestTicketDao().getFirstTestTicketForShift(workingShift.getShiftId(), testPdProgressStatuses);
            testTicketEventEventId = testTicketEvent == null ? Long.MAX_VALUE : testTicketEvent.getEventId();
            cppkServiceSale = localDaoSession.getCppkServiceSaleDao().getFirstServiceSaleForShift(workingShift.getShiftId());
            cppkServiceSaleEventId = cppkServiceSale == null ? Long.MAX_VALUE : cppkServiceSale.getEventId();
            fineSaleEvent = localDaoSession.getFineSaleEventDao().getFirstFineSaleEventForShift(workingShift.getShiftId(), fineProgressStatuses);
            fineSaleEventId = fineSaleEvent == null ? Long.MAX_VALUE : fineSaleEvent.getEventId();

            documentId = Math.min(Math.min(Math.min(Math.min(cppkTicketSalesEventId, cppkTicketReturnEventId), testTicketEventEventId), cppkServiceSaleEventId), fineSaleEventId);
            if (documentId == Long.MAX_VALUE) {
                return null;
            }
        } else {
            cppkTicketSales = localDaoSession.getCppkTicketSaleDao().getLastSaleForShift(workingShift.getShiftId(), progressStatuses);
            cppkTicketSalesEventId = cppkTicketSales == null ? Long.MIN_VALUE : cppkTicketSales.getEventId();
            cppkTicketReturn = localDaoSession.getCppkTicketReturnDao().getLastReturnForShift(workingShift.getShiftId(), progressStatuses);
            cppkTicketReturnEventId = cppkTicketReturn == null ? Long.MIN_VALUE : cppkTicketReturn.getEventId();
            testTicketEvent = localDaoSession.getTestTicketDao().getLastTestTicketForShift(workingShift.getShiftId(), testPdProgressStatuses);
            testTicketEventEventId = testTicketEvent == null ? Long.MIN_VALUE : testTicketEvent.getEventId();
            cppkServiceSale = localDaoSession.getCppkServiceSaleDao().getLastServiceSaleForShift(workingShift.getShiftId());
            cppkServiceSaleEventId = cppkServiceSale == null ? Long.MIN_VALUE : cppkServiceSale.getEventId();
            fineSaleEvent = localDaoSession.getFineSaleEventDao().getLastFineSaleEventForShift(workingShift.getShiftId(), fineProgressStatuses);
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
            long cashRegisterEventId = localDaoSession.getShiftEventDao().load(fineSaleEvent.getShiftEventId()).getCashRegisterEventId();
            CashRegisterEvent cashRegisterEvent = localDaoSession.getCashRegisterEventDao().load(cashRegisterEventId);
            document.cashier = localDaoSession.cashierDao().load(cashRegisterEvent.getCashierId());
            return document;
        }

        // В теории, сюда не доходим никогда
        return null;
    }

    /**
     * Информация по смене
     */
    public static class Statistics {
        public String shiftId;
        public String monthId;
        public Date fromDate;
        public Date toDate;
        public int shiftNum;
        public Document firstDocument = null;
        public Document lastDocument = null;
        public List<Cashier> cashiers = new ArrayList<>();
        public BigDecimal cashInFR = null;
    }

    /**
     * Первый/последний документ за смену
     */
    public static class Document {
        public Date printTime;
        public int number;
        public Cashier cashier;
    }
}
