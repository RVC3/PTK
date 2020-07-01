package ru.ppr.security.entity;

import java.util.Arrays;
import java.util.Date;

/**
 * Ключ ТППД
 *
 * @author Dmitry Nevolin
 */
public class PtsKey {
    /**
     * Идентфикатор объекта
     */
    private String id;
    /**
     * Идентфикатор Терминала ППД, для которого сформирован ключ
     *
     * прим. это DeviceId
     */
    private long complexInstanceId;
    /**
     * Код станции установки Терминала ППД
     */
    private int stationExpressCode;
    /**
     * Ключ устройства ТППД
     *
     * прим. это 16 битный суррогатный ключ
     */
    private int deviceKey;
    /**
     * Ключ (16 байт)
     */
    private byte[] key;
    /**
     * Дата начала действия
     */
    private Date validFromTimeUtc;
    /**
     * Дата окончания действия
     */
    private Date validTillTimeUtc;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public long getComplexInstanceId() {
        return complexInstanceId;
    }

    public void setComplexInstanceId(long complexInstanceId) {
        this.complexInstanceId = complexInstanceId;
    }

    public int getStationExpressCode() {
        return stationExpressCode;
    }

    public void setStationExpressCode(int stationExpressCode) {
        this.stationExpressCode = stationExpressCode;
    }

    public int getDeviceKey() {
        return deviceKey;
    }

    public void setDeviceKey(int deviceKey) {
        this.deviceKey = deviceKey;
    }

    public byte[] getKey() {
        return key;
    }

    public void setKey(byte[] key) {
        this.key = key;
    }

    public Date getValidFromTimeUtc() {
        return validFromTimeUtc;
    }

    public void setValidFromTimeUtc(Date validFromTimeUtc) {
        this.validFromTimeUtc = validFromTimeUtc;
    }

    public Date getValidTillTimeUtc() {
        return validTillTimeUtc;
    }

    public void setValidTillTimeUtc(Date validTillTimeUtc) {
        this.validTillTimeUtc = validTillTimeUtc;
    }

    @Override
    public String toString() {
        return "PtsKey{" +
                "id='" + id + '\'' +
                ", complexInstanceId=" + complexInstanceId +
                ", stationExpressCode=" + stationExpressCode +
                ", deviceKey=" + deviceKey +
                ", key=" + Arrays.toString(key) +
                ", validFromTimeUtc=" + validFromTimeUtc +
                ", validTillTimeUtc=" + validTillTimeUtc +
                '}';
    }
}
