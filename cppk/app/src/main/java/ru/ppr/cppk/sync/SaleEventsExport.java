package ru.ppr.cppk.sync;

import android.database.Cursor;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.concurrent.CountDownLatch;

import ru.ppr.cppk.db.LocalDaoSession;
import ru.ppr.cppk.db.local.BaseEntityDao;
import ru.ppr.cppk.db.local.CheckDao;
import ru.ppr.cppk.db.local.CppkTicketSaleDao;
import ru.ppr.cppk.db.local.EventDao;
import ru.ppr.cppk.db.local.LegalEntityDao;
import ru.ppr.cppk.db.local.PriceDao;
import ru.ppr.cppk.db.local.StationDeviceDao;
import ru.ppr.cppk.db.local.TicketEventBaseDao;
import ru.ppr.cppk.db.local.TicketSaleReturnEventBaseDao;
import ru.ppr.cppk.db.local.TrainInfoDao;
import ru.ppr.cppk.managers.NsiVersionManager;
import ru.ppr.cppk.sync.kpp.CPPKTicketSale;
import ru.ppr.cppk.sync.loader.TicketSaleLoader;
import ru.ppr.cppk.sync.loader.base.Offset;
import ru.ppr.cppk.sync.loader.baseEntities.CashRegisterWorkingShiftEventLoader;
import ru.ppr.cppk.sync.loader.baseEntities.EventLoader;
import ru.ppr.cppk.sync.loader.baseEntities.TicketEventBaseLoader;
import ru.ppr.cppk.sync.loader.baseEntities.TicketSaleReturnEventBaseLoader;
import ru.ppr.cppk.sync.loader.model.AdditionalInfoForEttLoader;
import ru.ppr.cppk.sync.loader.model.BankCardPaymentLoader;
import ru.ppr.cppk.sync.loader.model.CashRegisterLoader;
import ru.ppr.cppk.sync.loader.model.CashierLoader;
import ru.ppr.cppk.sync.loader.model.CheckLoader;
import ru.ppr.cppk.sync.loader.model.ExemptionLoader;
import ru.ppr.cppk.sync.loader.model.FeeLoader;
import ru.ppr.cppk.sync.loader.model.LegalEntityLoader;
import ru.ppr.cppk.sync.loader.model.ParentTicketInfoLoader;
import ru.ppr.cppk.sync.loader.model.PreTicketLoader;
import ru.ppr.cppk.sync.loader.model.PriceLoader;
import ru.ppr.cppk.sync.loader.model.SeasonTicketLoader;
import ru.ppr.cppk.sync.loader.model.SmartCardLoader;
import ru.ppr.cppk.sync.loader.model.StationDeviceLoader;
import ru.ppr.cppk.sync.loader.model.StationLoader;
import ru.ppr.cppk.sync.loader.model.TariffLoader;
import ru.ppr.cppk.sync.loader.model.TicketTypeLoader;
import ru.ppr.cppk.sync.loader.model.TrainInfoLoader;
import ru.ppr.cppk.sync.loader.model.local.SoftwareVersionLoader;
import ru.ppr.cppk.sync.loader.model.local.WorkingShiftEventLoader;
import ru.ppr.cppk.sync.writer.TicketSaleEventWriter;
import ru.ppr.cppk.sync.writer.base.CustomExportJsonWriter;
import ru.ppr.cppk.sync.writer.base.DateFormatter;
import ru.ppr.cppk.sync.writer.base.ExportJsonWriter;
import ru.ppr.logger.Logger;
import ru.ppr.nsi.NsiDaoSession;

/**
 * Экспортер событий продажи ПД
 *
 * @author Grigoriy Kashka
 */
public class SaleEventsExport extends BaseEventsExport {

    private static final String TAG = Logger.makeLogTag(SaleEventsExport.class);

    private final NsiVersionManager nsiVersionManager;

