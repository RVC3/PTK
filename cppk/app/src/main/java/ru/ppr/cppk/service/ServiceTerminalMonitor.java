package ru.ppr.cppk.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import ru.ppr.cppk.Globals;
import ru.ppr.cppk.di.Dagger;
import ru.ppr.cppk.managers.PosManager;
import ru.ppr.cppk.model.PosOperationResult;
import ru.ppr.ipos.model.FinancialTransactionResult;
import ru.ppr.logger.Logger;

/**
 * Сервис для отслеживания и отмены незавершенных транзакций на банковском терминале.
 */
public class ServiceTerminalMonitor extends Service {
    private static final String TAG = Logger.makeLogTag(ServiceTerminalMonitor.class);

    private ScheduledExecutorService scheduledExecutorService;

    private static volatile boolean serviceIsRunning;
    private static volatile boolean taskIsRunning;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        serviceIsRunning = true;
        scheduledExecutorService = Executors.newSingleThreadScheduledExecutor(r -> new Thread(r, "ServiceTerminalMonitorThread"));
        scheduledExecutorService.execute(taskRunnable);
        Logger.trace(TAG, "Created");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        shutdown();
        Logger.trace(TAG, "Destroyed.");
    }

    /**
     * Метод для проверки запущен ли сервис.
     *
     * @return результат проверки.
     */
    public static boolean isRunning() {
        return serviceIsRunning;
    }

    /**
     * Метод для проверки выполнения задачи сервисом.
     *
     * @return результат проверки.
     */
    public static boolean isBusy() {
        return taskIsRunning;
    }

    /**
     * Метод для проверки возможности выполнения операции на банковском терминале.
     *
     * @return результат проверки.
     */
    private boolean canWork() {
        final Globals globals = Globals.getInstance();
        final boolean posEnabled = globals.getPrivateSettingsHolder().get().isPosEnabled();

        return posEnabled && globals.getPosManager().isReady();
    }

    /**
     * Метод для проверки наличия незавершенных транзакций на банкоском терминале.
     *
     * @return результат проверки.
     */
    private synchronized static boolean hasWork() {
        return Globals.getInstance().getPosManager().isLastTransactionMustBeCancelled();
    }

    /**
     * Операция отмены последней транзакции
     */
    private Runnable taskRunnable = new Runnable() {
        @Override
        public void run() {

            Logger.trace(TAG, "Run task");

            final long period = Dagger.appComponent().commonSettingsStorage().get().getPosTerminalCheckPeriod();

            if (hasWork()) {
                if (!isBusy() && canWork()) {

                    taskIsRunning = true;

                    Logger.trace(TAG, "start cancelling...");
                    Globals.getInstance().getPosManager().cancelLastTransaction(new PosManager.AbstractFinancialTransactionListener() {
                        @Override
                        public void onConnectionTimeout() {
                            Logger.trace(TAG, "ServiceTerminalMonitor going to sleep!, onConnectionTimeout");
                            scheduleTask(period);
                            taskIsRunning = false;
                        }

                        @Override
                        public void onResult(@NonNull PosOperationResult<FinancialTransactionResult> operationResult) {
                            FinancialTransactionResult result = operationResult.getTransactionResult();
                            if (result == null || !result.isApproved()) {
                                Logger.trace(TAG, "ServiceTerminalMonitor going to sleep!, bad result");
                                scheduleTask(period);
                                return;
                            }
                            Logger.trace(TAG, "Cancelling completed!");
                            taskIsRunning = false;
                        }
                    });
                } else {
                    Logger.trace(TAG, "ServiceTerminalMonitor going to sleep!, is busy");
                    scheduledExecutorService.schedule(taskRunnable, period, TimeUnit.SECONDS);
                    taskIsRunning = false;
                }
            } else {
                Logger.trace(TAG, "Nothing to cancel");
                shutdown();
            }
        }
    };

    /**
     * Метод для планировки выполнения задачи.
     *
     * @param delay задержка перед выполнением в секундах.
     */
    private void scheduleTask(final long delay) {
        try {
            if (serviceIsRunning && !scheduledExecutorService.isTerminated()) {
                scheduledExecutorService.schedule(taskRunnable, delay, TimeUnit.SECONDS);
            }
        } catch (RejectedExecutionException ex) {
            Logger.trace(TAG, ex);
        }
    }

    /**
     * Останавливает сервис.
     */
    private void shutdown() {
        if (serviceIsRunning) {
            Logger.trace(TAG, "shutdown");
            serviceIsRunning = false;
            taskIsRunning = false;
            scheduledExecutorService.shutdown();
            stopSelf();
        }
    }

}