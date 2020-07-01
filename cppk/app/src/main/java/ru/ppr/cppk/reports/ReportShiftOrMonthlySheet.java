package ru.ppr.cppk.reports;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import ru.ppr.cppk.data.summary.FineSaleStatisticsBuilder;
import ru.ppr.cppk.data.summary.MonthInfoStatisticsBuilder;
import ru.ppr.cppk.data.summary.PdStatisticsBuilder;
import ru.ppr.cppk.data.summary.ReportCountStatisticsBuilder;
import ru.ppr.cppk.data.summary.ShiftInfoStatisticsBuilder;
import ru.ppr.cppk.data.summary.TicketTapeStatisticsBuilder;
import ru.ppr.cppk.data.summary.UpdateStatisticsBuilder;
import ru.ppr.cppk.di.Di;
import ru.ppr.cppk.entity.event.model34.TrainInfo;
import ru.ppr.cppk.entity.settings.ReportType;
import ru.ppr.cppk.localdb.model.TicketTapeEvent;
import ru.ppr.cppk.localdb.model.UpdateEvent;
import ru.ppr.cppk.localdb.model.UpdateEventType;
import ru.ppr.cppk.printer.rx.operation.PrintMonthSheetFooterOperation;
import ru.ppr.cppk.printer.rx.operation.shiftOrMonthSheet.CheckedPdCountTpl;
import ru.ppr.cppk.printer.rx.operation.shiftOrMonthSheet.FinesTpl;
import ru.ppr.cppk.printer.rx.operation.shiftOrMonthSheet.MonthInfoTpl;
import ru.ppr.cppk.printer.rx.operation.shiftOrMonthSheet.MonthStatesTpl;
import ru.ppr.cppk.printer.rx.operation.shiftOrMonthSheet.PdCountTpl;
import ru.ppr.cppk.printer.rx.operation.shiftOrMonthSheet.PrintShiftOrMonthSheetOperation;
import ru.ppr.cppk.printer.rx.operation.shiftOrMonthSheet.ProfitTpl;
import ru.ppr.cppk.printer.rx.operation.shiftOrMonthSheet.RouteCarrierStatisticsTpl;
import ru.ppr.cppk.printer.rx.operation.shiftOrMonthSheet.RouteCarrierTrainCategoryStatisticsTpl;
import ru.ppr.cppk.printer.rx.operation.shiftOrMonthSheet.RouteStatisticsTpl;
import ru.ppr.cppk.printer.rx.operation.shiftOrMonthSheet.ServicesTpl;
import ru.ppr.cppk.printer.rx.operation.shiftOrMonthSheet.ShiftInfoTpl;
import ru.ppr.cppk.printer.rx.operation.shiftOrMonthSheet.ShiftOrMonthSheetTpl;
import ru.ppr.cppk.printer.rx.operation.shiftOrMonthSheet.ShiftStatesTpl;
import ru.ppr.cppk.printer.rx.operation.shiftOrMonthSheet.TicketDirectionTpl;
import ru.ppr.cppk.printer.rx.operation.shiftOrMonthSheet.TicketTapeConsumptionTpl;
import ru.ppr.cppk.printer.rx.operation.shiftOrMonthSheet.TicketTypeInDirectionTpl;
import ru.ppr.cppk.printer.rx.operation.shiftOrMonthSheet.TicketTypeTpl;
import ru.ppr.cppk.utils.Decimals;
import ru.ppr.ikkm.IPrinter;
import ru.ppr.nsi.entity.FeeType;
import ru.ppr.nsi.entity.TrainCategory;
import ru.ppr.nsi.repository.FineRepository;
import rx.Observable;

/**
 * Пробная сменная ведомость
 * <p>
 * Сменная ведомость
 * <p>
 * Пробная месячная ведомость
 * <p>
 * Месячная ведомость
 *
 * @author Aleksandr Brazhkin
 */
public class ReportShiftOrMonthlySheet extends Report<ReportShiftOrMonthlySheet, PrintShiftOrMonthSheetOperation.Result> {

    private final FineRepository fineRepository;
    /**
     * Тип используемого отчета. По умолчанию "Пробная сменная ведомость"
     */
    private ReportType sheetType = ReportType.TestShiftShit;

    private ShiftOrMonthSheetTpl.Params mainParams;

    private String shiftId;
    private String monthId;
    private boolean buildForLastShift;
    private boolean buildForLastMonth;
    private boolean buildTestAtShiftStart;
    ///////////////////////////////////
    private boolean printFooter;
    ///////////////////////////////////
    private boolean monthClosed;

    /**
     * Конструктор Отчетов типа: Пробная сменная ведомость, Сменная ведомость,
     * Месячная ведомость
     */
    public ReportShiftOrMonthlySheet(FineRepository fineRepository) {
        super();
        this.fineRepository = fineRepository;
    }

    /**
     * Задать тип отчета: "Пробная сменная ведомость" (на начало смены)
     */
    public ReportShiftOrMonthlySheet setSheetTypeTestAtShiftStart() {
        this.sheetType = ReportType.TestShiftShit;
        this.buildTestAtShiftStart = true;
        return this;
    }

    /**
     * Задать тип отчета: "Пробная сменная ведомость" (на середину смены)
     */
    public ReportShiftOrMonthlySheet setSheetTypeTestAtShiftMiddle() {
        this.sheetType = ReportType.TestShiftShit;
        this.buildTestAtShiftStart = false;
        return this;
    }

    /**
     * Задать тип отчета: "Пробная месячная ведомость" (на середину месяца)
     */
    public ReportShiftOrMonthlySheet setSheetTypeTestAtMonthMiddle() {
        this.sheetType = ReportType.TestMonthShit;
        this.buildTestAtShiftStart = false;
        return this;
    }

    /**
     * Задать тип отчета: "Сменная ведомость"
     */
    public ReportShiftOrMonthlySheet setSheetTypeShift() {
        this.sheetType = ReportType.ShiftShit;
        return this;
    }

