package ru.ppr.inpas.lib.core;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.LinkedList;
import java.util.List;

import ru.ppr.inpas.lib.internal.IPosListener;
import ru.ppr.inpas.lib.internal.NetworkCommunicator;
import ru.ppr.inpas.lib.internal.PosCommunicator;
import ru.ppr.inpas.lib.internal.PosContext;
import ru.ppr.inpas.lib.logger.InpasLogger;
import ru.ppr.inpas.lib.protocol.SaPacket;
import ru.ppr.inpas.lib.protocol.model.SaField;
import ru.ppr.inpas.lib.protocol.model.TransactionStatus;
import ru.ppr.inpas.lib.state.ErrorState;
import ru.ppr.inpas.lib.state.IPosState;
import ru.ppr.inpas.lib.state.StateManager;

/**
 * Класс представляющий сущность задачи которая выполняется на POS терминале.
 */
public class PosTask implements Runnable, IPosListener {
    private static final String TAG = InpasLogger.makeTag(PosTask.class);

    private final PosCommunicator mPosCommunicator;
    private final NetworkCommunicator mNetworkCommunicator;
    private final PosContext mPosContext;
    private final List<IPosListener> mPosListeners;

    private Error mPosError;
    private boolean hasError;
    private final SaPacket mRequest;

    public PosTask(@NonNull final String mac, @NonNull final SaPacket request) {
        mPosCommunicator = new PosCommunicator(mac);
        mPosCommunicator.addPosListener(this);
        mNetworkCommunicator = new NetworkCommunicator();
        mNetworkCommunicator.addIPosListener(this);
        mPosContext = new PosContext(mPosCommunicator, mNetworkCommunicator);

        mPosListeners = new LinkedList<>();

        mRequest = request;
    }

    @Override
    public void run() {
        SaPacket result = null;
        InpasLogger.info(TAG, "Begin processing.");
        boolean isInterrupted = Thread.currentThread().isInterrupted();
        final long startTime = System.currentTimeMillis();

        if (SaPacket.isValid(mRequest)) {
            final Integer requestCode = mRequest.getInteger(SaField.SAF_OPERATION_CODE);

            if (requestCode != null) {
                mPosCommunicator.connect();

                if (mPosCommunicator.isConnected()) {
                    mPosContext.add(mRequest);

                    final StateManager stateManager = mPosContext.getStateManager();
                    stateManager.setNextState(IPosState.PosState.SEND);

                    IPosState.PosState nextState = stateManager.getNextState();

                    while (!hasError && (nextState != IPosState.PosState.DONE) && !isInterrupted) {
                        final IPosState mCurrentState = stateManager.getNext();
                        mCurrentState.doAction();
                        nextState = stateManager.getNextState();
                        isInterrupted = Thread.currentThread().isInterrupted();
                    }
                }
            } else {
                onError(Error.SA_PACKET_ERROR, null);
            }
        } else {
            onError(Error.SA_PACKET_ERROR, null);
        }

        if (isInterrupted) {
            InpasLogger.info(TAG, "Processing is interrupted.");
        } else {
            if (hasError) {
                handleError(mPosError, true);
            } else {
                result = mPosContext.getResult();

                if (result != null) {
                    if (isSuccessfulResult(result)) {
                        onComplete(result);
                    } else {
                        handleError(Error.ERROR, result);
                    }
                } else {
                    handleError(Error.RESULT_ERROR, false);
                }
            }

            mPosCommunicator.delay(100L);
            mPosCommunicator.disconnect();

            mNetworkCommunicator.removePosListener(this);
            mPosCommunicator.removePosListener(this);

            mPosListeners.clear();

            InpasLogger.info(TAG, "End processing, elapsed time: " + String.valueOf(System.currentTimeMillis() - startTime) + " ms.");
        }
    }

    @Override
    public void onComplete(@NonNull final SaPacket packet) {
        for (IPosListener listener : mPosListeners) {
            listener.onComplete(packet);
        }
    }

    @Override
    public void onChanged(@NonNull final State state) {
        for (IPosListener listener : mPosListeners) {
            listener.onChanged(state);
        }
    }

    @Override
    public void onError(@NonNull final Error error, @Nullable final SaPacket packet) {
        hasError = true;
        mPosError = error;

        if (Error.NETWORK_CONNECTION_ERROR == error) {
            hasError = false;
            mPosError = Error.ERROR;
        }

        InpasLogger.error(TAG, String.valueOf(error));
    }

    /**
     * Метод для проверки результата выполнения задачи.
     *
     * @param packet результата выполнения задачи.
     * @return результата проверки.
     */
    private boolean isSuccessfulResult(@Nullable final SaPacket packet) {
        boolean result = (packet != null);

        if (result) {
            final Integer value = packet.getInteger(SaField.SAF_TRX_STATUS);

            if (value != null) {
                final TransactionStatus status = TransactionStatus.from(value);
                result = (TransactionStatus.APPROVED == status);
            }
        }

        return result;
    }

    /**
     * Метод для обработки ошибки.
     *
     * @param error      тип ошибки.
     * @param withAction признак выполнения действий.
     */
    private void handleError(@NonNull final Error error, final boolean withAction) {
        if (withAction) {
            final IPosState state = new ErrorState(mPosContext);
            state.doAction();
        }

        final SaPacket packet = mPosContext.getResult();

        for (IPosListener listener : mPosListeners) {
            listener.onError(error, packet);
        }
    }

    /**
     * Метод для обработки ошибки.
     *
     * @param error  тип ошибки.
     * @param packet отсылаемый пакет в контексте ошибки.
     */
    private void handleError(@NonNull final Error error, final SaPacket packet) {
        for (IPosListener listener : mPosListeners) {
            listener.onError(error, packet);
        }
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
        return mPosCommunicator.getConnectionTimeout();
    }

    /**
     * Метод для установления таймаута на соедиенение.
     *
     * @param value значение таймаута в миллисекундах.
     */
    public void setPosConnectionTimeout(final long value) {
        mPosCommunicator.setConnectionTimeout(value);
    }

    /**
     * Метод возвращает текущее значение таймаута на соедиенение в миллисекундах.
     */
    public long getNetworkConnectionTimeout() {
        return mNetworkCommunicator.getConnectionTimeout();
    }

    /**
     * Метод для установления таймаута на соедиенение.
     *
     * @param value значение таймаута в миллисекундах.
     */
    public void setNetworkConnectionTimeout(final long value) {
        mNetworkCommunicator.setConnectionTimeout(value);
    }

}
