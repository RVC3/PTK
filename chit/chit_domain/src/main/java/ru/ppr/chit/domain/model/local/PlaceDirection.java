package ru.ppr.chit.domain.model.local;

/**
 * @author Dmitry Nevolin
 */
public enum PlaceDirection {

    /**
     * По ходу движения поезда.
     */
    FORWARD(1),

    /**
     * Против хода движения поезда.
     */
    BACKWARD(2),

    /**
     * Поворотное.
     */
    REVERSIBLE(3),

    /**
     * Боковое слева.
     */
    SIDE_LEFT(4),

    /**
     * Боковое справа.
     */
    SIDE_RIGHT(5);

    private final int code;

    PlaceDirection(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static PlaceDirection valueOf(int code) {
        for (PlaceDirection placeDirection : PlaceDirection.values()) {
            if (placeDirection.getCode() == code) {
                return placeDirection;
            }
        }
        return null;
    }

}
