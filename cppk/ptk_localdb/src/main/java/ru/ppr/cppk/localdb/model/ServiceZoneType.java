package ru.ppr.cppk.localdb.model;

import android.support.annotation.Nullable;

/**
 * Тип зоны действия
 *
 * @author Aleksandr Brazhkin
 */
public enum ServiceZoneType {
    /**
     * Зона не инициализирована
     */
    None(0),
    /**
     * Весь полигон
     */
    Polygone(1),
    /**
     * Одна станция
     */
    Station(2),
    /**
     * Одно направление
     */
    Direction(3),
    /**
     * Один производственный участок
     */
    ProductionSection(4);

    private final int code;

    ServiceZoneType(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    @Nullable
    public static ServiceZoneType valueOf(int code) {
        for (ServiceZoneType serviceZoneType : ServiceZoneType.values()) {
            if (serviceZoneType.getCode() == code) {
                return serviceZoneType;
            }
        }
        return null;
    }
}
