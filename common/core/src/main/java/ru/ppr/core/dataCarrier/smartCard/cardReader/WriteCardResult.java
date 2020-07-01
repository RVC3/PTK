package ru.ppr.core.dataCarrier.smartCard.cardReader;

import android.support.annotation.NonNull;

/**
 * Результат записи на карту.
 *
 * @author Aleksandr Brazhkin
 */
public class WriteCardResult {

    private final WriteCardErrorType writeCardErrorType;
    private final String description;

    public WriteCardResult() {
        this(WriteCardErrorType.SUCCESS, null);
    }

    public WriteCardResult(String description) {
        this(WriteCardErrorType.SUCCESS, description);
    }

    public WriteCardResult(@NonNull WriteCardErrorType writeCardErrorType) {
        this(writeCardErrorType, null);
    }

    public WriteCardResult(@NonNull WriteCardErrorType writeCardErrorType, String description) {
        this.writeCardErrorType = writeCardErrorType;
        this.description = description;
    }

    public boolean isSuccess() {
        return writeCardErrorType == WriteCardErrorType.SUCCESS;
    }

    public WriteCardErrorType getWriteCardErrorType() {
        return writeCardErrorType;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return "WriteCardResult{" +
                "writeCardErrorType=" + writeCardErrorType +
                ", description='" + description + '\'' +
                '}';
    }
}
