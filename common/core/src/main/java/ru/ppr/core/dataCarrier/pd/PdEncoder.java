package ru.ppr.core.dataCarrier.pd;

import android.support.annotation.NonNull;

import ru.ppr.core.dataCarrier.pd.base.Pd;

/**
 * Энкодер ПД.
 *
 * @author Aleksandr Brazhkin
 */
public interface PdEncoder {
    /**
     * Кодирует ПД.
     *
     * @param pd ПД
     * @return Данные ПД
     */
    @NonNull
    byte[] encode(@NonNull Pd pd);

    /**
     * Кодирует ПД без номера ключа ЭЦП.
     *
     * @param pd ПД
     * @return Данные ПД без номера ключа ЭЦП
     */
    @NonNull
    byte[] encodeWithoutEdsKeyNumber(@NonNull Pd pd);
}
