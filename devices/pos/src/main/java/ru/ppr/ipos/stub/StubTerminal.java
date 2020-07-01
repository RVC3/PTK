package ru.ppr.ipos.stub;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import ru.ppr.ipos.IPos;
import ru.ppr.ipos.exception.PosException;
import ru.ppr.ipos.model.FakeTransactionResult;
import ru.ppr.ipos.model.FinancialTransactionResult;
import ru.ppr.ipos.model.PosDay;
import ru.ppr.ipos.model.Transaction;
import ru.ppr.ipos.model.TransactionResult;
import ru.ppr.ipos.stub.db.PosStorage;
import ru.ppr.logger.Logger;
import ru.ppr.logger.LoggerAspect;
import ru.ppr.utils.Executors;


/**
 * Stub-реализация {@link IPos}.
 *
 * @author Aleksandr Brazhkin
 */
@LoggerAspect.IncludeClass
public class StubTerminal implements IPos {

    private static final String TAG = Logger.makeLogTag(StubTerminal.class);

    /**
     * Интревал тика при подключении, мс
     */
    private static final long TICK_INTERVAL = 1000;

    private final Context mContext;
    /**
     * Хранилище необходимых данных
     */
    private final PosStorage posStorage;
    /**
     * {@link Executor}  для запуска операций в отдельном потоке.
     */
    private final ExecutorService mExecutorService;
    /**
     * {@link Executor}  для запуска операций в отдельном потоке.
     */
    private final ExecutorService mClientExecutorService;
    /**
     * Флаг, что операция ещё выполняется
     */
    private volatile boolean running;
    /**
     * Объект для синхронизации выполнения кода
     */
    private final Object lock = new Object();
    /**
     * Таймаут подключения к терминалу
     */
    private long connectionTimeout;
    /**
     * Оставшееся время до выброса {@link ConnectionListener#onConnectionTimeout()}
     */
    private long currentTickValue;
    /**
     * {@link Future} для прерывания операции по таймауту
     */
    private Future transactionFuture;
    /**
     * Слушатели состояния подключения
     */
    private List<ConnectionListener> connectionListeners = new ArrayList<>();

    public StubTerminal(Context context, int connectionTimeout, PosStorage posStorage) {
        this.mContext = context.getApplicationContext();
        this.connectionTimeout = connectionTimeout;
        this.mExecutorService = Executors.newLoggableSingleThreadExecutor("StubTerminalExecutor", r -> new Thread(r, "StubTerminalExecutor"));
        this.mClientExecutorService = Executors.newLoggableSingleThreadExecutor("StubTerminalClientExecutor", r -> new Thread(r, "StubTerminalClientExecutor"));
        this.posStorage = posStorage;
    }

    /**
     * Запускает выполнение операции
     *
     * @param operation      Операция
     * @param resultListener Колбек
     * @param <R>            Тип результата
     */
    private <R extends TransactionResult> void doTransactionOnExecutor(Operation<R> operation, final ResultListener<R> resultListener) {
        synchronized (lock) {
            if (running) {
                throw new IllegalStateException("Operation is already running");
            }
            transactionFuture = mExecutorService.submit(() -> doTransaction(operation, resultListener));
            running = true;
        }
    }

    /**
     * Запускает выполнение операции
     *
     * @param operation      Операция
     * @param resultListener Колбек
     * @param <R>            Тип результата
     */
    private <R extends TransactionResult> void doTransaction(Operation<R> operation, final ResultListener<R> resultListener) {
        Logger.trace(TAG, "doTransaction start");
        currentTickValue = connectionTimeout;
        while (currentTickValue > 0) {
            long localCurrentTickValue = currentTickValue;
            mClientExecutorService.execute(() -> onTickBeforeTimeout(localCurrentTickValue));
            try {
                Thread.sleep(TICK_INTERVAL);
                currentTickValue -= TICK_INTERVAL;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                stopTransaction();
                mClientExecutorService.execute(() -> resultListener.onResult(null));
                return;
            }
        }
        posStorage.openConnection();
        mClientExecutorService.execute(() -> onConnected());
        R successResult = operation.call();
        stopTransaction();
        posStorage.closeConnection();
        mClientExecutorService.execute(() -> onDisconnected());
        mClientExecutorService.execute(() -> resultListener.onResult(successResult));
        Logger.trace(TAG, "doTransaction end");
    }

