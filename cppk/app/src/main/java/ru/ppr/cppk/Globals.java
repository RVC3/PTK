package ru.ppr.cppk;

import android.content.Context;
import android.os.Build;
import android.support.multidex.MultiDexApplication;

import com.facebook.stetho.Stetho;

import java.io.File;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import fr.coppernic.sdk.keyremapper.KeyRemap;
import ru.ppr.core.helper.Toaster;
import ru.ppr.core.ui.helper.crashreporter.CrashReporter;
import ru.ppr.core.ui.helper.crashreporter.FileLoggerCrashListener;
import ru.ppr.cppk.data.summary.RecentStationsStatistics;
import ru.ppr.cppk.db.LocalDaoSession;
import ru.ppr.cppk.devtools.SQLiteConnectionService;
import ru.ppr.cppk.di.Dagger;
import ru.ppr.cppk.di.Di;
import ru.ppr.cppk.entity.settings.PrivateSettings;
import ru.ppr.cppk.export.Broadcasts;
import ru.ppr.cppk.export.Exchange;
import ru.ppr.cppk.export.ExportUtils;
import ru.ppr.cppk.export.Response;
import ru.ppr.cppk.export.ServiceUtils;
import ru.ppr.cppk.helpers.AnrLogWriter;
import ru.ppr.cppk.helpers.AppToaster;
import ru.ppr.cppk.helpers.DropBoxLogWriter;
import ru.ppr.cppk.helpers.EdsManagerConfigSyncronizer;
import ru.ppr.cppk.helpers.EdsPathsMover;
import ru.ppr.cppk.helpers.FeedbackHelper;
import ru.ppr.cppk.helpers.FeedbackPathsCleaner;
import ru.ppr.cppk.helpers.FilePathProvider;
import ru.ppr.cppk.helpers.PaperUsageCounter;
import ru.ppr.cppk.helpers.SchedulersCPPK;
import ru.ppr.cppk.helpers.SharedPrefsCleaner;
import ru.ppr.cppk.logic.ShiftAlarmManager;
import ru.ppr.cppk.logic.ShiftManager;
import ru.ppr.cppk.managers.DiNetworkManagerModule;
import ru.ppr.cppk.managers.FileCleaner;
import ru.ppr.cppk.managers.PosManager;
import ru.ppr.cppk.managers.ScreenLockManager;
import ru.ppr.cppk.managers.ScreenLockManagerReal;
import ru.ppr.cppk.managers.ScreenLockManagerStub;
import ru.ppr.cppk.pos.PosType;
import ru.ppr.cppk.settings.SharedPreferencesUtils;
import ru.ppr.cppk.utils.PlaySound;
import ru.ppr.database.Database;
import ru.ppr.ipos.IPos;
import ru.ppr.logger.Logger;
import ru.ppr.nsi.NsiDaoSession;
import ru.ppr.security.SecurityDaoSession;
import ru.ppr.shtrih.LogbackConfig;
import rx.Completable;
import rx.Observable;
import rx.Subscriber;

public class Globals extends MultiDexApplication {

    protected static final String TAG = Logger.makeLogTag(Globals.class);

    // region Di
    @Inject
    EdsManagerConfigSyncronizer edsManagerConfigSyncronizer;
    @Inject
    SQLiteConnectionService sqLiteConnectionService;
    @Inject
    RecentStationsStatistics recentStationsStatistics;
    @Inject
    FileCleaner fileCleaner;
    @Inject
    FilePathProvider filePathProvider;
    // endregion

    @Deprecated
    public static Globals getInstance() {
        return Di.INSTANCE.getApp();
    }

    /**
     * Объект для работы а АРМ загрузки данных
     */
    private static volatile Exchange exchange = null;

    private static volatile Response response = null;
    private ScreenLockManager screenLockManager;
    private Toaster toaster;

    /**
     * Флаг запущенности окна синхронизации с ARM
     */
    private volatile boolean isSyncNow = false;

    public boolean isAllInited() {
        return isAllInited;
    }

