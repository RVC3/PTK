package ru.ppr.cppk.localdb.model;

import android.support.annotation.NonNull;

/**
 * Тип оплаты.
 * На ПТК используется только {@link #INDIVIDUAL_CASH} и {@link #INDIVIDUAL_BANK_CARD}.
 *
 * @author Grigoriy Kashka
 */
public enum PaymentType {
    /**
     * Наличный расчет физического лица
     */
    INDIVIDUAL_CASH(0),
    /**
     * Расчет банковской картой физического лица
     */
    INDIVIDUAL_BANK_CARD(1),
    /**
     * Безналичный расчет юридического лица
     */
    LEGAL_ENTITY_CASH_LESS(2),
    /**
     * Расчет через платежные системы
     */
    PAYMENT_SYSTEM(3);

    /**
     * Код
     */
    private int code;

    PaymentType(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    @NonNull
    public static PaymentType valueOf(int code) {
        for (PaymentType type : values()) {
            if (type.getCode() == code) {
                return type;
            }
        }
        return PaymentType.INDIVIDUAL_CASH;
    }
}
