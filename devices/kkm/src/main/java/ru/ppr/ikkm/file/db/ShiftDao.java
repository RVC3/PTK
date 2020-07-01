package ru.ppr.ikkm.file.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.common.base.Preconditions;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ru.ppr.ikkm.file.state.model.Check;
import ru.ppr.ikkm.file.state.model.Operator;
import ru.ppr.ikkm.file.state.model.Shift;
import ru.ppr.ikkm.file.state.model.ShiftState;

/**
 * Created by Артем on 21.01.2016.
 */
public class ShiftDao extends AbstractPrinterDao {

    private final static String SELECTION_FOR_STATE_ID = Properties.PRINTER_ID + " =?";

    protected ShiftDao(PrinterDaoSession database) {
        super(database);
    }

    public List<Shift> loadAll(long printerId){

        String[] selectionArgs = new String[]{String.valueOf(printerId)};
        Cursor cursor = getPrinterDaoSession().getDatabase().query(Properties.TABLE_NAME, null,
                SELECTION_FOR_STATE_ID, selectionArgs, null, null, Properties.OPEN_TIME);

        List<Shift> shifts = new ArrayList<>(cursor.getCount());
        while (cursor.moveToNext()) {
            Shift shift = readEntity(cursor);
            readAdditionInfoForShift(shift, cursor);
            shifts.add(shift);
        }
        return shifts;
    }

    public Shift load(long id) {
        String selection = Properties.ID + "=?";
        String[] selectionArgs = new String[]{String.valueOf(id)};

        Cursor cursor = getPrinterDaoSession().getDatabase().query(Properties.TABLE_NAME, null, selection,
                selectionArgs, null, null, null);

        Shift shift = null;
        if (cursor.moveToFirst()) {
            shift = readEntity(cursor);
            readAdditionInfoForShift(shift, cursor);
        }
        cursor.close();
        return shift;
    }

    /**
     * Дописывает дополнительные поля в смену
     * @param shift смена, в которую нужно сохранить информацию
     * @param cursor курсор, из которого будем брать ключи, по которым надо загрузить и нформацию
     */
    private void readAdditionInfoForShift(@NonNull Shift shift, Cursor cursor) {
        List<Check> checks = getPrinterDaoSession().getCheckDao().loadCheckForShift(shift);
        shift.setChecks(checks);
        long operatorId = cursor.getLong(cursor.getColumnIndex(Properties.OPERATOR_ID));
        Operator operator = getPrinterDaoSession().getCashierDao().load(operatorId);
        Preconditions.checkNotNull(operator, "Error load operator with id = " + operatorId);
        shift.setCashier(operator);
    }

    public long save(@NonNull Shift shift, long stateId) {

        Operator operator = shift.getCashier();
        Preconditions.checkNotNull(operator);
        getPrinterDaoSession().getCashierDao().saveOrReplace(operator, stateId);

        ContentValues cv = createValues(shift, stateId);
        long id = getPrinterDaoSession().getDatabase().insertWithOnConflict(Properties.TABLE_NAME,
                null, cv, SQLiteDatabase.CONFLICT_REPLACE);
        shift.setId(id);
        return id;
    }

    public boolean update(@NonNull Shift shift) {
        String whereClause = Properties.ID + " = " + shift.getId();
        ContentValues cv = createValues(shift, null);
        int count = getPrinterDaoSession().getDatabase().update(Properties.TABLE_NAME, cv, whereClause, null);
        return count == 1;
    }

    @Nullable
    public Shift loadLast(long printerId) {

        String[] selectionArgs = new String[]{String.valueOf(printerId)};
        String orderClause = Properties.OPEN_TIME + " DESC";
        Cursor cursor = getPrinterDaoSession().getDatabase().query(Properties.TABLE_NAME, null,
                SELECTION_FOR_STATE_ID, selectionArgs, null, null, orderClause, "1");

        Shift shift = null;
        if (cursor.moveToFirst()) {
            shift = readEntity(cursor);
            List<Check> checks = getPrinterDaoSession().getCheckDao().loadCheckForShift(shift);
            shift.setChecks(checks);
            long operatorId = cursor.getLong(cursor.getColumnIndex(Properties.OPERATOR_ID));
            Operator operator = getPrinterDaoSession().getCashierDao().load(operatorId);
            Preconditions.checkNotNull(operator, "Error load operator with id = " + operatorId);
            shift.setCashier(operator);
        }
        cursor.close();
        return shift;
    }

    private Shift readEntity(Cursor cursor) {
        Shift shift = new Shift();
        shift.setOpenShiftTime(new Date(cursor.getLong(cursor.getColumnIndex(Properties.OPEN_TIME))));
        shift.setShiftState(ShiftState.create(cursor.getInt(cursor.getColumnIndex(Properties.STATE))));
        shift.setShiftNumber(cursor.getInt(cursor.getColumnIndex(Properties.SHIFT_NUMBER)));
        shift.setId(cursor.getInt(cursor.getColumnIndex(Properties.ID)));

        int index = cursor.getColumnIndex(Properties.CLOSE_TIME);
        if (!cursor.isNull(index)) {
            shift.setCloseShiftTime(new Date(cursor.getLong(index)));
        }
        return shift;
    }

    private ContentValues createValues(@NonNull Shift shift, Long stateId) {

        ContentValues cv = new ContentValues();
        Long id = shift.getId();
        if (id != null) {
            cv.put(Properties.ID, id);
        }
        cv.put(Properties.OPEN_TIME, shift.getOpenShiftTime().getTime());
        cv.put(Properties.OPERATOR_ID, shift.getCashier().getId());
        cv.put(Properties.SHIFT_NUMBER, shift.getShiftNumber());
        cv.put(Properties.STATE, shift.getShiftState().getCode());
        cv.put(Properties.PRINTER_ID, stateId);
        Date closeTime = shift.getCloseShiftTime();
        if (closeTime != null) {
            cv.put(Properties.CLOSE_TIME, closeTime.getTime());
        }
        return cv;
    }

    public static class Properties {
        public static final String ID = "_id";
        public static final String OPEN_TIME = "OPEN_TIME";
        public static final String CLOSE_TIME = "CLOSE_TIME";
        public static final String STATE = "STATE";
        public static final String OPERATOR_ID = "OPERATOR_ID";
        public static final String SHIFT_NUMBER = "ShiftNumber";
        public static final String TABLE_NAME = "Shift";
        public static final String PRINTER_ID = "PRINTER_ID";
    }

    public static void createTable(SQLiteDatabase db, boolean ifNoExist) {
        String constraint = ifNoExist ? "IF NOT EXISTS " : "";
        String query = "CREATE TABLE " + constraint + "\"" + Properties.TABLE_NAME + "\" (" +
                Properties.ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                Properties.OPEN_TIME + " INTEGER NOT NULL," +
                Properties.CLOSE_TIME + " INTEGER, " +
                Properties.STATE + " INTEGER NOT NULL, " +
                Properties.SHIFT_NUMBER + " INTEGER NOT NULL, " +
                Properties.PRINTER_ID + " INTEGER NOT NULL, " +
                Properties.OPERATOR_ID + " INTEGER NOT NULL)";
        db.execSQL(query);
    }

    public static void dropTable(SQLiteDatabase db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "\"" + Properties.TABLE_NAME + "\"";
        db.execSQL(sql);
    }
}
