package ru.ppr.chit.api.entity;

import com.google.gson.annotations.SerializedName;

/**
 * Тип оформления билета.
 *
 * @author Aleksandr Brazhkin
 */
public enum TicketIssueTypeEntity {

    /**
     * Оформлен в виде штрихкода
     */
    @SerializedName("1")
    BARCODE(1),
    /**
     * Оформлен на БСК
     */
    @SerializedName("2")
    SMART_CARD(2);

    private final int code;

    TicketIssueTypeEntity(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static TicketIssueTypeEntity valueOf(int code) {
        for (TicketIssueTypeEntity ticketIssueType : TicketIssueTypeEntity.values()) {
            if (ticketIssueType.getCode() == code) {
                return ticketIssueType;
            }
        }
        return null;
    }
}
