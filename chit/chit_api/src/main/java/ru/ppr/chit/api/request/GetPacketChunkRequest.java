package ru.ppr.chit.api.request;

import java.util.UUID;

/**
 * @author Dmitry Nevolin
 */
public class GetPacketChunkRequest extends BaseRequest {

    /**
     * Идентификатор запроса
     */
    private UUID requestId;
    /**
     * Смещение в файле
     */
    private long packetOffset;
    /**
     * Длина чанка
     */
    private long chunkLength;

    public UUID getRequestId() {
        return requestId;
    }

    public void setRequestId(UUID requestId) {
        this.requestId = requestId;
    }

    public long getPacketOffset() {
        return packetOffset;
    }

    public void setPacketOffset(long packetOffset) {
        this.packetOffset = packetOffset;
    }

    public long getChunkLength() {
        return chunkLength;
    }

    public void setChunkLength(long chunkLength) {
        this.chunkLength = chunkLength;
    }

}
