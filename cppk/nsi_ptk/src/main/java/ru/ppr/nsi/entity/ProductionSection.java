package ru.ppr.nsi.entity;

/**
 * Участки из НСИ
 *
 * @author G.Kashka
 */
public class ProductionSection { // table ProductionSections in NSI{

    /**
     * Код участка
     */
    private int code;
    /**
     * Название участка
     */
    private String name;
    /**
     * Является УКК
     */
    private boolean isUkk;

    private Integer versionId = null;

    public ProductionSection() {

    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getVersionId() {
        return versionId;
    }

    public void setVersionId(Integer versionId) {
        this.versionId = versionId;
    }

    public boolean isUkk() {
        return isUkk;
    }

    public void setUkk(boolean ukk) {
        isUkk = ukk;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ProductionSection that = (ProductionSection) o;

        if (code != that.getCode()) return false;
        if (isUkk != that.isUkk()) return false;
        if (name != null ? !name.equals(that.getName()) : that.getName() != null) return false;
        return versionId != null ? versionId.equals(that.getVersionId()) : that.getVersionId() == null;

    }

    @Override
    public int hashCode() {
        int result = code;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (isUkk ? 1 : 0);
        result = 31 * result + (versionId != null ? versionId.hashCode() : 0);
        return result;
    }
}
