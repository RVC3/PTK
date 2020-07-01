/**
 * Класс реализует протокол обмена данными с АРМ
 * Слушатель событий инициированныр АРМОМ
 */

package ru.ppr.cppk.export;

import android.annotation.SuppressLint;
import android.os.Environment;
import android.support.annotation.IntDef;
import android.text.TextUtils;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import org.json.JSONException;
import org.json.JSONObject;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import javax.xml.parsers.ParserConfigurationException;

import ru.ppr.core.manager.eds.CheckSignResultState;
import ru.ppr.core.manager.eds.EdsManager;
import ru.ppr.core.manager.eds.EdsManagerWrapper;
import ru.ppr.cppk.Globals;
import ru.ppr.cppk.di.Dagger;
import ru.ppr.cppk.entity.settings.PrivateSettings;
import ru.ppr.cppk.export.loader.GetEventsReqLoader;
import ru.ppr.cppk.export.loader.PtkStartSyncRequestLoader;
import ru.ppr.cppk.export.model.request.GetEventsReq;
import ru.ppr.cppk.export.model.request.PtkStartSyncRequest;
import ru.ppr.cppk.helpers.EmergencyModeHelper;
import ru.ppr.cppk.legacy.EcpUtils;
import ru.ppr.cppk.utils.CommonSettingsUtils;
import ru.ppr.cppk.utils.PrivateSettingsUtils;
import ru.ppr.logger.Logger;
import ru.ppr.utils.CommonUtils;
import ru.ppr.utils.FileUtils;
import ru.ppr.utils.MD5Utils;

/**
 * Класс осуществляет работу протокола обмена данными с АРМ загрузки данных. В
 * отдельном потоке следит за изменением файликов, отвечающих за реализацию
 * протокола.
 *
 * @author G.Kashka
 */
public class Exchange {

    private static final String TAG = Logger.makeLogTag(Exchange.class);
    private static final int DELAY = 100; // задержка между опросами папки обмена
    private static final boolean enableLogFileContent = true;

    /* FOLDERS PATHS */
    public static final String DIR = Environment.getExternalStorageDirectory().getAbsolutePath() + "/#CPPKConnect";
    public static final String KPP = DIR + "/KPP";
    public static final String RDS = DIR + "/RDS";
    public static final String SECURITY = DIR + "/Security";
    public static final String SFT = DIR + "/Sft";
    public static final String SFT_OUT = SFT + "/out";
    public static final String SFT_LIC = SFT + "/lic";
    public static final String SOFTWARE = DIR + "/Software";
    public static final String SOFTWARE_NEW = SOFTWARE + "/temp";
    public static final String STATE = DIR + "/State";
    public static final String CANCEL = DIR + "/Cancel";


    /* SftTransport */
    /**
     * Файл информирует о  том, что касса сформировала и записала файлы с json списком удаляемых файлов и tar.gz архив новых ключей
     */
    public static final String SFT_transmissionCompleted = SFT + "/transmissioncompleted.info";
    public static final String SFT_inTarGz = Exchange.SFT + "/in.tar.gz";
    public static final String SFT_listDelete = Exchange.SFT + "/sftfilestodelete.bin";

    /* KPP */
    public static final String KPP_getEventsReqInfo = "/getEvents_req.info";
    public static final String KPP_getEventsReq = KPP + "/getEvents_req.bin";
    public static final String KPP_getEventsReqSig = KPP + "/getEvents_req.sig";

    /* RDS */
    public static final String RDS_RdsInfo = RDS + "/" + "RDS.info";
    public static final String RDS_Rds = RDS + "/" + "RDS.bin";
    public static final String RDS_RdsSig = RDS + "/" + "RDS.sig";

    /* Security */
    public static final String SECURITY_securityInfo = SECURITY + "/" + "Security.info";
    public static final String SECURITY_security = SECURITY + "/" + "Security.bin";
    public static final String SECURITY_securitySig = SECURITY + "/" + "Security.sig";

    /* State */
    public static final String STATE_getStateInfo = STATE + "/" + "getState_req.info";
    public static final String STATE_getState = STATE + "/" + "getState_req.bin";
    public static final String STATE_getStateSig = STATE + "/" + "getState_req.sig";

    public static final String STATE_getLastShiftInfo = STATE + "/" + "getLastShift_req.info";
    public static final String STATE_getLastShift = STATE + "/" + "getLastShift_req.bin";
    public static final String STATE_getLastShiftSig = STATE + "/" + "getLastShift_req.sig";

    public static final String STATE_getSettingsInfo = STATE + "/" + "getSettings_req.info";
    public static final String STATE_getSettings = STATE + "/" + "getSettings_req.bin";
    public static final String STATE_getSettingsSig = STATE + "/" + "getSettings_req.sig";

    public static final String STATE_ArmConnectedInfo = STATE + "/" + "armConnected.info";
    public static final String STATE_ArmConnected = STATE + "/" + "armConnected.bin";
    public static final String STATE_ArmConnectedSig = STATE + "/" + "armConnected.sig";

    public static final String STATE_ArmDisconnected = STATE + "/" + "armDisconnected.info";

    public static final String STATE_getBackupInfo = STATE + "/" + "getBackup_req.info";
    public static final String STATE_getBackup = STATE + "/" + "getBackup_req.bin";
    public static final String STATE_getBackupSig = STATE + "/" + "getBackup_req.sig";

    public static final String STATE_getTimeInfo = STATE + "/" + "getTime_req.info";
    public static final String STATE_getTime = STATE + "/" + "getTime_req.bin";
    public static final String STATE_getTimeSig = STATE + "/" + "getTime_req.sig";

    public static final String STATE_setTimeInfo = STATE + "/" + "setTime_req.info";
    public static final String STATE_setTime = STATE + "/" + "setTime_req.bin";
    public static final String STATE_setTimeSig = STATE + "/" + "setTime_req.sig";

    public static final String STATE_setSettingsInfo = STATE + "/" + "setSettings_req.info";
    public static final String STATE_setSettings = STATE + "/" + "setSettings_req.bin";
    public static final String STATE_setSettingsSig = STATE + "/" + "setSettings_req.sig";

    public static final String STATE_syncFinished = STATE + "/" + "syncFinished.info"; //без подписи, пустой. Касса сообщает что синхронизация завершена успешно

    /* Software */
    public static final String SOFTWARE_newVersionInfo = SOFTWARE + "/" + "newVersion.info";
    public static final String SOFTWARE_newVersion = SOFTWARE + "/" + "newVersion.apk"; // не архивированный.
    public static final String SOFTWARE_newVersionSig = SOFTWARE + "/" + "newVersion.sig";

    public static final String SOFTWARE_newVersionSettingsInfo = SOFTWARE + "/" + "newVersionSettings.info";
    public static final String SOFTWARE_newVersionSettings = SOFTWARE + "/" + "newVersionSettings.bin"; // общие настройки.
    public static final String SOFTWARE_newVersionSettingsSig = SOFTWARE + "/" + "newVersionSettings.sig";


    /**
     * возвращает файл, в который нужно при первом же обнаружении переименовать
     * файлик выкладываемый кассой, чтобы второй раз не генерировать события по
     * нему.
     *
     * @param filePath
     * @return
     */
    public static File getTempFile(String filePath, long requestTimeStamp) {
        return ExportUtils.getTempFile(filePath, requestTimeStamp);

    }

    /**
     * Ошибка протокола синхронизации с кассой.
     * Менять коды ошибок нельзя, их использует касса.
     * https://confluence.srvdev.ru/pages/viewpage.action?pageId=15892921
     */
    public static class Error {

        /**
         * нет ошибок
         */
        public static final int NONE = 0;
        /**
         * ошибка проверки подписи
         */
        public static final int ECP = 1;
        public static final int JSONException = 2;
        public static final int IOException = 3;
        public static final int ParseException = 4;
        public static final int UNKNOWN = 5;
        public static final int FileNotFoundException = 6;
        public static final int ZIP = 7;

        public static final int SetTimeError = 18;
        public static final int NewApkFileNotFound = 9;
        public static final int PullSftError = 10;
        public static final int ShiftError = 11;
        public static final int Backup = 12;
        public static final int NameNotFoundException = 14;

