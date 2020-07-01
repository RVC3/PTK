package ru.ppr.nsi.entity;

/**
 * Тип периода срока действия ПД.
 *
 * @author Aleksandr Brazhkin
 */
public enum ValidityPeriod {

    /**
     * Дни действия установленны в днях
     */
    DAY(1),
    /**
     * Дни действия установленны в месяцах
     */
    MONTH(2),
    /**
     * Дни действия установленны в датах
     */
    DATE(3);

    private final int code;

    ValidityPeriod(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static ValidityPeriod getByCode(int code) {
        for (ValidityPeriod validityPeriod : ValidityPeriod.values()) {
            if (validityPeriod.getCode() == code) {
                return validityPeriod;
            }
        }
        return null;
    }
}
