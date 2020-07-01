package ru.ppr.cppk.sync;

import android.database.Cursor;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.concurrent.CountDownLatch;

import ru.ppr.cppk.db.LocalDaoSession;
import ru.ppr.cppk.db.local.BaseEntityDao;
import ru.ppr.cppk.db.local.CPPKServiceSaleDao;
import ru.ppr.cppk.db.local.CashRegisterEventDao;
import ru.ppr.cppk.db.local.CashierDao;
import ru.ppr.cppk.db.local.CheckDao;
import ru.ppr.cppk.db.local.EventDao;
import ru.ppr.cppk.db.local.PriceDao;
import ru.ppr.cppk.db.local.ShiftEventDao;
import ru.ppr.cppk.db.local.StationDeviceDao;
import ru.ppr.cppk.sync.kpp.ServiceSale;
import ru.ppr.cppk.sync.loader.ServiceSaleLoader;
import ru.ppr.cppk.sync.loader.base.Offset;
import ru.ppr.cppk.sync.loader.baseEntities.EventLoader;
import ru.ppr.cppk.sync.loader.model.CashierLoader;
import ru.ppr.cppk.sync.loader.model.CheckLoader;
import ru.ppr.cppk.sync.loader.model.PriceLoader;
import ru.ppr.cppk.sync.loader.model.StationDeviceLoader;
import ru.ppr.cppk.sync.loader.model.StationLoader;
import ru.ppr.cppk.sync.loader.model.local.SoftwareVersionLoader;
import ru.ppr.cppk.sync.loader.model.local.WorkingShiftEventLoader;
import ru.ppr.cppk.sync.writer.ServiceSaleWriter;
import ru.ppr.cppk.sync.writer.base.CustomExportJsonWriter;
import ru.ppr.cppk.sync.writer.base.DateFormatter;
import ru.ppr.cppk.sync.writer.base.ExportJsonWriter;
import ru.ppr.logger.Logger;
import ru.ppr.nsi.NsiDaoSession;

/**
 * Экспортер событий продажи услуг
 *
 * @author Grigoriy Kashka
 */
public class ServiceSaleExport extends BaseEventsExport {

    private static final String TAG = Logger.makeLogTag(ServiceSaleExport.class);

    public ServiceSaleExport(LocalDaoSession localDaoSession, NsiDaoSession nsiDaoSession, File outputFile) {
        super(localDaoSession, nsiDaoSession, outputFile);
    }