    /**
     * Задать тип отчета: "Месячная ведомость"
     */
    public ReportShiftOrMonthlySheet setSheetTypeMonth() {
        this.sheetType = ReportType.MonthlySheet;
        return this;
    }

    /**
     * Задать id смены
     */
    public ReportShiftOrMonthlySheet setShiftId(String shiftId) {
        this.shiftId = shiftId;
        return this;
    }

    public ReportShiftOrMonthlySheet setBuildForLastShift() {
        this.buildForLastShift = true;
        return this;
    }

    /**
     * Задать id месяца
     */
    public ReportShiftOrMonthlySheet setMonthId(String monthId) {
        this.monthId = monthId;
        return this;
    }

    public ReportShiftOrMonthlySheet setBuildForLastMonth() {
        this.buildForLastMonth = true;
        return this;
    }

    /**
     * Указать, что нужно допечатать футер
     *
     * @return
     */
    public ReportShiftOrMonthlySheet setPrintFooter() {
        this.printFooter = true;
        return this;
    }

    @Override
    public ReportShiftOrMonthlySheet build() throws Exception {

        mainParams = new ShiftOrMonthSheetTpl.Params();
        mainParams.clicheParams = buildClicheParams();
        mainParams.isTest = (sheetType == ReportType.TestShiftShit || sheetType == ReportType.TestMonthShit);
        mainParams.isMonthly = (sheetType == ReportType.MonthlySheet || sheetType == ReportType.TestMonthShit);
        mainParams.isTestAtShiftStart = buildTestAtShiftStart;

        boolean buildForClosedShiftsOnly = sheetType == ReportType.TestMonthShit;

        ShiftInfoStatisticsBuilder.Statistics shiftInfoStatistics = null;
        if (shiftId != null || buildForLastShift) {
            shiftInfoStatistics = new ShiftInfoStatisticsBuilder()
                    .setShiftId(shiftId)
                    .setBuildForLastShift(buildForLastShift)
                    .build();
        }

        MonthInfoStatisticsBuilder.Statistics monthInfoStatistics = null;
        if (monthId != null || buildForLastMonth) {
            monthInfoStatistics = new MonthInfoStatisticsBuilder()
                    .setMonthId(monthId)
                    .setBuildForLastMonth(buildForLastMonth)
                    .setBuildForClosedShiftsOnly(buildForClosedShiftsOnly)
                    .build();
        }

        ReportCountStatisticsBuilder.Statistics reportCountStatistics = new ReportCountStatisticsBuilder()
                .setShiftId(shiftId)
                .setBuildForLastShift(buildForLastShift)
                .setMonthId(monthId)
                .setBuildForLastMonth(buildForLastMonth)
                .setBuildForClosedShiftsOnly(buildForClosedShiftsOnly)
                .setBuildForOutOfShiftEvents(true)
                .build();

        UpdateStatisticsBuilder.Statistics updateStatistics = new UpdateStatisticsBuilder()
                .setShiftId(shiftId)
                .setBuildForLastShift(buildForLastShift)
                .setMonthId(monthId)
                .setBuildForLastMonth(buildForLastMonth)
                .setUpdateEventType(UpdateEventType.SW)
                .build();

        PdStatisticsBuilder.Statistics pdStatistics = new PdStatisticsBuilder(getNsiVersionManager().getCurrentNsiVersionId())
                .setShiftId(shiftId)
                .setBuildForLastShift(buildForLastShift)
                .setMonthId(monthId)
                .setBuildForLastMonth(buildForLastMonth)
                .setBuildForClosedShiftsOnly(buildForClosedShiftsOnly)
                .build();

        FineSaleStatisticsBuilder.Statistics fineSaleStatistics = new FineSaleStatisticsBuilder(localDaoSession, fineRepository)
                .setShiftId(shiftId)
                .setBuildForLastShift(buildForLastShift)
                .setMonthId(monthId)
                .setBuildForLastMonth(buildForLastMonth)
                .setBuildForClosedShiftsOnly(buildForClosedShiftsOnly)
                .build();

        TicketTapeStatisticsBuilder.Statistics ticketTapeStatistics = new TicketTapeStatisticsBuilder(localDaoSession)
                .setShiftId(shiftId)
                .setBuildForLastShift(buildForLastShift)
                .setMonthId(monthId)
                .setBuildForLastMonth(buildForLastMonth)
                .setBuildForClosedShiftsOnly(buildForClosedShiftsOnly)
                .build();

        /**
         * Доп. данные
         */
        if (sheetType == ReportType.MonthlySheet || sheetType == ReportType.TestMonthShit) {
            monthClosed = monthInfoStatistics.isClosed;
        }

        /**
         * Данные по отчету
         */
        if (sheetType == ReportType.MonthlySheet || sheetType == ReportType.TestMonthShit) {
            // Для пробной месячной ведомости и месячной ведомости
            MonthInfoTpl.Params monthlySheetInfo = new MonthInfoTpl.Params();
            mainParams.monthlySheetInfo = monthlySheetInfo;

            if (sheetType == ReportType.TestMonthShit) {
                // Номер пробной месячной ведомости
                monthlySheetInfo.sheetNum = reportCountStatistics.reportsCount.testMonthSheetCount + 1;
            }

            // Количество пробных ведомостей
            monthlySheetInfo.testMonthSheetsCount = reportCountStatistics.reportsCount.testMonthSheetCount;

            monthlySheetInfo.testShiftSheetsCount = reportCountStatistics.reportsCount.testShiftSheetCount;
            monthlySheetInfo.monthNum = monthInfoStatistics.monthNum;
            monthlySheetInfo.auditTrailsCount = reportCountStatistics.reportsCount.auditTrailCount;
            // Версии ПО
            monthlySheetInfo.SWversions = new ArrayList<>();
            for (int i = 0; i < updateStatistics.updateEvents.size(); i++) {
                UpdateEvent updateEvent = updateStatistics.updateEvents.get(i);
                Date start = updateEvent.getOperationTime();
                Date end = (i == updateStatistics.updateEvents.size() - 1) ? updateStatistics.toDate : updateStatistics.updateEvents.get(i + 1).getOperationTime();
                MonthInfoTpl.SWVersion swVersion = new MonthInfoTpl.SWVersion(updateEvent.getVersion(), start, end);
                monthlySheetInfo.SWversions.add(swVersion);
            }
        } else {
            // Для пробной сменной и сменной ведомостей
            ShiftInfoTpl.Params shiftSheetInfo = new ShiftInfoTpl.Params();
            mainParams.shiftSheetInfo = shiftSheetInfo;
            if (sheetType == ReportType.ShiftShit) {
                // Версия ПО
                UpdateEvent updateEvent = updateStatistics.updateEvents.get(0);
                shiftSheetInfo.SWversion = updateEvent.getVersion();
            }
            // Номер смены
            shiftSheetInfo.shiftNum = shiftInfoStatistics.shiftNum;
            // Сумма в ФР
            shiftSheetInfo.cashFiscalRegister = shiftInfoStatistics.cashInFR == null ? getCashInFR() : shiftInfoStatistics.cashInFR;
            if (sheetType == ReportType.TestShiftShit) {
                // Номер пробной сменной ведомости
                shiftSheetInfo.sheetNum = reportCountStatistics.reportsCount.testShiftSheetCount + 1;
            } else if (sheetType == ReportType.ShiftShit) {
                // Количество пробных ведомостей и контрольных журналов за смену
                shiftSheetInfo.testSheetsCount = reportCountStatistics.reportsCount.testShiftSheetCount;
                shiftSheetInfo.auditTrailsCount = reportCountStatistics.reportsCount.auditTrailCount;
            }
        }

        /**
         * Состояния смены/месяца
         */
        if (sheetType == ReportType.MonthlySheet || sheetType == ReportType.TestMonthShit) {
            // Состояние месяца
            MonthStatesTpl.Params monthStates = new MonthStatesTpl.Params();
            mainParams.monthStates = monthStates;
            if (monthInfoStatistics.firstShiftStatistics != null) {
                monthStates.startShiftNum = monthInfoStatistics.firstShiftStatistics.shiftNum;
                monthStates.startOpenShiftDate = monthInfoStatistics.firstShiftStatistics.fromDate;
                monthStates.startCloseShiftDate = monthInfoStatistics.firstShiftStatistics.toDate;
                monthStates.startFirstDocNumber = monthInfoStatistics.firstShiftStatistics.firstDocument == null ? null : monthInfoStatistics.firstShiftStatistics.firstDocument.number;
                monthStates.startLastDocNumber = monthInfoStatistics.firstShiftStatistics.lastDocument == null ? null : monthInfoStatistics.firstShiftStatistics.lastDocument.number;
            }
            if (monthInfoStatistics.lastShiftStatistics != null) {
                monthStates.endShiftNum = monthInfoStatistics.lastShiftStatistics.shiftNum;
                monthStates.endOpenShiftDate = monthInfoStatistics.lastShiftStatistics.fromDate;
                monthStates.endCloseShiftDate = monthInfoStatistics.lastShiftStatistics.toDate;
                monthStates.endFirstDocNumber = monthInfoStatistics.lastShiftStatistics.firstDocument == null ? null : monthInfoStatistics.lastShiftStatistics.firstDocument.number;
                monthStates.endLastDocNumber = monthInfoStatistics.lastShiftStatistics.lastDocument == null ? null : monthInfoStatistics.lastShiftStatistics.lastDocument.number;
            }
        } else {
            // Состояние смены
            ShiftStatesTpl.Params shiftStates = new ShiftStatesTpl.Params();
            mainParams.shiftStates = shiftStates;
            if (shiftInfoStatistics.firstDocument != null) {
                shiftStates.startDocNumber = shiftInfoStatistics.firstDocument.number;
                shiftStates.startDocDate = shiftInfoStatistics.firstDocument.printTime;
                shiftStates.startCashierNum = Integer.valueOf(shiftInfoStatistics.firstDocument.cashier.getOfficialCode());
            }
            if (shiftInfoStatistics.lastDocument != null) {
                shiftStates.endDocNumber = shiftInfoStatistics.lastDocument.number;
                shiftStates.endDocDate = shiftInfoStatistics.lastDocument.printTime;
                shiftStates.endCashierNum = Integer.valueOf(shiftInfoStatistics.lastDocument.cashier.getOfficialCode());
            }
        }

        /**
         * Продажи
         */
        // -- Выручка --
        mainParams.profit = new ProfitTpl.Params();
        mainParams.profit.monthlyProfit = pdStatistics.monthCountAndProfit.profit.total;
        mainParams.profit.monthlyProfitAnnulled = pdStatistics.monthCountAndProfit.profit.totalRepeal;
        fillProfit(mainParams.profit, pdStatistics.countAndProfit.profit, pdStatistics.ticketTypeTransferStatistics.countAndProfit.profit, fineSaleStatistics);

        // -- Количество документов --
        mainParams.pdCount = new PdCountTpl.Params();
        fillDocumentCount(mainParams.pdCount, pdStatistics.documentCount, fineSaleStatistics.countAndProfit.count);
        // -- Штрафы --
        mainParams.fines = new FinesTpl.Params();
        fillFineStatistics(mainParams.fines, fineSaleStatistics.countAndProfit);
        // -- Багаж --
        mainParams.baggage = new TicketTypeTpl.Params();
        fillTicketTypeStatistics(mainParams.baggage, pdStatistics.ticketTypeBaggageStatistics);
        // -- Услуги (Сборы) --
        mainParams.services = new ServicesTpl.Params();
        fillServiceStatistics(mainParams.services, pdStatistics.countAndProfit, pdStatistics.feeStatistics);
        // -- Разовые полные ПД --
        mainParams.fullSingleTickets = new TicketTypeTpl.Params();
        fillTicketTypeStatistics(mainParams.fullSingleTickets, pdStatistics.ticketTypeFullStatistics);
        // -- Разовые детские ПД --
        mainParams.childSingleTickets = new TicketTypeTpl.Params();
        fillTicketTypeStatistics(mainParams.childSingleTickets, pdStatistics.ticketTypeChildStatistics);
        // -- Трансферы --
        mainParams.transferTickets = new TicketTypeTpl.Params();
        fillTicketTypeStatistics(mainParams.transferTickets, pdStatistics.ticketTypeTransferStatistics);
        /**
         * Проверено документов
         */
        mainParams.pdCheckedCount = new CheckedPdCountTpl.Params();
        fillControlCount(mainParams.pdCheckedCount, pdStatistics.controlCount);

        /**
         * Статистика по маршрутам
         */
        if (!pdStatistics.routesStatistics.isEmpty()) {
            mainParams.routeStatistics = new ArrayList<>();
            for (Map.Entry<Integer, PdStatisticsBuilder.RouteStatistics> routeStatisticsEntry : pdStatistics.routesStatistics.entrySet()) {
                RouteStatisticsTpl.Params routeTplParams = new RouteStatisticsTpl.Params();
                routeTplParams.routeCarriersStatistics = new ArrayList<>();
                routeTplParams.routeNum = routeStatisticsEntry.getKey();
                mainParams.routeStatistics.add(routeTplParams);
                for (Map.Entry<String, PdStatisticsBuilder.RouteCarrierStatistics> routeCarrierStatisticsEntry : routeStatisticsEntry.getValue().routeCarriersStatistics.entrySet()) {
                    RouteCarrierStatisticsTpl.Params routeCarrierTplParams = new RouteCarrierStatisticsTpl.Params();
                    routeCarrierTplParams.routeCarrierTrainCategoriesStatistics = new ArrayList<>();
                    routeCarrierTplParams.carrierName = routeCarrierStatisticsEntry.getValue().carrierName;
                    routeCarrierTplParams.carrierId = routeCarrierStatisticsEntry.getValue().carrierId;
                    routeTplParams.routeCarriersStatistics.add(routeCarrierTplParams);
                    for (Map.Entry<TrainInfo, PdStatisticsBuilder.RouteCarrierTrainCategoryStatistics> routeCarrierTrainCategoriesStatisticsEntry : routeCarrierStatisticsEntry.getValue().routeCarrierTrainCategoriesStatistics.entrySet()) {
                        RouteCarrierTrainCategoryStatisticsTpl.Params routeCarrierTrainCategoryTplParams = new RouteCarrierTrainCategoryStatisticsTpl.Params();
                        routeCarrierTplParams.routeCarrierTrainCategoriesStatistics.add(routeCarrierTrainCategoryTplParams);
                        routeCarrierTrainCategoryTplParams.tripType = routeCarrierTrainCategoriesStatisticsEntry.getKey().getTrainCategory();
                        switch (routeCarrierTrainCategoriesStatisticsEntry.getKey().getTrainCategoryCode()) {
                            case TrainCategory.CATEGORY_CODE_O: {
                                routeCarrierTrainCategoryTplParams.tariffName = "ПАССАЖИРСКИЙ";
                                break;
                            }
                            case TrainCategory.CATEGORY_CODE_7:
                            case TrainCategory.CATEGORY_CODE_C:
                            case TrainCategory.CATEGORY_CODE_M: {
                                routeCarrierTrainCategoryTplParams.tariffName = "СКОРЫЙ";
                                break;
                            }
                        }
                        fillRouteCarrierTrainCategoriesStatistics(routeCarrierTrainCategoryTplParams, routeCarrierTrainCategoriesStatisticsEntry.getValue());
                    }
                }
            }
        }

        if (sheetType != ReportType.TestShiftShit) {
            TicketTapeConsumptionTpl.Params ticketTapeConsumption = new TicketTapeConsumptionTpl.Params();
            mainParams.ticketTapeConsumption = ticketTapeConsumption;
            //расход ленты лолжен выводиться в метрах поэтому миллиметры делим на 1000
            ticketTapeConsumption.consumptionInMeters = Decimals.divide(new BigDecimal(ticketTapeStatistics.finishedTicketTapeConsumptionInMillimeters), Decimals.THOUSAND);
            ticketTapeConsumption.reels = new ArrayList<>();
            for (int j = 0; j < ticketTapeStatistics.ticketTapeEvents.size(); j++) {
                TicketTapeEvent ticketTapeEvent = ticketTapeStatistics.ticketTapeEvents.get(j).ticketTapeEvent;
                TicketTapeConsumptionTpl.Reel reel = new TicketTapeConsumptionTpl.Reel(ticketTapeEvent.getSeries(), Integer.valueOf(ticketTapeEvent.getNumber()));
                ticketTapeConsumption.reels.add(reel);
            }
            ticketTapeConsumption.receiptsCount = ticketTapeStatistics.returnsCount + ticketTapeStatistics.salesCount + ticketTapeStatistics.testTicketsCount;
            ticketTapeConsumption.testShiftSheetsCount = ticketTapeStatistics.reportsCount.testShiftSheetCount;
            ticketTapeConsumption.shiftSheetsCount = ticketTapeStatistics.reportsCount.shiftSheetCount;
            ticketTapeConsumption.discountShiftSheetsCount = ticketTapeStatistics.reportsCount.shiftDiscountSheetCount;
            ticketTapeConsumption.auditTrailsCount = ticketTapeStatistics.reportsCount.auditTrailCount;
            ticketTapeConsumption.discountMonthlySheetsCount = ticketTapeStatistics.reportsCount.monthDiscountSheetCount;
            ticketTapeConsumption.monthlySheetsCount = ticketTapeStatistics.reportsCount.monthSheetCount;
            ticketTapeConsumption.testMonthlySheetsCount = ticketTapeStatistics.reportsCount.testMonthSheetCount;
            ticketTapeConsumption.salesForEttLogCount = ticketTapeStatistics.reportsCount.salesForEttLogCount;
        }

        return this;
    }

