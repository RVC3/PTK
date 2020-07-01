package ru.ppr.chit.api.entity;

import com.google.gson.annotations.SerializedName;

/**
 * Статус билета.
 *
 * @author Aleksandr Brazhkin
 */
public enum TicketStateEntity {

    /**
     * Действительный
     */
    @SerializedName("1")
    VALID(1),
    /**
     * Аннулирован
     */
    @SerializedName("2")
    CANCELLED(2),
    /**
     * Возвращён
     */
    @SerializedName("3")
    RETURNED(3);

    private final int code;

    TicketStateEntity(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static TicketStateEntity valueOf(int code) {
        for (TicketStateEntity ticketState : TicketStateEntity.values()) {
            if (ticketState.getCode() == code) {
                return ticketState;
            }
        }
        return null;
    }
}
