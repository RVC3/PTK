package ru.ppr.cppk.localdb.model;

import java.util.Date;

import ru.ppr.cppk.localdb.model.base.LocalModelWithId;

/**
 * Событие обновления.
 */
public class UpdateEvent implements LocalModelWithId<Long> {
    /**
     * Id
     */
    private Long id;
    /**
     * Время обновления, мс
     */
    private Date operationTime;
    /**
     * Тип обновления (НСИ, ПО, ...)
     */
    private UpdateEventType type;
    /**
     * Новая версия
     */
    private String version;
    /**
     * Версия датаконтрактов на момент события
     */
    private int dataContractVersion;

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public Date getOperationTime() {
        return operationTime;
    }

    public void setOperationTime(Date operationTime) {
        this.operationTime = operationTime;
    }

    public UpdateEventType getType() {
        return type;
    }

    public void setType(UpdateEventType type) {
        this.type = type;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public int getDataContractVersion() {
        return dataContractVersion;
    }

    public void setDataContractVersion(int dataContractVersion) {
        this.dataContractVersion = dataContractVersion;
    }
}
