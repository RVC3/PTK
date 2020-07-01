package ru.ppr.core.dataCarrier.pd;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import ru.ppr.core.dataCarrier.pd.base.Pd;

/**
 * Деккодер ПД.
 *
 * @author Aleksandr Brazhkin
 */
public interface PdDecoder {
    /**
     * Декодирует ПД.
     *
     * @param data Данные ПД
     * @return ПД
     */
    @Nullable
    Pd decode(@NonNull byte[] data);
}