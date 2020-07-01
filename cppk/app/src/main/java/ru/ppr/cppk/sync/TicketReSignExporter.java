package ru.ppr.cppk.sync;

import android.database.Cursor;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.concurrent.CountDownLatch;

import ru.ppr.cppk.db.LocalDaoSession;
import ru.ppr.cppk.db.local.BaseEntityDao;
import ru.ppr.cppk.db.local.CPPKTicketReSignDao;
import ru.ppr.cppk.db.local.EventDao;
import ru.ppr.cppk.db.local.StationDeviceDao;
import ru.ppr.cppk.sync.kpp.CPPKTicketReSign;
import ru.ppr.cppk.sync.loader.CPPKTicketReSignLoader;
import ru.ppr.cppk.sync.loader.base.Offset;
import ru.ppr.cppk.sync.loader.baseEntities.EventLoader;
import ru.ppr.cppk.sync.loader.model.StationDeviceLoader;
import ru.ppr.cppk.sync.loader.model.StationLoader;
import ru.ppr.cppk.sync.loader.model.local.SoftwareVersionLoader;
import ru.ppr.cppk.sync.writer.CPPKTicketReSignWriter;
import ru.ppr.cppk.sync.writer.base.CustomExportJsonWriter;
import ru.ppr.cppk.sync.writer.base.DateFormatter;
import ru.ppr.cppk.sync.writer.base.ExportJsonWriter;
import ru.ppr.logger.Logger;
import ru.ppr.nsi.NsiDaoSession;

/**
 * Экспортер событий  {@link ru.ppr.cppk.sync.kpp.CPPKTicketReSign}
 *
 * @author Grigoriy Kashka
 */
public class TicketReSignExporter extends BaseEventsExport {

    private static final String TAG = Logger.makeLogTag(TicketReSignExporter.class);

    public TicketReSignExporter(LocalDaoSession localDaoSession, NsiDaoSession nsiDaoSession, File outputFile) {
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
        CPPKTicketReSignLoader cppkTicketReSignLoader = new CPPKTicketReSignLoader(
                localDaoSession,
                nsiDaoSession,
                eventLoader
        );

        CPPKTicketReSignWriter cppkTicketReSignWriter = new CPPKTicketReSignWriter(dateFormatter);

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
                    CPPKTicketReSign cppkTicketReSign = cppkTicketReSignLoader.load(cursor, new Offset());
                    writeToFileExecutor.submit(() -> {
                        cppkTicketReSignWriter.write(cppkTicketReSign, localWriter);
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
        final String ticketReSignEvent = "TicketReSign";
        final String eventTable = "Event";
        final String stationDeviceTable = "StationDevice";

        sb.append("SELECT ");
        sb.append(createColumnsForSelect(ticketReSignEvent, CPPKTicketReSignLoader.Columns.all)).append(", "); //TicketReSignEvent
        sb.append(createColumnsForSelect(eventTable, EventLoader.Columns.all)).append(", "); //Event
        sb.append(createColumnsForSelect(stationDeviceTable, StationDeviceLoader.Columns.all)); //StationDevice

        sb.append(" FROM ");
        sb.append(CPPKTicketReSignDao.TABLE_NAME).append(" ").append(ticketReSignEvent);

        //Event
        sb.append(" JOIN ").append(EventDao.TABLE_NAME).append(" ").append(eventTable).append(" ON ")
                .append(eventTable).append(".").append(BaseEntityDao.Properties.Id).append("=").append(ticketReSignEvent).append(".").append(CPPKTicketReSignDao.Properties.EventId);

        sb.append(" JOIN ").append(StationDeviceDao.TABLE_NAME).append(" ").append(stationDeviceTable).append(" ON ")
                .append(stationDeviceTable).append(".").append(BaseEntityDao.Properties.Id).append("=").append(eventTable).append(".").append(EventDao.Properties.StationDeviceId);

        sb.append(" WHERE ").append(eventTable).append(".").append(EventDao.Properties.CreationTimestamp).append(">").append(fromTime.getTime());

        return sb.toString();
    }
}