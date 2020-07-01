package ru.ppr.inpas.terminal;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import ru.ppr.inpas.lib.command.BaseCommand;
import ru.ppr.inpas.lib.command.financial.CancelCommand;
import ru.ppr.inpas.lib.command.financial.EmergencyCancellationCommand;
import ru.ppr.inpas.lib.command.financial.SaleCommand;
import ru.ppr.inpas.lib.command.nonfinancial.CloseSessionCommand;
import ru.ppr.inpas.lib.command.nonfinancial.TestHostCommand;
import ru.ppr.inpas.lib.command.nonfinancial.TestSelfCommand;
import ru.ppr.inpas.lib.command.nonfinancial.TransactionJournalCommand;
import ru.ppr.inpas.lib.core.PosDriver;
import ru.ppr.inpas.lib.internal.IPosListener;
import ru.ppr.inpas.lib.logger.InpasLogger;
import ru.ppr.inpas.lib.mapper.FinancialResultMapper;
import ru.ppr.inpas.lib.protocol.SaPacket;
import ru.ppr.inpas.lib.protocol.model.OperationCode;
import ru.ppr.inpas.lib.protocol.model.SaField;
import ru.ppr.ipos.IPos;
import ru.ppr.ipos.exception.PosException;
import ru.ppr.ipos.model.FinancialTransactionResult;
import ru.ppr.ipos.model.TransactionResult;

/**
 * Реализация интерфейса {@link ru.ppr.ipos.IPos}.
 * Все выполняемые транзакции должны содержать ID терминала. Если ID терминала отсутствует или указан не правильно,
 * то от POS терминала придет сообщение об ошибке с информацией о правильном ID терминала подключенного устройства.
 *
 * @see IPos
 */
public class InpasTerminal implements IPos {
    private static final String TAG = InpasLogger.makeTag(InpasTerminal.class);
    private static final long CONNECTION_TIMER_INTERVAL = 1000L;
    private static final String CONNECTION_TIMER_NAME = "Inpas Connection timer (terminal)";

    /**
     * Внутренние состояние терминала.
     */
    private enum TerminalState {
        ACTIVE, BUSY, TERMINATED
    }

    private final Object LOCK = new Object();
    private final ExecutorService PUBLISHER = Executors.newSingleThreadExecutor(r -> new Thread(r, "Publisher"));

    private final Settings mSettings;
    private final PosDriver mPosDriver;
    private final List<ConnectionListener> mConnectionListeners;

    private boolean mHasError;

    // В будущем: Удалить! Когда прошивка Inpas будет точно присылать статус транзакции.
    // Возникает ситуация, при которой ПТК считает, что результат выполнения операции неуспешен,
    // а PinPad считает, что все завершено успешно.
    private boolean mIsNeedToModifyCancelResult;

    private boolean mIsConnected;
    private long mConnectionTime;
    private Timer mConnectionTimer;

    private String mTerminalId;
    private TerminalState mTerminalState;

    private BaseCommand mTechnicalCancelCommand;
    private ResultListener<? super FinancialTransactionResult> mResultListener;

    private final IBluetoothManager mBluetoothManager;
    private final INetworkManager mNetworkManager;

    private CountDownLatch mCountDownLatch;

