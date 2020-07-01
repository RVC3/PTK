package ru.ppr.nsi.entity;

public abstract class BaseNSIObject<T> {

    private T code = null;
    private Integer versionId = null;
    private Integer deleteInVersionId = null;

    public T getCode() {
        return code;
    }

    public void setCode(T code) {
        this.code = code;
    }

    public Integer getVersionId() {
        return versionId;
    }

    public void setVersionId(Integer versionId) {
        this.versionId = versionId;
    }

    public Integer getDeleteInVersionId() {
        return deleteInVersionId;
    }

    public void setDeleteInVersionId(Integer deleteInVersionId) {
        this.deleteInVersionId = deleteInVersionId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BaseNSIObject<?> that = (BaseNSIObject<?>) o;

        if (code != null ? !code.equals(that.code) : that.code != null) return false;
        return versionId != null ? versionId.equals(that.versionId) : that.versionId == null;

    }

    @Override
    public int hashCode() {
        int result = code != null ? code.hashCode() : 0;
        result = 31 * result + (versionId != null ? versionId.hashCode() : 0);
        return result;
    }
}