        /**
         * ошибка формирования ответа по последней смене - смен еще небыло
         */
        public static final int NoShifts = 15;
        /**
         * ошибка переименования файла
         */
        public static final int FileRenameException = 16;
        /**
         * Некорректная версия НСИ
         */
        public static final int IncorrectNsiDataContractVersion = 53;
        /**
         * Некорректная версия Security базы
         */
        public static final int IncorrectSecurityDataContractVersion = 54;
        /**
         * Ошибка разбора частных настроек
         */
        public static final int INCORRECT_PRIVATE_CONFIG = 63;
        /**
         * Ошибка разбора общего конфига
         */
        public static final int INCORRECT_COMMON_CONFIG = 64;


        public Error() {
        }

        public Error(int code, String message) {
            this.code = code;
            this.message = message;
        }

        private String message = "";
        public int code = NONE;

        public String getMessage() {
            if (!TextUtils.isEmpty(message)) return message;
            if (code == ECP)
                message = "Ошибка цифровой подписи";
            else if (code == ZIP)
                message = "Ошибка архивирования";
            else if (TextUtils.isEmpty(message))
                message = null;
            return message;
        }

        public boolean isError() {
            return code != NONE;
        }

        @Override
        public String toString() {
            return "Error{" +
                    "message='" + message + '\'' +
                    ", code=" + code +
                    '}';
        }
    }

    public Exchange(Globals g, EdsManager edsManager, EdsManagerWrapper edsManagerWrapper) {
        this.g = g;
        this.edsManager = edsManager;
        this.edsManagerWrapper = edsManagerWrapper;
    }

    /* listener */
    public interface ExchangeListener {
        /**
         * запрос на выгрузку собитий от кассы к ПТК
         */
        void onGetEventsReq(GetEventsReq getEventsReq, Error error, long requestTimeStamp);

        /**
         * запрос на обновление базы данных НСИ _ReferenceDatabase.db
         */
        void onRdsDbUpdataReq(File gzipFile, Error error, long requestTimeStamp);

        /**
         * запрос на обновление базы данных НСИ _SecurityDatabase.db
         */
        void onSecurityDbUpdataReq(File gzipFile, Error error, long requestTimeStamp);

        void onNewVersionDetected(File apkFile, Error error, long requestTimeStamp);

        void onCommonSettingsDetected(File xmlFile, Error error, long requestTimeStamp);

        void onGetLastShiftReq(Error error, long requestTimeStamp);

        void onGetSettingsReq(Error error, long requestTimeStamp);

        void onGetTimeReq(Error error, long requestTimeStamp);

        void onGetStateReq(long requestTimeStamp);

        void onArmConnected(boolean oldRequest, boolean isEcpOk, Error error, PtkStartSyncRequest ptkStartSyncRequest, long requestTimeStamp);

        void onArmDisconnected(long requestTimeStamp);

        void onSetSettingsReq(PrivateSettings settings, Error error, long requestTimeStamp);

        void onSetTimeReq(long timeInMilliseconds, long eventDetectTimeStamp, Error error, long requestTimeStamp);

        void onStopped(String text);

        void onTransmissionCompleteDetected(File inTarGzFile, File jsonToDeleteFile, long requestTimeStamp);

        /**
         * касса сообщила что синхронизация успешно завершена!
         */
        void onSyncFinishedDetected(long requestTimeStamp);

        /**
         * запрос на получение полного бекапа ПТК
         */
        void onGetBackupReq(Error error, long requestTimeStamp);
    }


    @Retention(RetentionPolicy.SOURCE)
    @IntDef({EXCHANGE_STATE_WAIT_CONNECT, EXCHANGE_STATE_SYNCHRONIZATION})
    private @interface ExchangeState {
    }

    private
    @ExchangeState
    int exchangeState = EXCHANGE_STATE_WAIT_CONNECT;

    public
    @ExchangeState
    int getExchangeState() {
        return exchangeState;
    }

    public void setExchangeState(@ExchangeState int exchangeState) {
        this.exchangeState = exchangeState;
    }

    public static final int EXCHANGE_STATE_WAIT_CONNECT = 0;
    public static final int EXCHANGE_STATE_SYNCHRONIZATION = 1;

    private final Globals g;
    private final EdsManager edsManager;
    private final EdsManagerWrapper edsManagerWrapper;

    private SyncRunnable syncRunnable = null;
    private Future<?> syncFuture = null;
    private static final ExecutorService EXECUTOR = new ThreadPoolExecutor(0, //размер пула потоков
            1, // максимальное количество потоков в пуле
            2, // время, после которого поток уничтожается
            TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(),
            new ThreadFactoryBuilder().setNameFormat("listen-thread-%d").build());

    public synchronized void startListen(ExchangeListener listener) {
        // Если к нам так или иначе пришел лишний таск (а такого быть не должно) мы не должны падать,
        // просто не сабмитим новый таск в EXECUTOR, в теории последствий быть не должно
        // т.к. кроме самого сервиса который и так работает (иначе syncRunnable == null)
        // коллбек никому не нужен, а в последствии сервис должны остановить из того же места откуда запускали.
        // В будущем так или иначе решене временное, при наличии времени надо в принципе избавиться от данных ситуаций
        // см. http://agile.srvdev.ru/browse/CPPKPP-39379
        if (syncRunnable == null) {
            // Передаем коллбэк, который должен при завершении синхронизации очистить текущий syncRunnable
            syncRunnable = new SyncRunnable(g, edsManager, edsManagerWrapper, listener, this::clearCurrentRunnable);
            syncFuture = EXECUTOR.submit(syncRunnable);
        } else {
            Logger.error(TAG, "!!!ATTENTION!!! startListen called twice, syncRunnable != null");
        }
    }

    private synchronized void clearCurrentRunnable() {
        syncRunnable = null;
        syncFuture = null;
    }

    /**
     * останавливает сервис прослушивания
     */
    public synchronized void stop(String description) {

        if (syncFuture != null && syncRunnable != null) {
            Logger.info(getClass(), "Останавливаем сервис синхронизации - " + description);
            if (!syncFuture.isDone()) {
                syncFuture.cancel(true);
            }
            syncRunnable = null;
            syncFuture = null;
        }
    }

    /**
     * Создает пакет данных для проверки подписи файла
     *
     * @param dataFile
     * @param ecpFile
     * @return
     */
    private static EcpSigData getDataForCheck(File dataFile, File ecpFile) {

        Logger.info(Exchange.class, "Получаем данные для проверки подписи");
        Logger.info(Exchange.class, "Файл для проверки - " + dataFile.getName() + ", файл с подписью - " + ecpFile.getName());

        EcpSigData esd = new EcpSigData();

        if (!dataFile.exists() || !ecpFile.exists()) {
            Logger.info(Exchange.class, "Файл " + (!dataFile.exists() ? dataFile.getName() : ecpFile.getName()) + " не найден!");
            esd.isDataOk = false;
            return esd;
        }
        try {
            byte[] allData = org.apache.commons.io.FileUtils.readFileToByteArray(ecpFile);
            if (allData.length < 5) {
                esd.isDataOk = false;
                return esd;
            }

            byte[] keyNumber = new byte[4]; // номер ключа эцп
            esd.sig = new byte[allData.length - 4]; // подпись

            System.arraycopy(allData, 0, keyNumber, 0, keyNumber.length);
            System.arraycopy(allData, keyNumber.length, esd.sig, 0, allData.length - keyNumber.length);

            int intKeyNumber = java.nio.ByteBuffer.wrap(keyNumber).order(java.nio.ByteOrder.LITTLE_ENDIAN).getInt();
            esd.keyNumber = EcpUtils.convertIntToLong(intKeyNumber);
            esd.data = MD5Utils.generateMD5(dataFile);

            Logger.info(Exchange.class, "Номер ключа - " + esd.keyNumber + ",\nданные для проверки - " +
                    CommonUtils.byteArrayToString(esd.data) + ",\nподпись - " + CommonUtils.byteArrayToString(esd.sig));
            esd.isDataOk = true;

            Logger.info(Exchange.class, "Данные для проверки получены успешно");

        } catch (Exception e) {
            Logger.error(Exchange.class, "Ошибка при получении данных - " + e.getMessage());
            Logger.error(TAG, e);
            e.printStackTrace();
        }
        return esd;
    }

    public static String loadJSONFromFile(File f) throws IOException {
        FileInputStream stream = new FileInputStream(f);
        int size = stream.available();
        byte[] buffer = new byte[size];
        stream.read(buffer);
        String json = new String(buffer, "UTF-8");
        stream.close();
        return json;
    }

    /* CОБЫТИЯ */


    private static class EcpSigData {

        public EcpSigData() {
        }