    private void fillControlCount(CheckedPdCountTpl.Params params, PdStatisticsBuilder.ControlCount controlCount) {
        params.checkedOnBSKOnceOnlyTicketsCount = controlCount.bscSingleCount;
        params.checkedOnBSKSeasonTicketsCount = controlCount.bscForPeriodCount;
        params.checkedOnceOnlyTicketsCount = controlCount.singleCount;
    }

    private void fillProfit(ProfitTpl.Params params,
                            PdStatisticsBuilder.Profit profit,
                            PdStatisticsBuilder.Profit transferProfit,
                            FineSaleStatisticsBuilder.Statistics fineSaleStatistics) {
        params.totalProfitSum = profit.total;
        params.totalProfitSumAnnulled = profit.totalRepeal;
        params.tariffProfitSum = profit.tariff;
        params.tariffProfitSumAnnulled = profit.tariffRepeal;
        params.feeProfitSum = profit.fee;
        params.feeProfitSumAnnulled = profit.feeRepeal;
        params.feeVATSum = profit.feeVat;
        params.feeVATSumAnnulled = profit.feeVatRepeal;
        params.byCashTotalProfitSum = profit.totalCashPaymentSum;
        params.byCashTotalProfitSumAnnulled = profit.totalCashPaymentSumRepeal;
        params.byBankCardTotalProfitSum = profit.totalCardPaymentSum;
        params.byBankCardTotalProfitSumAnnulled = profit.totalCardPaymentSumRepeal;
        params.byCashTariffProfitSum = profit.tariffCashPaymentSum;
        params.byCashTariffProfitSumAnnulled = profit.tariffCashPaymentSumRepeal;
        params.byBankCardTariffProfitSum = profit.tariffCardPaymentSum;
        params.byBankCardTariffProfitSumAnnulled = profit.tariffCardPaymentSumRepeal;
        params.byCashFeeProfitSum = profit.feeCashPaymentSum;
        params.byCashFeeProfitSumAnnulled = profit.feeCashPaymentSumRepeal;
        params.byBankCardFeeProfitSum = profit.feeCardPaymentSum;
        params.byBankCardFeeProfitSumAnnulled = profit.feeCardPaymentSumRepeal;
        params.monthlyFineProfit = fineSaleStatistics.monthCountAndProfit.profit.total;
        params.fineProfitSum = fineSaleStatistics.countAndProfit.profit.total;
        params.byCashFineProfitCount = fineSaleStatistics.countAndProfit.count.cashPaymentCount;
        params.byCashFineProfitSum = fineSaleStatistics.countAndProfit.profit.totalCashPaymentSum;
        params.byBankCardFineProfitCount = fineSaleStatistics.countAndProfit.count.cardPaymentCount;
        params.byBankCardFineProfitSum = fineSaleStatistics.countAndProfit.profit.totalCardPaymentSum;
        params.transferTotalProfitSum = transferProfit.total;
        params.transferTotalProfitSumAnnulled = transferProfit.totalRepeal;
        params.transferTariffProfitSum = transferProfit.tariff;
        params.transferTariffProfitSumAnnulled = transferProfit.tariffRepeal;
        params.transferTariffVATSum = transferProfit.tariffVat;
        params.transferTariffVATSumAnnulled = transferProfit.tariffVatRepeal;
        params.transferFeeProfitSum = transferProfit.fee;
        params.transferFeeProfitSumAnnulled = transferProfit.feeRepeal;
        params.transferFeeVATSum = transferProfit.feeVat;
        params.transferFeeVATSumAnnulled = transferProfit.feeVatRepeal;
        params.byCashTransferTotalProfitSum = transferProfit.totalCashPaymentSum;
        params.byCashTransferTotalProfitSumAnnulled = transferProfit.totalCashPaymentSumRepeal;
        params.byBankCardTransferTotalProfitSum = transferProfit.totalCardPaymentSum;
        params.byBankCardTransferTotalProfitSumAnnulled = transferProfit.totalCardPaymentSumRepeal;
        params.byCashTransferTariffProfitSum = transferProfit.tariffCashPaymentSum;
        params.byCashTransferTariffProfitSumAnnulled = transferProfit.tariffCashPaymentSumRepeal;
        params.byCashTransferFeeProfitSum = transferProfit.feeCashPaymentSum;
        params.byCashTransferFeeProfitSumAnnulled = transferProfit.feeCashPaymentSumRepeal;
        params.byBankCardTransferTariffProfitSum = transferProfit.tariffCardPaymentSum;
        params.byBankCardTransferTariffProfitSumAnnulled = transferProfit.tariffCardPaymentSumRepeal;
        params.byBankCardTransferFeeProfitSum = transferProfit.feeCardPaymentSum;
        params.byBankCardTransferFeeProfitSumAnnulled = transferProfit.feeCardPaymentSumRepeal;
    }

