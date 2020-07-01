package ru.ppr.cppk.di;

import android.database.Cursor;
import android.database.DatabaseUtils;
import android.os.Build;
import android.os.Handler;
import android.util.Log;

import java.io.File;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import ru.ppr.core.domain.model.ApplicationInfo;
import ru.ppr.core.domain.model.DeviceInfo;
import ru.ppr.core.manager.IBluetoothManager;
import ru.ppr.core.manager.eds.EdsManager;
import ru.ppr.core.manager.eds.EdsManagerWrapper;
import ru.ppr.core.manager.factory.EdsCheckerFactory;
import ru.ppr.core.manager.network.NetworkManager;
import ru.ppr.core.ui.helper.crashreporter.CrashReporter;
import ru.ppr.core.ui.helper.crashreporter.FileLoggerCrashListener;
import ru.ppr.core.ui.helper.crashreporter.LoggerCrashListener;
import ru.ppr.core.ui.mvp.core.MvpProcessor;
import ru.ppr.cppk.AppComponent;
import ru.ppr.cppk.AppModule;
import ru.ppr.cppk.BuildConfig;
import ru.ppr.cppk.DaggerAppComponent;
import ru.ppr.cppk.Globals;
import ru.ppr.cppk.Holder;
import ru.ppr.cppk.HolderDefault;
import ru.ppr.cppk.PathsConstants;
import ru.ppr.cppk.db.LocalDaoSession;
import ru.ppr.cppk.entity.event.model.StationDevice;
import ru.ppr.cppk.entity.settings.PrivateSettings;
import ru.ppr.cppk.helpers.CashierSessionInfo;
import ru.ppr.cppk.helpers.DeviceSessionInfo;
import ru.ppr.cppk.helpers.UiThread;
import ru.ppr.cppk.helpers.UserSessionInfo;
import ru.ppr.cppk.helpers.crashReporter.AppKillerImpl;
import ru.ppr.cppk.helpers.crashReporter.DbLoggerCrashListener;
import ru.ppr.cppk.helpers.crashReporter.DbStateCrashListener;
import ru.ppr.cppk.helpers.crashReporter.FreeResourcesCrashListener;
import ru.ppr.cppk.logic.CriticalNsiChecker;
import ru.ppr.cppk.logic.DocumentNumberProvider;
import ru.ppr.cppk.logic.DocumentSalePd;
import ru.ppr.cppk.logic.FiscalHeaderParamsBuilder;
import ru.ppr.cppk.logic.NsiDataContractsVersionChecker;
import ru.ppr.cppk.logic.PermissionChecker;
import ru.ppr.cppk.logic.SecurityDataContractsVersionChecker;
import ru.ppr.cppk.logic.ShiftAlarmManager;
import ru.ppr.cppk.logic.ShiftManager;
import ru.ppr.cppk.logic.TestPdSaleDocumentFactory;
import ru.ppr.cppk.logic.builder.EventBuilder;
import ru.ppr.cppk.managers.AppNetworkManager;
import ru.ppr.cppk.managers.DbManager;
import ru.ppr.cppk.managers.DiNetworkManagerModule;
import ru.ppr.cppk.managers.MobileSignalManager;
import ru.ppr.cppk.managers.NsiVersionManager;
import ru.ppr.cppk.managers.PosManager;
import ru.ppr.cppk.managers.PrinterManager;
import ru.ppr.cppk.managers.db.LocalDaoSessionUpdatesListener;
import ru.ppr.cppk.managers.db.LocalDbManager;
import ru.ppr.cppk.managers.db.NsiDaoSessionUpdatesListener;
import ru.ppr.cppk.managers.db.NsiDbManager;
import ru.ppr.cppk.managers.db.SecurityDaoSessionUpdatesListener;
import ru.ppr.cppk.managers.db.SecurityDbManager;
import ru.ppr.cppk.printer.PrinterFactory;
import ru.ppr.database.Database;
import ru.ppr.logger.Logger;
import ru.ppr.nsi.NsiDaoSession;
import ru.ppr.security.SecurityDaoSession;

/**
 * Провайдер зависимостей.
 *
 * @author Aleksandr Brazhkin
 */
public enum Di {

    INSTANCE;

    private String DeviceModel;
    public String getDeviceModel(){return DeviceModel;}
    //region Модули
    /**
     * DI-модуль для менеджеров работы с сетью
     */
    private DiNetworkManagerModule diNetworkManagerModule;
    //endregion

