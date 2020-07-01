package ru.ppr.chit.api.auth;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * @author Dmitry Nevolin
 */
class OAuth2TokenEntity {

    @Expose
    @SerializedName("access_token")
    private String accessToken;
    @Expose
    @SerializedName("token_type")
    private String tokenType;
    @Expose
    @SerializedName("refresh_token")
    private String refreshToken;
    @Expose
    @SerializedName(".issued")
    private String issued;
    @Expose
    @SerializedName(".expires")
    private String expires;
    @Expose
    @SerializedName("expires_in")
    private long expiresIn;
    @Expose
    @SerializedName("client_id")
    private String clientId;

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getIssued() {
        return issued;
    }

    public void setIssued(String issued) {
        this.issued = issued;
    }

    public String getExpires() {
        return expires;
    }

    public void setExpires(String expires) {
        this.expires = expires;
    }

    public long getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(long expiresIn) {
        this.expiresIn = expiresIn;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

}