    private void fillDocumentCount(PdCountTpl.Params params, PdStatisticsBuilder.DocumentCount pdDocumentCount, FineSaleStatisticsBuilder.Count fineCount) {
        params.testPDCount = pdDocumentCount.testCount;
        params.baggagePDCount = pdDocumentCount.trainBaggageCount;
        params.servicePDcount = pdDocumentCount.serviceCount;
        params.canceledPDcount = pdDocumentCount.trainRepealCount;
        params.forExtraChargePDCount = pdDocumentCount.trainSingleWithAddPaymentCount;
        params.onlyOncePDCount = pdDocumentCount.trainSingleCount;
        params.fineCount = fineCount.totalCount;
        params.transferSingleCount = pdDocumentCount.transferSingleCount;
        params.transferSeasonCount = pdDocumentCount.transferSeasonCount;
        params.totalDocumentCount = pdDocumentCount.totalCount + fineCount.totalCount;
    }

    private void fillFineStatistics(FinesTpl.Params fines, FineSaleStatisticsBuilder.CountAndProfit countAndProfit) {
        fines.count = countAndProfit.count.totalCount;
        fines.sum = countAndProfit.profit.total;
        fines.byCashCount = countAndProfit.count.cashPaymentCount;
        fines.byCashProfitSum = countAndProfit.profit.totalCashPaymentSum;
        fines.byCardCount = countAndProfit.count.cardPaymentCount;
        fines.byCardProfitSum = countAndProfit.profit.totalCardPaymentSum;
    }

