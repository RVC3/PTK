package ru.ppr.security.dao;

import android.database.Cursor;

import ru.ppr.security.SecurityDaoSession;
import ru.ppr.security.entity.SmartCardStopListItem;
import ru.ppr.security.entity.StopCriteriaType;
import ru.ppr.utils.UnsignedLongUtils;

/**
 * DAO для таблицы базы безопасности <i>SmartCardStopListItem</i>.
 *
 * @author Aleksandr Brazhkin
 */
public class SmartCardStopListItemDao extends BaseEntityDao<SmartCardStopListItem, Void> {

    public static final String TABLE_NAME = "SmartCardStopListItem";

    public static class Properties {
        public static final String CRYSTAL_SERIAL_NUMBER = "CrystalSerialNumber";
        public static final String OUTER_NUMBER = "OuterNumber";
        public static final String REASON_CODE = "ReasonCode";
        public static final String SMART_CARD_TYPE_CODE = "SmartCardTypeCode";
        public static final String STOP_CRITERIA_TYPE = "StopCriteriaType";
    }

    public SmartCardStopListItemDao(SecurityDaoSession securityDaoSession) {
        super(securityDaoSession);
    }

    @Override
    String getTableName() {
        return TABLE_NAME;
    }

    @Override
    public SmartCardStopListItem fromCursor(Cursor cursor) {
        SmartCardStopListItem smartCardStopListItem = new SmartCardStopListItem();

        int index = cursor.getColumnIndex(SmartCardStopListItemDao.Properties.CRYSTAL_SERIAL_NUMBER);
        smartCardStopListItem.setCrystalSerialNumber(cursor.getString(index));

        index = cursor.getColumnIndex(SmartCardStopListItemDao.Properties.OUTER_NUMBER);
        if (index != -1) {
            String unsignedOuterNumberString;
            if (cursor.isNull(index)) {
                unsignedOuterNumberString = null;
            } else {
                unsignedOuterNumberString = UnsignedLongUtils.toUnsignedString(cursor.getLong(index), 10);
            }
            smartCardStopListItem.setOuterNumber(unsignedOuterNumberString);
        }

        index = cursor.getColumnIndex(SmartCardStopListItemDao.Properties.REASON_CODE);
        if (index != -1) smartCardStopListItem.setReasonCode(cursor.getInt(index));

        index = cursor.getColumnIndex(SmartCardStopListItemDao.Properties.STOP_CRITERIA_TYPE);
        if (index != -1)
            smartCardStopListItem.setStopCriteriaType(StopCriteriaType.getByCode(cursor.getInt(index)));

        return smartCardStopListItem;
    }
}
