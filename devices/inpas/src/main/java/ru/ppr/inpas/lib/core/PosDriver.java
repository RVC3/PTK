package ru.ppr.inpas.lib.core;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

import ru.ppr.inpas.lib.internal.IPosListener;
import ru.ppr.inpas.lib.logger.InpasLogger;
import ru.ppr.inpas.lib.protocol.SaPacket;

/**
 * Класс выполняющий основной функционал для работы с POS терминалом.
 */
public class PosDriver {
    private static final String TAG = InpasLogger.makeTag(PosDriver.class);

    private static final long DEFAULT_POS_CONNECTION_TIMEOUT = 60_000L;
    private static final long DEFAULT_NETWORK_CONNECTION_TIMEOUT = 60_000L;

    private final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor(r -> new Thread(r, TAG));

    private final String mDevice;
    private final List<IPosListener> mPosListeners = new CopyOnWriteArrayList<>();

    private long mPosConnectionTimeout;
    private long mNetworkConnectionTimeout;

    private PosTask mTask;

    public PosDriver(@NonNull final String mac) {
        if (TextUtils.isEmpty(mac)) {
            throw new IllegalArgumentException("Wrong Bluetooth device MAC address.");
        }

        mDevice = mac;

        mPosConnectionTimeout = DEFAULT_POS_CONNECTION_TIMEOUT;
        mNetworkConnectionTimeout = DEFAULT_NETWORK_CONNECTION_TIMEOUT;
    }

    /**
     * Метод для инициализации выполнения задачи драйвером.
     *
     * @param request SA пакет содержащий запрос согласно протоколу SA.
     * @see PosTask
     * @see SaPacket
     */
    public void process(@NonNull final SaPacket request) {
        if (inProcess()) {
            InpasLogger.info(TAG, "Task: " + String.valueOf(mTask) + " already executing. New task will be reject.");

            for (IPosListener listener : mPosListeners) {
                listener.onError(IPosListener.Error.PROCESS_ERROR, null);
            }
        } else {
            mTask = new PosTask(mDevice, request);
            mTask.setPosConnectionTimeout(mPosConnectionTimeout);
            mTask.setNetworkConnectionTimeout(mNetworkConnectionTimeout);

            for (IPosListener listener : mPosListeners) {
                mTask.addPosListener(listener);
            }

            try {
                EXECUTOR.execute(mTask);
            } catch (RejectedExecutionException ex) {
                InpasLogger.error(TAG, ex);
            }
        }
    }

    /**
     * Метод длоя завершения работы драйвера.
     */
    public void shutdown() {
        InpasLogger.info(TAG, "calling shutdown()");
        EXECUTOR.shutdown();

        try {
            if (!EXECUTOR.awaitTermination(500L, TimeUnit.MILLISECONDS)) {
                EXECUTOR.shutdownNow();
            }
        } catch (InterruptedException ex) {
            EXECUTOR.shutdownNow();
            InpasLogger.error(TAG, ex);
        }

        reset();
        mPosListeners.clear();
    }

    /**
     * Метод необходимый для подготовки драйвера к выполнению задачи.
     */
    public void reset() {
        mTask = null;
    }

    /**
     * Метод для проверки на начиние выполняющейся задачи.
     *
     * @return результат проверки.
     */
    public boolean inProcess() {
        return (mTask != null);
    }

    /**
     * Метод для добавления подписчика на событие {@link IPosListener}
     *
     * @param listener подписчик.
     */
    public void addPosListener(@NonNull final IPosListener listener) {
        mPosListeners.add(listener);
    }

    /**
     * Метод для удаления подписчика на событие {@link IPosListener}
     *
     * @param listener подписчик.
     */
    public void removePosListener(@NonNull final IPosListener listener) {
        mPosListeners.remove(listener);
    }

    /**
     * Метод возвращает текущее значение таймаута на соедиенение в миллисекундах.
     */
    public long getPosConnectionTimeout() {
        return mPosConnectionTimeout;
    }

    /**
     * Метод для установления таймаута на соедиенение.
     *
     * @param value значение таймаута в миллисекундах.
     */
    public void setPosConnectionTimeout(final long value) {
        mPosConnectionTimeout = value;
    }

    /**
     * Метод возвращает текущее значение таймаута на соедиенение в миллисекундах.
     */
    public long getNetworkConnectionTimeout() {
        return mNetworkConnectionTimeout;
    }

    /**
     * Метод для установления таймаута на соедиенение.
     *
     * @param value значение таймаута в миллисекундах.
     */
    public void setNetworkConnectionTimeout(final long value) {
        mNetworkConnectionTimeout = value;
    }

}