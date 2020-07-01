package ru.ppr.chit.nsidb.entity.base;

/**
 * Описывает сущность НСИ, у которой есть:
 * - код
 * - идентификатор версии НСИ
 *
 * @author Dmitry Nevolin
 */
public interface NsiEntityWithCV<C> extends NsiEntityWithCode<C>, NsiEntityWithVersionId {


}
