package ru.ppr.chit;

import android.os.Handler;
import android.support.multidex.MultiDexApplication;

import javax.inject.Inject;

import ru.ppr.chit.api.ApiType;
import ru.ppr.chit.barcode.BarcodeManagerConfigSynchronizer;
import ru.ppr.chit.bs.ApiManager;
import ru.ppr.chit.bs.RegistrationInformant;
import ru.ppr.chit.bs.export.BoardingEventExporter;
import ru.ppr.chit.bs.export.TicketBoardingExporter;
import ru.ppr.chit.bs.load.TicketListLoader;
import ru.ppr.chit.data.db.LocalDbManager;
import ru.ppr.chit.data.db.NsiDbManager;
import ru.ppr.chit.data.db.SecurityDbManager;
import ru.ppr.chit.di.Dagger;
import ru.ppr.chit.domain.model.local.AppRuntimeProperty;
import ru.ppr.chit.domain.model.local.AuthInfo;
import ru.ppr.chit.domain.repository.local.AppPropertiesRepository;
import ru.ppr.chit.domain.repository.local.AuthInfoRepository;
import ru.ppr.chit.domain.ticket.TicketManager;
import ru.ppr.chit.domain.tripservice.TripServiceInfoStorage;
import ru.ppr.chit.eds.EdsManagerConfigSyncronizer;
import ru.ppr.chit.helpers.ActivityCallbacks;
import ru.ppr.chit.helpers.CrashReporterBuilder;
import ru.ppr.chit.rfid.RfidManagerConfigSynchronizer;
import ru.ppr.cppk.devtools.SQLiteConnectionService;
import ru.ppr.logger.Logger;
import ru.ppr.logger.ServiceLoggerMonitor;

/**
 * @author Dmitry Nevolin
 */
public class App extends MultiDexApplication {

    private static final String TAG = Logger.makeLogTag(App.class);

    @Inject
    LocalDbManager localDbManager;
    @Inject
    NsiDbManager nsiDbManager;
    @Inject
    SecurityDbManager securityDbManager;
    @Inject
    ActivityCallbacks activityCallbacks;
    @Inject
    TicketBoardingExporter ticketBoardingExporter;
    @Inject
    BoardingEventExporter boardingEventExporter;
    @Inject
    TicketListLoader ticketListLoader;
    @Inject
    AuthInfoRepository authInfoRepository;
    @Inject
    ApiManager apiManager;
    @Inject
    CrashReporterBuilder crashReporterBuilder;
    @Inject
    EdsManagerConfigSyncronizer edsManagerConfigSyncronizer;
    @Inject
    RegistrationInformant registrationInformant;
    @Inject
    TicketManager ticketManager;
    @Inject
    TripServiceInfoStorage tripServiceInfoStorage;
    @Inject
    BarcodeManagerConfigSynchronizer barcodeManagerConfigSynchronizer;
    @Inject
    RfidManagerConfigSynchronizer rfidManagerConfigSynchronizer;
    @Inject
    AppDirsInitializer appDirsInitializer;
    @Inject
    AppPropertiesRepository appPropertiesRepository;
    @Inject
    SQLiteConnectionService sqLiteConnectionService;
    @Inject
    AppRuntimeProperty appRuntimeProperty;

    @Override
    public void onCreate() {
        Logger.trace(TAG, "Запуск приложения chit");
        super.onCreate();
        initLogger();
        initDi();
        initCrashReporter();
        initActivityLifecycleListener();
        initAppDirs();
        initDatabase();
        initExternalSQLiteTool();
        initEdsManager();
        initRfidManager();
        initBarcodeManager();
        initApiModule();
        initPublishers();
        startExporters();
        startLoadTicketList();
    }

    private void initDi() {
        Dagger.setAppComponent(DaggerAppComponent.builder().appModule(new AppModule(this, new Handler())).build());
        Dagger.appComponent().inject(this);
    }

    private void initLogger() {
        startService(ServiceLoggerMonitor.getCallingIntent(this));
    }

    private void initCrashReporter() {
        crashReporterBuilder.build().apply();
    }

    private void initActivityLifecycleListener() {
        registerActivityLifecycleCallbacks(activityCallbacks);
    }

    private void initAppDirs() {
        appDirsInitializer.init();
    }

    private void initDatabase() {
        localDbManager.openConnection();
        nsiDbManager.openConnection();
        securityDbManager.openConnection();
    }

    private void initExternalSQLiteTool() {
        sqLiteConnectionService.start(BuildConfig.SQLITE_STUDIO_SERVICE_PORT);
    }

    private void initApiModule() {
        AuthInfo authInfo = authInfoRepository.loadLast();
        Long deviceId = appPropertiesRepository.load().getDeviceId();
        if (authInfo != null && deviceId != null) {
            apiManager.updateApi(ApiType.RETROFIT, authInfo, deviceId);
        }
    }

    private void initEdsManager() {
        edsManagerConfigSyncronizer.init();
    }

    private void initRfidManager() {
        rfidManagerConfigSynchronizer.init();
    }

    private void initBarcodeManager() {
        barcodeManagerConfigSynchronizer.init();
    }

    private void initPublishers() {
        tripServiceInfoStorage.init();
        ticketManager.init();
        registrationInformant.init();
    }

    private void startExporters() {
        ticketBoardingExporter.start();
        boardingEventExporter.start();
    }

    private void startLoadTicketList() {
        ticketListLoader.start();
    }

}
