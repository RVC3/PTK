package ru.ppr.nsi.dao;

import android.database.Cursor;
import android.text.TextUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import ru.ppr.database.Query;
import ru.ppr.database.QueryBuilder;
import ru.ppr.database.cache.QueryCache;
import ru.ppr.logger.Logger;
import ru.ppr.nsi.NsiDaoSession;
import ru.ppr.nsi.entity.Version;

/**
 * DAO для таблицы НСИ <i>Versions</i>.
 *
 * @author Aleksandr Brazhkin
 */
public class VersionDao extends BaseEntityDao<Version, Integer> {

    private static final String TAG = Logger.makeLogTag(VersionDao.class);

    public static final String TABLE_NAME = "Versions";

    public static class Properties {
        public static final String VersionId = "VersionId";
        public static final String Description = "Description";
        public static final String CreatedDateTime = "CreatedDateTime";
        public static final String StartingDateTime = "StartingDateTime";
        public static final String Status = "Status";
        public static final String IsCriticalChange = "IsCriticalChange";
    }

    private SimpleDateFormat simpleDateFormat;

    public VersionDao(NsiDaoSession nsiDaoSession, QueryCache queryCache) {
        super(nsiDaoSession, queryCache);
        simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    @Override
    String getTableName() {
        return TABLE_NAME;
    }

    @Override
    public Version fromCursor(Cursor cursor) {
        Version version = new Version();
        version.setVersionId(cursor.getInt(cursor.getColumnIndex(Properties.VersionId)));
        version.setDescription(cursor.getString(cursor.getColumnIndex(Properties.Description)));
        version.setCreatedDateTime(parseDate(cursor.getString(cursor.getColumnIndex(Properties.CreatedDateTime))));
        version.setStartedDateTime(parseDate(cursor.getString(cursor.getColumnIndex(Properties.StartingDateTime))));
        @Version.Status int status = cursor.getInt(cursor.getColumnIndex(Properties.Status));
        version.setStatus(status);
        version.setIsCriticalChange(cursor.getInt(cursor.getColumnIndex(Properties.IsCriticalChange)) == 1);
        return version;
    }

    private synchronized Date parseDate(String dateString) {

        if (TextUtils.isEmpty(dateString)) {
            return null;
        }

        Date date = null;

        try {
            date = simpleDateFormat.parse(dateString);
        } catch (ParseException e) {
            Logger.error(TAG, e);
        }

        return date;
    }
}
