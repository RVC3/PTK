package ru.ppr.cppk.sync.kpp.model;

import java.math.BigDecimal;

/**
 * Информация о сборе
 *
 * @author Grigoriy Kashka
 */
public class Fee {
    /**
     * Сумма сбора
     */
    public BigDecimal Total;

    /**
     * НДС с суммы сбора
     */
    public BigDecimal Nds;

    /**
     * Тип сбора (ProcessingFeeTypeEnum)
     */
    public int FeeType;

    /**
     * Процент НДС со стоимости ПД
     */
    public int NdsPercent;
}
