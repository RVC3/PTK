package ru.ppr.core.dataCarrier.pd.base;

import java.util.Date;

import ru.ppr.core.dataCarrier.pd.PdVersion;

/**
 * ПД.
 *
 * @author Aleksandr Brazhkin
 */
public interface Pd {
    /**
     * Возвращает версию ПД.
     *
     * @return Версия ПД.
     */
    PdVersion getVersion();

    /**
     * Возвращает размер ПД в байтах.
     *
     * @return Размер ПДв байтах.
     */
    int getSize();

    /**
     * Возвращает время продажи билета.
     *
     * @return Время продажи билета
     */
    Date getSaleDateTime();

    /**
     * Возвращает номер ключа ЭЦП.
     *
     * @return Номер ключа ЭЦП
     */
    long getEdsKeyNumber();
}
