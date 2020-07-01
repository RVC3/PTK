package ru.ppr.cppk.export;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;
import android.os.SystemClock;
import android.provider.Settings;
import android.util.Pair;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import ru.ppr.core.domain.helper.DbFileUpdater;
import ru.ppr.core.manager.eds.TransportOutDirExportFileFilter;
import ru.ppr.cppk.BuildConfig;
import ru.ppr.cppk.Globals;
import ru.ppr.cppk.PathsConstants;
import ru.ppr.cppk.R;
import ru.ppr.cppk.db.DateFormatOperations;
import ru.ppr.cppk.db.LocalDaoSession;
import ru.ppr.cppk.db.local.ShiftEventDao;
import ru.ppr.cppk.di.Dagger;
import ru.ppr.cppk.di.Di;
import ru.ppr.cppk.entity.event.model.Check;
import ru.ppr.cppk.entity.event.model.StationDevice;
import ru.ppr.cppk.entity.settings.PrivateSettings;
import ru.ppr.cppk.export.Exchange.Error;
import ru.ppr.cppk.export.builder.PtkShiftSummaryBuilder;
import ru.ppr.cppk.export.builder.StateBuilder;
import ru.ppr.cppk.export.model.EventsDateTime;
import ru.ppr.cppk.export.model.PtkShiftSummary;
import ru.ppr.cppk.export.model.State;
import ru.ppr.cppk.export.model.request.GetEventsReq;
import ru.ppr.cppk.export.model.request.PtkStartSyncRequest;
import ru.ppr.cppk.export.writer.EventsDateTimeWriter;
import ru.ppr.cppk.export.writer.PtkShiftSummaryWriter;
import ru.ppr.cppk.export.writer.StateWriter;
import ru.ppr.cppk.helpers.FilePathProvider;
import ru.ppr.cppk.localdb.model.LogActionType;
import ru.ppr.cppk.localdb.model.LogEvent;
import ru.ppr.cppk.localdb.model.ShiftEvent;
import ru.ppr.cppk.localdb.model.UpdateEvent;
import ru.ppr.cppk.localdb.model.UpdateEventType;
import ru.ppr.cppk.logic.LogEventBuilder;
import ru.ppr.cppk.managers.FileCleaner;
import ru.ppr.cppk.managers.db.NsiDbManager;
import ru.ppr.cppk.managers.db.SecurityDbManager;
import ru.ppr.cppk.model.SentEvents;
import ru.ppr.cppk.settings.SharedPreferencesUtils;
import ru.ppr.cppk.sync.BankTransactionEventsExport;
import ru.ppr.cppk.sync.CashRegisterWorkingShiftExport;
import ru.ppr.cppk.sync.ControlEventsExport;
import ru.ppr.cppk.sync.FinePaidEventsExport;
import ru.ppr.cppk.sync.MonthClosureExport;
import ru.ppr.cppk.sync.ReturnEventsExport;
import ru.ppr.cppk.sync.SaleEventsExport;
import ru.ppr.cppk.sync.ServiceSaleExport;
import ru.ppr.cppk.sync.ServiceTicketControlsExport;
import ru.ppr.cppk.sync.TestTicketEventsExport;
import ru.ppr.cppk.sync.TicketPaperRollEventExporter;
import ru.ppr.cppk.sync.TicketReSignExporter;
import ru.ppr.cppk.ui.activity.base.Navigator;
import ru.ppr.cppk.utils.PrivateSettingsUtils;
import ru.ppr.cppk.utils.Utils;
import ru.ppr.edssft.model.SignDataResult;
import ru.ppr.logger.Logger;
import ru.ppr.nsi.NsiDaoSession;
import ru.ppr.targz.Packer;
import ru.ppr.utils.FileUtils;
import ru.ppr.utils.FileUtils2;
import ru.ppr.utils.MtpUtils;
import ru.ppr.utils.TimeLogger;
import ru.ppr.utils.ZipUtils;

/**
 * Класс - набор функций для реализации ответной части протокола обмена данными
 * с АРМ загрузки данных. Создает ответ за запросы кассы.
 *
 * @author G.Kashka
 */
public class Response {

    private static final String TAG = Logger.makeLogTag(Response.class);
    private static final int RESPONSE_COUNT = 2;

    public static final String KPP_getEventsResp = Exchange.KPP + "/" + "getEvents_resp.bin";
    public static final String KPP_getEventsRespSig = Exchange.KPP + "/" + "getEvents_resp.sig";

    public static final String RDS_RdsResp = Exchange.RDS + "/" + "RDS_resp.bin";
    public static final String RDS_RdsRespSig = Exchange.RDS + "/" + "RDS_resp.sig";

    public static final String SECURITY_SecurityResp = Exchange.SECURITY + "/" + "Security_resp.bin";
    public static final String SECURITY_SecurityRespSig = Exchange.SECURITY + "/" + "Security_resp.sig";

    public static final String STATE_getStateResp = Exchange.STATE + "/" + "getState_resp.bin";
    public static final String STATE_getStateRespSig = Exchange.STATE + "/" + "getState_resp.sig";
    /**
     * Файл с массивом названий файлов из папки SftTransport/in/ создается перед
     * state.bin удаляется/обновляется вместе с ним.
     */
    public static final String STATE_sftfileslist = Exchange.STATE + "/" + "sftfileslist.bin";

    public static final String STATE_getLastShiftResp = Exchange.STATE + "/" + "getLastShift_resp.bin";
    public static final String STATE_getLastShiftRespSig = Exchange.STATE + "/" + "getLastShift_resp.sig";

    public static final String STATE_armConnectedResp = Exchange.STATE + "/" + "armConnected.resp";

    public static final String STATE_getSettingsResp = Exchange.STATE + "/" + "getSettings_resp.bin";
    public static final String STATE_getSettingsRespSig = Exchange.STATE + "/" + "getSettings_resp.sig";

    public static final String STATE_getTimeResp = Exchange.STATE + "/" + "getTime_resp.bin";
    public static final String STATE_getTimeRespSig = Exchange.STATE + "/" + "getTime_resp.sig";

    public static final String STATE_getBackupResp = Exchange.STATE + "/" + "getBackup_resp.bin";
    public static final String STATE_getBackupRespSig = Exchange.STATE + "/" + "getBackup_resp.sig";

    public static final String STATE_setSettingsResp = Exchange.STATE + "/" + "setSettings_resp.bin";
    public static final String STATE_setSettingsRespSig = Exchange.STATE + "/" + "setSettings_resp.sig";

    public static final String STATE_setTimeResp = Exchange.STATE + "/" + "setTime_resp.bin";
    public static final String STATE_setTimeRespSig = Exchange.STATE + "/" + "setTime_resp.sig";

    public static final String STATE_syncFinishedResp = Exchange.STATE + "/" + "syncFinished.resp";
    public static final String STATE_syncFinishedRespSig = Exchange.STATE + "/" + "syncFinished.sig";

    public static final String STATE_syncCancelledInfo = Exchange.CANCEL + "/" + "syncCancelled.info";
    public static final String STATE_syncCancelledInfoSig = Exchange.CANCEL + "/" + "syncCancelled.sig";

    public static final String SOFTWARE_newVersionResp = Exchange.SOFTWARE + "/" + "newVersion_resp.bin";
    public static final String SOFTWARE_newVersionRespSig = Exchange.SOFTWARE + "/" + "newVersion_resp.sig";
    public static final String SOFTWARE_newApkFoundResp = Exchange.SOFTWARE + "/" + "newApkFound_resp.bin";
    public static final String SOFTWARE_newApkFoundRespSig = Exchange.SOFTWARE + "/" + "newApkFound_resp.sig";

    public static final String SOFTWARE_newVersionSettingsResp = Exchange.SOFTWARE + "/" + "newVersionSettings_resp.bin";
    public static final String SOFTWARE_newVersionSettingRespSig = Exchange.SOFTWARE + "/" + "newVersionSettings_resp.sig";

    public static final String SFT_transmissioncompleted_resp = Exchange.SFT + "/" + "transmissioncompleted.resp";

    private static final String[] eventRespFilePrefix = {
            "shiftEvents",
            "ticketControls",
            "ticketSales",
            "testTickets",
            "ticketReturns",
            "monthClosures",
            "ticketPaperRolls",
            "bankTransactions",
            "ticketReSigns",
            "serviceSales",
            "finePaidEvents",
            "serviceTicketControls"
    };

    private final Globals g;
    private final Gson gson;
    private final FileCleaner fileCleaner;
    private final FilePathProvider filePathProvider;
    private final LogEventBuilder logEventBuilder;

    //Флаг пользовательского завершения синхронизации
    private AtomicBoolean isCancelled = new AtomicBoolean(false);

    public static boolean DEBUG = BuildConfig.DEBUG;

    public void addLog(String log) {
        Logger.info(TAG, log);
    }

    LocalDaoSession getLocalDaoSession() {
        return g.getLocalDaoSession();
    }

    NsiDaoSession getNsiDaoSession() {
        return g.getNsiDaoSession();
    }

    public Response(Globals g) {
        this.g = g;
        gson = new GsonBuilder()
                .serializeNulls()
                .create();

        fileCleaner = Dagger.appComponent().fileCleaner();
        filePathProvider = Dagger.appComponent().filePathProvider();
        logEventBuilder = Dagger.appComponent().logEventBuilder();
    }

