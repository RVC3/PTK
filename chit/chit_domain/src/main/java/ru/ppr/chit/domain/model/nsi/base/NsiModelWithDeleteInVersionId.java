package ru.ppr.chit.domain.model.nsi.base;

/**
 * Описывает доменную модель НСИ, у которой есть:
 * - идентификатор версии НСИ для удаления
 *
 * @author Dmitry Nevolin
 */
public interface NsiModelWithDeleteInVersionId {

    Integer getDeleteInVersionId();

    void setDeleteInVersionId(Integer deleteInVersionId);

}
