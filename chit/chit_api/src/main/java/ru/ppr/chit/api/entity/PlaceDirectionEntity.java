package ru.ppr.chit.api.entity;

import com.google.gson.annotations.SerializedName;

/**
 * @author Dmitry Nevolin
 */
public enum PlaceDirectionEntity {

    /**
     * По ходу движения поезда.
     */
    @SerializedName("1")
    FORWARD(1),

    /**
     * Против хода движения поезда.
     */
    @SerializedName("2")
    BACKWARD(2),

    /**
     * Поворотное.
     */
    @SerializedName("3")
    REVERSIBLE(3),

    /**
     * Боковое слева.
     */
    @SerializedName("4")
    SIDE_LEFT(4),

    /**
     * Боковое справа.
     */
    @SerializedName("5")
    SIDE_RIGHT(5);

    private final int code;

    PlaceDirectionEntity(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static PlaceDirectionEntity valueOf(int code) {
        for (PlaceDirectionEntity placeDirection : PlaceDirectionEntity.values()) {
            if (placeDirection.getCode() == code) {
                return placeDirection;
            }
        }
        return null;
    }

}
