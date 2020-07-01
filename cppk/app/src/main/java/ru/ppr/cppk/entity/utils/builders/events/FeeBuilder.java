package ru.ppr.cppk.entity.utils.builders.events;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.common.base.Preconditions;

import java.math.BigDecimal;

import ru.ppr.cppk.localdb.model.Fee;
import ru.ppr.nsi.entity.FeeType;

/**
 * Билдер сущности {@link Fee}.
 *
 * @author Artem Ushakov
 */
public class FeeBuilder {

    private BigDecimal total;
    private BigDecimal nds;
    private FeeType feeType;

    public FeeBuilder setTotal(@NonNull BigDecimal total) {
        this.total = total;
        return this;
    }

    public FeeBuilder setNds(@NonNull BigDecimal nds) {
        this.nds = nds;
        return this;
    }

    public FeeBuilder setFeeType(@Nullable FeeType feeType) {
        this.feeType = feeType;
        return this;
    }

    @NonNull
    public Fee build() {
        Preconditions.checkNotNull(total, "Total is null");
        Preconditions.checkNotNull(nds, "Nds is null");

        Fee fee = new Fee();
        fee.setNds(nds);
        fee.setTotal(total);
        fee.setFeeType(feeType);
        return fee;
    }
}