        public byte[] data;
        public byte[] sig;
        public long keyNumber;
        public boolean isDataOk;
    }

    private static final AtomicInteger counter = new AtomicInteger(0);

    private static class SyncRunnable implements Runnable {

        private static final String[] FILES_FOR_DELETE_GET_STATE = new String[]{
                STATE_getStateInfo,
                STATE_getState,
                STATE_getStateSig,
                Response.STATE_getStateResp,
                Response.STATE_getStateRespSig,
                Response.STATE_sftfileslist
        };
        private static final String[] FILES_FOR_DELETE_ARM_CONNECTED = new String[]{
                STATE_ArmConnectedInfo,
                STATE_ArmConnected,
                STATE_ArmConnectedSig,
                Response.STATE_armConnectedResp,
                STATE_ArmDisconnected
        };
        private static final String[] FILES_FOR_DELETE_ARM_DISCONNECTED = new String[]{
                STATE_ArmDisconnected,
                STATE_ArmConnectedInfo,
                Response.STATE_armConnectedResp,
        };
        private static final String[] FILES_FOR_DELETE_SYNC_FINISHED = new String[]{
                STATE_syncFinished,
                Response.STATE_syncFinishedResp,
                Response.STATE_syncFinishedRespSig
        };
        private static final String[] FILES_FOR_DELETE_TRANSMISSION_COMPLETED = new String[]{
                SFT_transmissionCompleted,
                SFT_inTarGz,
                SFT_listDelete,
                Response.SFT_transmissioncompleted_resp
        };
        private static final String[] FILES_FOR_DELETE_GET_EVENTS = new String[]{
                KPP_getEventsReq,
                KPP_getEventsReqInfo,
                KPP_getEventsReqSig,
                Response.KPP_getEventsResp,
                Response.KPP_getEventsRespSig
        };
        private static final String[] FILES_FOR_DELETE_RDS = new String[]{
                RDS_Rds,
                RDS_RdsInfo,
                RDS_RdsSig,
                Response.RDS_RdsResp,
                Response.RDS_RdsRespSig
        };
        private static final String[] FILES_FOR_DELETE_SECURITY = new String[]{
                SECURITY_security,
                SECURITY_securityInfo,
                SECURITY_securitySig,
                Response.SECURITY_SecurityResp,
                Response.SECURITY_SecurityRespSig
        };
        private static final String[] FILES_FOR_DELETE_GET_LAST_SHIFT = new String[]{
                STATE_getLastShift,
                STATE_getLastShiftInfo,
                STATE_getLastShiftSig,
                Response.STATE_getLastShiftResp,
                Response.STATE_getLastShiftRespSig
        };
        private static final String[] FILES_FOR_DELETE_GET_SETTINGS = new String[]{
                STATE_getSettings,
                STATE_getSettingsInfo,
                STATE_getSettingsSig,
                Response.STATE_getSettingsResp,
                Response.STATE_getSettingsRespSig
        };
        private static final String[] FILES_FOR_DELETE_GET_TIME = new String[]{
                STATE_getTime,
                STATE_getStateInfo,
                STATE_getTimeSig,
                Response.STATE_getTimeResp,
                Response.STATE_getTimeRespSig
        };
        private static final String[] FILES_FOR_DELETE_SET_SETTINGS = new String[]{
                STATE_setSettings,
                STATE_setSettingsInfo,
                STATE_setSettingsSig,
                Response.STATE_setSettingsResp,
                Response.STATE_setSettingsRespSig
        };
        private static final String[] FILES_FOR_DELETE_SET_TIME = new String[]{
                STATE_setTime,
                STATE_setTimeInfo,
                STATE_setTimeSig,
                Response.STATE_setTimeResp,
                Response.STATE_setTimeRespSig
        };
        private static final String[] FILES_FOR_DELETE_GET_BACKUP = new String[]{
                STATE_getBackup,
                STATE_getBackupInfo,
                STATE_getBackupSig,
                Response.STATE_getBackupResp,
                Response.STATE_getBackupRespSig
        };
        private static final String[] FILES_FOR_DELETE_NEW_VERSION = new String[]{
                SOFTWARE_newVersion,
                SOFTWARE_newVersionInfo,
                SOFTWARE_newVersionSig,
                Response.SOFTWARE_newApkFoundResp,
                Response.SOFTWARE_newApkFoundRespSig,
                Response.SOFTWARE_newVersionResp,
                Response.SOFTWARE_newVersionRespSig,
        };
        private static final String[] FILES_FOR_DELETE_NEW_VERSION_SETTINGS = new String[]{
                SOFTWARE_newVersionSettings,
                SOFTWARE_newVersionSettingsInfo,
                SOFTWARE_newVersionSettingsSig,
                Response.SOFTWARE_newVersionSettingsResp,
                Response.SOFTWARE_newVersionSettingRespSig
        };

        private final Globals g;
        private final EdsManager edsManager;
        private final EdsManagerWrapper edsManagerWrapper;
        private final AtomicBoolean isRun;
        private final ExchangeListener listener;
        private final CountDownLatch latch;
        private final Runnable runnableAtTheEnd;

        private SyncRunnable(Globals globals, EdsManager edsManager, EdsManagerWrapper edsManagerWrapper, ExchangeListener listener, Runnable runnableAtTheEnd) {
            this.g = globals;
            this.edsManager = edsManager;
            this.edsManagerWrapper = edsManagerWrapper;
            isRun = new AtomicBoolean(false);
            this.listener = listener;
            latch = new CountDownLatch(1);
            this.runnableAtTheEnd = runnableAtTheEnd;
        }

        public void waitEnd() {
            try {
                latch.await();
            } catch (InterruptedException e) {
                Thread.interrupted();
            }
        }

        @Override
        public void run() {

            Logger.trace(getClass(), "Start sync runnable");

            try {
                isRun.set(true);
                g.getBroadcasts().newArmSyncEvent(g, ArmSyncEvents.Runned, null, new Error());
                //Fn.clearFolders(g, System.currentTimeMillis());

                Globals.getInstance().getResponse().createUpdateApkResponse();

                while (isRun.get()) {

                    if (Thread.currentThread().isInterrupted()) {
                        Logger.trace(getClass(), "Interrupt thread");
                        isRun.set(false);
                        return;
                    }
                    doInBackground();
                    TimeUnit.MILLISECONDS.sleep(DELAY);
                }
            } catch (Exception e) {
                Logger.error(TAG, e);
                // http://agile.srvdev.ru/browse/CPPKPP-38044
                // Rx.toBlocking().single() оборачивает InterruptedException в RuntimeException
                // Поэтому проверяем истинную причину исключения
                Throwable cause = e;
                while (cause.getCause() != null) {
                    cause = cause.getCause();
                }
                if (cause instanceof InterruptedException) {
                    Logger.trace(TAG, "Sync runnable is interrupted");
                    Thread.currentThread().interrupt();
                } else {
                    EmergencyModeHelper.startEmergencyMode(e);
                }
            } finally {
                latch.countDown();
                listener.onStopped("Sync runnable stopped");
            }
            runnableAtTheEnd.run();
            Logger.trace(getClass(), "Stop sync runnable");

        }

