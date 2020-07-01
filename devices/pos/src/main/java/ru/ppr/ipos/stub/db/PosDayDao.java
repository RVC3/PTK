package ru.ppr.ipos.stub.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import ru.ppr.database.Database;
import ru.ppr.ipos.model.PosDay;

/**
 * @author Dmitry Vinogradov
 */
public class PosDayDao {

    private final PosStubDaoSession localDaoSession;

    public static final String TABLE_NAME = "PosDay";

    public static class Properties {
        public static final String Id = "_id";
        public static final String Closed = "Closed";
    }

    public PosDayDao(@NonNull PosStubDaoSession localDaoSession) {
        this.localDaoSession = localDaoSession;
    }

    public String getTableName() {
        return TABLE_NAME;
    }

    public PosDay fromCursor(@NonNull final Cursor cursor) {
        PosDay posDay = new PosDay();
        posDay.setId(cursor.getInt(cursor.getColumnIndex(PosDayDao.Properties.Id)));
        posDay.setClosed(cursor.getInt(cursor.getColumnIndex(PosDayDao.Properties.Closed)) != 0);

        return posDay;
    }

    public ContentValues toContentValues(@NonNull final PosDay entity) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(Properties.Closed, entity.isClosed());
        return contentValues;
    }

    public int getKey(PosDay entity) {
        return entity.getId();
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
        return getTableName() + "." + PosDayDao.Properties.Id;
    }

    /**
     * Возвращает наименование колонки PK.
     *
     * @return Наименование колонки
     */
    protected String getPKColumnName() {
        return PosDayDao.Properties.Id;
    }

    /**
     * Обёртка над {@link #fromCursor} для Greendao
     */
    public PosDay readEntity(Cursor cursor, int offset) {
        return fromCursor(cursor);
    }

    /**
     * Получает запись таблицы локальной БД по коду (PrimaryKey).
     *
     * @param id Идентификатор
     * @return Сущность с указанным кодом
     */
    public PosDay load(int id) {

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
        PosDay entity = null;
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
    public long insertOrThrow(@NonNull PosDay entity) {
        ContentValues contentValues = toContentValues(entity);
        return db().insertOrThrow(getTableName(), null, contentValues);
    }

    /**
     * Выполняет обновление сущности в БД по идентификатору.
     *
     * @param entity Сущность
     */
    public void update(@NonNull PosDay entity) {
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
    public void delete(@NonNull PosDay entity) {
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
                Properties.Id + " INTEGER PRIMARY KEY, " +
                Properties.Closed + " INTEGER NOT NULL)";
        db.execSQL(builder);
    }

    public static void dropTable(Database db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "\"" + TABLE_NAME + "\"";
        db.execSQL(sql);
    }
}
