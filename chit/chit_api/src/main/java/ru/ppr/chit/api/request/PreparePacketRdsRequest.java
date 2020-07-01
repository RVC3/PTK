package ru.ppr.chit.api.request;

/**
 * @author Dmitry Nevolin
 */
public class PreparePacketRdsRequest extends BaseRequest {

    private int currentRdsVersion;

    public int getCurrentRdsVersion() {
        return currentRdsVersion;
    }

    public void setCurrentRdsVersion(int currentRdsVersion) {
        this.currentRdsVersion = currentRdsVersion;
    }

}
