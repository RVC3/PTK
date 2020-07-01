package ru.ppr.chit.nsidb.entity;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Property;

import ru.ppr.chit.nsidb.entity.base.NsiEntityWithCVD;

/**
 * @author Aleksandr Brazhkin
 */
@Entity(nameInDb = "AccessSchemes")
public class AccessSchemeEntity implements NsiEntityWithCVD<Long> {
    @Property(nameInDb = "Name")
    private String name;
    @Property(nameInDb = "DeviceTypeCode")
    private int deviceTypeCode;
    @Property(nameInDb = "TicketStorageTypeCode")
    private long ticketStorageTypeCode;
    @Property(nameInDb = "Priority")
    private int priority;
    @Property(nameInDb = "SmartCardStorageType")
    private int smartCardStorageType;
    @Property(nameInDb = "SamSlotNumber")
    private int samSlotNumber;
    @Property(nameInDb = "Code")
    private Long code;
    @Property(nameInDb = "VersionId")
    private int versionId;
    @Property(nameInDb = "DeleteInVersionId")
    private Integer deleteInVersionId;

    @Generated(hash = 563554792)
    public AccessSchemeEntity(String name, int deviceTypeCode,
                              long ticketStorageTypeCode, int priority, int smartCardStorageType,
                              int samSlotNumber, Long code, int versionId,
                              Integer deleteInVersionId) {
        this.name = name;
        this.deviceTypeCode = deviceTypeCode;
        this.ticketStorageTypeCode = ticketStorageTypeCode;
        this.priority = priority;
        this.smartCardStorageType = smartCardStorageType;
        this.samSlotNumber = samSlotNumber;
        this.code = code;
        this.versionId = versionId;
        this.deleteInVersionId = deleteInVersionId;
    }

    @Generated(hash = 1419028664)
    public AccessSchemeEntity() {
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getDeviceTypeCode() {
        return this.deviceTypeCode;
    }

    public void setDeviceTypeCode(int deviceTypeCode) {
        this.deviceTypeCode = deviceTypeCode;
    }

    public long getTicketStorageTypeCode() {
        return this.ticketStorageTypeCode;
    }

    public void setTicketStorageTypeCode(long ticketStorageTypeCode) {
        this.ticketStorageTypeCode = ticketStorageTypeCode;
    }

    public int getPriority() {
        return this.priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public int getSmartCardStorageType() {
        return this.smartCardStorageType;
    }

    public void setSmartCardStorageType(int smartCardStorageType) {
        this.smartCardStorageType = smartCardStorageType;
    }

    public int getSamSlotNumber() {
        return this.samSlotNumber;
    }

    public void setSamSlotNumber(int samSlotNumber) {
        this.samSlotNumber = samSlotNumber;
    }

    @Override
    public Long getCode() {
        return this.code;
    }

    @Override
    public void setCode(Long code) {
        this.code = code;
    }

    @Override
    public int getVersionId() {
        return this.versionId;
    }

    @Override
    public void setVersionId(int versionId) {
        this.versionId = versionId;
    }

    @Override
    public Integer getDeleteInVersionId() {
        return this.deleteInVersionId;
    }

    @Override
    public void setDeleteInVersionId(Integer deleteInVersionId) {
        this.deleteInVersionId = deleteInVersionId;
    }
}
