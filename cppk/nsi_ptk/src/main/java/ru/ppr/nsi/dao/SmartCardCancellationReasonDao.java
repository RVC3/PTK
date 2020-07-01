package ru.ppr.nsi.dao;

import android.database.Cursor;

import java.util.ArrayList;
import java.util.List;

import ru.ppr.database.cache.QueryCache;
import ru.ppr.nsi.NsiDaoSession;
import ru.ppr.nsi.entity.RepealReason;

/**
 * DAO для таблицы НСИ <i>SmartCardCancellationReasons</i>.
 *
 * @author Aleksandr Brazhkin
 */
public class SmartCardCancellationReasonDao extends BaseEntityDao<RepealReason, Integer> {

    public static final String TABLE_NAME = "SmartCardCancellationReasons";

    public static class Properties {
        public static final String ShortName = "ShortName";
        public static final String Code = "Code";
    }

    public SmartCardCancellationReasonDao(NsiDaoSession nsiDaoSession, QueryCache queryCache) {
        super(nsiDaoSession, queryCache);
    }

    @Override
    String getTableName() {
        return TABLE_NAME;
    }

    @Override
    public RepealReason fromCursor(Cursor cursor) {
        RepealReason repealReason = new RepealReason();

        int index = cursor.getColumnIndex(SmartCardCancellationReasonDao.Properties.Code);
        if (index != -1)
            repealReason.setCodeReason(cursor.getInt(index));

        index = cursor.getColumnIndex(SmartCardCancellationReasonDao.Properties.ShortName);
        if (index != -1)
            repealReason.setReasonRepeal(cursor.getString(index));

        return repealReason;
    }

    /**
     * Возвращает список причин аннулирования из НСИ.
     */
    public List<RepealReason> getRepailReasons(int nsiVersion) {
        List<RepealReason> list = new ArrayList<RepealReason>();

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(BaseEntityDao.Properties.VersionId).append(" <= ").append(nsiVersion).append(" AND ").append("(").append(BaseEntityDao.Properties.DeleteInVersionId)
                .append(" > ").append(nsiVersion).append(" OR ").append(BaseEntityDao.Properties.DeleteInVersionId).append(" is NULL)");

        Cursor cursor = null;
        try {
            cursor = db().query(SmartCardCancellationReasonDao.TABLE_NAME, null, stringBuilder.toString(), null, null, null, null);
            while (cursor.moveToNext()) {
                RepealReason repealReason = fromCursor(cursor);
                list.add(repealReason);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return list;

    }
}
