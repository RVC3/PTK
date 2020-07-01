package ru.ppr.chit.domain.model.nsi;

import ru.ppr.chit.domain.model.nsi.base.NsiModelWithCVD;

/**
 * Схема доступа.
 *
 * @author Dmitry Nevolin
 */
public class AccessScheme implements NsiModelWithCVD<Long> {

    /**
     * Название схемы
     */
    private String name;
    /**
     * Код типа устройства на полигоне, для которого схема
     */
    private int deviceTypeCode;
    /**
     * Код типа носителя билета
     */
    private long ticketStorageTypeCode;
    /**
     * Приоритет схемы
     */
    private int priority;
    /**
     * Непонятно что это, похоже что атавизм
     */
    private int smartCardStorageType;
    /**
     * Номер слота sam-модуля
     */
    private int samSlotNumber;
    /**
     * Код
     */
    private Long code;
    /**
     * Версия НСИ
     */
    private int versionId;
    /**
     * Версия удаления из НСИ
     */
    private Integer deleteInVersionId;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getDeviceTypeCode() {
        return deviceTypeCode;
    }

    public void setDeviceTypeCode(int deviceTypeCode) {
        this.deviceTypeCode = deviceTypeCode;
    }

    public long getTicketStorageTypeCode() {
        return ticketStorageTypeCode;
    }

    public void setTicketStorageTypeCode(long ticketStorageTypeCode) {
        this.ticketStorageTypeCode = ticketStorageTypeCode;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public int getSmartCardStorageType() {
        return smartCardStorageType;
    }

    public void setSmartCardStorageType(int smartCardStorageType) {
        this.smartCardStorageType = smartCardStorageType;
    }

    public int getSamSlotNumber() {
        return samSlotNumber;
    }

    public void setSamSlotNumber(int samSlotNumber) {
        this.samSlotNumber = samSlotNumber;
    }

    @Override
    public Long getCode() {
        return code;
    }

    @Override
    public void setCode(Long code) {
        this.code = code;
    }

    @Override
    public int getVersionId() {
        return versionId;
    }

    @Override
    public void setVersionId(int versionId) {
        this.versionId = versionId;
    }

    @Override
    public Integer getDeleteInVersionId() {
        return deleteInVersionId;
    }

    @Override
    public void setDeleteInVersionId(Integer deleteInVersionId) {
        this.deleteInVersionId = deleteInVersionId;
    }

}
