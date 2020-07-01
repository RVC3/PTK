package ru.ppr.cppk.entity.event.model34;

/**
 * На ПТК используется только {@link ReturnOperationType#Annulment}
 *
 * Created by Кашка Григорий on 13.12.2015.
 */
public enum ReturnOperationType {

    Annulment(0, "Аннулирование"),

    Return(1, "Возврат");

    /**
     * Числовой код (id)
     */
    private int code;

    /**
     * Описание
     */
    private String description;

    public int getCode() {
        return code;
    }

    static public ReturnOperationType getType(int code) {
        for (ReturnOperationType type : ReturnOperationType.values()) {
            if (type.getCode() == code) {
                return type;
            }
        }
        return ReturnOperationType.Annulment;
    }

    ReturnOperationType(int code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * description of DeviceType
     *
     * @return String
     */
    public String getDescription() {
        return description;
    }
}
