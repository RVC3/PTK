package ru.ppr.core.dataCarrier.pd.base;

/**
 * Реальный ПД (все кроме заглушки).
 *
 * @author Aleksandr Brazhkin
 */
public interface RealPd extends Pd {

    /**
     * Возвращает порядковый номер ПД.
     *
     * @return Порядковый номер (не фискальный) чека за календарный месяц
     */
    int getOrderNumber();
}
