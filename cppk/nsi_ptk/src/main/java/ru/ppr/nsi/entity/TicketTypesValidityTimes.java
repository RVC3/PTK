package ru.ppr.nsi.entity;

/**
 * Время действия типа билета.
 */
public class TicketTypesValidityTimes {

    private final int ticketTypeCode;
    private final int validFrom; // количество секунд с начала дня, после которого данный тип билета начинает действовать
    private final int validTo; // количество секунд с начала дня, до которого данный тип билета начинает действовать

    public TicketTypesValidityTimes(int ticketTypeCode, int validFrom, int validTo) {
        this.ticketTypeCode = ticketTypeCode;
        this.validFrom = validFrom;
        this.validTo = validTo;
    }

    public int getTicketTypeCode() {
        return ticketTypeCode;
    }

    public int getValidFrom() {
        return validFrom;
    }

    public int getValidTo() {
        return validTo;
    }
}
