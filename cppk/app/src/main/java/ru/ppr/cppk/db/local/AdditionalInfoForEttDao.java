package ru.ppr.cppk.db.local;

import android.content.ContentValues;
import android.database.Cursor;
import android.support.annotation.NonNull;

import java.util.Date;

import ru.ppr.cppk.db.LocalDaoSession;
import ru.ppr.cppk.localdb.model.AdditionalInfoForEtt;
import ru.ppr.database.Database;
import ru.ppr.database.garbage.base.GCNoLinkRemovable;

/**
 * Created by Александр on 15.08.2016.
 * <p>
 * Класс для работы с AdditionalInfoForEtt
 */
public class AdditionalInfoForEttDao extends BaseEntityDao<AdditionalInfoForEtt, Long> implements GCNoLinkRemovable {

    public static final String TABLE_NAME = "AdditionalInfoForEtt";

    @Override
    public boolean gcHandleNoLinkRemoveData(Database database) {
        // оставляем реализацию сборщика мусора по умолчанию
        return false;
    }

    public static class Properties {
        public static final String PassengerCategory = "PassengerCategory";
        public static final String IssueUnitCode = "IssueUnitCode";
        public static final String OwnerOrganizationCode = "OwnerOrganizationCode";
        public static final String PassengerFio = "PassengerFio";
        public static final String GuardianFio = "GuardianFio";
        public static final String Snils = "SNILS";
        public static final String IssueDataTime = "IssueDataTime";
    }

    public AdditionalInfoForEttDao(@NonNull LocalDaoSession localDaoSession) {
        super(localDaoSession);
    }

    @Override
    public String getTableName() {
        return TABLE_NAME;
    }

    @Override
    public AdditionalInfoForEtt fromCursor(Cursor cursor) {
        AdditionalInfoForEtt additionalInfoForEtt = new AdditionalInfoForEtt();
        additionalInfoForEtt.setId(cursor.getLong(cursor.getColumnIndex(BaseEntityDao.Properties.Id)));
        int index = cursor.getColumnIndex(Properties.PassengerCategory);
        if (!cursor.isNull(index)) {
            additionalInfoForEtt.setPassengerCategory(cursor.getString(index));
        }
        index = cursor.getColumnIndex(Properties.IssueUnitCode);
        if (!cursor.isNull(index)) {
            additionalInfoForEtt.setIssueUnitCode(cursor.getString(index));
        }
        index = cursor.getColumnIndex(Properties.OwnerOrganizationCode);
        if (!cursor.isNull(index)) {
            additionalInfoForEtt.setOwnerOrganizationCode(cursor.getString(index));
        }
        index = cursor.getColumnIndex(Properties.PassengerFio);
        if (!cursor.isNull(index)) {
            additionalInfoForEtt.setPassengerFio(cursor.getString(cursor.getColumnIndex(Properties.PassengerFio)));
        }
        index = cursor.getColumnIndex(Properties.GuardianFio);
        if (!cursor.isNull(index)) {
            additionalInfoForEtt.setGuardianFio(cursor.getString(index));
        }
        index = cursor.getColumnIndex(Properties.Snils);
        if (!cursor.isNull(index)) {
            additionalInfoForEtt.setSnils(cursor.getString(index));
        }
        index = cursor.getColumnIndex(Properties.IssueDataTime);
        if (!cursor.isNull(index)) {
            additionalInfoForEtt.setIssueDateTime(new Date(cursor.getLong((index))));
        }

        return additionalInfoForEtt;
    }

    @Override
    public ContentValues toContentValues(AdditionalInfoForEtt entity) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(Properties.GuardianFio, entity.getGuardianFio());
        contentValues.put(Properties.IssueDataTime, entity.getIssueDateTime() == null ? null : entity.getIssueDateTime().getTime());
        contentValues.put(Properties.IssueUnitCode, entity.getIssueUnitCode());
        contentValues.put(Properties.OwnerOrganizationCode, entity.getOwnerOrganizationCode());
        contentValues.put(Properties.PassengerCategory, entity.getPassengerCategory());
        contentValues.put(Properties.Snils, entity.getSnils());
        contentValues.put(Properties.PassengerFio, entity.getPassengerFio());
        return contentValues;
    }

    @Override
    public Long getKey(AdditionalInfoForEtt entity) {
        return entity.getId();
    }

    @Override
    public long insertOrThrow(@NonNull AdditionalInfoForEtt entity) {
        long id = super.insertOrThrow(entity);
        entity.setId(id);
        return id;
    }

}
