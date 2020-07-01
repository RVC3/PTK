package ru.ppr.chit.nsidb.entity;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Property;

import ru.ppr.chit.nsidb.entity.base.NsiEntityWithVD;

/**
 * @author Aleksandr Brazhkin
 */
@Entity(nameInDb = "AccessRules")
public class AccessRuleEntity implements NsiEntityWithVD {

    @Property(nameInDb = "AccessSchemeCode")
    private long accessSchemeCode;
    @Property(nameInDb = "MifareKeyCode")
    private int mifareKeyCode;
    @Property(nameInDb = "SectorNumber")
    private int sectorNumber;
    @Property(nameInDb = "SamKeyVersion")
    private int samKeyVersion;
    @Property(nameInDb = "CellNumber")
    private int cellNumber;
    @Property(nameInDb = "KeyType")
    private int keyType;
    @Property(nameInDb = "KeyName")
    private int keyName;
    @Property(nameInDb = "VersionId")
    private int versionId;
    @Property(nameInDb = "DeleteInVersionId")
    private Integer deleteInVersionId;

    @Generated(hash = 68144620)
    public AccessRuleEntity() {
    }

    @Generated(hash = 2063170999)
    public AccessRuleEntity(long accessSchemeCode, int mifareKeyCode,
            int sectorNumber, int samKeyVersion, int cellNumber, int keyType,
            int keyName, int versionId, Integer deleteInVersionId) {
        this.accessSchemeCode = accessSchemeCode;
        this.mifareKeyCode = mifareKeyCode;
        this.sectorNumber = sectorNumber;
        this.samKeyVersion = samKeyVersion;
        this.cellNumber = cellNumber;
        this.keyType = keyType;
        this.keyName = keyName;
        this.versionId = versionId;
        this.deleteInVersionId = deleteInVersionId;
    }

    public long getAccessSchemeCode() {
        return this.accessSchemeCode;
    }

    public void setAccessSchemeCode(long accessSchemeCode) {
        this.accessSchemeCode = accessSchemeCode;
    }

    public int getMifareKeyCode() {
        return this.mifareKeyCode;
    }

    public void setMifareKeyCode(int mifareKeyCode) {
        this.mifareKeyCode = mifareKeyCode;
    }

    public int getSectorNumber() {
        return this.sectorNumber;
    }

    public void setSectorNumber(int sectorNumber) {
        this.sectorNumber = sectorNumber;
    }

    public int getSamKeyVersion() {
        return this.samKeyVersion;
    }

    public void setSamKeyVersion(int samKeyVersion) {
        this.samKeyVersion = samKeyVersion;
    }

    public int getCellNumber() {
        return this.cellNumber;
    }

    public void setCellNumber(int cellNumber) {
        this.cellNumber = cellNumber;
    }

    public int getKeyType() {
        return this.keyType;
    }

    public void setKeyType(int keyType) {
        this.keyType = keyType;
    }

    public int getKeyName() {
        return this.keyName;
    }

    public void setKeyName(int keyName) {
        this.keyName = keyName;
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
