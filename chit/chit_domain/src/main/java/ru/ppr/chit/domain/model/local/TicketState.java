package ru.ppr.chit.domain.model.local;

import android.support.annotation.Nullable;

/**
 * Статус билета.
 *
 * @author Aleksandr Brazhkin
 */
public enum TicketState {
    /**
     * Действительный
     */
    VALID(1),
    /**
     * Аннулирован
     */
    CANCELLED(2),
    /**
     * Возвращён
     */
    RETURNED(3);

    private final int code;

    TicketState(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    @Nullable
    public static TicketState valueOf(int code) {
        for (TicketState ticketState : TicketState.values()) {
            if (ticketState.getCode() == code) {
                return ticketState;
            }
        }
        return null;
    }
}
