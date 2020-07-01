package ru.ppr.chit.nsidb.entity;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Property;

import ru.ppr.chit.nsidb.entity.base.NsiEntityWithCVD;

/**
 * Тип документа.
 *
 * @author Aleksandr Brazhkin
 */
@Entity(nameInDb = "CredentialDocumentTypes")
public class CredentialDocumentTypeEntity implements NsiEntityWithCVD<Long> {

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

    @Generated(hash = 668888029)
    public CredentialDocumentTypeEntity() {
    }

    @Generated(hash = 2010545292)
    public CredentialDocumentTypeEntity(String name, String shortName, Long code,
            int versionId, Integer deleteInVersionId) {
        this.name = name;
        this.shortName = shortName;
        this.code = code;
        this.versionId = versionId;
        this.deleteInVersionId = deleteInVersionId;
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
