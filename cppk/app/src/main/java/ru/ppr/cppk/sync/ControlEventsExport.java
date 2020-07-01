package ru.ppr.cppk.sync;

import android.database.Cursor;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.concurrent.CountDownLatch;

import ru.ppr.cppk.db.LocalDaoSession;
import ru.ppr.cppk.db.local.BaseEntityDao;
import ru.ppr.cppk.db.local.CppkTicketControlsDao;
import ru.ppr.cppk.db.local.EventDao;
import ru.ppr.cppk.db.local.StationDeviceDao;
import ru.ppr.cppk.db.local.TicketEventBaseDao;
import ru.ppr.cppk.managers.NsiVersionManager;
import ru.ppr.cppk.sync.kpp.CPPKTicketControl;
import ru.ppr.cppk.sync.loader.TicketControlLoader;
import ru.ppr.cppk.sync.loader.base.Offset;
import ru.ppr.cppk.sync.loader.baseEntities.CashRegisterWorkingShiftEventLoader;
import ru.ppr.cppk.sync.loader.baseEntities.EventLoader;
import ru.ppr.cppk.sync.loader.baseEntities.TicketEventBaseLoader;
import ru.ppr.cppk.sync.loader.model.CashRegisterLoader;
import ru.ppr.cppk.sync.loader.model.CashierLoader;
import ru.ppr.cppk.sync.loader.model.ParentTicketInfoLoader;
import ru.ppr.cppk.sync.loader.model.SmartCardLoader;
import ru.ppr.cppk.sync.loader.model.StationDeviceLoader;
import ru.ppr.cppk.sync.loader.model.StationLoader;
import ru.ppr.cppk.sync.loader.model.TariffLoader;
import ru.ppr.cppk.sync.loader.model.local.SoftwareVersionLoader;
import ru.ppr.cppk.sync.loader.model.local.WorkingShiftEventLoader;
import ru.ppr.cppk.sync.writer.TicketControlEventWriter;
import ru.ppr.cppk.sync.writer.base.CustomExportJsonWriter;
import ru.ppr.cppk.sync.writer.base.DateFormatter;
import ru.ppr.cppk.sync.writer.base.ExportJsonWriter;
import ru.ppr.logger.Logger;
import ru.ppr.nsi.NsiDaoSession;

/**
 * Экспортер событий контроля
 *
 * @author Aleksandr Brazhkin
 */
public class ControlEventsExport extends BaseEventsExport {

    private static final String TAG = Logger.makeLogTag(ControlEventsExport.class);

    private final NsiVersionManager nsiVersionManager;

    public ControlEventsExport(LocalDaoSession localDaoSession,
                               NsiDaoSession nsiDaoSession,
                               NsiVersionManager nsiVersionManager,
                               File outputFile
    ) {
        super(localDaoSession, nsiDaoSession, outputFile);
        this.nsiVersionManager = nsiVersionManager;
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
        ParentTicketInfoLoader parentTicketInfoLoader = new ParentTicketInfoLoader(localDaoSession, nsiDaoSession);
        SmartCardLoader smartCardLoader = new SmartCardLoader(
                localDaoSession,
                nsiDaoSession,
                parentTicketInfoLoader
        );
        TariffLoader tariffLoader = new TariffLoader(localDaoSession, nsiDaoSession);
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
        TicketEventBaseLoader ticketEventBaseLoader = new TicketEventBaseLoader(
                localDaoSession,
                nsiDaoSession,
                nsiVersionManager,
                eventLoader,
                smartCardLoader,
                tariffLoader,
                stationLoader,
                cashRegisterWorkingShiftEventLoader
        );
        TicketControlLoader ticketControlLoader = new TicketControlLoader(
                localDaoSession,
                nsiDaoSession,
                ticketEventBaseLoader
        );

        TicketControlEventWriter ticketControlEventWriter = new TicketControlEventWriter(dateFormatter);

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
                    CPPKTicketControl cppkTicketControl = ticketControlLoader.load(cursor, new Offset());
                    writeToFileExecutor.submit(() -> {
                        ticketControlEventWriter.write(cppkTicketControl, localWriter);
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
        Logger.trace(TAG, "tariff.putToCacheCount = " + tariffLoader.getPutToCacheCount());
        Logger.trace(TAG, "tariff.getFromCacheCount = " + tariffLoader.getGetFromCacheCount());

        Logger.trace(TAG, "export end, time = " + (System.currentTimeMillis() - startTime));
    }

    private String buildSqlQuery(Date fromTime) {
        StringBuilder sb = new StringBuilder();

        //Таблицы, вошедщие в JOIN по порядку полей в ответе
        final String ControlEventTable = "ControlEvent";
        final String EventTable = "Event";
        final String TicketEventBaseTable = "TicketEventBase";
        final String StationDeviceTable = "StationDevice";

        sb.append("SELECT ");
        sb.append(createColumnsForSelect(ControlEventTable, TicketControlLoader.Columns.all)).append(", ");
        sb.append(createColumnsForSelect(TicketEventBaseTable, TicketEventBaseLoader.Columns.allForControl)).append(", ");
        sb.append(createColumnsForSelect(EventTable, EventLoader.Columns.all)).append(", ");
        sb.append(createColumnsForSelect(StationDeviceTable, StationDeviceLoader.Columns.all));
        sb.append(" FROM ");
        sb.append(CppkTicketControlsDao.TABLE_NAME).append(" ").append(ControlEventTable);
        sb.append(" JOIN ").append(EventDao.TABLE_NAME).append(" ").append(EventTable).append(" ON ")
                .append(EventTable).append(".").append(BaseEntityDao.Properties.Id).append("=").append(ControlEventTable).append(".").append(CppkTicketControlsDao.Properties.EventId);
        sb.append(" JOIN ").append(TicketEventBaseDao.TABLE_NAME).append(" ").append(TicketEventBaseTable).append(" ON ")
                .append(TicketEventBaseTable).append(".").append(BaseEntityDao.Properties.Id).append("=").append(ControlEventTable).append(".").append(CppkTicketControlsDao.Properties.TicketEventBaseId);
        sb.append(" JOIN ").append(StationDeviceDao.TABLE_NAME).append(" ").append(StationDeviceTable).append(" ON ")
                .append(StationDeviceTable).append(".").append(BaseEntityDao.Properties.Id).append("=").append(EventTable).append(".").append(EventDao.Properties.StationDeviceId);
        sb.append(" WHERE ").append(EventTable).append(".").append(EventDao.Properties.CreationTimestamp).append(">").append(fromTime.getTime());
        //для тестов
        //sb.append(" AND ").append(TicketEventBaseTable).append(".").append(TicketEventBaseDao.Properties.SmartCardId).append(">0");
        //sb.append(" AND ").append(EventTable).append(".").append(ConstantsDB.Event_StationCode).append(">0");
        //sb.append(" LIMIT 500 ");

        return sb.toString();
    }


}