    private final IPosListener mPosListener = new IPosListener() {
        /**
         * Метод вызывается при успешном выполнении транзакции.
         *
         * @param packet набор данных согласно ответу для данной транзакции.
         * @see SaPacket
         */
        @Override
        public void onComplete(@NonNull final SaPacket packet) {
            synchronized (LOCK) {
                InpasLogger.info(TAG, "onComplete");

                mPosDriver.reset();
                FinancialTransactionResult result = FinancialResultMapper.from(packet);
                result = modifyResult(packet, result); // В будущем: Удалить! Когда прошивка Inpas будет точно присылать статус транзакции.

                mSettings.setLastTransactionApprovedStatus(result.isApproved());
                setTerminalState(TerminalState.ACTIVE);
                publish(mResultListener, result);
            }
        }

        /**
         * Метод вызывается при изменении состояния при взаимодействии с терминалом в ходе выполнения транзакции.
         *
         * @param state текущее состояние терминала.
         * @see ru.ppr.inpas.lib.internal.IPosListener.State
         */
        @Override
        public void onChanged(@NonNull final State state) {
            synchronized (LOCK) {
                InpasLogger.info(TAG, "onChanged");

                if (State.POS_CONNECTED == state) {
                    stopConnectionTimer();
                    mIsConnected = true;
                }

                PUBLISHER.submit(() -> {
                    for (ConnectionListener listener : mConnectionListeners) {
                        if (Thread.currentThread().isInterrupted()) {
                            break;
                        }

                        switch (state) {
                            case POS_CONNECTED:
                                listener.onConnected();
                                break;

                            case POS_DISCONNECTED:
                                listener.onDisconnected();
                                break;
                        }
                    }
                });
            }
        }

        /**
         * Метод вызывается при возникновении ошибки в ходе взаимодейтсвия с терминалом.
         *
         * @param error  конкретная ошибка.
         * @param packet при некторых ошибках может передаваться результат ошибки.
         * @see ru.ppr.inpas.lib.internal.IPosListener.Error
         * @see SaPacket
         */
        @Override
        public void onError(@NonNull final Error error, @Nullable final SaPacket packet) {
            stopConnectionTimer();

            synchronized (LOCK) {
                InpasLogger.info(TAG, "onError");
                mHasError = true;
                mPosDriver.reset();
                final FinancialTransactionResult result = (packet != null) ? FinancialResultMapper.from(packet) : null;
                setTerminalState(TerminalState.ACTIVE);

                switch (error) {
                    case POS_CONNECTION_TIMEOUT:
                    case NETWORK_CONNECTION_TIMEOUT: {
                        PUBLISHER.submit(() -> {
                            for (ConnectionListener listener : mConnectionListeners) {
                                if (Thread.currentThread().isInterrupted()) {
                                    break;
                                }

                                listener.onConnectionTimeout();
                            }
                        });
                    }
                    break;
                }

                publish(mResultListener, result);
            }
        }
    };

    /**
     * TODO: Удалить! Когда прошивка Inpas будет точно присылать статус транзакции.
     * Возникает ситуация, при которой ПТК считает, что результат выполнения операции неуспешен,
     * а PinPad считает, что все завершено успешно.В данной ситуации, согласно ответу от Inpas,
     * необходимо сравнивать 19 поле SA пакета (SAF_ADDITIONAL_RESPONSE_DATA) в присланном ответе.
     * При выполнении отмены/автоотмены в некоторых прошивках для PinPad
     * статус выполнения транзакции может быть равен 0. ПТК будет считать, что транзакция завершилась
     * неуспешно и попытается ее отменить. Данное поведение приведет к бесконечному циклу, который заблокирует
     * все операции продажи с использованием банковского терминала .
     *
     * @param packet исходные данные от PinPad.
     * @param result результат передаваемый подписчикам.
     * @return модифицированный результат передаваемый подписчикам.
     * @see ru.ppr.inpas.lib.protocol.model.TransactionStatus
     */
    @NonNull
    private FinancialTransactionResult modifyResult(@NonNull final SaPacket packet, @NonNull final FinancialTransactionResult result) {
        if (!result.isApproved() && mIsNeedToModifyCancelResult) {
            final String additionalResponseData = packet.getString(SaField.SAF_ADDITIONAL_RESPONSE_DATA);

            if (!TextUtils.isEmpty(additionalResponseData) && additionalResponseData.contains("ОДОБРЕНО. НЕТ ШАБЛОНА ЧЕКА")) {
                result.setApproved(true);
            }
        }

        mIsNeedToModifyCancelResult = false;

        return result;
    }

