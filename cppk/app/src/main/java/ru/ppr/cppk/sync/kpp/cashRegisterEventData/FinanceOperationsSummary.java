package ru.ppr.cppk.sync.kpp.cashRegisterEventData;

import java.math.BigDecimal;

/**
 * Суммарная информация по финансовым операциям
 *
 * @author Grigoriy Kashka
 */
public class FinanceOperationsSummary {
    /**
     * Сумма, руб
     */
    public BigDecimal amount;

    /**
     * Количество операций
     */
    public int count;
}
