package ru.ppr.cppk.printer.rx.operation;

import java.util.List;

import ru.ppr.cppk.printer.PrinterResourcesManager;
import ru.ppr.cppk.printer.rx.operation.base.PrinterBaseOperation;
import ru.ppr.ikkm.IPrinter;
import ru.ppr.logger.Logger;
import rx.Observable;

public class PrinterPrintLines extends PrinterBaseOperation {

    private static final String TAG = Logger.makeLogTag(PrinterPrintLines.class);

    public PrinterPrintLines(IPrinter printer, PrinterResourcesManager printerResourcesManager) {
        super(printer, printerResourcesManager);
    }

    private List<String> textLines;
    /**
     * Нужно ли добавить отступ в конце
     */
    private boolean addSpaceAtTheEnd;

    public PrinterPrintLines setTextLines(List<String> textLines) {
        this.textLines = textLines;
        return this;
    }

    public PrinterPrintLines setAddSpaceAtTheEnd(boolean addSpaceAtTheEnd) {
        this.addSpaceAtTheEnd = addSpaceAtTheEnd;
        return this;
    }

    public Observable<Void> call() {
        return wrap(() -> {
            for (String line : textLines)
                printer.printTextInNormalMode(line);

            if (addSpaceAtTheEnd) {
                //добавим пустых строк на отрыв, чтобы линия отрыва была ниже распечатанной строки
                printer.printTextInNormalMode("");
                printer.printTextInNormalMode("");
                printer.printTextInNormalMode("");
                printer.printTextInNormalMode("");
            }

            printer.waitPendingOperations();

            return (Void) null;
        });
    }

    @Override
    protected void connect() throws Exception {
        connectWithCheckingEKLZ();
    }
}
