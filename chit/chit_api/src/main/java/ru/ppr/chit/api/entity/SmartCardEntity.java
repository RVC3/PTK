package ru.ppr.chit.api.entity;

import com.google.gson.annotations.SerializedName;

/**
 * @author Dmitry Nevolin
 */
public class SmartCardEntity {

    /**
     * Внешний номер БСК
     * <br/><br/>Note: Только для ПД на БСК
     */
    private String outerNumber;
    /**
     * Номер кристалла БСК
     * <br/><br/>Note: Только для ПД на БСК
     */
    private String crystalSerialNumber;
    /**
     * Тип БСК
     * <br/><br/>Note: Только для ПД на БСК
     */
    private Type type;
    /**
     * Счетчик использования БСК из метки прохода
     * <br/><br/>Note: Только для ПД на БСК
     */
    private Integer usageCount;

    public String getOuterNumber() {
        return outerNumber;
    }

    public void setOuterNumber(String outerNumber) {
        this.outerNumber = outerNumber;
    }

    public String getCrystalSerialNumber() {
        return crystalSerialNumber;
    }

    public void setCrystalSerialNumber(String crystalSerialNumber) {
        this.crystalSerialNumber = crystalSerialNumber;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public Integer getUsageCount() {
        return usageCount;
    }

    public void setUsageCount(Integer usageCount) {
        this.usageCount = usageCount;
    }

    /**
     * ID и значения из таблицы TicketStorageTypes
     */
    public enum Type {

        /**
         * Неизвестен
         */
        @SerializedName("0")
        UNKNOWN(0),
        /**
         * Бумажный
         * Термолента для кассовых аппаратов 44 мм
         */
        @SerializedName("1")
        PAPER(1),
        /**
         * СКМ
         * Социальная карта москвича
         */
        @SerializedName("2")
        SKM(2),
        /**
         * СКМО
         * Социальная карта жителя Московской области
         */
        @SerializedName("6")
        SKMO(6),
        /**
         * ИПК
         * Студенческая карта
         */
        @SerializedName("7")
        IPK(7),
        /**
         * ЭТТ
         * Электронное транспортное требование
         */
        @SerializedName("8")
        ETT(8),
        /**
         * Тройка
         * Электронная карта «Тройка»
         */
        @SerializedName("10")
        TRK(10),
        /**
         * БСК на период
         * Бесконтактная смарт-карта без счетчика), выпускаемая ЦППК
         */
        @SerializedName("11")
        CPPK(11),
        /**
         * БСК на количество поездок
         * Бесконтактная смарт-карта со счетчиком), выпускаемая ЦППК
         */
        @SerializedName("12")
        CPPK_COUNTER(12),
        /**
         * БСК провожающего
         */
        @SerializedName("13")
        SEE_OF_CARD(13),
        /**
         * Служебная карта
         * Карта для авторизации.
         */
        @SerializedName("16")
        SERVICE(16),
        /**
         * Стрелка
         */
        @SerializedName("17")
        STRELKA(17);

        private final int code;

        Type(int code) {
            this.code = code;
        }

        public int getCode() {
            return code;
        }

    }

}
