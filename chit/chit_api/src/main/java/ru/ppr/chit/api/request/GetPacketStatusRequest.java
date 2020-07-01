package ru.ppr.chit.api.request;

import java.util.UUID;

/**
 * @author Dmitry Nevolin
 */
public class GetPacketStatusRequest extends BaseRequest {

    private UUID requestId;

    public UUID getRequestId() {
        return requestId;
    }

    public void setRequestId(UUID requestId) {
        this.requestId = requestId;
    }

}
