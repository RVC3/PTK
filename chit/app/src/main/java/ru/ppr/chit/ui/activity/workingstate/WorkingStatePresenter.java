package ru.ppr.chit.ui.activity.workingstate;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import javax.inject.Inject;

import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import ru.ppr.chit.api.ApiType;
import ru.ppr.chit.api.auth.AuthorizationState;
import ru.ppr.chit.bs.ApiManager;
import ru.ppr.chit.bs.RegistrationInformant;
import ru.ppr.chit.bs.RegistrationState;
import ru.ppr.chit.bs.synchronizer.GarbageLocalDatabase;
import ru.ppr.chit.bs.synchronizer.GlobalSynchronizer;
import ru.ppr.chit.bs.synchronizer.SynchronizerInformer;
import ru.ppr.chit.domain.exchangeevent.ExchangeEventManager;
import ru.ppr.chit.domain.model.local.AuthInfo;
import ru.ppr.chit.domain.model.local.ExchangeEvent;
import ru.ppr.chit.domain.model.nsi.Version;
import ru.ppr.chit.domain.provider.NsiVersionProvider;
import ru.ppr.chit.domain.repository.local.AppPropertiesRepository;
import ru.ppr.chit.domain.repository.local.AuthInfoRepository;
import ru.ppr.chit.domain.repository.local.EventRepository;
import ru.ppr.chit.domain.repository.local.ExchangeEventRepository;
import ru.ppr.chit.domain.repository.local.TicketControlEventRepository;
import ru.ppr.chit.domain.tripservice.TripServiceManager;
import ru.ppr.chit.helpers.AppSchedulers;
import ru.ppr.chit.helpers.UiThread;
import ru.ppr.chit.manager.SoftwareUpdateManager;
import ru.ppr.chit.manager.WiFiManager;
import ru.ppr.core.domain.model.ApplicationInfo;
import ru.ppr.core.ui.mvp.presenter.BaseMvpViewStatePresenter;
import ru.ppr.logger.Logger;

/**
 * @author Dmitry Nevolin
 */
class WorkingStatePresenter extends BaseMvpViewStatePresenter<WorkingStateView, WorkingStateViewState> {

    private static final String TAG = Logger.makeLogTag(WorkingStatePresenter.class);

    private boolean initialized;
    private Navigator navigator;

    private final CompositeDisposable disposables = new CompositeDisposable();
    private final AuthInfoRepository authInfoRepository;
    private final ApiManager apiManager;
    private final GlobalSynchronizer globalSynchronizer;
    private final GarbageLocalDatabase garbageLocalDatabase;
    private final NsiVersionProvider nsiVersionProvider;
    private final ApplicationInfo applicationInfo;
    private final TripServiceManager tripServiceManager;
    private final EventRepository eventRepository;
    private final ExchangeEventRepository exchangeEventRepository;
    private final TicketControlEventRepository ticketControlEventRepository;
    private final SoftwareUpdateManager softwareUpdateManager;
    private final WiFiManager wiFiManager;
    private final ExchangeEventManager exchangeEventManager;
    private final RegistrationInformant registrationInformant;
    private final AppPropertiesRepository appPropertiesRepository;
    private final SynchronizerInformer synchronizerInformer;
    private final UiThread uiThread;

    private RegistrationState registrationState = RegistrationState.NOT_PREPARED;
    private AuthorizationState authorizationState = AuthorizationState.NOT_AUTHORIZED;

