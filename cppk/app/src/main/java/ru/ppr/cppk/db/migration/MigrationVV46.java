package ru.ppr.cppk.db.migration;

import android.content.Context;

import ru.ppr.database.Database;
import ru.ppr.cppk.db.migration.base.MigrationBase;

/**
 * Данный метод обновляет локальную БД до версии 46, версии ниже 46 не поддреживаются
 * <p>
 * Было принято решение удалить скрипты обновлений БД до версий ниже 46, т.к.
 * Формат данных слишком сильно поменялся и перенос данных из старых версий в 46 версию
 * потребовал бы больших усилий.
 * <p>
 * При обновлении БД до 46 версии все данные стираются!
 */
public class MigrationVV46 extends MigrationBase {

    public MigrationVV46(Context context) {
        super(context);
    }

    @Override
    public void migrate(Database localDB) throws Exception {
        migrateWithScriptFromAssets(localDB);
    }

    @Override
    public int getVersionNumber() {
        return 46;
    }

    @Override
    public String getVersionDescription() {
        return "В таблицу Exemption добавлены колонки ActiveFromDate, VersionId";
    }

}
