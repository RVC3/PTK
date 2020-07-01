package ru.ppr.cppk.printer.rx.operation.shiftOrMonthSheet;

import java.util.List;

import ru.ppr.cppk.printer.TextFormatter;
import ru.ppr.cppk.printer.tpl.PrinterTpl;
import ru.ppr.ikkm.IPrinter;

/**
 * Шаблон печати.
 * Статистика по маршрутам (группировка по номре маршрута) (п.1.1.15)
 *
 * @author Aleksandr Brazhkin
 */
public class RouteStatisticsTpl extends PrinterTpl {

    private final Params params;
    private final TextFormatter textFormatter;

    RouteStatisticsTpl(Params params, TextFormatter textFormatter) {
        this.params = params;
        this.textFormatter = textFormatter;
    }

    @Override
    public void printToDriver(IPrinter printer) throws Exception {

        printer.printTextInNormalMode("--МАРШРУТ " + params.routeNum);

        if (params.routeCarriersStatistics != null && !params.routeCarriersStatistics.isEmpty()) {
            for (RouteCarrierStatisticsTpl.Params routeCarrierParams : params.routeCarriersStatistics) {
                routeCarrierParams.routeNum = params.routeNum;
                new RouteCarrierStatisticsTpl(routeCarrierParams, textFormatter).printToDriver(printer);
            }
        }
    }

    public static class Params {

        /**
         * Номер маршрута
         */
        public Integer routeNum = 0;
        /**
         * Данные по перевозчикам
         */
        public List<RouteCarrierStatisticsTpl.Params> routeCarriersStatistics;

    }

}
