package ru.ppr.core.dataCarrier.smartCard.entity;

/**
 * Эмисионные данные.
 *
 * @author Aleksandr Brazhkin
 */
public class EmissionData {
    /**
     * Версия формата
     */
    private int version;
    /**
     * Номер карты
     */
    private String cardNumber;
    /**
     * Номер карты
     * 8 цифр: ГГММВВКК, где
     * ГГ и ММ - год и месяц выпуска,
     * ВВ - порядковый номер карты, выпущенной для данного гражданина (00 - первичная эмиссия, 01-99 - перевыпуск карты),
     * КК - код региона (77 для Москвы, 90 и возможно 50 - для Московской области)
     */
    private String cardSeries;
    /**
     * Контрольная сумма
     */
    private int controlSum;

    public EmissionData() {
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public String getCardSeries() {
        return cardSeries;
    }

    public void setCardSeries(String cardSeries) {
        this.cardSeries = cardSeries;
    }

    public int getControlSum() {
        return controlSum;
    }

    public void setControlSum(int controlSum) {
        this.controlSum = controlSum;
    }
}
