package ru.ppr.ikkm.file.state.model;

/**
 * Состояние смены на принтере
 * Created by Артем on 21.01.2016.
 */
public enum ShiftState {
    OPEN(1), CLOSE(2);

    private final int code;

    ShiftState(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static ShiftState create(int code) {
        ShiftState[] states = values();
        for (ShiftState state : states) {
            if(code == state.code) {
                return state;
            }
        }
        return null;
    }
}
