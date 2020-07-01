package ru.ppr.cppk.printer.exception;

import ru.ppr.ikkm.exception.PrinterException;

/**
 * Ошибка попытки выполнения операции, недопустимой в незакрытой смене
 *
 * @author Grigoriy Kashka
 */
public class ShiftNotClosedException extends PrinterException {
    public ShiftNotClosedException() {
        super();
    }

    public ShiftNotClosedException(Throwable throwable) {
        super(throwable);
    }

    public ShiftNotClosedException(String detailMessage) {
        super(detailMessage);
    }
}
