package ru.ppr.cppk.db.local;

import ru.ppr.cppk.db.LocalDaoSession;

/**
 * DAO для таблицы локальной БД <i>PtkSettingsCommon</i>.
 *
 * @author Aleksandr Brazhkin
 */
public class CommonSettingsDao extends BaseDao {

    public static final String TABLE_NAME = "PtkSettingsCommon";

    public static class Properties {
        public static final String Name = "name";
        public static final String Value = "value";
    }

    public CommonSettingsDao(LocalDaoSession localDaoSession) {
        super(localDaoSession);
    }
}
