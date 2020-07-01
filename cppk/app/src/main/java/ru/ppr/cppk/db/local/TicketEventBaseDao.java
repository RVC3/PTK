package ru.ppr.cppk.db.local;

import android.content.ContentValues;
import android.database.Cursor;
import android.support.annotation.NonNull;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import ru.ppr.cppk.db.LocalDaoSession;
import ru.ppr.cppk.entity.event.base34.TicketEventBase;
import ru.ppr.cppk.entity.event.model.SmartCard;
import ru.ppr.cppk.localdb.model.ShiftEvent;
import ru.ppr.cppk.localdb.model.TicketWayType;
import ru.ppr.database.Database;
import ru.ppr.database.garbage.base.GCCascadeLinksRemovable;
import ru.ppr.database.references.ReferenceInfo;
import ru.ppr.logger.Logger;

/**
 * Created by Dmitry Nevolin on 12.02.2016.
 */
public class TicketEventBaseDao extends BaseEntityDao<TicketEventBase, Long> implements GCCascadeLinksRemovable {

    private static final String TAG = Logger.makeLogTag(TicketEventBaseDao.class);

    public static final String TABLE_NAME = "TicketEventBase";

    public static class Properties {
        public static final String CashRegisterWorkingShiftId = "CashRegisterWorkingShiftId";
        public static final String SaleDateTime = "SaleDateTime";
        public static final String ValidFromDateTime = "ValidFromDateTime";
        public static final String ValidTillDateTime = "ValidTillDateTime";
        public static final String TariffCode = "TariffCode";
        public static final String WayType = "WayType";
        public static final String Type = "Type";
        public static final String TypeCode = "TypeCode";
        public static final String SmartCardId = "SmartCardId";
        public static final String TicketTypeShortName = "TicketTypeShortName";
        public static final String TicketCategoryCode = "TicketCategoryCode";
        public static final String DepartureStationId = "DepartureStationCode";
        public static final String DestinationStationId = "DestinationStationCode";
        public static final String StartDayOffset = "StartDayOffset";
        public static final String DeletedMark = "DeletedMark";
    }

    public TicketEventBaseDao(@NonNull LocalDaoSession localDaoSession) {
        super(localDaoSession);

        // Регистрируем ссылки таблицы
        registerReference(Properties.CashRegisterWorkingShiftId, ShiftEventDao.TABLE_NAME, ReferenceInfo.ReferencesType.CASCADE);
        registerReference(Properties.SmartCardId, SmartCardDao.TABLE_NAME, ReferenceInfo.ReferencesType.NO_ACTION);
    }

    @Override
    public String getTableName() {
        return TABLE_NAME;
    }

    @Override
    public TicketEventBase fromCursor(Cursor cursor) {
        TicketEventBase out = new TicketEventBase();
        out.setId(cursor.getLong(cursor.getColumnIndex(BaseEntityDao.Properties.Id)));
        out.setShiftEventId(cursor.getLong(cursor.getColumnIndex(Properties.CashRegisterWorkingShiftId)));
        //SaleDateTime В бд сохранено в секундах, поэтому при выборки из БД его надо перевести в милисекунды
        out.setSaletime(new Date(TimeUnit.SECONDS.toMillis(cursor.getLong(cursor.getColumnIndex(Properties.SaleDateTime)))));
        //ValidFromDate В бд сохранено в секундах, поэтому при выборки из БД его надо перевести в милисекунды
        out.setValidFromDate(new Date(TimeUnit.SECONDS.toMillis(cursor.getLong(cursor.getColumnIndex(Properties.ValidFromDateTime)))));
        //ValidTillDate В бд сохранено в секундах, поэтому при выборки из БД его надо перевести в милисекунды
        out.setValidTillDate(new Date(TimeUnit.SECONDS.toMillis(cursor.getLong(cursor.getColumnIndex(Properties.ValidTillDateTime)))));
        out.setTariffCode(cursor.getInt(cursor.getColumnIndex(Properties.TariffCode)));
        out.setWayType(TicketWayType.valueOf(Integer.parseInt(cursor.getString(cursor.getColumnIndex(Properties.WayType)))));
        int columnIndex = cursor.getColumnIndex(Properties.Type);
        if (!cursor.isNull(columnIndex)) {
            out.setType(cursor.getString(cursor.getColumnIndex(Properties.Type)));
        }
        out.setTypeCode(cursor.getInt(cursor.getColumnIndex(Properties.TypeCode)));
        out.setTicketTypeShortName(cursor.getString(cursor.getColumnIndex(Properties.TicketTypeShortName)));
        out.setTicketCategoryCode(cursor.getInt(cursor.getColumnIndex(Properties.TicketCategoryCode)));
        out.setDepartureStationCode(cursor.getInt(cursor.getColumnIndex(Properties.DepartureStationId)));
        out.setDestinationStationCode(cursor.getInt(cursor.getColumnIndex(Properties.DestinationStationId)));

        columnIndex = cursor.getColumnIndex(Properties.SmartCardId);
        if (!cursor.isNull(columnIndex)) {
            out.setSmartCardId(cursor.getInt(columnIndex));
        }
        out.setStartDayOffset(cursor.getInt(cursor.getColumnIndex(Properties.StartDayOffset)));
        out.setDeletedMark(cursor.getInt(cursor.getColumnIndex(Properties.DeletedMark)) > 0);

        return out;
    }

