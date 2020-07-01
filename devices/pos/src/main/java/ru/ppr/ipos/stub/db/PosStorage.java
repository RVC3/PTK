package ru.ppr.ipos.stub.db;

import android.support.annotation.NonNull;

import ru.ppr.ipos.model.PosDay;
import ru.ppr.ipos.model.PosProperty;
import ru.ppr.ipos.model.Transaction;

/**
 * Предоставляет интерфейс для сохранения данных PosStub-терминала в хранилище.
 *
 * @author Dmitry Vinogradov
 */
public interface PosStorage {
    /**
     * Производит открытие хранилища PosStub-терминала
     */
    void openConnection();

    /**
     * Производит закрытие хранилища PosStub-терминала
     */
    void closeConnection();

    /**
     * Возвращает локальный Id последней транзакции Sale из таблицы PosProperty по ключу {@link PosProperty.Keys#SaleTransactionId}
     */
    int getLastLocalSaleTransactionId();

    /**
     * Сохраняет локальный Id последней транзакции Sale в таблице PosProperty по ключу {@link PosProperty.Keys#SaleTransactionId}
     */
    void setLastLocalSaleTransactionId(@NonNull PosProperty entity);

    /**
     * Возвращает последнюю транзакцию заданного типа из таблицы Transaction
     */
    Transaction getLastTransaction(Transaction.TransactionType bankOperationType);

    /**
     * Возвращает максимальный ID по всем транзакциям из таблицы Transaction
     */
    int getMaxTransactionId();

    /**
     * Сохраняет новую транзакцию в таблице Transaction
     */
    void saveNewTransaction(@NonNull Transaction entity);

    /**
     * Возвращает транзакцию заданного типа по заданному ID транзакции из таблицы Transaction
     */
    Transaction getTransaction(int transactionId, Transaction.TransactionType bankOperationType);

    /**
     * Возвращает последний день на stub POS-терминале из таблицы PosDay
     */
    PosDay getLastPosDay();

    /**
     * Сохраняет новый день на stub POS-терминале в таблице PosDay
     */
    void saveNewPosDay(@NonNull PosDay entity);

    /**
     * Обновляет значения дня на stub POS-терминале в таблице PosDay
     */
    void updatePosDay(@NonNull PosDay entity);

}
