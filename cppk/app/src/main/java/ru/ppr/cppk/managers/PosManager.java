package ru.ppr.cppk.managers;

import android.support.annotation.NonNull;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Date;
import java.util.concurrent.Executor;

import ru.ppr.cppk.Globals;
import ru.ppr.cppk.db.LocalDaoSession;
import ru.ppr.cppk.di.Dagger;
import ru.ppr.cppk.di.Di;
import ru.ppr.cppk.entity.event.base34.Event;
import ru.ppr.cppk.entity.event.base34.TerminalDay;
import ru.ppr.cppk.entity.event.base34.TicketSaleReturnEventBase;
import ru.ppr.cppk.entity.event.model.StationDevice;
import ru.ppr.cppk.entity.utils.builders.events.TerminalDayGenerator;
import ru.ppr.cppk.localdb.model.BankOperationResult;
import ru.ppr.cppk.localdb.model.BankOperationType;
import ru.ppr.cppk.localdb.model.BankTransactionEvent;
import ru.ppr.cppk.localdb.model.ShiftEvent;
import ru.ppr.cppk.logic.ShiftManager;
import ru.ppr.cppk.logic.fiscalDocStateSync.FiscalDocStateSyncChecker;
import ru.ppr.cppk.model.PosOperationResult;
import ru.ppr.cppk.utils.Decimals;
import ru.ppr.cppk.utils.SlipConverter;
import ru.ppr.ipos.IPos;
import ru.ppr.ipos.exception.PosException;
import ru.ppr.ipos.model.FinancialTransactionResult;
import ru.ppr.ipos.model.TransactionResult;
import ru.ppr.logger.Logger;
import ru.ppr.logger.LoggerAspect;
import ru.ppr.utils.Executors;

/**
 * Менеджер для работы с POS-устройством.
 *
 * @author Aleksandr Brazhkin
 */
@LoggerAspect.IncludeClass
public class PosManager {

    private static final String TAG = Logger.makeLogTag(PosManager.class);

    /**
     * {@link Executor} для запуска операций
     */
    private final Executor executor;
    /**
     * {@link ShiftManager}, предоставляющий информацию по текущей смене
     */
    private final ShiftManager mShiftManager;
    /**
     * Актуальное событие открытия/закрытия дня
     */
    private TerminalDay currentTerminalDay;
    /**
     * Флаг подготовленности внешних ресурсов для работы {@link IPos}.
     * Устанавливается в {@link true} перед отправкой первой команды на {@link IPos} в рамках одной операции.
     * Устанавливается в {@link false} после завершения выполнения операции.
     */
    private boolean resourcesPrepared;
    /**
     * {@link IPos.ConnectionListener} для выполняемой операции.
     * Используется только для отслеживания подключения исключительно для первой команды на {@link IPos} в рамках одной операции.
     * Устанавливается в {@code null} при завершении любой команды на {@link IPos}.
     */
    private IPos.ConnectionListener connectionListenerForOperation;
    /**
     * Флаг, что {@link #connectionListenerForOperation} получил событие {@link IPos.ConnectionListener#onConnected()}
     */
    private boolean connectedForCurrentOperation;
    /**
     * Флаг, что {@link #connectionListenerForOperation} получил событие {@link IPos.ConnectionListener#onConnectionTimeout()} ()}
     */
    private boolean connectionTimeoutHandledForCurrentOperation = false;

    public PosManager(ShiftManager shiftManager) {
        mShiftManager = shiftManager;
        executor = Executors.newLoggableSingleThreadExecutor(TAG, r -> new Thread(r, "PosManagerExecutor"));
    }

    /**
     * Устанавливает слушателя состояния подключения к {@link IPos} для выполняемой операции.
     * Вызывается только из корневой точки выполнения операции, чтобы пробросить события клиенту.
     *
     * @param transactionListener Слушатель состояния подключения
     * @param <R>                 Тип возвращаемого результата
     */
    private <R extends TransactionResult> void setConnectionListenerForTransaction(TransactionListener<R> transactionListener) {
        connectionListenerForOperation = new IPos.ConnectionListener() {
            @Override
            public void onTickBeforeTimeout(long value) {
                if (transactionListener != null) {
                    transactionListener.onTickBeforeTimeout(value);
                }
            }

            @Override
            public void onConnected() {
                connectedForCurrentOperation = true;
                if (transactionListener != null) {
                    transactionListener.onConnected();
                }
            }

            @Override
            public void onConnectionTimeout() {
                connectionTimeoutHandledForCurrentOperation = true;
                if (transactionListener != null) {
                    transactionListener.onConnectionTimeout();
                }
            }

            @Override
            public void onDisconnected() {

            }
        };
        getPos().addConnectionListener(connectionListenerForOperation);
    }

    /**
     * Удаляет слушателя состояния подключения к {@link IPos} для выполняемой операции.
     * Вызывается после любой операции на {@link IPos}, т.к. неизвестно, какая была первой.
     */
    private void removeConnectionListenerForTransaction() {
        if (connectionListenerForOperation == null) {
            return;
        }
        getPos().removeConnectionListener(connectionListenerForOperation);
        connectionListenerForOperation = null;
    }

    /**
     * Проверяет состояние готовности POS-устройства к работе.
     *
     * @return {@code true} если готово, {@code false} иначе.
     */
    public boolean isReady() {
        return isPosReady();
    }

    /**
     * Возвращает установленный таймаут на подключение к терминалу
     *
     * @return таймаут
     */
    public long getConnectionTimeout() {
        return getPos().getConnectionTimeout();
    }

    /**
     * Выполняет открытие дня.
     *
     * @param transactionListener Колбек
     */
    public void dayStart(TransactionListener<TransactionResult> transactionListener) {
        executor.execute(() -> dayStartInternal(true, transactionListener));
    }

    /**
     * Выполняет закрытие дня.
     *
     * @param transactionListener Колбек
     */
    public void dayEnd(TransactionListener<TransactionResult> transactionListener) {
        executor.execute(() -> dayEndInternal(true, transactionListener));
    }

    /**
     * Выполняет тихое (напрямую на POS-устройстве без отражения в БД) закрытия дня.
     *
     * @param transactionListener Колбек
     */
    public void silentDayEnd(TransactionListener<TransactionResult> transactionListener) {
        executor.execute(() -> silentDayEndInternal(true, transactionListener));
    }

    /**
     * Выполняет отмену последней транзакции.
     *
     * @param transactionListener Колбек
     */
    public void cancelLastTransaction(TransactionListener<FinancialTransactionResult> transactionListener) {
        executor.execute(() -> cancelLastTransactionInternal(true, transactionListener));
    }

