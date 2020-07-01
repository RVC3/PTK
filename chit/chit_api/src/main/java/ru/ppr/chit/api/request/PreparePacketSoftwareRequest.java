package ru.ppr.chit.api.request;

/**
 * @author Dmitry Nevolin
 */
public class PreparePacketSoftwareRequest extends BaseRequest {

    private String currentSoftwareVersion;

    public String getCurrentSoftwareVersion() {
        return currentSoftwareVersion;
    }

    public void setCurrentSoftwareVersion(String currentSoftwareVersion) {
        this.currentSoftwareVersion = currentSoftwareVersion;
    }

}
