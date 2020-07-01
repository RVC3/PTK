package ru.ppr.cppk.printer.rx.operation;

import java.util.List;

import ru.ppr.cppk.printer.PrinterResourcesManager;
import ru.ppr.cppk.printer.rx.operation.base.PrinterBaseOperation;
import ru.ppr.ikkm.IPrinter;
import ru.ppr.logger.Logger;
import rx.Observable;

/**
 * Created by Александр on 21.07.2016.
 */
public class PrinterGetShiftsInfo extends PrinterBaseOperation {

    private static final String TAG = Logger.makeLogTag(PrinterGetShiftsInfo.class);

    public PrinterGetShiftsInfo(IPrinter printer, PrinterResourcesManager printerResourcesManager) {
        super(printer, printerResourcesManager);
    }

    private Params params;

    public PrinterGetShiftsInfo setParams(Params params) {
        this.params = params;
        return this;
    }

    public Observable<List<IPrinter.ClosedShiftInfo>> call() {
        return wrap(() -> printer.getShiftsInfo(params.startShiftNum, params.endShiftNum));
    }

    public static class Params {
        public int startShiftNum;
        public int endShiftNum;
    }

}
