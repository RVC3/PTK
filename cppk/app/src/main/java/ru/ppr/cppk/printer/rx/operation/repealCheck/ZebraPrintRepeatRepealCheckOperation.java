package ru.ppr.cppk.printer.rx.operation.repealCheck;

import ru.ppr.cppk.printer.PrinterResourcesManager;
import ru.ppr.cppk.printer.TextFormatter;
import ru.ppr.ikkm.IPrinter;
import ru.ppr.logger.Logger;
import rx.Completable;

/**
 * Операция повтора печати чека аннулирования.
 *
 * @author Aleksandr Brazhkin
 */
public class ZebraPrintRepeatRepealCheckOperation extends PrintRepeatRepealCheckOperation {

    private static final String TAG = Logger.makeLogTag(ZebraPrintRepeatRepealCheckOperation.class);

    public ZebraPrintRepeatRepealCheckOperation(IPrinter printer, Params params, TextFormatter textFormatter, PrinterResourcesManager printerResourcesManager) {
        super(printer, params, textFormatter, printerResourcesManager);
    }

    @Override
    public Completable call() {
        return wrap(() -> {
            // Пока нет реализации
            // Не бросаем UnsupportedOperationException, чтобы не крашить приложение
            printer.printTextInNormalMode(textFormatter.alignCenterText("ПЕЧАТЬ ДУБЛИКАТА ЧЕКА"));
            printer.printTextInNormalMode(textFormatter.alignCenterText("НЕ ПОДДЕРЖИВАЕТСЯ"));
            return null;
        }).toCompletable();
    }

    @Override
    protected void connect() throws Exception {
        connectWithCheckingEKLZ();
    }

}
