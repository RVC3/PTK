package ru.ppr.nsi.entity;

import android.support.annotation.NonNull;

/**
 * Тип дня.
 */
public enum TypeOfDay {
    /**
     * Неизвестный тип дня
     */
    UNKNOWN(-1, "Неизвестный тип дня"),
    /**
     * Рабочий
     */
    WORKING_DAY(0, "Рабочий"),
    /**
     * Суббота
     */
    SATURDAY(1, "Суббота"),
    /**
     * Воскресенье
     */
    SUNDAY(2, "Воскресенье"),
    /**
     * Предпраздничный
     */
    PRE_HOLIDAY(3, "Предпраздничный"),
    /**
     * Праздник
     */
    HOLIDAY(4, "Праздник"),
    /**
     * Региональный праздник ([RegionCalendars] в этой таблице , если поле [DayType] имеет значение 0 - то день является праздником)
     */
    REGIONAL_HOLIDAY(5, "Региональный праздник"),
    /**
     * Региональный рабочий день ([RegionCalendars] в этой таблице , если поле [DayType] имеет значение 1 - то день является рабочим)
     */
    REGIONAL_BUSINESS_DAY(6, "Региональный рабочий день"),
    /**
     * Перенос выходного дня
     */
    HOLIDAY_TRANSFER(7, "Перенос выходного дня"),
    /**
     * Перенос рабочего дня
     */
    WORKING_DAY_TRANSFER(8, "Перенос рабочего дня"),
    /**
     * Послепраздничный
     */
    POST_HOLIDAY(9, "Послепраздничный"),
    /**
     * Перенесенный выходной
     */
    DAY_OFF(10, "Перенесенный выходной");

    /**
     * Числовой код типа дня
     */
    private final int code;
    /**
     * Описание
     */
    private final String description;

    TypeOfDay(int code, String description) {
        this.code = code;
        this.description = (description);
    }

    public String getDescription() {
        return description;
    }

    public int getCode() {
        return code;
    }

    @NonNull
    public static TypeOfDay valueOf(int code) {
        for (TypeOfDay type : TypeOfDay.values()) {
            if (type.getCode() == code) {
                return type;
            }
        }
        return TypeOfDay.UNKNOWN;
    }

}