    public void createGetEventsResp(GetEventsReq getEventsReq, Error error, long requestTimeStamp) {

        Logger.info(TAG, "createGetEventsResp START error.code=" + error.code);

        FileUtils.clearFolderMtp(g, Exchange.KPP);

        if (!error.isError()) {
            // Получаем текущее состояние смены, если закрыта - разрешаем
            // синхронизацию, если открыта то нет
            error = checkShiftState();
        }

        //запретим выгрузку если базы неактуальны
        if (!error.isError()) {
            error = checkDbDataContractVersions();
        }

        if (!error.isError()) {

            ResponsePacket[] rPackets = new ResponsePacket[eventRespFilePrefix.length];
            for (int i = 0; i < rPackets.length; i++) {
                rPackets[i] = new ResponsePacket();
            }
            long[] timeStampResults = new long[eventRespFilePrefix.length];

            //сравним данные о событиях ушедших в ЦОД с нашей информацией, при необходимости обновим.
            SentEvents cSentEvents = getLocalDaoSession().getSentEventsDao().load();
            if (cSentEvents.getSentShiftEvents() < getEventsReq.lastTimestampsForSentEvent.shiftEvents)
                cSentEvents.setSentShiftEvents(getEventsReq.lastTimestampsForSentEvent.shiftEvents);
            if (cSentEvents.getSentTicketControls() < getEventsReq.lastTimestampsForSentEvent.ticketControls)
                cSentEvents.setSentTicketControls(getEventsReq.lastTimestampsForSentEvent.ticketControls);
            if (cSentEvents.getSentTicketSales() < getEventsReq.lastTimestampsForSentEvent.ticketSales)
                cSentEvents.setSentTicketSales(getEventsReq.lastTimestampsForSentEvent.ticketSales);
            if (cSentEvents.getSentTestTickets() < getEventsReq.lastTimestampsForSentEvent.testTickets)
                cSentEvents.setSentTestTickets(getEventsReq.lastTimestampsForSentEvent.testTickets);
            if (cSentEvents.getSentTicketReturns() < getEventsReq.lastTimestampsForSentEvent.ticketReturns)
                cSentEvents.setSentTicketReturns(getEventsReq.lastTimestampsForSentEvent.ticketReturns);
            if (cSentEvents.getSentMonthClosures() < getEventsReq.lastTimestampsForSentEvent.monthClosures)
                cSentEvents.setSentMonthClosures(getEventsReq.lastTimestampsForSentEvent.monthClosures);
            if (cSentEvents.getSentTicketPaperRolls() < getEventsReq.lastTimestampsForSentEvent.ticketPaperRolls)
                cSentEvents.setSentTicketPaperRolls(getEventsReq.lastTimestampsForSentEvent.ticketPaperRolls);
            if (cSentEvents.getSentBankTransactions() < getEventsReq.lastTimestampsForSentEvent.bankTransactions)
                cSentEvents.setSentBankTransactions(getEventsReq.lastTimestampsForSentEvent.bankTransactions);
            if (cSentEvents.getSentTicketReSigns() < getEventsReq.lastTimestampsForSentEvent.ticketReSigns)
                cSentEvents.setSentTicketReSigns(getEventsReq.lastTimestampsForSentEvent.ticketReSigns);
            if (cSentEvents.getSentServiceSales() < getEventsReq.lastTimestampsForSentEvent.serviceSales)
                cSentEvents.setSentServiceSales(getEventsReq.lastTimestampsForSentEvent.serviceSales);
            if (cSentEvents.getSentFinePaidEvents() < getEventsReq.lastTimestampsForSentEvent.finePaidEvents)
                cSentEvents.setSentFinePaidEvents(getEventsReq.lastTimestampsForSentEvent.finePaidEvents);
            if (cSentEvents.getSentServiceTicketControls() < getEventsReq.lastTimestampsForSentEvent.serviceTicketControls)
                cSentEvents.setSentServiceTicketControls(getEventsReq.lastTimestampsForSentEvent.serviceTicketControls);
            getLocalDaoSession().getSentEventsDao().update(cSentEvents);

            // создаем архивчики с файлами событий ПТК
            g.getBroadcasts().newArmSyncEvent(g, ArmSyncEvents.ShiftEventsCreateStart, "", rPackets[0].error);
            timeStampResults[0] = createShiftEventsResp(Math.max(getEventsReq.lastTimestampsForEvent.shiftEvents, cSentEvents.getSentShiftEvents()), rPackets[0], requestTimeStamp);
            g.getBroadcasts().newArmSyncEvent(g, ArmSyncEvents.ShiftEventsRespCreated, "", rPackets[0].error);

            g.getBroadcasts().newArmSyncEvent(g, ArmSyncEvents.TicketControlsCreateStart, "", rPackets[1].error);
            timeStampResults[1] = createTicketControlsResp(Math.max(getEventsReq.lastTimestampsForEvent.ticketControls, cSentEvents.getSentTicketControls()), rPackets[1], requestTimeStamp);
            g.getBroadcasts().newArmSyncEvent(g, ArmSyncEvents.TicketControlsRespCreated, "", rPackets[1].error);

            g.getBroadcasts().newArmSyncEvent(g, ArmSyncEvents.TicketSalesCreateStart, "", rPackets[2].error);
            timeStampResults[2] = createTicketSalesResp(Math.max(getEventsReq.lastTimestampsForEvent.ticketSales, cSentEvents.getSentTicketSales()), rPackets[2], requestTimeStamp);
            g.getBroadcasts().newArmSyncEvent(g, ArmSyncEvents.TicketSalesRespCreated, "", rPackets[2].error);

            g.getBroadcasts().newArmSyncEvent(g, ArmSyncEvents.TestTicketsCreateStart, "", rPackets[3].error);
            timeStampResults[3] = createTestTicketsResp(Math.max(getEventsReq.lastTimestampsForEvent.testTickets, cSentEvents.getSentTestTickets()), rPackets[3], requestTimeStamp);
            g.getBroadcasts().newArmSyncEvent(g, ArmSyncEvents.TestTicketsRespCreated, "", rPackets[3].error);

            g.getBroadcasts().newArmSyncEvent(g, ArmSyncEvents.TicketReturnsCreateStart, "", rPackets[4].error);
            timeStampResults[4] = createTicketReturnsResp(Math.max(getEventsReq.lastTimestampsForEvent.ticketReturns, cSentEvents.getSentTicketReturns()), rPackets[4], requestTimeStamp);
            g.getBroadcasts().newArmSyncEvent(g, ArmSyncEvents.TicketReturnsRespCreated, "", rPackets[4].error);

            g.getBroadcasts().newArmSyncEvent(g, ArmSyncEvents.MonthClosuresCreateStart, "", rPackets[5].error);
            timeStampResults[5] = createMonthClosuresResp(Math.max(getEventsReq.lastTimestampsForEvent.monthClosures, cSentEvents.getSentMonthClosures()), rPackets[5], requestTimeStamp);
            g.getBroadcasts().newArmSyncEvent(g, ArmSyncEvents.MonthClosuresRespCreated, "", rPackets[5].error);

            g.getBroadcasts().newArmSyncEvent(g, ArmSyncEvents.TicketPaperRollsCreateStart, "", rPackets[6].error);
            timeStampResults[6] = createTicketPaperRollsResp(Math.max(getEventsReq.lastTimestampsForEvent.ticketPaperRolls, cSentEvents.getSentTicketPaperRolls()), rPackets[6], requestTimeStamp);
            g.getBroadcasts().newArmSyncEvent(g, ArmSyncEvents.TicketPaperRollsRespCreated, "", rPackets[6].error);

            g.getBroadcasts().newArmSyncEvent(g, ArmSyncEvents.BankTransactionsCreateStart, "", rPackets[7].error);
            timeStampResults[7] = createBankTransactionsResp(Math.max(getEventsReq.lastTimestampsForEvent.bankTransactions, cSentEvents.getSentBankTransactions()), rPackets[7], requestTimeStamp);
            g.getBroadcasts().newArmSyncEvent(g, ArmSyncEvents.BankTransactionsRespCreated, "", rPackets[7].error);

            g.getBroadcasts().newArmSyncEvent(g, ArmSyncEvents.TicketResignsCreateStart, "", rPackets[8].error);
            timeStampResults[8] = createTicketReSignsResp(Math.max(getEventsReq.lastTimestampsForEvent.ticketReSigns, cSentEvents.getSentTicketReSigns()), rPackets[8], requestTimeStamp);
            g.getBroadcasts().newArmSyncEvent(g, ArmSyncEvents.TicketResignsRespCreated, "", rPackets[8].error);

            g.getBroadcasts().newArmSyncEvent(g, ArmSyncEvents.ServiceSalesCreateStart, "", rPackets[9].error);
            timeStampResults[9] = createServiceSalesResp(Math.max(getEventsReq.lastTimestampsForEvent.serviceSales, cSentEvents.getSentServiceSales()), rPackets[9], requestTimeStamp);
            g.getBroadcasts().newArmSyncEvent(g, ArmSyncEvents.ServiceSalesRespCreated, "", rPackets[9].error);

            g.getBroadcasts().newArmSyncEvent(g, ArmSyncEvents.FinePaidEventsCreateStart, "", rPackets[10].error);
            timeStampResults[10] = createFinePaidEventsResp(Math.max(getEventsReq.lastTimestampsForEvent.finePaidEvents, cSentEvents.getSentFinePaidEvents()), rPackets[10], requestTimeStamp);
            g.getBroadcasts().newArmSyncEvent(g, ArmSyncEvents.FinePaidEventsRespCreated, "", rPackets[10].error);

            g.getBroadcasts().newArmSyncEvent(g, ArmSyncEvents.ServiceTicketControlsRespCreateStart, "", rPackets[11].error);
            timeStampResults[11] = createServiceTicketControlsResp(Math.max(getEventsReq.lastTimestampsForEvent.serviceTicketControls, cSentEvents.getSentServiceTicketControls()), rPackets[11], requestTimeStamp);
            g.getBroadcasts().newArmSyncEvent(g, ArmSyncEvents.ServiceTicketControlsRespCreated, "", rPackets[11].error);

            // запускаем процесс их подписывания
            Logger.info(TAG, "createGetEventsResp создание файлов json по всем событиям завершено, запускаем процесс их подписывания");
            startSigEventRespFile(timeStampResults, rPackets, 0, requestTimeStamp);
        }

        if (error.isError())
            createErrorFile(KPP_getEventsResp, KPP_getEventsRespSig, error, requestTimeStamp);

        Logger.info(TAG, "createGetEventsResp FINISH error.code=" + error.code);
    }

    private void startSigEventRespFile(final long[] timeStampResults, final ResponsePacket[] rPackets, final int currentItem, long requestTimeStamp) {

        Logger.info(TAG, "startSigEventRespFile START");

        if (currentItem < timeStampResults.length) {

            // если файл не пустой, то создаем подпись
            if (rPackets[currentItem].respFile != null && rPackets[currentItem].respFile.exists()) {
                String respFileNameNo = Exchange.KPP + "/" + eventRespFilePrefix[currentItem] + "_" + timeStampResults[currentItem] + ".bin";
                String sigFileNameNo = Exchange.KPP + "/" + eventRespFilePrefix[currentItem] + "_" + timeStampResults[currentItem] + ".sig";
                boolean isOk = createSigFile(rPackets[currentItem].respFile, sigFileNameNo, respFileNameNo, requestTimeStamp);
                g.getBroadcasts().newArmSyncEvent(g,
                        ArmSyncEvents.SigCreateReady,
                        Exchange.Fn.getFileWithTimeStamp(new File(sigFileNameNo), requestTimeStamp).getName(),
                        (isOk) ? (new Error()) : (new Error(Error.ECP, "")));
                int currentItemNew = currentItem + 1;
                // если все файлы кончились создаем общий resp по всем
                // типам Event-ов (почему-то нельзя использовать только
                // нижнюю конструкцию (самый нижний else в этой функции)
                // он туда не всегда заходит)
                if (currentItemNew >= timeStampResults.length) {
                    readyToSigEventItemFiles(timeStampResults, rPackets, requestTimeStamp);
                } else {
                    // когда дождались что файл подписан - запускаем
                    // подпись следующего.
                    startSigEventRespFile(timeStampResults, rPackets, currentItemNew, requestTimeStamp);
                }
            }
            // если вместо файла null переходим к следующему
            else {
                startSigEventRespFile(timeStampResults, rPackets, currentItem + 1, requestTimeStamp);
            }
        } else {
            // когда дождались что файл подписан - запускаем подпись следующего.
            // (этот блок пригодится если все файлы были пустыми)
            readyToSigEventItemFiles(timeStampResults, rPackets, requestTimeStamp);
        }

        Logger.info(TAG, "startSigEventRespFile FINISH");

    }

    /**
     * К этому моменту подпись файлов со списками событий смен, контроля,
     * продажи и печати тестового ПД - завершена, нужно создать файл getEvets_resp
     *
     * @param timeStampResults
     * @param rPackets
     */
    private void readyToSigEventItemFiles(long[] timeStampResults, ResponsePacket[] rPackets, long requestTimeStamp) {

        Logger.info(TAG, "readyToSigEventItemFiles START");

        Error error = new Error();
        for (ResponsePacket rPacket : rPackets) {
            if (rPacket.error.isError()) {
                error.code = rPacket.error.code;
                error = new Error(rPacket.error.code, error.getMessage() + " " + rPacket.error.getMessage());
            }
        }

        g.getBroadcasts().newArmSyncEvent(g, ArmSyncEvents.AllEventRespCreateStart, "", error);

        File f = ExportUtils.getTempFile(KPP_getEventsResp, requestTimeStamp);

        if (!error.isError()) {

            EventsDateTime eventsDateTime = new EventsDateTime();
            eventsDateTime.shiftEvents = timeStampResults[0];
            eventsDateTime.ticketControls = timeStampResults[1];
            eventsDateTime.ticketSales = timeStampResults[2];
            eventsDateTime.testTickets = timeStampResults[3];
            eventsDateTime.ticketReturns = timeStampResults[4];
            eventsDateTime.monthClosures = timeStampResults[5];
            eventsDateTime.ticketPaperRolls = timeStampResults[6];
            eventsDateTime.bankTransactions = timeStampResults[7];
            eventsDateTime.ticketReSigns = timeStampResults[8];
            eventsDateTime.serviceSales = timeStampResults[9];
            eventsDateTime.finePaidEvents = timeStampResults[10];
            eventsDateTime.serviceTicketControls = timeStampResults[11];

            EventsDateTimeWriter eventsDateTimeWriter = new EventsDateTimeWriter();

            // создаем временный файлик ответа getEvent_resp
            f.getParentFile().mkdirs();
            f.delete();

            try {
                FileOutputStream fos = new FileOutputStream(f, true);
                PrintStream ps = new PrintStream(fos);

                JSONObject json = eventsDateTimeWriter.getJson(eventsDateTime);

                ps.append(json.toString());

                ps.flush();
                fos.flush();

                ps.close();
                fos.close();

                addFileContentToLog(f.getAbsolutePath());

                createSigFile(f, KPP_getEventsRespSig, KPP_getEventsResp, requestTimeStamp);

            } catch (FileNotFoundException e) {
                error = new Error(Error.FileNotFoundException, e.getMessage());
                Logger.error(TAG, "Ошибка при попытке создать файл getEvents_resp - " + e.getMessage());
                Logger.error(TAG, e);
            } catch (JSONException e) {
                error = new Error(Error.JSONException, e.getMessage());
                Logger.error(TAG, "Ошибка при попытке создать файл getEvents_resp - " + e.getMessage());
                Logger.error(TAG, e);
            } catch (IOException e) {
                error = new Error(Error.IOException, e.getMessage());
                Logger.error(TAG, "Ошибка при попытке создать файл getEvents_resp - " + e.getMessage());
                Logger.error(TAG, e);
            }
        }

        // если были ошибки при создании файла с общим ответом, тогда рисуем
        // ошибку
        if (error.isError()) {
            FileUtils.deleteFileMtp(g, f);
            createErrorFile(KPP_getEventsResp, KPP_getEventsRespSig, error, requestTimeStamp);
        }

        if (!error.isError())
            SharedPreferencesUtils.setGetEventRespDateTime(g, System.currentTimeMillis());

        g.getBroadcasts().newArmSyncEvent(g, ArmSyncEvents.GetEventsRespCreated, null, error);

        Logger.info(TAG, "readyToSigEventItemFiles FINISH isOk: " + !error.isError());
    }

    /**
     * Создает файлы ответа с событиями по сменам и возвращает timestamp
     * последнего события
     *
     * @param fromTimestamp
     * @param rPacket
     * @return
     */
    public long createShiftEventsResp(long fromTimestamp, ResponsePacket rPacket, long requestTimeStamp) {

        Logger.info(TAG, "createShiftEventsResp START");

        long lastTimeStampEvent = getLocalDaoSession().getShiftEventDao().getLastShiftEventCreationTimeStamp();

        try {

            if (lastTimeStampEvent > fromTimestamp) {

                String fileName = Exchange.KPP + "/shiftEvents_" + lastTimeStampEvent;
                File jsonFile = new File(fileName + ".json");

                CashRegisterWorkingShiftExport shiftExport = new CashRegisterWorkingShiftExport(
                        getLocalDaoSession(),
                        getNsiDaoSession(),
                        Dagger.appComponent().nsiVersionManager(),
                        Dagger.appComponent().fineRepository(),
                        Dagger.appComponent().serviceTicketControlEventRepository(),
                        jsonFile);
                shiftExport.export(new Date(fromTimestamp));

                File zip = new File(fileName + "_" + requestTimeStamp);
                ZipUtils.zip(new String[]{jsonFile.getAbsolutePath()}, zip.getAbsolutePath());
                jsonFile.delete();

                rPacket.respFile = zip;
            } else {
                // http://agile.srvdev.ru/browse/CPPKPP-33106
                // Сообщаем кассе, что новых событий нет
                lastTimeStampEvent = 0;
            }

        } catch (FileNotFoundException e) {
            rPacket.error = new Error(Error.FileNotFoundException, e.getMessage());
            Logger.error(TAG, "Ошибка при попытке создать файл ShiftEvents - " + e.getMessage());
            Logger.error(TAG, e);
        } catch (IOException e) {
            rPacket.error = new Error(Error.IOException, e.getMessage());
            Logger.error(TAG, "Ошибка при попытке создать файл ShiftEvents - " + e.getMessage());
            Logger.error(TAG, e);
        } catch (Exception e) {
            rPacket.error = new Error(Error.UNKNOWN, e.getMessage());
            Logger.error(TAG, e);
        }

        Logger.info(TAG, "createShiftEventsResp FINISH isOk: " + !rPacket.error.isError());

        return lastTimeStampEvent;
    }

