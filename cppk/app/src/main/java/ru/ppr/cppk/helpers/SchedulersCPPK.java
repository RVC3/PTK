package ru.ppr.cppk.helpers;

import android.support.annotation.NonNull;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import ru.ppr.core.manager.BarcodeManager;
import ru.ppr.core.manager.eds.EdsManager;
import ru.ppr.core.manager.RfidManager;
import rx.Scheduler;
import rx.schedulers.Schedulers;

/**
 * Created by Александр on 22.12.2015.
 */
public class SchedulersCPPK {

    private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors(); // количество доступных ядер процессора
    private static final int CORE_POOL_SIZE = CPU_COUNT + 1; // минимальное количество потоков для работы
    private static final int MAXIMUM_POOL_SIZE = CPU_COUNT * 2 + 1; // максимальное количество потоков дляработы
    private static final int KEEP_ALIVE = 1;

    private static final AtomicInteger PRINTER_COUNTER = new AtomicInteger(0);

    private static final Executor NETWORK_THREAD_POOL_EXECUTOR = new ThreadPoolExecutor(
            CORE_POOL_SIZE,
            MAXIMUM_POOL_SIZE,
            KEEP_ALIVE,
            TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(128), // количество задач очереди
            new DefaultThreadFactory("network-thread"));

    private static final Executor BACKGROUND_THREAD_POOL_EXECUTOR = new ThreadPoolExecutor(
            CORE_POOL_SIZE,
            MAXIMUM_POOL_SIZE,
            KEEP_ALIVE,
            TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(128),  // количество задач очереди
            new DefaultThreadFactory("background-thread"));

    private static final ExecutorService POS_TERMINAL_EXECUTOR_SERVICE = Executors.newSingleThreadExecutor(new DefaultThreadFactory("pos-terminal"));
    private static final ExecutorService RFID_EXECUTOR_SERVICE = RfidManager.RFID_EXECUTOR_SERVICE;
    private static final ExecutorService BARCODE_EXECUTOR_SERVICE = BarcodeManager.BARCODE_EXECUTOR_SERVICE;

    private static final Scheduler RFID_SCHEDULER = Schedulers.from(RFID_EXECUTOR_SERVICE);
    private static final Scheduler BARCODE_SCHEDULER = Schedulers.from(BARCODE_EXECUTOR_SERVICE);
    private static final Scheduler NETWORK_EXECUTOR = Schedulers.from(NETWORK_THREAD_POOL_EXECUTOR);
    private static final Scheduler BACKGROUND_SCHEDULER = Schedulers.from(BACKGROUND_THREAD_POOL_EXECUTOR);
    private static final Scheduler PRINTER_SCHEDULER = Schedulers.from(Executors.newSingleThreadExecutor(SchedulersCPPK.defaultThreadFactory("Printer")));
    private static final Scheduler POS_TERMINAL_SCHEDULER = Schedulers.from(POS_TERMINAL_EXECUTOR_SERVICE);
    private static final Scheduler EDS_SCHEDULER = Schedulers.from(EdsManager.EDS_EXECUTOR);

    /**
     * Возвращает Scheduler для работы принтера
     *
     * @return
     */
    public static Scheduler printer() {
        return PRINTER_SCHEDULER;
    }

    /**
     * Возвращает Scheduler для работы POS-терминала
     *
     * @return
     */
    public static Scheduler posTerminal() {
        return POS_TERMINAL_SCHEDULER;
    }

    public static ExecutorService posTerminalExecutorService() {
        return POS_TERMINAL_EXECUTOR_SERVICE;
    }

    /**
     * Возвращает Scheduler для работы Rfid модулем
     *
     * @return
     */
    public static Scheduler rfid() {
        return RFID_SCHEDULER;
    }

    /**
     * Возвращает Scheduler для работы Barcode модулем
     *
     * @return
     */
    public static Scheduler barcode() {
        return BARCODE_SCHEDULER;
    }

    public static ExecutorService rfidExecutorService() {
        return RFID_EXECUTOR_SERVICE;
    }

    /**
     * Возвращает Scheduler для выполнения фоновых задач
     *
     * @return
     */
    public static Scheduler background() {
        return BACKGROUND_SCHEDULER;
    }

    public static Executor backgroundExecutor() {
        return BACKGROUND_THREAD_POOL_EXECUTOR;
    }

    /**
     * Возвращает Scheduler для выполнения сетевых запросов
     *
     * @return
     */
    public static Scheduler network() {
        return NETWORK_EXECUTOR;
    }

    /**
     * Возвращает Scheduler для выполнения запросов связанных с ЭЦП.
     *
     * @return
     */
    public static Scheduler eds() {
        return EDS_SCHEDULER;
    }

    private static class DefaultThreadFactory implements ThreadFactory {
        private final AtomicInteger mCount = new AtomicInteger(1);
        private final String factoryName;

        private DefaultThreadFactory(String factoryName) {
            this.factoryName = factoryName;
        }

        @Override
        public Thread newThread(@NonNull Runnable r) {
            return new Thread(r, factoryName + "-" + mCount.getAndIncrement());
        }
    }

    public static ThreadFactory defaultThreadFactory(String factoryName) {
        return new DefaultThreadFactory(factoryName);
    }

}
