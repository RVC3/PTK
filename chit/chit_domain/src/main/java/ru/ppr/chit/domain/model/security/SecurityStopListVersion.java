package ru.ppr.chit.domain.model.security;

import java.util.Date;

/**
 * Created by m.sidorov.
 */
public class SecurityStopListVersion {

    private Date ticketWhitelistItemVersion;

    public Date getTicketWhitelistItemVersion() {
        return ticketWhitelistItemVersion;
    }

    public void setTicketWhitelistItemVersion(Date ticketWhitelistItemVersion) {
        this.ticketWhitelistItemVersion = ticketWhitelistItemVersion;
    }

}
