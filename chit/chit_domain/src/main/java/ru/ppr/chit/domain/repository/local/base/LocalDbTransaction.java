package ru.ppr.chit.domain.repository.local.base;

/**
 * Транзакция локальной БД.
 *
 * @author Aleksandr Brazhkin
 */
public interface LocalDbTransaction {
    void begin();

    void end();

    void commit();
}