    @SuppressLint("SimpleDateFormat")
    /**
     * Создает ответ о событиях контроля и возвращает timestamp последнего события
     * @param fromTimestamp
     * @param rPacket
     * @param requestTimeStamp
     */
    public long createTicketControlsResp(long fromTimestamp, ResponsePacket rPacket, long requestTimeStamp) {

        Logger.info(TAG, "createTicketControlsResp START");

        long lastTimeStampEvent = getLocalDaoSession().getCppkTicketControlsDao().getLastControlEventCreationTimeStamp();

        try {

            if (lastTimeStampEvent > fromTimestamp) {

                String fileName = Exchange.KPP + "/ticketControls_" + lastTimeStampEvent;
                File jsonFile = new File(fileName + ".json");

                ControlEventsExport ce = new ControlEventsExport(getLocalDaoSession(), getNsiDaoSession(), Dagger.appComponent().nsiVersionManager(), jsonFile);
                ce.export(new Date(fromTimestamp));

                File zip = new File(fileName + "_" + requestTimeStamp);
                ZipUtils.zip(new String[]{jsonFile.getAbsolutePath()}, zip.getAbsolutePath());
                jsonFile.delete();

                rPacket.respFile = zip;
            } else {
                // http://agile.srvdev.ru/browse/CPPKPP-33106
                // Сообщаем кассе, что новых событий нет
                lastTimeStampEvent = 0;
            }

        } catch (FileNotFoundException e) {
            rPacket.error = new Error(Error.FileNotFoundException, e.getMessage());
            Logger.error(TAG, "Ошибка при попытке создать файл Событий TicketControls - " + e.getMessage());
            Logger.error(TAG, e);
        } catch (IOException e) {
            rPacket.error = new Error(Error.IOException, e.getMessage());
            Logger.error(TAG, "Ошибка при попытке создать файл Событий TicketControls - " + e.getMessage());
            Logger.error(TAG, e);
        } catch (Exception e) {
            rPacket.error = new Error(Error.UNKNOWN, e.getMessage());
            Logger.error(TAG, e);
        }
        Logger.info(TAG, "createTicketControlsResp FINISH isOk: " + !rPacket.error.isError());

        return lastTimeStampEvent;
    }

    /**
     * Создает ответ о событиях продажи штрафа и возвращает timestamp последнего события
     *
     * @param fromTimestamp
     * @param rPacket
     * @param requestTimeStamp
     * @return
     */
    public long createFinePaidEventsResp(long fromTimestamp, ResponsePacket rPacket, long requestTimeStamp) {

        Logger.info(TAG, "createFinePaidEventsResp START");

        long lastTimeStampEvent = getLocalDaoSession().getFineSaleEventDao().getLastFinePaidEventCreationTimeStamp();

        try {

            if (lastTimeStampEvent > fromTimestamp) {

                String fileName = Exchange.KPP + "/finePaidEvents_" + lastTimeStampEvent;
                File jsonFile = new File(fileName + ".json");

                FinePaidEventsExport finePaidEventsExport = new FinePaidEventsExport(getLocalDaoSession(), getNsiDaoSession(), jsonFile);
                finePaidEventsExport.export(new Date(fromTimestamp));

                File zip = new File(fileName + "_" + requestTimeStamp);
                ZipUtils.zip(new String[]{jsonFile.getAbsolutePath()}, zip.getAbsolutePath());
                jsonFile.delete();

                rPacket.respFile = zip;
            } else {
                // http://agile.srvdev.ru/browse/CPPKPP-33106
                // Сообщаем кассе, что новых событий нет
                lastTimeStampEvent = 0;
            }

        } catch (FileNotFoundException e) {
            rPacket.error = new Error(Error.FileNotFoundException, e.getMessage());
            Logger.error(TAG, "Ошибка при попытке создать файл Событий FinePaidEvents - " + e.getMessage());
            Logger.error(TAG, e);
        } catch (IOException e) {
            rPacket.error = new Error(Error.IOException, e.getMessage());
            Logger.error(TAG, "Ошибка при попытке создать файл Событий FinePaidEvents - " + e.getMessage());
            Logger.error(TAG, e);
        } catch (Exception e) {
            rPacket.error = new Error(Error.UNKNOWN, e.getMessage());
            Logger.error(TAG, e);
        }
        Logger.info(TAG, "createFinePaidEventsResp FINISH isOk: " + !rPacket.error.isError());

        return lastTimeStampEvent;
    }

    /**
     * Создает ответ о событиях контроля сервисных карт и возвращает timestamp последнего события
     *
     * @param fromTimestamp
     * @param rPacket
     * @param requestTimeStamp
     * @return
     */
    public long createServiceTicketControlsResp(long fromTimestamp, ResponsePacket rPacket, long requestTimeStamp) {

        Logger.info(TAG, "createServiceTicketControlsResp START");

        long lastTimeStampEvent = Dagger.appComponent().serviceTicketControlEventRepository().getLastServiceTicketControlCreationTimeStamp();

        try {

            if (lastTimeStampEvent > fromTimestamp) {

                String fileName = Exchange.KPP + "/serviceTicketControls_" + lastTimeStampEvent;
                File jsonFile = new File(fileName + ".json");

                ServiceTicketControlsExport serviceTicketControlsExport = new ServiceTicketControlsExport(getLocalDaoSession(), getNsiDaoSession(), jsonFile);
                serviceTicketControlsExport.export(new Date(fromTimestamp));

                File zip = new File(fileName + "_" + requestTimeStamp);
                ZipUtils.zip(new String[]{jsonFile.getAbsolutePath()}, zip.getAbsolutePath());
                jsonFile.delete();

                rPacket.respFile = zip;
            } else {
                // http://agile.srvdev.ru/browse/CPPKPP-33106
                // Сообщаем кассе, что новых событий нет
                lastTimeStampEvent = 0;
            }

        } catch (FileNotFoundException e) {
            rPacket.error = new Error(Error.FileNotFoundException, e.getMessage());
            Logger.error(TAG, "Ошибка при попытке создать файл Событий ServiceTicketControls - " + e.getMessage());
            Logger.error(TAG, e);
        } catch (IOException e) {
            rPacket.error = new Error(Error.IOException, e.getMessage());
            Logger.error(TAG, "Ошибка при попытке создать файл Событий ServiceTicketControls - " + e.getMessage());
            Logger.error(TAG, e);
        } catch (Exception e) {
            rPacket.error = new Error(Error.UNKNOWN, e.getMessage());
            Logger.error(TAG, e);
        }
        Logger.info(TAG, "createServiceTicketControlsResp FINISH isOk: " + !rPacket.error.isError());

        return lastTimeStampEvent;
    }


    /**
     * Создает ответ о событиях печати тестовых ПД и возвращает timestamp
     * последнего события
     *
     * @param fromTimestamp время в милисекундах
     * @param rPacket
     */
    public long createTestTicketsResp(long fromTimestamp, ResponsePacket rPacket, long requestTimeStamp) {

        Logger.info(TAG, "createTestTicketsResp START");

        long lastTimeStampEvent = getLocalDaoSession().getTestTicketDao().getLastTestTicketEventCreationTimeStamp();

        try {

            if (lastTimeStampEvent > fromTimestamp) {

                String fileName = Exchange.KPP + "/testTickets_" + lastTimeStampEvent;
                File jsonFile = new File(fileName + ".json");

                TestTicketEventsExport testTicketEventsExport = new TestTicketEventsExport(getLocalDaoSession(), getNsiDaoSession(), jsonFile);
                testTicketEventsExport.export(new Date(fromTimestamp));

                File zip = new File(fileName + "_" + requestTimeStamp);
                ZipUtils.zip(new String[]{jsonFile.getAbsolutePath()}, zip.getAbsolutePath());
                jsonFile.delete();

                rPacket.respFile = zip;
            } else {
                // http://agile.srvdev.ru/browse/CPPKPP-33106
                // Сообщаем кассе, что новых событий нет
                lastTimeStampEvent = 0;
            }

        } catch (FileNotFoundException e) {
            rPacket.error = new Error(Error.FileNotFoundException, e.getMessage());
            Logger.error(TAG, "Ошибка при попытке создать файл TestTickets - " + e.getMessage());
            Logger.error(TAG, e);
        } catch (IOException e) {
            rPacket.error = new Error(Error.IOException, e.getMessage());
            Logger.error(TAG, "Ошибка при попытке создать файл TestTickets - " + e.getMessage());
            Logger.error(TAG, e);
        } catch (Exception e) {
            rPacket.error = new Error(Error.UNKNOWN, e.getMessage());
            Logger.error(TAG, e);
        }

        Logger.info(TAG, "createTestTicketsResp FINISH isOk: " + !rPacket.error.isError());

        return lastTimeStampEvent;
    }

    /**
     * Создает файлы ответа с событиями продажи билетов, возвращает timestamp
     * последнего события
     *
     * @param fromTimestamp
     * @param rPacket
     */
    public long createTicketSalesResp(long fromTimestamp, ResponsePacket rPacket, long requestTimeStamp) {

        Logger.info(TAG, "createTicketSalesResp START");

        long lastTimeStampEvent = getLocalDaoSession().getCppkTicketSaleDao().getLastSaleEventCreationTimeStamp();

        try {

            if (lastTimeStampEvent > fromTimestamp) {

                String fileName = Exchange.KPP + "/ticketSales_" + lastTimeStampEvent;
                File jsonFile = new File(fileName + ".json");

                SaleEventsExport se = new SaleEventsExport(getLocalDaoSession(), getNsiDaoSession(), Dagger.appComponent().nsiVersionManager(), jsonFile);
                se.export(new Date(fromTimestamp));

                File zip = new File(fileName + "_" + requestTimeStamp);
                ZipUtils.zip(new String[]{jsonFile.getAbsolutePath()}, zip.getAbsolutePath());
                jsonFile.delete();

                rPacket.respFile = zip;
            } else {
                // http://agile.srvdev.ru/browse/CPPKPP-33106
                // Сообщаем кассе, что новых событий нет
                lastTimeStampEvent = 0;
            }

        } catch (FileNotFoundException e) {
            rPacket.error = new Error(Error.FileNotFoundException, e.getMessage());
            Logger.error(TAG, "Ошибка при попытке создать файл Событий TicketSales - " + e.getMessage());
            Logger.error(TAG, e);
        } catch (IOException e) {
            rPacket.error = new Error(Error.IOException, e.getMessage());
            Logger.error(TAG, "Ошибка при попытке создать файл Событий TicketSales - " + e.getMessage());
            Logger.error(TAG, e);
        } catch (Exception e) {
            rPacket.error = new Error(Error.UNKNOWN, e.getMessage());
            Logger.error(TAG, e);
        }

        Logger.info(TAG, "createTicketSalesResp FINISH isOk: " + !rPacket.error.isError());

        return lastTimeStampEvent;
    }

    /**
     * Создает файлы ответа с событиями аннулирования билетов, возвращает timestamp
     * последнего события
     *
     * @param fromTimestamp
     * @param rPacket
     */
    public long createTicketReturnsResp(long fromTimestamp, ResponsePacket rPacket, long requestTimeStamp) {

        Logger.info(TAG, "createTicketReturnsResp START");

        long lastTimeStampEvent = getLocalDaoSession().getCppkTicketReturnDao().getLastTicketReturnEventCreationTimeStamp();

        try {
            if (lastTimeStampEvent > fromTimestamp) {

                String fileName = Exchange.KPP + "/ticketReturns_" + lastTimeStampEvent;
                File jsonFile = new File(fileName + ".json");

                ReturnEventsExport returnEventsExport = new ReturnEventsExport(getLocalDaoSession(), getNsiDaoSession(), Dagger.appComponent().nsiVersionManager(), jsonFile);
                returnEventsExport.export(new Date(fromTimestamp));

                File zip = new File(fileName + "_" + requestTimeStamp);
                ZipUtils.zip(new String[]{jsonFile.getAbsolutePath()}, zip.getAbsolutePath());
                jsonFile.delete();

                rPacket.respFile = zip;
            } else {
                // http://agile.srvdev.ru/browse/CPPKPP-33106
                // Сообщаем кассе, что новых событий нет
                lastTimeStampEvent = 0;
            }
        } catch (FileNotFoundException e) {
            rPacket.error = new Error(Error.FileNotFoundException, e.getMessage());
            Logger.error(TAG, "Ошибка при попытке создать файл Событий TicketReturns - " + e.getMessage());
            Logger.error(TAG, e);
        } catch (IOException e) {
            rPacket.error = new Error(Error.IOException, e.getMessage());
            Logger.error(TAG, "Ошибка при попытке создать файл Событий TicketReturns - " + e.getMessage());
            Logger.error(TAG, e);
        } catch (Exception e) {
            rPacket.error = new Error(Error.UNKNOWN, e.getMessage());
            Logger.error(TAG, e);
        }
        Logger.info(TAG, "createTicketReturnsResp FINISH isOk: " + !rPacket.error.isError());
        return lastTimeStampEvent;
    }

