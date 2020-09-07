package ru.ppr.cppk.printer.rx.operation.shiftOrMonthSheet;

import ru.ppr.cppk.printer.TextFormatter;
import ru.ppr.cppk.printer.tpl.PrinterTpl;
import ru.ppr.ikkm.IPrinter;

/**
 * Шаблон печати.
 * Количество документов за смену (п.1.1.5)
 *
 * @author Aleksandr Brazhkin
 */
public class PdCountTpl extends PrinterTpl {

    private final Params params;
    private final TextFormatter textFormatter;

    PdCountTpl(Params params, TextFormatter textFormatter) {
        this.params = params;
        this.textFormatter = textFormatter;
    }

    @Override
    public void printToDriver(IPrinter printer) throws Exception {

        printer.printTextInNormalMode("КОЛИЧЕСТВО ДОКУМЕНТОВ");
        if (!params.isMonthly) {
            printer.printTextInNormalMode("ЗА СМЕНУ");
        }
        printer.printTextInNormalMode(" ВСЕГО         =" + params.totalDocumentCount);
        if (params.testPDCount > 0 || !params.isTestAtShiftStart) {
            printer.printTextInNormalMode(" ПРОБН.        =" + params.testPDCount);
        }
        if (params.onlyOncePDCount > 0 || !params.isTestAtShiftStart) {
            printer.printTextInNormalMode(" РАЗОВЫХ       =" + params.onlyOncePDCount);
            printer.printTextInNormalMode("  ВКЛ.ДОПЛ.7000=" + params.forExtraChargePDCount);
        }
        if (params.baggagePDCount > 0 || !params.isTestAtShiftStart) {
            printer.printTextInNormalMode(" БАГАЖ         =" + params.baggagePDCount);
        }
        if (params.servicePDcount > 0 || !params.isTestAtShiftStart) {
            printer.printTextInNormalMode(" УСЛУГИ        =" + params.servicePDcount);
        }
        if (params.transferCount() > 0 || !params.isTestAtShiftStart) {
            printer.printTextInNormalMode(" ТРАНСФЕР РАЗОВ=" + params.transferSingleCount);
            printer.printTextInNormalMode(" ТРАНСФЕР АБ   =" + params.transferSeasonCount);
        }
        if (params.fineCount > 0 || !params.isTestAtShiftStart) {
            printer.printTextInNormalMode(" ШТРАФЫ        =" + params.fineCount);
        }
        if (params.canceledPDcount > 0 || !params.isTestAtShiftStart) {
            printer.printTextInNormalMode(" АННУЛ.        =" + params.canceledPDcount);
        }
        printer.printTextInNormalMode(textFormatter.bigDelimiter());
        printer.closePage(0);
    }

    public static class Params {

        /**
         * Является месячной
         */
        protected Boolean isMonthly = false;
        /**
         * Является пробной на начало смены
         */
        public Boolean isTestAtShiftStart = false;
        /**
         * Общее количество выданных документов
         */
        public Integer totalDocumentCount = 0;
        /**
         * Количество пробных документов
         */
        public Integer testPDCount = 0;
        /**
         * Количество разовых ПД
         */
        public Integer onlyOncePDCount = 0;
        /**
         * Количество ПД, проданных по доплате
         */
        public Integer forExtraChargePDCount = 0;
        /**
         * Количество квитанций на провоз багажа
         */
        public Integer baggagePDCount = 0;
        /**
         * Количество услуг
         */
        public Integer servicePDcount = 0;
        /**
         * Количество аннулирований
         */
        public Integer canceledPDcount = 0;
        /**
         * Общее количество проданных штрафов
         */
        public Integer fineCount = 0;
        /**
         * Количество разовых трансферов
         */
        public Integer transferSingleCount = 0;
        /**
         * Количество абонементов трансферов
         */
        public Integer transferSeasonCount = 0;

        /**
         * Общее количество транферов
         */
        Integer transferCount() {
            return transferSingleCount + transferSeasonCount;
        }

    }

}
