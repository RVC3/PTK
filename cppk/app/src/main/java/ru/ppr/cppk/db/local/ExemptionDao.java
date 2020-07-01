package ru.ppr.cppk.db.local;

import android.content.ContentValues;
import android.database.Cursor;
import android.support.annotation.NonNull;

import java.math.BigDecimal;
import java.util.Date;

import ru.ppr.cppk.db.LocalDaoSession;
import ru.ppr.cppk.entity.event.model.ExemptionForEvent;
import ru.ppr.cppk.entity.event.model.SmartCard;
import ru.ppr.cppk.utils.DateTimeUtils;
import ru.ppr.database.Database;
import ru.ppr.database.garbage.base.GCNoLinkRemovable;
import ru.ppr.database.references.ReferenceInfo;
import ru.ppr.logger.Logger;

/**
 * DAO для таблицы локальной БД <i>Exemption</i>.
 *
 * @author Aleksandr Brazhkin
 */
public class ExemptionDao extends BaseEntityDao<ExemptionForEvent, Long> implements GCNoLinkRemovable {

    private static final String TAG = Logger.makeLogTag(ExemptionDao.class);

    public static final String TABLE_NAME = "Exemption";

    public static class Properties {
        public static final String Id = "_id";
        public static final String Fio = "Fio";
        public static final String Code = "Code";
        public static final String ActiveFromDate = "ActiveFromDate";
        public static final String VersionId = "VersionId";
        public static final String Express_Code = "ExpressCode";
        public static final String LossSum = "LossSum";
        public static final String SmartCardId = "SmartCardId";
        public static final String TypeOfDocument = "TypeOfDocument";
        public static final String NumberOfDocument = "NumberOfDocument";
        public static final String Organization = "Organization";
        public static final String RegionOkatoCode = "RegionOkatoCode";
        public static final String RequireSocialCard = "RequireSocialCard";
        public static final String IsSnilsUsed = "IsSnilsUsed";
        public static final String IssueDate = "IssueDate";
    }

    public ExemptionDao(LocalDaoSession localDaoSession) {
        super(localDaoSession);

        // Регистрируем ссылки таблицы
        registerReference(Properties.SmartCardId, SmartCardDao.TABLE_NAME, ReferenceInfo.ReferencesType.NO_ACTION);
    }

    @Override
    public String getTableName() {
        return TABLE_NAME;
    }

    @Override
    public ExemptionForEvent fromCursor(Cursor cursor) {
        int columnIndex;
        ExemptionForEvent exemptionForEvent = new ExemptionForEvent();
        exemptionForEvent.setId(cursor.getLong(cursor.getColumnIndex(Properties.Id)));
        exemptionForEvent.setFio(cursor.getString(cursor.getColumnIndex(Properties.Fio)));
        exemptionForEvent.setCode(cursor.getInt(cursor.getColumnIndex(Properties.Code)));
        exemptionForEvent.setActiveFromDate(new Date(cursor.getLong(cursor.getColumnIndex(Properties.ActiveFromDate))));
        exemptionForEvent.setVersionId(cursor.getLong(cursor.getColumnIndex(Properties.VersionId)));
        exemptionForEvent.setExpressCode(cursor.getInt(cursor.getColumnIndex(Properties.Express_Code)));
        exemptionForEvent.setLossSumm(new BigDecimal(cursor.getString(cursor.getColumnIndex(Properties.LossSum))));
        exemptionForEvent.setSmartCardId(cursor.getLong(cursor.getColumnIndex(Properties.SmartCardId)));
        columnIndex = cursor.getColumnIndex(Properties.TypeOfDocument);
        if (!cursor.isNull(columnIndex)) {
            exemptionForEvent.setTypeOfDocumentWhichApproveExemption(cursor.getString(columnIndex));
        }
        columnIndex = cursor.getColumnIndex(Properties.NumberOfDocument);
        if (!cursor.isNull(columnIndex)) {
            exemptionForEvent.setNumberOfDocumentWhichApproveExemption(cursor.getString(columnIndex));
        }
        exemptionForEvent.setOrganization(cursor.getString(cursor.getColumnIndex(Properties.Organization)));
        exemptionForEvent.setRequireSocialCard(cursor.getInt(cursor.getColumnIndex(Properties.RequireSocialCard)) > 0);
        exemptionForEvent.setSnilsUsed(cursor.getInt(cursor.getColumnIndex(Properties.IsSnilsUsed)) > 0);
        int okatoCodIndex = cursor.getColumnIndex(Properties.RegionOkatoCode);
        if (!cursor.isNull(okatoCodIndex)) {
            exemptionForEvent.setRegionOkatoCode(cursor.getString(okatoCodIndex));
        }
        int issueDateIndex = cursor.getColumnIndex(Properties.IssueDate);
        if (!cursor.isNull(issueDateIndex)) {
            exemptionForEvent.setIssueDate(DateTimeUtils.getDateFromSQLite(cursor.getString(issueDateIndex)));
        }
        return exemptionForEvent;
    }

    @Override
    public ContentValues toContentValues(ExemptionForEvent entity) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(ExemptionDao.Properties.SmartCardId, entity.getSmartCardId());
        contentValues.put(ExemptionDao.Properties.Code, entity.getCode());
        contentValues.put(ExemptionDao.Properties.ActiveFromDate, entity.getActiveFromDate().getTime());
        contentValues.put(ExemptionDao.Properties.VersionId, entity.getVersionId());
        contentValues.put(ExemptionDao.Properties.Express_Code, entity.getExpressCode());
        contentValues.put(ExemptionDao.Properties.Fio, entity.getFio());
        contentValues.put(ExemptionDao.Properties.LossSum, entity.getLossSumm().toString());
        contentValues.put(ExemptionDao.Properties.TypeOfDocument,
                entity.getTypeOfDocumentWhichApproveExemption());
        contentValues.put(ExemptionDao.Properties.NumberOfDocument,
                entity.getNumberOfDocumentWhichApproveExemption());
        contentValues.put(ExemptionDao.Properties.Organization, entity.getOrganization());
        contentValues.put(ExemptionDao.Properties.RegionOkatoCode, entity.getRegionOkatoCode());
        contentValues.put(ExemptionDao.Properties.RequireSocialCard, entity.isRequireSocialCard());
        contentValues.put(ExemptionDao.Properties.IsSnilsUsed, entity.isSnilsUsed());
        if (entity.getIssueDate() != null) {
            contentValues.put(ExemptionDao.Properties.IssueDate, DateTimeUtils.formatDateForSQLite(entity.getIssueDate()));
        }
        return contentValues;
    }

    @Override
    public Long getKey(ExemptionForEvent entity) {
        return entity.getId();
    }

    @Override
    public long insertOrThrow(@NonNull ExemptionForEvent entity) {
        long id = super.insertOrThrow(entity);
        entity.setId(id);
        return id;
    }

    @Override
    public boolean gcHandleNoLinkRemoveData(Database database) {
        // оставляем реализацию сборщика мусора по умолчанию
        return false;
    }

    public long insertExemption(@NonNull ExemptionForEvent exemption) {
        SmartCard smartCard = exemption.getSmartCardFromWhichWasReadAboutExemption();
        if (smartCard != null) {
            getLocalDaoSession().getSmartCardDao().save(smartCard);
            exemption.setSmartCardId(smartCard.getId());
        }
        return insertOrThrow(exemption);
    }
}
