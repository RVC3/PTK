package ru.ppr.nsi.dao;

import android.database.Cursor;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import ru.ppr.database.SqLiteUtils;
import ru.ppr.database.cache.QueryCache;
import ru.ppr.nsi.NsiDaoSession;
import ru.ppr.nsi.entity.AccessScheme;
import ru.ppr.nsi.entity.TicketStorageType;

/**
 * DAO для таблицы НСИ <i>AccessSchemes</i>.
 *
 * @author Aleksandr Brazhkin
 */
public class AccessSchemeDao extends BaseEntityDao<AccessScheme, Integer> {

    public static final String TABLE_NAME = "AccessSchemes";

    public static class Properties {
        public static final String Name = "Name";
        public static final String DeviceTypeCode = "DeviceTypeCode";
        public static final String TicketStorageTypeCode = "TicketStorageTypeCode";
        public static final String Priority = "Priority";
        public static final String SmartCardStorageType = "SmartCardStorageType";
        public static final String SamSlotNumber = "SamSlotNumber";
    }

    public AccessSchemeDao(NsiDaoSession nsiDaoSession, QueryCache queryCache) {
        super(nsiDaoSession, queryCache);
    }

    @Override
    String getTableName() {
        return TABLE_NAME;
    }

    @Override
    public AccessScheme fromCursor(Cursor cursor) {

        AccessScheme accessScheme = new AccessScheme();

        int index = cursor.getColumnIndex(Properties.Name);
        if (index != -1)
            accessScheme.setName(cursor.getString(index));

        index = cursor.getColumnIndex(Properties.DeviceTypeCode);
        if (index != -1)
            accessScheme.setDeviceTypeCode(cursor.getInt(index));

        index = cursor.getColumnIndex(Properties.TicketStorageTypeCode);
        if (index != -1)
            accessScheme.setTicketStorageTypeCode(cursor.getInt(index));

        index = cursor.getColumnIndex(Properties.Priority);
        if (index != -1)
            accessScheme.setPriority(cursor.getInt(index));

        index = cursor.getColumnIndex(Properties.SmartCardStorageType);
        if (index != -1)
            accessScheme.setSmartCardStorageType(cursor.getInt(index));

        index = cursor.getColumnIndex(AccessSchemeDao.Properties.SamSlotNumber);
        if (index != -1) {
            @AccessScheme.SamSlotNumber int samSlotNumberValue = cursor.getInt(index);
            accessScheme.setSamSlotNumber(samSlotNumberValue);
        }

        addBaseNSIData(accessScheme, Integer.class, cursor);

        return accessScheme;
    }

    public List<Integer> getAccessSchemeCodes(@Nullable List<TicketStorageType> ticketStorageTypes, int nsiVersionId) {
        List<Integer> accessSchemeCodes = new ArrayList<>();
        List<String> selectionArgsList = new ArrayList<>();

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("SELECT ");
        stringBuilder.append(BaseEntityDao.Properties.Code);
        stringBuilder.append(" FROM ");
        stringBuilder.append(AccessSchemeDao.TABLE_NAME);
        stringBuilder.append(" WHERE ");
        stringBuilder.append("1 = 1");
        if (ticketStorageTypes != null) {
            stringBuilder.append(" AND ");
            stringBuilder.append(AccessSchemeDao.Properties.TicketStorageTypeCode);
            stringBuilder.append(" IN ");
            stringBuilder.append(" ( ");
            stringBuilder.append(SqLiteUtils.makePlaceholders(ticketStorageTypes.size()));
            for (TicketStorageType ticketStorageType : ticketStorageTypes) {
                selectionArgsList.add(String.valueOf(ticketStorageType.getDBCode()));
            }
            stringBuilder.append(" ) ");
        }
        stringBuilder.append(" AND ").append(checkVersion(AccessSchemeDao.TABLE_NAME, nsiVersionId));

        String[] selectionArgs = new String[selectionArgsList.size()];
        selectionArgsList.toArray(selectionArgs);

        Cursor cursor = null;
        try {
            cursor = db().rawQuery(stringBuilder.toString(), selectionArgs);
            while (cursor.moveToNext()) {
                int code = cursor.getInt(0);
                accessSchemeCodes.add(code);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return accessSchemeCodes;
    }
}
