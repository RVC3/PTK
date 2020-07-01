package ru.ppr.security.dao;

import android.database.Cursor;
import android.text.TextUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import ru.ppr.database.QueryBuilder;
import ru.ppr.logger.Logger;
import ru.ppr.security.SecurityDaoSession;
import ru.ppr.security.entity.PtsKey;

/**
 * DAO для таблицы nsi <i>PtkKeys</i>
 *
 * @author Dmitry Nevolin
 */
public class PtsKeyDao extends BaseEntityDao<PtsKey, String> {

    private static final String TAG = Logger.makeLogTag(PtsKeyDao.class);

    public static final String TABLE_NAME = "PtsKey";

    public static class Properties {
        public static final String Id = "Id";
        public static final String ComplexInstanceId = "ComplexInstanceId";
        public static final String StationExpressCode = "StationExpressCode";
        public static final String DeviceKey = "DeviceKey";
        public static final String Key = "Key";
        public static final String ValidFromTimeUtc = "ValidFromTimeUtc";
        public static final String ValidTillTimeUtc = "ValidTillTimeUtc";
    }

    final static char[] hexArray = "0123456789ABCDEF".toCharArray();

    private SimpleDateFormat simpleDateFormat;

    public PtsKeyDao(SecurityDaoSession securityDaoSession) {
        super(securityDaoSession);

        simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    @Override
    String getTableName() {
        return TABLE_NAME;
    }

    @Override
    public PtsKey fromCursor(Cursor cursor) {
        PtsKey ptsKey = new PtsKey();

        ptsKey.setId(bytesToHexString(cursor.getBlob(cursor.getColumnIndex(Properties.Id))));
        ptsKey.setComplexInstanceId(cursor.getLong(cursor.getColumnIndex(Properties.ComplexInstanceId)));
        ptsKey.setStationExpressCode(cursor.getInt(cursor.getColumnIndex(Properties.StationExpressCode)));
        ptsKey.setDeviceKey(cursor.getInt(cursor.getColumnIndex(Properties.DeviceKey)));
        ptsKey.setKey(cursor.getBlob(cursor.getColumnIndex(Properties.Key)));
        ptsKey.setValidFromTimeUtc(parseDate(cursor.getString(cursor.getColumnIndex(Properties.ValidFromTimeUtc))));
        ptsKey.setValidTillTimeUtc(parseDate(cursor.getString(cursor.getColumnIndex(Properties.ValidTillTimeUtc))));

        return ptsKey;
    }

    /**
     * Загружает список ключей ТППД с указанным deviceKey
     *
     * @return Список ключей ТППД
     */
    public List<PtsKey> loadByDeviceKey(int deviceKey) {

        QueryBuilder qb = new QueryBuilder();
        qb.selectAll().from(TABLE_NAME).where().field(Properties.DeviceKey).eq(deviceKey);

        List<PtsKey> ptsKeys = new ArrayList<>();
        Cursor cursor = null;

        try {
            cursor = qb.build().run(db());

            while (cursor.moveToNext()) {
                PtsKey ptsKey = fromCursor(cursor);
                ptsKeys.add(ptsKey);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return ptsKeys;
    }

    private synchronized Date parseDate(String dateString) {
        Date date = null;

        if (!TextUtils.isEmpty(dateString)) {
            try {
                date = simpleDateFormat.parse(dateString);
            } catch (ParseException e) {
                Logger.error(TAG, e);
            }
        }

        return date;
    }

    /**
     * Получает запись таблицы по Id.
     *
     * @param id Идентификатор
     * @return Сущность с указанным кодом
     */
    public PtsKey load(String id) {

        QueryBuilder qb = new QueryBuilder();
        qb.selectAll().from(TABLE_NAME);
        qb.where().field(Properties.Id).eq("X'" + id + "'");

        Cursor cursor = null;
        try {
            cursor = qb.build().run(db());
            if (cursor.moveToFirst()) {
                return fromCursor(cursor);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return null;
    }

    private static String bytesToHexString(byte[] bytes) {
        if (bytes == null)
            return null;

        char[] hexChars = new char[bytes.length * 2];

        for (int i = 0; i < bytes.length; i++) {
            int value = bytes[i] & 0xFF;
            int index = i * 2;

            hexChars[index++] = hexArray[value >>> 4];
            hexChars[index++] = hexArray[value & 0x0F];
        }

        return new String(hexChars);
    }

}