    //region Зависимости
    private Globals app;
    /**
     * Создатель очетов о крашах
     */
    private CrashReporter crashReporter;
    /**
     * Менеджер автозакрытия смены
     */
    private ShiftAlarmManager shiftAlarmManager;
    /**
     * Менеджер смены
     */
    private ShiftManager shiftManager;
    /**
     * Менеджер для работы с POS-устройством.
     */
    private PosManager posManager;
    /**
     * Менеджер БД.
     */
    private DbManager dbManager;
    /**
     *
     */
    LocalDbManager localDbManager;
    /**
     *
     */
    NsiDbManager nsiDbManager;
    /**
     *
     */
    SecurityDbManager securityDbManager;
    /**
     * Высокоуровневая обертка для доступа к локальной БД.
     * Необходимо создавать новый экземпляр после закрытия/открытия соединения с БД
     */
    private final Holder<LocalDaoSession> localDaoSession = new HolderDefault<>();
    /**
     * Высокоуровневая обертка для доступа к базе НСИ.
     * Необходимо создавать новый экземпляр после закрытия/открытия соединения с БД
     */
    private final Holder<NsiDaoSession> nsiDaoSession = new HolderDefault<>();
    /**
     * Высокоуровневая обертка для доступа к базе Security.
     * Необходимо создавать новый экземпляр после закрытия/открытия соединения с БД
     */
    private final Holder<SecurityDaoSession> securityDaoSession = new HolderDefault<>();
    /**
     * Менеджер уровня сигнала мобильной сети
     */
    private MobileSignalManager mobileSignalManager;
    /**
     * Частные настройки
     */
    private Holder<PrivateSettings> privateSettings = new HolderDefault<>();
    /**
     * In-memory хранилище информации о ПТК.
     */
    private final DeviceSessionInfo deviceSessionInfo = new DeviceSessionInfo();
    /**
     * In-memory хранилище информации о текущем авторизованном пользователе.
     */
    private final UserSessionInfo userSessionInfo = new UserSessionInfo();
    /**
     * In-memory хранилище информации о текущем кассире.
     */
    private final CashierSessionInfo cashierSessionInfo = new CashierSessionInfo();
    /**
     * UI-поток.
     */
    private UiThread uiThread;
    /**
     * Класс для проверки разрешений текущего пользователя.
     */
    private PermissionChecker permissionChecker;
    /**
     * Менеджер для работы с принтером.
     */
    private PrinterManager printerManager;
    /**
     * Менеджер для работы с сетью
     */
    private AppNetworkManager appNetworkManager;
    /**
     * Менеджер версий NSI
     */
    private NsiVersionManager nsiVersionManager;
    //endregion

