package ru.ppr.cppk.sync.loader.model;

import android.database.Cursor;

import ru.ppr.cppk.db.LocalDaoSession;
import ru.ppr.cppk.db.local.StationDeviceDao;
import ru.ppr.cppk.sync.kpp.model.ParentTicketInfo;
import ru.ppr.cppk.sync.kpp.model.StationDevice;
import ru.ppr.cppk.sync.loader.base.BaseLoader;
import ru.ppr.cppk.sync.loader.base.Column;
import ru.ppr.cppk.sync.loader.base.Offset;
import ru.ppr.nsi.NsiDaoSession;

/**
 * @author Grigoriy Kashka
 */
public class StationDeviceLoader extends BaseLoader {

    public StationDeviceLoader(LocalDaoSession localDaoSession, NsiDaoSession nsiDaoSession) {
        super(localDaoSession, nsiDaoSession);
    }

    public static class Columns {
        static final Column DEVICE_ID = new Column(0, StationDeviceDao.Properties.DeviceId);
        static final Column MODEL = new Column(1, StationDeviceDao.Properties.Model);
        static final Column SERIAL_NUMBER = new Column(2, StationDeviceDao.Properties.SerialNumber);
        static final Column TYPE = new Column(3, StationDeviceDao.Properties.Type);
        static final Column PRODUCTION_SECTION_CODE = new Column(4, StationDeviceDao.Properties.ProductionSectionCode);

        public static Column[] all = new Column[]{
                DEVICE_ID,
                MODEL,
                SERIAL_NUMBER,
                TYPE,
                PRODUCTION_SECTION_CODE
        };
    }

    /**
     * Поля сущности {@link StationDevice}, предназначенные для {@link ru.ppr.cppk.sync.kpp.model.ParentTicketInfo} для выгрузки событий аннулирования
     */
    public static class ParentTicketInfoFoCppkTicketReturnColumns {
        static final Column DEVICE_ID = new Column(0, StationDeviceDao.Properties.DeviceId);

        public static Column[] all = new Column[]{
                DEVICE_ID
        };
    }

    public StationDevice load(Cursor cursor, Offset offset) {
        StationDevice stationDevice = new StationDevice();
        stationDevice.id = cursor.getString(offset.value + Columns.DEVICE_ID.index);
        stationDevice.model = cursor.getString(offset.value + Columns.MODEL.index);
        stationDevice.serialNumber = cursor.getString(offset.value + Columns.SERIAL_NUMBER.index);
        stationDevice.type = cursor.getInt(offset.value + Columns.TYPE.index);
        stationDevice.productionSectionCode = cursor.getInt(offset.value + Columns.PRODUCTION_SECTION_CODE.index);
        offset.value += Columns.all.length;
        return stationDevice;
    }


    /**
     * Заполнить поля в сущности {@link ParentTicketInfo} (ParentTicket Для события аннулирования)
     * http://agile.srvdev.ru/browse/CPPKPP-25093
     *
     * @param parentTicketInfo
     * @param cursor
     * @param offset
     */
    public void fillParentTicketForCppkTicketReturnFields(ParentTicketInfo parentTicketInfo, Cursor cursor, Offset offset) {
        parentTicketInfo.CashRegisterNumber = cursor.getString(offset.value + ParentTicketInfoFoCppkTicketReturnColumns.DEVICE_ID.index);
        offset.value += ParentTicketInfoFoCppkTicketReturnColumns.all.length;
    }
}
