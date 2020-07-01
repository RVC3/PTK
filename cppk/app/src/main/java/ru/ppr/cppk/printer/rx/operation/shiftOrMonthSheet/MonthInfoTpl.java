package ru.ppr.cppk.printer.rx.operation.shiftOrMonthSheet;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import ru.ppr.cppk.printer.TextFormatter;
import ru.ppr.cppk.printer.tpl.PrinterTpl;
import ru.ppr.ikkm.IPrinter;

/**
 * Шаблон печати.
 * Данные по отчету «Месячная ведомость» (п.1.8.1)
 * Данные по отчету "Пробная месячная ведомость"
 *
 * @author Aleksandr Brazhkin
 */
public class MonthInfoTpl extends PrinterTpl {

    private final Params params;
    private final TextFormatter textFormatter;

    MonthInfoTpl(Params params, TextFormatter textFormatter) {
        this.params = params;
        this.textFormatter = textFormatter;
    }

    @Override
    public void printToDriver(IPrinter printer) throws Exception {

        if (params.isTest) {
            printer.printTextInNormalMode(textFormatter.alignCenterText("ПРОБНАЯ ВЕДОМОСТЬ"));
            printer.printTextInNormalMode(textFormatter.alignCenterText("ВЕДОМОСТЬ № " + params.sheetNum));
        } else {
            printer.printTextInNormalMode(textFormatter.alignCenterText("МЕСЯЧНАЯ ВЕДОМОСТЬ"));
        }

        printer.printTextInNormalMode(textFormatter.alignCenterText("МЕСЯЦ № " + params.monthNum));
        printer.printTextInNormalMode(textFormatter.alignCenterText(textFormatter.asDate_dd_MM_yyyy_HH_mm_ss(params.date)));
        printer.printTextInNormalMode("");

        // Выводим список ПО за месяц
        int SWVersionsCount = params.SWversions.size();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
        for (int i = 0; i < SWVersionsCount; i++) {
            SWVersion swversion = params.SWversions.get(i);
            String version = "ПО v." + swversion.getVersion();
            String time = "";
            if (i > 0) {
                // Время установки
                SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
                time = timeFormat.format(swversion.getStart());
            }
            printer.printTextInNormalMode(textFormatter.alignWidthText(version, time));
            String start = dateFormat.format(swversion.getStart());
            String end = dateFormat.format(swversion.getEnd() == null ? params.date : swversion.getEnd());
            printer.printTextInNormalMode(start + "-" + end);
        }
        printer.printTextInNormalMode("");
        // https://aj.srvdev.ru/browse/CPPKPP-28518
        printer.printTextInNormalMode("ПРОБНЫХ МЕС. ВЕДОМОСТЕЙ " + params.testMonthSheetsCount);
        printer.printTextInNormalMode("КОНТР. ЖУРН.       " + params.auditTrailsCount);
        printer.printTextInNormalMode(textFormatter.bigDelimiter());

    }

    public static class Params {

        /**
         * Время с принтера
         */
        public Date date;
        /**
         * Является тестовой
         */
        protected Boolean isTest = true;
        /**
         * Порядковый номер снятой пробной ведомости в месяце
         */
        public Integer sheetNum = 0;
        /**
         * Номер месяца
         */
        public Integer monthNum = 0;
        /**
         * Версия ПО и период функционирования ПТК на данной версии ПО
         */
        public ArrayList<SWVersion> SWversions;
        /**
         * Количество распечатанных контрольных журналов
         */
        public Integer auditTrailsCount = 0;
        /**
         * Количество снятых пробных сменных ведомостей в месяце
         */
        public Integer testShiftSheetsCount = 0;
        /**
         * Количество снятых пробных месячных ведомостей в месяце
         */
        public Integer testMonthSheetsCount = 0;
    }

    public static class SWVersion {

        private String version;
        private Date start;
        private Date end;

        public SWVersion(String version, Date start, Date end) {
            this.version = version;
            this.start = start;
            this.end = end;
        }

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }

        public Date getStart() {
            return start;
        }

        public void setStart(Date start) {
            this.start = start;
        }

        public Date getEnd() {
            return end;
        }

        public void setEnd(Date end) {
            this.end = end;
        }

    }

}
