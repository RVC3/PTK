package ru.ppr.cppk.model;

import android.support.annotation.Nullable;

import ru.ppr.cppk.managers.PosManager;
import ru.ppr.ipos.model.TransactionResult;

/**
 * Результат выполнения операции {@link PosManager}
 *
 * @author Dmitry Vinogradov
 */
public class PosOperationResult<R extends TransactionResult> {
    /**
     * Результат транзакции
     */
    private final R transactionResult;
    /**
     * Сообщение об ошибке
     */
    private final String errorMessage;

    public PosOperationResult(R transactionResult, String errorMessage) {
        this.transactionResult = transactionResult;
        this.errorMessage = errorMessage;
    }

    @Nullable
    public R getTransactionResult() {
        return transactionResult;
    }

    @Nullable
    public String getErrorMessage() {
        return errorMessage;
    }
}
