package ru.ppr.nsi.dao;

import android.database.Cursor;

import java.util.ArrayList;
import java.util.List;

import ru.ppr.database.QueryBuilder;
import ru.ppr.database.cache.QueryCache;
import ru.ppr.nsi.NsiDaoSession;
import ru.ppr.nsi.entity.BaseNSIObject;

/**
 * Базовый DAO для таблиц НСИ.
 *
 * @param <T> Тип сущности, ассоциированой с таблицей НСИ.
 * @param <K> Тип ключа (Primary Key) для таблицы НСИ.
 * @author Aleksandr Brazhkin
 */
public abstract class BaseEntityDao<T, K> extends BaseDao {

    /**
     * Общие имена колонок для большинства таблиц НСИ.
     */
    public static class Properties {
        public static final String VersionId = "VersionId";
        public static final String DeleteInVersionId = "DeleteInVersionId";
        public static final String Code = "Code";
    }

    public BaseEntityDao(NsiDaoSession nsiDaoSession, QueryCache queryCache) {
        super(nsiDaoSession, queryCache);
    }

    /**
     * Возвращает имя таблицы, с которой ассоциирован данный DAO.
     *
     * @return Наименование таблицы
     */
    abstract String getTableName();

    /**
     * Обёртка над {@link #getTableName} для Greendao
     */
    public String getTablename() {
        return getTableName();
    }

    /**
     * Собирает сущность {@link T}, с которой ассоциирован данный DAO.
     *
     * @param cursor            Курсор с данными из таблицы
     * @return Сущность {@link T}
     */
    public abstract T fromCursor(Cursor cursor);

    /**
     * Обёртка над {@link #fromCursor} для Greendao
     */
    public T readEntity(Cursor cursor, int offset) {
        return fromCursor(cursor);
    }

    /**
     * Получает запись таблицы НСИ по коду (PrimaryKey).
     *
     * @param code      Код
     * @param versionId Версия НСИ, для которой происходит выборка
     * @return Сущность с указанным кодом
     */
    public T load(K code, int versionId) {

        StringBuilder stringBuilder = new StringBuilder();

        List<String> selectionArgsList = new ArrayList<>();

        stringBuilder.append("SELECT ");
        stringBuilder.append(" * ");
        stringBuilder.append(" FROM ");
        stringBuilder.append(getTableName());
        stringBuilder.append(" WHERE ");
        stringBuilder.append(Properties.Code).append(" = ").append("?");
        selectionArgsList.add(String.valueOf(code));
        stringBuilder.append(" AND ");
        stringBuilder.append(checkVersion(getTableName(), versionId));

        String[] selectionArgs = new String[selectionArgsList.size()];
        selectionArgsList.toArray(selectionArgs);

        Cursor cursor = null;
        T entity = null;
        try {
            cursor = db().rawQuery(stringBuilder.toString(), selectionArgs);
            if (cursor.moveToFirst()) {
                entity = fromCursor(cursor);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return entity;
    }

    public static <T> void addBaseNSIData(BaseNSIObject<T> baseNSIObject, Class<T> clazz, Cursor cursor) {
        int index = cursor.getColumnIndex(BaseEntityDao.Properties.Code);
        if (index != -1) {
            if (clazz == Integer.class) {
                ((BaseNSIObject<Integer>) baseNSIObject).setCode(cursor.getInt(index));
            } else if (clazz == Long.class) {
                ((BaseNSIObject<Long>) baseNSIObject).setCode(cursor.getLong(index));
            } else if (clazz == String.class) {
                ((BaseNSIObject<String>) baseNSIObject).setCode(cursor.getString(index));
            } else {
                throw new UnsupportedOperationException();
            }
        }
        index = cursor.getColumnIndex(BaseEntityDao.Properties.VersionId);
        if (index != -1)
            baseNSIObject.setVersionId(cursor.getInt(index));
        index = cursor.getColumnIndex(BaseEntityDao.Properties.DeleteInVersionId);
        if (index != -1)
            baseNSIObject.setDeleteInVersionId(cursor.getInt(index));
    }
}
