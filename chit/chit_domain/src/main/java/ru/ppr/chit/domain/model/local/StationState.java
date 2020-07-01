package ru.ppr.chit.domain.model.local;

/**
 * Статус станции
 * @author Dmitry Nevolin
 */
public enum StationState {

    /**
     * Актуальна, без изменений
     */
    ACTUAL(1),

    /**
     * Отменена
     */
    CANCELLED(2);

    private final int code;

    StationState(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static StationState valueOf(int code) {
        for (StationState stationState : StationState.values()) {
            if (stationState.getCode() == code) {
                return stationState;
            }
        }
        return null;
    }

}
