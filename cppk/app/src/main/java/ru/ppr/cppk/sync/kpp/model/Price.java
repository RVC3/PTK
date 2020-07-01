package ru.ppr.cppk.sync.kpp.model;

import java.math.BigDecimal;

/**
 * @author Grigoriy Kashka
 */
public class Price {
    /**
     * Сколько денег мы получили от покупателя
     */
    public BigDecimal Full;

    /**
     * НДС багажных квитанций
     */
    public BigDecimal Nds;

    /**
     * Сколько денег мы хотели получить от покупателя
     */
    public BigDecimal Payed;

    /**
     * Сколько денег мы должны отдать покупателю.
     * По идее, часто, но не обязательно это Full - Payed
     */
    public BigDecimal SummForReturn;

    /**
     * Процент НДС со стоимости ПД
     */
    public int NdsPercent;
}