    /**
     * Выполняет отмену транзакции по идентификатору.
     *
     * @param bankTransactionCashRegisterEventId Идентификатор транзакции продажи
     * @param transactionListener                Колбек
     */
    public void cancelTransaction(long bankTransactionCashRegisterEventId, TransactionListener<FinancialTransactionResult> transactionListener) {
        executor.execute(() -> cancelTransactionInternal(bankTransactionCashRegisterEventId, true, transactionListener));
    }

    /**
     * Выполняет транзакцию продажи.
     *
     * @param price               Сумма
     * @param transactionListener Колбек
     */
    public void sale(BigDecimal price, TransactionListener<FinancialTransactionResult> transactionListener) {
        if (price.compareTo(BigDecimal.ZERO) == 0)
            throw new IllegalStateException("Can't make transaction with cost = 0.0");
        executor.execute(() -> saleInternal(price, true, transactionListener));
    }

    /**
     * Получает журнал транзакций.
     *
     * @param transactionListener Колбек
     */
    public void getTransactionsJournal(TransactionListener<TransactionResult> transactionListener) {
        executor.execute(() -> getTransactionsJournalInternal(true, transactionListener));
    }

    /**
     * Выполняет тест POS-устройства.
     *
     * @param transactionListener Колбек
     */
    public void testDevice(TransactionListener<TransactionResult> transactionListener) {
        executor.execute(() -> testDeviceInternal(true, transactionListener));
    }

    /**
     * Выполняет тест хоста.
     *
     * @param transactionListener Колбек
     */
    public void testHost(TransactionListener<TransactionResult> transactionListener) {
        executor.execute(() -> testHostInternal(true, transactionListener));
    }

    /**
     * Вызвает меню POS-устройства.
     *
     * @param transactionListener Колбек
     */
    public void invokeAdministrativeMenu(TransactionListener<TransactionResult> transactionListener) {
        executor.execute(() -> invokeAdministrativeMenuInternal(true, transactionListener));
    }

    /**
     * Выполняет обновление софта POS-устройства.
     *
     * @param transactionListener Колбек
     */
    public void syncWithTMS(TransactionListener<TransactionResult> transactionListener) {
        executor.execute(() -> syncWithTMSInternal(true, transactionListener));
    }

    /**
     * Получает сводку по транзакциям.
     *
     * @param transactionListener Колбек
     */
    public void getTransactionsTotal(TransactionListener<TransactionResult> transactionListener) {
        executor.execute(() -> getTransactionsTotalInternal(true, transactionListener));
    }

    /**
     * Обработчик результата выполнения операции.
     *
     * @param <R> Тип результата
     */
    private class ResultHandler<R extends TransactionResult> {
        /**
         * Флаг, что используется для первой команды в цепочке
         */
        private final boolean isRoot;
        /**
         * Колбек для команды
         */
        private final TransactionListener<R> transactionListener;

        private ResultHandler(boolean isRoot, TransactionListener<R> transactionListener) {
            this.isRoot = isRoot;
            this.transactionListener = transactionListener;
        }

        void onResult(@NonNull PosOperationResult<R> operationResult) {
            boolean connected = connectedForCurrentOperation;
            boolean timeoutHandled = connectionTimeoutHandledForCurrentOperation;
            if (isRoot) {
                // Это значит, что это последний обработчик ошибки, нужно всё почистить
                connectedForCurrentOperation = false;
                connectionTimeoutHandledForCurrentOperation = false;
                removeConnectionListenerForTransaction();
                freeResources();
            }
            // Нельзя вызывать onResult(), если transactionListener - это слушатель извне, и он уже получил onConnectionTimeout()
            if (connected || !isRoot || !timeoutHandled) {
                if (transactionListener != null) {
                    transactionListener.onResult(operationResult);
                }
            }
        }
    }

    /**
     * Выполняет открытие дня.
     *
     * @param isRoot              Первая операция в цепочке
     * @param transactionListener Колбек
     */
    private void dayStartInternal(boolean isRoot, TransactionListener<TransactionResult> transactionListener) {

        ResultHandler<TransactionResult> resultHandler = new ResultHandler<>(isRoot, transactionListener);

        Runnable operation = () -> {

            if (!prepareResources(resultHandler)) {
                return;
            }

            getPos().openSession(result -> {

                removeConnectionListenerForTransaction();

                TerminalDay lastTerminalDay = currentTerminalDay;
                ShiftEvent shiftEvent = mShiftManager.getCurrentShiftEvent();

                if (result == null) {
                    Logger.trace(TAG, "transactionResult is null!");
                } else {
                    if (result.isApproved()) {

                        getLocalDaoSession().getLocalDb().beginTransaction();
                        try {
                            // добавляем информацию о ПТК
                            StationDevice stationDevice = Di.INSTANCE.getDeviceSessionInfo().getCurrentStationDevice();
                            if (stationDevice != null) {
                                getLocalDaoSession().getStationDeviceDao().insertOrThrow(stationDevice);
                            }
                            Event event = Di.INSTANCE.eventBuilder()
                                    .setDeviceId(stationDevice.getId())
                                    .build();
                            getLocalDaoSession().getEventDao().insertOrThrow(event);

                            TerminalDay newTerminalDay = new TerminalDayGenerator()
                                    .setTerminalDayId(lastTerminalDay.getTerminalDayId() + 1)
                                    .setStartDateTime(new Date())
                                    .setTerminalNumber(result.getTerminalId())
                                    .setEvent(event)
                                    .setStartShiftEventId(shiftEvent == null ? null : shiftEvent.getId())
                                    .setCurrentSaleTransactionId(0)
                                    .build();
                            getLocalDaoSession().getTerminalDayDao().insertOrThrow(newTerminalDay);

                            getLocalDaoSession().getLocalDb().setTransactionSuccessful();
                        } finally {
                            getLocalDaoSession().getLocalDb().endTransaction();
                        }
                        Logger.trace(TAG, "Starting new terminal day is successful!");
                        refreshState();
                    } else {
                        Logger.info(TAG, "Operation openSession is not approved by a bank!");
                    }
                }

                PosOperationResult<TransactionResult> operationResult = new PosOperationResult<>(result, null);
                resultHandler.onResult(operationResult);
            });
        };

        //если POS-устройство занято
        if (!isPosReady()) {
            PosOperationResult<TransactionResult> operationResult = new PosOperationResult<>(null, null);
            resultHandler.onResult(operationResult);
            return;
        }

        if (isRoot) {
            setConnectionListenerForTransaction(transactionListener);
        }

        ShiftEvent shiftEvent = mShiftManager.getCurrentShiftEvent();
        if (shiftEvent == null) {
            Logger.trace(TAG, "cashRegisterWorkingShift is null!");
            PosOperationResult<TransactionResult> operationResult = new PosOperationResult<>(null, null);
            resultHandler.onResult(operationResult);
            return;
        }

        //проверка на открытый день, если таблица пустая, считаем что день открытый и его нужно закрыть
        if (isDayStarted()) {
            Logger.trace(TAG, "Terminal day is currently started, need to end it before!");

            dayEndInternal(false, new AbstractTransactionListener() {
                @Override
                public void onResult(@NonNull PosOperationResult<TransactionResult> operationResult) {
                    TransactionResult result = operationResult.getTransactionResult();
                    if (result != null && result.isApproved()) {
                        if (!isDayCanBeOpened()) {
                            operationResult = new PosOperationResult<>(null, "Попытка открытия нескольких дней в одной смене");
                            resultHandler.onResult(operationResult);
                        } else {
                            operation.run();
                        }
                    } else {
                        PosOperationResult<TransactionResult> res = new PosOperationResult<>(null, null);
                        resultHandler.onResult(res);
                    }
                }
            });
        } else if (!isDayCanBeOpened()) {
            PosOperationResult<TransactionResult> operationResult = new PosOperationResult<>(null, "Попытка открытия нескольких дней в одной смене");
            resultHandler.onResult(operationResult);
        } else {
            operation.run();
        }
    }