    public SaleEventsExport(LocalDaoSession localDaoSession, NsiDaoSession nsiDaoSession, NsiVersionManager nsiVersionManager, File outputFile) {
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
        LegalEntityLoader legalEntityLoader = new LegalEntityLoader(localDaoSession, nsiDaoSession);
        ExemptionLoader exemptionLoader = new ExemptionLoader(localDaoSession, nsiDaoSession, smartCardLoader);
        CheckLoader checkLoader = new CheckLoader(localDaoSession, nsiDaoSession);
        TrainInfoLoader trainInfoLoader = new TrainInfoLoader(localDaoSession, nsiDaoSession);
        SeasonTicketLoader seasonTicketLoader = new SeasonTicketLoader(localDaoSession, nsiDaoSession);
        AdditionalInfoForEttLoader additionalInfoForEttLoader = new AdditionalInfoForEttLoader(localDaoSession, nsiDaoSession);
        PriceLoader priceLoader = new PriceLoader(localDaoSession, nsiDaoSession);
        FeeLoader feeLoader = new FeeLoader(localDaoSession, nsiDaoSession);
        BankCardPaymentLoader bankCardPaymentLoader = new BankCardPaymentLoader(localDaoSession, nsiDaoSession);
        TicketTypeLoader ticketTypeLoader = new TicketTypeLoader(localDaoSession, nsiDaoSession);
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
        TicketSaleReturnEventBaseLoader ticketSaleReturnEventBaseLoader = new TicketSaleReturnEventBaseLoader(
                localDaoSession,
                nsiDaoSession,
                ticketEventBaseLoader,
                parentTicketInfoLoader,
                legalEntityLoader,
                exemptionLoader,
                checkLoader,
                trainInfoLoader,
                seasonTicketLoader,
                additionalInfoForEttLoader,
                priceLoader,
                feeLoader,
                bankCardPaymentLoader,
                ticketTypeLoader,
                stationDeviceLoader);
        PreTicketLoader preTicketLoader = new PreTicketLoader(localDaoSession, nsiDaoSession, stationLoader);
        TicketSaleLoader ticketSaleLoader = new TicketSaleLoader(
                localDaoSession,
                nsiDaoSession,
                ticketSaleReturnEventBaseLoader,
                checkLoader,
                preTicketLoader
        );

        TicketSaleEventWriter ticketSaleEventWriter = new TicketSaleEventWriter(dateFormatter);

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
                    CPPKTicketSale cppkTicketSale = ticketSaleLoader.load(cursor, new Offset());
                    writeToFileExecutor.submit(() -> {
                        ticketSaleEventWriter.write(cppkTicketSale, localWriter);
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
        final String SaleEventTable = "SaleEvent";
        final String TicketSaleReturnEventBaseTable = "TicketSaleReturnEventBase";
        final String TicketEventBaseTable = "TicketEventBase";
        final String EventTable = "Event";
        final String StationDeviceTable = "StationDevice";
        final String LegalEntityTable = "LegalEntity";
        final String TrainInfoTable = "TrainInfo";
        final String PriceTable = "Price";
        final String CheckTable = "CheckTable";

        sb.append("SELECT ");
        sb.append(createColumnsForSelect(SaleEventTable, TicketSaleLoader.Columns.all)).append(", "); //CPPKTicketSales
        sb.append(createColumnsForSelect(TicketSaleReturnEventBaseTable, TicketSaleLoader.TicketSaleReturnEventBaseColumns.all)).append(", "); //TicketSaleReturnEventBase.IsTicketWritten
        sb.append(createColumnsForSelect(CheckTable, CheckLoader.TicketEventBaseColumns.all)).append(", "); //TicketEventBase.TicketNumber
        sb.append(createColumnsForSelect(TicketSaleReturnEventBaseTable, TicketSaleReturnEventBaseLoader.Columns.allForSale)).append(", ");
        sb.append(createColumnsForSelect(TicketEventBaseTable, TicketEventBaseLoader.Columns.allForSale)).append(", ");
        sb.append(createColumnsForSelect(EventTable, EventLoader.Columns.all)).append(", ");
        sb.append(createColumnsForSelect(StationDeviceTable, StationDeviceLoader.Columns.all)).append(", ");
        sb.append(createColumnsForSelect(LegalEntityTable, LegalEntityLoader.Columns.all)).append(", ");
        sb.append(createColumnsForSelect(TrainInfoTable, TrainInfoLoader.Columns.all)).append(", ");
        sb.append(createColumnsForSelect(PriceTable, PriceLoader.Columns.all));

        sb.append(" FROM ");
        sb.append(CppkTicketSaleDao.TABLE_NAME).append(" ").append(SaleEventTable);

        //Event
        sb.append(" JOIN ").append(EventDao.TABLE_NAME).append(" ").append(EventTable).append(" ON ")
                .append(EventTable).append(".").append(BaseEntityDao.Properties.Id).append("=").append(SaleEventTable).append(".").append(CppkTicketSaleDao.Properties.EventId);

        //Check
        sb.append(" JOIN ").append(CheckDao.TABLE_NAME).append(" ").append(CheckTable).append(" ON ")
                .append(CheckTable).append(".").append(BaseEntityDao.Properties.Id).append("=").append(TicketSaleReturnEventBaseTable).append(".").append(TicketSaleReturnEventBaseDao.Properties.CheckId);

        //TicketSaleReturnEventBase
        sb.append(" JOIN ").append(TicketSaleReturnEventBaseDao.TABLE_NAME).append(" ").append(TicketSaleReturnEventBaseTable).append(" ON ")
                .append(TicketSaleReturnEventBaseTable).append(".").append(BaseEntityDao.Properties.Id).append("=").append(SaleEventTable).append(".").append(CppkTicketSaleDao.Properties.TicketSaleReturnEventBaseId);

        //TicketEventBase
        sb.append(" JOIN ").append(TicketEventBaseDao.TABLE_NAME).append(" ").append(TicketEventBaseTable).append(" ON ")
                .append(TicketSaleReturnEventBaseTable).append(".").append(TicketSaleReturnEventBaseDao.Properties.TicketEventBaseId).append("=").append(TicketEventBaseTable).append(".").append(BaseEntityDao.Properties.Id);

        //StationDevice
        sb.append(" JOIN ").append(StationDeviceDao.TABLE_NAME).append(" ").append(StationDeviceTable).append(" ON ")
                .append(StationDeviceTable).append(".").append(BaseEntityDao.Properties.Id).append("=").append(EventTable).append(".").append(EventDao.Properties.StationDeviceId);

        //LegalEntity
        sb.append(" JOIN ").append(LegalEntityDao.TABLE_NAME).append(" ").append(LegalEntityTable).append(" ON ")
                .append(LegalEntityTable).append(".").append(LegalEntityDao.Properties.Id).append("=").append(TicketSaleReturnEventBaseTable).append(".").append(TicketSaleReturnEventBaseDao.Properties.LegalEntityId);

        //TrainInfo
        sb.append(" JOIN ").append(TrainInfoDao.TABLE_NAME).append(" ").append(TrainInfoTable).append(" ON ")
                .append(TrainInfoTable).append(".").append(TrainInfoDao.Properties.Id).append("=").append(TicketSaleReturnEventBaseTable).append(".").append(TicketSaleReturnEventBaseDao.Properties.TrainInfoId);

        //Price
        sb.append(" JOIN ").append(PriceDao.TABLE_NAME).append(" ").append(PriceTable).append(" ON ")
                .append(PriceTable).append(".").append(BaseEntityDao.Properties.Id).append("=").append(TicketSaleReturnEventBaseTable).append(".").append(TicketSaleReturnEventBaseDao.Properties.PriceId);


        sb.append(" WHERE ").append(EventTable).append(".").append(EventDao.Properties.CreationTimestamp).append(">").append(fromTime.getTime());
        //для тестов
        //sb.append(" AND ").append(TicketEventBaseTable).append(".").append(TicketEventBaseDao.Properties.SmartCardId).append(">0");
        //sb.append(" AND ").append(EventTable).append(".").append(ConstantsDB.Event_StationCode).append(">0");
        //sb.append(" LIMIT 500 ");

        return sb.toString();
    }
}
