package ru.ppr.cppk.sync;

import android.database.Cursor;
import android.util.Pair;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import ru.ppr.cppk.db.LocalDaoSession;
import ru.ppr.cppk.db.local.BaseEntityDao;
import ru.ppr.cppk.db.local.EventDao;
import ru.ppr.cppk.db.local.ShiftEventDao;
import ru.ppr.cppk.db.local.StationDeviceDao;
import ru.ppr.cppk.db.local.repository.ServiceTicketControlEventRepository;
import ru.ppr.cppk.localdb.model.ShiftEvent;
import ru.ppr.cppk.managers.NsiVersionManager;
import ru.ppr.cppk.sync.kpp.CashRegisterWorkingShift;
import ru.ppr.cppk.sync.loader.CashRegisterEventData.FeeExemptionOperationsSummaryBuilder;
import ru.ppr.cppk.sync.loader.CashRegisterEventData.FeeTaxOperationsSummaryBuilder;
import ru.ppr.cppk.sync.loader.CashRegisterEventData.ShiftClosureStatisticsBuilder;
import ru.ppr.cppk.sync.loader.CashRegisterEventData.TaxOperationsSummaryBuilder;
import ru.ppr.cppk.sync.loader.CashRegisterWorkingShiftLoader;
import ru.ppr.cppk.sync.loader.base.Offset;
import ru.ppr.cppk.sync.loader.baseEntities.CashRegisterEventLoader;
import ru.ppr.cppk.sync.loader.baseEntities.EventLoader;
import ru.ppr.cppk.sync.loader.model.CashRegisterLoader;
import ru.ppr.cppk.sync.loader.model.CashierLoader;
import ru.ppr.cppk.sync.loader.model.EventsStatisticLoader;
import ru.ppr.cppk.sync.loader.model.StationDeviceLoader;
import ru.ppr.cppk.sync.loader.model.StationLoader;
import ru.ppr.cppk.sync.loader.model.local.SoftwareVersionLoader;
import ru.ppr.cppk.sync.loader.model.local.WorkingShiftEventLoader;
import ru.ppr.cppk.sync.writer.CashRegisterWorkingShiftWriter;
import ru.ppr.cppk.sync.writer.base.CustomExportJsonWriter;
import ru.ppr.cppk.sync.writer.base.DateFormatter;
import ru.ppr.cppk.sync.writer.base.ExportJsonWriter;
import ru.ppr.database.SqLiteUtils;
import ru.ppr.logger.Logger;
import ru.ppr.nsi.NsiDaoSession;
import ru.ppr.nsi.repository.FineRepository;

/**
 * Экспортер сменных событий
 *
 * @author Grigoriy Kashka
 */
public class CashRegisterWorkingShiftExport extends BaseEventsExport {

    private static final String TAG = Logger.makeLogTag(CashRegisterWorkingShiftExport.class);

    private final NsiVersionManager nsiVersionManager;
    private final FineRepository fineRepository;
    private final ServiceTicketControlEventRepository serviceTicketControlEventRepository;