    /**
     * Выполняет закрытие дня.
     *
     * @param isRoot              Первая операция в цепочке
     * @param transactionListener Колбек
     */
    private void dayEndInternal(boolean isRoot, TransactionListener<TransactionResult> transactionListener) {

        ResultHandler<TransactionResult> resultHandler = new ResultHandler<>(isRoot, transactionListener);

        Runnable operation = () -> {

            if (!prepareResources(resultHandler)) {
                return;
            }

            getPos().closeSession(result -> {

                removeConnectionListenerForTransaction();

                TerminalDay lastTerminalDay = currentTerminalDay;
                ShiftEvent shiftEvent = mShiftManager.getCurrentShiftEvent();

                if (result == null) {
                    Logger.trace(TAG, "transactionResult is null!");
                } else {
                    if (result.isApproved()) {

                        getLocalDaoSession().getLocalDb().beginTransaction();
                        try {
                            // добавляем информацию о ПТК
                            StationDevice stationDevice = Di.INSTANCE.getDeviceSessionInfo().getCurrentStationDevice();
                            if (stationDevice != null) {
                                getLocalDaoSession().getStationDeviceDao().insertOrThrow(stationDevice);
                            }
                            Event event = Di.INSTANCE.eventBuilder()
                                    .setDeviceId(stationDevice.getId())
                                    .build();
                            getLocalDaoSession().getEventDao().insertOrThrow(event);

                            TerminalDay terminalDay = new TerminalDayGenerator()
                                    .setTerminalDayId(lastTerminalDay == null ? 0 : lastTerminalDay.getTerminalDayId())
                                    .setStartDateTime(lastTerminalDay == null ? new Date(0) : lastTerminalDay.getStartDateTime())
                                    .setEndDateTime(new Date())
                                    .setReport(SlipConverter.toImage(result.getReceipt()))
                                    .setTerminalNumber(result.getTerminalId())
                                    .setEvent(event)
                                    .setStartShiftEventId(lastTerminalDay == null ? null : lastTerminalDay.getStartShiftEventId())
                                    .setEndShiftEventId(shiftEvent == null ? null : shiftEvent.getId())
                                    .setCurrentSaleTransactionId(lastTerminalDay == null ? 0 : lastTerminalDay.getCurrentSaleTransactionId())
                                    .build();

                            getLocalDaoSession().getTerminalDayDao().insertOrThrow(terminalDay);
                            getLocalDaoSession().getLocalDb().setTransactionSuccessful();
                        } finally {
                            getLocalDaoSession().getLocalDb().endTransaction();
                        }
                        Logger.trace(TAG, "Ending last terminal day is successful!");
                        refreshState();
                    } else {
                        Logger.trace(TAG, "Operation closeSession is not approved by a bank!");
                    }
                }

                PosOperationResult<TransactionResult> operationResult = new PosOperationResult<>(result, null);
                resultHandler.onResult(operationResult);
            });
        };

        //если POS-устройство занято
        if (!isPosReady()) {
            PosOperationResult<TransactionResult> operationResult = new PosOperationResult<>(null, null);
            resultHandler.onResult(operationResult);
            return;
        }

        if (isRoot) {
            setConnectionListenerForTransaction(transactionListener);
        }

        //проверка на открытый день
        if (isDayStarted()) {
            ShiftEvent shiftEvent = mShiftManager.getCurrentShiftEvent();
            if (shiftEvent == null) {
                Logger.trace(TAG, "cashRegisterWorkingShift is null!");
                PosOperationResult<TransactionResult> operationResult = new PosOperationResult<>(null, null);
                resultHandler.onResult(operationResult);
                return;
            }
        } else {
            Logger.trace(TAG, "Terminal day is currently ended!");
            PosOperationResult<TransactionResult> operationResult = new PosOperationResult<>(null, null);
            resultHandler.onResult(operationResult);
            return;
        }

        //проверка на незавершенные транзакции
        if (isLastTransactionMustBeCancelled()) {
            Logger.trace(TAG, "Ending last terminal day is held, first of all we need to cancel uncompleted transaction!");

            cancelLastTransactionInternal(false, new AbstractFinancialTransactionListener() {
                @Override
                public void onResult(@NonNull PosOperationResult<FinancialTransactionResult> operationResult) {
                    FinancialTransactionResult result = operationResult.getTransactionResult();
                    if (result != null && result.isApproved())
                        operation.run();
                    else {
                        PosOperationResult<TransactionResult> res = new PosOperationResult<>(null, null);
                        resultHandler.onResult(res);
                    }
                }
            });
        } else {
            operation.run();
        }
    }