    public void setAllInited(boolean allInited) {
        isAllInited = allInited;
    }

    /**
     * Флаг прохода через инициализацию всего и вся на Splash (бывает что-то случается и приложение стартует не проходя через Splash сразу с CommonMenuActivity)
     */
    private volatile boolean isAllInited = false;

    private static IPos terminal;

    public void setIsSyncNow(boolean isSyncNow) {
        this.isSyncNow = isSyncNow;
    }

    public boolean getIsSyncNow() {
        return isSyncNow;
    }

    /**
     * Объект для работы с уведомлениями. Такой велосипед чтобы можно было сохранять очередь.
     */
    private static volatile Broadcasts broadcasts = null;

    // Sounds
    private PlaySound playSound = null;

    private final Object LOCK = new Object();

    /**
     * объект уведомлений о ходе сихронизации, для логов.
     *
     * @return
     */
    public Broadcasts getBroadcasts() {
        Broadcasts localInstance = broadcasts;
        if (localInstance == null) {
            synchronized (LOCK) {
                localInstance = broadcasts;
                if (localInstance == null) {
                    broadcasts = localInstance = new Broadcasts();
                }
            }
        }
        return localInstance;
    }

    public Exchange getExchange() {
        Exchange localInstance = exchange;
        if (localInstance == null) {
            synchronized (Exchange.class) {
                localInstance = exchange;
                if (localInstance == null) {
                    exchange = localInstance = new Exchange(Globals.this, di().getEdsManager(), di().getEdsManagerWrapper());
                }
            }
        }
        return localInstance;
    }

    @Deprecated
    public Database getLocalDb() {
        return getLocalDaoSession().getLocalDb();
    }

    /**
     * Переинициализирует обертку для доступа к локальной БД.
     */
    @Deprecated
    public void resetLocalDaoSession() {
        di().localDbManager().resetDaoSession();
    }

    /**
     * Переинициализирует обертку для доступа к НСИ БД.
     * Необходимо вызывать, если подключение к БД было закрыто
     */
    @Deprecated
    public void resetNsiDaoSession() {
        di().nsiDbManager().resetDaoSession();
    }

    /**
     * Переинициализирует обертку для доступа к Security БД.
     * Необходимо вызывать, если подключение к БД было закрыто
     */
    @Deprecated
    public void resetSecurityDaoSession() {
        di().securityDbManager().resetDaoSession();
    }

    @Deprecated
    public Database getNsiDb() {
        return getNsiDaoSession().getNsiDb();
    }

    public Database getSecurityDb() {
        return getSecurityDaoSession().getSecurityDb();
    }

    public PlaySound getPlaySound() {
        return playSound;
    }

