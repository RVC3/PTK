package ru.ppr.cppk.sync;

import android.database.Cursor;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.concurrent.CountDownLatch;

import ru.ppr.cppk.db.LocalDaoSession;
import ru.ppr.cppk.db.local.BaseEntityDao;
import ru.ppr.cppk.db.local.CheckDao;
import ru.ppr.cppk.db.local.EventDao;
import ru.ppr.cppk.db.local.StationDeviceDao;
import ru.ppr.cppk.db.local.TestTicketDao;
import ru.ppr.cppk.sync.kpp.TestTicketEvent;
import ru.ppr.cppk.sync.loader.TestTicketEventLoader;
import ru.ppr.cppk.sync.loader.base.Offset;
import ru.ppr.cppk.sync.loader.baseEntities.CashRegisterWorkingShiftEventLoader;
import ru.ppr.cppk.sync.loader.baseEntities.EventLoader;
import ru.ppr.cppk.sync.loader.model.CashRegisterLoader;
import ru.ppr.cppk.sync.loader.model.CashierLoader;
import ru.ppr.cppk.sync.loader.model.CheckLoader;
import ru.ppr.cppk.sync.loader.model.StationDeviceLoader;
import ru.ppr.cppk.sync.loader.model.StationLoader;
import ru.ppr.cppk.sync.loader.model.local.SoftwareVersionLoader;
import ru.ppr.cppk.sync.loader.model.local.WorkingShiftEventLoader;
import ru.ppr.cppk.sync.writer.TestTicketEventWriter;
import ru.ppr.cppk.sync.writer.base.CustomExportJsonWriter;
import ru.ppr.cppk.sync.writer.base.DateFormatter;
import ru.ppr.cppk.sync.writer.base.ExportJsonWriter;
import ru.ppr.logger.Logger;
import ru.ppr.nsi.NsiDaoSession;

/**
 * Экспортер событий продажи тестовго ПД
 *
 * @author Grigoriy Kashka
 */
public class TestTicketEventsExport extends BaseEventsExport {

    private static final String TAG = Logger.makeLogTag(TestTicketEventsExport.class);

    public TestTicketEventsExport(LocalDaoSession localDaoSession, NsiDaoSession nsiDaoSession, File outputFile) {
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
        CashRegisterLoader cashRegisterLoader = new CashRegisterLoader(localDaoSession, nsiDaoSession);
        CashRegisterWorkingShiftEventLoader cashRegisterWorkingShiftEventLoader = new CashRegisterWorkingShiftEventLoader(
                localDaoSession,
                nsiDaoSession,
                workingShiftEventLoader,
                cashierLoader,
                cashRegisterLoader
        );
        CheckLoader checkLoader = new CheckLoader(localDaoSession, nsiDaoSession);
        TestTicketEventLoader testTicketEventLoader = new TestTicketEventLoader(
                localDaoSession,
                nsiDaoSession,
                checkLoader,
                eventLoader,
                cashRegisterWorkingShiftEventLoader
        );

        TestTicketEventWriter testTicketEventWriter = new TestTicketEventWriter(dateFormatter);

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
                    TestTicketEvent testTicketEvent = testTicketEventLoader.load(cursor, new Offset());
                    writeToFileExecutor.submit(() -> {
                        testTicketEventWriter.write(testTicketEvent, localWriter);
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
        Logger.trace(TAG, "cashRegisterWorkingShiftEventLoader.putToCacheCount = " + cashRegisterWorkingShiftEventLoader.getPutToCacheCount());
        Logger.trace(TAG, "cashRegisterWorkingShiftEventLoader.getFromCacheCount = " + cashRegisterWorkingShiftEventLoader.getGetFromCacheCount());
        Logger.trace(TAG, "station.putToCacheCount = " + stationLoader.getPutToCacheCount());
        Logger.trace(TAG, "station.getFromCacheCount = " + stationLoader.getGetFromCacheCount());

        Logger.trace(TAG, "export end, time = " + (System.currentTimeMillis() - startTime));
    }

    private String buildSqlQuery(Date fromTime) {
        StringBuilder sb = new StringBuilder();

        //Таблицы, вошедщие в JOIN по порядку полей в ответе
        final String TestTicketEventTable = "TestTicketEventTable";
        final String EventTable = "Event";
        final String StationDeviceTable = "StationDevice";
        final String CheckTable = "CheckTable";

        sb.append("SELECT ");
        sb.append(createColumnsForSelect(TestTicketEventTable, TestTicketEventLoader.Columns.all)).append(", "); //TestTicketEvent
        sb.append(createColumnsForSelect(CheckTable, CheckLoader.TestTicketEventColumns.all)).append(", "); //TestTicketEvent.Number, TestTicketEvent.PrintDateTime
        sb.append(createColumnsForSelect(EventTable, EventLoader.Columns.all)).append(", "); //Event
        sb.append(createColumnsForSelect(StationDeviceTable, StationDeviceLoader.Columns.all)); //StationDevice

        sb.append(" FROM ");
        sb.append(TestTicketDao.TABLE_NAME).append(" ").append(TestTicketEventTable);

        //Event
        sb.append(" JOIN ").append(EventDao.TABLE_NAME).append(" ").append(EventTable).append(" ON ")
                .append(EventTable).append(".").append(BaseEntityDao.Properties.Id).append("=").append(TestTicketEventTable).append(".").append(TestTicketDao.Properties.EventId);

        //Check
        sb.append(" JOIN ").append(CheckDao.TABLE_NAME).append(" ").append(CheckTable).append(" ON ")
                .append(CheckTable).append(".").append(BaseEntityDao.Properties.Id).append("=").append(TestTicketEventTable).append(".").append(TestTicketDao.Properties.CheckId);

        //StationDevice
        sb.append(" JOIN ").append(StationDeviceDao.TABLE_NAME).append(" ").append(StationDeviceTable).append(" ON ")
                .append(StationDeviceTable).append(".").append(BaseEntityDao.Properties.Id).append("=").append(EventTable).append(".").append(EventDao.Properties.StationDeviceId);

        sb.append(" WHERE ").append(EventTable).append(".").append(EventDao.Properties.CreationTimestamp).append(">").append(fromTime.getTime());

        return sb.toString();
    }
}