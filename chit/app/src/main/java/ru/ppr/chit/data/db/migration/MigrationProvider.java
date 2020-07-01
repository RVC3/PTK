package ru.ppr.chit.data.db.migration;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import ru.ppr.chit.data.db.migration.versions.MigrationV2;
import ru.ppr.chit.data.db.migration.versions.MigrationV3;
import ru.ppr.chit.data.db.migration.versions.MigrationV4;
import ru.ppr.chit.data.db.migration.versions.MigrationV5;
import ru.ppr.chit.data.db.migration.versions.MigrationV6;
import ru.ppr.chit.data.db.migration.versions.base.Migration;

/**
 * Поставщик миграций, формирует список необходимых миграций по версии бд
 *
 * @author m.sidorov
 */

public class MigrationProvider {

    private final Context context;

    @Inject
    MigrationProvider(Context context) {
        this.context = context;
    }

    /**
     * Формирует список миграций, необходимых для обновления структуры базы с версии oldVersion до версии newVersion
     */
    public List<Migration> getMigrations(int oldVersion, int newVersion) {
        List<Migration> migrations = new ArrayList<>();

        switch (oldVersion) {
            case 1:
                migrations.add(new MigrationV2(context));
            case 2:
                migrations.add(new MigrationV3(context));
            case 3:
                migrations.add(new MigrationV4(context));
            case 4:
                migrations.add(new MigrationV5(context));
            case 5:
                migrations.add(new MigrationV6(context));
        }

        return migrations;
    }

}
