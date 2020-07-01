package ru.ppr.cppk.printer.rx.operation.btMonthReport;

import java.util.Date;

import ru.ppr.cppk.printer.PrinterResourcesManager;
import ru.ppr.cppk.printer.TextFormatter;
import ru.ppr.cppk.printer.rx.operation.base.PrinterBaseOperation;
import ru.ppr.ikkm.IPrinter;
import ru.ppr.logger.Logger;
import rx.Observable;

/**
 * @author Aleksandr Brazhkin
 */
public class ShtrihPrintBtMonthReportOperation extends PrinterBaseOperation implements PrintBtMonthReportOperation {

    private static final String TAG = Logger.makeLogTag(ShtrihPrintBtMonthReportOperation.class);

    private final BtMonthReportTpl.Params params;
    private final TextFormatter textFormatter;

    public ShtrihPrintBtMonthReportOperation(IPrinter printer, TextFormatter textFormatter, BtMonthReportTpl.Params params, PrinterResourcesManager printerResourcesManager) {
        super(printer, printerResourcesManager);
        this.textFormatter = textFormatter;
        this.params = params;
    }

    @Override
    public Observable<Result> call() {
        return wrap(() -> {
            Date date = printer.getDate();
            params.setDate(date);

            new BtMonthReportTpl(params, textFormatter).printToDriver(printer);

            return new PrintBtMonthReportOperation.Result(date);
        });
    }

    @Override
    protected void connect() throws Exception {
        connectWithCheckingEKLZ();
    }
}