    public CashRegisterWorkingShiftExport(LocalDaoSession localDaoSession,
                                          NsiDaoSession nsiDaoSession,
                                          NsiVersionManager nsiVersionManager,
                                          FineRepository fineRepository,
                                          ServiceTicketControlEventRepository serviceTicketControlEventRepository,
                                          File outputFile) {
        super(localDaoSession, nsiDaoSession, outputFile);
        this.nsiVersionManager = nsiVersionManager;
        this.fineRepository = fineRepository;
        this.serviceTicketControlEventRepository = serviceTicketControlEventRepository;
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
        CashierLoader cashierLoader = new CashierLoader(localDaoSession, nsiDaoSession);
        CashRegisterLoader cashRegisterLoader = new CashRegisterLoader(localDaoSession, nsiDaoSession);
        CashRegisterEventLoader cashRegisterEventLoader = new CashRegisterEventLoader(
                localDaoSession,
                nsiDaoSession,
                cashierLoader,
                cashRegisterLoader
        );
        WorkingShiftEventLoader workingShiftEventLoader = new WorkingShiftEventLoader(localDaoSession, nsiDaoSession);

        EventsStatisticLoader eventsStatisticLoader = new EventsStatisticLoader(localDaoSession, serviceTicketControlEventRepository);

        FeeTaxOperationsSummaryBuilder feeTaxOperationsSummaryBuilder = new FeeTaxOperationsSummaryBuilder();
        TaxOperationsSummaryBuilder taxOperationsSummaryBuilder = new TaxOperationsSummaryBuilder();
        FeeExemptionOperationsSummaryBuilder feeExemptionOperationsSummaryBuilder = new FeeExemptionOperationsSummaryBuilder();

        ShiftClosureStatisticsBuilder shiftClosureStatisticsBuilder = new ShiftClosureStatisticsBuilder(
                localDaoSession,
                nsiDaoSession,
                nsiVersionManager,
                fineRepository,
                feeTaxOperationsSummaryBuilder,
                taxOperationsSummaryBuilder,
                feeExemptionOperationsSummaryBuilder
        );

        CashRegisterWorkingShiftLoader cashRegisterWorkingShiftLoader = new CashRegisterWorkingShiftLoader(
                localDaoSession,
                nsiDaoSession,
                eventLoader,
                cashRegisterEventLoader,
                workingShiftEventLoader,
                eventsStatisticLoader,
                shiftClosureStatisticsBuilder
        );

        CashRegisterWorkingShiftWriter cashRegisterWorkingShiftWriter = new CashRegisterWorkingShiftWriter(dateFormatter);

        ExportJsonWriter writer = null;
        try {
            writer = new CustomExportJsonWriter(outputFile);

            final ExportJsonWriter localWriter = writer;

            Cursor cursor = null;
            try {
                Pair<String, String[]> sql = buildSqlQuery(fromTime);
                cursor = localDaoSession.getLocalDb().rawQuery(sql.first, sql.second);
                writeToFileExecutor.submit(() -> {
                    localWriter.beginArray();
                    return null;
                });
                while (cursor.moveToNext()) {
                    CashRegisterWorkingShift cashRegisterWorkingShift = cashRegisterWorkingShiftLoader.load(cursor, new Offset());
                    writeToFileExecutor.submit(() -> {
                        cashRegisterWorkingShiftWriter.write(cashRegisterWorkingShift, localWriter);
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
        Logger.trace(TAG, "station.putToCacheCount = " + stationLoader.getPutToCacheCount());
        Logger.trace(TAG, "station.getFromCacheCount = " + stationLoader.getGetFromCacheCount());

        Logger.trace(TAG, "export end, time = " + (System.currentTimeMillis() - startTime));
    }

    private Pair<String, String[]> buildSqlQuery(Date fromTime) {
        StringBuilder sb = new StringBuilder();

        EnumSet<ShiftEvent.ShiftProgressStatus> progressStatuses = ShiftEvent.ShiftProgressStatus.FINISHED_STATUSES;
        List<String> selectionArgsList = new ArrayList<>();

        //Таблицы, вошедщие в JOIN по порядку полей в ответе
        final String cashRegisterWorkingShiftTable = "ShiftEventTable";
        final String eventTable = "EventTable";
        final String stationDeviceTable = "StationDeviceTable";

        sb.append("SELECT ");
        sb.append(createColumnsForSelect(cashRegisterWorkingShiftTable, CashRegisterWorkingShiftLoader.Columns.all)).append(", "); //CashRegisterWorkingShiftEvent
        sb.append(createColumnsForSelect(cashRegisterWorkingShiftTable, WorkingShiftEventLoader.Columns.all)).append(", "); //CashRegisterWorkingShiftEvent.Number и StartDateTime
        sb.append(createColumnsForSelect(eventTable, EventLoader.Columns.all)).append(", "); //Event
        sb.append(createColumnsForSelect(stationDeviceTable, StationDeviceLoader.Columns.all)); //StationDevice

        sb.append(" FROM ");
        sb.append(ShiftEventDao.TABLE_NAME).append(" ").append(cashRegisterWorkingShiftTable);

        //Event
        sb.append(" JOIN ").append(EventDao.TABLE_NAME).append(" ").append(eventTable).append(" ON ")
                .append(eventTable).append(".").append(BaseEntityDao.Properties.Id).append("=").append(cashRegisterWorkingShiftTable).append(".").append(ShiftEventDao.Properties.EventId);

        //StationDevice
        sb.append(" JOIN ").append(StationDeviceDao.TABLE_NAME).append(" ").append(stationDeviceTable).append(" ON ")
                .append(stationDeviceTable).append(".").append(BaseEntityDao.Properties.Id).append("=").append(eventTable).append(".").append(EventDao.Properties.StationDeviceId);

        sb.append(" WHERE ").append(eventTable).append(".").append(EventDao.Properties.CreationTimestamp).append(">").append(fromTime.getTime());

        ////////////////////////////////////////////////////////////////////////////////
        {
            sb.append(" AND ");
            sb.append(cashRegisterWorkingShiftTable).append(".").append(ShiftEventDao.Properties.ProgressStatus);
            sb.append(" IN ");
            sb.append(" ( ");
            sb.append(SqLiteUtils.makePlaceholders(progressStatuses.size()));
            for (ShiftEvent.ShiftProgressStatus progressStatus : progressStatuses) {
                selectionArgsList.add(String.valueOf(progressStatus.getCode()));
            }
            sb.append(" ) ");
        }
        ////////////////////////////////////////////////////////////////////////////////

        String[] selectionArgs = new String[selectionArgsList.size()];
        selectionArgsList.toArray(selectionArgs);

        return new Pair<>(sb.toString(), selectionArgs);
    }
}