        private void doInBackground() {

            long requestTimeStamp;
            requestTimeStamp = Fn.getRequestFileTimeStamp(new File(STATE_ArmConnectedInfo));
            if (requestTimeStamp > 0) {
                Logger.trace(TAG, "STATE_ArmConnectedInfo detected");

                Fn.deleteFilesWithTimeStampMtp(g, new File(STATE_ArmConnectedInfo));
                Fn.deleteOldFiles(g, FILES_FOR_DELETE_ARM_CONNECTED, requestTimeStamp);
                File bin = Fn.getFileWithTimeStamp(new File(STATE_ArmConnected), requestTimeStamp);
                File sig = Fn.getFileWithTimeStamp(new File(STATE_ArmConnectedSig), requestTimeStamp);
                if (enableLogFileContent) {
                    addFileContentToLog(bin.getAbsolutePath());
                    addFileContentToLog(sig.getAbsolutePath());
                }
                File tempFile = getTempFile(STATE_ArmConnected, requestTimeStamp);
                FileUtils.renameFile(g, bin, tempFile);
                Dagger.appComponent().fileCleaner().clearDirRecursive(new File(DIR), TimeUnit.HOURS.toMillis(1));
                onARMConnectedReq(tempFile, requestTimeStamp);
                return;
            }

            if (Globals.getInstance().getExchange().getExchangeState() == EXCHANGE_STATE_WAIT_CONNECT) {
                return;
            }

            if (Thread.currentThread().isInterrupted()) {
                return;
            }

            requestTimeStamp = Fn.getRequestFileTimeStamp(new File(STATE_getStateInfo));
            if (requestTimeStamp > 0) {
                Logger.trace(TAG, "STATE_getStateInfo detected");
                Fn.deleteFilesWithTimeStampMtp(g, new File(STATE_getStateInfo));
                Fn.deleteOldFiles(g, FILES_FOR_DELETE_GET_STATE, requestTimeStamp);
                onGetStateReq(requestTimeStamp);
                return;
            }

            if (Thread.currentThread().isInterrupted()) {
                return;
            }

            requestTimeStamp = Fn.getRequestFileTimeStamp(new File(STATE_ArmDisconnected));
            if (requestTimeStamp > 0) {
                Logger.trace(TAG, "STATE_ArmDisconnected detected");
                Fn.deleteFilesWithTimeStampMtp(g, new File(STATE_ArmDisconnected));
                Fn.deleteOldFiles(g, FILES_FOR_DELETE_ARM_DISCONNECTED, requestTimeStamp);
                Dagger.appComponent().fileCleaner().clearDirRecursive(new File(DIR), TimeUnit.HOURS.toMillis(1));
                onArmDisconnected(requestTimeStamp);
                return;
            }

            if (Thread.currentThread().isInterrupted()) {
                return;
            }

            requestTimeStamp = Fn.getRequestFileTimeStamp(new File(STATE_syncFinished));
            if (requestTimeStamp > 0) {
                Logger.trace(TAG, "STATE_syncFinished detected");
                Fn.deleteFilesWithTimeStampMtp(g, new File(STATE_syncFinished));
                Fn.deleteOldFiles(g, FILES_FOR_DELETE_SYNC_FINISHED, requestTimeStamp);
                onSyncFinished(requestTimeStamp);
                return;
            }

            if (Thread.currentThread().isInterrupted()) {
                return;
            }

            requestTimeStamp = Fn.getRequestFileTimeStamp(new File(SFT_transmissionCompleted));
            if (requestTimeStamp > 0) {
                Logger.trace(TAG, "SFT_transmissionCompleted detected");
                Fn.deleteFilesWithTimeStampMtp(g, new File(SFT_transmissionCompleted));
                Fn.deleteOldFiles(g, FILES_FOR_DELETE_TRANSMISSION_COMPLETED, requestTimeStamp);
                File tarGzipWithTimeStamp = Fn.getFileWithTimeStamp(new File(SFT_inTarGz), requestTimeStamp);
                File deleteListWithTimeStamp = Fn.getFileWithTimeStamp(new File(SFT_listDelete), requestTimeStamp);
                onTransmissionComplete(tarGzipWithTimeStamp, deleteListWithTimeStamp, requestTimeStamp);
                return;
            }

            if (Thread.currentThread().isInterrupted()) {
                return;
            }

            final String DIR_KPP = Environment.getExternalStorageDirectory().getAbsolutePath() + "/#CPPKConnect/KPP";
            requestTimeStamp = Fn.getRequestFileTimeStamp(new File(DIR_KPP + KPP_getEventsReqInfo));
            if (requestTimeStamp > 0) {
                Logger.trace(TAG, "KPP_getEventsReqInfo detected");
                Fn.deleteFilesWithTimeStampMtp(g, new File(DIR_KPP + KPP_getEventsReqInfo));
                Fn.deleteOldFiles(g, FILES_FOR_DELETE_GET_EVENTS, requestTimeStamp);
                File getEventsReqWithTimeStamp = Fn.getFileWithTimeStamp(new File(KPP_getEventsReq), requestTimeStamp);
                File getEventsReqSigWithTimeStamp = Fn.getFileWithTimeStamp(new File(KPP_getEventsReqSig), requestTimeStamp);
                if (enableLogFileContent) {
                    addFileContentToLog(getEventsReqWithTimeStamp.getAbsolutePath());
                    addFileContentToLog(getEventsReqSigWithTimeStamp.getAbsolutePath());
                }
                File tempFile = getTempFile(KPP_getEventsReq, requestTimeStamp);
                FileUtils.renameFile(g, getEventsReqWithTimeStamp, tempFile);
                onGetEventsReq(tempFile, requestTimeStamp);
                return;
            }

            if (Thread.currentThread().isInterrupted()) {
                return;
            }

            requestTimeStamp = Fn.getRequestFileTimeStamp(new File(RDS_RdsInfo));
            if (requestTimeStamp > 0) {
                Logger.trace(TAG, "RDS_RdsInfo detected");
                Fn.deleteFilesWithTimeStampMtp(g, new File(RDS_RdsInfo));
                Fn.deleteOldFiles(g, FILES_FOR_DELETE_RDS, requestTimeStamp);
                File rdsWithTimeStamp = Fn.getFileWithTimeStamp(new File(RDS_Rds), requestTimeStamp);
                Logger.info(TAG, "RdsDb file size: " + rdsWithTimeStamp.length());
                File rdsSigWithTimeStamp = Fn.getFileWithTimeStamp(new File(RDS_RdsSig), requestTimeStamp);
                if (enableLogFileContent) {
                    addFileContentToLog(rdsSigWithTimeStamp.getAbsolutePath());
                }
                File tempFile = getTempFile(RDS_Rds, requestTimeStamp);
                FileUtils.renameFile(g, rdsWithTimeStamp, tempFile);
                onRdsDbUpdataReq(tempFile, requestTimeStamp);
                return;
            }

            if (Thread.currentThread().isInterrupted()) {
                return;
            }

            requestTimeStamp = Fn.getRequestFileTimeStamp(new File(SECURITY_securityInfo));
            if (requestTimeStamp > 0) {
                Logger.trace(TAG, "SECURITY_securityInfo detected");
                Fn.deleteFilesWithTimeStampMtp(g, new File(SECURITY_securityInfo));
                Fn.deleteOldFiles(g, FILES_FOR_DELETE_SECURITY, requestTimeStamp);
                File securityWithTimeStamp = Fn.getFileWithTimeStamp(new File(SECURITY_security), requestTimeStamp);
                Logger.info(TAG, "SecurityDb file size: " + securityWithTimeStamp.length());
                File securitySigWithTimeStamp = Fn.getFileWithTimeStamp(new File(SECURITY_securitySig), requestTimeStamp);
                if (enableLogFileContent) {
                    addFileContentToLog(securitySigWithTimeStamp.getAbsolutePath());
                }
                File tempFile = getTempFile(SECURITY_security, requestTimeStamp);
                FileUtils.renameFile(g, securityWithTimeStamp, tempFile);
                onSecurityDbUpdataReq(tempFile, requestTimeStamp);
                return;
            }

            if (Thread.currentThread().isInterrupted()) {
                return;
            }

            requestTimeStamp = Fn.getRequestFileTimeStamp(new File(STATE_getLastShiftInfo));
            if (requestTimeStamp > 0) {
                Logger.trace(TAG, "STATE_getLastShiftInfo detected");
                Fn.deleteFilesWithTimeStampMtp(g, new File(STATE_getLastShiftInfo));
                Fn.deleteOldFiles(g, FILES_FOR_DELETE_GET_LAST_SHIFT, requestTimeStamp);
                File getLastShiftWithTimeStamp = Fn.getFileWithTimeStamp(new File(STATE_getLastShift), requestTimeStamp);
                File getLastShiftSigWithTimeStamp = Fn.getFileWithTimeStamp(new File(STATE_getLastShiftSig), requestTimeStamp);
                if (enableLogFileContent) {
                    addFileContentToLog(getLastShiftWithTimeStamp.getAbsolutePath());
                    addFileContentToLog(getLastShiftSigWithTimeStamp.getAbsolutePath());
                }
                File tempFile = getTempFile(STATE_getLastShift, requestTimeStamp);
                FileUtils.renameFile(g, getLastShiftWithTimeStamp, tempFile);
                onGetLastShiftReq(tempFile, requestTimeStamp);
                return;
            }

            if (Thread.currentThread().isInterrupted()) {
                return;
            }

            requestTimeStamp = Fn.getRequestFileTimeStamp(new File(STATE_getSettingsInfo));
            if (requestTimeStamp > 0) {
                Logger.trace(TAG, "STATE_getSettingsInfo detected");
                Fn.deleteFilesWithTimeStampMtp(g, new File(STATE_getSettingsInfo));
                Fn.deleteOldFiles(g, FILES_FOR_DELETE_GET_SETTINGS, requestTimeStamp);
                File getSettingsWithTimeStamp = Fn.getFileWithTimeStamp(new File(STATE_getSettings), requestTimeStamp);
                File getSettingsSigWithTimeStamp = Fn.getFileWithTimeStamp(new File(STATE_getSettingsSig), requestTimeStamp);
                if (enableLogFileContent) {
                    addFileContentToLog(getSettingsWithTimeStamp.getAbsolutePath());
                    addFileContentToLog(getSettingsSigWithTimeStamp.getAbsolutePath());
                }
                File tempFile = getTempFile(STATE_getSettings, requestTimeStamp);
                FileUtils.renameFile(g, getSettingsWithTimeStamp, tempFile);
                onGetSettingsReq(tempFile, requestTimeStamp);
                return;
            }

            if (Thread.currentThread().isInterrupted()) {
                return;
            }

            requestTimeStamp = Fn.getRequestFileTimeStamp(new File(STATE_getTimeInfo));
            if (requestTimeStamp > 0) {
                Logger.trace(TAG, "STATE_getTimeInfo detected");
                Fn.deleteFilesWithTimeStampMtp(g, new File(STATE_getTimeInfo));
                Fn.deleteOldFiles(g, FILES_FOR_DELETE_GET_TIME, requestTimeStamp);
                File getTimeWithTimeStamp = Fn.getFileWithTimeStamp(new File(STATE_getTime), requestTimeStamp);
                File getTimeSigWithTimeStamp = Fn.getFileWithTimeStamp(new File(STATE_getTimeSig), requestTimeStamp);
                if (enableLogFileContent) {
                    addFileContentToLog(getTimeWithTimeStamp.getAbsolutePath());
                    addFileContentToLog(getTimeSigWithTimeStamp.getAbsolutePath());
                }
                File tempFile = getTempFile(STATE_getTime, requestTimeStamp);
                FileUtils.renameFile(g, getTimeWithTimeStamp, tempFile);
                onGetTimeReq(tempFile, requestTimeStamp);
                return;
            }

            if (Thread.currentThread().isInterrupted()) {
                return;
            }

            requestTimeStamp = Fn.getRequestFileTimeStamp(new File(STATE_setSettingsInfo));
            if (requestTimeStamp > 0) {
                Logger.trace(TAG, "STATE_setSettingsInfo detected");
                Fn.deleteFilesWithTimeStampMtp(g, new File(STATE_setSettingsInfo));
                Fn.deleteOldFiles(g, FILES_FOR_DELETE_SET_SETTINGS, requestTimeStamp);
                File setSettingsWithTimeStamp = Fn.getFileWithTimeStamp(new File(STATE_setSettings), requestTimeStamp);
                File setSettingsSigWithTimeStamp = Fn.getFileWithTimeStamp(new File(STATE_setSettingsSig), requestTimeStamp);
                if (enableLogFileContent) {
                    addFileContentToLog(setSettingsWithTimeStamp.getAbsolutePath());
                    addFileContentToLog(setSettingsSigWithTimeStamp.getAbsolutePath());
                }
                File tempFile = getTempFile(STATE_setSettings, requestTimeStamp);
                FileUtils.renameFile(g, setSettingsWithTimeStamp, tempFile);
                onSetSettingsReq(tempFile, requestTimeStamp);
                return;
            }

            if (Thread.currentThread().isInterrupted()) {
                return;
            }

            requestTimeStamp = Fn.getRequestFileTimeStamp(new File(STATE_setTimeInfo));
            if (requestTimeStamp > 0) {
                Logger.trace(TAG, "STATE_setTimeInfo detected");
                // запомним время обнаружения события, чтобы потом приплюсовать
                // задержку
                long eventDetectTimeStamp = System.currentTimeMillis();
                Fn.deleteFilesWithTimeStampMtp(g, new File(STATE_setTimeInfo));
                Fn.deleteOldFiles(g, FILES_FOR_DELETE_SET_TIME, requestTimeStamp);
                File setTimeWithTimeStamp = Fn.getFileWithTimeStamp(new File(STATE_setTime), requestTimeStamp);
                File setTimeSigWithTimeStamp = Fn.getFileWithTimeStamp(new File(STATE_setTimeSig), requestTimeStamp);
                if (enableLogFileContent) {
                    addFileContentToLog(setTimeWithTimeStamp.getAbsolutePath());
                    addFileContentToLog(setTimeSigWithTimeStamp.getAbsolutePath());
                }
                File tempFile = getTempFile(STATE_setTime, requestTimeStamp);
                FileUtils.renameFile(g, setTimeWithTimeStamp, tempFile);
                onSetTimeReq(eventDetectTimeStamp, tempFile, requestTimeStamp);
                return;
            }

            if (Thread.currentThread().isInterrupted()) {
                return;
            }

            requestTimeStamp = Fn.getRequestFileTimeStamp(new File(STATE_getBackupInfo));
            if (requestTimeStamp > 0) {
                Logger.trace(TAG, "STATE_getBackupInfo detected");
                Fn.deleteFilesWithTimeStampMtp(g, new File(STATE_getBackupInfo));
                Fn.deleteOldFiles(g, FILES_FOR_DELETE_GET_BACKUP, requestTimeStamp);
                File getBackupWithTimeStamp = Fn.getFileWithTimeStamp(new File(STATE_getBackup), requestTimeStamp);
                File getBackupSigWithTimeStamp = Fn.getFileWithTimeStamp(new File(STATE_getBackupSig), requestTimeStamp);
                if (enableLogFileContent) {
                    addFileContentToLog(getBackupWithTimeStamp.getAbsolutePath());
                    addFileContentToLog(getBackupSigWithTimeStamp.getAbsolutePath());
                }
                File tempFile = getTempFile(STATE_getBackup, requestTimeStamp);
                FileUtils.renameFile(g, getBackupWithTimeStamp, tempFile);
                onBackupReqDetected(tempFile, requestTimeStamp);
                return;
            }

            if (Thread.currentThread().isInterrupted()) {
                return;
            }

            requestTimeStamp = Fn.getRequestFileTimeStamp(new File(SOFTWARE_newVersionInfo));
            if (requestTimeStamp > 0) {
                Logger.trace(TAG, "SOFTWARE_newVersionInfo detected");
                Fn.deleteFilesWithTimeStampMtp(g, new File(SOFTWARE_newVersionInfo));
                Fn.deleteOldFiles(g, FILES_FOR_DELETE_NEW_VERSION, requestTimeStamp);
                File newVersionWithTimeStamp = Fn.getFileWithTimeStamp(new File(SOFTWARE_newVersion), requestTimeStamp);
                File newVersionSigWithTimeStamp = Fn.getFileWithTimeStamp(new File(SOFTWARE_newVersionSig), requestTimeStamp);
                if (enableLogFileContent) {
                    addFileContentToLog(newVersionSigWithTimeStamp.getAbsolutePath());
                }
                File tempFile = getTempFile(SOFTWARE_newVersion, requestTimeStamp);
                FileUtils.renameFile(g, newVersionWithTimeStamp, tempFile);
                onNewVersionDetected(tempFile, requestTimeStamp);
                return;
            }

            if (Thread.currentThread().isInterrupted()) {
                return;
            }

            requestTimeStamp = Fn.getRequestFileTimeStamp(new File(SOFTWARE_newVersionSettingsInfo));
            if (requestTimeStamp > 0) {
                Logger.trace(TAG, "SOFTWARE_newVersionSettingsInfo detected");
                Fn.deleteFilesWithTimeStampMtp(g, new File(SOFTWARE_newVersionSettingsInfo));
                Fn.deleteOldFiles(g, FILES_FOR_DELETE_NEW_VERSION_SETTINGS, requestTimeStamp);
                File newVersionSettingsWithTimeStamp = Fn.getFileWithTimeStamp(new File(SOFTWARE_newVersionSettings), requestTimeStamp);
                File newVersionSettingsSigWithTimeStamp = Fn.getFileWithTimeStamp(new File(SOFTWARE_newVersionSettingsSig), requestTimeStamp);
                if (enableLogFileContent) {
                    addFileContentToLog(newVersionSettingsWithTimeStamp.getAbsolutePath());
                    addFileContentToLog(newVersionSettingsSigWithTimeStamp.getAbsolutePath());
                }
                File tempFile = getTempFile(SOFTWARE_newVersionSettings, requestTimeStamp);
                FileUtils.renameFile(g, newVersionSettingsWithTimeStamp, tempFile);
                onCommonSettingsDetected(tempFile, requestTimeStamp);
                return;
            }

            if (Thread.currentThread().isInterrupted()) {
                return;
            }
        }