    /**
     * Выполняет тихое (напрямую на POS-устройстве без отражения в БД) закрытия дня.
     *
     * @param isRoot              Первая операция в цепочке
     * @param transactionListener Колбек
     */
    private void silentDayEndInternal(boolean isRoot, TransactionListener<TransactionResult> transactionListener) {

        ResultHandler<TransactionResult> resultHandler = new ResultHandler<>(isRoot, transactionListener);

        Runnable operation = () -> {

            if (!prepareResources(resultHandler)) {
                return;
            }

            getPos().closeSession(result -> {
                removeConnectionListenerForTransaction();
                PosOperationResult<TransactionResult> operationResult = new PosOperationResult<>(result, null);
                resultHandler.onResult(operationResult);
            });
        };

        //если POS-устройство занято
        if (!isPosReady()) {
            PosOperationResult<TransactionResult> operationResult = new PosOperationResult<>(null, null);
            resultHandler.onResult(operationResult);
            return;
        }

        if (isRoot) {
            setConnectionListenerForTransaction(transactionListener);
        }

        operation.run();

    }

    /**
     * Выполняет отмену последней транзакции.
     *
     * @param isRoot              Первая операция в цепочке
     * @param transactionListener Колбек
     */
    private void cancelLastTransactionInternal(boolean isRoot, TransactionListener<FinancialTransactionResult> transactionListener) {

        BankTransactionEvent lastSaleTransaction = getLocalDaoSession().getBankTransactionDao().getLastEventByType(BankOperationType.SALE);

        if (lastSaleTransaction == null) {
            Logger.info(TAG, "There is no last transaction!");
            PosOperationResult<FinancialTransactionResult> result = new PosOperationResult<>(null, null);
            transactionListener.onResult(result);
            return;
        }

        cancelTransactionInternal(lastSaleTransaction.getId(), isRoot, transactionListener);

    }

    /**
     * Выполняет отмену транзакции по идентификатору.
     *
     * @param bankTransactionCashRegisterEventId Идентификатор транзакции продажи
     * @param isRoot                             Первая операция в цепочке
     * @param transactionListener                Колбек
     */
    private void cancelTransactionInternal(long bankTransactionCashRegisterEventId, boolean isRoot, TransactionListener<FinancialTransactionResult> transactionListener) {
        ResultHandler<FinancialTransactionResult> resultHandler = new ResultHandler<>(isRoot, transactionListener);

        BankTransactionEvent saleEvent = getLocalDaoSession()
                .getBankTransactionDao()
                .load(bankTransactionCashRegisterEventId);

        BankTransactionEvent cancellationEvent = getLocalDaoSession()
                .getBankTransactionDao()
                .getEventByParams(saleEvent.getTransactionId(), BankOperationType.CANCELLATION, saleEvent.getTerminalDayId());

        Runnable operation = () -> {

            if (!prepareResources(resultHandler)) {
                return;
            }

            boolean isFakeTransactionId = saleEvent.getStatus() == BankTransactionEvent.Status.STARTED;

            BankTransactionEvent cancellationEventLocal;

            if (isFakeTransactionId) {
                cancellationEventLocal = null;
            } else {
                if (cancellationEvent == null) {
                    int amount = saleEvent.getTotal().multiply(Decimals.HUNDRED).intValue();
                    cancellationEventLocal = Dagger.appComponent().bankTransactionEventCreator()
                            .setFinancialTransactionResult(new EmptyFinancialTransactionResult(saleEvent.getTransactionId(), amount))
                            .setBankOperationType(BankOperationType.CANCELLATION)
                            .setStatus(BankTransactionEvent.Status.STARTED)
                            .create();
                } else {
                    cancellationEventLocal = cancellationEvent;
                }
            }

            getPos().cancel(makeFTRFromBTCRE(saleEvent), result -> {

                removeConnectionListenerForTransaction();

                if (result == null)
                    Logger.trace(TAG, "financialTransactionResult is null!");
                else {
                    if (isFakeTransactionId) {
                        // Логика со слов Александра Корчака:
                        // Если по локальной БД статус отменяемой транзакции - STARTED, наша задача удалить эту строчку из БД.
                        // Неважно, в каком состоянии на самом деле находится транзакция с точки зрения POS-устройства.
                        // Даже если, например, Ingenico ввиду особенностей реализации выполнила сейчас техническую отмену,
                        // мы просто скрываем этот факт. Считается, что вероятность технической отмены в Ingenico мала.
                        if (result.isApproved()) {
                            Logger.trace(TAG, "Deleting existing sale transaction, reason - result is approved");
                            getLocalDaoSession().getBankTransactionDao().delete(saleEvent);
                        } else if (result.getInvoiceNumber() == 0) {
                            // http://agile.srvdev.ru/browse/CPPKPP-32537
                            // Ситуация на Ingenico:
                            // Выставлен флаг LAST_SALE_TRANSACTION_KNOWN_FOR_EXTERNAL = true
                            // Это значит, что мы пытаемся делать техническую отмену
                            // Но терминал не дает нам отменить транзакцию, поскольку она не была завершена успешно
                            // В таком случае тоже удалим транзакцию продажи из БД
                            Logger.trace(TAG, "Deleting existing sale transaction, reason - invoice number = 0");
                            getLocalDaoSession().getBankTransactionDao().delete(saleEvent);
                        }
                    } else {
                        if (result.isApproved()) {
                            if (saleEvent.getStatus() != BankTransactionEvent.Status.COMPLETED_FULLY) {
                                // По сути, сюда мы попадем только если статус транзакции - COMPLETED_BUT_NOT_ASSOCIATED_WITH_FISCAL_OPERATION.
                                // Т.е. этот случай происходит при отмене последней незавершенной транзакции, и мы обязаны сменить её статус,
                                // чтобы больше не было незаверешенной транзакции.
                                saleEvent.setStatus(BankTransactionEvent.Status.COMPLETED_FULLY);
                                getLocalDaoSession().getBankTransactionDao().update(saleEvent);
                            }
                        }
                        fillBTCREFromFTR(cancellationEventLocal, result);
                        cancellationEventLocal.setStatus(BankTransactionEvent.Status.COMPLETED_FULLY);
                        getLocalDaoSession().getBankTransactionDao().update(cancellationEventLocal);
                    }
                }

                PosOperationResult<FinancialTransactionResult> operationResult = new PosOperationResult<>(result, null);
                resultHandler.onResult(operationResult);
            });

        };

        //если POS-устройство занято
        if (!isPosReady()) {
            PosOperationResult<FinancialTransactionResult> operationResult = new PosOperationResult<>(null, null);
            resultHandler.onResult(operationResult);
            return;
        }

        if (isRoot) {
            setConnectionListenerForTransaction(transactionListener);
        }

        if (saleEvent == null) {
            Logger.trace(TAG, "bankTransactionCashRegisterEvent == null");
            PosOperationResult<FinancialTransactionResult> operationResult = new PosOperationResult<>(null, null);
            resultHandler.onResult(operationResult);
            return;
        }

        if (saleEvent.getOperationType() != BankOperationType.SALE) {
            Logger.trace(TAG, "Incorrect operation type = " + saleEvent.getOperationType());
            PosOperationResult<FinancialTransactionResult> operationResult = new PosOperationResult<>(null, null);
            resultHandler.onResult(operationResult);
            return;
        }

        //проверка на открытый день
        if (isDayStarted()) {
            operation.run();
        } else {
            Logger.trace(TAG, "Terminal day is currently ended!");
            PosOperationResult<FinancialTransactionResult> operationResult = new PosOperationResult<>(null, "День на POS-терминале уже закрыт");
            resultHandler.onResult(operationResult);
        }

    }