    public long createTicketReSignsResp(long fromTimestamp, ResponsePacket rPacket, long requestTimeStamp) {
        Logger.info(TAG, "createTicketReSignsResp START");

        long lastTimeStampEvent = getLocalDaoSession().getCppkTicketReSignDao().getLastTicketReSignCreationTimeStamp();

        try {
            if (lastTimeStampEvent > fromTimestamp) {

                String fileName = Exchange.KPP + "/ticketReSigns_" + lastTimeStampEvent;
                File jsonFile = new File(fileName + ".json");

                TicketReSignExporter ticketReSignExporter = new TicketReSignExporter(getLocalDaoSession(), getNsiDaoSession(), jsonFile);
                ticketReSignExporter.export(new Date(fromTimestamp));

                File zip = new File(fileName + "_" + requestTimeStamp);
                ZipUtils.zip(new String[]{jsonFile.getAbsolutePath()}, zip.getAbsolutePath());
                jsonFile.delete();

                rPacket.respFile = zip;
            } else {
                // http://agile.srvdev.ru/browse/CPPKPP-33106
                // Сообщаем кассе, что новых событий нет
                lastTimeStampEvent = 0;
            }

        } catch (FileNotFoundException e) {
            rPacket.error = new Error(Error.FileNotFoundException, e.getMessage());
            Logger.error(TAG, "Ошибка при попытке создать файл Событий TicketReSigns - " + e.getMessage());
            Logger.error(TAG, e);
        } catch (IOException e) {
            rPacket.error = new Error(Error.IOException, e.getMessage());
            Logger.error(TAG, "Ошибка при попытке создать файл Событий TicketReSigns - " + e.getMessage());
            Logger.error(TAG, e);
        } catch (Exception e) {
            rPacket.error = new Error(Error.UNKNOWN, e.getMessage());
            Logger.error(TAG, e);
        }

        Logger.info(TAG, "createTicketReSignsResp FINISH isOk: " + !rPacket.error.isError());
        return lastTimeStampEvent;
    }

    /**
     * Создает ответ о событиях продажи услуг и возвращает timestamp
     * последнего события
     *
     * @param fromTimestamp время в милисекундах
     * @param rPacket
     */
    public long createServiceSalesResp(long fromTimestamp, ResponsePacket rPacket, long requestTimeStamp) {

        Logger.info(TAG, "createServiceSalesResp START");

        long lastTimeStampEvent = getLocalDaoSession().getCppkServiceSaleDao().getLastServiceSaleEventCreationTimeStamp();
        try {

            if (lastTimeStampEvent > fromTimestamp) {

                String fileName = Exchange.KPP + "/serviceSales_" + lastTimeStampEvent;
                File jsonFile = new File(fileName + ".json");

                ServiceSaleExport serviceSaleExport = new ServiceSaleExport(getLocalDaoSession(), getNsiDaoSession(), jsonFile);
                serviceSaleExport.export(new Date(fromTimestamp));

                File zip = new File(fileName + "_" + requestTimeStamp);
                ZipUtils.zip(new String[]{jsonFile.getAbsolutePath()}, zip.getAbsolutePath());
                jsonFile.delete();

                rPacket.respFile = zip;
            } else {
                // http://agile.srvdev.ru/browse/CPPKPP-33106
                // Сообщаем кассе, что новых событий нет
                lastTimeStampEvent = 0;
            }

        } catch (FileNotFoundException e) {
            rPacket.error = new Error(Error.FileNotFoundException, e.getMessage());
            Logger.error(TAG, "Ошибка при попытке создать файл ServiceSales - " + e.getMessage());
            Logger.error(TAG, e);
        } catch (IOException e) {
            rPacket.error = new Error(Error.IOException, e.getMessage());
            Logger.error(TAG, "Ошибка при попытке создать файл ServiceSales - " + e.getMessage());
            Logger.error(TAG, e);
        }

        Logger.info(TAG, "createServiceSalesResp FINISH isOk: " + !rPacket.error.isError());

        return lastTimeStampEvent;
    }

    /**
     * Создает файлы ответа с событиями закрытия месяца, возвращает timestamp
     * последнего события
     *
     * @param fromTimestamp
     * @param rPacket
     */
    public long createMonthClosuresResp(long fromTimestamp, ResponsePacket rPacket, long requestTimeStamp) {

        Logger.info(TAG, "createMonthClosuresResp START");

        long lastTimeStampEvent = getLocalDaoSession().getMonthEventDao().getLastMonthEventCreationTimeStamp();

        try {
            if (lastTimeStampEvent > fromTimestamp) {

                String fileName = Exchange.KPP + "/monthClosures_" + lastTimeStampEvent;
                File jsonFile = new File(fileName + ".json");

                MonthClosureExport monthClosureExport = new MonthClosureExport(getLocalDaoSession(),
                        getNsiDaoSession(),
                        Dagger.appComponent().nsiVersionManager(),
                        Dagger.appComponent().fineRepository(),
                        jsonFile);
                monthClosureExport.export(new Date(fromTimestamp));

                File zip = new File(fileName + "_" + requestTimeStamp);
                ZipUtils.zip(new String[]{jsonFile.getAbsolutePath()}, zip.getAbsolutePath());
                jsonFile.delete();

                rPacket.respFile = zip;
            } else {
                // http://agile.srvdev.ru/browse/CPPKPP-33106
                // Сообщаем кассе, что новых событий нет
                lastTimeStampEvent = 0;
            }

        } catch (FileNotFoundException e) {
            rPacket.error = new Error(Error.FileNotFoundException, e.getMessage());
            Logger.error(TAG, "Ошибка при попытке создать файл Событий MonthClosures - " + e.getMessage());
            Logger.error(TAG, e);
        } catch (IOException e) {
            rPacket.error = new Error(Error.IOException, e.getMessage());
            Logger.error(TAG, "Ошибка при попытке создать файл Событий MonthClosures - " + e.getMessage());
            Logger.error(TAG, e);
        } catch (Exception e) {
            rPacket.error = new Error(Error.UNKNOWN, e.getMessage());
            Logger.error(TAG, e);
        }

        Logger.info(TAG, "createMonthClosuresResp FINISH isOk: " + !rPacket.error.isError());

        return lastTimeStampEvent;
    }

    /**
     * Создает файлы ответа с событиями смены бумажной ленты, возвращает timestamp
     * последнего события
     *
     * @param fromTimestamp
     * @param rPacket
     */
    public long createTicketPaperRollsResp(long fromTimestamp, ResponsePacket rPacket, long requestTimeStamp) {

        Logger.info(TAG, "createTicketPaperRollsResp START");

        long lastTimeStampEvent = getLocalDaoSession().getTicketTapeEventDao().getLastTicketPaperRollEventCreationTimeStamp();

        try {
            if (lastTimeStampEvent > fromTimestamp) {

                String fileName = Exchange.KPP + "/ticketPaperRolls_" + lastTimeStampEvent;
                File jsonFile = new File(fileName + ".json");

                TicketPaperRollEventExporter ticketPaperRollEventExporter = new TicketPaperRollEventExporter(getLocalDaoSession(), getNsiDaoSession(), jsonFile);
                ticketPaperRollEventExporter.export(new Date(fromTimestamp));

                File zip = new File(fileName + "_" + requestTimeStamp);
                ZipUtils.zip(new String[]{jsonFile.getAbsolutePath()}, zip.getAbsolutePath());
                jsonFile.delete();

                rPacket.respFile = zip;
            } else {
                // http://agile.srvdev.ru/browse/CPPKPP-33106
                // Сообщаем кассе, что новых событий нет
                lastTimeStampEvent = 0;
            }

        } catch (FileNotFoundException e) {
            rPacket.error = new Error(Error.FileNotFoundException, e.getMessage());
            Logger.error(TAG, "Ошибка при попытке создать файл Событий TicketPaperRolls - " + e.getMessage());
            Logger.error(TAG, e);
        } catch (IOException e) {
            rPacket.error = new Error(Error.IOException, e.getMessage());
            Logger.error(TAG, "Ошибка при попытке создать файл Событий TicketPaperRolls - " + e.getMessage());
            Logger.error(TAG, e);
        } catch (Exception e) {
            rPacket.error = new Error(Error.UNKNOWN, e.getMessage());
            Logger.error(TAG, e);
        }

        Logger.info(TAG, "createTicketPaperRollsResp FINISH isOk: " + !rPacket.error.isError());

        return lastTimeStampEvent;
    }

    /**
     * Создает файлы ответа с событиями банковских транзакций, возвращает timestamp
     * последнего события
     *
     * @param fromTimestamp
     * @param rPacket
     */
    public long createBankTransactionsResp(long fromTimestamp, ResponsePacket rPacket, long requestTimeStamp) {

        Logger.info(TAG, "createBankTransactionsResp START");

        long lastTimeStampEvent = getLocalDaoSession().getBankTransactionDao().getLastBankTransactionEventCreationTimeStamp();

        try {
            if (lastTimeStampEvent > fromTimestamp) {

                String fileName = Exchange.KPP + "/bankTransactions_" + lastTimeStampEvent;
                File jsonFile = new File(fileName + ".json");

                BankTransactionEventsExport bankTransactionEventsExport = new BankTransactionEventsExport(getLocalDaoSession(), getNsiDaoSession(), jsonFile);
                bankTransactionEventsExport.export(new Date(fromTimestamp));

                File zip = new File(fileName + "_" + requestTimeStamp);
                ZipUtils.zip(new String[]{jsonFile.getAbsolutePath()}, zip.getAbsolutePath());
                jsonFile.delete();

                rPacket.respFile = zip;
            } else {
                // http://agile.srvdev.ru/browse/CPPKPP-33106
                // Сообщаем кассе, что новых событий нет
                lastTimeStampEvent = 0;
            }
        } catch (FileNotFoundException e) {
            rPacket.error = new Error(Error.FileNotFoundException, e.getMessage());
            Logger.error(TAG, "Ошибка при попытке создать файл Событий BankTransactions - " + e.getMessage());
            Logger.error(TAG, e);
        } catch (IOException e) {
            rPacket.error = new Error(Error.IOException, e.getMessage());
            Logger.error(TAG, "Ошибка при попытке создать файл Событий BankTransactions - " + e.getMessage());
            Logger.error(TAG, e);
        } catch (Exception e) {
            rPacket.error = new Error(Error.UNKNOWN, e.getMessage());
            Logger.error(TAG, e);
        }

        Logger.info(TAG, "createBankTransactionsResp FINISH isOk: " + !rPacket.error.isError());

        return lastTimeStampEvent;
    }

    public void createRdsDbUpdateResp(File zipFile, Error error, long requestTimeStamp) {

        Logger.info(TAG, "createRdsDbUpdateResp START error.code=" + error.code);

        //почистим, чтобы хватило места для больших файлов
        fileCleaner.clearDir(filePathProvider.getBackupsDir(), SharedPreferencesUtils.getMaxFileCountInBackupDir(g));

        // копирует приходящюю с арм загрузки НСИ в файлик
        if (DEBUG) {
            try {
                FileUtils2.copyFile(zipFile, new File(zipFile.getAbsolutePath() + "_" + requestTimeStamp + "_back.gz"), g);
            } catch (IOException e) {
                Logger.error(TAG, e);
            }
        }

        if (!error.isError()) {
            error = checkShiftState();
            if (!error.isError()) {
                File unzippedFile = new File(zipFile.getAbsolutePath() + "_unsipped");
                boolean unzipResult = ZipUtils.unpackGZip(zipFile.getAbsolutePath(), unzippedFile.getAbsolutePath());

                if (unzipResult) {
                    Di.INSTANCE.nsiDbManager().closeConnection();
                    try {
                        if (!new DbFileUpdater().updateFromFile(unzippedFile, g.getDatabasePath(NsiDbManager.DB_NAME), false)) {
                            error.code = Error.UNKNOWN;
                        }
                    } catch (IOException e) {
                        Logger.error(TAG, e);
                        error.code = Error.UNKNOWN;
                    }
                    Di.INSTANCE.nsiDbManager().resetDaoSession();
                    FileUtils2.deleteFile(unzippedFile, g);
                } else {
                    error = new Error(Error.ZIP, "unzip error");
                }
            }
        }

        zipFile.delete();

        if (!error.isError()) {
            Dagger.appComponent().updateEventCreator().setType(UpdateEventType.NSI).create();
        }

        createErrorFile(RDS_RdsResp, RDS_RdsRespSig, error, requestTimeStamp);

        g.getBroadcasts().newArmSyncEvent(g, ArmSyncEvents.RdsDbUpdateReady, null, error);

        Logger.info(TAG, "createRdsDbUpdateResp FINISH error.code=" + error.code);
    }

    public void createSecurityDbUpdateResp(File zipFile, Error error, long requestTimeStamp) {

        Logger.info(TAG, "createSecurityDbUpdateResp START error.code=" + error.code);

        //почистим, чтобы хватило места для больших файлов
        fileCleaner.clearDir(filePathProvider.getBackupsDir(), SharedPreferencesUtils.getMaxFileCountInBackupDir(g));

        //почистим, чтобы хватило места для больших файлов
        fileCleaner.clearDir(filePathProvider.getBackupsDir(), SharedPreferencesUtils.getMaxFileCountInBackupDir(g));

        if (DEBUG) {
            try {
                FileUtils2.copyFile(zipFile, new File(zipFile.getAbsolutePath() + "_" + requestTimeStamp + "_back.gz"), g);
            } catch (IOException e) {
                Logger.error(TAG, e);
            }
        }

        if (!error.isError()) {
            error = checkShiftState();
            if (!error.isError()) {
                File unzippedFile = new File(zipFile.getAbsolutePath() + "_unsipped");
                boolean unzipResult = ZipUtils.unpackGZip(zipFile.getAbsolutePath(), unzippedFile.getAbsolutePath());

                if (unzipResult) {
                    Di.INSTANCE.securityDbManager().closeConnection();
                    try {
                        if (!new DbFileUpdater().updateFromFile(unzippedFile, g.getDatabasePath(SecurityDbManager.DB_NAME), false)) {
                            error.code = Error.UNKNOWN;
                        }
                    } catch (IOException e) {
                        Logger.error(TAG, e);
                        error.code = Error.UNKNOWN;
                    }
                    Di.INSTANCE.securityDbManager().resetDaoSession();
                    FileUtils2.deleteFile(unzippedFile, g);
                } else {
                    error = new Error(Error.ZIP, "unzip error");
                }
            }
        }

        FileUtils2.deleteFile(zipFile, g);

        if (!error.isError()) {
            Dagger.appComponent().updateEventCreator().setType(UpdateEventType.STOP_LISTS).create();
            Dagger.appComponent().updateEventCreator().setType(UpdateEventType.SECURITY).create();
        }

        createErrorFile(SECURITY_SecurityResp, SECURITY_SecurityRespSig, error, requestTimeStamp);

        g.getBroadcasts().newArmSyncEvent(g, ArmSyncEvents.SecurityDbUpdateReady, null, error);

        Logger.info(TAG, "createSecurityDbUpdateResp FINISH error.code=" + error.code);
    }

