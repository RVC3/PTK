package ru.ppr.cppk.sync.loader;

import android.database.Cursor;

import java.util.Date;

import ru.ppr.cppk.db.LocalDaoSession;
import ru.ppr.cppk.db.local.ServiceTicketControlEventDao;
import ru.ppr.cppk.sync.kpp.ServiceTicketControlEvent;
import ru.ppr.cppk.sync.loader.base.BaseLoader;
import ru.ppr.cppk.sync.loader.base.Column;
import ru.ppr.cppk.sync.loader.base.Offset;
import ru.ppr.cppk.sync.loader.baseEntities.CashRegisterWorkingShiftEventLoader;
import ru.ppr.cppk.sync.loader.baseEntities.EventLoader;
import ru.ppr.nsi.NsiDaoSession;

/**
 * @author Grigoriy Kashka
 */
public class ServiceTicketControlEventLoader extends BaseLoader {

    private final EventLoader eventLoader;
    private final CashRegisterWorkingShiftEventLoader cashRegisterWorkingShiftEventLoader;

    public ServiceTicketControlEventLoader(LocalDaoSession localDaoSession,
                                           NsiDaoSession nsiDaoSession,
                                           EventLoader eventLoader,
                                           CashRegisterWorkingShiftEventLoader cashRegisterWorkingShiftEventLoader) {
        super(localDaoSession, nsiDaoSession);
        this.eventLoader = eventLoader;
        this.cashRegisterWorkingShiftEventLoader = cashRegisterWorkingShiftEventLoader;
    }

    public static class Columns {
        static final Column CONTROL_DATE_TIME = new Column(0, ServiceTicketControlEventDao.Properties.ControlDateTime);
        static final Column EDS_KEY_NUMBER = new Column(1, ServiceTicketControlEventDao.Properties.EdsKeyNumber);
        static final Column STOP_LIST_ID = new Column(2, ServiceTicketControlEventDao.Properties.StopListId);
        static final Column VALIDATION_RESULT = new Column(3, ServiceTicketControlEventDao.Properties.ValidationResult);
        static final Column CARD_NUMBER = new Column(4, ServiceTicketControlEventDao.Properties.CardNumber);
        static final Column CARD_CRISTAL_ID = new Column(5, ServiceTicketControlEventDao.Properties.CardCristalId);
        static final Column TICKET_STORAGE_TYPE = new Column(6, ServiceTicketControlEventDao.Properties.TicketStorageType);
        static final Column VALID_FROM = new Column(7, ServiceTicketControlEventDao.Properties.ValidFrom);
        static final Column VALID_TO = new Column(8, ServiceTicketControlEventDao.Properties.ValidTo);
        static final Column ZONE_TYPE = new Column(9, ServiceTicketControlEventDao.Properties.ZoneType);
        static final Column ZONE_VALUE = new Column(10, ServiceTicketControlEventDao.Properties.ZoneValue);
        static final Column CAN_TRAVEL = new Column(11, ServiceTicketControlEventDao.Properties.CanTravel);
        static final Column REQUIRE_PERSONIFICATION = new Column(12, ServiceTicketControlEventDao.Properties.RequirePersonification);
        static final Column REQUIRE_CHECK_DOCUMENT = new Column(13, ServiceTicketControlEventDao.Properties.RequireCheckDocument);
        static final Column TICKET_NUMBER = new Column(14, ServiceTicketControlEventDao.Properties.TicketNumber);
        static final Column TICKET_WRITE_DATE_TIME = new Column(15, ServiceTicketControlEventDao.Properties.TicketWriteDateTime);
        static final Column SMART_CARD_USAGE_COUNT = new Column(16, ServiceTicketControlEventDao.Properties.SmartCardUsageCount);
        static final Column PASSAGE_SIGN = new Column(17, ServiceTicketControlEventDao.Properties.PassageSign);
        static final Column TICKET_DEVICE_ID = new Column(18, ServiceTicketControlEventDao.Properties.TicketDeviceId);
        static final Column CASH_REGISTER_WORKING_SHIFT_ID = new Column(19, ServiceTicketControlEventDao.Properties.CashRegisterWorkingShiftId);

