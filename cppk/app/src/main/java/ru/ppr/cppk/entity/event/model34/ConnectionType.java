package ru.ppr.cppk.entity.event.model34;

import android.support.annotation.Nullable;

/**
 * Тип связи с родительским ПД.
 *
 * @author Grigoriy Kashka
 */
public enum ConnectionType {
    /**
     * Транзит
     * Родительский ПД - первый из двух оформленных ПД
     */
    TRANSIT(1),
    /**
     * Доплата
     * Родительский ПД - ПД на поезд 6000
     */
    SURCHARGE(2),
    /**
     * Трансфер
     * Родительский ПД - ПД на поезд
     */
    TRANSFER(4);

    /**
     * Код
     */
    private final int code;

    ConnectionType(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    @Nullable
    public static ConnectionType valueOf(int code) {
        for (ConnectionType type : ConnectionType.values()) {
            if (type.getCode() == code) {
                return type;
            }
        }
        return null;
    }
}
