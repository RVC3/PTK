package ru.ppr.ikkm.file.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import ru.ppr.ikkm.file.state.model.Operator;

/**
 * Created by Артем on 21.01.2016.
 */
public class OperatorDao extends AbstractPrinterDao {
    protected OperatorDao(PrinterDaoSession database) {
        super(database);
    }

    @Nullable
    public Operator load(long id) {
        String selection = Properties.ID + " =?";
        String[] selectionArgs = new String[]{String.valueOf(id)};
        Cursor cursor = getPrinterDaoSession().getDatabase().query(Properties.TABLE_NAME, null, selection,
                selectionArgs, null, null, null);

        Operator operator = null;
        if(cursor.moveToFirst()) {
            operator = readEntity(cursor);
        }
        cursor.close();
        return operator;
    }

    public long saveOrReplace(@NonNull Operator operator, long stateId) {
        ContentValues cv = createValues(operator);
        long id = getPrinterDaoSession().getDatabase().insertWithOnConflict(Properties.TABLE_NAME, null, cv,
                SQLiteDatabase.CONFLICT_REPLACE);
        operator.setId(id);
        return id;
    }

    private ContentValues createValues(@NonNull Operator operator) {
        ContentValues contentValues = new ContentValues();
        Long id = operator.getId();
        if (id != null) {
            contentValues.put(Properties.ID, id);
        }
        contentValues.put(Properties.CODE, operator.getOperatorCode());
        contentValues.put(Properties.NAME, operator.getOperatorName());
        return contentValues;
    }

    public Operator readEntity(Cursor cursor) {
        Operator operator = new Operator();
        operator.setId(cursor.getInt(cursor.getColumnIndex(Properties.ID)));
        operator.setOperatorCode((byte) cursor.getInt(cursor.getColumnIndex(Properties.CODE)));
        operator.setOperatorName(cursor.getString(cursor.getColumnIndex(Properties.NAME)));
        return operator;
    }

    public static class Properties {
        public static final String TABLE_NAME = "Operator";
        public static final String ID = "_id";
        public static final String NAME = "OperatorName";
        public static final String CODE = "OperatorCode";
    }

    public static void createTable(SQLiteDatabase db, boolean ifNoExist) {
        String constraint = ifNoExist? "IF NOT EXISTS ": "";
        StringBuilder builder = new StringBuilder();
        builder.append("CREATE TABLE ").append(constraint).append("\"").append(Properties.TABLE_NAME).append("\" (")
                .append(Properties.ID).append(" INTEGER PRIMARY KEY AUTOINCREMENT,")
                .append(Properties.CODE).append(" INTEGER NOT NULL,")
                .append(Properties.NAME).append(" TEXT NOT NULL)");
        db.execSQL(builder.toString());
    }

    public static void dropTable(SQLiteDatabase db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "\"" + Properties.TABLE_NAME + "\"";
        db.execSQL(sql);
    }
}