    /**
     * Наличие MAC-адреса обязательно.
     *
     * @param mac              MAC-адрес POS терминала.
     * @param bluetoothManager реализация для задейтсвования bluetooth.
     * @param networkManager   реализация для задействования сети.
     */
    public InpasTerminal(@NonNull final Context context,
                         @NonNull final String mac,
                         @NonNull final IBluetoothManager bluetoothManager,
                         @NonNull final INetworkManager networkManager) {
        mSettings = new Settings(context);
        mConnectionListeners = new CopyOnWriteArrayList<>();
        mBluetoothManager = bluetoothManager;
        mNetworkManager = networkManager;
        mPosDriver = new PosDriver(mac);
        mPosDriver.addPosListener(mPosListener);

        setTerminalState(TerminalState.ACTIVE);
        InpasLogger.setIgnoreLogTag(false);
    }

    /**
     * Метод для выполнения комманды.
     *
     * @param command        команда для выполнения.
     * @param resultListener подписчик на получение результата выполнения команды.
     */
    private void execute(@Nullable final BaseCommand command, @Nullable final ResultListener<? super FinancialTransactionResult> resultListener) {
        if (isReady()) {
            setTerminalState(TerminalState.BUSY);
            mHasError = false;

            if (command != null) {
                boolean canProcess = true;

                if ((command.getOperationCode() != OperationCode.TEST_HOST.getValue()) && !hasTerminalId()) {
                    InpasLogger.error(TAG, "No terminal id.");
                    canProcess = false;
                }

                if (canProcess) {
                    mResultListener = resultListener;
                    command.setTerminalId(mTerminalId);

                    if (command.getOperationCode() == OperationCode.SALE.getValue()) {
                        mTechnicalCancelCommand = command;

                        mSettings.setLastTransactionId(command.getTransactionNumber());
                        mSettings.setLastTransactionApprovedStatus(false);
                    }

                    try {
                        if (!mIsConnected) {
                            startConnectionTimer();
                        }

                        mPosDriver.process(command.getPacket());
                    } catch (Exception ex) {
                        InpasLogger.error(TAG, ex);
                        publish(mResultListener, null);
                        setTerminalState(TerminalState.ACTIVE);
                    }
                } else {
                    InpasLogger.error(TAG, "Command will not be execute.");
                    publish(resultListener, null);
                    setTerminalState(TerminalState.ACTIVE);
                }
            } else {
                InpasLogger.error(TAG, "No command to execute.");
                publish(resultListener, null);
                setTerminalState(TerminalState.ACTIVE);
            }
        } else {
            InpasLogger.error(TAG, "Command will not be execute, terminal state: " + String.valueOf(mTerminalState));
            publish(resultListener, null);
        }
    }

    @Override
    public void prepareResources() throws PosException {
        InpasLogger.info(TAG, "Calling prepareResources()");

        if (isReady()) {
            mBluetoothManager.enable();
            mNetworkManager.enable();

            if (!mBluetoothManager.isEnabled()) {
                throw new PosException("Could not enable bluetooth.");
            }

            if (!mNetworkManager.isEnabled()) {
                throw new PosException("Could not enable network.");
            }

            if (!hasTerminalId()) {
                requestTerminalId();
            }
        } else {
            throw new IllegalStateException("Wrong terminal state! State = " + String.valueOf(mTerminalState));
        }
    }

    @Override
    public void freeResources() throws PosException {
        InpasLogger.info(TAG, "Calling freeResources() start");

        if (isReady()) {
            InpasLogger.info(TAG, "Try to free resources...");

            mBluetoothManager.disable();
            mNetworkManager.disable();
            mResultListener = null;
            mIsConnected = false;
            mHasError = false;
        } else {
            throw new IllegalStateException("Wrong terminal state! State = " + String.valueOf(mTerminalState));
        }

        InpasLogger.info(TAG, "Calling freeResources() finish");
    }

    @Override
    public void terminate() {
        InpasLogger.info(TAG, "Calling terminate()");

        mResultListener = null;

        mPosDriver.shutdown();
        mPosDriver.removePosListener(mPosListener);
        mConnectionListeners.clear();

        PUBLISHER.shutdown();

        try {
            if (!PUBLISHER.awaitTermination(500L, TimeUnit.MILLISECONDS)) {
                PUBLISHER.shutdownNow();
            }
        } catch (InterruptedException ex) {
            PUBLISHER.shutdownNow();
        }

        setTerminalState(TerminalState.TERMINATED);
    }