    public void export(Date fromTime) throws IOException {

        long startTime = System.currentTimeMillis();
        Logger.trace(TAG, "export start");

        DateFormatter dateFormatter = new DateFormatter();
        StationLoader stationLoader = new StationLoader(localDaoSession, nsiDaoSession);
        SoftwareVersionLoader softwareVersionLoader = new SoftwareVersionLoader(localDaoSession, nsiDaoSession);
        StationDeviceLoader stationDeviceLoader = new StationDeviceLoader(localDaoSession, nsiDaoSession);
        EventLoader eventLoader = new EventLoader(localDaoSession,
                nsiDaoSession,
                stationLoader,
                stationDeviceLoader, softwareVersionLoader

        );
        WorkingShiftEventLoader workingShiftEventLoader = new WorkingShiftEventLoader(localDaoSession, nsiDaoSession);
        CashierLoader cashierLoader = new CashierLoader(localDaoSession, nsiDaoSession);
        CheckLoader checkLoader = new CheckLoader(localDaoSession, nsiDaoSession);
        PriceLoader priceLoader = new PriceLoader(localDaoSession, nsiDaoSession);

        ServiceSaleLoader serviceSaleLoader = new ServiceSaleLoader(
                localDaoSession,
                nsiDaoSession,
                checkLoader,
                workingShiftEventLoader,
                cashierLoader,
                priceLoader,
                eventLoader
        );

        ServiceSaleWriter serviceSaleWriter = new ServiceSaleWriter(dateFormatter);

        ExportJsonWriter writer = null;
        try {
            writer = new CustomExportJsonWriter(outputFile);

            final ExportJsonWriter localWriter = writer;

            Cursor cursor = null;
            try {
                cursor = localDaoSession.getLocalDb().rawQuery(buildSqlQuery(fromTime), null);
                writeToFileExecutor.submit(() -> {
                    localWriter.beginArray();
                    return null;
                });
                while (cursor.moveToNext()) {
                    ServiceSale serviceSale = serviceSaleLoader.load(cursor, new Offset());
                    writeToFileExecutor.submit(() -> {
                        serviceSaleWriter.write(serviceSale, localWriter);
                        return null;
                    });
                }
                CountDownLatch countDownLatch = new CountDownLatch(1);
                writeToFileExecutor.submit(() -> {
                    localWriter.endArray();
                    countDownLatch.countDown();
                    return null;
                });
                try {
                    countDownLatch.await();
                } catch (InterruptedException e) {
                    Logger.error(TAG, e);
                    throw new RuntimeException("Interrupting is not implemented", e);
                }
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        } catch (Exception e) {
            Logger.error(TAG, e);
            throw e;
        } finally {
            if (writer != null) {
                writer.close();
            }
        }

        Logger.trace(TAG, "softwareVersion.putToCacheCount = " + softwareVersionLoader.getPutToCacheCount());
        Logger.trace(TAG, "softwareVersion.getFromCacheCount = " + softwareVersionLoader.getGetFromCacheCount());
        Logger.trace(TAG, "station.putToCacheCount = " + stationLoader.getPutToCacheCount());
        Logger.trace(TAG, "station.getFromCacheCount = " + stationLoader.getGetFromCacheCount());

        Logger.trace(TAG, "export end, time = " + (System.currentTimeMillis() - startTime));
    }

    private String buildSqlQuery(Date fromTime) {
        StringBuilder sb = new StringBuilder();

        //Таблицы, вошедщие в JOIN по порядку полей в ответе
        final String serviceSaleTable = "ServiceSaleEvent";
        final String eventTable = "Event";
        final String stationDeviceTable = "StationDevice";
        final String checkTable = "CheckTable";
        final String priceTable = "PriceTable";
        final String shiftTable = "ShiftTable";
        final String cashRegisterEventTable = "CashRegisterEventTable";
        final String cashierTable = "CashierTable";

        sb.append("SELECT ");
        sb.append(createColumnsForSelect(serviceSaleTable, ServiceSaleLoader.Columns.all)).append(", "); //ServiceSaleEvent
        sb.append(createColumnsForSelect(checkTable, CheckLoader.ServiceSaleColumns.all)).append(", "); //ServiceSale.TicketNumber
        sb.append(createColumnsForSelect(shiftTable, WorkingShiftEventLoader.ServiceSaleColumns.all)).append(", "); //ServiceSale.WorkingShiftNumber
        sb.append(createColumnsForSelect(priceTable, PriceLoader.ServiceSaleColumns.all)).append(", "); //ServiceSale.Price и ServiceSale.PriceNds
        sb.append(createColumnsForSelect(cashierTable, CashierLoader.Columns.all)).append(", "); //Cashier
        sb.append(createColumnsForSelect(checkTable, CheckLoader.Columns.all)).append(", "); //Check
        sb.append(createColumnsForSelect(eventTable, EventLoader.Columns.all)).append(", "); //Event
        sb.append(createColumnsForSelect(stationDeviceTable, StationDeviceLoader.Columns.all)); //StationDevice

        sb.append(" FROM ");
        sb.append(CPPKServiceSaleDao.TABLE_NAME).append(" ").append(serviceSaleTable);

        //Event
        sb.append(" JOIN ").append(EventDao.TABLE_NAME).append(" ").append(eventTable).append(" ON ")
                .append(eventTable).append(".").append(BaseEntityDao.Properties.Id).append("=").append(serviceSaleTable).append(".").append(CPPKServiceSaleDao.Properties.EventId);

        //Price
        sb.append(" JOIN ").append(PriceDao.TABLE_NAME).append(" ").append(priceTable).append(" ON ")
                .append(priceTable).append(".").append(BaseEntityDao.Properties.Id).append("=").append(serviceSaleTable).append(".").append(CPPKServiceSaleDao.Properties.PriceId);

        //Check
        sb.append(" JOIN ").append(CheckDao.TABLE_NAME).append(" ").append(checkTable).append(" ON ")
                .append(checkTable).append(".").append(BaseEntityDao.Properties.Id).append("=").append(serviceSaleTable).append(".").append(CPPKServiceSaleDao.Properties.CheckId);

        //CashRegisterWorkingShift
        sb.append(" JOIN ").append(ShiftEventDao.TABLE_NAME).append(" ").append(shiftTable).append(" ON ")
                .append(shiftTable).append(".").append(BaseEntityDao.Properties.Id).append("=").append(serviceSaleTable).append(".").append(CPPKServiceSaleDao.Properties.CashRegisterWorkingShiftId);

        //StationDevice
        sb.append(" JOIN ").append(StationDeviceDao.TABLE_NAME).append(" ").append(stationDeviceTable).append(" ON ")
                .append(stationDeviceTable).append(".").append(BaseEntityDao.Properties.Id).append("=").append(eventTable).append(".").append(EventDao.Properties.StationDeviceId);

        //CashRegisterEvent
        sb.append(" JOIN ").append(CashRegisterEventDao.TABLE_NAME).append(" ").append(cashRegisterEventTable).append(" ON ")
                .append(cashRegisterEventTable).append(".").append(BaseEntityDao.Properties.Id).append("=").append(shiftTable).append(".").append(ShiftEventDao.Properties.CashRegisterEventId);

        //Cashier
        sb.append(" JOIN ").append(CashierDao.TABLE_NAME).append(" ").append(cashierTable).append(" ON ")
                .append(cashierTable).append(".").append(BaseEntityDao.Properties.Id).append("=").append(cashRegisterEventTable).append(".").append(CashRegisterEventDao.Properties.CashierId);

        sb.append(" WHERE ").append(eventTable).append(".").append(EventDao.Properties.CreationTimestamp).append(">").append(fromTime.getTime());

        return sb.toString();
    }
}