    /**
     * Останавливает выполнение текущей операции.
     */
    private void stopTransaction() {
        Logger.trace(TAG, "stopTransaction outer");
        synchronized (lock) {
            Logger.trace(TAG, "stopTransaction start");
            if (!running) {
                Logger.trace(TAG, "not running!");
                Logger.trace(TAG, "stopTransaction end");
                return;
            }

            if (transactionFuture != null) {
                transactionFuture.cancel(true);
                transactionFuture = null;
                Logger.trace(TAG, "transactionFuture canceled");
            }
            running = false;
            Logger.trace(TAG, "stopTransaction end");
        }
    }

    private void onConnected() {
        Logger.trace(TAG, "onConnected");
        for (ConnectionListener connectionListener : connectionListeners)
            connectionListener.onConnected();
    }

    private void onTickBeforeTimeout(long value) {
        Logger.trace(TAG, "onTickBeforeTimeout, value = " + value);
        for (ConnectionListener connectionListener : connectionListeners)
            connectionListener.onTickBeforeTimeout(value);
    }

    private void onDisconnected() {
        Logger.trace(TAG, "onDisconnected");
        for (ConnectionListener connectionListener : connectionListeners)
            connectionListener.onDisconnected();
    }

    private int getNewId() {

        Transaction lastTransaction = posStorage.getLastTransaction(Transaction.TransactionType.ALL);
        int lastId;
        if (lastTransaction != null) {
            lastId = lastTransaction.getId();
        } else {
            lastId = -1;
        }
        int newId = lastId + 1;
        return newId;
    }

    private int getNewTransactionId() {
        int lastTransactionId = posStorage.getMaxTransactionId();
        if (lastTransactionId == -1) {
            lastTransactionId = 0; // TransactionId не может быть равен 0, поэтому начинаем нумерацию с 1
        }
        int newTransactionId = lastTransactionId + 1;
        return newTransactionId;
    }

    @Override
    public void prepareResources() throws PosException {
        // NOP
    }

    @Override
    public void freeResources() throws PosException {
        // NOP
    }

