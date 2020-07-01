package ru.ppr.nsi.dao;

import android.database.Cursor;

import java.util.Date;

import ru.ppr.nsi.entity.NsiStatus;
import ru.ppr.logger.Logger;
import ru.ppr.nsi.NsiDaoSession;

/**
 * DAO для таблицы НСИ <i>Versions</i>.
 *
 * @author Aleksandr Brazhkin
 */
public class VersionDao extends BaseDao {

    private static final String TAG = Logger.makeLogTag(VersionDao.class);

    public static final String TABLE_NAME = "Versions";

    public static class Properties {
        public static final String CreatedDateTime = "CreatedDateTime";
        public static final String StartingDateTime = "StartingDateTime";
        public static final String Status = "Status";
    }

    /**
     * Статусы версии НСИ, с которыми допустимо работать.
     */
    private final NsiStatus[] nsiStatuses;

    public VersionDao(NsiDaoSession nsiDaoSession, NsiStatus[] nsiStatuses) {
        super(nsiDaoSession);
        this.nsiStatuses = nsiStatuses;
    }

    /**
     * Возвращает версию НСИ в определенный момент времени.
     *
     * @param date Время
     * @return
     */
    public int getVersionIdForDate(Date date) {

        StringBuilder stringBuilder = new StringBuilder();
        int versionId = -1;

        stringBuilder.append("SELECT ");
        stringBuilder.append("MAX(VersionId)");
        stringBuilder.append(" FROM ");
        stringBuilder.append(TABLE_NAME);
        stringBuilder.append(" WHERE 1=1 ");

        if (nsiStatuses != null && nsiStatuses.length > 0) {
            stringBuilder.append(" AND ");
            stringBuilder.append(Properties.Status).append(" in (");
            for (int i = 0; i < nsiStatuses.length; i++) {
                if (i != 0) stringBuilder.append(", ");
                stringBuilder.append(nsiStatuses[i].getStatusCode());
            }
            stringBuilder.append(") ");
        }

        if (date != null) {
            stringBuilder.append(" AND ");
            stringBuilder.append(Properties.StartingDateTime);
            stringBuilder.append(" < ");
            stringBuilder.append("(SELECT datetime(").append(date.getTime() / 1000).append(", 'unixepoch'))");
        }
        // http://www.sqlite.org/lang_datefunc.html
        // https://aj.srvdev.ru/browse/CPPKPP-27999

        Cursor cursor = null;
        try {
            cursor = db().rawQuery(stringBuilder.toString(), null);
            if (cursor.moveToFirst()) {
                if (!cursor.isNull(0)) {
                    versionId = cursor.getInt(0);
                }
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        if (versionId == -1) {
            Logger.trace(TAG, "Incorrect result from database while select NSI version");
        }

        return versionId;
    }

    /**
     * Проверяет корректна ли версия НСИ на ПТК
     */
    public boolean isVersionIdValid() {
        return -1 != getCurrentVersionId();
    }

    /**
     * Определяет текущую версию NSI
     *
     * @return
     */
    public int getCurrentVersionId() {
        return getVersionIdForDate(new Date());
    }
}
