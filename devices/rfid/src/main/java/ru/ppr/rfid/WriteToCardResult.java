package ru.ppr.rfid;

/**
 * Тип ошибки при записи данных на смарт-карту.
 */
public enum WriteToCardResult {

    SUCCESS,
    UID_DOES_NOT_MATCH,
    CAN_NOT_SEARCH_CARD,
    UNKNOWN_ERROR, WRITE_ERROR,
    NOT_SUPPORTED;

    public boolean isOk() {
        return SUCCESS.equals(this);
    }

}