    /**
     * Создает файлик ответа при успешном обновлении ПО. Обновлялось ПО или нет - записано в соответствующем флаге в SharedPreferences
     */
    public void createUpdateApkResponse() {
        Logger.info(TAG, "createUpdateApkResponse START");
        if (SharedPreferencesUtils.isSoftwareUpdatedFlag(g.getApplicationContext())) {
            Logger.info(TAG, "createSigFile. запускаем создание файла респонса об обновлении ПО...");
            UpdateEvent lastUpdateEvent = Dagger.appComponent().updateEventRepository().getLastUpdateEvent(UpdateEventType.SW, false);
            String lastVersion = lastUpdateEvent == null ? "" : lastUpdateEvent.getVersion();
            FileUtils.clearFolderMtp(g, Exchange.SOFTWARE);
            Error error = new Error(Error.NONE, "update ok!");
            long requestTimeStamp = SharedPreferencesUtils.getSoftwareUpdateRequestTimestamp(g.getApplicationContext());
            createErrorFile(SOFTWARE_newVersionResp, SOFTWARE_newVersionRespSig, error, requestTimeStamp);
            g.getBroadcasts().newArmSyncEvent(g, ArmSyncEvents.PoUpdateReady, lastVersion, error);
            SharedPreferencesUtils.setSoftwareUpdatedFlag(g.getApplicationContext(), false);
        } else {
            Logger.info(TAG, "createUpdateApkResponse обновления ПО ПТК не было, создание файла ответа не требуется!");
        }
        Logger.info(TAG, "createUpdateApkResponse FINISH");
    }

    /**
     * Формирует отчет о последней смене на ПТК
     */
    public void createGetLastShiftResp(Error error, long requestTimeStamp) {
        Logger.info(TAG, "createGetLastShiftResp() START error.code=" + error.code);
        if (!error.isError()) {
            // Получаем текущее состояние смены, если закрыта - разрешаем
            // синхронизацию, если открыта то нет
            error = checkShiftState();
        }

        //запретим выгрузку если базы неактуальны
        if (!error.isError()) {
            error = checkDbDataContractVersions();
        }

        if (!error.isError()) {

            ShiftEventDao shiftDao = getLocalDaoSession().getShiftEventDao();
            ShiftEvent lastEvent = shiftDao.getLastShiftEvent(ShiftEvent.ShiftProgressStatus.FINISHED_STATUSES);


            if (lastEvent == null) {
                error = new Error(Error.NoShifts, "Смен на ПТК еще не было");
            } else {

                PtkShiftSummaryBuilder PtkShiftSummaryBuilder = new PtkShiftSummaryBuilder(getLocalDaoSession(),
                        Dagger.appComponent().nsiVersionManager(),
                        Dagger.appComponent().fineRepository(),
                        Dagger.appComponent().privateSettingsHolder());

                JSONObject root = null;

                try {
                    PtkShiftSummary ptkShiftSummary = PtkShiftSummaryBuilder.build(lastEvent);
                    root = new PtkShiftSummaryWriter().getJson(ptkShiftSummary);
                } catch (Exception e) {
                    error = new Exchange.Error(Error.UNKNOWN, e.getMessage());
                    Logger.error(TAG, "createGetLastShiftResp() Ошибка создания файла " + STATE_getLastShiftResp + ": " + e.getMessage());
                    Logger.error(TAG, e);
                }

                if (!error.isError()) {

                    File f = ExportUtils.getTempFile(STATE_getLastShiftResp, requestTimeStamp);
                    f.getParentFile().mkdirs();

                    try {
                        FileOutputStream fos = new FileOutputStream(f, true);
                        PrintStream ps = new PrintStream(fos);

                        ps.append(root.toString());
                        Logger.trace(TAG, "createGetLastShiftResp() json: " + root);

                        ps.flush();
                        fos.flush();

                        ps.close();
                        fos.close();

                        createSigFile(f, STATE_getLastShiftRespSig, STATE_getLastShiftResp, requestTimeStamp);

                    } catch (Exception e) {
                        Logger.error(TAG, "Ошибка при создании файла ответа на запрос состояния последней смены - " + e.getMessage());
                        Logger.error(TAG, e);
                        error = new Error(Error.UNKNOWN, e.getMessage());
                    }
                }
            }
        }

        if (error.isError())
            createErrorFile(STATE_getLastShiftResp, STATE_getLastShiftRespSig, error, requestTimeStamp);

        g.getBroadcasts().newArmSyncEvent(g, ArmSyncEvents.GetLastShiftRespCreated, null, error);

        Logger.info(TAG, "createGetLastShiftResp FINISH error.code=" + error.code);
    }

    public void createGetSettingsResp(Error error, long requestTimeStamp) {
        Logger.info(TAG, "createGetSettingsResp START error.code=" + error.code);
        if (!error.isError()) {
            // Получаем текущее состояние смены, если закрыта - разрешаем синхронизацию, если открыта то нет
            error = checkShiftState();
        }

        if (!error.isError()) {

            PrivateSettings ptkSettings = g.getPrivateSettingsHolder().get();

            JSONObject root = new JSONObject();
            File f = ExportUtils.getTempFile(STATE_getSettingsResp, requestTimeStamp);
            f.getParentFile().mkdirs();

            try {
                FileOutputStream fos = new FileOutputStream(f, true);
                PrintStream ps = new PrintStream(fos);

                JSONObject defJson = PrivateSettingsUtils.getInstance().getJSON(ptkSettings);

                root.put("Settings", defJson);

                Logger.trace(TAG, root.toString());

                ps.append(root.toString());

                ps.flush();
                fos.flush();

                ps.close();
                fos.close();

                createSigFile(f, STATE_getSettingsRespSig, STATE_getSettingsResp, requestTimeStamp);

            } catch (JSONException e) {
                error = new Error(Error.JSONException, e.getMessage());
                Logger.error(TAG, "Ошибка при создании файла ответа на запрос частных настроек ПТК - " + e.getMessage());
                Logger.error(TAG, e);
            } catch (IOException e) {
                error = new Error(Error.IOException, e.getMessage());
                Logger.error(TAG, "Ошибка при создании файла ответа на запрос частных настроек ПТК - " + e.getMessage());
                Logger.error(TAG, e);
            }
        }

        if (error.isError())
            createErrorFile(STATE_getSettingsResp, STATE_getSettingsRespSig, error, requestTimeStamp);

        g.getBroadcasts().newArmSyncEvent(g, ArmSyncEvents.GetPrivateSettingsRespCreated, null, error);
        Logger.info(TAG, "createGetSettingsResp FINISH error.code=" + error.code);
    }

    public void createGetTimeResp(Error error, long requestTimeStamp) {
        Logger.info(TAG, "createGetTimeResp START error.code=" + error.code);
        createCurrentTimeResp(error, requestTimeStamp);
    }

    @SuppressLint("SimpleDateFormat")
    private void createCurrentTimeResp(Error error, long requestTimeStamp) {
        // {"currentTime":"2014-10-09 12:16:13"}
        Logger.info(TAG, "createCurrentTimeResp START error.code=" + error.code);
        if (!error.isError()) {
            // Получаем текущее состояние смены, если закрыта - разрешаем
            // синхронизацию, если открыта то нет
            error = checkShiftState();
        }

        if (!error.isError()) {
            JSONObject root = new JSONObject();
            File f = ExportUtils.getTempFile(STATE_getTimeResp, requestTimeStamp);
            f.getParentFile().mkdirs();
            f.delete();
            String cTime = null;
            try {
                FileOutputStream fos = new FileOutputStream(f, true);
                PrintStream ps = new PrintStream(fos);
                cTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(System.currentTimeMillis());
                root.put("currentTime", cTime);
                ps.append(root.toString());

                ps.flush();
                fos.flush();

                ps.close();
                fos.close();
                createSigFile(f, STATE_getTimeRespSig, STATE_getTimeResp, requestTimeStamp);

            } catch (IOException e) {
                error = new Error(Error.IOException, e.getMessage());
                Logger.error(TAG, "Ошибка при создании файла ответа на запрос текущего времени ПТК - " + e.getMessage());
                Logger.error(TAG, e);
            } catch (JSONException e) {
                error = new Error(Error.JSONException, e.getMessage());
                Logger.error(TAG, "Ошибка при создании файла ответа на запрос текущего времени ПТК - " + e.getMessage());
                Logger.error(TAG, e);
            }

            Logger.info(TAG, "createCurrentTimeResp " + STATE_getTimeResp + " currentTime: " + cTime);
        }

        if (error.isError())
            createErrorFile(STATE_getTimeResp, STATE_getTimeRespSig, error, requestTimeStamp);

        g.getBroadcasts().newArmSyncEvent(g, ArmSyncEvents.GetCurrentTimeRespCreated, null, error);
        Logger.info(TAG, "createCurrentTimeResp FINISH error.code=" + error.code);
    }

    /**
     * В протоколе этого ответа вообще нет, но в случае ошибки все-равно будем
     * выдавать Перед вызовом этого метода следует вызвать функцию сохранения
     * настроек
     *
     * @param settings
     * @param error
     * @return
     */
    public void createSetSettingsResp(PrivateSettings settings, Error error, long requestTimeStamp) {
        Logger.info(TAG, "createSetSettingsResp START error.code=" + error.code);
        if (!error.isError())
            error = checkShiftState();

        if (!error.isError()) {
            Dagger.appComponent().privateSettingsRepository().savePrivateSettings(settings);
            // С кассы прилетает ограниченный набор параметров, достанем всё из БД с учётом обновлений
            Di.INSTANCE.getPrivateSettings().set(Dagger.appComponent().privateSettingsRepository().getPrivateSettings());
            Di.INSTANCE.getDeviceSessionInfo().setCurrentStationDevice(StationDevice.getThisDevice());

            boolean isTimeSyncEnabled = settings.isTimeSyncEnabled();

            int flag = isTimeSyncEnabled ? 1 : 0;
            Settings.Global.putInt(g.getContentResolver(), Settings.Global.AUTO_TIME, flag);
            Settings.Global.putInt(g.getContentResolver(), Settings.Global.AUTO_TIME_ZONE, flag);
        }

        createErrorFile(STATE_setSettingsResp, STATE_setSettingsRespSig, error, requestTimeStamp);
        g.getBroadcasts().newArmSyncEvent(g, ArmSyncEvents.SetPrivateSettingsRespCreated, null, error);
        Logger.info(TAG, "createSetSettingsResp FINISH error.code=" + error.code);
    }

