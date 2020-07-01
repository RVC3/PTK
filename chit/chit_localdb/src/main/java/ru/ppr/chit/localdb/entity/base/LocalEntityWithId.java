package ru.ppr.chit.localdb.entity.base;

/**
 * Описывает сущность локальной базы, у которой есть:
 * - идентификатор
 *
 * @author Dmitry Nevolin
 */
public interface LocalEntityWithId<ID> {

    String PropertyId = "_id";

    ID getId();

    void setId(ID id);

}
