package ru.ppr.chit.data.db.migration.versions.base;

import ru.ppr.database.Database;

/**
 * Общий интерфейс миграции бд
 *
 * @author m.sidorov
 */

public interface Migration {

    void migrate(Database db) throws Exception;
    int getVersionNumber();
    String getVersionDescription();

}
