package ru.ppr.cppk.localdb.model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.math.BigDecimal;

import ru.ppr.cppk.localdb.model.base.LocalModelWithId;
import ru.ppr.nsi.entity.FeeType;


/**
 * Информация о сборе
 *
 * @author Grigoriy Kashka
 */
public class Fee implements LocalModelWithId<Long> {
    /**
     * Id
     */
    private Long id;
    /**
     * Сумма сбора
     */
    private BigDecimal total = BigDecimal.ZERO;
    /**
     * НДС с суммы сбора
     */
    private BigDecimal nds = BigDecimal.ZERO;
    /**
     * Тип сбора из таблицы НСИ ProcessingFees
     */
    private FeeType feeType = null;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @NonNull
    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(@NonNull BigDecimal total) {
        this.total = total;
    }

    @NonNull
    public BigDecimal getNds() {
        return nds;
    }

    public void setNds(@NonNull BigDecimal nds) {
        this.nds = nds;
    }

    @Nullable
    public FeeType getFeeType() {
        return feeType;
    }

    public void setFeeType(@Nullable FeeType feeType) {
        this.feeType = feeType;
    }

}
