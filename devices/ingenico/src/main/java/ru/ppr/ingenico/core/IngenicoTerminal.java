package ru.ppr.ingenico.core;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;

import com.ingenico.pclservice.PclService;
import com.ingenico.pclutilities.PclUtilities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import ru.ppr.ingenico.model.operations.Operation;
import ru.ppr.ingenico.model.operations.OperationCancel;
import ru.ppr.ingenico.model.operations.OperationCloseSession;
import ru.ppr.ingenico.model.operations.OperationGetTransactionsJournal;
import ru.ppr.ingenico.model.operations.OperationGetTransactionsTotal;
import ru.ppr.ingenico.model.operations.OperationInvokeAdministrativeMenu;
import ru.ppr.ingenico.model.operations.OperationOpenSession;
import ru.ppr.ingenico.model.operations.OperationSale;
import ru.ppr.ingenico.model.operations.OperationSyncWithTMS;
import ru.ppr.ingenico.model.operations.OperationTechnicalCancel;
import ru.ppr.ingenico.model.operations.OperationTestHost;
import ru.ppr.ingenico.model.operations.OperationTestTerminal;
import ru.ppr.ipos.IPos;
import ru.ppr.ipos.exception.PosException;
import ru.ppr.ipos.model.FinancialTransactionResult;
import ru.ppr.ipos.model.TransactionResult;
import ru.ppr.logger.Logger;
import ru.ppr.logger.LoggerAspect;
import ru.ppr.utils.Executors;

/**
 * Ingenico реализация {@link IPos}.
 *
 * @author Aleksandr Brazhkin
 */
@LoggerAspect.IncludeClass
public class IngenicoTerminal implements IPos {

    private static final String TAG = Logger.makeLogTag(IngenicoTerminal.class);

    /**
     * Наименование файла хранилища по умолчанию
     */
    public static final String DEFAULT_PREFS_FILE_NAME = "IngenicoPrefs";
    /**
     * Наименование файла конфига для прослойки по умолчанию
     */
    public static final String DEFAULT_CONFIG_FILE_NAME = "IngenicoPclConfig.txt";
    /**
     * Интревал тика при подключении, мс
     */
    private static final long TICK_INTERVAL = 1000;
    /**
     * Мнимое время подключения при выполнении фейковых транзакций, мс
     */
    private static final long CONNECTION_TIME_FOR_FAKE_TRANSACTION = 2000;


    private static final String BROADCAST_CONNECTION_STATE_ACTION = "com.ingenico.pclservice.intent.action.STATE_CHANGED";
    private static final String BROADCAST_CONNECTION_STATE = "state";
    private static final String BROADCAST_CONNECTION_STATE_CONNECTED = "CONNECTED";
    private static final String BROADCAST_CONNECTION_STATE_DISCONNECTED = "DISCONNECTED";

    private static final String EXTRA_KEY_PACKAGE_NAME = "PACKAGE_NAME";
    private static final String EXTRA_KEY_FILE_NAME = "FILE_NAME";

