package ru.ppr.cppk.printer.rx.operation.openShift;

import java.util.Date;
import java.util.List;

import ru.ppr.cppk.printer.PrinterResourcesManager;
import ru.ppr.cppk.printer.TextFormatter;
import ru.ppr.cppk.printer.paramBuilders.FiscalHeaderBuilder;
import ru.ppr.ikkm.IPrinter;
import ru.ppr.ikkm.exception.PrinterException;
import ru.ppr.logger.Logger;
import rx.Observable;

/**
 * Операция открытия смены на принтере.
 *
 * @author Aleksandr Brazhkin
 */
public class ShtrihOpenShiftOperation extends OpenShiftOperation {

    private static final String TAG = Logger.makeLogTag(ShtrihOpenShiftOperation.class);

    public ShtrihOpenShiftOperation(IPrinter printer, Params params, TextFormatter textFormatter, PrinterResourcesManager printerResourcesManager) {
        super(printer, params, textFormatter, printerResourcesManager);
    }

    @Override
    public Observable<Result> call() {
        return wrap(() -> {
            boolean shiftIsOpened = printer.isShiftOpened();

            if (!shiftIsOpened) {
                FiscalHeaderBuilder fiscalHeaderBuilder = new FiscalHeaderBuilder();
                List<String> headerLines = fiscalHeaderBuilder.buildHeaderLines(params.headerParams, textFormatter);

                printer.setHeaderLines(headerLines);
                // задаем кассира, см. http://agile.srvdev.ru/browse/CPPKPP-35431
                printer.setCashier(params.userId, params.userName);

                // открываем смену на ФР
                printer.openShift(params.userId, params.userName);
            }

            Date date = printer.getDate();
            int shiftNum = printer.getShiftNum();
            int spndNumber = printer.getLastSPND();

            return new Result(date, shiftNum, spndNumber);
        })
                .onErrorResumeNext(throwable -> wrap(() -> {
                    if (printer.isShiftOpened()) {
                        Date date = printer.getDate();
                        int shiftNum = printer.getShiftNum();
                        int spndNumber = printer.getLastSPND();
                        return new Result(date, shiftNum, spndNumber);
                    } else {
                        throw new PrinterException(throwable);
                    }
                }));
    }

    @Override
    protected void connect() throws Exception {
        connectWithCheckingEKLZ();
    }

}