    /**
     * Выставляет текущее время, добавляя задержку с момента детектирования
     * сообщения до момента выставления.
     *
     * @param timeInMilliseconds
     * @param error
     * @return
     */
    public void createSetTimeResp(long timeInMilliseconds, long eventTimeMilliseconds, Error error, long requestTimeStamp) {
        Logger.info(TAG, "createSetTimeResp(timeInMilliseconds=" + timeInMilliseconds + ", eventTimeMilliseconds=" + eventTimeMilliseconds + ", requestTimeStamp=" + requestTimeStamp + ") START error.code=" + error.code);
        boolean isDataCorrect = !error.isError() && timeInMilliseconds > 0 && eventTimeMilliseconds > 0;

        if (!isDataCorrect)
            error = new Error(Error.SetTimeError, "Ошибка синхронизации времени. Некорректное значение времени!");

        if (!error.isError()) {
            error = checkShiftState();
        }

        if (!error.isError()) {
            // проверяем флаг разрешения синхронизации времени
            boolean isTimeSyncEnabled = Di.INSTANCE.getPrivateSettings().get().isTimeSyncEnabled();
            if (isTimeSyncEnabled) {

                // Вычисляем смещение необходимое для установки времени
                long timeForAdd = timeInMilliseconds - eventTimeMilliseconds;

                long newTime = System.currentTimeMillis() + timeForAdd;

                // допустимое смещение в миллисекундах (5 минут)
                long alowedTimeMilliseconds = TimeUnit.MINUTES.toMillis(Dagger.appComponent().commonSettingsStorage().get().getTimeChangesPeriod());

                // проверяем флаг разрешения синхронизации времени более 5 минут
                boolean isAutoTimeSyncEnabled = Di.INSTANCE.getPrivateSettings().get().isAutoTimeSyncEnabled();
                // попытка установить смещение времени более 5 минут
                if (Math.abs(timeForAdd) > alowedTimeMilliseconds && !isAutoTimeSyncEnabled) {
                    error = new Error(Error.SetTimeError,
                            "Попытка установки времени превышающего допустимое смещение при запрете в частных настройках ПТК");
                } else {
//                    CashRegisterWorkingShift lastShiftInfo = getLocalDaoSession()
//                            .getCashRegisterWorkingShiftDao().getLastShiftEvent();
//
//                    // если последней смены нет, или время закрытия последней
//                    // смены (- 1 час) меньше времени предлагаемого кассой, то
//                    // можно менять.
//                    if (lastShiftInfo == null
//                            || (lastShiftInfo.getCloseTime().getTime() - Printer.OPEN_SHIFT_MAX_ENABLED_TIME) < (System.currentTimeMillis() + timeForAdd)) {
//                        SystemClock.setCurrentTimeMillis(System.currentTimeMillis() + timeForAdd);
//                    } else {
//                        error = new Error(Error.SetTimeError, "Ошибка синхронизации времени. Раница между временем закрытия последней смены ("
//                                + DateFormatOperations.getUtcString(lastShiftInfo.getCloseTime()) + ") и временем предагаемым кассой ("
//                                + DateFormatOperations.getUtcString(System.currentTimeMillis() + timeForAdd) + ") - больше 1 часа!");
//                    }
                    ///////////////////////////////////////////////////
                    ShiftEvent lastShift = getLocalDaoSession().getShiftEventDao().getLastShiftEvent(ShiftEvent.ShiftProgressStatus.FINISHED_STATUSES);
                    Check lastCheck = getLocalDaoSession().getCheckDao().getLastCheckForPeriod(null, null);

                    long lastCheckTime = (lastCheck != null ? lastCheck.getPrintDatetime().getTime() : 0);
                    long lastShiftOpenTime = (lastShift != null ? lastShift.getStartTime().getTime() : 0);
                    long lastShiftCloseTime = (lastShift != null && lastShift.getCloseTime() != null ? lastShift.getCloseTime().getTime() : 0);
                    long lastEvent = getLocalDaoSession().getEventDao().getLastEventTimeStamp();

                    StringBuilder errMsgBuilder = new StringBuilder();
                    errMsgBuilder.append("Ошибка синхронизации. ");
                    if (lastCheckTime >= newTime) {
                        errMsgBuilder.append("Время печати последнего чека ");
                        errMsgBuilder.append(DateFormatOperations.getUtcString(new Date(lastCheckTime)));
                    } else if (lastShiftOpenTime >= newTime) {
                        errMsgBuilder.append("Время открытия поледней смены ");
                        errMsgBuilder.append(DateFormatOperations.getUtcString(new Date(lastShiftOpenTime)));
                    } else if (lastShiftCloseTime >= newTime) {
                        errMsgBuilder.append("Время закрытия поледней смены ");
                        errMsgBuilder.append(DateFormatOperations.getUtcString(new Date(lastShiftCloseTime)));
                    } else if (lastEvent >= newTime) {
                        errMsgBuilder.append("Время последнего события ");
                        errMsgBuilder.append(DateFormatOperations.getUtcString(new Date(lastEvent)));
                    } else {
                        errMsgBuilder = null;
                        SystemClock.setCurrentTimeMillis(newTime);
                    }

                    if (errMsgBuilder != null) {
                        errMsgBuilder.append(" больше, чем устанавливаемое время ");
                        errMsgBuilder.append(DateFormatOperations.getUtcString(new Date(newTime)));
                        error = new Error(Error.SetTimeError, errMsgBuilder.toString());
                    }
                }
            } else { // стоит запрет на обновление времени
                error = new Error(Error.SetTimeError, "Ошибка синхронизации времени. В частных настройках ПТК запрещена синхронизация времени");
            }
        }

        //если ошибок небыло вернем текущее время
        if (!error.isError()) {
            JSONObject root = new JSONObject();
            File f = ExportUtils.getTempFile(STATE_setTimeResp, requestTimeStamp);
            f.getParentFile().mkdirs();
            f.delete();
            String cTime = null;
            try {
                FileOutputStream fos = new FileOutputStream(f, true);
                PrintStream ps = new PrintStream(fos);
                cTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(System.currentTimeMillis());
                root.put("currentTime", cTime);
                ps.append(root.toString());

                ps.flush();
                fos.flush();

                ps.close();
                fos.close();
                createSigFile(f, STATE_setTimeRespSig, STATE_setTimeResp, requestTimeStamp);

            } catch (IOException e) {
                error = new Error(Error.IOException, e.getMessage());
                Logger.error(TAG, "Ошибка при создании файла ответа на установку текущего времени ПТК - " + e.getMessage());
                Logger.error(TAG, e);
            } catch (JSONException e) {
                error = new Error(Error.JSONException, e.getMessage());
                Logger.error(TAG, "Ошибка при создании файла ответа на установку текущего времени ПТК - " + e.getMessage());
                Logger.error(TAG, e);
            }

            Logger.info(TAG, "createSetTimeResp " + STATE_getTimeResp + " currentTime: " + cTime);
        }

        if (error.isError())
            createErrorFile(STATE_setTimeResp, STATE_setTimeRespSig, error, requestTimeStamp);

        g.getBroadcasts().newArmSyncEvent(g, ArmSyncEvents.SetTimeRespCreated, null, error);
        Logger.info(TAG, "createSetTimeResp FINISH timeInMilliseconds:" + timeInMilliseconds + " error.code=" + error.code);
    }

    /**
     * создает файл с ошибкой, и подписывает его
     *
     * @param respPath
     * @param respSigFilePath
     * @param error
     * @return
     */
    public void createErrorFile(String respPath, String respSigFilePath, Error error, long requestTimeStamp) {
        Logger.info(TAG, "createErrorFile START error.code=" + error.code);
        File f = createErrorTempFile(respPath, error, requestTimeStamp);
        createSigFile(f, respSigFilePath, respPath, requestTimeStamp);
        Logger.info(TAG, "createErrorFile FINISH " + respPath);
    }

    /**
     * Создает временный файл ответа.
     *
     * @param realFilePath
     * @param error
     * @return
     */
    private File createErrorTempFile(String realFilePath, Error error, long requestTimeStamp) {
        Logger.info(TAG, "createErrorTempFile START error.code=" + error.code + " " + realFilePath);
        JSONObject root = new JSONObject();
        File f = ExportUtils.getTempFile(realFilePath, requestTimeStamp);
        f.getParentFile().mkdirs();
        boolean isOK = false;
        try {
            FileOutputStream fos = new FileOutputStream(f, true);
            PrintStream ps = new PrintStream(fos);

            root.put("errorCode", error.code);
            root.put("errorMessage", error.getMessage());
            root.put("DeviceId", Di.INSTANCE.getPrivateSettings().get().getTerminalNumber());

            ps.append(root.toString());

            ps.flush();
            fos.flush();

            ps.close();
            fos.close();
            isOK = true;
        } catch (IOException e) {
            Logger.error(TAG, "Ошибка при создании файла " + f.getName() + " - " + e.getMessage());
            Logger.error(TAG, e);
        } catch (JSONException e) {
            Logger.error(TAG, "Ошибка при создании файла " + f.getName() + " - " + e.getMessage());
            Logger.error(TAG, e);
        }
        if (isOK)
            addFileContentToLog(f.getAbsolutePath());

        Logger.info(TAG, "createErrorTempFile FINISH error.code=" + error.code + " " + f.getName());
        return f;
    }

    /**
     * Запускает intent на установку новой версии ПО
     *
     * @param apkFile
     */
    private boolean startInstallApkIntent(File apkFile, long requestTimeStamp) {
        if (apkFile != null && apkFile.isFile() && apkFile.exists()) {
            SharedPreferencesUtils.setSoftwareUpdateRequestTimestamp(g, requestTimeStamp);
            Logger.info(TAG, "startInstallApkIntent START file: " + apkFile.getName());
            Intent installIntent = new Intent(Intent.ACTION_VIEW);
            installIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            installIntent.setDataAndType(Uri.parse("file://" + apkFile.toString()), "application/vnd.android.package-archive");
            g.startActivity(installIntent);
            return true;
        }
        return false;
    }

    public void createPullSftResp(long requestTimeStamp) {
        Logger.info(TAG, "createPullSftResp START - запускаем функцию pull sft...");
        g.getBroadcasts().newArmSyncEvent(g, ArmSyncEvents.PullSftStart, null, new Error());

        boolean isPulledOk = false;
        try {
            isPulledOk = Di.INSTANCE.getEdsManagerWrapper().pullEdsCheckerBlocking();
        } catch (Exception e) {
            Logger.error(TAG, "Error getting ecp state state", e);
        }

        addLog("createPullSftResp функция pull вернула результат: isPulledOk: " + isPulledOk);
        Error error = new Error((isPulledOk) ? Error.NONE : Error.PullSftError, "");
        addLog("createPullSftResp запускаем создание файла " + SFT_transmissioncompleted_resp);
        File f = createErrorTempFile(SFT_transmissioncompleted_resp, error, requestTimeStamp);
        convertTempBinFileToResponse(f, SFT_transmissioncompleted_resp, requestTimeStamp, RESPONSE_COUNT);
        addLog("createPullSftResp onEcpPulled FINISH isPulledOk: " + isPulledOk);
        g.getBroadcasts().newArmSyncEvent(g, ArmSyncEvents.PullSftRespCreated, null, error);

    }

    /**
     * Распаковывает пакет с ключами и создает ответ
     */
    public void createTransmissionCompleteResp1(File tarGzip, File deleteList, long requestTimeStamp) {

        Logger.info(TAG, "createTransmissionCompleteResp START");

        try {
            Di.INSTANCE.getEdsManagerWrapper().closeBlocking();
            Logger.info(TAG, "createTransmissionCompleteResp функция closeProcessorJava вернула результат: isCloseProcessorOk: " + true);
        } catch (Exception e) {
            Logger.error(TAG, "Ecp closed fail", e);
        }

        Error unzipError = new Error();
        Error deleteFilesError = new Error();

        // распаковка с помощью tar.gz
        if (tarGzip.exists()) {
            Logger.info(TAG, "Начинаем распаковывать архив с ключами");
            Packer packer = new Packer(g.getApplicationContext());
            if (!packer.unpack(tarGzip, Di.INSTANCE.getEdsManager().getEdsDirs().getEdsTransportInDir().getAbsolutePath()))
                unzipError = new Error(Error.ZIP, "Ошибка распаковки tar.gz архива");
        }

        g.getBroadcasts().newArmSyncEvent(g, ArmSyncEvents.PublicKeyUnzipReady, null, unzipError);

        if (unzipError.isError())
            Logger.info(TAG, "createTransmissionCompleteResp ошибка распаковки:" + unzipError.getMessage());

        if (deleteList.exists()) {
            Logger.info(TAG, "createTransmissionCompleteResp Зафиксирован запрос на удаление устаревших файлов SFT");

            g.getBroadcasts().newArmSyncEvent(g, ArmSyncEvents.PublicKeyDeleteReqDetected, null, new Error());

            Gson gson = new Gson();
            try {
                String json = Exchange.loadJSONFromFile(deleteList);
                Logger.info(TAG, "createTransmissionCompleteResp json: " + json);
                String[] out = gson.fromJson(json, String[].class);
                // в цикле удаляем все ненужные файлы из папки in
                for (String item : out) {
                    boolean res = new File(Di.INSTANCE.getEdsManager().getEdsDirs().getEdsTransportInDir().getAbsolutePath() + "/" + item).delete();

                    g.getBroadcasts().newArmSyncEvent(g, ArmSyncEvents.OldSftFileDeleteReady, item + " - " + ((res) ? "ОК" : "Ошибка"), new Error());

                    Logger.info(TAG, "createTransmissionCompleteResp удаление " + item + " - " + ((res) ? "ОК" : "Ошибка"));
                }
            } catch (IOException e) {
                deleteFilesError = new Error(Error.IOException, "\nОшибка чтения из файла sftfilestodelete.bin (" + e.getMessage() + ")");
                Logger.error(TAG, "createTransmissionCompleteResp Возникла ошибка при удалении файлов: " + e.getMessage());
                Logger.error(TAG, e);
            }
        } else {
            Logger.info(TAG, "createTransmissionCompleteResp Запрос на удаление устаревших файлов SFT отсутствует!");
        }

        g.getBroadcasts().newArmSyncEvent(g, ArmSyncEvents.PublicKeyDeleteReady, null, deleteFilesError);

        final File folderWithLic = new File(Exchange.SFT_LIC);
        final String[] licFiles = folderWithLic.list();
        if (licFiles != null && licFiles.length > 0) {
            g.getBroadcasts().newArmSyncEvent(g, ArmSyncEvents.TakeLicsStart, null, new Error());
            Logger.info(TAG, "Обнаружены новые лицензии. Количество новых лицензий - " + licFiles.length);
            for (String licFile : licFiles) {
                Logger.trace(TAG, "Новый файл лицензии - " + licFile);
            }
            Logger.info(TAG, "Подхватываем новые лицензии из папки lic");
            final boolean takeResult = Di.INSTANCE.getEdsManagerWrapper().takeLicensesBlocking(folderWithLic);
            Logger.trace(TAG, "функция takeLic завершилась с результатом " + takeResult);

            g.getBroadcasts().newArmSyncEvent(g, ArmSyncEvents.TransportLicFolderClearStart, null, new Error());
            Logger.info(TAG, "createTransmissionCompleteResp очищаем папку " + Exchange.SFT_LIC);
            FileUtils.clearFolderMtp(g, Exchange.SFT_LIC);
        } else {
            Logger.info(TAG, "Новых файлов лицензии не обнаружено");
        }

        if (unzipError.isError() || deleteFilesError.isError()) {
            Error error = new Error(Error.UNKNOWN, "unpack result: " + unzipError.getMessage()
                    + "    deletefiles result: " + deleteFilesError.getMessage());
            File f = createErrorTempFile(SFT_transmissioncompleted_resp, error, requestTimeStamp);
            FileUtils.renameFile(g, f, new File(SFT_transmissioncompleted_resp));
            g.getBroadcasts().newArmSyncEvent(g, ArmSyncEvents.PullSftRespCreated, null, error);
        } else {
            createPullSftResp(requestTimeStamp);
        }

        Logger.info(TAG, "createTransmissionCompleteResp FINISH");
    }