        private void addFileContentToLog(String filePath) {
            Logger.info(getClass(), FileUtils.getFileContent(filePath));
        }

        /**
         * Событие, ARM запросил State.
         */
        private void onGetStateReq(long requestTimeStamp) {
            if (listener != null)
                listener.onGetStateReq(requestTimeStamp);
        }

        /**
         * Событие, ARM собщил, что он подключился к ПТК.
         */
        private void onARMConnectedReq(File dataFile, long requestTimeStamp) {
            EcpSigData eds = getDataForCheck(dataFile, Fn.getFileWithTimeStamp(new File(STATE_ArmConnectedSig), requestTimeStamp));
            boolean isEcpValid = eds.isDataOk && edsManagerWrapper.verifySignBlocking(eds.data, eds.sig, eds.keyNumber).getState() == CheckSignResultState.VALID;
            onARMConnectedReqReady(isEcpValid, dataFile, requestTimeStamp);
        }

        private void onARMConnectedReqReady(boolean isEcpOk, File dataFile, long requestTimeStamp) {

            Error error = new Error();
            PtkStartSyncRequest ptkStartSyncRequest = null;

            // В будущем выпиилсть в релизе 3 когда не останется касс с датаконтрактами ниже 79
            //http://agile.srvdev.ru/browse/CPPKPP-42823
            boolean oldRequest = !dataFile.exists();

            if (!oldRequest) {
                try {
                    JSONObject reqJson = new JSONObject(loadJSONFromFile(dataFile));
                    Logger.trace(TAG, reqJson.toString());
                    ptkStartSyncRequest = new PtkStartSyncRequestLoader().fromJson(reqJson);
                } catch (JSONException e) {
                    error = new Error(Error.JSONException, e.getMessage());
                } catch (IOException e) {
                    error = new Error(Error.IOException, e.getMessage());
                }

                FileUtils.deleteFileMtp(g, dataFile);
                Fn.deleteFilesWithTimeStampMtp(g, new File(STATE_ArmConnectedSig));
            }

            if (listener != null)
                listener.onArmConnected(oldRequest, isEcpOk, error, ptkStartSyncRequest, requestTimeStamp);
        }

