package ru.ppr.core.dataCarrier.pd;

import android.support.annotation.Nullable;

import javax.inject.Inject;

/**
 * Детектор версии ПД.
 *
 * @author Aleksandr Brazhkin
 */
public class PdVersionDetector {

    private static final int VERSION_INDEX = 0;

    @Inject
    public PdVersionDetector() {

    }

    /**
     * Возвращает версию ПД.
     *
     * @param data Данные ПД
     * @return Версия ПД
     */
    @Nullable
    public PdVersion getVersion(byte[] data) {
        return PdVersion.getByCode(data[VERSION_INDEX]);
    }
}
