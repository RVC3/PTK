package ru.ppr.inpas.lib.parser.model;

import android.support.annotation.NonNull;

/**
 * Поддерживаемы теги в образе чека.
 */
public enum ReceiptTag {
    UNKNOWN(""),
    CASHIER_RECEIPT("0xDA"), // Чек кассира.
    CARD_TYPE("0xDE"), // Наименование типа карты.
    RECEIPT("0xDF"); // Чек клиента, Отказной чек, Сверка итогов.

    final String mValue;

    ReceiptTag(@NonNull final String value) {
        mValue = value;
    }

    @NonNull
    public String getValue() {
        return mValue;
    }

}