    /**
     * Выполняет транзакцию продажи.
     *
     * @param price               Сумма
     * @param isRoot              Первая операция в цепочке
     * @param transactionListener Колбек
     */
    private void saleInternal(BigDecimal price, boolean isRoot, TransactionListener<FinancialTransactionResult> transactionListener) {
        ResultHandler<FinancialTransactionResult> resultHandler = new ResultHandler<>(isRoot, transactionListener);

        Runnable operation = () -> {

            if (!prepareResources(resultHandler)) {
                return;
            }

            int amount = price.multiply(Decimals.HUNDRED).intValue();

            //покупка
            BankTransactionEvent saleEvent = Dagger.appComponent().bankTransactionEventCreator()
                    .setFinancialTransactionResult(new EmptyFinancialTransactionResult(getNewTransactionId(), amount))
                    .setBankOperationType(BankOperationType.SALE)
                    .setStatus(BankTransactionEvent.Status.STARTED)
                    .create();

            Logger.trace(TAG, "executing sale(price = " + amount + ")");

            getPos().sale(amount, saleEvent.getTransactionId(), result -> {

                removeConnectionListenerForTransaction();

                if (result == null) {
                    Logger.info(TAG, "financialTransactionResult is null!");
                } else if (result.getId() == 0 && result.isApproved()) {
                    // http://agile.srvdev.ru/browse/CPPKPP-34295
                    // http://agile.srvdev.ru/browse/CPPKPP-33848
                    // C уровня Ingenico бывает и такое, что ничего нет, а approved = true.
                    // На самом деле это означает, что такой транзакции не было.
                    // Оставим статус STARTED, чтобы удалить после аннулирования.
                    // Данные транзакции не обновляем (Это важно!), что позволит отменять транзакцию с локальным Id, а не с полученным Id транзакции = 0
                    // В обоих бекапах с полинона, где возникла данная проблема, состояние драйвера следующее:
                    // <boolean name="LAST_SALE_TRANSACTION_KNOWN_FOR_EXTERNAL" value="false" />
                    // <int name="LAST_SALE_TRANSACTION_EXTERNAL_ID" value="0" />
                    // <int name="LAST_SALE_TRANSACTION_LOCAL_ID" value="локальный id, не равный 0" />
                    // Данное состояние уводит в ветку фейковой отмены транзакции.
                    // В теории, это должно корректно отработать на текущей реализации Ingenico.
                    Logger.error(TAG, "Invoice number = 0. Ignoring terminal response");
                } else {
                    fillBTCREFromFTR(saleEvent, result);
                    saleEvent.setStatus(result.isApproved() ?
                            BankTransactionEvent.Status.COMPLETED_BUT_NOT_ASSOCIATED_WITH_FISCAL_OPERATION :
                            BankTransactionEvent.Status.COMPLETED_FULLY);
                }
                getLocalDaoSession().getBankTransactionDao().update(saleEvent);

                PosOperationResult<FinancialTransactionResult> operationResult = new PosOperationResult<>(result, null);
                resultHandler.onResult(operationResult);
            });
        };

        //если POS-устройство занято
        if (!isPosReady()) {
            PosOperationResult<FinancialTransactionResult> operationResult = new PosOperationResult<>(null, null);
            resultHandler.onResult(operationResult);
            return;
        }

        if (BigDecimal.ZERO.equals(price)) {
            Logger.trace(TAG, "Could not sale, price = 0");
            PosOperationResult<FinancialTransactionResult> operationResult = new PosOperationResult<>(null, null);
            resultHandler.onResult(operationResult);
            return;
        }

        if (isRoot) {
            setConnectionListenerForTransaction(transactionListener);
        }

        Runnable afterPrevDayClosedRunnable = () -> dayStartInternal(false, new AbstractTransactionListener() {
            @Override
            public void onResult(@NonNull PosOperationResult<TransactionResult> operationResult) {
                TransactionResult result = operationResult.getTransactionResult();
                if (result != null && result.isApproved())
                    operation.run();
                else {
                    PosOperationResult<FinancialTransactionResult> res = new PosOperationResult<>(null, null);
                    resultHandler.onResult(res);
                }
            }
        });

        Runnable afterCancelTransactionRunnable = () -> {
            //проверка на открытый день
            if (isDayStarted()) {
                //проверка на то, нужно ли переоткрыть день
                if (isDayMustBeReopened()) {
                    dayEndInternal(false, new AbstractTransactionListener() {
                        @Override
                        public void onResult(@NonNull PosOperationResult<TransactionResult> operationResult) {
                            TransactionResult result = operationResult.getTransactionResult();
                            if (result != null && result.isApproved())
                                afterPrevDayClosedRunnable.run();
                            else {
                                PosOperationResult<FinancialTransactionResult> res = new PosOperationResult<>(null, null);
                                resultHandler.onResult(res);
                            }
                        }
                    });
                } else {
                    operation.run();
                }
            } else {
                afterPrevDayClosedRunnable.run();
            }
        };

        //проверка на незавершенные транзакции
        if (isLastTransactionMustBeCancelled()) {
            Logger.trace(TAG, "Sale is held, first of all we need to cancel uncompleted transaction!");

            cancelLastTransactionInternal(false, new AbstractFinancialTransactionListener() {
                @Override
                public void onResult(@NonNull PosOperationResult<FinancialTransactionResult> operationResult) {
                    FinancialTransactionResult result = operationResult.getTransactionResult();
                    if (result != null && result.isApproved())
                        afterCancelTransactionRunnable.run();
                    else {
                        PosOperationResult<FinancialTransactionResult> res = new PosOperationResult<>(null, null);
                        resultHandler.onResult(res);
                    }
                }
            });
        } else {
            afterCancelTransactionRunnable.run();
        }
    }

