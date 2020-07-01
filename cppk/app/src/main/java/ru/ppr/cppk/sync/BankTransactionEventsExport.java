package ru.ppr.cppk.sync;

import android.database.Cursor;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.concurrent.CountDownLatch;

import ru.ppr.cppk.db.LocalDaoSession;
import ru.ppr.cppk.db.local.BankTransactionDao;
import ru.ppr.cppk.db.local.BaseEntityDao;
import ru.ppr.cppk.db.local.EventDao;
import ru.ppr.cppk.db.local.StationDeviceDao;
import ru.ppr.cppk.localdb.model.BankOperationResult;
import ru.ppr.cppk.sync.kpp.BankTransactionEvent;
import ru.ppr.cppk.sync.loader.BankTransactionEventLoader;
import ru.ppr.cppk.sync.loader.base.Offset;
import ru.ppr.cppk.sync.loader.baseEntities.CashRegisterWorkingShiftEventLoader;
import ru.ppr.cppk.sync.loader.baseEntities.EventLoader;
import ru.ppr.cppk.sync.loader.model.CashRegisterLoader;
import ru.ppr.cppk.sync.loader.model.CashierLoader;
import ru.ppr.cppk.sync.loader.model.StationDeviceLoader;
import ru.ppr.cppk.sync.loader.model.StationLoader;
import ru.ppr.cppk.sync.loader.model.local.SoftwareVersionLoader;
import ru.ppr.cppk.sync.loader.model.local.WorkingShiftEventLoader;
import ru.ppr.cppk.sync.writer.BankTransactionEventWriter;
import ru.ppr.cppk.sync.writer.base.CustomExportJsonWriter;
import ru.ppr.cppk.sync.writer.base.DateFormatter;
import ru.ppr.cppk.sync.writer.base.ExportJsonWriter;
import ru.ppr.logger.Logger;
import ru.ppr.nsi.NsiDaoSession;

/**
 * Экспортер событий  BankTransactionEvent
 *
 * @author Grigoriy Kashka
 */
public class BankTransactionEventsExport extends BaseEventsExport {

    private static final String TAG = Logger.makeLogTag(BankTransactionEventsExport.class);

    public BankTransactionEventsExport(LocalDaoSession localDaoSession, NsiDaoSession nsiDaoSession, File outputFile) {
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

        BankTransactionEventLoader bankTransactionEventLoader = new BankTransactionEventLoader(
                localDaoSession,
                nsiDaoSession,
                eventLoader,
                cashRegisterWorkingShiftEventLoader
        );

        BankTransactionEventWriter bankTransactionEventWriter = new BankTransactionEventWriter(dateFormatter);

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
                    BankTransactionEvent bankTransactionEvent = bankTransactionEventLoader.load(cursor, new Offset());
                    writeToFileExecutor.submit(() -> {
                        bankTransactionEventWriter.write(bankTransactionEvent, localWriter);
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
        final String bankTransactionEvent = "BankTransactionEvent";
        final String eventTable = "Event";
        final String stationDeviceTable = "StationDevice";

        sb.append("SELECT ");
        sb.append(createColumnsForSelect(bankTransactionEvent, BankTransactionEventLoader.Columns.all)).append(", "); //BankTransactionEvent
        sb.append(createColumnsForSelect(eventTable, EventLoader.Columns.all)).append(", "); //Event
        sb.append(createColumnsForSelect(stationDeviceTable, StationDeviceLoader.Columns.all)); //StationDevice

        sb.append(" FROM ");
        sb.append(BankTransactionDao.TABLE_NAME).append(" ").append(bankTransactionEvent);

        //Event
        sb.append(" JOIN ").append(EventDao.TABLE_NAME).append(" ").append(eventTable).append(" ON ")
                .append(eventTable).append(".").append(BaseEntityDao.Properties.Id).append("=").append(bankTransactionEvent).append(".").append(BankTransactionDao.Properties.EventId);

        //StationDevice
        sb.append(" JOIN ").append(StationDeviceDao.TABLE_NAME).append(" ").append(stationDeviceTable).append(" ON ")
                .append(stationDeviceTable).append(".").append(BaseEntityDao.Properties.Id).append("=").append(eventTable).append(".").append(EventDao.Properties.StationDeviceId);

        sb.append(" WHERE ").append(eventTable).append(".").append(EventDao.Properties.CreationTimestamp).append(">").append(fromTime.getTime());
        sb.append(" AND ");
        sb.append(bankTransactionEvent).append(".").append(BankTransactionDao.Properties.Status).append("<>").append(ru.ppr.cppk.localdb.model.BankTransactionEvent.Status.STARTED.getCode());
        sb.append(" AND ");
        sb.append(bankTransactionEvent).append(".").append(BankTransactionDao.Properties.OperationResult).append("=").append(BankOperationResult.Approved.getCode());

        return sb.toString();
    }
}