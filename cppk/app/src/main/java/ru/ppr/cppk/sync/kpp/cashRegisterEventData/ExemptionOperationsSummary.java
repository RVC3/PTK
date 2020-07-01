package ru.ppr.cppk.sync.kpp.cashRegisterEventData;

import java.math.BigDecimal;

/**
 * Суммарная информация по льготным финансовым операциям
 *
 * @author Grigoriy Kashka
 */
public class ExemptionOperationsSummary extends FinanceOperationsSummary {
    /**
     * Выпадающий доход за продажу льготных билетов, руб
     */
    public BigDecimal exemptionLoss;
}