    public void init(Globals app, DiNetworkManagerModule diNetworkManagerModule) {
        this.app = app;
        this.diNetworkManagerModule = diNetworkManagerModule;

        uiThread = new UiThread(new Handler());

        crashReporter = new CrashReporter.Builder()
                .setApplicationInfo(applicationInfo())
                .setDeviceInfo(deviceInfo())
                .addCrashListener(new LoggerCrashListener())
                .addCrashListener(new LoggerCrashListener())
                .addCrashListener(new FileLoggerCrashListener(new File(PathsConstants.LOG_FATALS), FileLoggerCrashListener.DEFAULT_MAX_FILE_COUNT))
                .addCrashListener(new DbLoggerCrashListener(getApp()))
                .addCrashListener(new DbStateCrashListener(getApp()))
                .addCrashListener(new FreeResourcesCrashListener())
                .setAppKiller(new AppKillerImpl(getApp()))
                .build();
        crashReporter.apply();

        DeviceModel = android.os.Build.MODEL;
        Logger.trace("SystemBarUtils", "---Phone model is " + DeviceModel);

        nsiDbManager = new NsiDbManager(getApp());
        nsiDaoSession().set(nsiDbManager.getDaoSession());
        securityDbManager = new SecurityDbManager(getApp());
        securityDaoSession().set(securityDbManager.getDaoSession());
        localDbManager = new LocalDbManager(getApp());
        localDaoSession().set(localDbManager.getDaoSession());

        dbManager = new DbManager(localDaoSession(), nsiDaoSession(), securityDaoSession());

        initDi();

        mobileSignalManager = new MobileSignalManager(getApp());

        nsiVersionManager = new NsiVersionManager(Dagger.appComponent().versionRepository(), localDaoSession(), Dagger.appComponent().commonSettingsStorage());

        permissionChecker = new PermissionChecker(userSessionInfo, securityDaoSession());

        shiftAlarmManager = new ShiftAlarmManager(getApp(), nsiVersionManager(),
                new CriticalNsiChecker(nsiVersionManager(), getUserSessionInfo(), permissionChecker()));
        shiftManager = new ShiftManager(getApp(), shiftAlarmManager);
        posManager = new PosManager(shiftManager);

        new LocalDaoSessionUpdatesListener(localDbManager(), nsiDbManager(), securityDbManager(), localDaoSession(), getPrivateSettings(), Dagger.appComponent().commonSettingsStorage(), getShiftManager(), getPosManager(), Dagger.appComponent().pdSaleEnvFactory());
        new NsiDaoSessionUpdatesListener(nsiDbManager(), nsiDaoSession(), localDaoSession(), getPrivateSettings(), nsiVersionManager(), Dagger.appComponent().pdSaleEnvFactory(), Dagger.appComponent().fineRepository());
        new SecurityDaoSessionUpdatesListener(securityDbManager(), securityDaoSession());

        try{
            Logger.debug(TAG, "init: DI exec SQL ");



            List<String> queries = new LinkedList<>(Arrays.asList(
                    "UPDATE TicketTypes SET Tax = replace(Tax, '18', '20') WHERE Tax is '18' AND DeleteInVersionId ISNULL;",
                    "UPDATE Taxes SET Value = replace(Value, '18', '20') WHERE Value is '18' AND DeleteInVersionId ISNULL;"

            ));

            int [] nums = {25,45,65,55,80,100,150,200,250,300,350,500};

            int [] nums_processing = {20, 100, 200, 1000};

            // Сервисные сборы
            for (int i: nums ) {
                double tax_before = Math.round(i * 18.0 / 118.0 * 100.0) / 100.0;
                double tax_after = Math.round(i * 20.0 / 120.0 * 100.0) / 100.0;
                String ss = String.format(Locale.US,
                        "UPDATE ServiceFees SET Tax = " +
                        "replace(Tax, '%.2f', '%.2f') " +
                        "WHERE Tariff is '%d' AND DeleteInVersionId ISNULL;",
                        tax_before, tax_after, i);
                queries.add(ss);
            }


            // Processing
            for (int i: nums_processing ) {
                double tax_before = Math.round(i * 18.0 / 118.0 * 100.0) / 100.0;
                double tax_after = Math.round(i * 20.0 / 120.0 * 100.0) / 100.0;
                String ss = String.format(Locale.US,
                        "UPDATE ProcessingFees SET Tax = " +
                                "replace(Tax, '%.2f', '%.2f') " +
                                "WHERE Tariff is '%d' AND DeleteInVersionId ISNULL;",
                        tax_before, tax_after, i);
                queries.add(ss);
            }

            Database database = nsiDbManager().getDaoSession().getNsiDb();

            for(String query:queries){
                Logger.debug(TAG, "init: DI execute " + query);

                Cursor curs = database.rawQuery(query, null);
                Logger.debug(TAG, "init: DI exec SQL " + DatabaseUtils.dumpCursorToString(curs));
            }

        }
        catch(Exception e){
            Logger.error("TAG", e);
        }

        printerManager = new PrinterManager(getApp(), printerFactory(), Dagger.appComponent().privateSettingsHolder());

    }

    private void initDi() {
        AppComponent appComponent = DaggerAppComponent.builder().appModule(new AppModule(Di.INSTANCE)).build();
        Dagger.setAppComponent(appComponent);
    }

    public Globals getApp() {
        return app;
    }

    public UiThread uiThread() {
        return uiThread;
    }

    public CrashReporter getCrashReporter() {
        return crashReporter;
    }

    public MvpProcessor getMvpProcessor() {
        return Dagger.appComponent().mvpProcessor();
    }

    public ShiftAlarmManager getShiftAlarmManager() {
        return shiftAlarmManager;
    }

    public ShiftManager getShiftManager() {
        return shiftManager;
    }

    public PosManager getPosManager() {
        return posManager;
    }

    public EdsCheckerFactory getEdsCheckerFactory() {
        return new EdsCheckerFactory(getApp(), bluetoothManager());
    }

    public EdsManager getEdsManager() {
        return Dagger.appComponent().edsManager();
    }

    public EdsManagerWrapper getEdsManagerWrapper() {
        return Dagger.appComponent().edsManagerWrapper();
    }

    public DbManager getDbManager() {
        return dbManager;
    }

    public MobileSignalManager mobileSignalManager() {
        return mobileSignalManager;
    }

    public IBluetoothManager bluetoothManager() {
        return Dagger.appComponent().blIBluetoothManager();
    }

    public Holder<PrivateSettings> getPrivateSettings() {
        return privateSettings;
    }

    public DeviceSessionInfo getDeviceSessionInfo() {
        return deviceSessionInfo;
    }

