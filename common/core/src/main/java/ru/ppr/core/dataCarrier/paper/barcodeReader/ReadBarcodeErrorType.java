package ru.ppr.core.dataCarrier.paper.barcodeReader;

/**
 * Тип ошибки чтения данных с ШК.
 *
 * @author Aleksandr Brazhkin
 */
public enum ReadBarcodeErrorType {
    /**
     * Отсутствие ошибки
     */
    NONE(0),
    /**
     * Не удалось получить ПД из данных ШК.
     */
    PD_IS_NOT_PARSED(1),
    /**
     * Какая-то другая ошибка. Обрабатываем как раньше
     */
    OTHER(2);

    private int code;

    ReadBarcodeErrorType(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
