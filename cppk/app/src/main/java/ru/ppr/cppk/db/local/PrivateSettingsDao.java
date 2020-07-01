package ru.ppr.cppk.db.local;

import ru.ppr.cppk.db.LocalDaoSession;

/**
 * DAO для таблицы локальной БД <i>PtkSettingsPrivate</i>.
 *
 * @author Aleksandr Brazhkin
 */
public class PrivateSettingsDao extends BaseDao {

    public static final String TABLE_NAME = "PtkSettingsPrivate";

    public static class Properties {
        public static final String Name = "name";
        public static final String Value = "value";
    }

    public PrivateSettingsDao(LocalDaoSession localDaoSession) {
        super(localDaoSession);
    }
}
