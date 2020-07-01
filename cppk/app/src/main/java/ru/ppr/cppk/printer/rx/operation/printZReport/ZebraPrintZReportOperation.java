package ru.ppr.cppk.printer.rx.operation.printZReport;

import ru.ppr.cppk.printer.PrinterResourcesManager;
import ru.ppr.cppk.printer.rx.operation.base.InOpenedShiftOperation;
import ru.ppr.cppk.printer.rx.operation.base.PrinterBaseOperation;
import ru.ppr.ikkm.IPrinter;
import rx.Observable;

/**
 * @author Dmitry Nevolin
 */
public class ZebraPrintZReportOperation extends InOpenedShiftOperation implements PrintZReportOperation {

    private final Params params;

    public ZebraPrintZReportOperation(IPrinter printer, PrinterResourcesManager printerResourcesManager, Params params) {
        super(printer, printerResourcesManager);

        this.params = params;
    }

    @Override
    public Observable<Result> call() {
        return wrap(() -> {
            if (printer.isShiftOpened()) {
                printer.printZReport();
            }

            return new PrintZReportOperation.Result(printer.getDate());
        });
    }

    @Override
    protected void connect() throws Exception {
        if (params.checkEklz) {
            connectWithCheckingEKLZ();
        } else {
            printer.connect();
        }
    }

}
