package ru.ppr.cppk.db.local.repository;

import android.content.ContentValues;
import android.database.Cursor;
import android.support.annotation.NonNull;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import ru.ppr.cppk.db.local.BaseDao;
import ru.ppr.cppk.db.local.LocalDbSessionManager;
import ru.ppr.cppk.db.local.PrivateSettingsDao;
import ru.ppr.cppk.db.local.repository.base.BaseLocalDbRepository;
import ru.ppr.cppk.entity.settings.PrivateSettings;

/**
 * Репозиторий для {@link PrivateSettings}.
 *
 * @author Aleksandr Brazhkin
 */
public class PrivateSettingsRepository extends BaseLocalDbRepository<PrivateSettings> {

    @Inject
    PrivateSettingsRepository(LocalDbSessionManager localDbSessionManager) {
        super(localDbSessionManager);
    }

    @Override
    protected BaseDao dao() {
        return daoSession().privateSettingsDao();
    }

    /**
     * Получает из БД частные настройки ПТК
     *
     * @return
     */
    public PrivateSettings getPrivateSettings() {
        PrivateSettings out = new PrivateSettings();

        Cursor cursor = null;

        try {
            final String query = "select * from " + PrivateSettingsDao.TABLE_NAME;
            cursor = db().rawQuery(query, null);

            final int nameIndex = cursor.getColumnIndex(PrivateSettingsDao.Properties.Name);
            final int valueIndex = cursor.getColumnIndex(PrivateSettingsDao.Properties.Value);

            final Map<String, String> settings = new HashMap<>();

            while (cursor.moveToNext()) {
                final String name = cursor.getString(nameIndex);
                final String value = cursor.getString(valueIndex);

                settings.put(name, value);
            }

            out.setSettings(settings);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return out;
    }

    /**
     * Cохраняет изменения частных настроек ПТК
     *
     * @param settings
     */
    public void savePrivateSettings(@NonNull final PrivateSettings settings) {

        final ContentValues contentValues = new ContentValues();

        try {
            beginTransaction();

            for (Map.Entry<String, String> entry : settings.getSettings().entrySet()) {
                contentValues.put(PrivateSettingsDao.Properties.Name, entry.getKey());
                contentValues.put(PrivateSettingsDao.Properties.Value, entry.getValue());

                final String whereClause = PrivateSettingsDao.Properties.Name + " = ?";
                final String[] selectionArgs = new String[]{entry.getKey()};

                final int rows = db().update(PrivateSettingsDao.TABLE_NAME, contentValues, whereClause, selectionArgs);

                if (rows <= 0) {
                    db().insert(PrivateSettingsDao.TABLE_NAME, null, contentValues);
                }

                contentValues.clear();
            }

            setTransactionSuccessful();
        } finally {
            endTransaction();
        }

    }
}
