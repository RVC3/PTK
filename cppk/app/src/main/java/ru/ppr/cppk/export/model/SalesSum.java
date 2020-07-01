package ru.ppr.cppk.export.model;

import java.math.BigDecimal;

/**
 * Created by григорий on 14.07.2016.
 */
public class SalesSum {
    /**
     * сумма вырученных денег за билеты без учета сбора
     */
    public BigDecimal ticketsSum = BigDecimal.ZERO;
    /**
     * сумма вырученных денег за багаж без учета сбора
     */
    public BigDecimal luggageSum = BigDecimal.ZERO;
    /**
     * сумма вырученных денег за сборы (сумма Fee.Full)
     */
    public BigDecimal feeSum = BigDecimal.ZERO;
    /**
     * сумма вырученных денег за штрафы
     */
    public BigDecimal finesSum = BigDecimal.ZERO;

    @Override
    public String toString() {
        return "SalesSum{" +
                "ticketsSum=" + ticketsSum +
                ", luggageSum=" + luggageSum +
                ", feeSum=" + feeSum +
                ", finesSum=" + finesSum +
                '}';
    }
}
