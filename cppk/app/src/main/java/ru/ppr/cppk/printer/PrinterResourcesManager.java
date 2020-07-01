package ru.ppr.cppk.printer;

import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import ru.ppr.cppk.helpers.PrivateSettingsHolder;
import ru.ppr.ikkm.IPrinter;
import ru.ppr.ikkm.exception.PrinterException;
import ru.ppr.logger.Logger;
import ru.ppr.utils.Executors;

/**
 * Менеджер для упарвления ресурсами принтера.
 * Обуспечивает задержку уведомления принтера о необходимости освободить ресурсы.
 *
 * @author Aleksandr Brazhkin
 */
public class PrinterResourcesManager {

    private static final String TAG = Logger.makeLogTag(PrinterResourcesManager.class);

    private final Object LOCK = new Object();
    /**
     * Принтер
     */
    private final IPrinter printer;
    /**
     * Отложенная задача освобождения ресурсов
     */
    private Future delayedFreeTask = null;
    /**
     * Флаг для уверенности, что нет вызовов prepare/free в неправильном порядке.
     */
    private boolean resourcesArePreparedLocal;
    /**
     * Флаг для исключения повторного вызова методов на принтере.
     */
    private boolean resourcesArePreparedAtPrinter;
    /**
     * Хранилище Частных настроек ПТК
     */
    private final PrivateSettingsHolder privateSettingsHolder;

    public PrinterResourcesManager(IPrinter printer, PrivateSettingsHolder privateSettingsHolder) {
        this.printer = printer;
        this.privateSettingsHolder = privateSettingsHolder;
    }

    /**
     * Выполняет подготовку внешних ресурсов, необходимых для работы принтера (Bluetooth, Network)
     *
     * @throws Exception В случае ошибки выполнения операции
     */
    public void prepareResources() throws Exception {
        synchronized (LOCK) {
            if (resourcesArePreparedLocal) {
                throw new IllegalStateException("Resources are prepared already");
            }
            if (delayedFreeTask != null) {
                delayedFreeTask.cancel(false);
                delayedFreeTask = null;
            }
            resourcesArePreparedLocal = true;
            if (!resourcesArePreparedAtPrinter) {
                printer.prepareResources();
                resourcesArePreparedAtPrinter = true;
            }
        }
    }

    /**
     * Выполняет освобождение внешних ресурсов, необходимых для работы принтера (Bluetooth, Network)
     *
     * @param rightNow {@code true} если осовбождение следует произвести немедленно, {@code false} иначе
     * @throws Exception В случае ошибки выполнения операции
     */
    public void freeResources(boolean rightNow) throws Exception {
        synchronized (LOCK) {
            if (!resourcesArePreparedLocal) {
                throw new IllegalStateException("Resources are not prepared yet");
            }
            resourcesArePreparedLocal = false;
            if (rightNow) {
                freeRightNow();
            } else {
                delayedFreeTask = scheduledExecutorService.schedule(() -> {
                    freeRightNow();
                    return null;
                }, privateSettingsHolder.get().getPrinterDisconnectTimeout(), TimeUnit.SECONDS);
            }
        }
    }

    /**
     * Выполняет освобождение внешних ресурсов, необходимых для работы принтера (Bluetooth, Network)
     *
     * @throws PrinterException В случае ошибки выполнения операции
     */
    private void freeRightNow() throws PrinterException {
        synchronized (LOCK) {
            if (resourcesArePreparedAtPrinter) {
                printer.freeResources();
                resourcesArePreparedAtPrinter = false;
            }
        }
    }

    /**
     * {@link Executor}  для запуска операций в отдельном потоке.
     */
    private final ScheduledExecutorService scheduledExecutorService = new ScheduledThreadPoolExecutor(
            1, r -> new Thread(r, "PrinterResourcesManagerExecutor")) {
        @Override
        protected void afterExecute(Runnable r, Throwable t) {
            super.afterExecute(r, t);
            Executors.logErrorAfterExecute(TAG, r, t);
        }
    };
}
