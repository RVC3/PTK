package ru.ppr.ingenico.core;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import ru.ppr.ingenico.model.operations.Operation;
import ru.ppr.ingenico.model.requests.Request;
import ru.ppr.ingenico.model.responses.Response;
import ru.ppr.ingenico.utils.Arcus2Utils;
import ru.ppr.ipos.model.TransactionResult;
import ru.ppr.logger.Logger;
import ru.ppr.utils.CommonUtils;
import ru.ppr.utils.Executors;

/**
 * Исполнитель операций по протоколу Arcus2
 *
 * @author Aleksandr Brazhkin
 */
class Arcus2Server {

    private static final String TAG = Logger.makeLogTag(Arcus2Server.class);

    /**
     * Перерыв, который необходимо сделать после ответа на  ping прежде, чем слать операцию.
     * Выявлено практическим путем, иначе начинается хаос: байты ping'а и байты операции перемешиваются.
     */
    private static final int PING_DELAY = 200;
    /**
     * Порт для запуска Arcus2 сервера
     */
    private final int port;
    /**
     * {@link Executor} для запуска операций в отдельном потоке.
     */
    private final ScheduledExecutorService mScheduledExecutorService;
    /**
     * {@link Executor} для запуска операций в отдельном потоке.
     */
    private final ExecutorService mExecutorService;
    /**
     * {@link Future} для прерывания операции по таймауту
     */
    private Future timeoutFuture;
    /**
     * {@link Future} для прерывания операции по таймауту
     */
    private Future operationFuture;
    /**
     * Флаг, что операция выполняется
     */
    private volatile boolean running = false;
    /**
     * Флаг, что операция прервана
     */
    private boolean interrupted;
    /**
     * Сокет сервера
     */
    private ServerSocket serverSocket = null;
    /**
     * Флаг, что находится в состоянии {@link ServerSocket#accept()}
     */
    private volatile boolean inAcceptState;
    /**
     * Объект для синхронизации выполнения кода
     */
    private final Object lock = new Object();

    public Arcus2Server(int port, ScheduledExecutorService scheduledExecutorService) {
        this.port = port;
        this.mExecutorService = Executors.newLoggableSingleThreadExecutor(TAG, r -> new Thread(r, "IngenicoArcusRunner"));
        this.mScheduledExecutorService = scheduledExecutorService;
    }

    /**
     * Освобождение всех ресурсов, связанных с объектом
     */
    void terminate() {
        synchronized (lock) {
            if (running) {
                stop();
            }
            mExecutorService.shutdown();
        }
    }

    /**
     * Запускает выполнение операции
     *
     * @param operation Операция
     * @param callback  Колбек
     * @param <R>       Тип результата
     */
    <R extends TransactionResult> void run(Operation<R> operation, Callback<R> callback) {
        synchronized (lock) {
            if (running) {
                throw new IllegalStateException("Operation is already running");
            }
            running = true;
            interrupted = false;
            operationFuture = mExecutorService.submit(() -> runInternal(operation, callback));
        }
    }

