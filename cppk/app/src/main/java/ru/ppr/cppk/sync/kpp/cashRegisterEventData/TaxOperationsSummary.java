package ru.ppr.cppk.sync.kpp.cashRegisterEventData;

import java.math.BigDecimal;

/**
 * Суммарная информация по финансовым операциям, облагаемых налогом
 *
 * @author Grigoriy Kashka
 */
public class TaxOperationsSummary extends FinanceOperationsSummary {
    /**
     * НДС, руб
     */
    public BigDecimal tax;
}
