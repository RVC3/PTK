package ru.ppr.chit.api.entity;

import com.google.gson.annotations.SerializedName;

/**
 * Статус станции
 * @author Dmitry Nevolin
 */
public enum StationStateEntity {

    /**
     * Актуальна, без изменений
     */
    @SerializedName("1")
    ACTUAL(1),

    /**
     * Отменена
     */
    @SerializedName("2")
    CANCELLED(2);

    private final int code;

    StationStateEntity(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static StationStateEntity valueOf(int code) {
        for (StationStateEntity stationStateEntity : StationStateEntity.values()) {
            if (stationStateEntity.getCode() == code) {
                return stationStateEntity;
            }
        }
        return null;
    }

}
