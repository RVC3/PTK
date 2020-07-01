package ru.ppr.chit.nsidb.entity.base;

/**
 * Описывает сущность НСИ, у которой есть:
 * - код
 * - идентификатор версии НСИ
 * - идентификатор версии НСИ для удаления
 *
 * @author Dmitry Nevolin
 */
public interface NsiEntityWithCVD<C> extends NsiEntityWithCV<C>, NsiEntityWithVD {


}
