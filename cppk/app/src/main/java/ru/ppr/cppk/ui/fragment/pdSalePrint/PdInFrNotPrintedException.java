package ru.ppr.cppk.ui.fragment.pdSalePrint;

import ru.ppr.ikkm.exception.PrinterException;

/**
 * ПД лег на ФР, но не был распечатан.
 *
 * @author Grigoriy Kashka
 */
public class PdInFrNotPrintedException extends PrinterException {

    public PdInFrNotPrintedException(Throwable throwable) {
        super(throwable);
    }

    public PdInFrNotPrintedException(String message) {
        super(message);
    }
}