    @Override
    public void setConnectionTimeout(long connectionTimeout) {
        if (isReady()) {
            mPosDriver.setPosConnectionTimeout(connectionTimeout);
            mPosDriver.setNetworkConnectionTimeout(connectionTimeout);
        } else {
            throw new IllegalStateException("Wrong terminal state! State = " + String.valueOf(mTerminalState));
        }
    }

    @Override
    public long getConnectionTimeout() {
        return mPosDriver.getPosConnectionTimeout();
    }

    @Override
    public boolean isReady() {
        synchronized (LOCK) {
            InpasLogger.info(TAG, "isReady(), Terminal State: " + String.valueOf(mTerminalState) + ", Driver in process: " + String.valueOf(mPosDriver.inProcess()));
            return (TerminalState.ACTIVE == mTerminalState) && !mPosDriver.inProcess();
        }
    }

    @Override
    public void addConnectionListener(@NonNull final ConnectionListener connectionListener) {
        mConnectionListeners.add(connectionListener);
    }

    @Override
    public void removeConnectionListener(@NonNull final ConnectionListener connectionListener) {
        mConnectionListeners.remove(connectionListener);
    }

    @Override
    public void testHost(ResultListener<TransactionResult> resultListener) {
        InpasLogger.info(TAG, "Calling testHost()");

        if (isReady()) {
            execute(new TestHostCommand(), resultListener);
        } else {
            throw new IllegalStateException("Wrong terminal state! State = " + String.valueOf(mTerminalState));
        }
    }

    @Override
    public void testSelf(ResultListener<TransactionResult> resultListener) {
        InpasLogger.info(TAG, "Calling testSelf()");

        if (isReady()) {
            setTerminalState(TerminalState.BUSY);

            mResultListener = result -> {
                if (resultListener != null) {
                    resultListener.onResult(result);
                }

                mCountDownLatch.countDown();
            };

            try {
                startConnectionTimer();
                mPosDriver.process(new TestSelfCommand().getPacket());
                mCountDownLatch = new CountDownLatch(1);
                mCountDownLatch.await();
            } catch (InterruptedException ex) {
                InpasLogger.error(TAG, ex);
            } finally {
                setTerminalState(TerminalState.ACTIVE);
            }
        } else {
            throw new IllegalStateException("Wrong terminal state! State = " + String.valueOf(mTerminalState));
        }
    }

    @Override
    public void invokeApplicationMenu(ResultListener<TransactionResult> resultListener) {
        InpasLogger.info(TAG, "Calling invokeApplicationMenu()");

        if (isReady()) {
            execute(null, resultListener);
        } else {
            throw new IllegalStateException("Wrong terminal state! State = " + String.valueOf(mTerminalState));
        }
    }

    @Override
    public void updateSoftware(ResultListener<TransactionResult> resultListener) {
        InpasLogger.info(TAG, "Calling updateSoftware()");

        if (isReady()) {
            execute(null, resultListener);
        } else {
            throw new IllegalStateException("Wrong terminal state! State = " + String.valueOf(mTerminalState));
        }
    }

    /**
     * В Inpas не предусмотрена данная команда.
     */
    @Override
    public void openSession(ResultListener<TransactionResult> resultListener) {
        InpasLogger.info(TAG, "Calling openSession()");

        if (isReady()) {
            testHost(resultListener);
        } else {
            throw new IllegalStateException("Wrong terminal state! State = " + String.valueOf(mTerminalState));
        }
    }

    @Override
    public void closeSession(ResultListener<TransactionResult> resultListener) {
        InpasLogger.info(TAG, "Calling closeSession()");

        if (isReady()) {
            execute(new CloseSessionCommand(0), resultListener); // В будущем: Check transaction number.
        } else {
            throw new IllegalStateException("Wrong terminal state! State = " + String.valueOf(mTerminalState));
        }
    }

