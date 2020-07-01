package ru.ppr.cppk.data.summary;

import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;

import ru.ppr.cppk.db.LocalDaoSession;
import ru.ppr.cppk.entity.event.base34.CPPKTicketReturn;
import ru.ppr.cppk.entity.event.base34.CPPKTicketSales;
import ru.ppr.cppk.entity.event.base34.CashRegisterEvent;
import ru.ppr.cppk.entity.event.base34.TestTicketEvent;
import ru.ppr.cppk.entity.event.base34.TicketEventBase;
import ru.ppr.cppk.entity.event.base34.TicketSaleReturnEventBase;
import ru.ppr.cppk.entity.event.model.Cashier;
import ru.ppr.cppk.entity.event.model.Check;
import ru.ppr.cppk.entity.event.model34.ProgressStatus;
import ru.ppr.cppk.entity.settings.ReportType;
import ru.ppr.cppk.localdb.model.MonthEvent;
import ru.ppr.cppk.localdb.model.ShiftEvent;
import ru.ppr.cppk.localdb.model.TicketTapeEvent;

/**
 * Класс - билдер статистики по чековой ленте
 *
 * @author Aleksandr Brazhkin
 */
public class TicketTapeStatisticsBuilder {

    private final LocalDaoSession localDaoSession;

    /**
     * UUID смены
     */
    private String shiftId;
    /**
     * UUID месяца
     */
    private String monthId;
    /**
     * UUID ленты
     */
    private String ticketTapeId;
    /**
     * Флаг сбора статистики для последней смены
     */
    private boolean buildForLastShift;
    /**
     * Флаг сбора статистики для последнего месяца
     */
    private boolean buildForLastMonth;
    /**
     * Флаг сбора статистики только для закрытых смен
     */
    private boolean buildForClosedShiftsOnly;
    /**
     * Флаг сбора статистики для событий вне смены
     */
    private boolean buildForOutOfShiftEvents = true;
    /**
     * Флаг сбора статистики по последней бобине
     */
    private boolean buildForLastTicketTape;
    ////////////////////////////////////////

    public TicketTapeStatisticsBuilder(LocalDaoSession localDaoSession) {
        this.localDaoSession = localDaoSession;
    }

    /**
     * Задать id смены
     */
    public TicketTapeStatisticsBuilder setShiftId(String shiftId) {
        this.shiftId = shiftId;
        return this;
    }

    /**
     * Задать флаг, сбора статистики для последней смены
     *
     * @param buildForLastShift
     * @return
     */
    public TicketTapeStatisticsBuilder setBuildForLastShift(boolean buildForLastShift) {
        this.buildForLastShift = buildForLastShift;
        return this;
    }

    /**
     * Задать id месяца
     */
    public TicketTapeStatisticsBuilder setMonthId(String monthId) {
        this.monthId = monthId;
        return this;
    }

    /**
     * Задать флаг сбора статистики для последнего месяца
     *
     * @param buildForLastMonth
     * @return
     */
    public TicketTapeStatisticsBuilder setBuildForLastMonth(boolean buildForLastMonth) {
        this.buildForLastMonth = buildForLastMonth;
        return this;
    }

    /**
     * Устанавливает флаг необходимости сбора сведений только для закрытых смен
     *
     * @param buildForClosedShiftsOnly {@code true} - только для закрытых смен, {@code false} - иначе.
     * @return {@code this}
     */
    public TicketTapeStatisticsBuilder setBuildForClosedShiftsOnly(boolean buildForClosedShiftsOnly) {
        this.buildForClosedShiftsOnly = buildForClosedShiftsOnly;
        return this;
    }

    /**
     * Устанавливает флаг необходимости сбора сведений вне смен
     *
     * @param buildForOutOfShiftEvents {@code true} - учитывать события вне смен, {@code false} - иначе.
     * @return {@code this}
     */
    public TicketTapeStatisticsBuilder setBuildForOutOfShiftEvents(boolean buildForOutOfShiftEvents) {
        this.buildForOutOfShiftEvents = buildForOutOfShiftEvents;
        return this;
    }

    /**
     * Задать id бобины
     *
     * @param ticketTapeId
     * @return
     */
    public TicketTapeStatisticsBuilder setTicketTapeId(String ticketTapeId) {
        this.ticketTapeId = ticketTapeId;
        return this;
    }

    /**
     * Задать флаг сбора статистики для последней бобины
     *
     * @param buildForLastTicketTape
     * @return
     */
    public TicketTapeStatisticsBuilder setBuildForLastTicketTape(boolean buildForLastTicketTape) {
        this.buildForLastTicketTape = buildForLastTicketTape;
        return this;
    }