        /**
         * Событие, ARM собщил, что он завершил действия по синхронизации и отключается от ПТК.
         */
        private void onArmDisconnected(long requestTimeStamp) {
            if (listener != null)
                listener.onArmDisconnected(requestTimeStamp);
        }

        /**
         * Событие, ARM собщил, что синхронизация успешно завершена
         */
        private void onSyncFinished(long requestTimeStamp) {
            if (listener != null)
                listener.onSyncFinishedDetected(requestTimeStamp);
        }

        /**
         * Событие, когда нужно подергать sft для инициализации ключей, подпись не
         * проверяем, т.к. это делается на ПТК с неактивированной библиотекой SFT
         */
        private void onTransmissionComplete(File inTarGzFile, File jsonToDeleteFile, long requestTimeStamp) {
            if (listener != null)
                listener.onTransmissionCompleteDetected(inTarGzFile, jsonToDeleteFile, requestTimeStamp);
        }

        /**
         * Формирует данные в byte[] для запуска проверки подписи, и запускает
         * проверку
         */
        private void onGetEventsReq(final File dataFile, long requestTimeStamp) {
            EcpSigData esd = getDataForCheck(dataFile, Fn.getFileWithTimeStamp(new File(KPP_getEventsReqSig), requestTimeStamp));
            boolean isEcpValid = esd.isDataOk && edsManagerWrapper.verifySignBlocking(esd.data, esd.sig, esd.keyNumber).getState() == CheckSignResultState.VALID;
            onGetEventsReqReady(isEcpValid, dataFile, requestTimeStamp);
        }

        /**
         * Обработка результата проверки подписи у файлa getEvent_req
         *
         * @param isEcpOk
         * @param dataFile
         */
        private void onGetEventsReqReady(boolean isEcpOk, File dataFile, long requestTimeStamp) {
            Error error = new Error();

            GetEventsReq getEventsReq = null;

            try {
                if (!isEcpOk)
                    error.code = Error.ECP;
                JSONObject obj = new JSONObject(loadJSONFromFile(dataFile));

                getEventsReq = new GetEventsReqLoader().fromJson(obj);
            } catch (JSONException e) {
                error = new Error(Error.JSONException, e.getMessage());
            } catch (IOException e) {
                error = new Error(Error.IOException, e.getMessage());
            }

            FileUtils.deleteFileMtp(g, dataFile);
            Fn.deleteFilesWithTimeStampMtp(g, new File(KPP_getEventsReqSig));

            if (listener != null)
                listener.onGetEventsReq(getEventsReq, error, requestTimeStamp);
        }

        private void onSetSettingsReq(final File dataFile, long requestTimeStamp) {
            EcpSigData esd = getDataForCheck(dataFile, Fn.getFileWithTimeStamp(new File(STATE_setSettingsSig), requestTimeStamp));
            boolean isEcpValid = esd.isDataOk && edsManagerWrapper.verifySignBlocking(esd.data, esd.sig, esd.keyNumber).getState() == CheckSignResultState.VALID;
            onSetSettingsReqReady(isEcpValid, dataFile, requestTimeStamp);
        }

        private void onSetSettingsReqReady(boolean isEcpOk, File dataFile, long requestTimeStamp) {

            Error error = new Error();
            if (!isEcpOk)
                error.code = Error.ECP;
            PrivateSettings settings = new PrivateSettings(g.getPrivateSettingsHolder().get());
            try {
                JSONObject overrides = new JSONObject(loadJSONFromFile(dataFile));
                Logger.trace(TAG, overrides.toString());
                JSONObject settingsJson = overrides.getJSONObject("Settings");
                settings = PrivateSettingsUtils.getInstance().parseFromJson(settingsJson);
            } catch (JSONException e) {
                error = new Error(Error.INCORRECT_PRIVATE_CONFIG, e.getMessage());
            } catch (IOException e) {
                error = new Error(Error.INCORRECT_PRIVATE_CONFIG, e.getMessage());
            } catch (IllegalArgumentException e) {
                error = new Error(Error.INCORRECT_PRIVATE_CONFIG, e.getMessage());
            }

            FileUtils.deleteFileMtp(g, dataFile);
            Fn.deleteFilesWithTimeStampMtp(g, new File(STATE_setSettingsSig));

            if (listener != null) {
                listener.onSetSettingsReq(settings, error, requestTimeStamp);
            }
        }

        private void onSetTimeReq(final long eventDetectTimeStamp, final File dataFile, long requestTimeStamp) {
            EcpSigData esd = getDataForCheck(dataFile, Fn.getFileWithTimeStamp(new File(STATE_setTimeSig), requestTimeStamp));
            boolean isEcpValid = esd.isDataOk && edsManagerWrapper.verifySignBlocking(esd.data, esd.sig, esd.keyNumber).getState() == CheckSignResultState.VALID;
            onSetTimeReqReady(isEcpValid, eventDetectTimeStamp, dataFile, requestTimeStamp);
        }

        @SuppressLint("SimpleDateFormat")
        private void onSetTimeReqReady(boolean isEcpOk, long eventDetectTimeStamp, File dataFile, long requestTimeStamp) {
            Error error = new Error();
            if (!isEcpOk)
                error.code = Error.ECP;
            long timeInMilliseconds = 0;
            try {
                // {"currentTime":"2014-10-09 12:16:13"}
                JSONObject obj = new JSONObject(loadJSONFromFile(dataFile));
                String currentTime = obj.getString("CurrentTime");
                String replased = currentTime.replace('T', ' '); // "2014-11-05T01:58:24.236945+04:00";
                timeInMilliseconds = (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(replased)).getTime();
            } catch (JSONException e) {
                error = new Error(Error.JSONException, e.getMessage());
            } catch (IOException e) {
                error = new Error(Error.IOException, e.getMessage());
            } catch (ParseException e) {
                error = new Error(Error.ParseException, e.getMessage());
            }

            FileUtils.deleteFileMtp(g, dataFile);
            Fn.deleteFilesWithTimeStampMtp(g, new File(STATE_setTimeSig));

            if (listener != null)
                listener.onSetTimeReq(timeInMilliseconds, eventDetectTimeStamp, error, requestTimeStamp);
        }