    @Override
    public ContentValues toContentValues(TicketEventBase entity) {
        ContentValues contentValues = new ContentValues();

        SmartCard smartCard = getLocalDaoSession().getSmartCardDao().load(entity.getSmartCardId());
        if (smartCard != null)
            contentValues.put(Properties.SmartCardId, smartCard.getId());

        contentValues.put(Properties.TariffCode, entity.getTariffCode());
        contentValues.put(Properties.DepartureStationId, entity.getDepartureStationCode());
        contentValues.put(Properties.DestinationStationId, entity.getDestinationStationCode());

        ShiftEvent shiftEvent = getLocalDaoSession().getShiftEventDao().load(entity.getShiftEventId());
        if (shiftEvent != null)
            contentValues.put(Properties.CashRegisterWorkingShiftId, shiftEvent.getId());

        if (entity.getSaledateTime() != null)
            contentValues.put(Properties.SaleDateTime, TimeUnit.MILLISECONDS.toSeconds(entity.getSaledateTime().getTime())); //SaleDateTime СОХРАНЯЕМ В СЕКУНДАХ

        contentValues.put(Properties.TicketTypeShortName, entity.getTicketTypeShortName());
        contentValues.put(Properties.TicketCategoryCode, entity.getTicketCategoryCode());
        contentValues.put(Properties.TypeCode, entity.getTypeCode());

        if (entity.getValidFromDate() != null)
            contentValues.put(Properties.ValidFromDateTime, TimeUnit.MILLISECONDS.toSeconds(entity.getValidFromDate().getTime())); //ValidFromDate СОХРАНЯЕМ В СЕКУНДАХ

        if (entity.getValidTillDate() != null)
            contentValues.put(Properties.ValidTillDateTime, TimeUnit.MILLISECONDS.toSeconds(entity.getValidTillDate().getTime())); //ValidTillDate СОХРАНЯЕМ В СЕКУНДАХ

        if (entity.getWayType() != null)
            contentValues.put(Properties.WayType, entity.getWayType().getCode());

        if (entity.getType() != null)
            contentValues.put(Properties.Type, entity.getType());

        contentValues.put(Properties.StartDayOffset, entity.getStartDayOffset());
        contentValues.put(Properties.DeletedMark, entity.getDeletedMark());

        return contentValues;
    }

    @Override
    public Long getKey(TicketEventBase entity) {
        return entity.getId();
    }

    @Override
    public long insertOrThrow(@NonNull TicketEventBase entity) {
        long id = super.insertOrThrow(entity);
        entity.setId(id);
        return id;
    }

    @Override
    public String getDeletedMarkField() {
        return Properties.DeletedMark;
    }

    @Override
    public boolean gcHandleRemoveCascadeLink(Database database, String referenceTable, String referenceField) {
        return false;
    }

}