    @Inject
    WorkingStatePresenter(WorkingStateViewState workingStateViewState,
                          AuthInfoRepository authInfoRepository,
                          ApiManager apiManager,
                          GlobalSynchronizer globalSynchronizer,
                          GarbageLocalDatabase garbageLocalDatabase,
                          NsiVersionProvider nsiVersionProvider,
                          ApplicationInfo applicationInfo,
                          TripServiceManager tripServiceManager,
                          EventRepository eventRepository,
                          ExchangeEventRepository exchangeEventRepository,
                          TicketControlEventRepository ticketControlEventRepository,
                          SoftwareUpdateManager softwareUpdateManager,
                          WiFiManager wiFiManager,
                          ExchangeEventManager exchangeEventManager,
                          RegistrationInformant registrationInformant,
                          AppPropertiesRepository appPropertiesRepository,
                          SynchronizerInformer synchronizerInformer,
                          UiThread uiThread) {
        super(workingStateViewState);
        this.authInfoRepository = authInfoRepository;
        this.apiManager = apiManager;
        this.globalSynchronizer = globalSynchronizer;
        this.garbageLocalDatabase = garbageLocalDatabase;
        this.nsiVersionProvider = nsiVersionProvider;
        this.applicationInfo = applicationInfo;
        this.tripServiceManager = tripServiceManager;
        this.eventRepository = eventRepository;
        this.exchangeEventRepository = exchangeEventRepository;
        this.ticketControlEventRepository = ticketControlEventRepository;
        this.softwareUpdateManager = softwareUpdateManager;
        this.wiFiManager = wiFiManager;
        this.exchangeEventManager = exchangeEventManager;
        this.registrationInformant = registrationInformant;
        this.appPropertiesRepository = appPropertiesRepository;
        this.synchronizerInformer = synchronizerInformer;
        this.uiThread = uiThread;
    }

    void initialize() {
        if (!initialized) {
            initialized = true;
            onInitialize();
        }
    }

    private void onInitialize() {
        Logger.trace(TAG, "onInitialize");
        exchangeEventManager
                .completedExchangeEvents()
                .observeOn(AppSchedulers.background())
                .subscribe(exchangeEvent -> updateViews());
        registrationInformant
                .getRegistrationStatePublisher()
                .observeOn(AppSchedulers.background())
                .doOnNext(registrationState -> this.registrationState = registrationState)
                .subscribe(registrationState -> {
                    updateViews();
                });
        registrationInformant
                .getAuthorizationStatePublisher()
                .observeOn(AppSchedulers.background())
                .doOnNext(authorizationState -> this.authorizationState = authorizationState)
                .subscribe(authorizationState -> {
                    updateViews();
                });


        disposables.add(Single
                .fromCallable(tripServiceManager::isTripServiceStarted)
                .doOnSuccess(tripServiceStarted -> updateViews())
                .observeOn(AndroidSchedulers.mainThread())
                // решили, что кнопка соединения с БС должна быть видима всегда (хотя есть сомнения по этому поводу)
                //.doOnSuccess(tripServiceStarted -> view.setBsConnectVisible(!tripServiceStarted))
                .subscribeOn(AppSchedulers.background())
                .subscribe()
        );

        disposables.add(synchronizerInformer.getSynchronizePublisher()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(syncEvent -> {
                    Logger.info(synchronizerInformer.TAG, syncEvent.getMessage());
                    view.setSyncProgressMessage(syncEvent.getMessage());
                })
        );
    }

    void setNavigator(Navigator navigator) {
        this.navigator = navigator;
    }

    void onWifiSettingsClicked() {
        Logger.trace(TAG, "onWifiSettingsClicked");
        navigator.navigateToWifiSettings();
    }

    void onForceSyncClicked() {
        Logger.trace(TAG, "onForceSyncClicked");
        startSync();
    }

    void onBsConnectClicked() {
        Logger.trace(TAG, "onBsConnectClicked");
        navigator.navigateToReadBsQrCode();
    }

    void onAuthInfoIdReceived(@Nullable Long authInfoId) {
        Logger.trace(TAG, "onAuthInfoIdReceived(authInfoId): " + authInfoId);
        Disposable disposable = Completable
                .fromAction(() -> {
                    if (authInfoId != null) {
                        AuthInfo authInfo = authInfoRepository.load(authInfoId);
                        if (authInfo != null) {
                            Logger.trace(TAG, "onAuthInfoIdReceived(authInfo): " + authInfo);
                            onAuthInfoReceived(authInfo);
                        }
                    }
                })
                .subscribeOn(AppSchedulers.background())
                .subscribe();
        disposables.add(disposable);
    }

    void onWifiSettingsChanged() {
        Logger.trace(TAG, "onWifiSettingsChanged");
        Disposable disposable = Completable.fromAction(this::updateViews)
                .subscribeOn(AppSchedulers.background())
                .subscribe();
        disposables.add(disposable);
    }

