package ru.ppr.core.dataCarrier.readbarcodetask;

/**
 * Исключение возникающие при ошибке чтения ШК.
 *
 * @author Aleksandr Brazhkin
 */
public class BarcodeNotReadException extends Exception {
    public BarcodeNotReadException() {
    }

    public BarcodeNotReadException(String message) {
        super(message);
    }

    public BarcodeNotReadException(Throwable cause) {
        super(cause);
    }
}