        private void onGetTimeReq(final File dataFile, long requestTimeStamp) {
            EcpSigData esd = getDataForCheck(dataFile, Fn.getFileWithTimeStamp(new File(STATE_getTimeSig), requestTimeStamp));
            boolean isEcpValid = esd.isDataOk && edsManagerWrapper.verifySignBlocking(esd.data, esd.sig, esd.keyNumber).getState() == CheckSignResultState.VALID;
            onGetTimeReqReady(isEcpValid, dataFile, requestTimeStamp);
        }

        private void onGetTimeReqReady(boolean isEcpOk, File dataFile, long requestTimeStamp) {
            Error error = new Error();
            if (!isEcpOk)
                error.code = Error.ECP;

            FileUtils.deleteFileMtp(g, dataFile);
            Fn.deleteFilesWithTimeStampMtp(g, new File(STATE_getTimeSig));

            if (listener != null)
                listener.onGetTimeReq(error, requestTimeStamp);
        }

        private void onGetSettingsReq(final File dataFile, long requestTimeStamp) {
            EcpSigData esd = getDataForCheck(dataFile, Fn.getFileWithTimeStamp(new File(STATE_getSettingsSig), requestTimeStamp));
            boolean isEcpValid = esd.isDataOk && edsManagerWrapper.verifySignBlocking(esd.data, esd.sig, esd.keyNumber).getState() == CheckSignResultState.VALID;
            onGetSettingsReqReady(isEcpValid, dataFile, requestTimeStamp);
        }

        private void onGetSettingsReqReady(boolean isEcpOk, File dataFile, long requestTimeStamp) {
            Error error = new Error();
            if (!isEcpOk)
                error.code = Error.ECP;

            FileUtils.deleteFileMtp(g, dataFile);
            Fn.deleteFilesWithTimeStampMtp(g, new File(STATE_getSettingsSig));

            if (listener != null)
                listener.onGetSettingsReq(error, requestTimeStamp);
        }

        private void onGetBackupReqReady(boolean isEcpOk, File dataFile, long requestTimeStamp) {
            Error error = new Error();
            if (!isEcpOk)
                error.code = Error.ECP;

            FileUtils.deleteFileMtp(g, dataFile);
            Fn.deleteFilesWithTimeStampMtp(g, new File(STATE_getBackupSig));

            if (listener != null)
                listener.onGetBackupReq(error, requestTimeStamp);
        }

        private void onGetLastShiftReq(final File dataFile, long requestTimeStamp) {
            EcpSigData esd = getDataForCheck(dataFile, Fn.getFileWithTimeStamp(new File(STATE_getLastShiftSig), requestTimeStamp));
            boolean isEcpValid = esd.isDataOk && edsManagerWrapper.verifySignBlocking(esd.data, esd.sig, esd.keyNumber).getState() == CheckSignResultState.VALID;
            onGetLastShiftReqReady(isEcpValid, dataFile, requestTimeStamp);
        }

        private void onGetLastShiftReqReady(boolean isEcpOk, File dataFile, long requestTimeStamp) {
            Error error = new Error();
            if (!isEcpOk)
                error.code = Error.ECP;

            FileUtils.deleteFileMtp(g, dataFile);
            Fn.deleteFilesWithTimeStampMtp(g, new File(STATE_getLastShiftSig));

            if (listener != null)
                listener.onGetLastShiftReq(error, requestTimeStamp);
        }

        private void onSecurityDbUpdataReq(final File dataFile, long requestTimeStamp) {
            EcpSigData esd = getDataForCheck(dataFile, Fn.getFileWithTimeStamp(new File(SECURITY_securitySig), requestTimeStamp));
            boolean isEcpValid = esd.isDataOk && edsManagerWrapper.verifySignBlocking(esd.data, esd.sig, esd.keyNumber).getState() == CheckSignResultState.VALID;
            onSecurityDbUpdataReqReady(isEcpValid, dataFile, requestTimeStamp);
        }

        private void onSecurityDbUpdataReqReady(boolean isEcpOk, File dataFile, long requestTimeStamp) {
            Error error = new Error();
            if (!isEcpOk)
                error.code = Error.ECP;

            Fn.deleteFilesWithTimeStampMtp(g, new File(SECURITY_securitySig));

            if (listener != null)
                listener.onSecurityDbUpdataReq(dataFile, error, requestTimeStamp);
        }

        /**
         * Обноружен файл с новой версией ПО, запускаем проверку подписи
         */
        private void onNewVersionDetected(final File dataFile, long requestTimeStamp) {
            EcpSigData esd = getDataForCheck(dataFile, Fn.getFileWithTimeStamp(new File(SOFTWARE_newVersionSig), requestTimeStamp));
            boolean isEcpValid = esd.isDataOk && edsManagerWrapper.verifySignBlocking(esd.data, esd.sig, esd.keyNumber).getState() == CheckSignResultState.VALID;
            onNewVersionReady(isEcpValid, dataFile, requestTimeStamp);
        }

        /**
         * Проверка подписи у файла с новой версией ПО состоялась
         */
        private void onNewVersionReady(boolean isEcpOk, File dataFile, long requestTimeStamp) {
            Error error = new Error();
            if (!isEcpOk)
                error.code = Error.ECP;

            Fn.deleteFilesWithTimeStampMtp(g, new File(SOFTWARE_newVersionSig));

            if (listener != null)
                listener.onNewVersionDetected(dataFile, error, requestTimeStamp);
        }

        /**
         * Обнаружен файл с общими настройками, запускаем проверку подписи
         */
        private void onCommonSettingsDetected(final File dataFile, long requestTimeStamp) {
            EcpSigData esd = getDataForCheck(dataFile, Fn.getFileWithTimeStamp(new File(SOFTWARE_newVersionSettingsSig), requestTimeStamp));
            boolean isEcpValid = esd.isDataOk && edsManagerWrapper.verifySignBlocking(esd.data, esd.sig, esd.keyNumber).getState() == CheckSignResultState.VALID;
            onCommonSettingsDetected(isEcpValid, dataFile, requestTimeStamp);
        }

        /**
         * Проверка подписи у файла с общими настройками состоялась
         */
        private void onCommonSettingsDetected(boolean isEcpOk, File dataFile, long requestTimeStamp) {
            Error error = new Error();

            if (!isEcpOk) {
                error.code = Error.ECP;

                Fn.deleteFilesWithTimeStampMtp(g, new File(SOFTWARE_newVersionSettingsSig));

                if (listener != null)
                    listener.onCommonSettingsDetected(dataFile, error, requestTimeStamp);

                return;
            }

            //Сам объект CommonSettings нас не интересует, т.к. все равно будем заново после обновления ПО пытаться его десериализовать
            //сейчас важно проверить что он вообще похож на настоящий
            //http://agile.srvdev.ru/browse/CPPKPP-43642
            try {
                CommonSettingsUtils.loadCommonSettingsFromXmlFile(dataFile);
            } catch (ParserConfigurationException exception) {
                error = new Error(Error.INCORRECT_COMMON_CONFIG, exception.getMessage());
            } catch (SAXException exception) {
                error = new Error(Error.INCORRECT_COMMON_CONFIG, exception.getMessage());
            } catch (IOException exception) {
                error = new Error(Error.INCORRECT_COMMON_CONFIG, exception.getMessage());
            } catch (IllegalArgumentException exception) {
                // Обобщение для NumberFormatException, поймали при парсинге allowedStationCodes с пробелом в конце
                error = new Error(Error.INCORRECT_COMMON_CONFIG, exception.getMessage());
            }

            FileUtils.deleteFileMtp(g, new File(SOFTWARE_newVersionSettingsSig));

            if (listener != null)
                listener.onCommonSettingsDetected(dataFile, error, requestTimeStamp);
        }

        /**
         * Проверка подписи у файла с новой версией НСИ
         */
        private void onRdsDbUpdataReq(final File dataFile, long requestTimeStamp) {

            Logger.info(Exchange.class, "Начинаем обновление RDS базы");

            EcpSigData esd = getDataForCheck(dataFile, Fn.getFileWithTimeStamp(new File(RDS_RdsSig), requestTimeStamp));
            boolean isEcpValid = esd.isDataOk && edsManagerWrapper.verifySignBlocking(esd.data, esd.sig, esd.keyNumber).getState() == CheckSignResultState.VALID;
            onRdsDbUpdataReqReady(isEcpValid, dataFile, requestTimeStamp);
        }