    private final Context mContext;
    /**
     * {@link Executor}  для запуска операций в отдельном потоке.
     */
    private final ScheduledExecutorService mScheduledExecutorService;
    /**
     * {@link Executor}  для запуска операций в отдельном потоке.
     */
    private final ExecutorService mExecutorService;
    /**
     * Менеджер для включения/выключения Bluetooth
     */
    private final BluetoothManager bluetoothManager;
    /**
     * Менеджер для включения/выключения интернета
     */
    private final InternetManager internetManager;
    /**
     * Класс от PCL для установки mac-адреса
     */
    private final PclUtilities pclUtilities;
    /**
     * Исполнитель операций по протоколу Arcus2
     */
    private final Arcus2Server arcus2Server;
    /**
     * Сервис от PCL, выполняющий какую-то работу
     */
    private LoggablePclService pclService;
    /**
     * Подключение к сервису PCL
     */
    private ServiceConnection pclConnection;
    /**
     * Ресивер для получения сведений о готовности PCL сервиса
     */
    private BroadcastReceiver pclReceiver;
    /**
     * Обретка над колбеком, переданным в выполняемую операцию
     */
    private MethodCallback methodCallback;
    /**
     * MAC-адрес устройства
     */
    private final String macAddress;
    /**
     * Порт для {@link Arcus2Server}
     */
    private final int port;
    /**
     * Флаг, что MAC адрес установлен
     */
    private boolean companionActivated;
    /**
     * Флаг, что сработал {@link Arcus2Server.Callback#onConnected()}
     */
    private boolean connected;
    /**
     * Флаг, что операция прервана
     */
    private boolean interrupted;
    /**
     * Флаг, что операция ещё выполняется
     */
    private volatile boolean running;
    /**
     * {@link Future} для прерывания операции по таймауту
     */
    private Future operationFuture;
    /**
     * Таймаут подключения к терминалу
     */
    private long connectionTimeout;
    /**
     * Оставшееся время до выброса {@link ConnectionListener#onConnectionTimeout()}
     */
    private long currentTickValue;
    /**
     * Объект для синхронизации выполнения кода
     */
    private final Object lock = new Object();
    /**
     * {@link Future} для прерывания операции подключения по таймауту
     */
    private Future connectionTimeoutFuture;
    /**
     * Хранилище необходимых данных
     */
    private final SharedPrefs mSharedPrefs;
    /**
     * Имя пакета для прослойки PCL
     */
    private final String packageName;
    /**
     * Файл конфига для прослойки PCL
     */
    private final String configFileName;
    /**
     * Слушатели состояния подключения
     */
    private List<ConnectionListener> connectionListeners = new ArrayList<>();

    public IngenicoTerminal(Context context,
                            BluetoothManager bluetoothManager,
                            InternetManager internetManager,
                            String sharedPrefsFileName,
                            String packageName,
                            String configFileName,
                            String macAddress,
                            int port,
                            int connectionTimeout) {
        this.mContext = context.getApplicationContext();
        this.mSharedPrefs = new SharedPrefs(mContext, sharedPrefsFileName);
        this.configFileName = configFileName;
        this.packageName = packageName;
        this.bluetoothManager = bluetoothManager;
        this.internetManager = internetManager;
        pclUtilities = new PclUtilities(context, packageName, configFileName);
        this.macAddress = macAddress;
        this.port = port;
        this.connectionTimeout = connectionTimeout;
        this.mExecutorService = Executors.newLoggableSingleThreadExecutor(TAG, r -> new Thread(r, "IngenicoExecutor"));
        this.mScheduledExecutorService = Executors.newLoggableSingleThreadScheduledExecutor(TAG, r -> new Thread(r, "IngenicoScheduledExecutor"));
        this.arcus2Server = new Arcus2Server(port, mScheduledExecutorService);
    }

    /**
     * Запускает выполнение операции
     *
     * @param task Операция
     */
    private void doTransactionOnExecutor(Runnable task) {
        synchronized (lock) {
            if (running) {
                throw new IllegalStateException("Operation is already running");
            }
            running = true;
            connected = false;
            interrupted = false;
            operationFuture = mExecutorService.submit(task);
        }
    }

    /**
     * Запускает выполнение операции
     *
     * @param operation      Операция
     * @param resultListener Колбек
     * @param <O>            Тип операции
     * @param <R>            Тип результата
     */
    private <O extends Operation, R extends TransactionResult> void doTransactionOnExecutor(final O operation, final ResultListener<R> resultListener) {
        doTransactionOnExecutor(() -> doTransaction(operation, resultListener));
    }

