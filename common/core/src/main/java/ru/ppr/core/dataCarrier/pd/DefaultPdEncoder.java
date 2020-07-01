package ru.ppr.core.dataCarrier.pd;

import android.support.annotation.NonNull;

import ru.ppr.core.dataCarrier.pd.base.Pd;

/**
 * Кодер ПД по умолчанию.
 * Следует использовать если не найден подходящий кодер для конкретного ПД.
 */
public class DefaultPdEncoder implements PdEncoder {

    @NonNull
    @Override
    public byte[] encode(@NonNull Pd pd) {
        return new byte[0];
    }

    @NonNull
    @Override
    public byte[] encodeWithoutEdsKeyNumber(@NonNull Pd pd) {
        return new byte[0];
    }
}