    @Override
    public void onCreate() {
        // Логируем информацию о факте запуска приложения
        Logger.info(TAG, "Запуск приложения CPPK " + BuildConfig.VERSION_NAME);
        Logger.info(TAG, "Firmware version: " + Build.DISPLAY);
        // Передаем вызов родительскому классу
        super.onCreate();
        // Создаем бекапы ANR и системных логов
        createAnrBackupWithoutCallback();
        createDropBoxBackupWithoutCallback();

        toaster = new AppToaster(this);

        final ServiceUtils serviceUtils = ServiceUtils.get();
        serviceUtils.init(this);

        DiNetworkManagerModule diNetworkManagerModule = new DiNetworkManagerModule();
        di().init(this, diNetworkManagerModule);

        // Проверяем факт обновления ПО и выполняем необходимые действия
        Dagger.appComponent().appVersionUpdateRegister().checkForUpdate();

        // Внедряем зависимости
        Dagger.appComponent().inject(this);

        initExternalSQLiteTool();
        // Устанавливаем текущую конфигурацию для EdsManager'а
        edsManagerConfigSyncronizer.sync();

        screenLockManager = Dagger.appComponent().commonSettingsStorage().get().isAutoBlockingEnabled() ? new ScreenLockManagerReal(this, Dagger.appComponent().commonSettingsStorage().get().getAutoBlockingTimeout()) : new ScreenLockManagerStub();

        // Create playsound instance
        playSound = new PlaySound(getApplicationContext());

        ExportUtils.createExchangeFolders(Globals.this);
        fileCleaner.clearDirRecursive(new File(Exchange.DIR), TimeUnit.HOURS.toMillis(1));
        fileCleaner.clearDir(filePathProvider.getBackupsRestoreDir(), 1);
        fileCleaner.clearDir(filePathProvider.getBackupsReplaceDir(), 1);
        fileCleaner.clearDir(filePathProvider.getBackupsDir(), SharedPreferencesUtils.getMaxFileCountInBackupDir(this));
        fileCleaner.clearDir(new File(PathsConstants.FEEDBACK), FeedbackHelper.REPORT_MAX_SCREENS_COUNT);
        fileCleaner.clearDir(new File(PathsConstants.LOG_FATALS), FileLoggerCrashListener.DEFAULT_MAX_FILE_COUNT);
        fileCleaner.clearDir(new File(PathsConstants.LOG_ANR), AnrLogWriter.MAX_LOG_COUNT);
        fileCleaner.clearDir(new File(PathsConstants.LOG_DROPBOX), DropBoxLogWriter.MAX_LOG_COUNT);
        fileCleaner.clearDir(filePathProvider.getInfotecsLogsDir(), 10);
        fileCleaner.clearDir(filePathProvider.getShtrihLogsDir(), LogbackConfig.MAX_FILE_COUNT);
        fileCleaner.clearDir(new File(PathsConstants.LOG_ZEBRA), 10);
        fileCleaner.clearDir(new File(PathsConstants.PRINTER), 2);
        // Пока не запускаем
        // Нужна проверка устройства, иначе удалим все изображения на своих телефонах
        //FileUtils2.clearDir(filePathProvider.getSystemExternalPicturesDir(), null);

        // создаем стандартные настройки
        SharedPreferencesUtils.createDefaultSettings(getApplicationContext());

        //Устанавливаем время откючения дисплея, значение берется из общих настроек
        setScreenOffTimeout();

        serviceUtils.stopARMservice("Globals.onCreate");

        //startShiftAlarms();

        disablePeriferal();

        //инициализируем дравйер терминала по последним настройкам
        updatePosTerminalWithoutCallback(this, SharedPreferencesUtils.getPosMacAddress(this), SharedPreferencesUtils.getPosPort(this), SharedPreferencesUtils.getPosTerminalType(this));

        //сбросим ремап хардварных кнопок
        KeyRemap.getInstance().removeAllMapping(this);

        new FeedbackPathsCleaner(this).deleteOldDir();

        // Инициализируем статистику по станциям.
        recentStationsStatistics.init();

        // Перемещение папок ЭЦП
        new EdsPathsMover(this, di().getEdsManager().getEdsDirs()).moveDirs();

        // Очистка SharedPreferences от устаревших ключей
        new SharedPrefsCleaner(this).deleteUnusedKeys();

        Stetho.initializeWithDefaults(this);
    }

    private Di di() {
        return Di.INSTANCE;
    }

    @Deprecated
    public void setScreenOffTimeout() {
        long timeout = TimeUnit.SECONDS.toMillis(Dagger.appComponent().commonSettings().getScreenOffTimeout());
        Dagger.appComponent().screenManager().setScreenOffTimeout((int) timeout);
    }

    public void disablePeriferal() {
        Logger.info(Globals.class, "Выключаем WiFi или мобильные данные...");
        di().networkManager().disable(null);

        // Не выключаем блютус, т.к. если пытаться выключать включенный блютус,
        // то ЭЦП не может найти лицензии с выключеным блютусом.
        // Так происходит только если выключать включенный блютус
        //задача на эту тему: http://agile.srvdev.ru/browse/CPPKPP-27967
    }

    public PaperUsageCounter getPaperUsageCounter() {
        return Dagger.appComponent().paperUsageCounter();
    }

    @Deprecated
    public ShiftAlarmManager getShiftAlarmManager() {
        return di().getShiftAlarmManager();
    }

