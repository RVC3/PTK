package ru.ppr.cppk.printer.rx.operation.openShift;

import java.util.Date;

import ru.ppr.cppk.printer.PrinterResourcesManager;
import ru.ppr.cppk.printer.TextFormatter;
import ru.ppr.cppk.printer.paramBuilders.FiscalHeaderBuilder;
import ru.ppr.cppk.printer.rx.operation.base.InClosedShiftOperation;
import ru.ppr.ikkm.IPrinter;
import rx.Observable;

/**
 * Операция открытия смены на принтере.
 *
 * @author Aleksandr Brazhkin
 */
public abstract class OpenShiftOperation extends InClosedShiftOperation {

    final Params params;
    final TextFormatter textFormatter;

    OpenShiftOperation(IPrinter printer, Params params, TextFormatter textFormatter, PrinterResourcesManager printerResourcesManager) {
        super(printer, printerResourcesManager);
        this.params = params;
        this.textFormatter = textFormatter;
    }

    public abstract Observable<Result> call();

    public static class Result {
        /**
         * Время открытия смены
         */
        private Date operationTime;
        /**
         * Номер открытой смены
         */
        private int shiftNum;

        /**
         * Номер последнего фискального документа
         */
        private int spndNumber;

        public Result(Date date, int shiftNum, int spndNumber) {
            this.operationTime = date;
            this.shiftNum = shiftNum;
            this.spndNumber = spndNumber;
        }

        public Date getOperationTime() {
            return operationTime;
        }

        public int getShiftNum() {
            return shiftNum;
        }

        public int getSpndNumber() {
            return spndNumber;
        }
    }

    public static class Params {

        /**
         * Фамилия и инициалы кассира
         */
        public String userName;
        /**
         * Код кассира (порядковый номер кассира в смене)
         */
        public int userId;
        /**
         * Параметры для заголовка
         */
        public FiscalHeaderBuilder.Params headerParams;

        @Override
        public String toString() {
            return "OpenShiftOperation.Params{" +
                    "userName='" + userName + '\'' +
                    ", userId=" + userId +
                    ", headerParams=" + headerParams +
                    '}';
        }
    }
}
