package ru.ppr.chit.domain.model.local;

import java.util.Date;

import ru.ppr.chit.domain.model.local.base.LocalModelWithId;

/**
 * Данные, необходимые для первичной авторизации на базовой станции.
 *
 * @author Dmitry Nevolin
 */
public class AuthInfo implements LocalModelWithId<Long> {

    /**
     * Идентификатор
     */
    private Long id;
    /**
     * Базовый адрес URL API для терминалов
     */
    private String baseUri;
    /**
     * Код авторизации устройства
     */
    private String authorizationCode;
    /**
     * Идентификатор клиентского приложения
     */
    private String clientId;
    /**
     * Служит для авторизации клиента по протоколу OAuth
     */
    private String clientSecret;
    /**
     * Идентификатор устройства, назначенный базовой станцией
     */
    private Long terminalId;
    /**
     * Идентификатор базовой станции
     */
    private String baseStationId;
    /**
     * Отпечаток серверного сертификата
     */
    private String thumbprint;
    /**
     * Серийный номер серверного сертификата
     */
    private String serialNumber;
    /**
     * Дата и время авторизации
     */
    private Date authorizationDate;

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

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

    public String getBaseStationId() {
        return baseStationId;
    }

    public void setBaseStationId(String baseStationId) {
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

    public Date getauthorizationDate() {
        return authorizationDate;
    }

    public void setAuthorizationDate(Date authorizationDate) {
        this.authorizationDate = authorizationDate;
    }

    @Override
    public String toString() {
        return "AuthInfo{" +
                "id=" + id +
                ", baseUri='" + baseUri + '\'' +
                ", authorizationCode='" + authorizationCode + '\'' +
                ", clientId='" + clientId + '\'' +
                ", clientSecret='" + clientSecret + '\'' +
                ", terminalId='" + terminalId + '\'' +
                ", thumbprint='" + thumbprint + '\'' +
                ", serialNumber='" + serialNumber + '\'' +
                '}';
    }

}
