package ru.ppr.cppk.localdb.model;

import android.support.annotation.NonNull;

import java.math.BigDecimal;

import ru.ppr.cppk.localdb.model.base.LocalModelWithId;

/**
 * Цена
 */
public class Price implements LocalModelWithId<Long> {
    /**
     * Id
     */
    private Long id;
    /**
     * Сколько денег мы получили от покупателя
     */
    private BigDecimal full = BigDecimal.ZERO;
    /**
     * НДС багажных квитанций
     */
    private BigDecimal nds = BigDecimal.ZERO;
    /**
     * Сколько денег мы хотели получить от покупателя
     */
    private BigDecimal payed = BigDecimal.ZERO;
    /**
     * Сколько денег мы должны отдать покупателю. По идее, часто, но не
     * обязательно это {@link #full} - @see {@link #payed}.
     */
    private BigDecimal sumForReturn = BigDecimal.ZERO;

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    @NonNull
    public BigDecimal getFull() {
        return full;
    }

    public void setFull(@NonNull BigDecimal full) {
        this.full = full;
    }

    @NonNull
    public BigDecimal getNds() {
        return nds;
    }

    public void setNds(@NonNull BigDecimal nds) {
        this.nds = nds;
    }

    @NonNull
    public BigDecimal getPayed() {
        return payed;
    }

    public void setPayed(@NonNull BigDecimal payed) {
        this.payed = payed;
    }

    @NonNull
    public BigDecimal getSumForReturn() {
        return sumForReturn;
    }

    public void setSumForReturn(@NonNull BigDecimal sumForReturn) {
        this.sumForReturn = sumForReturn;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Price price = (Price) o;

        return full.equals(price.full) && nds.equals(price.nds) && payed.equals(price.payed) && sumForReturn.equals(price.sumForReturn);

    }

    @Override
    public int hashCode() {
        int result = full.hashCode();

        result = 31 * result + nds.hashCode();
        result = 31 * result + payed.hashCode();
        result = 31 * result + sumForReturn.hashCode();

        return result;
    }

    @Override
    public String toString() {
        return "Price{" +
                "id=" + id +
                ", full=" + full +
                ", nds=" + nds +
                ", payed=" + payed +
                ", sumForReturn=" + sumForReturn +
                '}';
    }
}
