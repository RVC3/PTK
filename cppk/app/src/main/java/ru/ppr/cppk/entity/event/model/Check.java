package ru.ppr.cppk.entity.event.model;

import com.google.common.base.Preconditions;

import java.util.Date;

/**
 * Чек
 *
 * @author A.Ushakov
 */
public class Check {

    private long id;

    /**
     * порядковый номер
     */
    private int orderNumber;

    private String additionalInfo;

    /**
     * Время печати - в в милисекундах
     */
    private Date printDateTime;

    /**
     * номер из принтера
     */
    private int snpdNumber;

    public int getSnpdNumber() {
        return snpdNumber;
    }

    public void setSnpdNumber(int snpdNumber) {
        this.snpdNumber = snpdNumber;
    }

    public void setPrintDateTimeInMillis(Date timestamp) {
        this.printDateTime = timestamp;
    }

    public Date getPrintDatetime() {
        Preconditions.checkNotNull(printDateTime, "PrintDateTime is null");
        return printDateTime;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(int orderNumber) {
        this.orderNumber = orderNumber;
    }

    /**
     * Номер, символ # и значение контрольного проверочного кода
     */
    public String getAdditionalInfo() {
        return additionalInfo;
    }

    public void setAdditionalInfo(String additionalInfo) {
        this.additionalInfo = additionalInfo;
    }

    /**
     * Непонятная сущность, сейчас используется для удобства разработки - сразу видно что за чек, а вообще нужно разобраться как это заполнять.
     *
     * @author G.Kashka
     */
    public enum AdditionalInfo {

        /**
         * Неизвестный тип
         */
        Unknown(""),

        /**
         * Тестовый
         */
        Test("Тестовый"),

        /**
         * Доплата
         */
        Surcharge("Доплата"),

        /**
         * Льготный
         */
        WithExemption("Льготный"),

        /**
         * Аннулирование
         */
        Repeal("Аннулирование"),

        /**
         * Детский
         */
        Child("Детский"),

        /**
         * Полный
         */
        Full("Полный");

        private final String descriptionString;

        AdditionalInfo(String description) {
            descriptionString = description;
        }

        public String getDescription() {
            return descriptionString;
        }

        public static AdditionalInfo getValue(String description) {
            for (AdditionalInfo type : AdditionalInfo.values()) {
                if (type.getDescription().equals(description)) {
                    return type;
                }
            }
            return AdditionalInfo.Unknown;
        }

    }
}
