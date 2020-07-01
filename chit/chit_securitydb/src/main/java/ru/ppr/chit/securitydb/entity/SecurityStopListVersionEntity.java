package ru.ppr.chit.securitydb.entity;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Property;

import java.util.Date;

/**
 * Версия в стоп-листа.
 *
 * @author m.sidorov
 */
@Entity(nameInDb = "SecurityStopListVersion")
public class SecurityStopListVersionEntity {

    @Property(nameInDb = "TicketWhitelistItemVersion")
    private Date ticketWhitelistItemVersion;
    @Generated(hash = 1232912109)
    public SecurityStopListVersionEntity(Date ticketWhitelistItemVersion) {
        this.ticketWhitelistItemVersion = ticketWhitelistItemVersion;
    }
    @Generated(hash = 1599479493)
    public SecurityStopListVersionEntity() {
    }
    public Date getTicketWhitelistItemVersion() {
        return this.ticketWhitelistItemVersion;
    }
    public void setTicketWhitelistItemVersion(Date ticketWhitelistItemVersion) {
        this.ticketWhitelistItemVersion = ticketWhitelistItemVersion;
    }

}
