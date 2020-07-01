package ru.ppr.cppk.printer.rx.operation.shiftOrMonthSheet;

import java.util.List;

import ru.ppr.cppk.printer.TextFormatter;
import ru.ppr.cppk.printer.tpl.PrinterTpl;
import ru.ppr.ikkm.IPrinter;

/**
 * Шаблон печати.
 * Статистика по маршрутам (группировка по перевозчику) (п.1.1.15)
 *
 * @author Aleksandr Brazhkin
 */
public class RouteCarrierStatisticsTpl extends PrinterTpl {

    private final Params params;
    private final TextFormatter textFormatter;

    RouteCarrierStatisticsTpl(Params params, TextFormatter textFormatter) {
        this.params = params;
        this.textFormatter = textFormatter;
    }

    @Override
    public void printToDriver(IPrinter printer) throws Exception {

        printer.printTextInNormalMode(params.carrierName);
        if (params.routeCarrierTrainCategoriesStatistics != null && !params.routeCarrierTrainCategoriesStatistics.isEmpty()) {
            for (RouteCarrierTrainCategoryStatisticsTpl.Params routeCarrierTrainCategoryParams : params.routeCarrierTrainCategoriesStatistics) {
                routeCarrierTrainCategoryParams.carrierId = params.carrierId;
                new RouteCarrierTrainCategoryStatisticsTpl(routeCarrierTrainCategoryParams, textFormatter).printToDriver(printer);
            }
        }

    }

    public static class Params {

        /**
         * Номер маршрута
         */
        protected Integer routeNum = 0;
        /**
         * идентификатор перевозчика
         */
        public String carrierId;
        /**
         * наименование организации-перевозчика
         */
        public String carrierName;
        /**
         * Данные по категориям поезда
         */
        public List<RouteCarrierTrainCategoryStatisticsTpl.Params> routeCarrierTrainCategoriesStatistics;

    }

}
