package ru.ppr.cppk.printer.rx.operation.openShift;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import ru.ppr.cppk.printer.PrinterResourcesManager;
import ru.ppr.cppk.printer.TextFormatter;
import ru.ppr.cppk.printer.paramBuilders.FiscalHeaderBuilder;
import ru.ppr.ikkm.IPrinter;
import ru.ppr.logger.Logger;
import rx.Observable;

/**
 * Операция открытия смены на принтере.
 *
 * @author Aleksandr Brazhkin
 */
public class ZebraOpenShiftOperation extends OpenShiftOperation {

    private static final String TAG = Logger.makeLogTag(ZebraOpenShiftOperation.class);

    public ZebraOpenShiftOperation(IPrinter printer, Params params, TextFormatter textFormatter, PrinterResourcesManager printerResourcesManager) {
        super(printer, params, textFormatter, printerResourcesManager);
    }

    @Override
    public Observable<OpenShiftOperation.Result> call() {
        return wrap(() -> {
            boolean shiftIsOpened = printer.isShiftOpened();

            if (!shiftIsOpened) {
                FiscalHeaderBuilder fiscalHeaderBuilder = new FiscalHeaderBuilder();
                List<String> headerLines = fiscalHeaderBuilder.buildHeaderLines(params.headerParams, textFormatter);

                // В заголовок устанавливаем только одну строку
                // Остальное печатается обычным текстом при печати чека
                String lastHeaderLine = headerLines.isEmpty() ? "" : headerLines.get(headerLines.size() - 1);
                printer.setHeaderLines(Collections.singletonList(lastHeaderLine));

                // открываем смену на ФР
                printer.openShift(params.userId, params.userName);
            }

            Date date = printer.getDate();
            int shiftNum = printer.getShiftNum();
            int spnd = printer.getLastSPND();

            return new Result(date, shiftNum, spnd);
        });
    }

    @Override
    protected void connect() throws Exception {
        connectWithCheckingEKLZ();
    }

}
