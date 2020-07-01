package ru.ppr.core.dataCarrier.pd;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import ru.ppr.core.dataCarrier.pd.base.Pd;

/**
 * Декодер ПД по умолчанию.
 * Следует использовать если не найден подходящий декодер для конкретного ПД.
 */
public class DefaultPdDecoder implements PdDecoder {

    @Nullable
    @Override
    public Pd decode(@NonNull byte[] data) {
        return null;
    }

}