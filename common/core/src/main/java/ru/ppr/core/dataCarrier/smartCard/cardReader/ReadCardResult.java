package ru.ppr.core.dataCarrier.smartCard.cardReader;

import android.support.annotation.NonNull;

/**
 * Результат чтения карты.
 *
 * @param <T> Тип данных, считываемых с карты.
 * @author Aleksandr Brazhkin
 */
public class ReadCardResult<T> {
    private final T data;
    private final ReadCardErrorType readCardErrorType;
    private final String description;

    public ReadCardResult(T data) {
        this(data, ReadCardErrorType.NONE, null);
    }

    public ReadCardResult(T data, String description) {
        this(data, ReadCardErrorType.NONE, description);
    }

    public ReadCardResult(@NonNull ReadCardErrorType readCardErrorType) {
        this(null, readCardErrorType, null);
    }

    public ReadCardResult(@NonNull ReadCardErrorType readCardErrorType, String description) {
        this(null, readCardErrorType, description);
    }

    private ReadCardResult(T data, ReadCardErrorType readCardErrorType, String description) {
        this.data = data;
        this.readCardErrorType = readCardErrorType;
        this.description = description;
    }

    public boolean isSuccess() {
        return readCardErrorType == ReadCardErrorType.NONE;
    }

    public T getData() {
        return data;
    }

    public ReadCardErrorType getReadCardErrorType() {
        return readCardErrorType;
    }

    public String getDescription() {
        return description;
    }


    @Override
    public String toString() {
        return "ReadCardResult{" +
                "data=" + data +
                ", readCardErrorType=" + readCardErrorType +
                ", description='" + description + '\'' +
                '}';
    }
}
