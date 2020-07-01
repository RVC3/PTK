package ru.ppr.chit.domain.model.nsi;

import ru.ppr.chit.domain.model.nsi.base.NsiModelWithCVD;

/**
 * Тип документа.
 *
 * @author Aleksandr Brazhkin
 */
public class CredentialDocumentType implements NsiModelWithCVD<Long> {

    private String name;
    private String shortName;
    private Long code;
    private int versionId;
    private Integer deleteInVersionId;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
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
