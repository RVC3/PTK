package ru.ppr.cppk.sync;

import android.database.Cursor;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.concurrent.CountDownLatch;

import ru.ppr.cppk.data.summary.TicketTapeStatisticsBuilder;
import ru.ppr.cppk.db.LocalDaoSession;
import ru.ppr.cppk.db.local.BaseEntityDao;
import ru.ppr.cppk.db.local.EventDao;
import ru.ppr.cppk.db.local.StationDeviceDao;
import ru.ppr.cppk.db.local.TicketTapeEventDao;
import ru.ppr.cppk.sync.kpp.TicketPaperRollEvent;
import ru.ppr.cppk.sync.loader.TicketPaperRollEventLoader;
import ru.ppr.cppk.sync.loader.base.Offset;
import ru.ppr.cppk.sync.loader.baseEntities.CashRegisterEventLoader;
import ru.ppr.cppk.sync.loader.baseEntities.EventLoader;
import ru.ppr.cppk.sync.loader.model.CashRegisterLoader;
import ru.ppr.cppk.sync.loader.model.CashierLoader;
import ru.ppr.cppk.sync.loader.model.StationDeviceLoader;
import ru.ppr.cppk.sync.loader.model.StationLoader;
import ru.ppr.cppk.sync.loader.model.local.SoftwareVersionLoader;
import ru.ppr.cppk.sync.loader.model.local.WorkingShiftEventLoader;
import ru.ppr.cppk.sync.writer.TicketPaperRollEventWriter;
import ru.ppr.cppk.sync.writer.base.CustomExportJsonWriter;
import ru.ppr.cppk.sync.writer.base.DateFormatter;
import ru.ppr.cppk.sync.writer.base.ExportJsonWriter;
import ru.ppr.logger.Logger;
import ru.ppr.nsi.NsiDaoSession;

/**
 * @author Grigoriy Kashka
 */
public class TicketPaperRollEventExporter extends BaseEventsExport {

    private static final String TAG = Logger.makeLogTag(TicketPaperRollEventExporter.class);

    public TicketPaperRollEventExporter(LocalDaoSession localDaoSession, NsiDaoSession nsiDaoSession, File outputFile) {
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
        CashRegisterEventLoader cashRegisterEventLoader = new CashRegisterEventLoader(
                localDaoSession,
                nsiDaoSession,
                cashierLoader,
                cashRegisterLoader
        );
        TicketTapeStatisticsBuilder ticketTapeStatisticsBuilder = new TicketTapeStatisticsBuilder(localDaoSession);

        TicketPaperRollEventLoader ticketPaperRollEventLoader = new TicketPaperRollEventLoader(
                localDaoSession,
                nsiDaoSession,
                eventLoader,
                cashRegisterEventLoader,
                workingShiftEventLoader,
                ticketTapeStatisticsBuilder
        );

        TicketPaperRollEventWriter ticketPaperRollEventWriter = new TicketPaperRollEventWriter(dateFormatter);

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
                    TicketPaperRollEvent ticketPaperRollEvent = ticketPaperRollEventLoader.load(cursor, new Offset());
                    writeToFileExecutor.submit(() -> {
                        ticketPaperRollEventWriter.write(ticketPaperRollEvent, localWriter);
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
        Logger.trace(TAG, "cashRegisterEvent.putToCacheCount = " + cashRegisterEventLoader.getPutToCacheCount());
        Logger.trace(TAG, "cashRegisterEvent.getFromCacheCount = " + cashRegisterEventLoader.getGetFromCacheCount());
        Logger.trace(TAG, "workingShiftEventLoader.putToCacheCount = " + workingShiftEventLoader.getPutToCacheCount());
        Logger.trace(TAG, "workingShiftEventLoader.getFromCacheCount = " + workingShiftEventLoader.getGetFromCacheCount());
        Logger.trace(TAG, "station.putToCacheCount = " + stationLoader.getPutToCacheCount());
        Logger.trace(TAG, "station.getFromCacheCount = " + stationLoader.getGetFromCacheCount());

        Logger.trace(TAG, "export end, time = " + (System.currentTimeMillis() - startTime));
    }

    private String buildSqlQuery(Date fromTime) {
        StringBuilder sb = new StringBuilder();

        //Таблицы, вошедщие в JOIN по порядку полей в ответе
        final String TicketTapeEventTable = "TicketTapeEventTable";
        final String EventTable = "Event";
        final String StationDeviceTable = "StationDevice";

        sb.append("SELECT ");
        sb.append(createColumnsForSelect(TicketTapeEventTable, TicketPaperRollEventLoader.Columns.all)).append(", "); //TicketTapeEvent
        sb.append(createColumnsForSelect(EventTable, EventLoader.Columns.all)).append(", "); //Event
        sb.append(createColumnsForSelect(StationDeviceTable, StationDeviceLoader.Columns.all)); //StationDevice

        sb.append(" FROM ");
        sb.append(TicketTapeEventDao.TABLE_NAME).append(" ").append(TicketTapeEventTable);

        //Event
        sb.append(" JOIN ").append(EventDao.TABLE_NAME).append(" ").append(EventTable).append(" ON ")
                .append(EventTable).append(".").append(BaseEntityDao.Properties.Id).append("=").append(TicketTapeEventTable).append(".").append(TicketTapeEventDao.Properties.EventId);

        //StationDevice
        sb.append(" JOIN ").append(StationDeviceDao.TABLE_NAME).append(" ").append(StationDeviceTable).append(" ON ")
                .append(StationDeviceTable).append(".").append(BaseEntityDao.Properties.Id).append("=").append(EventTable).append(".").append(EventDao.Properties.StationDeviceId);

        sb.append(" WHERE ").append(EventTable).append(".").append(EventDao.Properties.CreationTimestamp).append(">").append(fromTime.getTime());

        return sb.toString();
    }
}