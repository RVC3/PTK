package ru.ppr.chit.securitydb.entity;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Property;

import java.util.Date;

/**
 * Билет в белом списке.
 *
 * @author Aleksandr Brazhkin
 */
@Entity(nameInDb = "TicketWhiteListItem")
public class TicketWhiteListItemEntity {

    @Property(nameInDb = "TicketId")
    private String ticketId;
    @Property(nameInDb = "ChangedDateTime")
    private Date changedDateTime;

    @Generated(hash = 48684157)
    public TicketWhiteListItemEntity(String ticketId, Date changedDateTime) {
        this.ticketId = ticketId;
        this.changedDateTime = changedDateTime;
    }

    @Generated(hash = 121406994)
    public TicketWhiteListItemEntity() {
    }

    public String getTicketId() {
        return this.ticketId;
    }

    public void setTicketId(String ticketId) {
        this.ticketId = ticketId;
    }

    public Date getChangedDateTime() {
        return this.changedDateTime;
    }

    public void setChangedDateTime(Date changedDateTime) {
        this.changedDateTime = changedDateTime;
    }
}
