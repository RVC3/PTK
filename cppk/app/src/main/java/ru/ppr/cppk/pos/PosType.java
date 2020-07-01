package ru.ppr.cppk.pos;

import ru.ppr.cppk.BuildConfig;

/**
 * Тип POS терминала.
 */
public enum PosType {
    DEFAULT(0),
    INGENICO(1),
    INPAS(2),
    BUILTIN(3);

    private final int value;

    PosType(final int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static PosType from(final int value) {
        PosType posType = BuildConfig.DEBUG ? DEFAULT : INGENICO;

        for (PosType type : PosType.values()) {
            if (type.getValue() == value) {
                posType = type;
                break;
            }
        }

        return posType;
    }

}