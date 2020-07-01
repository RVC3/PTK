package ru.ppr.chit.nsidb.entity.base;

/**
 * Описывает сущность НСИ, у которой есть:
 * - идентификатор версии НСИ
 * - идентификатор версии НСИ для удаления
 *
 * @author Dmitry Nevolin
 */
public interface NsiEntityWithVD extends NsiEntityWithVersionId, NsiEntityWithDeleteInVersionId {


}
