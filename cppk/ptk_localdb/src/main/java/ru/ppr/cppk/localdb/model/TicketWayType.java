package ru.ppr.cppk.localdb.model;

/**
 * Направление действия ПД.
 */
public enum TicketWayType {
    /**
     * Билет в одну сторону
     */
    OneWay(0),
    /**
     * Билет туда и обратно
     */
    TwoWay(1);

    /**
     * Код направления
     */
    private final int code;

    TicketWayType(int code) {
        this.code = code;
    }

    public static TicketWayType valueOf(int code) {
        for (TicketWayType type : values()) {
            if (type.getCode() == code) {
                return type;
            }
        }
        return TicketWayType.OneWay;
    }

    public int getCode() {
        return code;
    }

}
