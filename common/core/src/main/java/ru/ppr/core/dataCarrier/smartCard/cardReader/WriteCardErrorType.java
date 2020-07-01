package ru.ppr.core.dataCarrier.smartCard.cardReader;

/**
 * Тип ошибки записи на карту.
 *
 * @author Aleksandr Brazhkin
 */
public enum WriteCardErrorType {
    SUCCESS(0),
    UID_DOES_NOT_MATCH(1),
    CAN_NOT_SEARCH_CARD(2),
    WRITE_ERROR(3),
    NOT_SUPPORTED(4),
    UNKNOWN_ERROR(5);

    private int code;

    WriteCardErrorType(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
