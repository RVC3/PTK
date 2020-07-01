package ru.ppr.cppk.printer.rx.operation.closeShift;

import java.util.Date;

import ru.ppr.cppk.printer.PrinterResourcesManager;
import ru.ppr.cppk.printer.rx.operation.base.PrinterBaseOperation;
import ru.ppr.ikkm.IPrinter;
import rx.Observable;

/**
 * @author Aleksandr Brazhkin
 */
public class ZebraCloseShiftOperation extends PrinterBaseOperation implements CloseShiftOperation {

    private final Params params;

    public ZebraCloseShiftOperation(IPrinter printer, PrinterResourcesManager printerResourcesManager, Params params) {
        super(printer, printerResourcesManager);
        this.params = params;
    }

    @Override
    public Observable<Result> call() {
        return wrap(() -> {
            boolean shiftIsOpened = printer.isShiftOpened();

            if (shiftIsOpened) {
                // https://aj.srvdev.ru/browse/CPPKPP-26281?focusedCommentId=114468&page=com.atlassian.jira.plugin.system.issuetabpanels:comment-tabpanel#comment-114468
                // Если смена на принтере открыта, то печатаем Z отчет
                // Если же уже закрыта, то просто пропускаем этот шаг
                printer.printZReport();

            }
            Date date = printer.getDate();
            int spnd = printer.getLastSPND();
            return new Result(date, spnd);
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
