package ru.ppr.nsi.dao;

import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import ru.ppr.database.SqLiteUtils;
import ru.ppr.database.cache.QueryCache;
import ru.ppr.nsi.NsiDaoSession;
import ru.ppr.nsi.entity.AccessRule;
import ru.ppr.nsi.entity.AccessScheme;
import ru.ppr.nsi.entity.DeviceType;

/**
 * DAO для таблицы НСИ <i>AccessRules</i>.
 *
 * @author Aleksandr Brazhkin
 */
public class AccessRuleDao extends BaseEntityDao<AccessRule, Integer> {

    public static final String TABLE_NAME = "AccessRules";

    public static class Properties {
        public static final String AccessSchemeCode = "AccessSchemeCode";
        public static final String MifareKeyCode = "MifareKeyCode";
        public static final String SectorNumber = "SectorNumber";
        public static final String SamKeyVersion = "SamKeyVersion";
        public static final String CellNumber = "CellNumber";
        public static final String KeyType = "KeyType";
        public static final String KeyName = "KeyName";
    }

    public AccessRuleDao(NsiDaoSession nsiDaoSession, QueryCache queryCache) {
        super(nsiDaoSession, queryCache);
    }

    @Override
    String getTableName() {
        return TABLE_NAME;
    }

    @Override
    public AccessRule fromCursor(Cursor cursor) {

        AccessRule accessRule = new AccessRule();

        int index = cursor.getColumnIndex(AccessRuleDao.Properties.MifareKeyCode);
        if (index != -1)
            accessRule.setMifareKeyCode(cursor.getInt(index));

        index = cursor.getColumnIndex(AccessRuleDao.Properties.SectorNumber);
        if (index != -1)
            accessRule.setSectorNumber((byte) cursor.getInt(index));

        index = cursor.getColumnIndex(AccessRuleDao.Properties.SamKeyVersion);
        if (index != -1)
            accessRule.setSamKeyVersion((byte) cursor.getInt(index));

        index = cursor.getColumnIndex(AccessRuleDao.Properties.CellNumber);
        if (index != -1)
            accessRule.setCellNumber((byte) cursor.getInt(index));

        index = cursor.getColumnIndex(AccessRuleDao.Properties.KeyType);
        if (index != -1) {
            @AccessRule.KeyType int keyTypeValue = cursor.getInt(index);
            accessRule.setKeyType(keyTypeValue);
        }
        index = cursor.getColumnIndex(AccessRuleDao.Properties.KeyName);
        if (index != -1) {
            @AccessRule.KeyName int keyNameValue = cursor.getInt(index);
            accessRule.setKeyName(keyNameValue);
        }

        addBaseNSIData(accessRule, Integer.class, cursor);

        return accessRule;
    }

    /**
     * Получает список правил доступа для сектора карты
     *
     * @param sectorNumber           - номер сектора карты на котором планируем авторизоваться
     * @param accessSchemeCodesIn    - список разрешенных для поиска типов карт, если null - тогда все карты
     * @param keyTypes               - список типов доступа
     * @param accessSchemeCodesNotIn - набор точно неподходящих схем
     */
    @NonNull
    public Map<AccessScheme, List<AccessRule>> getNewAccess(int sectorNumber,
                                                            @NonNull List<Integer> keyTypes,
                                                            @Nullable List<Integer> accessSchemeCodesIn,
                                                            @Nullable List<Integer> accessSchemeCodesNotIn,
                                                            int nsiVersionId) {
        Map<AccessScheme, List<AccessRule>> map = new LinkedHashMap<>();

        List<String> selectionArgsList = new ArrayList<>();

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("SELECT ");
        stringBuilder.append("*");
        stringBuilder.append(" FROM ");
        stringBuilder.append(AccessRuleDao.TABLE_NAME + ", " + AccessSchemeDao.TABLE_NAME);
        stringBuilder.append(" WHERE ");
        stringBuilder.append(AccessSchemeDao.TABLE_NAME).append(".").append(BaseEntityDao.Properties.Code);
        stringBuilder.append(" = ");
        stringBuilder.append(AccessRuleDao.TABLE_NAME).append(".").append(Properties.AccessSchemeCode);
        stringBuilder.append(" AND ");
        stringBuilder.append(AccessSchemeDao.Properties.DeviceTypeCode).append(" = ").append("?");
        selectionArgsList.add(String.valueOf(DeviceType.Ptk.getCode()));
        stringBuilder.append(" AND ");
        stringBuilder.append(Properties.SectorNumber).append(" = ").append("?");
        selectionArgsList.add(String.valueOf(sectorNumber));
        if (!keyTypes.isEmpty()) {
            stringBuilder.append(" AND ");
            stringBuilder.append(Properties.KeyType);
            stringBuilder.append(" IN ");
            stringBuilder.append(" ( ");
            stringBuilder.append(SqLiteUtils.makePlaceholders(keyTypes.size()));
            for (Integer keyType : keyTypes) {
                selectionArgsList.add(String.valueOf(keyType));
            }
            stringBuilder.append(" ) ");
        }
        if (accessSchemeCodesIn != null) {
            stringBuilder.append(" AND ");
            stringBuilder.append(AccessSchemeDao.TABLE_NAME).append(".").append(BaseEntityDao.Properties.Code);
            stringBuilder.append(" IN ");
            stringBuilder.append(" ( ");
            stringBuilder.append(SqLiteUtils.makePlaceholders(accessSchemeCodesIn.size()));
            for (Integer code : accessSchemeCodesIn) {
                selectionArgsList.add(String.valueOf(code));
            }
            stringBuilder.append(" ) ");
        }
        if (accessSchemeCodesNotIn != null) {
            stringBuilder.append(" AND ");
            stringBuilder.append(AccessSchemeDao.TABLE_NAME).append(".").append(BaseEntityDao.Properties.Code);
            stringBuilder.append(" NOT IN ");
            stringBuilder.append(" ( ");
            stringBuilder.append(SqLiteUtils.makePlaceholders(accessSchemeCodesNotIn.size()));
            for (Integer ignoredAccessSchemeCode : accessSchemeCodesNotIn) {
                selectionArgsList.add(String.valueOf(ignoredAccessSchemeCode));
            }
            stringBuilder.append(" ) ");
        }
        stringBuilder.append(" AND ").append(checkVersion(AccessRuleDao.TABLE_NAME, nsiVersionId));
        stringBuilder.append(" AND ").append(checkVersion(AccessSchemeDao.TABLE_NAME, nsiVersionId));
        stringBuilder.append(" ORDER BY ").append(AccessSchemeDao.Properties.Priority).append(",").append(AccessSchemeDao.Properties.TicketStorageTypeCode);

        String[] selectionArgs = new String[selectionArgsList.size()];
        selectionArgsList.toArray(selectionArgs);

        Cursor cursor = null;
        try {
            cursor = db().rawQuery(stringBuilder.toString(), selectionArgs);
            while (cursor.moveToNext()) {
                AccessScheme accessScheme = getNsiDaoSession().getAccessSchemeDao().fromCursor(cursor);
                AccessRule accessRule = fromCursor(cursor);
                List<AccessRule> accessRules = map.get(accessScheme);
                if (accessRules == null) {
                    accessRules = new ArrayList<>();
                    map.put(accessScheme, accessRules);
                }
                accessRules.add(accessRule);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return map;
    }

}
