package ru.ppr.ikkm.exception;

public class PrinterException extends Exception {

    /**
     *
     */
    private static final long serialVersionUID = -8486449769355253572L;

    public PrinterException() {
        super();
    }

    public PrinterException(Throwable throwable) {
        super(throwable);
    }

    public PrinterException(String detailMessage) {
        super(detailMessage);
    }
}
