package ru.ppr.ikkm.file.state.model;

import java.math.BigDecimal;

/**
 * Товарная позиция чека
 * Created by Артем on 21.01.2016.
 */
public class Item {

    private long id;
    private BigDecimal sum = BigDecimal.ZERO; // стоимость товарной позиции
    private BigDecimal discount = BigDecimal.ZERO; // размер скидки
    private BigDecimal total = BigDecimal.ZERO; // стоимость с учетом скидки
    private String goodDescription; // описание позиции
    private Check check; // чек, к которому принадлежит позиция
    private BigDecimal nds = BigDecimal.ZERO; // размер ндс, которая включена в стоимость с учетом скидки

    public BigDecimal getNds() {
        return nds;
    }

    public Check getCheck() {
        return check;
    }

    public void setCheck(Check check) {
        this.check = check;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public BigDecimal getSum() {
        return sum;
    }

    public void setSum(BigDecimal sum) {
        this.sum = sum;
    }

    public BigDecimal getDiscount() {
        return discount;
    }

    public void setDiscount(BigDecimal discount) {
        this.discount = discount;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public String getGoodDescription() {
        return goodDescription;
    }

    public void setGoodDescription(String goodDescription) {
        this.goodDescription = goodDescription;
    }

    public void setNds(BigDecimal nds) {
        this.nds = nds;
    }
}
