package ru.ppr.chit.domain.model.security;

import java.util.Date;

import ru.ppr.chit.domain.model.local.TicketId;

/**
 * Билет в белом списке.
 *
 * @author Aleksandr Brazhkin
 */
public class TicketWhiteListItem {

    private TicketId ticketId;
    private Date changedDateTime;

    public TicketId getTicketId() {
        return ticketId;
    }

    public void setTicketId(TicketId ticketId) {
        this.ticketId = ticketId;
    }

    public Date getChangedDateTime() {
        return changedDateTime;
    }

    public void setChangedDateTime(Date changedDateTime) {
        this.changedDateTime = changedDateTime;
    }

}
