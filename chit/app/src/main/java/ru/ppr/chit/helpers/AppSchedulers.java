package ru.ppr.chit.helpers;

import android.support.annotation.NonNull;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import io.reactivex.Scheduler;
import io.reactivex.schedulers.Schedulers;
import ru.ppr.core.manager.RfidManager;

/**
 * Класс-утилита для доступа к планировщикам приложения
 *
 * @author Dmitry Nevolin
 */
public class AppSchedulers {

    private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors(); // количество доступных ядер процессора
    private static final int CORE_POOL_SIZE = CPU_COUNT + 1; // минимальное количество потоков для работы
    private static final int MAXIMUM_POOL_SIZE = CPU_COUNT * 2 + 1; // максимальное количество потоков дляработы
    private static final int KEEP_ALIVE = 1;

    private static final ExecutorService CLOCK_EXECUTOR_SERVICE = Executors.newSingleThreadExecutor(new DefaultThreadFactory("clock"));
    private static final ExecutorService EXPORT_EXECUTOR_SERVICE = Executors.newSingleThreadExecutor(new DefaultThreadFactory("export"));
    private static final Executor BACKGROUND_THREAD_POOL_EXECUTOR = new ThreadPoolExecutor(
            CORE_POOL_SIZE,
            MAXIMUM_POOL_SIZE,
            KEEP_ALIVE,
            TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(128), // количество задач очереди
            new DefaultThreadFactory("background-thread"));
    private static final ExecutorService RFID_EXECUTOR_SERVICE = RfidManager.RFID_EXECUTOR_SERVICE;

    private static final Scheduler CLOCK_SCHEDULER = Schedulers.from(CLOCK_EXECUTOR_SERVICE);
    private static final Scheduler EXPORT_SCHEDULER = Schedulers.from(EXPORT_EXECUTOR_SERVICE);
    private static final Scheduler BACKGROUND_SCHEDULER = Schedulers.from(BACKGROUND_THREAD_POOL_EXECUTOR);
    private static final Scheduler RFID_SCHEDULER = Schedulers.from(RFID_EXECUTOR_SERVICE);

    /**
     * @return Scheduler для часов
     */
    public static Scheduler clock() {
        return CLOCK_SCHEDULER;
    }

    /**
     * @return Scheduler для ExportService
     */
    public static Scheduler export() {
        return EXPORT_SCHEDULER;
    }

    /**
     * Возвращает Scheduler для работы Rfid модуля
     */
    public static Scheduler rfid() {
        return RFID_SCHEDULER;
    }

    /**
     * Возвращает Scheduler для выполнения фоновых задач
     */
    public static Scheduler background() {
        return BACKGROUND_SCHEDULER;
    }

    private static class DefaultThreadFactory implements ThreadFactory {

        private final AtomicInteger count = new AtomicInteger(1);
        private final String factoryName;

        private DefaultThreadFactory(String factoryName) {
            this.factoryName = factoryName;
        }

        @Override
        public Thread newThread(@NonNull Runnable runnable) {
            return new Thread(runnable, factoryName + "-" + count.getAndIncrement());
        }

    }

}
