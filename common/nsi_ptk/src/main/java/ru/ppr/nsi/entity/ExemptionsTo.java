package ru.ppr.nsi.entity;

import android.support.annotation.Nullable;

/**
 * Соответствие льготы категории поезда, типу билета.
 */
public class ExemptionsTo {

    private final int exemptionCode;
    private final String carrierCode;
    private final String expressTicketCode;
    private final Integer ticketTypeCode;
    private final int trainCategoryCode;
    private final int carClassCode;
    private final Integer ticketProcessingDelay; // задержка оформления второго билета по одному и тому же коду(минуты)

    public ExemptionsTo(int exemptionCode, String carrierCode, String expressTicketCode,
                        Integer ticketTypeCode, int trainCategoryCode, int carClassCode,
                        Integer ticketProcessingDelay) {
        this.exemptionCode = exemptionCode;
        this.carrierCode = carrierCode;
        this.expressTicketCode = expressTicketCode;
        this.ticketTypeCode = ticketTypeCode;
        this.trainCategoryCode = trainCategoryCode;
        this.carClassCode = carClassCode;
        this.ticketProcessingDelay = ticketProcessingDelay;
    }

    public int getExemptionCode() {
        return exemptionCode;
    }

    @Nullable
    public String getCarrierCode() {
        return carrierCode;
    }

    public String getExpressTicketCode() {
        return expressTicketCode;
    }

    @Nullable
    public Integer getTicketTypeCode() {
        return ticketTypeCode;
    }

    public int getTrainCategoryCode() {
        return trainCategoryCode;
    }

    public int getCarClassCode() {
        return carClassCode;
    }

    @Nullable
    public Integer getTicketProcessingDelay() {
        return ticketProcessingDelay;
    }
}