    /**
     * Запускает выполнение операции
     *
     * @param operation Операция
     * @param callback  Колбек
     * @param <R>       Тип результата
     */
    private <R extends TransactionResult> void runInternal(Operation<R> operation, Callback<R> callback) {

        Logger.trace(TAG, "runOnExecutor start, operation = " + operation.getClass().getSimpleName());

        Socket clientSocket = null;
        InputStream inputStream = null;
        OutputStream outputStream = null;

        try {
            if (interrupted) {
                Logger.trace(TAG, "interrupted at start, callback.onError");
                callback.onError();
                return;
            }

            synchronized (lock) {
                if (!interrupted) {
                    serverSocket = new ServerSocket(port);
                    // Ожидание клиента - это лишь часть процесса подключения.
                    // Общий таймаут контролируется извне, поэтому
                    // установим таймаут в 0, т.е. неограниченный.
                    serverSocket.setSoTimeout(0);
                    // Пытаемся установить подключение к терминалу
                    inAcceptState = true;
                    Logger.trace(TAG, "serverSocket created");
                } else {
                    Logger.trace(TAG, "serverSocket NOT created");
                }
            }

            if (serverSocket != null) {
                Logger.trace(TAG, "serverSocket.accept()");
                clientSocket = serverSocket.accept();

                synchronized (lock) {
                    Logger.trace(TAG, "clientSocket accepted");
                    inAcceptState = false;
                }
            }

            if (interrupted) {
                Logger.trace(TAG, "interrupted after client socket, callback.onError");
                callback.onError();
                return;
            }

            Logger.trace(TAG, "callback.onConnected");
            callback.onConnected();

            inputStream = new DataInputStream(clientSocket.getInputStream());
            outputStream = new DataOutputStream(clientSocket.getOutputStream());

            // Флаг, что мы ответили на пинг терминала
            boolean pinged = false;
            // Флаг, что операция запущена
            boolean operationStarted = false;

            // Стартуем таймер на ожидании момента, когда терминал пришлет пинг и мы на него ответим
            restartTimer(operation.getTimeout());

            while (!interrupted && clientSocket.isConnected() && !clientSocket.isClosed()) {

                ByteArrayOutputStream cache = new ByteArrayOutputStream();
                if (inputStream.available() > 0) {
                    // Терминал что-то шлет, будем читать
                    while (inputStream.available() > 0) {
                        cache.write(inputStream.read());
                    }
                    byte[] cacheBytes = cache.toByteArray();
                    Logger.trace(TAG, "cacheBytes = " + CommonUtils.bytesToHexWithSpaces(cacheBytes));

                    if (Arcus2Utils.isPing(cacheBytes)) {
                        outputStream.write(Arcus2Utils.getPing());
                        pinged = true;
                        try {
                            Thread.sleep(PING_DELAY);
                        } catch (InterruptedException e) {
                            Logger.trace(TAG, "ping sleep interrupted, break");
                            Thread.currentThread().interrupt();
                            break;
                        }
                    } else {
                        // Стартуем таймер в ожидании нового запроса от терминала
                        restartTimer(operation.getTimeout());

                        Request.Body requestBody = Request.getBody(cacheBytes);

                        Logger.trace(TAG, "requestBody = " + requestBody.getType().name());
                        Logger.trace(TAG, "terminal request: " + requestBody.getType().name());

                        Response tmp = operation.makeResponse(requestBody);

                        byte[] response = tmp.packSelf();

                        Logger.trace(TAG, "response = " + CommonUtils.bytesToHexWithSpaces(response));
                        Logger.trace(TAG, "ptk response: " + tmp.toString());

                        outputStream.write(response);

                        if (operation.isCompleted()) {
                            Logger.trace(TAG, "operation isCompleted, break");
                            break;
                        }
                    }

                } else {
                    // Нечего читать с терминала в данный момент
                    if (pinged) {
                        if (!operationStarted) {
                            // Попробуем мы выполнить операцию на терминале, т.к. ping уже прошел
                            byte[] operationBytes = operation.packSelf();
                            Logger.trace(TAG, "operationBytes = " + CommonUtils.bytesToHexWithSpaces(operationBytes));
                            outputStream.write(operationBytes);

                            operationStarted = true;
                        }
                    }
                }
            }

            synchronized (lock) {
                if (timeoutFuture != null) {
                    timeoutFuture.cancel(false);
                    timeoutFuture = null;
                    Logger.trace(TAG, "timeoutFuture canceled");
                }
            }

            if (operation.isCompleted()) {
                R transactionResult = operation.getResult();
                synchronized (lock) {
                    Logger.trace(TAG, "running = false");
                    running = false;
                }
                Logger.trace(TAG, "callback.onResult");
                callback.onResult(transactionResult);
                return;
            }
            synchronized (lock) {
                Logger.trace(TAG, "running = false");
                running = false;
            }
            Logger.trace(TAG, "callback.onError");
            callback.onError();
            return;
        } catch (IOException e) {
            Logger.error(TAG, e);
            synchronized (lock) {
                inAcceptState = false;
                running = false;
            }
            callback.onError();
            return;
        } finally {
            synchronized (lock) {
                if (serverSocket != null) {
                    try {
                        serverSocket.close();
                        serverSocket = null;
                    } catch (IOException e) {
                        // only log
                        Logger.error(TAG, e);
                    }
                }
            }
            if (clientSocket != null) {
                try {
                    clientSocket.close();
                } catch (IOException e) {
                    // only log
                    Logger.error(TAG, e);
                }
            }
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    // only log
                    Logger.error(TAG, e);
                }
            }
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    // only log
                    Logger.error(TAG, e);
                }
            }
        }
    }

    /**
     * Останавливает выполнение текущей операции.
     */
    void stop() {
        Logger.trace(TAG, "stop outer");
        synchronized (lock) {
            Logger.trace(TAG, "stop synchronized");

            interrupted = true;

            if (operationFuture != null) {
                operationFuture.cancel(true);
                operationFuture = null;
                Logger.trace(TAG, "stop, operationFuture canceled");
            }

            if (timeoutFuture != null) {
                timeoutFuture.cancel(false);
                timeoutFuture = null;
                Logger.trace(TAG, "stop, timeoutFuture canceled");
            }

            if (inAcceptState) {
                Logger.trace(TAG, "stop, inAcceptState");
                // Закрываем сокет только если нужно прервать выполнение serverSocket.accept(),
                // иначе даем завершиться функции корректно.
                if (serverSocket != null) {
                    try {
                        serverSocket.close();
                        // Дабы не словить NPE на serverSocket.accept()
                        // Ссылка очистится внутри finally
                        // serverSocket = null;
                    } catch (IOException e) {
                        // only log
                        Logger.error(TAG, e);
                    }
                }
            }
        }
    }

    /**
     * Откладывает {@link Future} для прерывания операции по таймауту
     *
     * @param timeout время, на которое нужно отложить прерывание, мс
     */
    private void restartTimer(long timeout) {
        Logger.trace(TAG, "restartTimer, timeout = " + timeout);
        synchronized (lock) {
            if (timeoutFuture != null) {
                Logger.trace(TAG, "restartTimer, timeoutFuture canceled");
                timeoutFuture.cancel(false);
            }
            timeoutFuture = mScheduledExecutorService.schedule(() -> {
                stop();
            }, timeout, TimeUnit.MILLISECONDS);
        }
    }

    /**
     * Колбек для выполнения операций
     *
     * @param <R> Тип результата
     */
    interface Callback<R extends TransactionResult> {

        /**
         * Подключение установлено
         */
        void onConnected();

        /**
         * Получен результат
         *
         * @param result Результат
         */
        void onResult(R result);

        /**
         * Произошла ошибка
         */
        void onError();
    }
}