    /**
     * Получает журнал транзакций.
     *
     * @param isRoot              Первая операция в цепочке
     * @param transactionListener Колбек
     */
    private void getTransactionsJournalInternal(boolean isRoot, TransactionListener<TransactionResult> transactionListener) {
        ResultHandler<TransactionResult> resultHandler = new ResultHandler<>(isRoot, transactionListener);

        Runnable operation = () -> {

            if (!prepareResources(resultHandler)) {
                return;
            }

            getPos().getTransactionsJournal(result -> {

                removeConnectionListenerForTransaction();

                PosOperationResult<TransactionResult> operationResult = new PosOperationResult<>(result, null);
                resultHandler.onResult(operationResult);
            });
        };

        //если POS-устройство занято
        if (!isPosReady()) {
            PosOperationResult<TransactionResult> operationResult = new PosOperationResult<>(null, null);
            resultHandler.onResult(operationResult);
            return;
        }

        if (isRoot) {
            setConnectionListenerForTransaction(transactionListener);
        }

        //проверка на открытый день
        if (isDayStarted()) {
            operation.run();
        } else {
            Logger.trace(TAG, "Terminal day is currently ended!");
            PosOperationResult<TransactionResult> operationResult = new PosOperationResult<>(null, null);
            resultHandler.onResult(operationResult);
        }
    }

    /**
     * Выполняет тест POS-устройства.
     *
     * @param isRoot              Первая операция в цепочке
     * @param transactionListener Колбек
     */
    private void testDeviceInternal(boolean isRoot, TransactionListener<TransactionResult> transactionListener) {
        ResultHandler<TransactionResult> resultHandler = new ResultHandler<>(isRoot, transactionListener);

        Runnable operation = () -> {

            if (!prepareResources(resultHandler)) {
                return;
            }

            getPos().testSelf(result -> {

                removeConnectionListenerForTransaction();

                PosOperationResult<TransactionResult> operationResult = new PosOperationResult<>(result, null);
                resultHandler.onResult(operationResult);
            });
        };

        //если POS-устройство занято
        if (!isPosReady()) {
            PosOperationResult<TransactionResult> operationResult = new PosOperationResult<>(null, null);
            resultHandler.onResult(operationResult);
            return;
        }

        if (isRoot) {
            setConnectionListenerForTransaction(transactionListener);
        }

        operation.run();
    }

    /**
     * Выполняет тест хоста.
     *
     * @param isRoot              Первая операция в цепочке
     * @param transactionListener Колбек
     */
    private void testHostInternal(boolean isRoot, TransactionListener<TransactionResult> transactionListener) {
        ResultHandler<TransactionResult> resultHandler = new ResultHandler<>(isRoot, transactionListener);

        Runnable operation = () -> {

            if (!prepareResources(resultHandler)) {
                return;
            }

            getPos().testHost(result -> {

                removeConnectionListenerForTransaction();

                PosOperationResult<TransactionResult> operationResult = new PosOperationResult<>(result, null);
                resultHandler.onResult(operationResult);
            });
        };

        //если POS-устройство занято
        if (!isPosReady()) {
            PosOperationResult<TransactionResult> operationResult = new PosOperationResult<>(null, null);
            resultHandler.onResult(operationResult);
            return;
        }

        if (isRoot) {
            setConnectionListenerForTransaction(transactionListener);
        }

        operation.run();
    }

    /**
     * Вызвает меню POS-устройства.
     *
     * @param isRoot              Первая операция в цепочке
     * @param transactionListener Колбек
     */
    private void invokeAdministrativeMenuInternal(boolean isRoot, TransactionListener<TransactionResult> transactionListener) {
        ResultHandler<TransactionResult> resultHandler = new ResultHandler<>(isRoot, transactionListener);

        Runnable operation = () -> {

            if (!prepareResources(resultHandler)) {
                return;
            }

            getPos().invokeApplicationMenu(result -> {

                removeConnectionListenerForTransaction();

                PosOperationResult<TransactionResult> operationResult = new PosOperationResult<>(result, null);
                resultHandler.onResult(operationResult);
            });
        };

        //если POS-устройство занято
        if (!isPosReady()) {
            PosOperationResult<TransactionResult> operationResult = new PosOperationResult<>(null, null);
            resultHandler.onResult(operationResult);
            return;
        }

        if (isRoot) {
            setConnectionListenerForTransaction(transactionListener);
        }

        operation.run();
    }

    /**
     * Выполняет обновление софта POS-устройства.
     *
     * @param isRoot              Первая операция в цепочке
     * @param transactionListener Колбек
     */
    private void syncWithTMSInternal(boolean isRoot, TransactionListener<TransactionResult> transactionListener) {
        ResultHandler<TransactionResult> resultHandler = new ResultHandler<>(isRoot, transactionListener);

        Runnable operation = () -> {

            if (!prepareResources(resultHandler)) {
                return;
            }

            getPos().updateSoftware(result -> {

                removeConnectionListenerForTransaction();

                PosOperationResult<TransactionResult> operationResult = new PosOperationResult<>(result, null);
                resultHandler.onResult(operationResult);
            });
        };

        //если POS-устройство занято
        if (!isPosReady()) {
            PosOperationResult<TransactionResult> operationResult = new PosOperationResult<>(null, null);
            resultHandler.onResult(operationResult);
            return;
        }

        if (isRoot) {
            setConnectionListenerForTransaction(transactionListener);
        }

        operation.run();
    }

    /**
     * Получает сводку по транзакциям.
     *
     * @param isRoot              Первая операция в цепочке
     * @param transactionListener Колбек
     */
    private void getTransactionsTotalInternal(boolean isRoot, TransactionListener<TransactionResult> transactionListener) {
        ResultHandler<TransactionResult> resultHandler = new ResultHandler<>(isRoot, transactionListener);

        Runnable operation = () -> {

            if (!prepareResources(resultHandler)) {
                return;
            }

            getPos().getTransactionsTotal(result -> {

                removeConnectionListenerForTransaction();

                PosOperationResult<TransactionResult> operationResult = new PosOperationResult<>(result, null);
                resultHandler.onResult(operationResult);
            });
        };

        //если POS-устройство занято
        if (!isPosReady()) {
            PosOperationResult<TransactionResult> operationResult = new PosOperationResult<>(null, null);
            resultHandler.onResult(operationResult);
            return;
        }

        if (isRoot) {
            setConnectionListenerForTransaction(transactionListener);
        }

        operation.run();
    }

