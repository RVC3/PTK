package ru.ppr.chit.domain.model.local.base;

/**
 * Описывает доменную локальную модель, у которой есть:
 * - идентификатор
 *
 * @author Dmitry Nevolin
 */
public interface LocalModelWithId<ID> {

    ID getId();

    void setId(ID id);

}
