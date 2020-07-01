package ru.ppr.core.dataCarrier.pd.base;

import ru.ppr.core.dataCarrier.pd.PdVersion;

/**
 * Базоый класс для ПД v.4, v.12, v.14, v15.
 *
 * @author Aleksandr Brazhkin
 */
public abstract class BasePdV4V12V14V15 extends BasePdWithoutPlace implements PdWithTicketType {

    /**
     * Тип билета
     */
    @PdWithTicketType.TicketType
    private int ticketType;

    public BasePdV4V12V14V15(PdVersion version, int size) {
        super(version, size);
    }

    @PdWithTicketType.TicketType
    @Override
    public int getTicketType() {
        return ticketType;
    }

    public void setTicketType(@PdWithTicketType.TicketType int ticketType) {
        this.ticketType = ticketType;
    }
}
