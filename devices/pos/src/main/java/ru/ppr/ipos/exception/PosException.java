package ru.ppr.ipos.exception;

/**
 * Исключение уровня POS-устройства.
 *
 * @author Dmitry Nevolin
 */
public class PosException extends Exception {

    public PosException() {
    }

    public PosException(String detailMessage) {
        super(detailMessage);
    }

    public PosException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public PosException(Throwable throwable) {
        super(throwable);
    }

}
