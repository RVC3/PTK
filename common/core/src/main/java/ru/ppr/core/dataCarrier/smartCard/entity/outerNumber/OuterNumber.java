package ru.ppr.core.dataCarrier.smartCard.entity.outerNumber;

import java.util.Date;

import ru.ppr.core.dataCarrier.smartCard.entity.BscType;

/**
 * Внешний номер.
 *
 * @author Aleksandr Brazhkin
 */
public class OuterNumber {

    /**
     * Срок действия
     */
    private Date validityTerm;
    /**
     * Дата инициализации
     */
    private Date initDate;
    /**
     * Тип БСК
     */
    private BscType bscType;
    /**
     * Серия БСК, записанная на поверхности карты (4 цифры перед номером)
     */
    private String bscSeries;
    /**
     * Внешний номер БСК, 10 цифр
     */
    private String bscNumber;

    public OuterNumber() {

    }

    public Date getValidityTerm() {
        return validityTerm;
    }

    public void setValidityTerm(Date validityTerm) {
        this.validityTerm = validityTerm;
    }

    public Date getInitDate() {
        return initDate;
    }

    public void setInitDate(Date initDate) {
        this.initDate = initDate;
    }

    public BscType getBscType() {
        return bscType;
    }

    public void setBscType(BscType bscType) {
        this.bscType = bscType;
    }

    public String getBscSeries() {
        return bscSeries;
    }

    public void setBscSeries(String bscSeries) {
        this.bscSeries = bscSeries;
    }

    public String getBscNumber() {
        return bscNumber;
    }

    public void setBscNumber(String bscNumber) {
        this.bscNumber = bscNumber;
    }
}
