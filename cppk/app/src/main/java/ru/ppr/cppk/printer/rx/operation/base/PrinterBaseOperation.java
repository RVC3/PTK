package ru.ppr.cppk.printer.rx.operation.base;

import java.util.concurrent.Callable;

import ru.ppr.cppk.di.Di;
import ru.ppr.cppk.printer.PrinterResourcesManager;
import ru.ppr.cppk.printer.exception.IncorrectEKLZNumberException;
import ru.ppr.ikkm.IPrinter;
import ru.ppr.ikkm.exception.PrinterException;
import ru.ppr.logger.Logger;
import rx.Observable;

/**
 * Базовый класс дял операций, выполняемых на принтере.
 *
 * @author Aleksandr Brazhkin
 */
public abstract class PrinterBaseOperation {

    protected final IPrinter printer;
    protected final PrinterResourcesManager printerResourcesManager;

    public PrinterBaseOperation(IPrinter printer, PrinterResourcesManager printerResourcesManager) {
        this.printer = printer;
        this.printerResourcesManager = printerResourcesManager;
    }

    /**
     * Подключение к принтеру с проверкой номера EKLZ (если номер ЭКЛЗ не тот, то мы не имеем права работать на этом фискальнике)
     *
     * @throws PrinterException
     * @throws IncorrectEKLZNumberException
     */
    protected void connectWithCheckingEKLZ() throws PrinterException, IncorrectEKLZNumberException {
        printer.connect();
        String printerEKLZNumber = printer.getEKLZNumber();
        String printerSerialNumber = printer.getRegNumber();
        if (!Di.INSTANCE.printerManager().getEklzChecker().check(printerEKLZNumber, printerSerialNumber)) {
            printer.disconnect();
            throw new IncorrectEKLZNumberException();
        }
    }

    /**
     * Оборачивает код в {@link Observable}, содержащий в себе подключение к принтеру и подготовку/освобождение русурсов.
     *
     * @param callable Исполняемый код
     * @param <T>      Тип возвращамого результата
     * @return Результат выполения {@code callable}
     */
    protected <T> Observable<T> wrap(Callable<T> callable) {
        return Observable
                .fromCallable(() -> {
                    try {
                        prepareResources();
                        connect();
                        performAdditionalChecks();
                        T result = callable.call();
                        freeResources(false);
                        return result;
                    } catch (Exception e) {
                        Logger.error(getClass(), e);
                        freeResources(true);
                        throw e;
                    }
                });
    }

    /**
     * Выполняет подготовку внешних ресурсов, необходимых для работы принтера (Bluetooth, Network)
     *
     * @throws Exception В случае ошибки выполнения операции
     */
    private void prepareResources() throws Exception {
        printerResourcesManager.prepareResources();
    }

    /**
     * Выполняет освобождение внешних ресурсов, необходимых для работы принтера (Bluetooth, Network)
     *
     * @param rightNow {@code true} если осовбождение следует произвести немедленно, {@code false} иначе
     * @throws Exception В случае ошибки выполнения операции
     */
    private void freeResources(boolean rightNow) throws Exception {
        printerResourcesManager.freeResources(rightNow);
    }

    /**
     * Выполняет подключение к принтеру
     *
     * @throws Exception В случае ошибки выполнения операции
     */
    protected void connect() throws Exception {
        printer.connect();
    }

    /**
     * Выполнить дополнительные проверки
     *
     * @throws Exception
     */
    protected void performAdditionalChecks() throws Exception {
    }
}
