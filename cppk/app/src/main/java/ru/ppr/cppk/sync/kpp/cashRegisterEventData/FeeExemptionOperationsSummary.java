package ru.ppr.cppk.sync.kpp.cashRegisterEventData;

import java.math.BigDecimal;

/**
 * Суммарная информация по льготным финансовым операциям, по которым может взиматься сбор
 *
 * @author Grigoriy Kashka
 */
public class FeeExemptionOperationsSummary extends FeeTaxOperationsSummary {
    /**
     * Выпадающий доход за продажу льготных билетов, руб
     */
    public BigDecimal exemptionLoss;
}
