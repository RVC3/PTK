package ru.ppr.cppk.printer.rx.operation.bankSlip;

import ru.ppr.cppk.printer.PrinterResourcesManager;
import ru.ppr.cppk.printer.TextFormatter;
import ru.ppr.cppk.printer.paramBuilders.ReportClicheParamsBuilder;
import ru.ppr.cppk.printer.tpl.ReportClicheTpl;
import ru.ppr.ikkm.IPrinter;
import ru.ppr.logger.Logger;
import rx.Observable;

/**
 * @author Grigoriy Kashka
 */
public class ShtrihPrintBankSlipOperation extends PrinterPrintBankSlipOperation {

    private static final String TAG = Logger.makeLogTag(ShtrihPrintBankSlipOperation.class);

    public ShtrihPrintBankSlipOperation(IPrinter printer, PrinterPrintBankSlipOperation.Params params, TextFormatter textFormatter, PrinterResourcesManager printerResourcesManager) {
        super(printer, params, textFormatter, printerResourcesManager);
    }

    @Override
    public Observable<Object> call() {
        return wrap(() -> {
            new ReportClicheTpl(ReportClicheParamsBuilder.build(), textFormatter, false).printToDriver(printer);

            new BankSlipTpl(tplPrinter, params.tplParams, textFormatter).printToDriver();

            //добавим пустых строк на отрыв
            for (int i = 0; i < 4; i++) {
                printer.printTextInNormalMode("");
            }

            printer.waitPendingOperations();

            return null;
        });
    }

    @Override
    protected void connect() throws Exception {
        connectWithCheckingEKLZ();
    }
}

