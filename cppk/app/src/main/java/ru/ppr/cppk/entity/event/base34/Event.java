package ru.ppr.cppk.entity.event.base34;

import java.util.Date;
import java.util.UUID;

public class Event {

    /**
     * Первичный ключ для таблицы
     */
    private long id;

    /**
     * (заполняется автоматически, если пустой)
     */
    private UUID uuid;

    /**
     * (заполняется автоматически) в миллисекундах в utc
     */
    private Date creationTimestamp;

    /**
     * Версия НСИ при использовании которой было создано событие
     */
    private int versionId;

    /**
     * Код станции работы ПТК в случае активной работы в режиме мобильной кассы
     */
    private Long stationCode = null;

    /**
     * Локальный id устройства создания события (таблица StationDevice._id)
     */
    private long deviceId = -1;

    /**
     * отметка, что запись будет удалена сборщиком мусора
     */
    private boolean deletedMark = false;

    /**
     * Id версии ПО при которой был создан event
     */
    private long softwareUpdateEventId = -1;

    public Event() {
        this.uuid = UUID.randomUUID();
        this.creationTimestamp = new Date(System.currentTimeMillis());
    }

    public Event(UUID uuid) {
        this.uuid = uuid;
        this.creationTimestamp = new Date(System.currentTimeMillis());
    }

    public Long getStationCode() {
        return stationCode;
    }

    public void setStationCode(Long stationCode) {
        this.stationCode = stationCode;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }

    public void setVersionId(int versionId) {
        this.versionId = versionId;
    }

    public int getVersionId() {
        return versionId;
    }

    public long getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(long deviceId) {
        this.deviceId = deviceId;
    }

    public long getSoftwareUpdateEventId() {
        return softwareUpdateEventId;
    }

    public void setSoftwareUpdateEventId(long softwareUpdateEventId) {
        this.softwareUpdateEventId = softwareUpdateEventId;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public void setCreationTime(Date timestamp) {
        this.creationTimestamp = timestamp;
    }

    public Date getCreationTimestamp() {
        return creationTimestamp;
    }

    public long getCreationTimestammpInMillis() {
        return creationTimestamp.getTime();
    }

    public void setDeletedMark(boolean value) {
        this.deletedMark = value;
    }

    public boolean getDeletedMark(){
        return deletedMark;
    }
}
