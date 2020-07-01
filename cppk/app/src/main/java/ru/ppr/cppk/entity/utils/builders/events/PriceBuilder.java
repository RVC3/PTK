package ru.ppr.cppk.entity.utils.builders.events;

import android.support.annotation.NonNull;

import com.google.common.base.Preconditions;

import java.math.BigDecimal;

import ru.ppr.cppk.localdb.model.Price;

/**
 * Билдер сущности {@link Price}.
 *
 * @author Artem Ushakov
 */
public class PriceBuilder {

    private BigDecimal sumForReturn;
    private BigDecimal full;
    private BigDecimal nds;
    private BigDecimal payed;

    public PriceBuilder setFull(BigDecimal full) {
        this.full = full;
        return this;
    }

    public PriceBuilder setPayed(BigDecimal payed) {
        this.payed = payed;
        return this;
    }

    public PriceBuilder setSumForReturn(BigDecimal sumForReturn) {
        this.sumForReturn = sumForReturn;
        return this;
    }

    public PriceBuilder setNds(BigDecimal nds) {
        this.nds = nds;
        return this;
    }

    @NonNull
    public Price build() {
        Preconditions.checkNotNull(sumForReturn, "SumFoReturn is null");
        Preconditions.checkNotNull(full, "Full is null");
        Preconditions.checkNotNull(nds, "Nds is null");
        Preconditions.checkNotNull(payed, "Payed is null");

        Price price = new Price();
        price.setSumForReturn(sumForReturn);
        price.setPayed(payed);
        price.setNds(nds);
        price.setFull(full);

        return price;
    }
}