    /**
     * Собирает статистику по сборам (услугам)
     *
     * @param params
     * @param pdCountAndProfit
     */
    private void fillServiceStatistics(ServicesTpl.Params params, PdStatisticsBuilder.CountAndProfit pdCountAndProfit, HashMap<FeeType, PdStatisticsBuilder.FeeStatisticsDetails> feeStatistics) {
        params.totalProfitSum = pdCountAndProfit.profit.fee;
        params.totalCount = pdCountAndProfit.count.totalFeeCount;
        params.totalProfitSumAnnulled = pdCountAndProfit.profit.feeRepeal;
        params.totalAnnulledCount = pdCountAndProfit.count.totalRepealFeeCount;
        params.vatSumExceptAnnulled = pdCountAndProfit.profit.feeVat.subtract(pdCountAndProfit.profit.feeVatRepeal);
        params.byBankCardProfitSumExceptAnnulled = pdCountAndProfit.profit.feeCardPaymentSum.subtract(pdCountAndProfit.profit.feeCardPaymentSumRepeal);

        for (Map.Entry<FeeType, PdStatisticsBuilder.FeeStatisticsDetails> entry : feeStatistics.entrySet()) {
            PdStatisticsBuilder.FeeStatisticsDetails feeStatisticsDetails = entry.getValue();
            ServicesTpl.DetailParams detailParams = new ServicesTpl.DetailParams(
                    entry.getKey().getDescription(),
                    feeStatisticsDetails.total.subtract(feeStatisticsDetails.totalRepeal),
                    feeStatisticsDetails.count - feeStatisticsDetails.repealCount);
            params.detailParams.add(detailParams);
        }
    }

