package ru.ppr.cppk.entity.event.model;

import ru.ppr.cppk.entity.event.model.Check.AdditionalInfo;


public enum TicketKind {

    /**
     * Неизвестный тип
     */
    Unknown(666, "Неизвестный тип"),

    /**
     * Полный
     */
    Full(0, "Полный"),

    /**
     * Детский
     */
    Child(10, "Детский"),

    /**
     * Льготный
     */
    WithExemption(20, "Льготный");

    private final int ticketKind;
    private final String descriptionString;

    TicketKind(int ticketKindId, String description) {
        this.ticketKind = ticketKindId;
        descriptionString = description;
    }

    public int getTicketKind() {

        return ticketKind;
    }

    public String getDescription() {
        return descriptionString;
    }

    public static TicketKind getTicketKind(int code) {
        for (TicketKind type : TicketKind.values()) {
            if (type.getTicketKind() == code) {
                return type;
            }
        }
        return TicketKind.Unknown;
    }

    /**
     * Возвращает список типов билетов
     *
     * @return
     */
    public static TicketKind[] getAllTicketKind() {
        return new TicketKind[]{Full, Child, WithExemption};
    }

    public AdditionalInfo getAdditionalInfo() {
        if (this == Full) return AdditionalInfo.Full;
        if (this == Child) return AdditionalInfo.Child;
        if (this == WithExemption) return AdditionalInfo.WithExemption;
        return AdditionalInfo.Unknown;
    }

}