    private void onAuthInfoReceived(@NonNull AuthInfo authInfo) {
        uiThread.post(() -> view.setSyncProgressVisible(true));
        Long deviceId = appPropertiesRepository.load().getDeviceId();
        if (deviceId == null) {
            throw new IllegalStateException("deviceId must be set");
        }
        // При подключении к БС очищаем старый токен, чтобы OAuth2Authenticator запросил новый токен
        apiManager.clearAuthToken();
        apiManager.updateApi(ApiType.RETROFIT, authInfo, deviceId);
        startSync();
    }

    private void startSync() {
        Logger.trace(TAG, "startSync");
        uiThread.post(() -> view.setSyncProgressVisible(true));
        // 1. Запуск процесса синхронизации с БС:
        // - загрузка информации о поезде
        // - загрузка списка билетов
        // - загрузка списка посаженных билетов
        // - синхронизация Нси
        // - Базы безопасности
        // - ПО
        // - Лицензий СФТ
        // - Данных СФТ
        Disposable disposable =
                globalSynchronizer.start()
                        // Запускаем сборку мусора в локальной базе данных
                        .andThen(garbageLocalDatabase.execute())
                        // 2. Запуск установки новой версии ПО
                        .andThen(Completable.fromAction(softwareUpdateManager::updateSoftware))
                        .subscribeOn(AppSchedulers.background())
                        // В будущем разная реакция
                        .subscribe(() -> {
                                    updateViews();
                                    uiThread.post(() -> view.setSyncProgressVisible(false));
                                },
                                error -> {
                                    Logger.error(TAG, error);
                                    updateViews();
                                    uiThread.post(() -> view.setSyncProgressVisible(false));
                                    uiThread.post(() -> view.setSyncErrorMessage(error.getMessage()));
                                });
        disposables.add(disposable);
    }

    private void updateViews() {
        Logger.trace(TAG, "updateViews");
        Long deviceId = appPropertiesRepository.load().getDeviceId();
        AuthInfo authInfo = authInfoRepository.loadLast();
        String wifiPointSsid = wiFiManager.getPointSsid();
        String softwareVersion = applicationInfo.getVersionName();
        Version version = nsiVersionProvider.getCurrentNsiVersionModel();
        WorkingStateView.LastExchangeInfo lastExchangeInfo = getLastExchangeInfo();
        int notExported = getNotExported();
        // Если не авторизован или авторизация битая
        boolean canBsConnect = authorizationState == AuthorizationState.AUTHORIZED_BROKEN || authorizationState == AuthorizationState.NOT_AUTHORIZED;
        boolean canForceSync = apiManager.isApiAvailable() &&
                // Если авторизован на БС
                authorizationState == AuthorizationState.AUTHORIZED;


        uiThread.post(() -> {
            view.setTerminalId(deviceId);
            view.setBsId(authInfo != null ? authInfo.getBaseStationId() : null);
            view.setWifiSsid(wifiPointSsid);
            view.setSoftwareVersion(softwareVersion);
            view.setNsiVersion(version);
            view.setLastExchangeInfo(lastExchangeInfo);
            view.setNotExported(notExported);
            view.setForceSyncVisible(canForceSync);
            view.setBsConnectVisible(canBsConnect);
        });
    }

    private WorkingStateView.LastExchangeInfo getLastExchangeInfo() {
        ExchangeEvent exchangeEvent = exchangeEventRepository.loadLast();
        if (exchangeEvent != null) {
            WorkingStateView.LastExchangeInfo lastExchangeInfo = new WorkingStateView.LastExchangeInfo();
            lastExchangeInfo.success = exchangeEvent.getStatus() != ExchangeEvent.Status.ERROR;
            lastExchangeInfo.date = exchangeEvent.getEvent(eventRepository).getCreatedAt();
            return lastExchangeInfo;
        } else {
            return null;
        }
    }

    private int getNotExported() {
        return ticketControlEventRepository.loadAllNotExported().size();
    }

    interface Navigator {

        void navigateToWifiSettings();

        void navigateToReadBsQrCode();

    }

}
