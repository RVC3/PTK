package ru.ppr.cppk.printer.exception;

import ru.ppr.ikkm.exception.PrinterException;

/**
 * Ошибка попытки выполнения операции, недопустимой в неоткрытой смене
 *
 * @author Grigoriy Kashka
 */
public class ShiftNotOpenedException extends PrinterException {
    public ShiftNotOpenedException() {
        super();
    }

    public ShiftNotOpenedException(Throwable throwable) {
        super(throwable);
    }

    public ShiftNotOpenedException(String detailMessage) {
        super(detailMessage);
    }
}