    @Override
    public void getTransactionsJournal(ResultListener<TransactionResult> resultListener) {
        InpasLogger.info(TAG, "Calling getTransactionsJournal()");

        if (isReady()) {
            execute(new TransactionJournalCommand(), resultListener);
        } else {
            throw new IllegalStateException("Wrong terminal state! State = " + String.valueOf(mTerminalState));
        }
    }

    @Override
    public void getTransactionsTotal(ResultListener<TransactionResult> resultListener) {
        InpasLogger.info(TAG, "Calling getTransactionsTotal()");

        if (isReady()) {
            execute(null, resultListener);
        } else {
            throw new IllegalStateException("Wrong terminal state! State = " + String.valueOf(mTerminalState));
        }
    }

    @Override
    public void sale(int price, int saleTransactionId, ResultListener<FinancialTransactionResult> resultListener) {
        InpasLogger.info(TAG, "Calling sale()");

        if (isReady()) {
            execute(new SaleCommand(price, saleTransactionId), resultListener);
        } else {
            throw new IllegalStateException("Wrong terminal state! State = " + String.valueOf(mTerminalState));
        }
    }

    @Override
    public void cancel(FinancialTransactionResult saleResult, ResultListener<FinancialTransactionResult> resultListener) {
        InpasLogger.info(TAG, "Calling cancel()");

        if (isReady()) {
            final long lastTransactionId = mSettings.getLastTransactionId();

            if (lastTransactionId != saleResult.getId()) {
                if (saleResult.isApproved()) {
                    execute(new CancelCommand(saleResult.getAmount(), saleResult.getId(),
                                    OperationCode.SALE.getValue(),
                                    saleResult.getAuthorizationId(), saleResult.getRRN()
                            ),
                            resultListener
                    );
                } else {
                    publish(resultListener, null);
                }
            } else {
                if (saleResult.isApproved()) {
                    mTechnicalCancelCommand = new SaleCommand(saleResult.getAmount(), saleResult.getId());
                    technicalCancel(resultListener);
                } else {
                    if (mSettings.getLastTransactionApprovedStatus()) {
                        mTechnicalCancelCommand = new SaleCommand(saleResult.getAmount(), saleResult.getId());
                        technicalCancel(resultListener);
                    } else {
                        mIsNeedToModifyCancelResult = true;
                        execute(new TestHostCommand(), resultListener);
                    }
                }
            }
        } else {
            throw new IllegalStateException("Wrong terminal state! State = " + String.valueOf(mTerminalState));
        }
    }

    @Override
    public void technicalCancel(ResultListener<FinancialTransactionResult> resultListener) {
        InpasLogger.info(TAG, "Calling technicalCancel()");

        if (isReady()) {
            if ((mTechnicalCancelCommand != null) && mSettings.getLastTransactionApprovedStatus()) {
                execute(new EmergencyCancellationCommand(mTechnicalCancelCommand), resultListener);
            } else {
                execute(null, resultListener);
            }
        } else {
            throw new IllegalStateException("Wrong terminal state! State = " + String.valueOf(mTerminalState));
        }
    }

    /**
     * Метод для получения ID Pos терминала.
     * Необходимо вызывать, если ID Pos терминала не был получен или некорректен.
     */
    private void requestTerminalId() {
        InpasLogger.info(TAG, "Calling requestTerminalId()");
        setTerminalState(TerminalState.BUSY);

        mResultListener = result -> {
            if (result != null) {
                setTerminalId(result.getTerminalId());
            }

            mCountDownLatch.countDown();
        };

        try {
            startConnectionTimer();
            mPosDriver.process(new TestHostCommand().getPacket());
            mCountDownLatch = new CountDownLatch(1);
            mCountDownLatch.await();
        } catch (InterruptedException ex) {
            InpasLogger.error(TAG, ex);
        } finally {
            setTerminalState(TerminalState.ACTIVE);
        }
    }