    private void fillTicketTypeStatistics(TicketTypeTpl.Params params, PdStatisticsBuilder.TicketTypeStatistics ticketTypeStatistics) {
        params.count = ticketTypeStatistics.countAndProfit.count.totalCount;
        params.countAnnulled = ticketTypeStatistics.countAndProfit.count.totalRepealCount;
        params.sum = ticketTypeStatistics.countAndProfit.profit.total;
        params.sumAnnulled = ticketTypeStatistics.countAndProfit.profit.totalRepeal;
        params.feeSum = ticketTypeStatistics.countAndProfit.profit.fee;
        params.feeSumAnnulled = ticketTypeStatistics.countAndProfit.profit.feeRepeal;
        params.VATSum = ticketTypeStatistics.countAndProfit.profit.totalVat;
        params.VATSumAnnulled = ticketTypeStatistics.countAndProfit.profit.totalVatRepeal;
        params.byBankCardTotalSum = ticketTypeStatistics.countAndProfit.profit.totalCardPaymentSum;
        params.byBankCardTotalSumAnnulled = ticketTypeStatistics.countAndProfit.profit.totalCardPaymentSumRepeal;
        params.byBankCardTariffSum = ticketTypeStatistics.countAndProfit.profit.tariffCardPaymentSum;
        params.byBankCardTariffSumAnnulled = ticketTypeStatistics.countAndProfit.profit.tariffCardPaymentSumRepeal;
        params.byBankCardFeeSum = ticketTypeStatistics.countAndProfit.profit.feeCardPaymentSum;
        params.byBankCardFeeSumAnnulled = ticketTypeStatistics.countAndProfit.profit.feeCardPaymentSumRepeal;
        params.byCashTotalSum = ticketTypeStatistics.countAndProfit.profit.totalCashPaymentSum;
        params.byCashTotalSumAnnulled = ticketTypeStatistics.countAndProfit.profit.totalCashPaymentSumRepeal;
        params.byCashTariffSum = ticketTypeStatistics.countAndProfit.profit.tariffCashPaymentSum;
        params.byCashTariffSumAnnulled = ticketTypeStatistics.countAndProfit.profit.tariffCashPaymentSumRepeal;
        params.byCashFeeSum = ticketTypeStatistics.countAndProfit.profit.feeCashPaymentSum;
        params.byCashFeeSumAnnulled = ticketTypeStatistics.countAndProfit.profit.feeCashPaymentSumRepeal;
        params.extraChargeSum = ticketTypeStatistics.withAddPaymentCountAndProfit.profit.total;
        params.extraChargeSumAnnulled = ticketTypeStatistics.withAddPaymentCountAndProfit.profit.totalRepeal;
        params.extraChargeCount = ticketTypeStatistics.withAddPaymentCountAndProfit.count.totalCount;
        params.extraChargeCountAnnulled = ticketTypeStatistics.withAddPaymentCountAndProfit.count.totalRepealCount;

        ////////////////////////////////////////////////////////////////////
        params.there = new TicketDirectionTpl.Params();
        fillDirectionStatistics(params.there, ticketTypeStatistics.directionThereStatistics);
        params.thereBack = new TicketDirectionTpl.Params();
        fillDirectionStatistics(params.thereBack, ticketTypeStatistics.directionThereBackStatistics);
    }

