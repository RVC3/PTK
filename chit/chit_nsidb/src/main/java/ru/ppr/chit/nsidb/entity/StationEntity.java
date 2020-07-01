package ru.ppr.chit.nsidb.entity;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Property;
import org.greenrobot.greendao.annotation.Generated;

import ru.ppr.chit.nsidb.entity.base.NsiEntityWithCVD;

/**
 * Станция.
 *
 * @author Aleksandr Brazhkin
 */
@Entity(nameInDb = "Stations")
public class StationEntity implements NsiEntityWithCVD<Long> {

    @Property(nameInDb = "EsrCode")
    private Integer esrCode;
    @Property(nameInDb = "Name")
    private String name;
    @Property(nameInDb = "ShortName")
    private String shortName;
    @Property(nameInDb = "Code")
    private Long code;
    @Property(nameInDb = "VersionId")
    private int versionId;
    @Property(nameInDb = "DeleteInVersionId")
    private Integer deleteInVersionId;
    @Generated(hash = 1264484337)
    public StationEntity(Integer esrCode, String name, String shortName, Long code,
            int versionId, Integer deleteInVersionId) {
        this.esrCode = esrCode;
        this.name = name;
        this.shortName = shortName;
        this.code = code;
        this.versionId = versionId;
        this.deleteInVersionId = deleteInVersionId;
    }
    @Generated(hash = 1480181119)
    public StationEntity() {
    }
    public Integer getEsrCode() {
        return this.esrCode;
    }
    public void setEsrCode(Integer esrCode) {
        this.esrCode = esrCode;
    }
    public String getName() {
        return this.name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getShortName() {
        return this.shortName;
    }
    public void setShortName(String shortName) {
        this.shortName = shortName;
    }
    public Long getCode() {
        return this.code;
    }
    public void setCode(Long code) {
        this.code = code;
    }
    public int getVersionId() {
        return this.versionId;
    }
    public void setVersionId(int versionId) {
        this.versionId = versionId;
    }
    public Integer getDeleteInVersionId() {
        return this.deleteInVersionId;
    }
    public void setDeleteInVersionId(Integer deleteInVersionId) {
        this.deleteInVersionId = deleteInVersionId;
    }

}
