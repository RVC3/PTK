package ru.ppr.cppk.printer.rx.operation.closeShift;

import java.util.Date;

import ru.ppr.cppk.printer.PrinterResourcesManager;
import ru.ppr.cppk.printer.TextFormatter;
import ru.ppr.cppk.printer.rx.common.ShtrihFiscalHeaderSetter;
import ru.ppr.cppk.printer.rx.operation.base.PrinterBaseOperation;
import ru.ppr.ikkm.IPrinter;
import rx.Observable;

/**
 * @author Aleksandr Brazhkin
 */
public class ShtrihCloseShiftOperation extends PrinterBaseOperation implements CloseShiftOperation {

    private final TextFormatter textFormatter;
    private final Params params;

    public ShtrihCloseShiftOperation(IPrinter printer, PrinterResourcesManager printerResourcesManager, TextFormatter textFormatter, Params params) {
        super(printer, printerResourcesManager);
        this.textFormatter = textFormatter;
        this.params = params;
    }

    @Override
    public Observable<Result> call() {
        return wrap(() -> {
            new ShtrihFiscalHeaderSetter(printer, textFormatter).setHeader(params.headerParams);

            boolean shiftIsOpened = printer.isShiftOpened();

            if (shiftIsOpened) {
                // https://aj.srvdev.ru/browse/CPPKPP-26281?focusedCommentId=114468&page=com.atlassian.jira.plugin.system.issuetabpanels:comment-tabpanel#comment-114468
                // Если смена на принтере открыта, то печатаем Z отчет
                // Если же уже закрыта, то просто пропускаем этот шаг
                printer.printZReport();
                printer.printTextInNormalMode("");
                printer.printTextInNormalMode("");
                printer.printTextInNormalMode("");
                printer.printTextInNormalMode("");
            }
            Date date = printer.getDate();
            int spnd = printer.getLastSPND();
            return new Result(date, spnd);
        });
    }
}
