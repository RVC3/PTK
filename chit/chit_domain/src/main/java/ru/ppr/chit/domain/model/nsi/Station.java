package ru.ppr.chit.domain.model.nsi;

import ru.ppr.chit.domain.model.nsi.base.NsiModelWithCVD;

/**
 * Станция
 *
 * @author Aleksandr Brazhkin
 */
public class Station implements NsiModelWithCVD<Long> {

    private Integer esrCode;
    /**
     * Полное название станции
     */
    private String name;
    /**
     * Сокращённое название станции
     */
    private String shortName;
    /**
     * Код станции
     */
    private Long code;
    private int versionId;
    private Integer deleteInVersionId;

    public Integer getEsrCode() {
        return esrCode;
    }

    public void setEsrCode(Integer esrCode) {
        this.esrCode = esrCode;
    }

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
