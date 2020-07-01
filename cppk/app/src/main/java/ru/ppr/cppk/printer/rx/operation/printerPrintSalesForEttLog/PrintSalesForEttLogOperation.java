package ru.ppr.cppk.printer.rx.operation.printerPrintSalesForEttLog;

import java.util.Date;

import ru.ppr.cppk.printer.PrinterResourcesManager;
import ru.ppr.cppk.printer.TextFormatter;
import ru.ppr.cppk.printer.rx.operation.base.PrinterBaseOperation;
import ru.ppr.ikkm.IPrinter;
import ru.ppr.logger.Logger;
import rx.Observable;

/**
 * Created by Александр on 11.08.2016.
 * <p>
 * 1.6 Журнал оформления по ЭТТ
 */
public class PrintSalesForEttLogOperation extends PrinterBaseOperation {

    private static final String TAG = Logger.makeLogTag(PrintSalesForEttLogOperation.class);

    private final PrinterTplSalesForEttLog.Params params;
    private final TextFormatter textFormatter;

    public PrintSalesForEttLogOperation(IPrinter printer, PrinterTplSalesForEttLog.Params params, TextFormatter textFormatter, PrinterResourcesManager printerResourcesManager) {
        super(printer, printerResourcesManager);
        this.params = params;
        this.textFormatter = textFormatter;
    }

    public Observable<Result> call() {
        return wrap(() -> {
            Date date = printer.getDate();
            params.date = date;

            new PrinterTplSalesForEttLog(params, textFormatter).printToDriver(printer);
            return new Result(date);
        });
    }

    @Override
    protected void connect() throws Exception {
        connectWithCheckingEKLZ();
    }

    public static class Result {

        private Date operationTime;

        public Result(Date date) {
            this.operationTime = date;
        }

        public Date getOperationTime() {
            return operationTime;
        }
    }
}
