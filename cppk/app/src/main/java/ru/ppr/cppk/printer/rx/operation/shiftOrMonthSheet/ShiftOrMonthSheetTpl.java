package ru.ppr.cppk.printer.rx.operation.shiftOrMonthSheet;

import java.util.ArrayList;
import java.util.Date;

import ru.ppr.cppk.printer.TextFormatter;
import ru.ppr.cppk.printer.tpl.PrinterTpl;
import ru.ppr.cppk.printer.tpl.ReportClicheTpl;
import ru.ppr.ikkm.IPrinter;

/**
 * Шаблон печати.
 * 1.1 Реквизиты пробной сменной ведомости
 * 1.2 Реквизиты сменной ведомости
 *
 * @author Aleksandr Brazhkin
 */
public class ShiftOrMonthSheetTpl extends PrinterTpl {

    private final Params params;
    private final TextFormatter textFormatter;

    ShiftOrMonthSheetTpl(Params params, TextFormatter textFormatter) {
        this.params = params;
        this.textFormatter = textFormatter;
    }

    @Override
    public void printToDriver(IPrinter printer) throws Exception {

        new ReportClicheTpl(params.clicheParams, textFormatter, true).printToDriver(printer);

        if (params.isMonthly) {
            params.monthlySheetInfo.isTest = params.isTest;
            params.monthlySheetInfo.date = params.date;
            new MonthInfoTpl(params.monthlySheetInfo, textFormatter).printToDriver(printer);
            new MonthStatesTpl(params.monthStates, textFormatter).printToDriver(printer);
        } else {
            params.shiftSheetInfo.isTest = params.isTest;
            params.shiftSheetInfo.date = params.date;
            new ShiftInfoTpl(params.shiftSheetInfo, textFormatter).printToDriver(printer);
            params.shiftStates.isTest = params.isTest;
            params.shiftStates.isTestAtShiftStart = params.isTestAtShiftStart;
            new ShiftStatesTpl(params.shiftStates, textFormatter).printToDriver(printer);
        }
        params.profit.isTestAtShiftStart = params.isTestAtShiftStart;
        params.profit.isMonthly = params.isMonthly;
        new ProfitTpl(params.profit, textFormatter).printToDriver(printer);
        params.pdCount.isMonthly = params.isMonthly;
        params.pdCount.isTestAtShiftStart = params.isTestAtShiftStart;
        new PdCountTpl(params.pdCount, textFormatter).printToDriver(printer);
        printer.waitPendingOperations(); // В будущем костыль убрать когда зебра починит https://aj.srvdev.ru/browse/CPPKPP-27416 https://terlis.intraservice.ru/Task/View/32111
        if (params.fines.count > 0) {
            new FinesTpl(params.fines, textFormatter).printToDriver(printer);
        }
        printer.printTextInNormalMode(textFormatter.alignCenterText("БАГАЖ"));
        params.baggage.isBaggage = true;
        new TicketTypeTpl(params.baggage, textFormatter).printToDriver(printer);
        if (params.services.totalCount > 0) {
            new ServicesTpl(params.services, textFormatter).printToDriver(printer);
        }
        printer.waitPendingOperations(); // В будущем костыль убрать когда зебра починит https://aj.srvdev.ru/browse/CPPKPP-27416 https://terlis.intraservice.ru/Task/View/32111
        printer.printTextInNormalMode(textFormatter.alignCenterText("ТРАНСФЕР"));
        params.transferTickets.isTransferTicket = true;
        new TicketTypeTpl(params.transferTickets, textFormatter).printToDriver(printer);
        new CheckedPdCountTpl(params.pdCheckedCount, textFormatter).printToDriver(printer);
        printer.waitPendingOperations(); // В будущем костыль убрать когда зебра починит https://aj.srvdev.ru/browse/CPPKPP-27416 https://terlis.intraservice.ru/Task/View/32111
        printer.printTextInNormalMode(textFormatter.alignCenterText("РАЗОВЫЕ ПОЛНЫЕ"));
        new TicketTypeTpl(params.fullSingleTickets, textFormatter).printToDriver(printer);
        printer.waitPendingOperations(); // В будущем костыль убрать когда зебра починит https://aj.srvdev.ru/browse/CPPKPP-27416 https://terlis.intraservice.ru/Task/View/32111
        printer.printTextInNormalMode(textFormatter.alignCenterText("РАЗОВЫЕ ДЕТСКИЕ"));
        params.childSingleTickets.isChildTicket = true;
        new TicketTypeTpl(params.childSingleTickets, textFormatter).printToDriver(printer);
        //выводим блоки "Статистика по маршрутам" и "Расход билетной ленты" для всех нетестовых ведомостей и для всех месячных
        if (!params.isTest || params.isMonthly) {
            if (params.routeStatistics != null && params.routeStatistics.size() > 0) {
                printer.waitPendingOperations(); // В будущем костыль убрать когда зебра починит https://aj.srvdev.ru/browse/CPPKPP-27416 https://terlis.intraservice.ru/Task/View/32111
                printer.printTextInNormalMode(textFormatter.alignCenterText("СТАТИСТИКА ПО МАРШРУТАМ"));
                for (RouteStatisticsTpl.Params routeStatistics : params.routeStatistics) {
                    new RouteStatisticsTpl(routeStatistics, textFormatter).printToDriver(printer);
                }
                printer.printTextInNormalMode(textFormatter.bigDelimiter());
            }
            new TicketTapeConsumptionTpl(params.ticketTapeConsumption, textFormatter).printToDriver(printer);
        }
        //выведем для пробной месячной ведомости и для всех немесячных
        if (!params.isMonthly || (params.isMonthly && params.isTest)) {
            printer.printTextInNormalMode(textFormatter.alignCenterText("ПЕЧАТЬ ЗАКОНЧЕНА"));
            printer.printTextInNormalMode(textFormatter.bigDelimiter());
            printer.printTextInNormalMode(" ");
            printer.printTextInNormalMode(" ");
            printer.printTextInNormalMode(" ");
        }
        printer.waitPendingOperations();
        printer.closePage(0);

    }

