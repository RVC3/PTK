package ru.ppr.chit.domain.model.local;

import android.support.annotation.Nullable;

/**
 * Тип оформления билета.
 *
 * @author Aleksandr Brazhkin
 */
public enum TicketIssueType {

    /**
     * Оформлен в виде штрихкода
     */
    BARCODE(1),
    /**
     * Оформлен на БСК
     */
    SMART_CARD(2);

    private final int code;

    TicketIssueType(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    @Nullable
    public static TicketIssueType valueOf(int code) {
        for (TicketIssueType ticketIssueType : TicketIssueType.values()) {
            if (ticketIssueType.getCode() == code) {
                return ticketIssueType;
            }
        }
        return null;
    }
}
