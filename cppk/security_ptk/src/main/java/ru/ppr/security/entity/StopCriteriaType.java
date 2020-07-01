package ru.ppr.security.entity;

/**
 * Критерий типизации стоп листов.
 *
 * @author Grigoriy Kashka
 */
public enum StopCriteriaType {

    /**
     * Запрет на чтение и запись
     */
    READ_AND_WRITE(1),
    /**
     * Запрет на запись
     */
    WRITE(2),
    /**
     * Запрет на использование СТУ на карте.
     * СТУ - Служебная транспортная услуга
     */
    SERVICE_TICKET_USAGE(3);

    private int code;

    StopCriteriaType(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static StopCriteriaType getByCode(int code) {
        for (StopCriteriaType stopCriteriaType : StopCriteriaType.values()) {
            if (stopCriteriaType.getCode() == code) {
                return stopCriteriaType;
            }
        }
        return null;
    }
}
