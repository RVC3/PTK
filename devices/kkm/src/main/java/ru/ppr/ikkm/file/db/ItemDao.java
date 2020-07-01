package ru.ppr.ikkm.file.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import ru.ppr.ikkm.file.state.model.Check;
import ru.ppr.ikkm.file.state.model.Item;

/**
 * Created by Артем on 21.01.2016.
 */
public class ItemDao extends AbstractPrinterDao {
    protected ItemDao(PrinterDaoSession database) {
        super(database);
    }

    public Item load(long id) {

        String selection = Properties.ID + " =?";
        String[] selectionArgs = new String[]{String.valueOf(id)};

        Cursor cursor = getPrinterDaoSession().getDatabase().query(Properties.TABLE_NAME, null, selection,
                selectionArgs, null, null, null);

        Item item = null;
        if (cursor.moveToFirst()) {
            item = readEntity(cursor);
            //Тут жу допишем чек, к которому относится данный элемент
            long checkId = cursor.getLong(cursor.getColumnIndex(Properties.CHECK_ID));
            Check check = getPrinterDaoSession().getCheckDao().load(checkId);
            item.setCheck(check);
        }
        cursor.close();
        return item;
    }

    public List<Item> loadItemsForCheck(Check checkId) {
        String selection = Properties.CHECK_ID + " =?";
        String[] selectionArgs = new String[]{String.valueOf(checkId.getId())};
        Cursor cursor = getPrinterDaoSession().getDatabase().query(Properties.TABLE_NAME, null, selection,
                selectionArgs, null, null, null);
        List<Item> items = new ArrayList<>(cursor.getCount());
        while (cursor.moveToFirst()){
            Item item = readEntity(cursor);
            item.setCheck(checkId);
            items.add(item);
        }
        cursor.close();
        return items;
    }

    public long save(@NonNull Item item) {
        ContentValues cv = createValues(item);
        long id = getPrinterDaoSession().getDatabase().insertOrThrow(Properties.TABLE_NAME, null, cv);
        item.setId(id);
        return id;
    }

    private ContentValues createValues(@NonNull Item item) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(Properties.CHECK_ID, item.getCheck().getId());
        contentValues.put(Properties.DESCRIPTION, item.getGoodDescription());
        contentValues.put(Properties.DISCOUNT, item.getDiscount().toString());
        contentValues.put(Properties.SUM, item.getSum().toString());
        contentValues.put(Properties.TOTAL, item.getTotal().toString());
        contentValues.put(Properties.NDS, item.getNds().toString());
        return contentValues;
    }

    public static class Properties {
        public static final String TABLE_NAME = "Item";
        public static final String ID = "_id";
        public static final String SUM = "Sum";
        public static final String DISCOUNT = "Discount";
        public static final String TOTAL = "Total";
        public static final String DESCRIPTION = "Description";
        public static final String CHECK_ID = "CheckId";
        public static final String NDS = "Nds";
    }

    public Item readEntity(Cursor cursor) {
        Item item = new Item();
        item.setId(cursor.getInt(cursor.getColumnIndex(Properties.ID)));
        item.setDiscount(new BigDecimal(cursor.getString(cursor.getColumnIndex(Properties.DISCOUNT))));
        item.setGoodDescription(cursor.getString(cursor.getColumnIndex(Properties.DESCRIPTION)));
        item.setSum(new BigDecimal(cursor.getString(cursor.getColumnIndex(Properties.SUM))));
        item.setTotal(new BigDecimal(cursor.getString(cursor.getColumnIndex(Properties.TOTAL))));
        item.setNds(new BigDecimal(cursor.getString(cursor.getColumnIndex(Properties.NDS))));
        return item;
    }

    public static void createTable(SQLiteDatabase db, boolean ifNoExist) {
        String constraint = ifNoExist? "IF NOT EXISTS ": "";
        String builder = "CREATE TABLE " + constraint + "\"" + Properties.TABLE_NAME + "\" (" +
                Properties.ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                Properties.SUM + " REAL NOT NULL," +
                Properties.CHECK_ID + " INTEGER NOT NULL, " +
                Properties.DESCRIPTION + " TEXT NOT NULL, " +
                Properties.DISCOUNT + " REAL NOT NULL, " +
                Properties.NDS + " REAL NOT NULL, " +
                Properties.TOTAL + " REAL NOT NULL)";
        db.execSQL(builder);
    }

    public static void dropTable(SQLiteDatabase db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "\"" + Properties.TABLE_NAME + "\"";
        db.execSQL(sql);
    }
}
