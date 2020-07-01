package ru.ppr.core.dataCarrier.pd.base;

import ru.ppr.core.dataCarrier.pd.PdVersion;

/**
 * Базовый класс для реальных ПД  (всех кроме заглушки).
 *
 * @author Aleksandr Brazhkin
 */
public abstract class BaseRealPd extends BasePd implements RealPd {
    /**
     * Порядковый номер (не фискальный) чека за календарный месяц
     */
    private int orderNumber;

    public BaseRealPd(PdVersion version, int size) {
        super(version, size);
    }

    @Override
    public int getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(int orderNumber) {
        this.orderNumber = orderNumber;
    }
}
