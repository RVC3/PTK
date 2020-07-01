package ru.ppr.ipos;

import android.support.annotation.Nullable;

import ru.ppr.ipos.exception.PosException;
import ru.ppr.ipos.model.FinancialTransactionResult;
import ru.ppr.ipos.model.TransactionResult;

/**
 * Интерфейс POS-устройства
 *
 * @author Dmitry Nevolin
 */
public interface IPos {

    /**
     * Колбек для транзакций, выполняемых на POS-устройстве
     *
     * @param <T> Тип возращаемого результата
     */
    interface ResultListener<T extends TransactionResult> {
        /**
         * Возвращает результат транзакции
         *
         * @param result результат.
         */
        void onResult(@Nullable T result);
    }

    /**
     * Колбэк для отслеживания состояния подключения к POS-устройству
     */
    interface ConnectionListener {

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
         * Срабатывает до {@link #onDisconnected()}
         */
        void onConnected();

        /**
         * Уведомляет, что подключение к POS-устройству прервано по таймауту.
         * Срабатывает после {@link #onTickBeforeTimeout(long)}, если подключение прервано по таймауту.
         * Иначе не срабатывает вообще.
         * {@link #onDisconnected()} не будет вызван.
         */
        void onConnectionTimeout();

        /**
         * Уведомляет, что произведено отключение от POS-устройства.
         * Срабатывает толкьо после {@link #onConnected()} в случае успешного подключения.
         */
        void onDisconnected();
    }

    //<editor-fold desc="Методы взаимодействия с драйвером">

    /**
     * Подготавливает окружение драйвера (Например, включает Bluetooth)
     *
     * @throws PosException
     */
    void prepareResources() throws PosException;

    /**
     * Подготавливает окружение драйвера (Например, выключает Bluetooth)
     *
     * @throws PosException
     */
    void freeResources() throws PosException;

    /**
     * Метод взаимодействия с драйвером. Освобождает ресурсы, используемые драйвером.
     */
    void terminate();

    /**
     * Метод взаимодействия с драйвером. Если соединение с POS-устройством не было установлено, то по истечении
     * времени, заданного этим методом, на POS-устройство будет выслана команда перезагрузки
     *
     * @param connectionTimeout таймаут, мс.
     */
    void setConnectionTimeout(long connectionTimeout);

    /**
     * Метод взаимодействия с драйвером. Возвращает время таймаута.
     *
     * @return таймаут, мс.
     */
    long getConnectionTimeout();

    /**
     * Возвращает состояние готовности POS-утсройства к работе.
     *
     * @return {@code true} если готов, {@code false} иначе.
     */
    boolean isReady();

    /**
     * Метод взаимодействия с драйвером. Добавляет слушателя состояния соединения.
     *
     * @param connectionListener Слушатель
     */
    void addConnectionListener(ConnectionListener connectionListener);

    /**
     * Метод взаимодействия с драйвером. Удаляет слушателя состояния соединения.
     *
     * @param connectionListener Слушатель
     */
    void removeConnectionListener(ConnectionListener connectionListener);
    //</editor-fold>

    //<editor-fold desc="Методы взаимодействия с POS-устройством">

    /**
     * Метод взаимодействия с POS-устройством. Тест хоста.
     *
     * @param resultListener Колбек
     */
    void testHost(ResultListener<TransactionResult> resultListener);

    /**
     * Метод взаимодействия с POS-устройством. Тест POS-устройства.
     *
     * @param resultListener Колбек
     */
    void testSelf(ResultListener<TransactionResult> resultListener);

    /**
     * Метод взаимодействия с POS-устройством. Вызвает меню приложения.
     *
     * @param resultListener Колбек
     */
    void invokeApplicationMenu(ResultListener<TransactionResult> resultListener);

    /**
     * Метод взаимодействия с POS-устройством. Начать обновление софта.
     *
     * @param resultListener Колбек
     */
    void updateSoftware(ResultListener<TransactionResult> resultListener);

    /**
     * Метод взаимодействия с POS-устройством. Открыть день в POS-устройстве.
     *
     * @param resultListener Колбек
     */
    void openSession(ResultListener<TransactionResult> resultListener);

    /**
     * Метод взаимодействия с POS-устройством. Закрыть день в POS-устройстве.
     *
     * @param resultListener Колбек
     */
    void closeSession(ResultListener<TransactionResult> resultListener);

    /**
     * Метод взаимодействия с POS-устройством. Журнал транзакций.
     *
     * @param resultListener Колбек
     */
    void getTransactionsJournal(ResultListener<TransactionResult> resultListener);

    /**
     * Метод взаимодействия с POS-устройством. Сводка по транзакциям.
     *
     * @param resultListener Колбек
     */
    void getTransactionsTotal(ResultListener<TransactionResult> resultListener);

    /**
     * Метод взаимодействия с POS-устройством. Операция продажи.
     *
     * @param price             Цена в копейках
     * @param saleTransactionId Локальнй идентификатор транзакции
     * @param resultListener    Колбек
     */
    void sale(int price, int saleTransactionId, ResultListener<FinancialTransactionResult> resultListener);

    /**
     * Метод взаимодействия с POS-устройством. Делает отмену транзакции в рамках дня.
     */
    void cancel(FinancialTransactionResult saleResult, ResultListener<FinancialTransactionResult> resultListener);

    /**
     * Метод взаимодействия с POS-устройством. Техническая отмена последней, проведенной транзакции.
     * Использовать с осторожностью.
     * Допускается использовать данный метод только в случае, когда мы точно знаем, что серверу известно о последней транзакции.
     * Иначе следует отменять транзакцию по идентификатору {@link #cancel(FinancialTransactionResult, ResultListener)}
     *
     * @param resultListener Колбек
     */
    void technicalCancel(ResultListener<FinancialTransactionResult> resultListener);
    //</editor-fold>

}