    /**
     * Запускает выполнение операции
     *
     * @param operation      Операция
     * @param resultListener Колбек
     * @param <O>            Тип операции
     * @param <R>            Тип результата
     */
    private <O extends Operation, R extends TransactionResult> void doTransaction(final O operation, final ResultListener<R> resultListener) {

        Logger.trace(TAG, "doTransaction start, operation = " + operation.getClass().getSimpleName());

        startConnectionTimeoutTimer();

        methodCallback = () -> mExecutorService.execute(() -> {
            Logger.trace(TAG, "resultListener.onResult");
            resultListener.onResult(null);
        });

        mScheduledExecutorService.execute(() -> {
            if (!companionActivated) {
                int res = pclUtilities.ActivateCompanion(macAddress);
                if (res == 0) {
                    Logger.trace(TAG, "companion activated successfully");
                    companionActivated = true;
                } else {
                    synchronized (lock) {
                        Logger.trace(TAG, "could not activate companion");
                        stopTransaction();
                        running = false;
                    }
                    mExecutorService.execute(() -> onDisconnected());
                    methodCallback.sendResultOnError();
                    methodCallback = null;
                    return;
                }
            } else {
                Logger.trace(TAG, "companion already activated");
            }

            pclConnection = new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName name, IBinder binder) {
                    mScheduledExecutorService.execute(() -> {
                        try {
                            Logger.trace(TAG, "pclConnection.onServiceConnected");
                            PclService.LocalBinder localBinder = (PclService.LocalBinder) binder;
                            pclService = (LoggablePclService) localBinder.getService();
                            pclService.addDynamicBridge(port, 1);
                        } catch (Exception e) {
                            Logger.error(TAG, e);
                            throw e;
                        }
                    });
                }

                @Override
                public void onServiceDisconnected(ComponentName name) {
                    Logger.trace(TAG, "pclConnection.onServiceDisconnected");
                    mScheduledExecutorService.execute(() -> stopTransaction());
                }
            };

            pclReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    Logger.trace(TAG, "pclReceiver.onReceive, outer");
                    mScheduledExecutorService.execute(() -> {
                        String state = intent.getStringExtra(BROADCAST_CONNECTION_STATE);

                        Logger.trace(TAG, "pclReceiver.onReceive, state = " + state);

                        if (BROADCAST_CONNECTION_STATE_CONNECTED.equals(state)) {
                            if (interrupted) {
                                Logger.trace(TAG, "pclReceiver.onReceive, state = " + state + " interrupted");
                                // Вызван stopTransaction().
                                // Не нужно запускать операцию на Arcus2Server'е
                                return;
                            }

                            Logger.trace(TAG, "arcus2Server.run");

                            arcus2Server.run(operation, new Arcus2Server.Callback<R>() {
                                @Override
                                public void onConnected() {
                                    Logger.trace(TAG, "arcus2Server.onConnected");
                                    mScheduledExecutorService.execute(() -> {
                                        if (interrupted) {
                                            Logger.trace(TAG, "arcus2Server.onConnected interrupted");
                                            // Вызван stopTransaction().
                                            // Клиент уже точно получит onResult(null)
                                            // Arcus2Server сейчас будет остановлен.
                                            // Уже поздно сообщать, что подключение выполнено
                                            return;
                                        }
                                        connected = true;
                                        if (connectionTimeoutFuture != null) {
                                            connectionTimeoutFuture.cancel(false);
                                            connectionTimeoutFuture = null;
                                        } else {
                                            Logger.error(TAG, "Illegal state: connectionTimeoutFuture is null in onConnected()");
                                        }

                                        mExecutorService.execute(() -> IngenicoTerminal.this.onConnected());
                                    });
                                }

                                @Override
                                public void onResult(R result) {
                                    Logger.trace(TAG, "arcus2Server.onResult, result = " + result);
                                    mScheduledExecutorService.execute(() -> {
                                        if (connected) {
                                            Logger.trace(TAG, "arcus2Server.onResult, connected");
                                            synchronized (lock) {
                                                stopTransaction();
                                                running = false;
                                            }
                                            mExecutorService.execute(() -> onDisconnected());
                                            Logger.trace(TAG, "resultListener.onResult");
                                            mExecutorService.execute(() -> resultListener.onResult(result));
                                            methodCallback = null;
                                        } else {
                                            Logger.error(TAG, "Illegal state: connected is false in onResult()");
                                        }
                                    });
                                }

                                @Override
                                public void onError() {
                                    Logger.trace(TAG, "arcus2Server.onError");
                                    mScheduledExecutorService.execute(() -> {
                                        if (connected) {
                                            synchronized (lock) {
                                                Logger.trace(TAG, "arcus2Server.onError, connected");
                                                stopTransaction();
                                                running = false;
                                            }
                                            mExecutorService.execute(() -> onDisconnected());
                                            Logger.trace(TAG, "resultListener.onResult");
                                            mExecutorService.execute(() -> resultListener.onResult(null));
                                            methodCallback = null;
                                        } else {
                                            stopTransaction();
                                        }
                                    });
                                }
                            });
                        } else if (BROADCAST_CONNECTION_STATE_DISCONNECTED.equals(state)) {
                            stopTransaction();
                        }
                    });
                }
            };

            Intent intent = new Intent(mContext, LoggablePclService.class)
                    .putExtra(EXTRA_KEY_PACKAGE_NAME, packageName)
                    .putExtra(EXTRA_KEY_FILE_NAME, configFileName);

            mContext.registerReceiver(pclReceiver, new IntentFilter(BROADCAST_CONNECTION_STATE_ACTION));
            mContext.bindService(intent, pclConnection, Context.BIND_AUTO_CREATE);
        });

        Logger.trace(TAG, "doTransaction end, operation = " + operation.getClass().getSimpleName());
    }

    /**
     * Запускает таймер для прерывания подключения по таймауту
     */
    private void startConnectionTimeoutTimer() {
        Logger.trace(TAG, "startConnectionTimeoutTimer");
        currentTickValue = connectionTimeout;

        connectionTimeoutFuture = mScheduledExecutorService.scheduleAtFixedRate(() -> {

            Logger.trace(TAG, "connectionTimeoutFuture tick, currentTickValue = " + currentTickValue);

            if (connectionTimeoutFuture == null) {
                Logger.trace(TAG, "timer canceled, return");
                // Таймер был отменен
                return;
            }
            if (currentTickValue > 0) {
                long localCurrentTickValue = currentTickValue;
                mExecutorService.execute(() -> onTickBeforeTimeout(localCurrentTickValue));
                currentTickValue -= TICK_INTERVAL;
            } else {
                stopTransaction();
                mExecutorService.execute(() -> onConnectionTimeout());
            }
        }, 0, TICK_INTERVAL, TimeUnit.MILLISECONDS);
    }

    /**
     * Выполняет отмену транзакции с фейковым идентификатором
     *
     * @param saleResult     Информация о продаже
     * @param resultListener Колбек
     */
    private void doFakeCancelOnExecutor(FinancialTransactionResult saleResult, ResultListener<FinancialTransactionResult> resultListener) {
        doTransactionOnExecutor(() -> mScheduledExecutorService.execute(() -> {
            Logger.trace(TAG, "doFakeCancel started");
            currentTickValue = connectionTimeout;
            // Потикаем для того, чтоб пользователь успел посмотреть на таймер
            // Используем такой подход со слов Александра Корчака
            long minTickValue = currentTickValue - CONNECTION_TIME_FOR_FAKE_TRANSACTION - 1;
            while (currentTickValue > minTickValue) {
                long localCurrentTickValue = currentTickValue;
                mExecutorService.execute(() -> onTickBeforeTimeout(localCurrentTickValue));
                try {
                    Thread.sleep(TICK_INTERVAL);
                    currentTickValue -= TICK_INTERVAL;
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    synchronized (lock) {
                        Logger.trace(TAG, "doFakeCancel stopTransaction timer interrupted");
                        stopTransaction();
                        running = false;
                        mExecutorService.execute(() -> resultListener.onResult(null));
                    }
                    return;
                }
            }
            connected = true;
            mExecutorService.execute(() -> onConnected());
            synchronized (lock) {
                Logger.trace(TAG, "doFakeCancel stopTransaction");
                stopTransaction();
                running = false;
            }
            mExecutorService.execute(() -> onDisconnected());

            // Фейковый результат финансовой транзакции
            FinancialTransactionResult financialTransactionResult = new FinancialTransactionResult();
            financialTransactionResult.setId(saleResult.getId());
            financialTransactionResult.setTimeStamp(new Date());
            financialTransactionResult.setApproved(true);
            financialTransactionResult.setAmount(saleResult.getAmount());
            financialTransactionResult.setCurrencyCode(saleResult.getCurrencyCode());
            financialTransactionResult.setReceipt(Collections.emptyList());

            mExecutorService.execute(() -> resultListener.onResult(financialTransactionResult));
            Logger.trace(TAG, "doFakeCancel completed");
        }));
    }

    /**
     * Останавливает выполнение текущей операции.
     */
    private void stopTransaction() {
        Logger.trace(TAG, "stopTransaction outer");
        synchronized (lock) {

            Logger.trace(TAG, "stopTransaction start synchronized");

            if (!running) {
                Logger.trace(TAG, "stopTransaction, not running, return");
                Logger.trace(TAG, "stopTransaction end synchronized");
                return;
            }

            interrupted = true;

            if (operationFuture != null) {
                operationFuture.cancel(true);
                operationFuture = null;
                Logger.trace(TAG, "operationFuture canceled");
            }

            if (pclReceiver != null) {
                mContext.unregisterReceiver(pclReceiver);
                pclReceiver = null;
                Logger.trace(TAG, "pclReceiver unregistered");
            }
            if (pclConnection != null) {
                // Значит, была вызвана команда bindService, будем ждать заверешения Service.onDestroy()
                CountDownLatch countDownLatch = new CountDownLatch(1);
                BroadcastReceiver serviceDestroyReceiver = new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        // Сервис уничтожен
                        countDownLatch.countDown();
                    }
                };
                mContext.registerReceiver(serviceDestroyReceiver, new IntentFilter(LoggablePclService.ACTION_SERVICE_DESTROYED));
                mContext.unbindService(pclConnection);
                pclConnection = null;
                Logger.trace(TAG, "pclConnection unbind");
                try {
                    Logger.trace(TAG, "waiting pclService destroy...");
                    // Решаемая проблема: https://aj.srvdev.ru/browse/CPPKPP-30628
                    // Новая операция запускается не дождавшись смерти сервиса после выполнения предыдущей.
                    // Случается так потому что pclService.onDestroy выполняется целую секунду.
                    // В течении этой секунды мы уже запускаем новую операцию и нам прилетает в неё событие "onDisconnected" от предыдущей.
                    // По этому событию мы прерываем новую операцию.
                    countDownLatch.await();
                    Logger.trace(TAG, "pclService destroyed");
                } catch (InterruptedException e) {
                    Logger.error(TAG, e);
                }
            }
            pclService = null;
            if (connectionTimeoutFuture != null) {
                connectionTimeoutFuture.cancel(false);
                connectionTimeoutFuture = null;
                Logger.trace(TAG, "connectionTimeoutFuture canceled");
            }
            Logger.trace(TAG, "arcus2Server.stop");
            arcus2Server.stop();
            if (!connected) {
                running = false;
                if (methodCallback != null) {
                    Logger.trace(TAG, "stopTransaction, !connected, sendResultOnError");
                    // Возможно, arcus2Server не был запущен вообще, а, возможно, был запущен, но ещё не успел подключиться.
                    // В обоих случаях мы отвечаем клиенту ошибкой, не дожидаясь ответа от arcus2Server'а (которого может и не быть).
                    methodCallback.sendResultOnError();
                    methodCallback = null;
                }
            } else {
                Logger.trace(TAG, "Connected is true, wait Arcus2Server for result");
            }

            Logger.trace(TAG, "stopTransaction end synchronized");
        }
    }

    private void onTickBeforeTimeout(long value) {
        Logger.trace(TAG, "onTickBeforeTimeout, value = " + value);
        for (ConnectionListener connectionListener : connectionListeners)
            connectionListener.onTickBeforeTimeout(value);
    }

    private void onConnected() {
        Logger.trace(TAG, "onConnected");
        for (ConnectionListener connectionListener : connectionListeners)
            connectionListener.onConnected();
    }

    private void onConnectionTimeout() {
        Logger.trace(TAG, "onConnectionTimeout");
        for (ConnectionListener connectionListener : connectionListeners)
            connectionListener.onConnectionTimeout();
    }

    private void onDisconnected() {
        Logger.trace(TAG, "onDisconnected");
        for (ConnectionListener connectionListener : connectionListeners)
            connectionListener.onDisconnected();
    }

    @Override
    public void prepareResources() throws PosException {
        if (!bluetoothManager.enable()) {
            throw new PosException("Could not enable Bluetooth");
        }
        if (!internetManager.enable()) {
            throw new PosException("Could not enable internet");
        }
    }

    @Override
    public void freeResources() throws PosException {
        if (!bluetoothManager.disable()) {
            throw new PosException("Could not disable Bluetooth");
        }
        if (!internetManager.disable()) {
            throw new PosException("Could not disable internet");
        }
    }

    @Override
    public void terminate() {
        Logger.trace(TAG, "terminate, outer");
        Future<Boolean> future = mScheduledExecutorService.submit(() -> {
            synchronized (lock) {
                Logger.trace(TAG, "terminate start synchronized");
                if (running) {
                    Logger.trace(TAG, "terminate, running, stopTransaction");
                    stopTransaction();
                    Logger.trace(TAG, "terminate end synchronized");
                    return false;
                }
                arcus2Server.terminate();
                mExecutorService.shutdown();
                mScheduledExecutorService.shutdown();
                Logger.trace(TAG, "terminate end synchronized");
                return true;
            }
        });

        try {
            Logger.trace(TAG, "terminate future.get() start");
            for (; future.get() == false; ) ;
            Logger.trace(TAG, "terminate future.get() end");
        } catch (InterruptedException e) {
            Logger.error(TAG, e);
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            Logger.error(TAG, e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void setConnectionTimeout(long connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }

    @Override
    public long getConnectionTimeout() {
        return connectionTimeout;
    }

    @Override
    public boolean isReady() {
        return !running;
    }

    @Override
    public void addConnectionListener(ConnectionListener connectionListener) {
        connectionListeners.add(connectionListener);
    }

    @Override
    public void removeConnectionListener(ConnectionListener connectionListener) {
        connectionListeners.remove(connectionListener);
    }

    @Override
    public void testHost(ResultListener<TransactionResult> resultListener) {
        doTransactionOnExecutor(new OperationTestHost(), resultListener);
    }

    @Override
    public void testSelf(ResultListener<TransactionResult> resultListener) {
        doTransactionOnExecutor(new OperationTestTerminal(), resultListener);
    }

    @Override
    public void invokeApplicationMenu(ResultListener<TransactionResult> resultListener) {
        doTransactionOnExecutor(new OperationInvokeAdministrativeMenu(), resultListener);
    }

    @Override
    public void updateSoftware(ResultListener<TransactionResult> resultListener) {
        doTransactionOnExecutor(new OperationSyncWithTMS(), resultListener);
    }

    @Override
    public void openSession(ResultListener<TransactionResult> resultListener) {
        doTransactionOnExecutor(new OperationOpenSession(), resultListener);
    }

    @Override
    public void closeSession(ResultListener<TransactionResult> resultListener) {
        doTransactionOnExecutor(new OperationCloseSession(), resultListener);
    }

    @Override
    public void getTransactionsJournal(ResultListener<TransactionResult> resultListener) {
        doTransactionOnExecutor(new OperationGetTransactionsJournal(), resultListener);
    }

    @Override
    public void getTransactionsTotal(ResultListener<TransactionResult> resultListener) {
        doTransactionOnExecutor(new OperationGetTransactionsTotal(), resultListener);
    }

    @Override
    public void sale(int price, int saleTransactionId, ResultListener<FinancialTransactionResult> resultListener) {
        Logger.trace(TAG, "calling sale(price = " + price + ")");
        mSharedPrefs.setLastSaleTransactionLocalId(saleTransactionId);
        mSharedPrefs.setLastSaleTransactionKnownForExternal(false);
        mSharedPrefs.setLastSaleTransactionExternalId(-1);
        doTransactionOnExecutor(new OperationSale(price, saleTransactionId, new OperationSale.Callback() {
            @Override
            public void onTransactionKnownForServer() {
                mSharedPrefs.setLastSaleTransactionKnownForExternal(true);
            }

            @Override
            public void onServerTransactionIdKnown(int serverTransactionId) {
                mSharedPrefs.setLastSaleTransactionExternalId(serverTransactionId);
            }
        }), resultListener);
    }

    @Override
    public void cancel(FinancialTransactionResult saleResult, ResultListener<FinancialTransactionResult> resultListener) {
        if (saleResult.getId() == mSharedPrefs.getLastSaleTransactionLocalId()) {
            // Попытка отменить последнюю транзакцию продажи по локальному id
            Logger.trace(TAG, "cancel transaction with localId = " + saleResult.getId());
            if (mSharedPrefs.isLastSaleTransactionKnownForExternal()) {
                // Сервер знает об этой транзакции, хотя мы и не успели получить внешний id транзакции.
                // По этой причне вызываем техническую отмену последеней транзакции.
                Logger.trace(TAG, "running technical cancel for localId = " + saleResult.getId());
                doTransactionOnExecutor(new OperationTechnicalCancel(), resultListener);
            } else {
                // Имитируем отмену транзакции, на сервер не идем
                Logger.trace(TAG, "running fake cancel for localId = " + saleResult.getId());
                doFakeCancelOnExecutor(saleResult, resultListener);
            }
        } else {
            // Считаем, что это внешний id, пытаемся отменить на сервере
            doTransactionOnExecutor(new OperationCancel(saleResult.getInvoiceNumber()), resultListener);
        }
    }

    @Override
    public void technicalCancel(ResultListener<FinancialTransactionResult> resultListener) {
        doTransactionOnExecutor(new OperationTechnicalCancel(), resultListener);
    }

    /**
     * Сервис от PCL, выполняющий какую-то работу.
     * Логируется на всякий случай.
     */
    public static class LoggablePclService extends PclService {

        private static final String TAG = Logger.makeLogTag(LoggablePclService.class);

        static final String ACTION_SERVICE_DESTROYED = "ru.ppr.ingenico.intent.action.SERVICE_DESTROYED";

        public void onCreate() {
            super.onCreate();
            Logger.trace(TAG, "onCreate " + toString());
        }

        public IBinder onBind(Intent intent) {
            Logger.trace(TAG, "onBind " + toString());
            return super.onBind(intent);
        }

        public void onRebind(Intent intent) {
            Logger.trace(TAG, "onRebind " + toString());
            super.onRebind(intent);
        }

        public boolean onUnbind(Intent intent) {
            Logger.trace(TAG, "onUnbind " + toString());
            return super.onUnbind(intent);
        }

        public void onDestroy() {
            Logger.trace(TAG, "onDestroy start" + toString());
            super.onDestroy();
            Logger.trace(TAG, "onDestroy end" + toString());
            notifyDestroyed();
        }

        /**
         * Шлёт broadcast о завершении работы сервиса
         */
        private void notifyDestroyed() {
            Intent intent = new Intent();
            intent.setAction(ACTION_SERVICE_DESTROYED);
            sendBroadcast(intent);
        }

    }

    /**
     * Обретка над колбеком, переданным в выполняемую операцию
     */
    private interface MethodCallback {
        void sendResultOnError();
    }

    /**
     * Менеджер для включения/выключения Bluetooth
     */
    public interface BluetoothManager {
        boolean enable();

        boolean disable();

        boolean isEnabled();
    }

    /**
     * Менеджер для включения/выключения интернета
     */
    public interface InternetManager {
        boolean enable();

        boolean disable();
    }
}
