package ru.ppr.rfid;

/**
 * Результат выполнения функций RfidReal.
 *
 * @author Artem Ushakov
 */
public class RfidResult<T> {

    private final CardReadErrorType errorType;
    private final String errorMessage;
    private final T result;

    public RfidResult(CardReadErrorType errorType, String errorMessage) {
        this.errorType = errorType;
        this.errorMessage = errorMessage;
        this.result = null;
    }

    public RfidResult(T result) {
        this.errorType = CardReadErrorType.NONE;
        this.errorMessage = "Success";
        this.result = result;
    }

    public boolean isOk() {
        return errorType == CardReadErrorType.NONE;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public CardReadErrorType getErrorType() {
        return errorType;
    }

    public T getResult() {
        return result;
    }
}
