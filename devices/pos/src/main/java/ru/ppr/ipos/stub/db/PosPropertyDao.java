package ru.ppr.ipos.stub.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import ru.ppr.database.Database;
import ru.ppr.ipos.model.PosProperty;

/**
 * @author Dmitry Vinogradov
 */
public class PosPropertyDao {

    private final PosStubDaoSession localDaoSession;

    public static final String TABLE_NAME = "Properties";

    public static class Properties {
        public static final String Key = "Key";
        public static final String Value = "Value";
    }

    public PosPropertyDao(@NonNull PosStubDaoSession localDaoSession) {
        this.localDaoSession = localDaoSession;
    }

    public String getTableName() {
        return TABLE_NAME;
    }

    public PosProperty fromCursor(@NonNull final Cursor cursor) {
        PosProperty posProperty = new PosProperty();
        posProperty.setPropertyKey(cursor.getString(cursor.getColumnIndex(Properties.Key)));
        posProperty.setPropertyValue(cursor.getString(cursor.getColumnIndex(Properties.Value)));
        return posProperty;
    }

    public ContentValues toContentValues(@NonNull final PosProperty entity) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(Properties.Key, entity.getPropertyKey());
        contentValues.put(Properties.Value, entity.getPropertyValue());
        return contentValues;
    }

    public String getKey(PosProperty entity) {
        return entity.getPropertyKey();
    }

    protected PosStubDaoSession getLocalDaoSession() {
        return localDaoSession;
    }

    protected Database db() {
        return getLocalDaoSession().getLocalDb();
    }

    /**
     * Возвращает наименование колонки id с именем таблицы.
     *
     * @return Наименование колонки
     */
    public String getIdWithTableName() {
        return getTableName() + "." + Properties.Key;
    }

    /**
     * Возвращает наименование колонки PK.
     *
     * @return Наименование колонки
     */
    protected String getPKColumnName() {
        return Properties.Key;
    }

    /**
     * Обёртка над {@link #fromCursor} для Greendao
     */
    public PosProperty readEntity(Cursor cursor, int offset) {
        return fromCursor(cursor);
    }

    /**
     * Получает запись таблицы локальной БД по коду (PrimaryKey).
     *
     * @param id Идентификатор
     * @return Сущность с указанным кодом
     */
    public PosProperty load(int id) {

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
        PosProperty entity = null;
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

    /**
     * Выполняет добавление сущности в БД по идентификатору.
     *
     * @param entity Сущность
     * @return row_id
     */
    public long insertOrThrow(@NonNull PosProperty entity) {
        ContentValues contentValues = toContentValues(entity);
        return db().insertOrThrow(getTableName(), null, contentValues);
    }

    /**
     * Выполняет обновление сущности в БД по идентификатору.
     *
     * @param entity Сущность
     */
    public void update(@NonNull PosProperty entity) {
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
    public void delete(@NonNull PosProperty entity) {
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

    public static void createTable(Database db, boolean ifNoExist) {
        String constraint = ifNoExist ? "IF NOT EXISTS " : "";
        String builder = "CREATE TABLE " + constraint + "\"" + TABLE_NAME + "\" (" +
                Properties.Key + " TEXT NOT NULL PRIMARY KEY, " +
                Properties.Value + " TEXT NOT NULL)";
        db.execSQL(builder);
    }

    public static void dropTable(Database db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "\"" + TABLE_NAME + "\"";
        db.execSQL(sql);
    }
}