    /**
     * Внутренний метод для уведомления подписчиков о возвращаемом результате выполнения транзакции.
     *
     * @param listener подписчик на получение результата.
     * @param result   результат выполнения транзакции.
     */
    private void publish(@Nullable final ResultListener<? super FinancialTransactionResult> listener,
                         @Nullable final FinancialTransactionResult result) {
        PUBLISHER.submit(() -> {
            if (listener != null) {
                InpasLogger.info(TAG, "Send result to: " + listener.toString());
                listener.onResult(result);
            } else {
                InpasLogger.info(TAG, "No result listener.");
            }
        });
    }

    /**
     * Метод используемый для установки внутреннего состояния терминала.
     *
     * @param state желаемое состояние.
     * @see TerminalState
     */
    private void setTerminalState(@NonNull final TerminalState state) {
        synchronized (LOCK) {
            InpasLogger.info(TAG, "Set terminal state: " + String.valueOf(state));
            mTerminalState = state;
        }
    }

    /**
     * Метод для остановки таймера для установления соединения с POS терминалом.
     */
    private void stopConnectionTimer() {
        synchronized (LOCK) {
            if (mConnectionTimer != null) {
                mConnectionTimer.cancel();
                mConnectionTimer = null;

                InpasLogger.info(TAG, CONNECTION_TIMER_NAME + ": stop.");
            }

            mConnectionTime = 0L;
        }
    }

    /**
     * Метод для запуска таймера установления соединения с POS терминалом.
     */
    private void startConnectionTimer() {
        synchronized (LOCK) {
            InpasLogger.info(TAG, CONNECTION_TIMER_NAME + ": start.");
            mConnectionTime = 0L;

            mConnectionTimer = new Timer(CONNECTION_TIMER_NAME);
            mConnectionTimer.scheduleAtFixedRate(new TimerTask() {
                                                     @Override
                                                     public void run() {
                                                         synchronized (LOCK) {
                                                             if (!mIsConnected && !mHasError) {
                                                                 InpasLogger.info(TAG, CONNECTION_TIMER_NAME + ": tick.");
                                                                 mConnectionTime += CONNECTION_TIMER_INTERVAL;

                                                                 if (getConnectionTimeout() < mConnectionTime) {
                                                                     PUBLISHER.submit(() -> {
                                                                                 if (!Thread.currentThread().isInterrupted()) {
                                                                                     mPosListener.onError(IPosListener.Error.POS_CONNECTION_TIMEOUT, null);
                                                                                 }
                                                                             }
                                                                     );
                                                                 } else {
                                                                     final long tick = getConnectionTimeout() - mConnectionTime;

                                                                     PUBLISHER.submit(() -> {
                                                                                 for (ConnectionListener listener : mConnectionListeners) {
                                                                                     if (Thread.currentThread().isInterrupted()) {
                                                                                         break;
                                                                                     }

                                                                                     listener.onTickBeforeTimeout(tick);
                                                                                 }
                                                                             }
                                                                     );
                                                                 }
                                                             }
                                                         }
                                                     }
                                                 },
                    0L, CONNECTION_TIMER_INTERVAL
            );
        }
    }

    /**
     * Метод использующийся для установления наличия ID POS терминала.
     *
     * @return результат наличия ID POS терминала.
     */
    private boolean hasTerminalId() {
        boolean result = !TextUtils.isEmpty(mTerminalId);
        InpasLogger.info(TAG, "hasTerminalId = " + String.valueOf(result));

        return result;
    }

    /**
     * Метод необходимый для установления ID POS терминала.
     *
     * @param terminalId ID POS терминала
     */
    private void setTerminalId(@NonNull final String terminalId) {
        if (TextUtils.isEmpty(terminalId)) {
            InpasLogger.error(TAG, "Incorrect terminal id value.");
        } else {
            mTerminalId = terminalId;
            InpasLogger.info(TAG, "Set terminal id: " + terminalId);
        }
    }

    /**
     * Интерфейс для инициализации взаимодействия по Bluetooth.
     */
    public interface IBluetoothManager {
        void enable();

        void disable();

        boolean isEnabled();
    }

    /**
     * Интерфейс для инициализации взаимодействия по сети.
     */
    public interface INetworkManager {
        void enable();

        void disable();

        boolean isEnabled();
    }

}