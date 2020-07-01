package ru.ppr.ikkm.file.transaction;

/**
 * Created by Артем on 22.01.2016.
 */
public class TransactionException extends Exception {

    public static final String TOTAL_NOT_EQUALS = "Receive total (%1.2f) not equals with calculate total (%2.2f)";
    public static final String PAYED_LESS_THAN_TOTAL = "Payed (%1.2f) less then total (%2.2f)";

    public TransactionException() {
    }

    public TransactionException(String detailMessage) {
        super(detailMessage);
    }

    public TransactionException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public TransactionException(Throwable throwable) {
        super(throwable);
    }
}
