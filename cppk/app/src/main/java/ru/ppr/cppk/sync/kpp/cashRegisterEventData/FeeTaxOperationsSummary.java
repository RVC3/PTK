package ru.ppr.cppk.sync.kpp.cashRegisterEventData;


import java.math.BigDecimal;

/**
 * Суммарная информация по финансовым операциям, облагаемых налогом и по которым может взиматься сбор
 *
 * @author Grigoriy Kashka
 */
public class FeeTaxOperationsSummary extends TaxOperationsSummary {
    /**
     * сбор, руб
     */
    public BigDecimal fee;

    /**
     * Ндс со сбора, руб
     */
    public BigDecimal feeTax;
}
