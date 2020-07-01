package ru.ppr.core.dataCarrier.pd.base;

import ru.ppr.core.dataCarrier.pd.PdVersion;

/**
 * Базоый класс для ПД v.3 и v.11.
 *
 * @author Aleksandr Brazhkin
 */
public abstract class BasePdV3V11 extends BasePdWithoutPlace implements PdWithDirection, PdWithTicketType {

    /**
     * Тип билета
     */
    @PdWithTicketType.TicketType
    private int ticketType;
    /**
     * Направление
     */
    @PdWithDirection.Direction
    private int direction;

    public BasePdV3V11(PdVersion version, int size) {
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

    @PdWithDirection.Direction
    @Override
    public int getDirection() {
        return direction;
    }

    public void setDirection(@PdWithDirection.Direction int direction) {
        this.direction = direction;
    }
}