    public UserSessionInfo getUserSessionInfo() {
        return userSessionInfo;
    }

    public CashierSessionInfo getCashierSessionInfo() {
        return cashierSessionInfo;
    }

    public PermissionChecker permissionChecker() {
        return permissionChecker;
    }

    public PrinterManager printerManager() {
        return printerManager;
    }

    public NsiVersionManager nsiVersionManager() {
        return nsiVersionManager;
    }

    public EventBuilder eventBuilder() {
        return new EventBuilder(getPrivateSettings().get(),
                localDaoSession().get(),
                nsiVersionManager());
    }

    public FiscalHeaderParamsBuilder fiscalHeaderParamsBuilder() {
        return new FiscalHeaderParamsBuilder(getPrivateSettings().get(),
                Dagger.appComponent().commonSettingsStorage().get(),
                getDeviceSessionInfo(),
                getCashierSessionInfo(),
                nsiVersionManager(),
                Dagger.appComponent().stationRepository(),
                Dagger.appComponent().productionSectionRepository());
    }

    public DocumentNumberProvider documentNumberProvider() {
        return Dagger.appComponent().documentNumberProvider();
    }

    /*
    public FineSaleDocumentFactory fineSaleDocumentFactory() {
        return new FineSaleDocumentFactory(
                fiscalHeaderParamsBuilder(),
                documentNumberProvider(),
                printerManager().getOperationFactory(),
                localDaoSession().get(),
                getShiftManager(),
                eventBuilder()
        );
    }
*/
    public TestPdSaleDocumentFactory testPdSaleDocumentFactory() {
        return new TestPdSaleDocumentFactory(

                localDaoSession().get(),
                getShiftManager(),
                eventBuilder(),
                documentNumberProvider(),
                privateSettings.get(),
                fiscalHeaderParamsBuilder(),
                userSessionInfo,
                printerManager.getOperationFactory(),
                Dagger.appComponent().paperUsageCounter(),
                Dagger.appComponent().privateSettingsHolder(),
                nsiVersionManager(),
                Dagger.appComponent().stationRepository()
        );
    }

    public ApplicationInfo applicationInfo() {
        return new ApplicationInfo(
                BuildConfig.DEBUG,
                BuildConfig.APPLICATION_ID,
                BuildConfig.BUILD_TYPE,
                BuildConfig.FLAVOR,
                BuildConfig.VERSION_CODE,
                BuildConfig.VERSION_NAME
        );
    }

    public DeviceInfo deviceInfo() {
        return new DeviceInfo(
                Build.VERSION.RELEASE,
                Build.VERSION.SDK_INT,
                Build.DISPLAY,
                Build.BRAND,
                Build.MODEL
        );
    }

    public AppNetworkManager networkManager() {
        if (appNetworkManager == null) {
            NetworkManager mobileNetworkManager = diNetworkManagerModule.mobileNetworkManager(getApp(), Dagger.appComponent().airplaneModeManager());
            NetworkManager wifiNetworkManager = diNetworkManagerModule.wifiNetworkManager(getApp(), Dagger.appComponent().airplaneModeManager());
            AppNetworkManager.NetworkType networkType = diNetworkManagerModule.networkType(getPrivateSettings().get());
            appNetworkManager = diNetworkManagerModule.networkManager(mobileNetworkManager, wifiNetworkManager, networkType);
        }
        return appNetworkManager;
    }

    public PrinterFactory printerFactory() {
        return new PrinterFactory(getApp(), bluetoothManager(), networkManager(), Dagger.appComponent().commonSettingsStorage());
    }

    public SecurityDataContractsVersionChecker securityDataContractsVersionChecker() {
        return new SecurityDataContractsVersionChecker(localDaoSession().get(), BuildConfig.DATA_CONTRACTS_VERSION);
    }

    public NsiDataContractsVersionChecker nsiDataContractsVersionChecker() {
        return new NsiDataContractsVersionChecker(localDaoSession().get(), BuildConfig.DATA_CONTRACTS_VERSION);
    }

    public LocalDbManager localDbManager() {
        return localDbManager;
    }

    public NsiDbManager nsiDbManager() {
        return nsiDbManager;
    }

    public SecurityDbManager securityDbManager() {
        return securityDbManager;
    }

    public Holder<LocalDaoSession> localDaoSession() {
        return localDaoSession;
    }

    public Holder<NsiDaoSession> nsiDaoSession() {
        return nsiDaoSession;
    }

    public Holder<SecurityDaoSession> securityDaoSession() {
        return securityDaoSession;
    }

    private static final String TAG = Logger.makeLogTag(Di.class);
}
