package ru.ppr.cppk.db.local;

import android.content.ContentValues;
import android.database.Cursor;
import android.support.annotation.NonNull;

import ru.ppr.cppk.localdb.model.ParentTicketInfo;
import ru.ppr.cppk.db.LocalDaoSession;
import ru.ppr.cppk.entity.event.model.SmartCard;
import ru.ppr.database.Database;
import ru.ppr.database.garbage.base.GCNoLinkRemovable;
import ru.ppr.database.references.ReferenceInfo;
import ru.ppr.logger.Logger;
import ru.ppr.nsi.entity.TicketStorageType;

/**
 * DAO для таблицы локальной БД <i>SmartCard</i>.
 *
 * @author Dmitry Nevolin
 */
public class SmartCardDao extends BaseEntityDao<SmartCard, Long> implements GCNoLinkRemovable {

    private static final String TAG = Logger.makeLogTag(SmartCardDao.class);

    public static final String TABLE_NAME = "SmartCard";

    public static class Properties {
        public static final String Id = "_id";
        public static final String OuterNumber = "OuterNumber";
        public static final String CrystalSerialNumber = "CrystalSerialNumber";
        public static final String TypeCode = "TypeCode";
        public static final String UsageCount = "UsageCount";
        public static final String Track = "Track";
        public static final String Issuer = "Issuerr";
        public static final String PresentTicket1 = "PresentTicket1";
        public static final String PresentTicket2 = "PresentTicket2";
    }

    public SmartCardDao(@NonNull LocalDaoSession localDaoSession) {
        super(localDaoSession);

        // Регистрируем ссылки таблицы
        registerReference(Properties.PresentTicket1, ParentTicketInfoDao.TABLE_NAME, ReferenceInfo.ReferencesType.NO_ACTION);
        registerReference(Properties.PresentTicket2, ParentTicketInfoDao.TABLE_NAME, ReferenceInfo.ReferencesType.NO_ACTION);
    }

    @Override
    public String getTableName() {
        return TABLE_NAME;
    }

    @Override
    public SmartCard fromCursor(Cursor cursor) {
        SmartCard out = new SmartCard();
        out.setId(cursor.getLong(cursor.getColumnIndex(Properties.Id)));
        out.setOuterNumber(cursor.getString(cursor.getColumnIndex(Properties.OuterNumber)));
        out.setCrystalSerialNumber(cursor.getString(cursor.getColumnIndex(Properties.CrystalSerialNumber)));
        out.setType(TicketStorageType.getTypeByDBCode(cursor.getInt(cursor.getColumnIndex(Properties.TypeCode))));
        out.setUsageCount(cursor.getInt(cursor.getColumnIndex(Properties.UsageCount)));
        out.setTrack(cursor.getInt(cursor.getColumnIndex(Properties.Track)));
        out.setPresentTicket1Id(cursor.getInt(cursor.getColumnIndex(Properties.PresentTicket1)));
        out.setPresentTicket2Id(cursor.getInt(cursor.getColumnIndex(Properties.PresentTicket2)));
        int index = cursor.getColumnIndex(Properties.Issuer);
        if (!cursor.isNull(index)) {
            out.setIssuer(cursor.getString(index));
        } else {
            out.setIssuer("");
        }
        return out;
    }

    @Override
    public ContentValues toContentValues(SmartCard entity) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(Properties.CrystalSerialNumber, entity.getCrystalSerialNumber());
        contentValues.put(Properties.OuterNumber, entity.getOuterNumber());
        contentValues.put(Properties.Track, entity.getTrack());
        contentValues.put(Properties.TypeCode, entity.getType().getDBCode());
        contentValues.put(Properties.UsageCount, entity.getUsageCount());
        contentValues.put(Properties.Issuer, entity.getIssuer());
        if (entity.getPresentTicket1() != null) {
            contentValues.put(Properties.PresentTicket1, entity.getPresentTicket1().getId());
        }

        if (entity.getPresentTicket2() != null) {
            contentValues.put(Properties.PresentTicket2, entity.getPresentTicket2().getId());
        }
        return contentValues;
    }

    @Override
    public Long getKey(SmartCard entity) {
        return entity.getId();
    }

    @Override
    public long insertOrThrow(@NonNull SmartCard entity) {
        long id = super.insertOrThrow(entity);
        entity.setId(id);
        return id;
    }

    public void save(@NonNull SmartCard smartCard) {
        db().beginTransaction();
        try {

            ParentTicketInfo presentTicket1 = smartCard.getPresentTicket1();
            if (presentTicket1 != null) {
                getLocalDaoSession().getParentTicketInfoDao().insertOrThrow(presentTicket1);
                smartCard.setPresentTicket1Id(presentTicket1.getId());
            }

            ParentTicketInfo presentTicket2 = smartCard.getPresentTicket2();
            if (presentTicket2 != null) {
                getLocalDaoSession().getParentTicketInfoDao().insertOrThrow(presentTicket2);
                smartCard.setPresentTicket2Id(presentTicket2.getId());
            }

            ContentValues contentValues = toContentValues(smartCard);
            long id = getLocalDaoSession().getLocalDb().insertOrThrow(TABLE_NAME, null, contentValues);
            smartCard.setId(id);
            db().setTransactionSuccessful();
        } catch (Exception e) {
            throw e;
        } finally {
            db().endTransaction();
        }
    }

    @Override
    public boolean gcHandleNoLinkRemoveData(Database database) {
        // оставляем стандартный алгоритм сборщика мусора, удаляющий записи, на кторые нет ссылок
        return false;
    }

}
