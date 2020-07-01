package ru.ppr.chit.data.db;

import ru.ppr.database.Database;

/**
 * Менеджер, осуществляющий миграцию на новую версию структуры локальной БД.
 *
 * @author Aleksandr Brazhkin
 */
public interface DbMigrationManager {
    void upgradeIfNeed(Database db) throws Exception;
}
