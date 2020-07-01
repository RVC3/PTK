package ru.ppr.ipos.model;

/**
 * @author Dmitry Vinogradov
 */
public class PosProperty {

    public static class Keys {
        public static final String SaleTransactionId = "SaleTransactionId";
    }

    /**
     * Ключ параметра.
     */
    private String propertyKey;
    /**
     * Значение параметра.
     */
    private String propertyValue;

    @Override
    public String toString() {
        return "PosProperty{" +
                "propertyKey=" + propertyKey +
                ", propertyValue=" + propertyValue +
                '}';
    }

    public String getPropertyKey() {
        return propertyKey;
    }

    public void setPropertyKey(String key) {
        this.propertyKey = key;
    }

    public String getPropertyValue() {
        return propertyValue;
    }

    public void setPropertyValue(String value) {
        this.propertyValue = value;
    }
}