        /**
         * Проверка подписи у файла с новой версией НСИ завершена
         */
        private void onRdsDbUpdataReqReady(boolean isSignOk, File dataFile, long requestTimeStamp) {

            Logger.info(Exchange.class, "Проверка подписи для RDS завершена с результатом - " + isSignOk);

            Error error = new Error();
            if (!isSignOk) {
                error.code = Error.ECP;
            }

            Fn.deleteFilesWithTimeStampMtp(g, new File(RDS_RdsSig));

            if (listener != null)
                listener.onRdsDbUpdataReq(dataFile, error, requestTimeStamp);
        }

        private void onBackupReqDetected(final File dataFile, long requestTimeStamp) {
            EcpSigData esd = getDataForCheck(dataFile, Fn.getFileWithTimeStamp(new File(STATE_getBackupSig), requestTimeStamp));
            boolean isEcpValid = esd.isDataOk && edsManagerWrapper.verifySignBlocking(esd.data, esd.sig, esd.keyNumber).getState() == CheckSignResultState.VALID;
            onGetBackupReqReady(isEcpValid, dataFile, requestTimeStamp);
        }
    }


    public static class Fn {

        public static void clearFolders(Globals globals, long timeStamp) {
            Logger.trace(TAG, "clearingFolders start");
            Logger.trace(TAG, "clearingFolders " + STATE);
            clearOldFilesAndFolders(globals, new File(STATE), timeStamp, false);
            if (Thread.currentThread().isInterrupted()) {
                Logger.trace(TAG, "clearingFolders interrupted");
                return;
            }
            Logger.trace(TAG, "clearingFolders " + SOFTWARE);
            clearOldFilesAndFolders(globals, new File(SOFTWARE), timeStamp, false);
            if (Thread.currentThread().isInterrupted()) {
                Logger.trace(TAG, "clearingFolders interrupted");
                return;
            }
            Logger.trace(TAG, "clearingFolders " + RDS);
            clearOldFilesAndFolders(globals, new File(RDS), timeStamp, false);
            if (Thread.currentThread().isInterrupted()) {
                Logger.trace(TAG, "clearingFolders interrupted");
                return;
            }
            Logger.trace(TAG, "clearingFolders " + CANCEL);
            clearOldFilesAndFolders(globals, new File(CANCEL), timeStamp, false);
            if (Thread.currentThread().isInterrupted()) {
                Logger.trace(TAG, "clearingFolders interrupted");
                return;
            }
            Logger.trace(TAG, "clearingFolders " + KPP);
            clearOldFilesAndFolders(globals, new File(KPP), timeStamp, false);
            if (Thread.currentThread().isInterrupted()) {
                Logger.trace(TAG, "clearingFolders interrupted");
                return;
            }
            Logger.trace(TAG, "clearingFolders " + SECURITY);
            clearOldFilesAndFolders(globals, new File(SECURITY), timeStamp, false);
            if (Thread.currentThread().isInterrupted()) {
                Logger.trace(TAG, "clearingFolders interrupted");
                return;
            }
            Logger.trace(TAG, "clearingFolders " + new File(SFT));
            File sftDir = new File(SFT);
            File[] files = sftDir.listFiles();
            if (files != null) {
                for (File f : files) {
                    if (f.isDirectory()) {
                        if ("lic".equals(f.getName()) || "out".equals(f.getName())) {
                            Logger.trace(TAG, "clearingFolders skip " + f.getName());
                            continue;
                        }
                    }
                    clearOldFilesAndFolders(globals, f, timeStamp, true);
                }
            }
            Logger.trace(TAG, "clearingFolders end");
        }

        public static void deleteOldFiles(Globals globals, String[] files, long timeStamp) {
            if (files != null) {
                for (String file : files) {
                    File[] filesWithTimestamp = getFilesWithTimeStamp(new File(file));
                    if (filesWithTimestamp != null) {
                        for (File fileWithTimestamp : filesWithTimestamp) {
                            if (getFileTimeStamp(fileWithTimestamp) < timeStamp) {
                                fileWithTimestamp.delete();
                                Logger.trace(TAG, "Old file deleted: " + fileWithTimestamp.getAbsolutePath());
                                FileUtils.sendFileDeletedMtp(globals, fileWithTimestamp);
                            }
                        }
                    }
                }
            }
        }

        private static void clearOldFilesAndFolders(Globals globals, File dirOrFile, long timeStamp, boolean deleteRoot) {

            File[] files = !deleteRoot && dirOrFile.isDirectory() ? dirOrFile.listFiles() : new File[]{dirOrFile};
            if (files != null) {
                StringBuilder stringBuilder = new StringBuilder();
                for (File f : files) {
                    stringBuilder.delete(0, stringBuilder.length());
                    stringBuilder.append("clearOldFilesAndFolders ");
                    stringBuilder.append(f.getAbsolutePath());
                    stringBuilder.append(" isDir=").append(f.isDirectory());

                    // До удаления файлов, иначе время мизменения папки станет текущим
                    long lastModified = f.lastModified();
                    if (f.isDirectory()) {
                        clearOldFilesAndFolders(globals, f, timeStamp, false);
                    }
                    if (lastModified < timeStamp) {
                        f.delete();
                        FileUtils.sendFileDeletedMtp(globals, f);
                        stringBuilder.append(" deleted as old");
                    } else {
                        stringBuilder.append(" skipped as new");
                    }
                    stringBuilder.append(" (").append(lastModified).append(",").append(timeStamp).append(")");
                    Logger.trace(TAG, stringBuilder.toString());
                }
            }
        }

        public static File getFileWithTimeStamp(File file, long requestTimeStamp) {
            String absolutePath = file.getAbsolutePath();
            int indexOfPoint = absolutePath.indexOf('.');
            String absolutePathWithoutExtension = absolutePath.substring(0, indexOfPoint);
            String extension = absolutePath.substring(indexOfPoint, absolutePath.length());
            String resultAbsolutePath = absolutePathWithoutExtension + "_" + requestTimeStamp + extension;
            return new File(resultAbsolutePath);
        }

        public static File getFileWithTimeStampAndIndex(File file, long requestTimeStamp, long index) {
            File fileWithTimeStamp = getFileWithTimeStamp(file, requestTimeStamp);
            File fileWithTimeStampAndIndex = index == 0 ? fileWithTimeStamp : getFileWithTimeStamp(fileWithTimeStamp, index);
            return fileWithTimeStampAndIndex;
        }

        private static long getRequestFileTimeStamp(File requestFile) {

            File[] files = getFilesWithTimeStamp(requestFile);

            long timeStamp = 0;
            if (files != null) {
                for (File file : files) {
                    long newTimeStamp = getFileTimeStamp(file);
                    if (newTimeStamp > timeStamp) {
                        timeStamp = newTimeStamp;
                    }
                }
            }

            return timeStamp;
        }

        private static long getFileTimeStamp(File file) {
            String fileName = file.getName();
            int indexOfUnderscore = fileName.lastIndexOf('_');
            int indexOfPoint = fileName.indexOf('.');
            String newTimeStampStr = fileName.substring(indexOfUnderscore + 1, indexOfPoint);
            long newTimeStamp = 0;
            try {
                newTimeStamp = Long.valueOf(newTimeStampStr);
            } catch (NumberFormatException e) {
                Logger.trace(TAG, "Invalid timestamp in file: " + file.getAbsolutePath());
            }
            return newTimeStamp;
        }

        private static File[] getFilesWithTimeStamp(File requestFile) {
            File parent = requestFile.getParentFile();
            int indexOfPoint = requestFile.getName().indexOf(".");
            if (indexOfPoint < 0) {
                indexOfPoint = requestFile.getName().length();
            }
            String requestFileName = requestFile.getName().substring(0, indexOfPoint);
            String extension = requestFile.getName().substring(indexOfPoint, requestFile.getName().length());
            //добавим _ иначе в фильтр попадают все файлы у которых начало такое же например newVersion.info и newVersionSettings.info
            File[] files = parent.listFiles((dir, filename) -> filename.startsWith(requestFileName + "_") && filename.endsWith(extension));
            return files;
        }

        private static void deleteFilesWithTimeStampMtp(Globals globals, File requestFile) {

            File[] files = getFilesWithTimeStamp(requestFile);

            if (files != null) {
                for (File file : files) {
                    FileUtils.deleteFileMtp(globals, file);
                }
            }

        }
    }

}