        public static Column[] all = new Column[]{
                CONTROL_DATE_TIME,
                EDS_KEY_NUMBER,
                STOP_LIST_ID,
                VALIDATION_RESULT,
                CARD_NUMBER,
                CARD_CRISTAL_ID,
                TICKET_STORAGE_TYPE,
                VALID_FROM,
                VALID_TO,
                ZONE_TYPE,
                ZONE_VALUE,
                CAN_TRAVEL,
                REQUIRE_PERSONIFICATION,
                REQUIRE_CHECK_DOCUMENT,
                TICKET_NUMBER,
                TICKET_WRITE_DATE_TIME,
                SMART_CARD_USAGE_COUNT,
                PASSAGE_SIGN,
                TICKET_DEVICE_ID,
                CASH_REGISTER_WORKING_SHIFT_ID
        };
    }

    public ServiceTicketControlEvent load(Cursor cursor, Offset offset) {

        ServiceTicketControlEvent serviceTicketControlEvent = new ServiceTicketControlEvent();

        serviceTicketControlEvent.controlDateTime = new Date(cursor.getLong(offset.value + Columns.CONTROL_DATE_TIME.index));
        serviceTicketControlEvent.edsKeyNumber = cursor.getLong(offset.value + Columns.EDS_KEY_NUMBER.index);
        serviceTicketControlEvent.stopListId = cursor.getInt(offset.value + Columns.STOP_LIST_ID.index);
        serviceTicketControlEvent.validationResult = cursor.getInt(offset.value + Columns.VALIDATION_RESULT.index);
        serviceTicketControlEvent.cardNumber = cursor.getString(offset.value + Columns.CARD_NUMBER.index);
        serviceTicketControlEvent.cardCristalId = cursor.getString(offset.value + Columns.CARD_CRISTAL_ID.index);
        serviceTicketControlEvent.cardType = cursor.getInt(offset.value + Columns.TICKET_STORAGE_TYPE.index);
        serviceTicketControlEvent.validFromUtc = new Date(cursor.getLong(offset.value + Columns.VALID_FROM.index));
        serviceTicketControlEvent.validToUtc = cursor.isNull(offset.value + Columns.VALID_TO.index) ? null : new Date(cursor.getLong(offset.value + Columns.VALID_TO.index));
        serviceTicketControlEvent.zoneType = cursor.getInt(offset.value + Columns.ZONE_TYPE.index);
        serviceTicketControlEvent.zoneValue = cursor.getInt(offset.value + Columns.ZONE_VALUE.index);
        serviceTicketControlEvent.canTravel = cursor.getInt(offset.value + Columns.CAN_TRAVEL.index) == 1;
        serviceTicketControlEvent.requirePersonification = cursor.getInt(offset.value + Columns.REQUIRE_PERSONIFICATION.index) == 1;
        serviceTicketControlEvent.requireCheckDocument = cursor.getInt(offset.value + Columns.REQUIRE_CHECK_DOCUMENT.index) == 1;
        serviceTicketControlEvent.ticketNumber = cursor.getInt(offset.value + Columns.TICKET_NUMBER.index);
        serviceTicketControlEvent.ticketWriteDateTime = new Date(cursor.getLong(offset.value + Columns.TICKET_WRITE_DATE_TIME.index));
        serviceTicketControlEvent.smartCardUsageCount = cursor.getInt(offset.value + Columns.SMART_CARD_USAGE_COUNT.index);
        serviceTicketControlEvent.passageSign = cursor.getInt(offset.value + Columns.PASSAGE_SIGN.index) == 1;
        serviceTicketControlEvent.ticketDeviceId = cursor.getString(offset.value + Columns.TICKET_DEVICE_ID.index);

        long cashRegisterWorkingShiftId = cursor.getLong(offset.value + Columns.CASH_REGISTER_WORKING_SHIFT_ID.index);

        offset.value += Columns.all.length;

        eventLoader.fill(serviceTicketControlEvent, cursor, offset);
        cashRegisterWorkingShiftEventLoader.fill(serviceTicketControlEvent, cashRegisterWorkingShiftId);

        return serviceTicketControlEvent;
    }

}