    private void fillDirectionStatistics(TicketDirectionTpl.Params params, PdStatisticsBuilder.DirectionStatistics directionStatistics) {
        params.count = directionStatistics.presenceOfExemptionStatistics.countAndProfit.count.totalCount;
        params.countAnnulled = directionStatistics.presenceOfExemptionStatistics.countAndProfit.count.totalRepealCount;
        params.sum = directionStatistics.presenceOfExemptionStatistics.countAndProfit.profit.total;
        params.sumAnnulled = directionStatistics.presenceOfExemptionStatistics.countAndProfit.profit.totalRepeal;
        params.fullCount = directionStatistics.presenceOfExemptionStatistics.fullPricePresenceOfExemptionDetailStatistics.countAndProfit.count.totalCount;
        params.fullCountAnnulled = directionStatistics.presenceOfExemptionStatistics.fullPricePresenceOfExemptionDetailStatistics.countAndProfit.count.totalRepealCount;
        params.fullSum = directionStatistics.presenceOfExemptionStatistics.fullPricePresenceOfExemptionDetailStatistics.countAndProfit.profit.total;
        params.fullSumAnnulled = directionStatistics.presenceOfExemptionStatistics.fullPricePresenceOfExemptionDetailStatistics.countAndProfit.profit.totalRepeal;
        params.discountCount = directionStatistics.presenceOfExemptionStatistics.withExemptionPresenceOfExemptionDetailStatistics.countAndProfit.count.totalCount;
        params.discountCountAnnulled = directionStatistics.presenceOfExemptionStatistics.withExemptionPresenceOfExemptionDetailStatistics.countAndProfit.count.totalRepealCount;
        params.discountSum = directionStatistics.presenceOfExemptionStatistics.withExemptionPresenceOfExemptionDetailStatistics.countAndProfit.profit.total;
        params.discountSumAnnulled = directionStatistics.presenceOfExemptionStatistics.withExemptionPresenceOfExemptionDetailStatistics.countAndProfit.profit.totalRepeal;
        params.discountLossSum = directionStatistics.presenceOfExemptionStatistics.withExemptionPresenceOfExemptionDetailStatistics.countAndProfit.profit.lossSum;
        params.discountLossSumAnnulled = directionStatistics.presenceOfExemptionStatistics.withExemptionPresenceOfExemptionDetailStatistics.countAndProfit.profit.lossSumRepeal;
        params.discountTariff = directionStatistics.presenceOfExemptionStatistics.withExemptionPresenceOfExemptionDetailStatistics.countAndProfit.profit.tariff;
        params.discountTariffRepeal = directionStatistics.presenceOfExemptionStatistics.withExemptionPresenceOfExemptionDetailStatistics.countAndProfit.profit.tariffRepeal;
        params.discountFee = directionStatistics.presenceOfExemptionStatistics.withExemptionPresenceOfExemptionDetailStatistics.countAndProfit.profit.fee;
        params.discountFeeRepeal = directionStatistics.presenceOfExemptionStatistics.withExemptionPresenceOfExemptionDetailStatistics.countAndProfit.profit.feeRepeal;
        params.discountByBSKCount = directionStatistics.presenceOfExemptionStatistics.withExemptionPresenceOfExemptionDetailStatistics.exemptionFromSmartCardStatistics.countAndProfit.count.totalCount;
        params.discountByBSKCountAnnulled = directionStatistics.presenceOfExemptionStatistics.withExemptionPresenceOfExemptionDetailStatistics.exemptionFromSmartCardStatistics.countAndProfit.count.totalRepealCount;
        params.discountByETTCount = directionStatistics.presenceOfExemptionStatistics.withExemptionPresenceOfExemptionDetailStatistics.exemptionFromSmartCardStatistics.ettCountAndProfit.count.totalCount;
        params.discountByETTCountAnnulled = directionStatistics.presenceOfExemptionStatistics.withExemptionPresenceOfExemptionDetailStatistics.exemptionFromSmartCardStatistics.ettCountAndProfit.count.totalRepealCount;
        params.noMoneyCount = directionStatistics.presenceOfExemptionStatistics.noMoneyPresenceOfExemptionDetailStatistics.countAndProfit.count.totalCount;
        params.noMoneyCountAnnulled = directionStatistics.presenceOfExemptionStatistics.noMoneyPresenceOfExemptionDetailStatistics.countAndProfit.count.totalRepealCount;
        params.noMoneyFee = directionStatistics.presenceOfExemptionStatistics.noMoneyPresenceOfExemptionDetailStatistics.countAndProfit.profit.fee;
        params.noMoneyFeeRepeal = directionStatistics.presenceOfExemptionStatistics.noMoneyPresenceOfExemptionDetailStatistics.countAndProfit.profit.feeRepeal;
        params.noMoneyLossSum = directionStatistics.presenceOfExemptionStatistics.noMoneyPresenceOfExemptionDetailStatistics.countAndProfit.profit.lossSum;
        params.noMoneyLossSumAnnulled = directionStatistics.presenceOfExemptionStatistics.noMoneyPresenceOfExemptionDetailStatistics.countAndProfit.profit.lossSumRepeal;
        params.noMoneyByBSKCount = directionStatistics.presenceOfExemptionStatistics.noMoneyPresenceOfExemptionDetailStatistics.exemptionFromSmartCardStatistics.countAndProfit.count.totalCount;
        params.noMoneyByBSKCountAnnulled = directionStatistics.presenceOfExemptionStatistics.noMoneyPresenceOfExemptionDetailStatistics.exemptionFromSmartCardStatistics.countAndProfit.count.totalRepealCount;
        params.noMoneyByETTCount = directionStatistics.presenceOfExemptionStatistics.noMoneyPresenceOfExemptionDetailStatistics.exemptionFromSmartCardStatistics.ettCountAndProfit.count.totalCount;
        params.noMoneyByETTCountAnnulled = directionStatistics.presenceOfExemptionStatistics.noMoneyPresenceOfExemptionDetailStatistics.exemptionFromSmartCardStatistics.ettCountAndProfit.count.totalRepealCount;

        params.ticketTypeParamsList = new ArrayList<>();
        Collections.sort(directionStatistics.ticketTypeInDirectionStatisticsList, (t1, t2) -> t1.ticketType.getCode() - t2.ticketType.getCode());
        for (PdStatisticsBuilder.TicketTypeInDirectionStatistics ticketTypeInDirectionStatistics : directionStatistics.ticketTypeInDirectionStatisticsList) {
            TicketTypeInDirectionTpl.Params ticketTypeParams = new TicketTypeInDirectionTpl.Params();
            fillTicketTypeInDirectionStatistics(ticketTypeParams, ticketTypeInDirectionStatistics);
            params.ticketTypeParamsList.add(ticketTypeParams);
        }
    }

