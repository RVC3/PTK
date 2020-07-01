package ru.ppr.chit.domain.model.nsi;

import ru.ppr.chit.domain.model.nsi.base.NsiModelWithCVD;

/**
 * Категория ПД.
 *
 * @author Aleksandr Brazhkin
 */
public class TicketCategory implements NsiModelWithCVD<Long> {
    /**
     * Наименование
     */
    private String name;
    /**
     * Код станции
     */
    private Long code;
    /**
     * Версия НСИ, в которой была добавлена запись
     */
    private int versionId;
    /**
     * Версия НСИ, в которой была удалена запись
     */
    private Integer deleteInVersionId;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
