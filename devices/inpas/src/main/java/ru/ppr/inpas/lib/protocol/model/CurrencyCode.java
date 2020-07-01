package ru.ppr.inpas.lib.protocol.model;

import android.support.annotation.NonNull;

/**
 * Поддерживаемые коды валют.
 * Буквенный код российского рубля в стандарте ISO 4217 — RUB,
 * RUB(643), RUR(810)
 */
public enum CurrencyCode {
    RUB(643, "RUB"),
    RUR(810, "RUR");

    private final int value;
    private final String code;

    CurrencyCode(final int value, @NonNull final String code) {
        this.value = value;
        this.code = code;
    }

    /**
     * Метод для получения цифрового кода валюты.
     *
     * @return код валюты.
     */
    public int getDigitalCode() {
        return value;
    }

    /**
     * Метод для получения буквенного кода валюты.
     *
     * @return код валюты.
     */
    @NonNull
    public String getLetterCode() {
        return code;
    }

}
