package ru.ppr.cppk.db.local;

import android.content.ContentValues;
import android.database.Cursor;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import ru.ppr.cppk.db.LocalDaoSession;
import ru.ppr.database.QueryBuilder;
import ru.ppr.database.base.BaseTableDao;
import ru.ppr.database.references.ReferenceInfo;

/**
 * Базовый DAO для таблиц локальной БД.
 *
 * @param <T> Тип сущности, ассоциированой с таблицей локальной БД.
 * @param <K> Тип ключа (Primary Key) для таблицы локальной БД.
 * @author Aleksandr Brazhkin
 */
public abstract class BaseEntityDao<T, K> extends BaseDao implements BaseTableDao {

    /**
     * Общие имена колонок для большинства таблиц локальной БД.
     */
    public static class Properties {
        public static final String Id = "_id";
    }

    public BaseEntityDao(LocalDaoSession localDaoSession) {
        super(localDaoSession);

        // Регистрирует таблицу в списке сущностей базы данных
        getLocalDaoSession().registerEntity(this);
    }

    /**
     * Возвращает имя таблицы, с которой ассоциирован данный DAO.
     *
     * @return Наименование таблицы
     */
    public abstract String getTableName();

    /**
     * Возвращает наименование колонки PK. (реализация для BaseTableDao)
     *
     * @return Наименование колонки
     */
    @Override
    public String getPkField(){
        return getPKColumnName();
    }

    /**
     * Возвращает наименование колонки id с именем таблицы.
     *
     * @return Наименование колонки
     */
    public String getIdWithTableName() {
        return getTableName() + "." + Properties.Id;
    }

    /**
     * Возвращает наименование колонки PK.
     *
     * @return Наименование колонки
     */
    protected String getPKColumnName() {
        return Properties.Id;
    }

    // Регистрирует ссылку на другую таблицу
    protected void registerReference(String referenceField, String masterTable, ReferenceInfo.ReferencesType referenceType){
        getLocalDaoSession().getReferences().registerReference(masterTable, Properties.Id, this.getTableName(), referenceField, referenceType);
    }

    /**
     * Собирает сущность {@link T}, с которой ассоциирован данный DAO.
     *
     * @param cursor Курсор с данными из таблицы
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
     * Собирает {@link ContentValues} из данных сущности.
     *
     * @param entity Сущность
     * @return {@link ContentValues}
     */
    public abstract ContentValues toContentValues(T entity);

    public abstract K getKey(T entity);

    /**
     * Получает запись таблицы локальной БД по коду (PrimaryKey).
     *
     * @param id Идентификатор
     * @return Сущность с указанным кодом
     */
    public T load(K id) {

        if (id == null) {
            return null;
        }

        StringBuilder stringBuilder = new StringBuilder();

        List<String> selectionArgsList = new ArrayList<>();

        stringBuilder.append("SELECT ");
        stringBuilder.append(" * ");
        stringBuilder.append(" FROM ");
        stringBuilder.append(getTableName());
        stringBuilder.append(" WHERE ");
        stringBuilder.append(getPKColumnName()).append(" = ").append("?");
        selectionArgsList.add(String.valueOf(id));

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

    @NonNull
    public List<T> loadAll() {
        QueryBuilder qb = new QueryBuilder();
        qb.selectAll().from(getTableName());

        List<T> entities = new ArrayList<>();

        Cursor cursor = qb.build().run(db());
        try {
            while (cursor.moveToNext()) {
                T entity = fromCursor(cursor);
                entities.add(entity);
            }
        } finally {
            cursor.close();
        }
        return entities;
    }

    /**
     * Выполняет добавление сущности в БД по идентификатору.
     *
     * @param entity Сущность
     * @return row_id
     */
    public long insertOrThrow(@NonNull T entity) {
        ContentValues contentValues = toContentValues(entity);
        return db().insertOrThrow(getTableName(), null, contentValues);
    }

    /**
     * Выполняет обновление сущности в БД по идентификатору.
     *
     * @param entity Сущность
     */
    public void update(@NonNull T entity) {
        String whereClause = getPKColumnName() + " = " + "?";
        String[] whereArgs = new String[]{String.valueOf(getKey(entity))};

        ContentValues contentValues = toContentValues(entity);
        db().update(getTableName(), contentValues, whereClause, whereArgs);
    }

    /**
     * Выполняет удаление сущности в БД по идентификатору.
     *
     * @param entity Сущность
     */
    public void delete(@NonNull T entity) {
        String whereClause = getPKColumnName() + " = " + "?";
        String[] whereArgs = new String[]{String.valueOf(getKey(entity))};

        db().delete(getTableName(), whereClause, whereArgs);
    }

    /**
     * Выполняет запрос к таблице.
     *
     * @param columns       Запрашиваемые имена колонок
     * @param selection     Условие выбора
     * @param selectionArgs Аргументы для условия выбора
     * @param orderBy       Тип сортировки
     * @return Курсор с данными
     */
    public Cursor query(String[] columns, String selection, String[] selectionArgs, String orderBy) {
        return db().query(getTableName(), columns, selection, selectionArgs, null, null, orderBy);
    }
}
