package ru.ppr.core.dataCarrier.paper.barcodeReader;

import android.support.annotation.NonNull;

/**
 * Результат чтения ШК.
 *
 * @param <T> Тип данных, считываемых с ШК.
 * @author Aleksandr Brazhkin
 */
public class ReadBarcodeResult<T> {
    private final T data;
    private final ReadBarcodeErrorType readBarcodeErrorType;
    private final String description;

    public ReadBarcodeResult(T data) {
        this(data, ReadBarcodeErrorType.NONE, null);
    }

    public ReadBarcodeResult(T data, String description) {
        this(data, ReadBarcodeErrorType.NONE, description);
    }

    public ReadBarcodeResult(@NonNull ReadBarcodeErrorType readBarcodeErrorType) {
        this(null, readBarcodeErrorType, null);
    }

    public ReadBarcodeResult(@NonNull ReadBarcodeErrorType readBarcodeErrorType, String description) {
        this(null, readBarcodeErrorType, description);
    }

    private ReadBarcodeResult(T data, ReadBarcodeErrorType readBarcodeErrorType, String description) {
        this.data = data;
        this.readBarcodeErrorType = readBarcodeErrorType;
        this.description = description;
    }

    public boolean isSuccess() {
        return readBarcodeErrorType == ReadBarcodeErrorType.NONE;
    }

    public T getData() {
        return data;
    }

    public ReadBarcodeErrorType getReadBarcodeErrorType() {
        return readBarcodeErrorType;
    }

    public String getDescription() {
        return description;
    }


    @Override
    public String toString() {
        return "ReadCardResult{" +
                "data=" + data +
                ", readCardErrorType=" + readBarcodeErrorType +
                ", description='" + description + '\'' +
                '}';
    }
}
