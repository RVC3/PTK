package ru.ppr.core.dataCarrier.smartCard.entity;

/**
 * Информация о БСК.
 *
 * @author Aleksandr Brazhkin
 */
public class BscInformation {
    /**
     * Код льготы.
     */
    private int exemptionCode;
    /**
     * Месяц окончания действия карты
     */
    private int endMonth;
    /**
     * Год окончания действия карты
     */
    private int endYear;
    /**
     * Тип БСК
     */
    private BscType bscType;
    /**
     * Последние 14 цифр внешнего номера карты
     */
    private String bscNumber;


    public BscInformation() {

    }

    public int getExemptionCode() {
        return exemptionCode;
    }

    public void setExemptionCode(int exemptionCode) {
        this.exemptionCode = exemptionCode;
    }

    public int getEndMonth() {
        return endMonth;
    }

    public void setEndMonth(int endMonth) {
        this.endMonth = endMonth;
    }

    public int getEndYear() {
        return endYear;
    }

    public void setEndYear(int endYear) {
        this.endYear = endYear;
    }

    public BscType getBscType() {
        return bscType;
    }

    public void setBscType(BscType bscType) {
        this.bscType = bscType;
    }

    public String getBscNumber() {
        return bscNumber;
    }

    public void setBscNumber(String bscNumber) {
        this.bscNumber = bscNumber;
    }
}