    /**
     * Сбилдить статистику
     *
     * @return
     */
    public Statistics build() {

        boolean forShiftMode = false;
        boolean forMonthMode = false;
        boolean forTicketTapeMode = false;
        boolean forAllMode = true;
        ShiftEvent workingShifts = null;
        MonthEvent month = null;
        TicketTapeEvent ticketTapeStartEvent = null;
        TicketTapeEvent ticketTapeEndEvent = null;
        Date fromTimeStamp = null;
        Date toTimeStamp = null;

        if (buildForLastShift && buildForLastMonth ||
                buildForLastShift && buildForLastTicketTape ||
                buildForLastMonth && buildForLastTicketTape) {
            throw new IllegalArgumentException("invalid mode");
        }

        if (shiftId != null && monthId != null ||
                shiftId != null && ticketTapeId != null ||
                monthId != null && ticketTapeId != null) {
            throw new IllegalArgumentException("invalid mode");
        }

        if ((buildForLastShift || buildForLastMonth || buildForLastTicketTape) && (shiftId != null || monthId != null || ticketTapeId != null)) {
            throw new IllegalArgumentException("invalid mode");
        }

        if (buildForLastShift) {
            // Для последней смены
            workingShifts = localDaoSession.getShiftEventDao().getLastShiftEvent(ShiftEvent.ShiftProgressStatus.FINISHED_STATUSES);
            if (workingShifts == null) {
                throw new IllegalStateException("last shift is null");
            }
            forShiftMode = true;
            forMonthMode = forTicketTapeMode = forAllMode = !forShiftMode;
        }

        if (buildForLastMonth) {
            // Для последнего месяца
            month = localDaoSession.getMonthEventDao().getLastMonthEvent();
            if (month == null) {
                throw new IllegalStateException("last month is null");
            }
            forMonthMode = true;
            forShiftMode = forTicketTapeMode = forAllMode = !forMonthMode;
        }

        if (buildForLastTicketTape) {
            // Для последней бобины
            ticketTapeStartEvent = localDaoSession.getTicketTapeEventDao().getLastTicketTapeEvent();
            if (ticketTapeStartEvent == null) {
                throw new IllegalStateException("last ticketTapeStartEvent is null");
            }
            forTicketTapeMode = true;
            forShiftMode = forMonthMode = forAllMode = !forTicketTapeMode;
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

        if (ticketTapeId != null) {
            // Для конкретной бобины
            ticketTapeStartEvent = localDaoSession.getTicketTapeEventDao().getStartTicketTapeEventByTicketTapeId(ticketTapeId);
            if (ticketTapeStartEvent == null) {
                throw new IllegalStateException("ticketTapeStartEvent is null");
            }
            forTicketTapeMode = true;
            forShiftMode = forMonthMode = forAllMode = !forTicketTapeMode;
        }

        if (workingShifts == null && month == null && ticketTapeStartEvent == null) {
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

        if (forTicketTapeMode) {
            // Для бобины получаем событие изъятия
            ticketTapeEndEvent = localDaoSession.getTicketTapeEventDao().getEndTicketTapeEventByTicketTapeId(ticketTapeId);
            if (ticketTapeEndEvent == null) {
                //throw new IllegalStateException("ticketTapeEndEvent is null");
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
        } else if (forTicketTapeMode) {
            fromTimeStamp = ticketTapeStartEvent.getStartTime();
            toTimeStamp = ticketTapeEndEvent == null ? null : ticketTapeEndEvent.getEndTime();
        } else if (forAllMode) {
            fromTimeStamp = null;
            toTimeStamp = null;
        }

        /**
         * ID смены/месяца/бобины; Даты начала, оконачания
         */
        Statistics statistics = new Statistics();
        statistics.shiftId = forShiftMode ? workingShifts.getShiftId() : null;
        statistics.ticketTapeId = forTicketTapeMode ? ticketTapeStartEvent.getTicketTapeId() : null;
        statistics.monthId = (forMonthMode || forMonthMode) ? month.getMonthId() : null;
        statistics.fromDate = fromTimeStamp;
        statistics.toDate = toTimeStamp;

        /**
         * Расход билетной ленты
         */
        List<TicketTapeEvent> ticketTapeEvents = null;
        if (forShiftMode) {
            ticketTapeEvents = localDaoSession.getTicketTapeEventDao().getFinishedTicketTapeEventsForShift(workingShifts.getShiftId());
        } else if (forMonthMode) {
            ticketTapeEvents = localDaoSession.getTicketTapeEventDao().getFinishedTicketTapeEventsForMonth(
                    month.getMonthId(),
                    // https://aj.srvdev.ru/browse/CPPKPP-32090
                    // Для месячной ведомости учитываем только закрытые смены
                    buildForClosedShiftsOnly ? EnumSet.of(ShiftEvent.Status.ENDED) : null,
                    buildForOutOfShiftEvents
            );
        } else if (forTicketTapeMode) {
            ticketTapeEvents = Collections.singletonList(ticketTapeStartEvent);
        } else if (forAllMode) {
            ticketTapeEvents = localDaoSession.getTicketTapeEventDao().getFinishedTicketTapeEvents();
        }

        for (TicketTapeEvent ticketTapeEvent : ticketTapeEvents) {
            TicketTapeInfo ticketTapeInfo = new TicketTapeInfo();
            ticketTapeInfo.ticketTapeEvent = ticketTapeEvent;
            ticketTapeInfo.firstDocument = getFirstOrLastDocumentForTicketTape(ticketTapeEvent, true);
            ticketTapeInfo.lastDocument = getFirstOrLastDocumentForTicketTape(ticketTapeEvent, false);
            statistics.ticketTapeEvents.add(ticketTapeInfo);
        }


        for (TicketTapeEvent ticketTapeEvent : ticketTapeEvents) {
            statistics.finishedTicketTapeConsumptionInMillimeters += localDaoSession.getTicketTapeEventDao().getTicketTapeLength(ticketTapeEvent.getTicketTapeId());
        }

        /**
         * Расход билетной ленты для бобины
         */
        if (forTicketTapeMode) {
            // Получаем расход ленты для бобины
            // Если события окончания билетной ленты нет, оставляем null
            if (ticketTapeEndEvent != null) {
                statistics.paperConsumption = ticketTapeEndEvent.getPaperConsumption();
            }
        }

        /**
         * Расход билетной ленты за смену
         */
        if (forShiftMode) {
            // Получаем начальные/конечные показания одометра за смену
            // Если события закрытия смены нет, оставляем null
            ShiftEvent lastShiftEvent = localDaoSession.getShiftEventDao().getLastCashRegisterWorkingShiftByShiftId(workingShifts.getShiftId(), ShiftEvent.ShiftProgressStatus.FINISHED_STATUSES);
            if (lastShiftEvent.getStatus() == ShiftEvent.Status.ENDED) {
                statistics.paperConsumption = lastShiftEvent.getPaperConsumption();
            }
        }

        /**
         * Количество отчетов на лентах
         */
        List<String> ticketTapeIds = new ArrayList<>();
        for (TicketTapeEvent ticketTapeEvent : ticketTapeEvents) {
            ticketTapeIds.add(ticketTapeEvent.getTicketTapeId());
        }
        HashMap<ReportType, Integer> reportsCount;
        if (!ticketTapeIds.isEmpty()) {
            reportsCount = localDaoSession.getTicketTapeEventDao().getPrintReportEventsCountForTicketTapes(ticketTapeIds);
        } else {
            reportsCount = new HashMap<>();
        }

        int auditTrailCount = reportsCount.containsKey(ReportType.AuditTrail) ? reportsCount.get(ReportType.AuditTrail) : 0;
        int testShiftSheetCount = reportsCount.containsKey(ReportType.TestShiftShit) ? reportsCount.get(ReportType.TestShiftShit) : 0;
        int shiftSheetCount = reportsCount.containsKey(ReportType.ShiftShit) ? reportsCount.get(ReportType.ShiftShit) : 0;
        int shiftDiscountSheetCount = reportsCount.containsKey(ReportType.DiscountedShiftShit) ? reportsCount.get(ReportType.DiscountedShiftShit) : 0;
        int shiftClearingSheetCount = reportsCount.containsKey(ReportType.SheetShiftBlanking) ? reportsCount.get(ReportType.SheetShiftBlanking) : 0;
        int monthSheetCount = reportsCount.containsKey(ReportType.MonthlySheet) ? reportsCount.get(ReportType.MonthlySheet) : 0;
        int monthDiscountSheetCount = reportsCount.containsKey(ReportType.DiscountedMonthlySheet) ? reportsCount.get(ReportType.DiscountedMonthlySheet) : 0;
        int monthClearingSheetCount = reportsCount.containsKey(ReportType.SheetBlankingMonth) ? reportsCount.get(ReportType.SheetBlankingMonth) : 0;
        int testMonthSheetCount = reportsCount.containsKey(ReportType.TestMonthShit) ? reportsCount.get(ReportType.TestMonthShit) : 0;
        int salesForEttLogCount = reportsCount.containsKey(ReportType.SalesForEttLog) ? reportsCount.get(ReportType.SalesForEttLog) : 0;

        statistics.reportsCount.auditTrailCount = auditTrailCount;
        statistics.reportsCount.testShiftSheetCount = testShiftSheetCount;
        statistics.reportsCount.shiftSheetCount = shiftSheetCount;
        statistics.reportsCount.shiftDiscountSheetCount = shiftDiscountSheetCount;
        statistics.reportsCount.shiftClearingSheetCount = shiftClearingSheetCount;
        statistics.reportsCount.monthSheetCount = monthSheetCount;
        statistics.reportsCount.monthDiscountSheetCount = monthDiscountSheetCount;
        statistics.reportsCount.monthClearingSheetCount = monthClearingSheetCount;
        statistics.reportsCount.testMonthSheetCount = testMonthSheetCount;
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

        /**
         * Количество продаж на лентах
         */
        if (!ticketTapeIds.isEmpty()) {
            statistics.salesCount = localDaoSession.getTicketTapeEventDao().getCPPKTicketSalesCountForTicketTapes(ticketTapeIds);
        }

        /**
         * Количество возвратов на лентах
         */
        if (!ticketTapeIds.isEmpty()) {
            statistics.returnsCount = localDaoSession.getTicketTapeEventDao().getCPPKTicketReturnsCountForTicketTapes(ticketTapeIds);
        }

        /**
         * Количество пробных ПД на лентах
         */
        if (!ticketTapeIds.isEmpty()) {
            statistics.testTicketsCount = localDaoSession.getTicketTapeEventDao().getTestTicketEventsCountForTicketTapes(ticketTapeIds);
        }

        /**
         * Количество оформленных услуг на лентах
         */
        if (!ticketTapeIds.isEmpty()) {
            statistics.servicesCount = localDaoSession.getCppkServiceSaleDao().getServiceSalesCountForTicketTapes(ticketTapeIds);
        }

        return statistics;
    }

    /**
     * Вернет первое или последнее событие по билетной ленте (снятие или установка)
     *
     * @param ticketTapeEvent Билетная лента
     * @param first           {@code true}, если нужен первый документ, {@code false} - если последний
     * @return Первый/последний документ
     */
    @Nullable
    private Document getFirstOrLastDocumentForTicketTape(TicketTapeEvent ticketTapeEvent, boolean first) {

        CPPKTicketSales cppkTicketSales;
        long cppkTicketSalesEventId;
        CPPKTicketReturn cppkTicketReturn;
        long cppkTicketReturnEventId;
        TestTicketEvent testTicketEvent;
        long testTicketEventEventId;

        EnumSet<ProgressStatus> progressStatuses = EnumSet.of(ProgressStatus.CheckPrinted, ProgressStatus.Completed);
        EnumSet<ShiftEvent.Status> shiftStatuses = null;
        EnumSet<TestTicketEvent.Status> testPdStatuses = EnumSet.of(TestTicketEvent.Status.CHECK_PRINTED, TestTicketEvent.Status.COMPLETED);

        long documentId;
        if (first) {
            cppkTicketSales = localDaoSession.getCppkTicketSaleDao().getFirstSaleForTicketTape(ticketTapeEvent.getTicketTapeId(), progressStatuses, shiftStatuses);
            cppkTicketSalesEventId = cppkTicketSales == null ? Long.MAX_VALUE : cppkTicketSales.getEventId();
            cppkTicketReturn = localDaoSession.getCppkTicketReturnDao().getFirstReturnForTicketTape(ticketTapeEvent.getTicketTapeId(), progressStatuses, shiftStatuses);
            cppkTicketReturnEventId = cppkTicketReturn == null ? Long.MAX_VALUE : cppkTicketReturn.getEventId();
            testTicketEvent = localDaoSession.getTestTicketDao().getFirstTestTicketForTicketTape(ticketTapeEvent.getTicketTapeId(), shiftStatuses, testPdStatuses);
            testTicketEventEventId = testTicketEvent == null ? Long.MAX_VALUE : testTicketEvent.getEventId();

            documentId = Math.min(Math.min(cppkTicketSalesEventId, cppkTicketReturnEventId), testTicketEventEventId);
            if (documentId == Long.MAX_VALUE) {
                return null;
            }
        } else {
            cppkTicketSales = localDaoSession.getCppkTicketSaleDao().getLastSaleForTicketTape(ticketTapeEvent.getTicketTapeId(), progressStatuses, shiftStatuses);
            cppkTicketSalesEventId = cppkTicketSales == null ? Long.MIN_VALUE : cppkTicketSales.getEventId();
            cppkTicketReturn = localDaoSession.getCppkTicketReturnDao().getLastReturnForTicketTape(ticketTapeEvent.getTicketTapeId(), progressStatuses, shiftStatuses);
            cppkTicketReturnEventId = cppkTicketReturn == null ? Long.MIN_VALUE : cppkTicketReturn.getEventId();
            testTicketEvent = localDaoSession.getTestTicketDao().getLastTestTicketForTicketTape(ticketTapeEvent.getTicketTapeId(), shiftStatuses, testPdStatuses);
            testTicketEventEventId = testTicketEvent == null ? Long.MIN_VALUE : testTicketEvent.getEventId();

            documentId = Math.max(Math.max(cppkTicketSalesEventId, cppkTicketReturnEventId), testTicketEventEventId);
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
        }

        // В теории, сюда не доходим никогда
        return null;
    }

    /**
     * объект статистики билетной ленты
     */
    public static class Statistics {
        /**
         * UUID смены
         */
        public String shiftId;
        /**
         * UUID месяца
         */
        public String monthId;
        /**
         * UUID билетной ленты
         */
        public String ticketTapeId;
        /**
         * Дата начала
         */
        public Date fromDate;
        /**
         * дата конца
         */
        public Date toDate;
        /**
         * Информация по количеству напечатанных отчетов
         */
        public ReportsCount reportsCount = new ReportsCount();
        /**
         * События установки/замены билетной ленты за период
         */
        public List<TicketTapeInfo> ticketTapeEvents = new ArrayList<>();
        /**
         * Расход билетной ленты, мм.
         * Остается {@code null}, если в БД нет данных,
         * т.е. нет события закрытия смены (для смены)
         * или нет события окончания билетной ленты (для бобины)
         * Если {@code null}, брать текущие значения из {@link ru.ppr.cppk.helpers.PaperUsageCounter}
         */
        public Long paperConsumption;
        /**
         * Количество миллиметров уже закончившихся бобин (текущаяя болина сюда не входит)
         */
        public long finishedTicketTapeConsumptionInMillimeters;
        /**
         * Количество тестовых ПД
         */
        public int testTicketsCount;
        /**
         * Количество чеков продаж
         */
        public int salesCount;
        /**
         * Количество чеков аннулирования
         */
        public int returnsCount;
        /**
         * Количество оформленных услуг
         */
        public int servicesCount;
    }

    /**
     * Контейнет "Документ"
     */
    public static class Document {
        /**
         * Дата\время печати
         */
        public Date printTime;
        /**
         * номер документа
         */
        public int number;
        /**
         * Кассир, печатавший документ
         */
        public Cashier cashier;
    }

    /**
     * Класс - контейнер Информации по билетной ленте
     */
    public static class TicketTapeInfo {
        /**
         * Событие учета Билетной ленты
         */
        public TicketTapeEvent ticketTapeEvent;
        /**
         * Первый документ
         */
        public Document firstDocument = null;
        /**
         * Последний документ
         */
        public Document lastDocument = null;
    }

    /**
     * Обект-хранилище поличества напечатанных отчетов за какой-либо период
     */
    public static class ReportsCount {
        /**
         * общее количество
         */
        public int totalCount;
        /**
         * Количество контрольных журналов
         */
        public int auditTrailCount;
        /**
         * Количество пробных сменных ведомостей
         */
        public int testShiftSheetCount;
        /**
         * Количество сменных ведомостей
         */
        public int shiftSheetCount;
        /**
         * Количество льготных сменных ведосмостей
         */
        public int shiftDiscountSheetCount;
        /**
         * Количество Ведомостей гашения смены
         */
        public int shiftClearingSheetCount;
        /**
         * Количество месячных ведомостей
         */
        public int monthSheetCount;
        /**
         * Количество пробных месячных ведомостей
         */
        public int testMonthSheetCount;
        /**
         * Количество Журналов оформления по ЭТТ
         */
        public int salesForEttLogCount;
        /**
         * Количество льготных месячных ведомостей
         */
        public int monthDiscountSheetCount;
        /**
         * Количество ведомостей гажения месяца
         */
        public int monthClearingSheetCount;
    }
}
