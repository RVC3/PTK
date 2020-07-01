package ru.ppr.cppk.model;

import java.math.BigDecimal;

/**
 * @author Aleksandr Brazhkin
 */
public class PdSaleTempData {
    private BigDecimal totalWithDiscount;

    public BigDecimal getTotalWithDiscount() {
        return totalWithDiscount;
    }

    public void setTotalWithDiscount(BigDecimal totalWithDiscount) {
        this.totalWithDiscount = totalWithDiscount;
    }
}
