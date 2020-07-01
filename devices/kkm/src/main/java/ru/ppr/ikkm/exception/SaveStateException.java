package ru.ppr.ikkm.exception;

/**
 * Created by Артем on 21.01.2016.
 */
public class SaveStateException extends Exception{

    public SaveStateException() {
    }

    public SaveStateException(String detailMessage) {
        super(detailMessage);
    }

    public SaveStateException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public SaveStateException(Throwable throwable) {
        super(throwable);
    }
}