    /**
     * Обработка события подключения ПТК к АРМ загрузки данных
     *
     * @param oldRequest          - флаг говорит о том что касса старая и информация о кассе не пришла // В будущем выпилить в релизе 3 когда не останется касс с датаконтрактами ниже 79 http://agile.srvdev.ru/browse/CPPKPP-42823
     * @param isEcpOk             - результат проверки ЭЦП
     * @param error               - ошибка
     * @param ptkStartSyncRequest - моделька запроса
     * @param requestTimeStamp    - время запроса
     */
    public void armConnected(boolean oldRequest, boolean isEcpOk, Error error, PtkStartSyncRequest ptkStartSyncRequest, long requestTimeStamp) {
        Logger.info(TAG, "armConnected(oldRequest=" + oldRequest +
                ", isEcpOk=" + isEcpOk +
                ", error=" + error +
                ", ptkStartSyncRequest=" + ptkStartSyncRequest +
                ", requestTimeStamp=" + requestTimeStamp +
                ") START");
        isCancelled.set(false);
        // очищаем папку Cancel
        Logger.info(TAG, "armConnected. Cleaning #CPPKConnect/Cancel start");
        FileUtils.clearFolderMtp(g, Exchange.CANCEL);
        Logger.info(TAG, "armConnected. Cleaning #CPPKConnect/Cancel end");
        Logger.info(TAG, "armConnected. Cleaning CPPKInternal/Backup/Temp start");
        FileUtils2.clearDir(filePathProvider.getBackupsTempDir(), null);
        Logger.info(TAG, "armConnected. Cleaning CPPKInternal/Backup/Temp end");

        if (!oldRequest && !error.isError() && ptkStartSyncRequest == null) {
            error = new Error(Error.UNKNOWN, "ptkStartSyncRequest is null");
        }

        if (!oldRequest && !error.isError() && ptkStartSyncRequest.device == null) {
            error = new Error(Error.UNKNOWN, "ptkStartSyncRequest.device is null");
        }

        if (!error.isError() || oldRequest) {

            if (!oldRequest) {
                LogEvent logEventStandard = logEventBuilder
                        .setLogActionType(LogActionType.SYNCHRONISATION_WITH_ARM_START)
                        .setMessage("Запрос на начало синхронизации с АРМ, device: " + ptkStartSyncRequest.device.toString())
                        .build();
                getLocalDaoSession().logEventDao().insertOrThrow(logEventStandard);
            }

            // Получаем текущее состояние смены, если закрыта - разрешаем
            // синхронизацию, если открыта то нет
            error = checkShiftState();

            //при любом состоянии смены разрешаем переход в режим синхронизации, чтобы можно было обновить ключи
            SharedPreferencesUtils.setARMConnectedDateTime(g, System.currentTimeMillis());
            Globals.getInstance().getExchange().setExchangeState(Exchange.EXCHANGE_STATE_SYNCHRONIZATION);
            Navigator.navigateToArmConnectedStateActivity(g, false);

        }

        File f = createErrorTempFile(STATE_armConnectedResp, error, requestTimeStamp);
        convertTempBinFileToResponse(f, STATE_armConnectedResp, requestTimeStamp, RESPONSE_COUNT);
        // Это событие будем выводить вручную при старте активити
        // g.getBroadcasts().newArmSyncEvent(g, ArmSyncEvents.Connected, null, error);
        Logger.info(TAG, "armConnected FINISH error.code=" + error.code);
    }

    /**
     * Обработка события завершения синхронизации ПТК - АРМ загрузки данных
     */
    public void armDisconnected(long requestTimeStamp) {
        Logger.info(TAG, "armDisconnected START");
        g.getBroadcasts().newArmSyncEvent(g, ArmSyncEvents.Disconnected, null, new Error());
        // SharedPreferencesUtils.setARMConnectedDateTime(g,
        // System.currentTimeMillis());
        if (!isCancelled.get()) {
            Globals.getInstance().getExchange().setExchangeState(Exchange.EXCHANGE_STATE_WAIT_CONNECT);
            Navigator.navigateToArmConnectedStateActivity(g, true);
        } else
            Logger.info(TAG, "armDisconnected Синхронизация уже была прервана пользователем ПТК");
        Logger.info(TAG, "armDisconnected FINISH");
    }

    /**
     * Обработка события обновления общих настроек ПТК
     */
    public void createCommonSettingsResp(File xmlFile, Error error, long requestTimeStamp) {
        Logger.info(TAG, "createCommonSettingsResp START error.code=" + error.code);

        if (!error.isError()) {
            // Получаем текущее состояние смены, если закрыта - разрешаем
            // синхронизацию, если открыта то нет
            error = checkShiftState();
        }

        if (!error.isError()) {
            if (!Dagger.appComponent().commonSettingsTempStorage().save(xmlFile)) {
                error = new Error(Error.INCORRECT_COMMON_CONFIG, "Ошибка сохранения общих настроек во временное хранлище");
            }
        }
        FileUtils2.deleteFile(xmlFile, g);
        createErrorFile(SOFTWARE_newVersionSettingsResp, SOFTWARE_newVersionSettingRespSig, error, requestTimeStamp);
        g.getBroadcasts().newArmSyncEvent(g, ArmSyncEvents.SetCommonSettingsRespCreated, null, error);
        Logger.info(TAG, "createCommonSettingsResp FINISH error.code=" + error.code);
    }

    /**
     * Обработка события обновления ПО ПТК
     */
    public void createNewVersionResp(File apkFile, Error error, long requestTimeStamp) {
        Logger.info(TAG, "createNewVersionResp START error.code=" + error.code);
        if (error.isError()) {
            createErrorFile(SOFTWARE_newVersionResp, SOFTWARE_newVersionRespSig, error, requestTimeStamp);
        } else {
            // Получаем текущее состояние смены, если закрыта - разрешаем
            // синхронизацию, если открыта то нет
            error = checkShiftState();
            if (error.isError())
                createErrorFile(SOFTWARE_newVersionResp, SOFTWARE_newVersionRespSig, error, requestTimeStamp);
        }

        if (!error.isError()) {

            // создаем бекап для отладки
            if (DEBUG)
                FileUtils.copy(g, apkFile, new File(apkFile.getAbsolutePath() + "_back_" + requestTimeStamp + ".apk"));

            if (error.code == Error.NONE) {

                // очищаем папку new
                FileUtils.clearFolderMtp(g, Exchange.SOFTWARE_NEW);

                boolean result = false;
                // запуск ручного обновления, нужно будет подтвердить на ПТК
                if (apkFile != null && apkFile.exists()) {

                    Logger.info(TAG, "Перемещаем файл в папку без # чтобы его можно было запустить на установку...");
                    File apkNewFile = new File(PathsConstants.TEMP + "/app.apk");
                    FileUtils.renameFile(Globals.getInstance(), apkFile, apkNewFile);

                    error = new Error(Error.NONE, g.getResources().getString(R.string.armDetectApkUserArmMessage));
                    createErrorFile(SOFTWARE_newApkFoundResp, SOFTWARE_newApkFoundRespSig, error, requestTimeStamp);
                    result = startInstallApkIntent(apkNewFile, requestTimeStamp);
                }
                if (!result) {
                    error = new Error(Error.NewApkFileNotFound, "не удалось обнаружить файл с новой версией ПО");
                    createErrorFile(SOFTWARE_newVersionResp, SOFTWARE_newVersionRespSig, error, requestTimeStamp);
                }
            }

            if (error.isError())
                g.getBroadcasts().newArmSyncEvent(g, ArmSyncEvents.UpdatePoReqDetected, null, error);
        }
        Logger.info(TAG, "createNewVersionResp FINISH error.code=" + error.code);
    }

    public static class ResponsePacket {
        File respFile = null;
        Error error = new Error();
    }

    /**
     * событие успешного завершения синхронизации, добавление в лог, создание
     * ответа для кассы
     */
    public void createOnSyncFinishedResp(long requestTimeStamp) {

        Logger.info(TAG, "createOnSyncFinishedResp START");

        // Получаем текущее состояние смены, если закрыта - разрешаем
        // синхронизацию, если открыта то нет
        Error error = checkShiftState();
        if (error.isError())
            createErrorFile(STATE_syncFinishedResp, STATE_syncFinishedRespSig, error, requestTimeStamp);

        if (!error.isError()) {
            Dagger.appComponent().updateEventCreator().setType(UpdateEventType.ALL).create();
            createErrorFile(STATE_syncFinishedResp, STATE_syncFinishedRespSig, new Error(), requestTimeStamp);
        }
        Logger.info(TAG, "createOnSyncFinishedResp FINISH error.code=" + error.code);
    }

    /**
     * событие завершения процесса синхронизации иницированное ПТК
     */
    public void createSyncCancelledInfo() {
        Logger.info(TAG, "createSyncCancelledInfo START");
        isCancelled.set(true);
        createErrorFile(STATE_syncCancelledInfo, STATE_syncCancelledInfoSig, new Error(Error.NONE, "Пользователь завершил синхронизацию"), System.currentTimeMillis());
    }

    /**
     * Реализует проверку доступности функции синхронизации в разрезе состояния
     * смены
     */
    private boolean isShiftClosed() {
        return Globals.getInstance().getShiftManager().isShiftClosed();
    }

    /**
     * Выполняет проверку на закрытость смены и возвращает соответствующую
     * ошибку
     */
    private Error checkShiftState() {
        return (isShiftClosed()) ? new Error() : new Error(Error.ShiftError, "Синхронизация недоступна при открытой смене!");
    }

    /**
     * Выполняет проверку на соответствие версии датаконтрактов баз
     *
     * @return - соответствующая ошибка
     */
    private Error checkDbDataContractVersions() {
        Error error = new Error();

        if (!(Di.INSTANCE.nsiDataContractsVersionChecker().isDataContractVersionValid() && Dagger.appComponent().nsiVersionManager().checkCurrentVersionIdValid())) {
            error = new Error(Error.IncorrectNsiDataContractVersion, "Выгрузка событий недоступна, обновите НСИ!");
        } else if (!Di.INSTANCE.securityDataContractsVersionChecker().isDataContractVersionValid() && Dagger.appComponent().securityDaoSession().getSecurityDataVersionDao().getSecurityVersion() != null) {
            error = new Error(Error.IncorrectSecurityDataContractVersion, "Выгрузка событий недоступна, обновите базу безопасности!");
        }

        return error;
    }


    private UUID createGetBackupRespUUID = null;

    /**
     * Создает бекап ПТК и формирует ответ
     */
    public void createGetBackupResp(Error error, long requestTimeStamp) {

        Logger.info(TAG, "createGetBackupResp START error.code=" + error.code);

        createGetBackupRespUUID = TimeLogger.addStartEvent("стартуем процесс создания ответа на запрос файла бекапа ");

        long number = Di.INSTANCE.getPrivateSettings().get().getTerminalNumber();
        String datetime = DateFormatOperations.getUtcString(new Date()).replace(":", "-").replace("T", "_").replace("Z", "");
        String softwareVersion = Utils.getSoftwareVersion(g.getApplicationContext());
        final File backupFile = new File(Exchange.STATE + "/" + number + "_backup_" + softwareVersion + "_" + datetime);

        //не будем запрещать создание бекапа при открытой смене

        if (!error.isError()) {
            UUID startUUID = TimeLogger.addStartEvent("стартуем создание файла бекапа");
            Pair<Boolean, String> backupRes = Dagger.appComponent().syncBackupCreator().start();
            TimeLogger.addFinishEvent(startUUID, "создание файла бекапа завершено: " + (backupRes.first ? "успешно - " : "с ошибкой - ") + backupRes.second);

            File tempBackupFile = new File(backupRes.second);

            UUID moveFileUUID = TimeLogger.addStartEvent("стартуем перенос файла бекапа");

            if (!backupRes.first || !tempBackupFile.exists()) {
                error = new Error(Error.Backup, "Ошибка создания бекапа на ПТК");
            } else if (!tempBackupFile.renameTo(backupFile)) {
                error = new Error(Error.Backup, "Ошибка перемещения файла бекапа в транспортную папку на ПТК");
            }

            TimeLogger.addFinishEvent(moveFileUUID, "перенос файла бекапа завершено успешно: " + !error.isError());

            g.getBroadcasts().newArmSyncEvent(g, ArmSyncEvents.BackupCreated, null, error);

        }

        if (!error.isError()) {
            FileUtils.makeFileVisibleMtp(g, backupFile);

            final File backupSigFile = new File(backupFile.getAbsolutePath() + ".sig");
            final File backupZipFile = new File(backupFile.getAbsolutePath() + ".zip");

            final UUID createBackupSig = TimeLogger.addStartEvent("стартуем создание подписи к файлу бекапа " + ((double) (backupFile.length() / 1024 / 1024)) + " Mb");

            g.getBroadcasts().newArmSyncEvent(g, ArmSyncEvents.BackupSigCreateStart, null, error);
            boolean isOk = createSigFile(backupFile, backupSigFile.getAbsolutePath(), backupZipFile.getAbsolutePath(), requestTimeStamp);

            g.getBroadcasts().newArmSyncEvent(g, ArmSyncEvents.SigCreateReady, backupSigFile.getName(), (isOk) ? (new Error()) : (new Error(Error.ECP, "")));
            createBackupResponseReadyCreateBackupSig(backupZipFile, backupSigFile, isOk, requestTimeStamp);
            // если бекап успешно создан грохнем все файлы из папки с логами sft, принтера и из папки с крашами
            if (isOk) {
                g.getBroadcasts().newArmSyncEvent(g, ArmSyncEvents.ClearSftLogFolder, null, new Error());
                FileUtils.clearFolderMtp(g, Dagger.appComponent().filePathProvider().getInfotecsLogsDir().getAbsolutePath());
                g.getBroadcasts().newArmSyncEvent(g, ArmSyncEvents.ClearFatalsLogFolder, null, new Error());
                FileUtils.clearFolderMtp(g, PathsConstants.LOG_FATALS);
                g.getBroadcasts().newArmSyncEvent(g, ArmSyncEvents.ClearZebraLogFolder, null, new Error());
                FileUtils.clearFolderMtp(g, PathsConstants.LOG_ZEBRA);
            }
            TimeLogger.addFinishEvent(createBackupSig, "создание подписи к файлу бекапа завершено успешно: " + isOk);
        }

        if (error.isError())
            createErrorFile(STATE_getBackupResp, STATE_getBackupRespSig, error, requestTimeStamp);

        Logger.info(TAG, "createGetBackupResp FINISH error.code=" + error.code);
    }

