package ru.ppr.cppk.localdb.model;

import android.support.annotation.Nullable;

/**
 * Результат выполнения операции на POS-устройстве.
 */
public enum BankOperationResult {
    /**
     * Отклонена
     */
    Rejected(0),
    /**
     * Проведена
     */
    Approved(1);

    /**
     * Код
     */
    private int code;

    BankOperationResult(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    @Nullable
    public static BankOperationResult valueOf(int code) {
        for (BankOperationResult type : BankOperationResult.values()) {
            if (type.getCode() == code) {
                return type;
            }
        }
        return null;
    }
}
