package ru.ppr.cppk.entity;

/**
 * Режим работы ПТК
 *
 * @author Григорий
 */
public enum PtkMode {
    /**
     * Нормальный
     */
    NORMAL("normal"),
    /**
     * Сервисный
     */
    SERVICE("service"),
    /**
     * Аварийный
     */
    EMERGENCY("emergency");

    private String typeValue;

    private PtkMode(String type) {
        typeValue = type;
    }

    static public PtkMode getType(String pType) {
        for (PtkMode type : PtkMode.values()) {
            if (type.getTypeValue().equals(pType)) {
                return type;
            }
        }
        return PtkMode.NORMAL;
    }

    public String getTypeValue() {
        return typeValue;
    }
}
