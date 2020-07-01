package ru.ppr.nsi.entity;

import java.math.BigDecimal;

/**
 * Сбор
 *
 * @author Brazhkin A.V.
 */
public class ProcessingFee extends BaseNSIObject<Integer> {
    /**
     * Сумма
     */
    private BigDecimal tariff = BigDecimal.ZERO;
    /**
     * Налог
     */
    private BigDecimal tax = BigDecimal.ZERO;
    /**
     * Категория поезда
     */
    private int trainCategoryCode;
    /**
     * Тип сбора
     */
    private FeeType feeType;


    public ProcessingFee() {

    }

    public BigDecimal getTariff() {
        return tariff;
    }

    public void setTariff(BigDecimal tariff) {
        this.tariff = tariff;
    }

    public BigDecimal getTax() {
        return tax;
    }

    public void setTax(BigDecimal tax) {
        this.tax = tax;
    }

    public int getTrainCategoryCode() {
        return trainCategoryCode;
    }

    public void setTrainCategoryCode(int trainCategoryCode) {
        this.trainCategoryCode = trainCategoryCode;
    }

    public FeeType getFeeType() {
        return feeType;
    }

    public void setFeeType(FeeType feeType) {
        this.feeType = feeType;
    }

    @Override
    public String toString() {
        return "ProcessingFee{" +
                "tariff=" + tariff +
                ", tax=" + tax +
                ", trainCategoryCode=" + trainCategoryCode +
                ", feeType=" + feeType +
                ", code=" + getCode() +
                ", versionId=" + getVersionId() +
                ", deleteInVersionId=" + getDeleteInVersionId() +
                '}';
    }
}
