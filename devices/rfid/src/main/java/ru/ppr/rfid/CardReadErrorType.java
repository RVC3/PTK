package ru.ppr.rfid;

/**
 * Тип ошибки чтения смарт-карты.
 */
public enum CardReadErrorType {

    /**
     * Отсутствие ошибки
     */
    NONE(0),
    /**
     * Ошибка авторизации, ошибку такого типа нам необходимо отличать от остальных
     * из-за комментария https://aj.srvdev.ru/browse/CPPKPP-27899
     */
    AUTHORIZATION(1),
    /**
     * Какая-то другая ошибка. Обрабатываем как раньше
     */
    OTHER(2),

    /**
     * Не тот UID карты
     */
    UID_DOES_NOT_MATCH(3);

    private int code;

    CardReadErrorType(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

}
