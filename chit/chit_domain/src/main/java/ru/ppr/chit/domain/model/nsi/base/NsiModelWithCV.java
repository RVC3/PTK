package ru.ppr.chit.domain.model.nsi.base;

/**
 * Описывает доменную модель НСИ, у которой есть:
 * - код
 * - идентификатор версии НСИ
 *
 * @author Dmitry Nevolin
 */
public interface NsiModelWithCV<C> extends NsiModelWithCode<C>, NsiModelWithVersionId {


}