    @Override
    public void terminate() {
        Logger.trace(TAG, "terminate, outer");
        Future<Boolean> future = mExecutorService.submit(() -> {
            synchronized (lock) {
                Logger.trace(TAG, "terminate start synchronized");
                if (running) {
                    Logger.trace(TAG, "terminate, running, stopTransaction");
                    stopTransaction();
                }
                mExecutorService.shutdown();
                mClientExecutorService.shutdown();
                Logger.trace(TAG, "terminate end synchronized");
                return true;
            }
        });

        try {
            Logger.trace(TAG, "terminate future.get() start");
            future.get();
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
        Operation<TransactionResult> operation = () -> {
            TransactionResult transactionResult = new FakeTransactionResult(getNewId());
            return transactionResult;
        };
        doTransactionOnExecutor(operation, resultListener);
    }

    @Override
    public void testSelf(ResultListener<TransactionResult> resultListener) {
        Operation<TransactionResult> operation = () -> {
            TransactionResult transactionResult = new FakeTransactionResult(getNewId());
            return transactionResult;
        };
        doTransactionOnExecutor(operation, resultListener);
    }

    @Override
    public void invokeApplicationMenu(ResultListener<TransactionResult> resultListener) {
        Operation<TransactionResult> operation = () -> {
            TransactionResult transactionResult = new FakeTransactionResult(getNewId());
            return transactionResult;
        };
        doTransactionOnExecutor(operation, resultListener);
    }

    @Override
    public void updateSoftware(ResultListener<TransactionResult> resultListener) {
        Operation<TransactionResult> operation = () -> {
            TransactionResult transactionResult = new FakeTransactionResult(getNewId());
            return transactionResult;
        };
        doTransactionOnExecutor(operation, resultListener);
    }

    @Override
    public void openSession(ResultListener<TransactionResult> resultListener) {

        Operation<TransactionResult> operation = () -> {

            if (!posDayIsClosed()) {
                closePosDay();
            }
            openNewPosDay();

            TransactionResult transactionResult = new FakeTransactionResult(getNewId());
            return transactionResult;
        };
        doTransactionOnExecutor(operation, resultListener);
    }

    @Override
    public void closeSession(ResultListener<TransactionResult> resultListener) {

        Operation<TransactionResult> operation = () -> {
            if (posDayIsClosed()) {
                openNewPosDay();
            }
            closePosDay();

            TransactionResult transactionResult = new FakeTransactionResult(getNewId());
            return transactionResult;
        };
        doTransactionOnExecutor(operation, resultListener);
    }

    @Override
    public void getTransactionsJournal(ResultListener<TransactionResult> resultListener) {

        // В будущем: реализовать метод с поведением, необходимым для нужд стаба
        // Пока что он просто возвращает заглушку
        Operation<TransactionResult> operation = () -> {
            TransactionResult transactionResult = new FakeTransactionResult(getNewId());
            return transactionResult;
        };
        doTransactionOnExecutor(operation, resultListener);
    }

    @Override
    public void getTransactionsTotal(ResultListener<TransactionResult> resultListener) {
        // В будущем: реализовать метод с поведением, необходимым для нужд стаба
        // Пока что он просто возвращает заглушку
        Operation<TransactionResult> operation = () -> {
            TransactionResult transactionResult = new FakeTransactionResult(getNewId());
            return transactionResult;
        };
        doTransactionOnExecutor(operation, resultListener);
    }

    @Override
    public void sale(int price, int saleTransactionId, ResultListener<FinancialTransactionResult> resultListener) {

        // В будущем: сохранение saleTransactionId в таблицу Properties в БД стаба ?

        Operation<FinancialTransactionResult> operation = () -> {
            int id = getNewId();
            int transactionId = getNewTransactionId();
            Transaction saleTransaction = new Transaction(id, transactionId, price);

            PosDay posDay;
            if (posDayIsClosed()) {
                posDay = openNewPosDay();
            } else {
                posDay = posStorage.getLastPosDay();
            }
            saleTransaction.setDayId(posDay.getId());
            saleTransaction.setBankOperationType(Transaction.TransactionType.SALE);
            saleTransaction.setBankResponse("sale BankResponse");
            posStorage.saveNewTransaction(saleTransaction);

            FinancialTransactionResult financialTransactionResult = getFinancialTransactionResultFromFake(saleTransaction);
            return financialTransactionResult;
        };
        doTransactionOnExecutor(operation, resultListener);
    }

    @Override
    public void cancel(FinancialTransactionResult saleResult, ResultListener<FinancialTransactionResult> resultListener) {

        Operation<FinancialTransactionResult> operation = () -> {

            if (posDayIsClosed()) {
                return null;
            } else {
                int transactionId = saleResult.getId();
                Transaction saleTransaction = posStorage.getTransaction(transactionId, Transaction.TransactionType.SALE);
                Transaction prevCancelTransaction = posStorage.getTransaction(transactionId, Transaction.TransactionType.CANCELLATION);
                if (saleTransaction != null && prevCancelTransaction == null) {

                    PosDay posDay = posStorage.getLastPosDay();
                    if (posDay.getId() == saleTransaction.getDayId()) {
                        int id = getNewId();
                        Transaction cancelTransaction = new Transaction(id, transactionId, saleResult.getAmount());
                        cancelTransaction.setDayId(posDay.getId());
                        cancelTransaction.setBankOperationType(Transaction.TransactionType.CANCELLATION);
                        cancelTransaction.setBankResponse("cancel BankResponse");
                        posStorage.saveNewTransaction(cancelTransaction);

                        FinancialTransactionResult financialTransactionResult = getFinancialTransactionResultFromFake(cancelTransaction);
                        return financialTransactionResult;
                    } else {
                        return null;
                    }
                } else {
                    return null;
                }
            }
        };
        doTransactionOnExecutor(operation, resultListener);
    }

    @Override
    public void technicalCancel(ResultListener<FinancialTransactionResult> resultListener) {

        // Судя по всему метод не вызывается (вызовы есть только в Ingenico). Протестировать не удалось
        Operation<FinancialTransactionResult> operation = () -> {

            if (posDayIsClosed()) {
                return null;
            } else {
                Transaction lastSaleTransaction = posStorage.getLastTransaction(Transaction.TransactionType.SALE);
                int transactionId = lastSaleTransaction.getTransactionId();

                Transaction prevCancelTransaction = posStorage.getTransaction(transactionId, Transaction.TransactionType.CANCELLATION);
                if (lastSaleTransaction != null && prevCancelTransaction == null) {

                    PosDay posDay = posStorage.getLastPosDay();
                    if (posDay.getId() == lastSaleTransaction.getDayId()) {
                        int id = getNewId();
                        Transaction cancelTransaction = new Transaction(id, transactionId, lastSaleTransaction.getAmount());
                        cancelTransaction.setDayId(posDay.getId());
                        cancelTransaction.setBankOperationType(Transaction.TransactionType.CANCELLATION);
                        cancelTransaction.setBankResponse("cancel BankResponse");
                        posStorage.saveNewTransaction(cancelTransaction);

                        FinancialTransactionResult financialTransactionResult = getFinancialTransactionResultFromFake(cancelTransaction);
                        return financialTransactionResult;
                    } else {
                        return null;
                    }
                } else {
                    return null;
                }
            }
        };
        doTransactionOnExecutor(operation, resultListener);
    }

    private FinancialTransactionResult getFinancialTransactionResultFromFake(Transaction fakeTransaction) {

        FinancialTransactionResult financialTransactionResult = new FinancialTransactionResult();

        financialTransactionResult.setId(fakeTransaction.getTransactionId());
        financialTransactionResult.setTimeStamp(fakeTransaction.getTimeStamp());
        financialTransactionResult.setApproved(fakeTransaction.isApproved());
        financialTransactionResult.setTerminalId(fakeTransaction.getTerminalId());
        financialTransactionResult.setInvoiceNumber(fakeTransaction.getInvoiceNumber());
        financialTransactionResult.setReceipt(fakeTransaction.getReceipt());
        financialTransactionResult.setBankResponseCode(fakeTransaction.getBankResponseCode());
        financialTransactionResult.setBankResponse(fakeTransaction.getBankResponse());
        financialTransactionResult.setAmount(fakeTransaction.getAmount());
        financialTransactionResult.setCardPAN(fakeTransaction.getCardPAN());
        financialTransactionResult.setRRN(fakeTransaction.getRRN());
        financialTransactionResult.setMerchantId(fakeTransaction.getMerchantId());
        financialTransactionResult.setAuthorizationId(fakeTransaction.getAuthorizationId());
        financialTransactionResult.setIssuerName(fakeTransaction.getIssuerName());
        financialTransactionResult.setCurrencyCode(fakeTransaction.getCurrencyCode());
        financialTransactionResult.setApplicationName(fakeTransaction.getApplicationName());

        return financialTransactionResult;
    }

    private boolean posDayIsClosed() {
        PosDay posDay = posStorage.getLastPosDay();
        if (posDay == null) {
            return true;
        } else {
            return posDay.isClosed();
        }
    }

    private PosDay openNewPosDay() {
        PosDay lastPosDay = posStorage.getLastPosDay();

        PosDay posDay = new PosDay();
        int posDayId;
        if (lastPosDay == null) {
            posDayId = 0;
        } else {
            posDayId = lastPosDay.getId() + 1;
        }
        posDay.setId(posDayId);
        posDay.setClosed(false);
        posStorage.saveNewPosDay(posDay);

        return posDay;
    }

    private void closePosDay() {

        PosDay posDay = posStorage.getLastPosDay();
        if (posDay != null) {
            posDay.setClosed(true);
            posStorage.updatePosDay(posDay);
        }
    }

    interface Operation<R> {
        R call();
    }
}
