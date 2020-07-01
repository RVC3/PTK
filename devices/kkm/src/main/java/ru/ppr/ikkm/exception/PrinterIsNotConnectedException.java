package ru.ppr.ikkm.exception;

public class PrinterIsNotConnectedException extends PrinterException {

    /**
     *
     */
    private static final long serialVersionUID = -8486449769355253572L;

    public PrinterIsNotConnectedException() {
        super();
    }

    public PrinterIsNotConnectedException(Throwable throwable) {
        super(throwable);
    }

}
