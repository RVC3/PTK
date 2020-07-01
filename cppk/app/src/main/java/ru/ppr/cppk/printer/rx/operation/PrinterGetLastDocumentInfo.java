package ru.ppr.cppk.printer.rx.operation;

import java.util.Date;

import ru.ppr.cppk.printer.PrinterResourcesManager;
import ru.ppr.cppk.printer.rx.operation.base.PrinterBaseOperation;
import ru.ppr.ikkm.IPrinter;
import ru.ppr.logger.Logger;
import rx.Observable;

/**
 * Команда получения информации о последнем фискальном документе.
 *
 * @author Aleksandr Brazhkin
 */
public class PrinterGetLastDocumentInfo extends PrinterBaseOperation {

    private static final String TAG = Logger.makeLogTag(PrinterGetLastDocumentInfo.class);

    public PrinterGetLastDocumentInfo(IPrinter printer, PrinterResourcesManager printerResourcesManager) {
        super(printer, printerResourcesManager);
    }

    public Observable<Result> call() {
        return wrap(() -> {
            Date date = printer.getLastCheckTime();
            int SPND = printer.getLastSPND();

            return new Result(date, SPND);
        });
    }

    /**
     * Результат печати чека
     */
    public static class Result {

        /**
         * Сквозной номер документа (СПНД) назначается самим ФР.
         */
        private int spnd;
        /**
         * Время проведения операции по фискальнику
         */
        private Date operationTime;

        public Result(Date date, int spnd) {
            this.spnd = spnd;
            operationTime = date;
        }

        /**
         * Вернет номер СПНД
         *
         * @return
         */
        public int getSpnd() {
            return spnd;
        }

        /**
         * Вернет дату/время совершения операции на фискальние
         *
         * @return
         */
        public Date getOperationTime() {
            return operationTime;
        }

        @Override
        public String toString() {
            return "Result{" +
                    "spnd=" + spnd +
                    ", operationTime=" + operationTime +
                    '}';
        }
    }
}
