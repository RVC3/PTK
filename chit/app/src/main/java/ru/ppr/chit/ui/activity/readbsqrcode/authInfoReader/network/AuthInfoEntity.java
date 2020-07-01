package ru.ppr.chit.ui.activity.readbsqrcode.authInfoReader.network;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * @author Dmitry Nevolin
 */
class AuthInfoEntity {

    @Expose
    @SerializedName("BaseUri")
    private String baseUri;
    @Expose
    @SerializedName("AuthorizationCode")
    private String authorizationCode;
    @Expose
    @SerializedName("ClientId")
    private String clientId;
    @Expose
    @SerializedName("ClientSecret")
    private String clientSecret;
    @Expose
    @SerializedName("TerminalId")
    private Long terminalId;
    @Expose
    @SerializedName("BaseStationId")
    private Long baseStationId;
    @Expose
    @SerializedName("Thumbprint")
    private String thumbprint;
    @Expose
    @SerializedName("SerialNumber")
    private String serialNumber;

    public String getBaseUri() {
        return baseUri;
    }

    public void setBaseUri(String baseUri) {
        this.baseUri = baseUri;
    }

    public String getAuthorizationCode() {
        return authorizationCode;
    }

    public void setAuthorizationCode(String authorizationCode) {
        this.authorizationCode = authorizationCode;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public Long getTerminalId() {
        return terminalId;
    }

    public void setTerminalId(Long terminalId) {
        this.terminalId = terminalId;
    }

    public Long getBaseStationId() {
        return baseStationId;
    }

    public void setBaseStationId(Long baseStationId) {
        this.baseStationId = baseStationId;
    }

    public String getThumbprint() {
        return thumbprint;
    }

    public void setThumbprint(String thumbprint) {
        this.thumbprint = thumbprint;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

}
