package ru.ppr.core.dataCarrier.pd.base;

/**
 * Абонемент на количество поездок.
 *
 * @author Aleksandr Brazhkin
 */
public interface PdWithCounter extends Pd {
    /**
     * Возвращает стартовое значение счетчика.
     *
     * @return Значение счетчика, при котором начинает действовать абонемент, включительно.
     */
    int getStartCounterValue();

    /**
     * Возвращает конечное значение счетчика.
     *
     * @return Значение счетчика, при котором завершает действовать абонемент, включительно.
     */
    int getEndCounterValue();
}
