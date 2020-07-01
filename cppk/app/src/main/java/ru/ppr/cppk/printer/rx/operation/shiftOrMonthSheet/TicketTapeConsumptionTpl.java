package ru.ppr.cppk.printer.rx.operation.shiftOrMonthSheet;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;

import ru.ppr.cppk.printer.TextFormatter;
import ru.ppr.cppk.printer.tpl.PrinterTpl;
import ru.ppr.ikkm.IPrinter;

/**
 * Шаблон печати.
 * Расход билетной ленты (п.1.8.3)
 *
 * @author Aleksandr Brazhkin
 */
public class TicketTapeConsumptionTpl extends PrinterTpl {

    private final Params params;
    private final TextFormatter textFormatter;

    TicketTapeConsumptionTpl(Params params, TextFormatter textFormatter) {
        this.params = params;
        this.textFormatter = textFormatter;
    }

    @Override
    public void printToDriver(IPrinter printer) throws Exception {

        printer.printTextInNormalMode(textFormatter.alignCenterText("РАСХОД БИЛЕТНОЙ ЛЕНТЫ"));

        // Вывод списка установленных бобин
        Iterator<Reel> reels = params.reels.iterator();
        String previousSeries = new String();
        String res = "";
        int previousNumber = -1;
        int rangeFrom = -1;
        while (reels.hasNext()) {
            Reel reel = reels.next();
            if (reel.getSeries().equals(previousSeries)) {
                if (previousNumber + 1 == reel.getNumber()) {
                    // Одна серия, номера подряд - вывод как диапазона
                    previousNumber++;
                } else {
                    if (rangeFrom == previousNumber) {
                        res += (textFormatter.asStr06d(rangeFrom));
                    } else {
                        res += (textFormatter.asStr06d(rangeFrom) + "-" + textFormatter.asStr06d(previousNumber));
                    }
                    printer.printTextInNormalMode(res);
                    res = "";
                    previousNumber = reel.getNumber();
                    rangeFrom = previousNumber;
                }
            } else {
                if (!previousSeries.isEmpty()) {
                    // Сменилась серия, выводим последние номера с предыдущей
                    if (rangeFrom == previousNumber) {
                        res += (textFormatter.asStr06d(rangeFrom));
                    } else {
                        res += (textFormatter.asStr06d(rangeFrom) + "-" + textFormatter.asStr06d(previousNumber));
                    }
                    printer.printTextInNormalMode(res);
                    res = "";
                }
                res = reel.getSeries() + " ";
                rangeFrom = reel.getNumber();
                previousNumber = rangeFrom;
                previousSeries = reel.getSeries();
            }
            if (!reels.hasNext()) {
                // Конец списка, выводим последнюю строку
                if (rangeFrom == previousNumber) {
                    res += (textFormatter.asStr06d(rangeFrom));
                } else {
                    res += (textFormatter.asStr06d(rangeFrom) + "-" + textFormatter.asStr06d(previousNumber));
                }
                // Последняя бобина израсходована полностью
                res += " вкл.";
                printer.printTextInNormalMode(res);
                res = "";
            }
        }
        int reelsCount = params.reels.size();
        if (reelsCount==0)
            printer.printTextInNormalMode("ЛЕНТА НЕ ЗАКОНЧИЛАСЬ");
        printer.printTextInNormalMode("КОЛИЧ. БОБИН        =" + reelsCount);
        printer.printTextInNormalMode("КОЛИЧ. ЧЕКОВ        =" + params.receiptsCount);
        int reportsCount = params.testShiftSheetsCount + params.shiftSheetsCount + params.discountShiftSheetsCount + params.auditTrailsCount
                + params.discountMonthlySheetsCount + params.monthlySheetsCount + params.testMonthlySheetsCount + params.salesForEttLogCount;
        printer.printTextInNormalMode("КОЛИЧ. ОТЧЕТОВ      =" + reportsCount);
        if (params.testShiftSheetsCount != 0) {
            printer.printTextInNormalMode(" ПРОБНАЯ СМЕННАЯ    =" + params.testShiftSheetsCount);
        }
        if (params.shiftSheetsCount != 0) {
            printer.printTextInNormalMode(" СМЕННАЯ            =" + params.shiftSheetsCount);
        }
        if (params.discountShiftSheetsCount != 0) {
            printer.printTextInNormalMode(" ЛЬГОТНАЯ СМЕННАЯ   =" + params.discountShiftSheetsCount);
        }
        if (params.auditTrailsCount != 0) {
            printer.printTextInNormalMode(" КОНТРОЛЬНЫЙ ЖУРНАЛ =" + params.auditTrailsCount);
        }
        if (params.salesForEttLogCount != 0) {
            printer.printTextInNormalMode(" ЖУРНАЛ ПО ЭТТ      =" + params.salesForEttLogCount);
        }
        if (params.testMonthlySheetsCount != 0) {
            printer.printTextInNormalMode(" ПРОБНАЯ МЕСЯЧНАЯ   =" + params.testMonthlySheetsCount);
        }
        if (params.discountMonthlySheetsCount != 0) {
            printer.printTextInNormalMode(" ЛЬГОТНАЯ МЕСЯЧНАЯ  =" + params.discountMonthlySheetsCount);
        }
        if (params.monthlySheetsCount != 0) {
            printer.printTextInNormalMode(" МЕСЯЧНАЯ           =" + params.monthlySheetsCount);
        }
        printer.printTextInNormalMode("РАСХОД Л.(м)   =" + textFormatter.asMoney(params.consumptionInMeters));
        printer.printTextInNormalMode(textFormatter.bigDelimiter());

    }

