package ru.ppr.cppk.dataCarrier.rfid.cardReaderTypes;

import android.support.annotation.NonNull;

import com.google.gson.Gson;

import ru.ppr.rfid.CardReadErrorType;

/**
 * Результат чтения или записи на карту
 *
 * Created by Артем on 08.02.2016.
 */
public class Result<T> {

    private final CardReadErrorType errorType;
    private final String textError;
    private final T result;

    public Result(CardReadErrorType errorType, String textError) {
        this.errorType = errorType;
        this.textError = textError;
        this.result = null;
    }

    public Result(@NonNull T result) {
        this.result = result;
        errorType = CardReadErrorType.NONE;
        textError = "Success";
    }

    public String getTextError() {
        return textError;
    }

    public CardReadErrorType getErrorType() {
        return errorType;
    }

    /**
     * Возвращает результат если флаг успешности результата == true, иначе бросает IllegalStateException
     * @return
     */
    @NonNull
    public T getResult() {

        if(isError()){
            throw new IllegalStateException("Result has error");
        }

        return result;
    }

    public boolean isError() {
        return errorType != CardReadErrorType.NONE;
    }

    public String toString() {
        return new Gson().toJson(this);
    }
}
