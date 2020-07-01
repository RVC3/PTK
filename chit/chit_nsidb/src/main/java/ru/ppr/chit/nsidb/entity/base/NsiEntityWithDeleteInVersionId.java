package ru.ppr.chit.nsidb.entity.base;

/**
 * Описывает сущность НСИ, у которой есть:
 * - идентификатор версии НСИ для удаления
 *
 * @author Dmitry Nevolin
 */
public interface NsiEntityWithDeleteInVersionId {

    String Property = "DeleteInVersionId";

    Integer getDeleteInVersionId();

    void setDeleteInVersionId(Integer deleteInVersionId);

}
