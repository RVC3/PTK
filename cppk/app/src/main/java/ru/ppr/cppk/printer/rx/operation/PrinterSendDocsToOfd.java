package ru.ppr.cppk.printer.rx.operation;

import android.os.SystemClock;

import java.util.concurrent.TimeUnit;

import ru.ppr.cppk.printer.PrinterResourcesManager;
import ru.ppr.cppk.printer.rx.operation.base.PrinterBaseOperation;
import ru.ppr.ikkm.IPrinter;
import ru.ppr.ikkm.model.OfdDocsState;
import ru.ppr.logger.Logger;
import rx.Observable;

/**
 * Операция отправки данных в ОФД
 *
 * @author Grigoriy Kashka
 */
public class PrinterSendDocsToOfd extends PrinterBaseOperation {

    private static final String TAG = Logger.makeLogTag(PrinterSendDocsToOfd.class);

    /**
     * Пауза между попытками запроса в миллисекундах
     */
    private static final long DELAY = 3000;

    /**
     * Максимальное время ожидания отправки данных в ОФД в секундах.
     */
    private final int timeout;

    /**
     * Конструктор
     *
     * @param printer                 - принтер
     * @param timeout                 - максимальное время ожидания отправки данных в ОФД в секундах.
     * @param printerResourcesManager - менеджер для управления ресурсами
     */
    public PrinterSendDocsToOfd(IPrinter printer, int timeout, PrinterResourcesManager printerResourcesManager) {
        super(printer, printerResourcesManager);
        this.timeout = timeout;
    }

    public Observable<OfdDocsState> call() {
        Logger.trace(TAG, "call(timeout=" + timeout + ")");
        return wrap(() -> {
            long startTime = SystemClock.elapsedRealtime();
            OfdDocsState ofdDocsState = printer.getOfdDocsState();
            if (ofdDocsState.getUnsentDocumentsCount() > 0) {
                printer.startSendingDocsToOfd();
                while (ofdDocsState.getUnsentDocumentsCount() > 0 && (startTime + TimeUnit.SECONDS.toMillis(timeout) - DELAY) > SystemClock.elapsedRealtime()) {
                    Thread.sleep(DELAY);
                    ofdDocsState = printer.getOfdDocsState();
                }
            }

            return ofdDocsState;
        });
    }

}
