package ru.ppr.inpas.lib.protocol.model;

/**
 * Способ ввода карты.
 */
public enum CardInputMode {

    /**
     * Ввод карты через POS-терминал или пинпад.
     */
    POS_TERMINAL_OR_PINPAD(3);

    private final int mValue;

    CardInputMode(final int value) {
        mValue = value;
    }

    public int getValue() {
        return mValue;
    }

}
