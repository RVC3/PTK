package ru.ppr.cppk.localdb.model.base;

/**
 * Доменная модель локальной БД с идентификатором (PK).
 *
 * @author Aleksandr Brazhkin
 */
public interface LocalModelWithId<ID> {
    ID getId();

    void setId(ID id);
}