    public static class Params {
        /**
         * Время с принтера
         */
        public Date date;
        /**
         * Параметры для клише
         */
        public ReportClicheTpl.Params clicheParams;
        /**
         * Является пробной
         */
        public Boolean isTest = false;
        /**
         * Является пробной на начало смены
         */
        public Boolean isTestAtShiftStart = false;
        /**
         * Является месячной
         */
        public Boolean isMonthly = false;
        /**
         * Данные по сменному отчету
         */
        public ShiftInfoTpl.Params shiftSheetInfo;
        /**
         * Состояния смены
         */
        public ShiftStatesTpl.Params shiftStates;
        /**
         * Данные по месячному отчету
         */
        public MonthInfoTpl.Params monthlySheetInfo;
        /**
         * Состояния месяцев
         */
        public MonthStatesTpl.Params monthStates;
        /**
         * Выручка
         */
        public ProfitTpl.Params profit;
        /**
         * Количество документов за смену
         */
        public PdCountTpl.Params pdCount;
        /**
         * Багаж
         */
        public TicketTypeTpl.Params baggage;
        /**
         * Услуги (сборы)
         */
        public ServicesTpl.Params services;
        /**
         * Штрафы
         */
        public FinesTpl.Params fines;
        /**
         * Проверено документов
         */
        public CheckedPdCountTpl.Params pdCheckedCount;
        /**
         * Суммарные данные по разовым полным ПД
         */
        public TicketTypeTpl.Params fullSingleTickets;
        /**
         * Суммарные данные по разовым детским ПД
         */
        public TicketTypeTpl.Params childSingleTickets;
        /**
         * Суммарные данные по трансферам
         */
        public TicketTypeTpl.Params transferTickets;
        /**
         * Расход билетной ленты
         */
        //определим переменную чтобы не крашилось при выгрузке событий по сменам...
        public TicketTapeConsumptionTpl.Params ticketTapeConsumption;
        /**
         * Статистика по маршрутам
         */
        public ArrayList<RouteStatisticsTpl.Params> routeStatistics;

    }

}
