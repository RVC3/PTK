package ru.ppr.chit.api.request;

/**
 * @author Dmitry Nevolin
 */
public class PreparePacketSecurityRequest {

    private int currentSecurityVersion;

    // utc время
    private String ticketWhitelistItemVersion;

    public PreparePacketSecurityRequest(int currentSecurityVersion, String TicketWhitelistItemVersion) {
        this.currentSecurityVersion = currentSecurityVersion;
        this.ticketWhitelistItemVersion = TicketWhitelistItemVersion;
    }

}