    /**
     * Возвращает текущее POS-устройство
     *
     * @return Текущее POS-устройство
     */
    public IPos getPos() {
        return Globals.getPosTerminal();
    }

    /**
     * Возвращает {@link LocalDaoSession}
     *
     * @return {@link LocalDaoSession}
     */
    private LocalDaoSession getLocalDaoSession() {
        return Dagger.appComponent().localDaoSession();
    }

    /**
     * Актулизирует сведения по текущему дню
     */
    public void refreshState() {
        currentTerminalDay = getLocalDaoSession().getTerminalDayDao().getLastTerminalDay();
    }

    /**
     * Проверяет открыт ли день
     *
     * @return {@code true} если день открыт или его нет, {@code false} иначе
     */
    public boolean isDayStarted() {
        // Николай Лившиц: Если последнего дня нет (т.е. база чистая) считаем день октрытым.
        TerminalDay lastTerminalDay = currentTerminalDay;
        return lastTerminalDay == null || lastTerminalDay.getEndDateTime() == null;
    }

    /**
     * Проверяет открыт ли день
     *
     * @return {@code true} если день существует и открыт, {@code false} иначе
     */
    public boolean isDayStartedAndExists() {
        TerminalDay lastTerminalDay = currentTerminalDay;
        return lastTerminalDay != null && lastTerminalDay.getEndDateTime() == null;
    }

    /**
     * Проверяет, требуется ли открыть новый день.
     *
     * @return {@code true}, если требуется, {@code false} иначе
     */
    public boolean isDayMustBeReopened() {
        TerminalDay lastTerminalDay = currentTerminalDay;
        ShiftEvent shiftEvent = mShiftManager.getCurrentShiftEvent();

        return lastTerminalDay == null
                || shiftEvent != null
                && isDayStarted()
                && shiftEvent.getStatus() == ShiftEvent.Status.STARTED
                && lastTerminalDay.getStartShiftEventId() != shiftEvent.getId();
    }

    /**
     * Проверяет, возможно ли открыть день
     *
     * @return {@code true}, если можно, {@code false} иначе
     */
    public boolean isDayCanBeOpened() {
        TerminalDay lastTerminalDay = currentTerminalDay;
        ShiftEvent shiftEvent = mShiftManager.getCurrentShiftEvent();
        //запретим открывать день вне смены
        if (!mShiftManager.isShiftOpened())
            return false;
        //если день еще не открывался, значит можно
        if (lastTerminalDay == null)
            return true;
        //можно открывать день, если предыдущий день был открыт в другой смене
        return lastTerminalDay.getStartShiftEventId() != shiftEvent.getId();
    }

    /**
     * Проверяет, требуется ли отмена последней транзакции продажи
     *
     * @return {@code true}, если требуется отмена, {@code false} иначе
     */
    public boolean isLastTransactionMustBeCancelled() {
        BankTransactionEvent lastSaleTransaction = getLocalDaoSession().getBankTransactionDao().getLastEventByType(BankOperationType.SALE);

        if (mShiftManager.isShiftOpened()) {
            FiscalDocStateSyncChecker fiscalDocStateSyncChecker = Dagger.appComponent().fiscalDocStateSyncChecker();
            FiscalDocStateSyncChecker.Result result = fiscalDocStateSyncChecker.check();

            if (lastSaleTransaction != null && !result.isEmpty()) {
                BankTransactionEvent fiscalDocTransaction;

                if (result.getTicketSaleEvent() != null) {
                    TicketSaleReturnEventBase ticketSaleReturnEventBase = getLocalDaoSession().getTicketSaleReturnEventBaseDao()
                            .load(result.getTicketSaleEvent().getTicketSaleReturnEventBaseId());
                    fiscalDocTransaction = getLocalDaoSession().getBankTransactionDao().load(ticketSaleReturnEventBase.getBankTransactionEventId());
                } else if (result.getTicketReturnEvent() != null) {
                    fiscalDocTransaction = getLocalDaoSession().getBankTransactionDao().load(result.getTicketReturnEvent().getBankTransactionCashRegisterEventId());
                } else {//if (result.getFineSaleEvent() != null)
                    fiscalDocTransaction = getLocalDaoSession().getBankTransactionDao().load(result.getFineSaleEvent().getBankTransactionEventId());
                }
                //для тестовых ПД синхронизацию не делаем, т.к. банковских событий там не бывает

                // в рамках http://agile.srvdev.ru/browse/CPPKPP-35201 обсуждение функционала ниже было по скайпу
                // Если последняя транзакция должна быть отменена, необходимо еще проверить та же самая ли
                // это транзакция, и в таком случае считаем что ничего отменять не нужно, т.к. статус поменяется в рамках
                // механизма синхронзации состояния ФР, эквивалентность определяется по суррогатному id
                if (fiscalDocTransaction != null && lastSaleTransaction.getId().equals(fiscalDocTransaction.getId())) {
                    return false;
                }
            }
        }

        return lastSaleTransaction != null
                && lastSaleTransaction.getStatus() != BankTransactionEvent.Status.COMPLETED_FULLY
                && lastSaleTransaction.getStatus() != BankTransactionEvent.Status.COMPLETED_WITHOUT_POS;
    }

    /**
     * Конвертирует {@link BankTransactionEvent} в {@link FinancialTransactionResult}
     *
     * @param bankTransactionEvent Транзакция локальной БД
     * @return Транзакция в представлении {@link IPos}
     */
    private FinancialTransactionResult makeFTRFromBTCRE(BankTransactionEvent bankTransactionEvent) {
        FinancialTransactionResult financialTransactionResult = new FinancialTransactionResult();

        financialTransactionResult.setId((int) bankTransactionEvent.getTransactionId());
        financialTransactionResult.setTimeStamp(bankTransactionEvent.getTransactionDateTime());
        financialTransactionResult.setApproved(bankTransactionEvent.getOperationResult() == BankOperationResult.Approved);
        financialTransactionResult.setTerminalId(bankTransactionEvent.getTerminalNumber());
        financialTransactionResult.setInvoiceNumber((int) bankTransactionEvent.getBankCheckNumber());
        //вроде бы не нужны, да и брать неоткуда
        //financialTransactionResult.setReceipt();
        //financialTransactionResult.setBankResponseCode();
        //financialTransactionResult.setBankResponse();
        financialTransactionResult.setAmount(bankTransactionEvent.getTotal().multiply(Decimals.HUNDRED).intValue());
        financialTransactionResult.setCardPAN(bankTransactionEvent.getCardPan());
        financialTransactionResult.setRRN(bankTransactionEvent.getRrn());
        financialTransactionResult.setMerchantId(bankTransactionEvent.getMerchantId());
        financialTransactionResult.setAuthorizationId(bankTransactionEvent.getAuthorizationCode());
        financialTransactionResult.setIssuerName(bankTransactionEvent.getCardEmitentName());
        financialTransactionResult.setCurrencyCode(bankTransactionEvent.getCurrencyCode());
        financialTransactionResult.setApplicationName(bankTransactionEvent.getSmartCardApplicationName());

        return financialTransactionResult;
    }

