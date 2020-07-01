package ru.ppr.nsi.entity;

import android.support.annotation.Nullable;

/**
 * Вид сбора.
 * Справочник «Сборы за оформление»
 *
 * @author Aleksandr Brazhkin
 */
public enum FeeType {
    /**
     * Сбор за оформление ПД в вагоне поезда
     */
    PD_IN_TRAIN(0, "сбор за оформление ПД в вагоне поезда"),
    /**
     * Сбор за оформление багажной квитанции в вагоне поезда
     */
    BAGGAGE_IN_TRAIN(1, "сбор за оформление багажной квитанции в вагоне поезда"),
    /**
     * Сбор за оформление ПД после поездки
     */
    PD_AFTER_TRIP(2, "сбор за оформление ПД после поездки"),
    /**
     * Сбор за бронирование ПД с местом
     */
    BOOKING_PD_WITH_PLACE(3, "сбор за бронирование ПД с местом"),
    /**
     * Сбор за возврат ПД с местом;
     */
    RETURN_PD_WITH_PLACE(4, "сбор за возврат ПД с местом"),
    /**
     * Сбор за возврат абонмента
     */
    RETURN_SEASON_TICKET(5, "сбор за возврат абонемента"),
    /**
     * Минимальный сбор за возврат абонемента
     */
    MIN_FEE_FOR_RETURN_SUBSCRIPTION(6, "Минимальный сбор за возврат абонемента"),
    /**
     * Сбор за оформление трансфера в поезде
     */
    TRANSFER_IN_TRAIN(7, "Сбор за оформление трансфера в поезде"),
    /**
     * Сбор за оформление трансфера в микроавтобусе
     */
    TRANSFER_IN_BUS(8, "Сбор за оформление трансфера в автобусе");

    private final int code;

    private final String description;

    FeeType(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    @Nullable
    public static FeeType valueOf(int code) {
        for (FeeType item : FeeType.values()) {
            if (item.code == code) {
                return item;
            }
        }
        return null;
    }

}