    private void fillTicketTypeInDirectionStatistics(TicketTypeInDirectionTpl.Params params, PdStatisticsBuilder.TicketTypeInDirectionStatistics ticketTypeInDirectionStatistics) {
        params.ticketTypeShortName = ticketTypeInDirectionStatistics.ticketType.getShortName();
        params.count = ticketTypeInDirectionStatistics.countAndProfit.count.totalCount;
        params.countAnnulled = ticketTypeInDirectionStatistics.countAndProfit.count.totalRepealCount;
        params.sum = ticketTypeInDirectionStatistics.countAndProfit.profit.total;
        params.sumAnnulled = ticketTypeInDirectionStatistics.countAndProfit.profit.totalRepeal;
        params.tariff = ticketTypeInDirectionStatistics.countAndProfit.profit.tariff;
        params.tariffRepeal = ticketTypeInDirectionStatistics.countAndProfit.profit.tariffRepeal;
        params.fee = ticketTypeInDirectionStatistics.countAndProfit.profit.fee;
        params.feeRepeal = ticketTypeInDirectionStatistics.countAndProfit.profit.feeRepeal;
    }

    private void fillRouteCarrierTrainCategoriesStatistics(RouteCarrierTrainCategoryStatisticsTpl.Params params, PdStatisticsBuilder.RouteCarrierTrainCategoryStatistics routeCarrierTrainCategoryStatistics) {
        params.count = routeCarrierTrainCategoryStatistics.presenceOfExemptionStatistics.countAndProfit.count.totalCount;
        params.countAnnulled = routeCarrierTrainCategoryStatistics.presenceOfExemptionStatistics.countAndProfit.count.totalRepealCount;
        params.sum = routeCarrierTrainCategoryStatistics.presenceOfExemptionStatistics.countAndProfit.profit.total;
        params.feeSum = routeCarrierTrainCategoryStatistics.presenceOfExemptionStatistics.countAndProfit.profit.fee;
        params.sumAnnulled = routeCarrierTrainCategoryStatistics.presenceOfExemptionStatistics.countAndProfit.profit.totalRepeal;
        params.fullCount = routeCarrierTrainCategoryStatistics.presenceOfExemptionStatistics.fullPricePresenceOfExemptionDetailStatistics.countAndProfit.count.totalCount;
        params.fullCountAnnulled = routeCarrierTrainCategoryStatistics.presenceOfExemptionStatistics.fullPricePresenceOfExemptionDetailStatistics.countAndProfit.count.totalRepealCount;
        params.fullSum = routeCarrierTrainCategoryStatistics.presenceOfExemptionStatistics.fullPricePresenceOfExemptionDetailStatistics.countAndProfit.profit.total;
        params.fullSumAnnulled = routeCarrierTrainCategoryStatistics.presenceOfExemptionStatistics.fullPricePresenceOfExemptionDetailStatistics.countAndProfit.profit.totalRepeal;
        params.discountCount = routeCarrierTrainCategoryStatistics.presenceOfExemptionStatistics.withExemptionPresenceOfExemptionDetailStatistics.countAndProfit.count.totalCount;
        params.discountCountAnnulled = routeCarrierTrainCategoryStatistics.presenceOfExemptionStatistics.withExemptionPresenceOfExemptionDetailStatistics.countAndProfit.count.totalRepealCount;
        params.discountSum = routeCarrierTrainCategoryStatistics.presenceOfExemptionStatistics.withExemptionPresenceOfExemptionDetailStatistics.countAndProfit.profit.total;
        params.discountSumAnnulled = routeCarrierTrainCategoryStatistics.presenceOfExemptionStatistics.withExemptionPresenceOfExemptionDetailStatistics.countAndProfit.profit.totalRepeal;
        params.noMoneyCount = routeCarrierTrainCategoryStatistics.presenceOfExemptionStatistics.noMoneyPresenceOfExemptionDetailStatistics.countAndProfit.count.totalCount;
        params.noMoneyCountAnnulled = routeCarrierTrainCategoryStatistics.presenceOfExemptionStatistics.noMoneyPresenceOfExemptionDetailStatistics.countAndProfit.count.totalRepealCount;
        params.noMoneyLossSum = routeCarrierTrainCategoryStatistics.presenceOfExemptionStatistics.noMoneyPresenceOfExemptionDetailStatistics.countAndProfit.profit.lossSum;
        params.noMoneyLossSumAnnulled = routeCarrierTrainCategoryStatistics.presenceOfExemptionStatistics.noMoneyPresenceOfExemptionDetailStatistics.countAndProfit.profit.lossSumRepeal;
        params.byBankCardCount = routeCarrierTrainCategoryStatistics.presenceOfExemptionStatistics.countAndProfit.count.cardPaymentCount;
        params.byBankCardCountAnnulled = routeCarrierTrainCategoryStatistics.presenceOfExemptionStatistics.countAndProfit.count.cardPaymentCountRepeal;
        params.byBankCardSum = routeCarrierTrainCategoryStatistics.presenceOfExemptionStatistics.countAndProfit.profit.totalCardPaymentSum;
        params.byBankCardSumAnnulled = routeCarrierTrainCategoryStatistics.presenceOfExemptionStatistics.countAndProfit.profit.totalCardPaymentSumRepeal;
    }

    @Override
    protected Observable<PrintShiftOrMonthSheetOperation.Result> printImpl(IPrinter printer) {
        return Di.INSTANCE.printerManager().getOperationFactory()
                .getPrintShiftOrMonthSheetOperation(mainParams)
                .call()
                .flatMap(clearingSheetResult -> {
                    if ((sheetType == ReportType.MonthlySheet) && printFooter) {
                        // Если нужно допечатать футер, допечатаем
                        PrintMonthSheetFooterOperation.Params params = new PrintMonthSheetFooterOperation.Params();
                        params.isMonthClosed = monthClosed;
                        return Di.INSTANCE.printerManager().getOperationFactory()
                                .getPrintMonthSheetFooterOperation(params)
                                .call()
                                .flatMap(printFooterResult -> Observable.just(clearingSheetResult));
                    } else {
                        return Observable.just(clearingSheetResult);
                    }
                });
    }

    @Override
    protected Observable<Void> addPrintReportEventObservable(PrintShiftOrMonthSheetOperation.Result printResult) {
        return addPrintReportEventObservable(sheetType, printResult.getOperationTime());
    }
}
