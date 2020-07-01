package ru.ppr.cppk.printer.rx.operation.ticketTapeStartReport;

import java.util.Date;

import ru.ppr.cppk.printer.PrinterResourcesManager;
import ru.ppr.cppk.printer.TextFormatter;
import ru.ppr.ikkm.IPrinter;
import rx.Observable;

/**
 * @author Dmitry Nevolin
 */
public class ShtrihPrintTapeStartReportOperation extends PrintTapeStartReportOperation {

    private final TapeStartReportTpl.Params params;
    private final TextFormatter textFormatter;

    public ShtrihPrintTapeStartReportOperation(IPrinter printer, TapeStartReportTpl.Params params, TextFormatter textFormatter, PrinterResourcesManager printerResourcesManager) {
        super(printer, printerResourcesManager);

        this.params = params;
        this.textFormatter = textFormatter;
    }

    @Override
    public Observable<Result> call() {
        return wrap(() -> {
            Date date = printer.getDate();

            params.date = date;

            new TapeStartReportTpl(params, textFormatter).printToDriver(printer);
            //добавим пустых строк на отрыв
            printer.printTextInNormalMode("");
            printer.waitPendingOperations();

            return new Result(date);
        });
    }

}
