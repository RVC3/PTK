package ru.ppr.core.dataCarrier.pd;

import android.support.annotation.NonNull;

import ru.ppr.core.dataCarrier.pd.base.Pd;

/**
 * Фабрика энкодеров ПД.
 *
 * @author Aleksandr Brazhkin
 */
public interface PdEncoderFactory {
    /**
     * Создает энкодер для ПД.
     *
     * @param pd ПД
     * @return Энкодер ПД
     */
    @NonNull
    PdEncoder create(@NonNull Pd pd);
}
