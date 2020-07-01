package ru.ppr.chit.domain.model.local;

import ru.ppr.chit.domain.model.local.base.LocalModelWithId;

/**
 * Данные о токене авторизации посредством протокола OAuth2
 *
 * @author Dmitry Nevolin
 */
public class OAuth2Token implements LocalModelWithId<Long> {

    private Long id;
    private String accessToken;
    private String tokenType;
    private String refreshToken;
    private String issued;
    private String expires;
    private Long expiresIn;
    private String clientId;
    private boolean broken;
    private Long authInfoId;

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

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

    public Long getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(Long expiresIn) {
        this.expiresIn = expiresIn;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public boolean isBroken() {
        return broken;
    }

    public void setBroken(boolean broken) {
        this.broken = broken;
    }

    public Long getAuthInfoId() {
        return authInfoId;
    }

    public void setAuthInfoId(Long authInfoId) {
        this.authInfoId = authInfoId;
    }

    @Override
    public String toString() {
        return "OAuth2Token{" +
                "id=" + id +
                ", accessToken='" + accessToken + '\'' +
                ", tokenType='" + tokenType + '\'' +
                ", refreshToken='" + refreshToken + '\'' +
                ", issued='" + issued + '\'' +
                ", expires='" + expires + '\'' +
                ", expiresIn=" + expiresIn +
                ", clientId='" + clientId + '\'' +
                ", broken=" + broken +
                ", authInfoId=" + authInfoId +
                '}';
    }

}