    /**
     * Создает файл ответа на запрос создания бекапа, когда архив с бекапом и
     * его подпись уже лежат в State
     *
     * @param backupFile
     * @param backupSigFile
     * @param isOk
     */
    protected void createBackupResponseReadyCreateBackupSig(File backupFile, File backupSigFile, boolean isOk, long requestTimeStamp) {

        Logger.info(TAG, "createBackupResponseReadyCreateBackupSig START isOk=" + isOk);

        TimeLogger.addFinishEvent(createGetBackupRespUUID, "Создание и подпись файла бекапа завершено успешно: " + isOk);

        Error error = new Error();

        if (!isOk || backupFile == null || backupSigFile == null) {
            error = new Error(Error.Backup, "Ошибка создания подписи к файлу с архивом бекапа");
        }

        File backupFileWithTimeStamp = Exchange.Fn.getFileWithTimeStamp(backupFile, requestTimeStamp);
        File backupSigFileWithTimeStamp = Exchange.Fn.getFileWithTimeStamp(backupSigFile, requestTimeStamp);

        g.getBroadcasts().newArmSyncEvent(g, ArmSyncEvents.BackupRespCreateStart, null, error);

        if (!error.isError()) {

            File f = ExportUtils.getTempFile(STATE_getBackupResp, requestTimeStamp);

            // создаем временный файлик ответа getBackup_resp
            JSONObject root = new JSONObject();

            f.getParentFile().mkdirs();

            try {
                FileOutputStream fos = new FileOutputStream(f, true);
                PrintStream ps = new PrintStream(fos);

                root.put("FileNameBackUp", backupFileWithTimeStamp.getName());
                root.put("FileNameBackUpSig", backupSigFileWithTimeStamp.getName());

                ps.append(root.toString());

                ps.flush();
                fos.flush();

                ps.close();
                fos.close();

                addFileContentToLog(f.getAbsolutePath());

                Logger.info(TAG, "поставим задержку 3 секунды из опасения что файл с бекапом кассе станет виден позже файла с респонсом https://aj.srvdev.ru/browse/CPPKPP-26228");
                Thread.sleep(3000);

                Logger.info(TAG, "Создаем файлы ответа на запрос бекапа...");
                createSigFile(f, STATE_getBackupRespSig, STATE_getBackupResp, requestTimeStamp);

            } catch (FileNotFoundException e) {
                error = new Error(Error.FileNotFoundException, e.getMessage());
                Logger.error(TAG, "createBackupResponseReadyCreateBackupSig ошибка: " + e.getMessage());
                Logger.error(TAG, e);
            } catch (JSONException e) {
                error = new Error(Error.JSONException, e.getMessage());
                Logger.error(TAG, "createBackupResponseReadyCreateBackupSig ошибка: " + e.getMessage());
                Logger.error(TAG, e);
            } catch (IOException e) {
                error = new Error(Error.IOException, e.getMessage());
                Logger.error(TAG, "createBackupResponseReadyCreateBackupSig ошибка: " + e.getMessage());
                Logger.error(TAG, e);
            } catch (InterruptedException e) {
                error = new Error(Error.UNKNOWN, e.getMessage());
                Logger.error(TAG, "createBackupResponseReadyCreateBackupSig ошибка: " + e.getMessage());
                Logger.error(TAG, e);
            }
        }

        if (error.isError())
            createErrorFile(STATE_getBackupResp, STATE_getBackupRespSig, error, requestTimeStamp);

        Logger.info(TAG, "createBackupResponseReadyCreateBackupSig FINISH error.code=" + error.code);
    }

    /**
     * Создает файл sftfileslist.bin - json список файлов в папке IN
     */
    private void createSftFilesListFile(Globals g, long requestTimeStamp) {

        Logger.info(TAG, "createSftFilesListFile START");

        g.getBroadcasts().newArmSyncEvent(g, ArmSyncEvents.CreateSftFilesListFileStart, "", new ru.ppr.cppk.export.Exchange.Error());

        try {
            File exchangeSftOutDir = new File(Exchange.SFT_OUT);

            Logger.info(TAG, "createSftFilesListFile очищаем папку: " + exchangeSftOutDir.getAbsolutePath());
            FileUtils2.clearDir(exchangeSftOutDir, g);

            File edsTransportOutDir = new File(Di.INSTANCE.getEdsManager().getEdsDirs().getEdsTransportOutDir().getAbsolutePath());
            File edsUtilDstDir = new File(Di.INSTANCE.getEdsManager().getEdsDirs().getEdsUtilDstDir().getAbsolutePath());

            File[] filesInOutDir = edsTransportOutDir.listFiles(new TransportOutDirExportFileFilter());
            int filesInOutDirCount = filesInOutDir == null ? 0 : filesInOutDir.length;
            File[] filesInUtilDstDir = edsUtilDstDir.listFiles();
            int filesInUtilDstDirCount = filesInUtilDstDir == null ? 0 : filesInUtilDstDir.length;
            File[] filesForCopy = new File[filesInOutDirCount + filesInUtilDstDirCount];
            if (filesInOutDirCount > 0) {
                System.arraycopy(filesInOutDir, 0, filesForCopy, 0, filesInOutDirCount);
            }
            if (filesInUtilDstDirCount > 0) {
                System.arraycopy(filesInUtilDstDir, 0, filesForCopy, filesInOutDirCount, filesInUtilDstDirCount);
            }

            addLog("createSftFilesListFile начинаем копировать файлы из " + edsTransportOutDir.getAbsolutePath()
                    + " в " + Exchange.SFT_OUT + ", файлов для копирования - " + filesForCopy.length);
            for (File fileForCopy : filesForCopy) {

                if (Thread.currentThread().isInterrupted()) {
                    addLog("Операция копирования файлов прервана");
                    return;
                }

                File dst = new File(exchangeSftOutDir, fileForCopy.getName());
                addLog("Копируем из:" + fileForCopy.getAbsolutePath() + " в " + dst.getAbsolutePath());

                FileUtils2.copyFile(fileForCopy, dst, g);
            }
            addLog("createSftFilesListFile закончили копировать файлы, количество скопированный файлов -  " + filesForCopy.length);

            Logger.info(TAG, "createSftFilesListFile Запускаем обновление содержимого папки out для кассы");
            MtpUtils.refreshDir(g, exchangeSftOutDir);

        } catch (IOException e) {
            Logger.error(TAG, "createSftFilesListFile", e);
        }

        File jsonFile = ExportUtils.getTempFile(STATE_sftfileslist, requestTimeStamp);
        if (!jsonFile.getParentFile().exists() && !jsonFile.getParentFile().mkdirs()) {
            Logger.error(TAG, "Could not create dir for file: " + jsonFile.getAbsolutePath());
        }

        PrintStream ps = null;
        try {

            FileOutputStream fos = new FileOutputStream(jsonFile, true);
            ps = new PrintStream(fos);

            JSONArray root = new JSONArray();

            File edsTransportInDir = new File(Di.INSTANCE.getEdsManager().getEdsDirs().getEdsTransportInDir().getAbsolutePath());
            String[] filesInInDir = edsTransportInDir.list();
            if (filesInInDir != null) {
                for (String fileInInDir : filesInInDir) {
                    root.put(fileInInDir);
                }
            }

            ps.append(root.toString());
            ps.flush();

        } catch (IOException e) {
            Logger.error(TAG, "createSftFilesListFile ошибка", e);
        } finally {
            if (ps != null) {
                ps.close();
            }
        }


        g.getBroadcasts().newArmSyncEvent(g, ArmSyncEvents.CreateSftFilesListFileReady, "", new ru.ppr.cppk.export.Exchange.Error());

        convertTempBinFileToResponse(jsonFile, STATE_sftfileslist, requestTimeStamp, RESPONSE_COUNT);
        Logger.info(TAG, "createSftFilesListFile FINISH");
    }


    /**
     * Создает файл ответа на запрос состояния
     *
     * @param requestTimeStamp
     */
    @SuppressLint("WrongConstant")
    public void createGetStateResp(long requestTimeStamp) {
        createSftFilesListFile(g, requestTimeStamp);
        int ptkSftState = 0;
        try {
            ptkSftState = Di.INSTANCE.getEdsManagerWrapper().getStateBlocking().getState();
        } catch (Exception e) {
            Logger.error(TAG, "Error get ecp state", e);
        }

        createGetStateRespReady(ptkSftState, requestTimeStamp);
    }

    private void createGetStateRespReady(int ptkSftState, long requestTimeStamp) {

        Logger.info(TAG, "createGetStateRespReady() START");

        JSONObject root = null;
        File f = ExportUtils.getTempFile(STATE_getStateResp, requestTimeStamp);
        f.getParentFile().mkdirs();

        Exchange.Error error = new Exchange.Error();

        StateBuilder stateBuilder = new StateBuilder(Di.INSTANCE.getApp(),
                Di.INSTANCE.getDbManager().getLocalDaoSession().get(),
                Di.INSTANCE.nsiVersionManager(),
                Di.INSTANCE.getDbManager().getSecurityDaoSession().get(),
                Di.INSTANCE.getPrivateSettings().get(),
                Di.INSTANCE.applicationInfo(),
                Di.INSTANCE.securityDataContractsVersionChecker(),
                Di.INSTANCE.nsiDataContractsVersionChecker(),
                Dagger.appComponent().serviceTicketControlEventRepository());
        stateBuilder.setSftState(ptkSftState);
        State state = null;

        try {
            state = stateBuilder.build();
        } catch (PackageManager.NameNotFoundException e) {
            error = new Exchange.Error(Error.NameNotFoundException, e.getMessage());
            Logger.error(TAG, "createGetStateRespReady() Ошибка создания файла State: " + e.getMessage());
            Logger.error(TAG, e);
        }

        if (!error.isError()) {
            try {
                root = new StateWriter().getJson(state);
            } catch (JSONException e) {
                error = new Exchange.Error(Error.JSONException, e.getMessage());
                Logger.error(TAG, "createGetStateRespReady() Ошибка создания файла State: " + e.getMessage());
                Logger.error(TAG, e);
            }
        }

        if (error.isError()) {
            createErrorFile(STATE_getStateResp, STATE_getStateRespSig, error, requestTimeStamp);
        } else {
            try {
                FileOutputStream fos = new FileOutputStream(f, true);
                PrintStream ps = new PrintStream(fos);

                ps.append(root.toString());
                Logger.trace(TAG, "createGetStateRespReady() json: " + root);

                ps.flush();
                fos.flush();

                ps.close();
                fos.close();

                Logger.info(TAG, root.toString());

                createSigFile(f, STATE_getStateRespSig, STATE_getStateResp, requestTimeStamp);

            } catch (IOException e) {
                Logger.error(TAG, "createGetStateRespReady() ошибка: " + e.getMessage());
                Logger.error(TAG, e);
            }
        }

        Logger.info(TAG, "createGetStateRespReady() FINISH");
    }

    /**
     * Создает файл подписи и переименовывает файл с данными
     *
     * @param tempBinFile
     * @param targetSigFile
     * @param targetBinFile
     * @param requestTimeStamp
     */
    public boolean createSigFile(final File tempBinFile, final String targetSigFile, final String targetBinFile, long requestTimeStamp) {

        int responseCount = RESPONSE_COUNT;

        Logger.trace(TAG, "createSigFile. start " + targetSigFile + ", requestTimeStamp = " + requestTimeStamp);
        File defaultTargetSigFile = Exchange.Fn.getFileWithTimeStamp(new File(targetSigFile), requestTimeStamp);
        Globals.getInstance().getBroadcasts().newArmSyncEvent(Globals.getInstance(), ArmSyncEvents.CreateFileSigStart, defaultTargetSigFile.getName(), new Exchange.Error());
        SignDataResult signDataResult = ExportUtils.createSigToFile(tempBinFile, Di.INSTANCE.getApp());
        Logger.trace(TAG, "createSigFile. создание самой подписи завершено");
        byte[] sig = ExportUtils.createSigData(signDataResult.getSignature(), signDataResult.getEdsKeyNumber());
        Logger.trace(TAG, "createSigFile. создание данных которые нужно записать в файл sig завершено");
        //сохраняем подпись в файл
        File tempSigFile = ExportUtils.getTempFile(targetSigFile, requestTimeStamp);
        boolean result = ExportUtils.saveSigToFile(sig, tempSigFile);
        Logger.trace(TAG, "createSigFile. Создание temp-файла " + tempSigFile.getName() + " завершено, res = " + result);

        if (result) {
            for (int i = 0; i < responseCount; i++) {
                File dst = Exchange.Fn.getFileWithTimeStampAndIndex(new File(targetSigFile), requestTimeStamp, i);
                boolean res = FileUtils.copy(g, tempSigFile, dst);
                result |= res;
                Logger.trace(TAG, "Создаем копию файла c уведомлением по MTP " + tempSigFile.getName() + " -> " + dst.getName() + ", res = " + res);
                Exchange.Error error = (res) ? new Error() : new Error(Error.FileRenameException, dst.getName());
                Globals.getInstance().getBroadcasts().newArmSyncEvent(Globals.getInstance(), ArmSyncEvents.CreateFileSigFinish, dst.getName(), error);
            }
            Logger.trace(TAG, "Удаляем temp файл c уведомлением по MTP " + tempSigFile.getName());
            FileUtils.deleteFileMtp(g, tempSigFile);

            //Создаем bin файлы из temp-файла
            convertTempBinFileToResponse(tempBinFile, targetBinFile, requestTimeStamp, responseCount);
        }

        return result;
    }

    private boolean convertTempBinFileToResponse(File tempBinFile, String targetBinFile, long requestTimeStamp, int responseCount) {

        boolean result = false;
        for (int i = 0; i < responseCount; i++) {
            File dst = Exchange.Fn.getFileWithTimeStampAndIndex(new File(targetBinFile), requestTimeStamp, i);
            boolean res = FileUtils.copy(g, tempBinFile, dst);
            result |= res;
            Logger.trace(TAG, "Создаем копию файла c уведомлением по MTP " + tempBinFile.getName() + " -> " + dst.getName() + ", res = " + res);
            Exchange.Error error = (res) ? new Error() : new Error(Error.FileRenameException, dst.getName());
            Globals.getInstance().getBroadcasts().newArmSyncEvent(Globals.getInstance(), ArmSyncEvents.CreateFileFinish, dst.getName(), error);
        }
        Logger.trace(TAG, "Удаляем temp файл c уведомлением по MTP " + tempBinFile.getName());
        FileUtils.deleteFileMtp(g, tempBinFile);

        return result;
    }

    private void addFileContentToLog(String filePath) {
        Logger.info(TAG, FileUtils.getFileContent(filePath));
    }

}
