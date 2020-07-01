package ru.ppr.chit.api.response;

import ru.ppr.chit.api.entity.PacketStatusEntity;

/**
 * @author Dmitry Nevolin
 */
public class GetPacketStatusResponse extends BaseResponse {

    /**
     * Статус пакета
     */
    private PacketStatusEntity packetStatus;
    /**
     * Хеш пакета в md5
     */
    private String packetHash;
    /**
     * Размер пакета
     */
    private long packetLength;

    public PacketStatusEntity getPacketStatus() {
        return packetStatus;
    }

    public void setPacketStatus(PacketStatusEntity packetStatus) {
        this.packetStatus = packetStatus;
    }

    public String getPacketHash() {
        return packetHash;
    }

    public void setPacketHash(String packetHash) {
        this.packetHash = packetHash;
    }

    public long getPacketLength() {
        return packetLength;
    }

    public void setPacketLength(long packetLength) {
        this.packetLength = packetLength;
    }

}
