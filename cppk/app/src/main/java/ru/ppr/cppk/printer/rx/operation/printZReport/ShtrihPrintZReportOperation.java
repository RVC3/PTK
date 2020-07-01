package ru.ppr.cppk.printer.rx.operation.printZReport;

import ru.ppr.cppk.printer.PrinterResourcesManager;
import ru.ppr.cppk.printer.rx.operation.base.InOpenedShiftOperation;
import ru.ppr.cppk.printer.rx.operation.base.PrinterBaseOperation;
import ru.ppr.ikkm.IPrinter;
import rx.Observable;

/**
 * @author Dmitry Nevolin
 */
public class ShtrihPrintZReportOperation extends InOpenedShiftOperation implements PrintZReportOperation {


    public ShtrihPrintZReportOperation(IPrinter printer, PrinterResourcesManager printerResourcesManager) {
        super(printer, printerResourcesManager);
    }

    @Override
    public Observable<Result> call() {
        return wrap(() -> {
            if (printer.isShiftOpened()) {
                printer.printZReport();
                printer.printTextInNormalMode("");
                printer.printTextInNormalMode("");
                printer.printTextInNormalMode("");
                printer.printTextInNormalMode("");
            }

            return new PrintZReportOperation.Result(printer.getDate());
        });
    }

}