    public static class Params {

        /**
         * Серии и номера бобин в смене или месяце
         */
        public ArrayList<Reel> reels = new ArrayList<Reel>();
        /**
         * Количество выходных форм, распечатанных за месяц на билетной ленте
         * полностью израсходованных бобин
         */
        public Integer receiptsCount = 0;
        /**
         * количество распечатанных отчетов в текущей смене/месяце
         * <p>
         * Пробная сменная ведомость
         */
        public Integer testShiftSheetsCount = 0;
        /**
         * количество распечатанных отчетов в текущей смене/месяце
         * <p>
         * Сменная ведомость
         */
        public Integer shiftSheetsCount = 0;
        /**
         * количество распечатанных отчетов в текущей смене/месяце
         * <p>
         * Льготная сменная ведомость
         */
        public Integer discountShiftSheetsCount = 0;
        /**
         * количество распечатанных отчетов в текущей смене/месяце
         * <p>
         * Льготная месячная ведомость
         */
        public Integer discountMonthlySheetsCount = 0;
        /**
         * количество распечатанных отчетов в текущей смене/месяце
         * <p>
         * Месячная ведомость
         */
        public Integer monthlySheetsCount = 0;
        /**
         * количество распечатанных отчетов в текущей смене/месяце
         * <p>
         * Пробная месячная ведомость
         */
        public Integer testMonthlySheetsCount = 0;
        /**
         * количество распечатанных отчетов в текущей смене/месяце
         * <p>
         * Журнал оформления по ЭТТ
         */
        public Integer salesForEttLogCount = 0;
        /**
         * количество распечатанных отчетов в текущей смене/месяце
         * <p>
         * Контрольный журнал
         */
        public Integer auditTrailsCount = 0;
        /**
         * Расход ленты в метрах
         */
        public BigDecimal consumptionInMeters = BigDecimal.ZERO;
    }

    public static class Reel {

        private String series;
        private int number;

        public Reel(String series, int number) {
            this.series = series;
            this.number = number;
        }

        public String getSeries() {
            return series;
        }

        public void setSeries(String series) {
            this.series = series;
        }

        public int getNumber() {
            return number;
        }

        public void setNumber(int number) {
            this.number = number;
        }

    }

}