    /**
     * Возвращает фейковый идентификатор для новой транзакции.
     *
     * @return Идентификатор
     */
    private int getNewTransactionId() {
        TerminalDay terminalDay = currentTerminalDay;

        if (terminalDay != null) {
            terminalDay.setCurrentSaleTransactionId(terminalDay.getCurrentSaleTransactionId() + 1);
            getLocalDaoSession().getTerminalDayDao().update(terminalDay);
            refreshState();
        }

        return terminalDay == null ? 0 : terminalDay.getCurrentSaleTransactionId();
    }

    /**
     * Результат финансовой транзакции по умолчанию.
     * Используется для добавления события в БД перед началом транзакции.
     */
    private static class EmptyFinancialTransactionResult extends FinancialTransactionResult {

        private EmptyFinancialTransactionResult(int transactionId, int amount) {
            setId(transactionId);
            setTimeStamp(new Date());
            setApproved(false);
            setAmount(amount);
            setCurrencyCode("RUB");
            setReceipt(Collections.emptyList());
        }
    }

    /**
     * Заполняет {@link BankTransactionEvent} данными из {@link FinancialTransactionResult}
     *
     * @param initTransaction            Транзакция локальной БД
     * @param financialTransactionResult Транзакция в представлении {@link IPos}
     */
    private void fillBTCREFromFTR(@NonNull BankTransactionEvent initTransaction,
                                  @NonNull FinancialTransactionResult financialTransactionResult) {
        initTransaction.setTransactionId(financialTransactionResult.getId());
        initTransaction.setTransactionDateTime(financialTransactionResult.getTimeStamp());
        initTransaction.setOperationResult(financialTransactionResult.isApproved() ? BankOperationResult.Approved : BankOperationResult.Rejected);
        initTransaction.setTerminalNumber(financialTransactionResult.getTerminalId());
        initTransaction.setTotal(Decimals.divide(new BigDecimal(financialTransactionResult.getAmount()), Decimals.HUNDRED));
        initTransaction.setCardPan(financialTransactionResult.getCardPAN());
        initTransaction.setRrn(financialTransactionResult.getRRN());
        initTransaction.setMerchantId(financialTransactionResult.getMerchantId());
        initTransaction.setAuthorizationCode(financialTransactionResult.getAuthorizationId());
        initTransaction.setCardEmitentName(financialTransactionResult.getIssuerName());
        initTransaction.setCurrencyCode(financialTransactionResult.getCurrencyCode());
        initTransaction.setSmartCardApplicationName(financialTransactionResult.getApplicationName());
        initTransaction.setBankCheckNumber(financialTransactionResult.getInvoiceNumber());
    }

    /**
     * Подготавливае внешние ресурсы, необходимые для работы {@link IPos}
     *
     * @param resultHandler Колбек для уведомления об ошибке
     * @return {@code true} в случае успешного выполнения, {@code false иначе}
     */
    private <R extends TransactionResult> boolean prepareResources(ResultHandler<R> resultHandler) {
        if (resourcesPrepared) {
            return true;
        }
        try {
            getPos().prepareResources();
            resourcesPrepared = true;
            return true;
        } catch (PosException e) {
            Logger.error(TAG, e);
            PosOperationResult<R> operationResult = new PosOperationResult<>(null, null);
            resultHandler.onResult(operationResult);
        }
        return false;
    }

    /**
     * Освобождает внешние ресурсы, необходимые для работы {@link IPos}
     */
    private void freeResources() {
        try {
            getPos().freeResources();
        } catch (PosException e) {
            Logger.error(TAG, e);
        } finally {
            resourcesPrepared = false;
        }
    }

    /**
     * Проверяет состояние готовности POS-устройства к работе.
     *
     * @return {@code true} если готово, {@code false} иначе.
     */
    private boolean isPosReady() {
        return Globals.getPosTerminal().isReady();
    }

    /**
     * Абстрактная реализация колбека для операции {@link PosManager}
     */
    public static abstract class AbstractTransactionListener implements TransactionListener<TransactionResult> {

        @Override
        public void onConnectionTimeout() {

        }

        @Override
        public void onTickBeforeTimeout(long value) {

        }

        @Override
        public void onConnected() {

        }
    }

    /**
     * Абстрактная реализация колбека для операции {@link PosManager}
     */
    public static abstract class AbstractFinancialTransactionListener implements TransactionListener<FinancialTransactionResult> {

        @Override
        public void onConnectionTimeout() {

        }

        @Override
        public void onTickBeforeTimeout(long value) {

        }

        @Override
        public void onConnected() {

        }
    }

    /**
     * Колбека для операции {@link PosManager}
     */
    public interface TransactionListener<R extends TransactionResult> {

        /**
         * Уведомляет об оставшемся времени до прерывания попытки подключения по таймауту {@link #onConnectionTimeout()}.
         * Вызывается несколько раз.
         * Всегда срабатывает первым из всех колбэков.
         *
         * @param value Время до прерывания по таймауту, мс
         */
        void onTickBeforeTimeout(long value);

        /**
         * Уведомляет, что подключение к POS-устройству установлено
         * Срабатывает после {@link #onTickBeforeTimeout(long)}, если подключение не будет прервано по таймауту.
         * Иначе не срабатывает вообще.
         * Срабатывает до {@link #onResult(PosOperationResult)}
         */
        void onConnected();

        /**
         * Уведомляет, что подключение к POS-устройству прервано по таймауту.
         * Срабатывает после {@link #onTickBeforeTimeout(long)}, если подключение прервано по таймауту.
         * Иначе не срабатывает вообще.
         * {@link #onResult(PosOperationResult)} не будет вызван.
         */
        void onConnectionTimeout();

        /**
         * Возвращает результат транзакции.
         * Срабатывает только после {@link #onConnected()} в случае успешного подключения.
         *
         * @param result результат.
         */
        void onResult(@NonNull PosOperationResult<R> result);
    }
}
