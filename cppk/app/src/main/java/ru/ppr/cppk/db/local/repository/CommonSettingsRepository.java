package ru.ppr.cppk.db.local.repository;

import android.content.ContentValues;
import android.database.Cursor;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import ru.ppr.cppk.Globals;
import ru.ppr.cppk.db.local.BaseDao;
import ru.ppr.cppk.db.local.CommonSettingsDao;
import ru.ppr.cppk.db.local.LocalDbSessionManager;
import ru.ppr.cppk.db.local.repository.base.BaseLocalDbRepository;
import ru.ppr.cppk.entity.settings.CommonSettings;
import ru.ppr.cppk.settings.SharedPreferencesUtils;
import ru.ppr.database.QueryBuilder;

/**
 * Репозиторий для {@link CommonSettings}.
 *
 * @author Aleksandr Brazhkin
 */
@Singleton
public class CommonSettingsRepository extends BaseLocalDbRepository<CommonSettings> {

    @Inject
    CommonSettingsRepository(LocalDbSessionManager localDbSessionManager) {
        super(localDbSessionManager);
    }

    @Override
    protected BaseDao dao() {
        return daoSession().commonSettingsDao();
    }

    public CommonSettings load() {
        final CommonSettings commonSettings = new CommonSettings();

        QueryBuilder qb = new QueryBuilder();
        qb.selectAll().from(CommonSettingsDao.TABLE_NAME);

        Cursor cursor = qb.build().run(db());

        try {
            final int nameIndex = cursor.getColumnIndex(CommonSettingsDao.Properties.Name);
            final int valueIndex = cursor.getColumnIndex(CommonSettingsDao.Properties.Value);

            final Map<String, String> settings = new HashMap<>();

            while (cursor.moveToNext()) {
                final String name = cursor.getString(nameIndex);
                final String value = cursor.getString(valueIndex);

                settings.put(name, value);
            }

            commonSettings.setSettings(settings);
        } finally {
            cursor.close();
        }

        return commonSettings;
    }

    public void update(CommonSettings commonSettings) {
        final ContentValues contentValues = new ContentValues();

        try {
            db().beginTransaction();
            for (Map.Entry<String, String> entry : commonSettings.getSettings().entrySet()) {
                contentValues.put(CommonSettingsDao.Properties.Name, entry.getKey());
                contentValues.put(CommonSettingsDao.Properties.Value, entry.getValue());

                final String whereClause = CommonSettingsDao.Properties.Name + " = ?";
                final String[] selectionArgs = new String[]{entry.getKey()};

                final int rows = db().update(CommonSettingsDao.TABLE_NAME, contentValues, whereClause, selectionArgs);
                if (rows <= 0) {
                    db().insert(CommonSettingsDao.TABLE_NAME, null, contentValues);
                }
                contentValues.clear();
            }
            db().setTransactionSuccessful();
        } finally {
            db().endTransaction();
        }
    }
}
