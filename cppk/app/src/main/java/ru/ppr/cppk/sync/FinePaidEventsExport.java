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
import ru.ppr.cppk.db.local.FineSaleEventDao;
import ru.ppr.cppk.db.local.StationDeviceDao;
import ru.ppr.cppk.localdb.model.FineSaleEvent;
import ru.ppr.cppk.sync.kpp.FinePaidEvent;
import ru.ppr.cppk.sync.loader.FinePaidEventLoader;
import ru.ppr.cppk.sync.loader.base.Offset;
import ru.ppr.cppk.sync.loader.baseEntities.CashRegisterWorkingShiftEventLoader;
import ru.ppr.cppk.sync.loader.baseEntities.EventLoader;
import ru.ppr.cppk.sync.loader.model.BankCardPaymentLoader;
import ru.ppr.cppk.sync.loader.model.CashRegisterLoader;
import ru.ppr.cppk.sync.loader.model.CashierLoader;
import ru.ppr.cppk.sync.loader.model.CheckLoader;
import ru.ppr.cppk.sync.loader.model.StationDeviceLoader;
import ru.ppr.cppk.sync.loader.model.StationLoader;
import ru.ppr.cppk.sync.loader.model.local.FineLoader;
import ru.ppr.cppk.sync.loader.model.local.SoftwareVersionLoader;
import ru.ppr.cppk.sync.loader.model.local.WorkingShiftEventLoader;
import ru.ppr.cppk.sync.writer.FinePaidEventWriter;
import ru.ppr.cppk.sync.writer.base.CustomExportJsonWriter;
import ru.ppr.cppk.sync.writer.base.DateFormatter;
import ru.ppr.cppk.sync.writer.base.ExportJsonWriter;
import ru.ppr.logger.Logger;
import ru.ppr.nsi.NsiDaoSession;

/**
 * Экспортер событий продажи штрафов
 *
 * @author Grigoriy Kashka
 */
public class FinePaidEventsExport extends BaseEventsExport {

    private static final String TAG = Logger.makeLogTag(FinePaidEventsExport.class);

    public FinePaidEventsExport(LocalDaoSession localDaoSession, NsiDaoSession nsiDaoSession, File outputFile) {
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
        BankCardPaymentLoader bankCardPaymentLoader = new BankCardPaymentLoader(localDaoSession, nsiDaoSession);
        FineLoader fineLoader = new FineLoader(localDaoSession, nsiDaoSession);

        FinePaidEventLoader finePaidEventLoader = new FinePaidEventLoader(
                localDaoSession,
                nsiDaoSession,
                checkLoader,
                eventLoader,
                cashRegisterWorkingShiftEventLoader,
                bankCardPaymentLoader,
                fineLoader
        );

        FinePaidEventWriter finePaidEventWriter = new FinePaidEventWriter(dateFormatter);

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
                    FinePaidEvent finePaidEvent = finePaidEventLoader.load(cursor, new Offset());
                    writeToFileExecutor.submit(() -> {
                        finePaidEventWriter.write(finePaidEvent, localWriter);
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
        final String FinePaidEventTable = "FinePaidEvent";
        final String EventTable = "Event";
        final String StationDeviceTable = "StationDevice";
        final String CheckTable = "CheckTable";

        sb.append("SELECT ");
        sb.append(createColumnsForSelect(FinePaidEventTable, FinePaidEventLoader.Columns.all)).append(", "); //FinePaidEvent
        sb.append(createColumnsForSelect(CheckTable, CheckLoader.FinePaidEventColumns.all)).append(", "); //FinePaidEvent.DocNumber
        sb.append(createColumnsForSelect(EventTable, EventLoader.Columns.all)).append(", "); //Event
        sb.append(createColumnsForSelect(StationDeviceTable, StationDeviceLoader.Columns.all)).append(", "); //StationDevice
        sb.append(createColumnsForSelect(CheckTable, CheckLoader.Columns.all)); //Check

        sb.append(" FROM ");
        sb.append(FineSaleEventDao.TABLE_NAME).append(" ").append(FinePaidEventTable);

        //Event
        sb.append(" JOIN ").append(EventDao.TABLE_NAME).append(" ").append(EventTable).append(" ON ")
                .append(EventTable).append(".").append(BaseEntityDao.Properties.Id).append("=").append(FinePaidEventTable).append(".").append(FineSaleEventDao.Properties.EventId);

        //Check
        sb.append(" JOIN ").append(CheckDao.TABLE_NAME).append(" ").append(CheckTable).append(" ON ")
                .append(CheckTable).append(".").append(BaseEntityDao.Properties.Id).append("=").append(FinePaidEventTable).append(".").append(FineSaleEventDao.Properties.CheckId);

        //StationDevice
        sb.append(" JOIN ").append(StationDeviceDao.TABLE_NAME).append(" ").append(StationDeviceTable).append(" ON ")
                .append(StationDeviceTable).append(".").append(BaseEntityDao.Properties.Id).append("=").append(EventTable).append(".").append(EventDao.Properties.StationDeviceId);

        sb.append(" WHERE ").append(EventTable).append(".").append(EventDao.Properties.CreationTimestamp).append(">").append(fromTime.getTime());
        sb.append(" AND ");
        sb.append(" ( ");
        {
            sb.append(FinePaidEventTable).append(".").append(FineSaleEventDao.Properties.Status).append("=").append(FineSaleEvent.Status.CHECK_PRINTED.getCode());
            sb.append(" OR ");
            sb.append(FinePaidEventTable).append(".").append(FineSaleEventDao.Properties.Status).append("=").append(FineSaleEvent.Status.COMPLETED.getCode());
        }
        sb.append(" ) ");

        return sb.toString();
    }
}