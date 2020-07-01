package ru.ppr.cppk.sync.loader.baseEntities;

import android.database.Cursor;
import android.support.v4.util.Pair;

import java.util.Date;

import ru.ppr.cppk.db.LocalDaoSession;
import ru.ppr.cppk.db.local.TicketEventBaseDao;
import ru.ppr.cppk.managers.NsiVersionManager;
import ru.ppr.cppk.sync.kpp.baseEntities.TicketEventBase;
import ru.ppr.cppk.sync.kpp.model.Tariff;
import ru.ppr.cppk.sync.loader.base.BaseLoader;
import ru.ppr.cppk.sync.loader.base.Column;
import ru.ppr.cppk.sync.loader.base.Offset;
import ru.ppr.cppk.sync.loader.model.SmartCardLoader;
import ru.ppr.cppk.sync.loader.model.StationLoader;
import ru.ppr.cppk.sync.loader.model.TariffLoader;
import ru.ppr.nsi.NsiDaoSession;

/**
 * @author Aleksandr Brazhkin
 */
public class TicketEventBaseLoader extends BaseLoader {

    private final NsiVersionManager nsiVersionManager;
    private final EventLoader eventLoader;
    private final CashRegisterWorkingShiftEventLoader cashRegisterWorkingShiftEventLoader;
    private final SmartCardLoader smartCardLoader;
    private final TariffLoader tariffLoader;
    private final StationLoader stationLoader;

    public TicketEventBaseLoader(LocalDaoSession localDaoSession,
                                 NsiDaoSession nsiDaoSession,
                                 NsiVersionManager nsiVersionManager,
                                 EventLoader eventLoader,
                                 SmartCardLoader smartCardLoader,
                                 TariffLoader tariffLoader,
                                 StationLoader stationLoader,
                                 CashRegisterWorkingShiftEventLoader cashRegisterWorkingShiftEventLoader) {
        super(localDaoSession, nsiDaoSession);
        this.nsiVersionManager = nsiVersionManager;
        this.cashRegisterWorkingShiftEventLoader = cashRegisterWorkingShiftEventLoader;
        this.eventLoader = eventLoader;
        this.smartCardLoader = smartCardLoader;
        this.tariffLoader = tariffLoader;
        this.stationLoader = stationLoader;
    }

    public static class Columns {
        static final Column SALE_DATE_TIME = new Column(0, TicketEventBaseDao.Properties.SaleDateTime);
        static final Column VALID_FROM_DATETIME = new Column(1, TicketEventBaseDao.Properties.ValidFromDateTime);
        static final Column VALID_TILL_DATETIME = new Column(2, TicketEventBaseDao.Properties.ValidTillDateTime);
        static final Column DEPARTURE_STATION_CODE = new Column(3, TicketEventBaseDao.Properties.DepartureStationId);
        static final Column DESTINATION_STATION_CODE = new Column(4, TicketEventBaseDao.Properties.DestinationStationId);
        static final Column TARIFF_CODE = new Column(5, TicketEventBaseDao.Properties.TariffCode);
        static final Column WAY_TYPE = new Column(6, TicketEventBaseDao.Properties.WayType);
        static final Column TYPE = new Column(7, TicketEventBaseDao.Properties.Type);
        static final Column TYPE_CODE = new Column(8, TicketEventBaseDao.Properties.TypeCode);
        static final Column SMART_CARD_ID = new Column(9, TicketEventBaseDao.Properties.SmartCardId);
        static final Column CASH_REGISTER_WORKING_SHIFT_ID = new Column(10, TicketEventBaseDao.Properties.CashRegisterWorkingShiftId);

        public static Column[] allForControl = new Column[]{
                SALE_DATE_TIME,
                VALID_FROM_DATETIME,
                VALID_TILL_DATETIME,
                DEPARTURE_STATION_CODE,
                DESTINATION_STATION_CODE,
                TARIFF_CODE,
                WAY_TYPE,
                TYPE,
                TYPE_CODE,
                SMART_CARD_ID,
                CASH_REGISTER_WORKING_SHIFT_ID
        };

        public static Column[] allForSale = allForControl;

        public static Column[] allForReturn = new Column[]{
                SALE_DATE_TIME,
                VALID_FROM_DATETIME,
                VALID_TILL_DATETIME,
                DEPARTURE_STATION_CODE,
                DESTINATION_STATION_CODE,
                TARIFF_CODE,
                WAY_TYPE,
                TYPE,
                TYPE_CODE,
                SMART_CARD_ID
        };
    }

    /**
     * Заполнить поля сущности TicketEventBase
     *
     * @param ticketEventBase      - сама сущность
     * @param forControl           - флаг того, что выгрузка для контроля
     * @param forReturn            - флаг того, что выгрузка для аннулирования
     * @param returnWorkingShiftId - идентификатор cashRegisterWorkingShiftId для события аннулирования
     * @param cursor               - курсок
     * @param offset               - смещение
     */
    public void fill(TicketEventBase ticketEventBase, boolean forControl, boolean forReturn, long returnWorkingShiftId, Cursor cursor, Offset offset) {
        //TicketNumber - заполняется на уровне сущности CppkTicketControl или на уровне CppkTicketSale
        ticketEventBase.SaleDateTime = new Date(cursor.getLong(offset.value + Columns.SALE_DATE_TIME.index) * 1000);
        ticketEventBase.ValidFromDateTime = new Date(cursor.getLong(offset.value + Columns.VALID_FROM_DATETIME.index) * 1000);
        ticketEventBase.ValidTillDateTime = new Date(cursor.getLong(offset.value + Columns.VALID_TILL_DATETIME.index) * 1000);

        ticketEventBase.WayType = cursor.getInt(offset.value + Columns.WAY_TYPE.index);
        int typeIndex = offset.value + Columns.TYPE.index;
        if (!cursor.isNull(typeIndex)) {
            ticketEventBase.Type = cursor.getString(typeIndex);
        }
        ticketEventBase.TypeCode = cursor.getInt(offset.value + Columns.TYPE_CODE.index);

        long tariffCode = cursor.getLong(offset.value + Columns.TARIFF_CODE.index);

        long departureStationCode = cursor.getLong(offset.value + Columns.DEPARTURE_STATION_CODE.index);
        long destinationStationCode = cursor.getLong(offset.value + Columns.DESTINATION_STATION_CODE.index);

        long workingShiftId = forReturn ? returnWorkingShiftId : cursor.getLong(offset.value + Columns.CASH_REGISTER_WORKING_SHIFT_ID.index);
        long smartCardId = cursor.getLong(offset.value + Columns.SMART_CARD_ID.index);

        if (forControl)
            offset.value += Columns.allForControl.length;
        else if (forReturn)
            offset.value += Columns.allForReturn.length;
        else
            offset.value += Columns.allForSale.length;

        ticketEventBase.SmartCard = (smartCardId > 0) ? smartCardLoader.load(smartCardId) : null;
        eventLoader.fill(ticketEventBase, cursor, offset);
        cashRegisterWorkingShiftEventLoader.fill(ticketEventBase, workingShiftId);

        Pair<Tariff, Integer> tariffResp = tariffLoader.loadTariff(tariffCode, forControl ? nsiVersionManager.getNsiVersionIdForDate(ticketEventBase.SaleDateTime) : ticketEventBase.VersionId);
        ticketEventBase.Tariff = tariffResp.first;
        ticketEventBase.DepartureStation = stationLoader.loadStation(departureStationCode, tariffResp.second);
        ticketEventBase.DestinationStation = stationLoader.loadStation(destinationStationCode, tariffResp.second);
    }

}