    @Deprecated
    public ShiftManager getShiftManager() {
        return di().getShiftManager();
    }

    @Deprecated
    public PosManager getPosManager() {
        return di().getPosManager();
    }

    public ScreenLockManager getScreenLockManager() {
        return screenLockManager;
    }

    @Deprecated
    public Holder<PrivateSettings> getPrivateSettingsHolder() {
        return di().getPrivateSettings();
    }

    @Deprecated
    public CrashReporter getCrashReporter() {
        return di().getCrashReporter();
    }

    @Deprecated
    public LocalDaoSession getLocalDaoSession() {
        return di().getDbManager().getLocalDaoSession().get();
    }

    @Deprecated
    public NsiDaoSession getNsiDaoSession() {
        return di().getDbManager().getNsiDaoSession().get();
    }

    @Deprecated
    public SecurityDaoSession getSecurityDaoSession() {
        return di().getDbManager().getSecurityDaoSession().get();
    }

    public Toaster getToaster() {
        return toaster;
    }

    @Override
    public void onTerminate() {
        // user kills
        ServiceUtils.get().stopARMservice("Globals.onTerminate()");
        Logger.info(TAG, "onTerminate()");
        super.onTerminate();
    }

    /**
     * Возвращает объект для создания ответов на запросы кассы при синхронизации
     */
    public Response getResponse() {
        Response localInstance = response;
        if (localInstance == null) {
            synchronized (Response.class) {
                localInstance = response;
                if (localInstance == null) {
                    response = localInstance = new Response(this);
                }
            }
        }
        return localInstance;
    }

    /**
     * Асинхронное обновление POS-терминала
     *
     * @param macAddress
     * @param port
     * @return
     */
    public static Observable<Void> updatePosTerminal(String macAddress, int port, PosType posType) {
        return Observable.fromCallable(() -> {
            if (terminal != null)
                terminal.terminate();

            terminal = Dagger.appComponent().posTerminalFactory().getPosTerminal(posType, macAddress);

            Logger.trace(TAG, "updatePosTerminal, pos type: " + String.valueOf(posType));

            return null;
        });
    }

    public static void updatePosTerminalWithoutCallback(Context context, String macAddress, int port, PosType posType) {
        updatePosTerminal(macAddress, port, posType)
                .subscribeOn(SchedulersCPPK.posTerminal())
                .subscribe(new Subscriber<Void>() {
                    @Override
                    public void onCompleted() {
                        Logger.trace(TAG, "updatePosTerminalWithoutCallback onCompleted, mac address " + macAddress + ", port " + String.valueOf(port) + " , type " + posType.name());
                    }

                    @Override
                    public void onError(Throwable e) {
                        Logger.error(TAG, "updatePosTerminalWithoutCallback onError", e);
                    }

                    @Override
                    public void onNext(Void aVoid) {
                        Logger.trace(TAG, "updatePosTerminalWithoutCallback onNext");
                    }
                });
    }

    private void createAnrBackupWithoutCallback() {
        Completable
                .fromAction(() -> {
                    AnrLogWriter anrLogWriter = new AnrLogWriter(getApplicationContext(), new File(PathsConstants.LOG_ANR));
                    anrLogWriter.createLog();
                })
                .subscribeOn(SchedulersCPPK.background())
                .subscribe(() -> {
                }, throwable -> Logger.error(TAG, throwable));
    }

    private void createDropBoxBackupWithoutCallback() {
        Completable
                .fromAction(() -> {
                    DropBoxLogWriter dropBoxLogWriter = new DropBoxLogWriter(getApplicationContext(), new File(PathsConstants.LOG_DROPBOX));
                    dropBoxLogWriter.createLog();
                })
                .subscribeOn(SchedulersCPPK.background())
                .subscribe(() -> {
                }, throwable -> Logger.error(TAG, throwable));
    }

    private void initExternalSQLiteTool() {
        sqLiteConnectionService.start(BuildConfig.SQLITE_STUDIO_SERVICE_PORT);
    }

    public static IPos getPosTerminal() {
        return terminal;
    }

}
