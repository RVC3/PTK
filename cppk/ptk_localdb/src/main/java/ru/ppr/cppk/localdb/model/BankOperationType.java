package ru.ppr.cppk.localdb.model;

import android.support.annotation.Nullable;

/**
 * Тип банковской транзакции.
 *
 * @author Grigoriy Kashka
 */
public enum BankOperationType {
    /**
     * Продажа
     */
    SALE(1),
    /**
     * Аннулирование
     */
    CANCELLATION(2),
    /**
     * Возврат
     */
    RETURN(3);

    /**
     * Код
     */
    private final int code;

    BankOperationType(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    @Nullable
    public static BankOperationType valueOf(int code) {
        for (BankOperationType type : BankOperationType.values()) {
            if (type.getCode() == code) {
                return type;
            }
        }
        return null;
    }
}
