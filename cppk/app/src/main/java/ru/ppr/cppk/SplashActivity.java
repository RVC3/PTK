package ru.ppr.cppk;

import android.content.Context;
import android.content.Intent;
import android.device.PiccManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.util.Pair;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import ru.ppr.core.dataCarrier.findcardtask.FindCardTask;
import ru.ppr.core.dataCarrier.smartCard.cardReader.ReadCardResult;
import ru.ppr.core.dataCarrier.smartCard.cardReader.base.CardReader;
import ru.ppr.core.dataCarrier.smartCard.cardReader.top.AuthCardReader;
import ru.ppr.core.dataCarrier.smartCard.entity.AuthCardData;
import ru.ppr.core.dataCarrier.smartCard.entity.outerNumber.OuterNumber;
import ru.ppr.core.manager.eds.EdsManager;
import ru.ppr.core.manager.eds.EdsManagerWrapper;
import ru.ppr.cppk.dataCarrier.AuthCardDataMapper;
import ru.ppr.cppk.dataCarrier.OuterNumberMapper;
import ru.ppr.cppk.dataCarrier.entity.BscInformation;
import ru.ppr.cppk.di.Dagger;
import ru.ppr.cppk.di.Di;
import ru.ppr.cppk.entity.AuthCard;
import ru.ppr.cppk.entity.settings.LocalUser;
import ru.ppr.cppk.entity.settings.PrivateSettings;
import ru.ppr.cppk.export.ServiceUtils;
import ru.ppr.cppk.helpers.BackupAccessRequestConditionsChecker;
import ru.ppr.cppk.helpers.EmergencyModeHelper;
import ru.ppr.cppk.helpers.FilePathProvider;
import ru.ppr.cppk.helpers.RootAccessRequestConditionsChecker;
import ru.ppr.cppk.helpers.SchedulersCPPK;
import ru.ppr.cppk.helpers.StatusBarHelper;
import ru.ppr.cppk.localdb.model.CashRegister;
import ru.ppr.cppk.localdb.model.LogActionType;
import ru.ppr.cppk.localdb.model.LogEvent;
import ru.ppr.cppk.localdb.model.TicketWayType;
import ru.ppr.cppk.logic.CashRegisterValidityChecker;
import ru.ppr.cppk.logic.DeviceIdChecker;
import ru.ppr.cppk.managers.FileCleaner;
import ru.ppr.cppk.model.PdSaleParams;
import ru.ppr.cppk.settings.SharedPreferencesUtils;
import ru.ppr.cppk.settings.SplashViewManager;
import ru.ppr.cppk.systembar.FeedbackProgressDialog;
import ru.ppr.cppk.systembar.SystemBarActivity;
import ru.ppr.cppk.ui.activity.base.Navigator;
import ru.ppr.cppk.ui.activity.controlreadbsc.model.ControlReadBscParams;
import ru.ppr.cppk.ui.activity.pdSale.PdSaleActivity;
import ru.ppr.cppk.ui.dialog.SimpleDialog;
import ru.ppr.edssft.SftEdsChecker;
import ru.ppr.edssft.model.GetStateResult;
import ru.ppr.logger.Logger;
import ru.ppr.logger.ServiceLoggerMonitor;
import ru.ppr.nsi.entity.TicketCategory;
import ru.ppr.security.entity.SecuritySettings;
import ru.ppr.utils.Empty;
import rx.Completable;
import rx.Observable;
import rx.Single;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.subscriptions.Subscriptions;

import ru.ppr.core.domain.model.EdsType;

/**
 * Экран авторизации (Сплеш).
 */
public class SplashActivity extends SystemBarActivity {

    private static final String TAG = Logger.makeLogTag(SplashActivity.class);

    private PiccManager piccReader = null;
    /**
     * Крутилка ожидания создания бекапа
     */
    private FeedbackProgressDialog progressDialog;
    /**
     * Приглашение создать бекап
     */
    private SimpleDialog backupAttentionDialog;
    /**
     * Уведомление об окончании создания бекапа
     */
    private SimpleDialog backupCompletedDialog;

    private static final int BACKUP_ATTENTION_DIALOG_ID = 1;
    private static final int BACKUP_COMPLETED_DIALOG_ID = 2;
    private static final String BACKUP_ATTENTION_DIALOG_TAG = "BACKUP_ATTENTION_DIALOG_TAG";
    private static final String BACKUP_COMPLETED_DIALOG_TAG = "BACKUP_COMPLETED_DIALOG_TAG";

    private SplashViewManager splashViewManager;

    /**
     * Время поиска авторизационной карты, мс
     */
    private static final long AUTH_CARD_SEARCH_TIME = 5000;

    // EXTRAS
    private static final String EXTRA_EMERGENCY_MODE = "EXTRA_EMERGENCY_MODE";

    public static Intent getCallingIntent(Context context, boolean emergencyMode) {
        Intent intent = new Intent(context, SplashActivity.class);
        intent.putExtra(EXTRA_EMERGENCY_MODE, emergencyMode);
        return intent;
    }

    private enum InitErrorType {
        NONE,
        EDS,
        NSI,
        SECURITY,
        DEVICE_ID,
        TIMEZONE
    }

    /**
     * Eds Wrapper
     */
    private EdsManagerWrapper edsManagerWrapper;

    /**
     * Подписка на процесс инициализации ЭЦП
     */
    private Subscription initEdsSubscription;
    /**
     * Подписка на процесс поиска карты
     */
    private Subscription waitCardSubscription;
    /**
     * Подписка на процесс кеширования данных
     */
    private Subscription updateCacheSubscription = Subscriptions.unsubscribed();
    /**
     * Таймер на отсчет времени до возможности повторной попытки авторизации
     */
    private CountDownTimer lockTimer = null;
    /**
     * Частные настройки
     */
    private PrivateSettings privateSettings;
    /**
     * Команда поиска смарт-карты.
     */
    private FindCardTask findCardTask;

    private FileCleaner fileCleaner;
    private FilePathProvider filePathProvider;

    // Views
    private View scanIndicator;
    private TextView message, errorMessage, versionView;
    private Button authBtn, repeatBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash);

        privateSettings = di().getPrivateSettings().get();
        edsManagerWrapper = di().getEdsManagerWrapper();
        filePathProvider = Dagger.appComponent().filePathProvider();
        fileCleaner = Dagger.appComponent().fileCleaner();

        denyScreenLock();
        disableCheckForScreen();
        //На данном экране нет необходимости получать сообщения о истечении времени закрытия смены
        resetRegisterReceiver();

        versionView = (TextView) findViewById(R.id.version);
        versionView.setText(getResources().getString(R.string.splash_version, BuildConfig.VERSION_NAME));

        scanIndicator = findViewById(R.id.scanIndicator);
        message = (TextView) findViewById(R.id.message);
        errorMessage = (TextView) findViewById(R.id.errorMessage);

        authBtn = (Button) findViewById(R.id.authBtn);
        authBtn.setOnClickListener(v -> startAuthProgress());

        repeatBtn = (Button) findViewById(R.id.repeatBtn);
        repeatBtn.setOnClickListener(v -> configureStatusBar());

        resetCurrentUser();
        readEmergencyFlagFromIntent(getIntent());

        startService(ServiceLoggerMonitor.getCallingIntent(this));

        if (BuildConfig.DEBUG) {
            // Для возможности уйти в рут-меню с обычного телефона или эмулятора
            versionView.setOnLongClickListener(v -> {
                Navigator.navigateToMenuActivity(SplashActivity.this);
                finish();
/*                Navigator.navigateToRootAccessRequestActivity(SplashActivity.this);
                finish();*/
                return false;
            });
            //для дебажной сборки дадим возможность авторизации без карты
            authBtn.setOnLongClickListener(v -> {
                Navigator.navigateToEnterPinActivity(SplashActivity.this, null);
                finish();
                return false;
            });
        }

        // останавливаем сервис синхронизации, и запустим его только после инициализации sft
        Logger.info(TAG, "stopARMservice...");
        ServiceUtils.get().stopARMservice("Splash.onCreate()");

        configureStatusBar();

        splashViewManager = Dagger.appComponent().splashViewManager();
    }



    @Override
    protected void onResume() {
        super.onResume();
        rootAccessRequestConditionsChecker.start();
        backupAccessRequestConditionsChecker.start();

        switch (splashViewManager.getState()) {
            case DEFAULT:
                hideBackupProgress();
                break;
            case ATTENTION_DIALOG:
                hideBackupProgress();
                showBackupAttentionDialog();
                break;
            case IN_PROGRESS:
                showBackupProgress();
                break;
            case BACKUP_COMPLETED_DIALOG:
                hideBackupProgress();
                showBackupCompletedDialog();
                break;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        rootAccessRequestConditionsChecker.stop();
        backupAccessRequestConditionsChecker.stop();
        if (isFinishing()) {
            if (initEdsSubscription != null && !initEdsSubscription.isUnsubscribed()) {
                initEdsSubscription.unsubscribe();
                initEdsSubscription = null;
            }
            if (waitCardSubscription != null && !waitCardSubscription.isUnsubscribed()) {
                waitCardSubscription.unsubscribe();
                waitCardSubscription = null;
            }
            updateCacheSubscription.unsubscribe();
        }

        hideBackupAttentionDialog();
        hideProgressDialog();
        hideBackupCompletedDialog();
    }

    /**
     * Обработка кнопок для запроса на рут меню и запрет ухода с окна SPLASH по back
     *
     * @param keyCode
     * @param event
     * @return
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return conditionsCheckerOnKey(keyCode, event) ||
                keyCode == KeyEvent.KEYCODE_BACK ||
                super.onKeyDown(keyCode, event);
    }

    /**
     * Обработка кнопок для запроса на рут меню
     *
     * @param keyCode
     * @param event
     * @return
     */
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        return conditionsCheckerOnKey(keyCode, event) ||
                super.onKeyUp(keyCode, event);
    }

    private boolean conditionsCheckerOnKey(int keyCode, KeyEvent event) {
        boolean backupAccessRequestCondition = backupAccessRequestConditionsChecker.onKey(keyCode, event);
        boolean rootAccessRequestCondition = rootAccessRequestConditionsChecker.onKey(keyCode, event);
        return backupAccessRequestCondition || rootAccessRequestCondition;
    }

    /**
     * Переопределяем обработчик чтобы запретить переход на главное меню по нажатию на кнопку Settings
     **/
    @Override
    public void onClickSettings() {
    }

    /**
     * показывает или скрывает actionBar (возможность вытащить шторку)
     * если не отреагировать на всплывашке KingoRoot то рутовая команда повиснет
     * http://stackoverflow.com/questions/2758612/executorservice-that-interrupts-tasks-after-a-timeout
     */
    private void configureStatusBar() {

//        startAuthProgress();

        Completable
                .fromAction(() -> {
                    errorMessage.setVisibility(View.GONE);
                    message.setVisibility(View.VISIBLE);
                    message.setText(R.string.splash_msg_disabling_status_bar);
                    repeatBtn.setVisibility(View.GONE);
                })
                .observeOn(SchedulersCPPK.background())
                .andThen(BuildConfig.DEBUG ? Single.just(Empty.INSTANCE) : new StatusBarHelper().disableStatusBar(10000))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe(empty -> {
                    Logger.trace(TAG, "Status bar disabled successfully");
                    // Если активити умирает, ненадо ничего делать
                    if (!isFinishing()) {
                        defaultBehavior();
                    }
                }, throwable -> {
                    Logger.error(TAG, throwable);
                    message.setVisibility(View.GONE);
                    errorMessage.setVisibility(View.VISIBLE);
                    errorMessage.setText(R.string.splash_msg_no_root);
                    repeatBtn.setVisibility(View.VISIBLE);
                });
    }

    /**
     * Запускает стандартный сценарий подготовки работы ПТК
     */
    private void defaultBehavior() {

        Logger.trace(TAG, "defaultBehavior()");

        if (!checkPtkNumbers()) {
            Logger.trace(TAG, "navigateToSetUserIdActivity...");
            Navigator.navigateToSetUserIdActivity(this);
            finish();
            return;
        }
        if (!checkPrinterBinding()) {
            Logger.trace(TAG, "navigateToPrinterBindingActivity...");
            Navigator.navigateToPrinterBindingActivity(this);
            finish();
            return;
        }

        if (!checkPosBinding()) {
            Logger.trace(TAG, "navigateToPosBindingActivity...");
            Navigator.navigateToPosBindingActivity(this);
            finish();
            return;
        }

        Logger.trace(TAG, "Проверяем часовой пояс...");
        if (!checkTimeZone()) {
            //если не совпадает смещение timezone то работать нельзя, по словам Николая эта проблема должна решаться через root
            showInitError(InitErrorType.TIMEZONE);
            return;
        }

        Logger.trace(TAG, "Все проверки пройдены!");
        Logger.trace(TAG, "Запускаем получение статуса SFT...");

        initEds();
    }

    /**
     * Запускает процесс авторизации
     */
    private void startAuthProgress() {
        //добавим такую проверку поскольку можно одновременно тыкать как на боковые кнопки так и на основную
        if (waitCardSubscription == null) {
            Logger.trace(TAG, "startAuthProgress()");
            errorMessage.setVisibility(View.GONE);
            message.setVisibility(View.VISIBLE);
            message.setText(R.string.splash_msg_bring_card);
            authBtn.setVisibility(View.GONE);

//            if (PhoneModel.equals("i9000S")) startWaitCard_i9000S();
//            else startWaitCard();
            startWaitCard();
        }
    }

    /**
     * Читает флаг запуска в аварийном режиме.
     */
    private void readEmergencyFlagFromIntent(Intent intent) {
        boolean emergencyMode = false;
        Bundle extras = intent.getExtras();
        if (extras != null) {
            emergencyMode = extras.getBoolean(EXTRA_EMERGENCY_MODE, false);
            extras.remove(EXTRA_EMERGENCY_MODE);
        }
        setEmergencyModeState(emergencyMode);
    }

    /**
     * Показывает или прячет надпись Аварийный Режим!
     */
    private void setEmergencyModeState(boolean emergencyMode) {
        View emergencyModeView = findViewById(R.id.emergencyMode);
        emergencyModeView.setVisibility(emergencyMode ? View.VISIBLE : View.GONE);
        if (emergencyMode) {
            Logger.trace(TAG, "Move to emergency mode!");
            LogEvent logEventEmergency = Dagger.appComponent().logEventBuilder()
                    .setLogActionType(LogActionType.EMERGENCY_MODE_ON)
                    .build();
            Dagger.appComponent().localDaoSession().logEventDao().insertOrThrow(logEventEmergency);
        }
    }

    /**
     * Запускает процесс инициализации ЭЦП
     */
    private void initEds() {
        Logger.info(TAG, "initEds() START");
        initEdsSubscription =
                Completable
                        .fromAction(() -> {
                            di().uiThread().post(() -> {
                                errorMessage.setVisibility(View.GONE);
                                message.setVisibility(View.VISIBLE);
                                message.setText(R.string.splash_msg_init_eds_progress);
                                repeatBtn.setVisibility(View.GONE);
                            });

                            GetStateResult edsSateRes = edsManagerWrapper.getStateBlocking();
                            Logger.info(TAG, "initEds() edsSateRes = " + edsSateRes.toString());
                            EdsType ltype = edsManagerWrapper.getCurrentEdsType();
                            Logger.info(TAG, "ltype = " + ltype);

                            initEdsSubscription = null;

                            //к этому моменту уже всё железо и библиотеки проинитились, ставим флаг в true
                            di().getApp().setAllInited(true);

                            // запускаем слежение за подключением к АРМ
                            /*
                             * Блокируем запуск armService в методе initEds,
                             * т.к. сплеш может умереть слишком рано и запуститься новый,
                             * а оба потока будут сидеть на строке edsManagerWrapper.getStateBlocking();
                             * то в итоге вызов метода registerPowerDetect может произойти дважды,
                             * соответственно дважды запустить armService. Раньше там бросался
                             * exception в подобных ситуациях, теперь он будет убран, но тем не менее
                             * данную ситуацию нужно обработать.
                             */
                            if (isFinishing()) {
                                return;
                            }

                            Logger.trace(TAG, "initEds() запускаем слежение за подключением к АРМ...");
                            ServiceUtils.get().registerPowerDetect("Запуск слежения за питанием на окне авторизации");


                            //TODO Uncomment
                            //HACK, forcefully setting to true to avoid error
                            boolean edsInitRes = edsSateRes.isSuccessful() && edsSateRes.getState() == SftEdsChecker.SFT_STATE_ALL_LICENSES;
                            boolean skipErrors = true;

                            if(skipErrors){
                                edsInitRes = true;
                                di().uiThread().post(this::startCheckAccessLockedLimitationPeriod);
                            }

//                            Logger.trace(TAG, "edsSateRes.getState() = " + edsSateRes.getState() + "edsSateRes.isSuccessful() = " + edsSateRes.isSuccessful() );

                            if(!skipErrors) {
                                if (!edsInitRes) {
                                    di().uiThread().post(() -> showInitError(InitErrorType.EDS));
                                } else {
                                    //проверка на возможную подмену deviceId
                                    DeviceIdChecker.Result validateDeviceIdRes = validateDeviceId();
                                    if (validateDeviceIdRes == DeviceIdChecker.Result.INVALID_PTK_NUMBER)
                                        di().uiThread().post(() -> showInitError(InitErrorType.DEVICE_ID));
                                    else if (validateDeviceIdRes == DeviceIdChecker.Result.EDS_ERROR) {
                                        di().uiThread().post(() -> showInitError(InitErrorType.EDS));
                                    } else if (!checkNsiDataContractVersion()) {
                                        di().uiThread().post(() -> showInitError(InitErrorType.NSI));
                                    } else if (!checkSecurityDataContractVersion()) {
                                        di().uiThread().post(() -> showInitError(InitErrorType.SECURITY));
                                    } else {
                                        di().uiThread().post(this::startCheckAccessLockedLimitationPeriod);
                                    }
                                }
                            }
                        })
                        .subscribeOn(SchedulersCPPK.background())
                        .subscribe(() -> Logger.info(TAG, "initEds() FINISH"), throwable -> {
                            initEdsSubscription = null;
                            EmergencyModeHelper.startEmergencyMode(throwable);
                        });
    }

    /**
     * Функция производит бесконечное считывание с порта RfidReal пока программа не
     * завершиться или не считает какие либо данные с БСК
     */
    private void startWaitCard_i9000S() {
        if (piccReader == null) piccReader = new PiccManager();
        waitCardSubscription = Completable
                .fromAction(() -> scanIndicator.setVisibility(View.VISIBLE))
//                .subscribeOn(SchedulersCPPK.rfid())
                .andThen(Observable.create(s -> {
                    Logger.info(TAG, "Opening Card-reader begin");
                    int ret = piccReader.open();
                    if (ret != 0) {
                        Logger.info(TAG, "Opening Card-reader failed");
                        s.onError(new Exception("Card not found"));
                    }
                    else {
                        Logger.info(TAG, "Opening Card-reader success");
                        int scanCard = -1;
                        byte CardType[] = new byte[2];
                        byte Atq[] = new byte[14];
                        while (scanCard <= 0) scanCard = piccReader.request(CardType, Atq);
                        if (scanCard > 0) {
                            Logger.info(TAG, "Request success");
                            s.onNext(scanCard);
                            s.onCompleted();
                        }
                    }
                }))
                .timeout(AUTH_CARD_SEARCH_TIME, TimeUnit.MILLISECONDS, SchedulersCPPK.background())
//                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(scanCard -> scanIndicator.setVisibility(View.GONE))
                .doOnError(throwable -> scanIndicator.setVisibility(View.GONE))
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe(scanCard -> {
                            Logger.info(TAG, "Subscribe.");
                            char SAK = 1;
                            byte sak[] = new byte[1];
                            sak[0] = (byte) SAK;
                            byte SN[] = new byte[10];
                            int SNLen = piccReader.antisel(SN, sak);
                            if (SNLen >= 0){
                                Logger.info(TAG, "RF-Card is active. SN = " + SN.toString() + ", SAK = " + sak.toString() + ".");
                                byte atr[] = new byte[32];
                                int res = piccReader.activateEx(atr);
                                if (res == 0) {
                                    Navigator.navigateToEnterPinActivity(SplashActivity.this, /*authCard*/ null);
                                    finish();

                                }
                                else Logger.info(TAG, "Card activate failed");
                            }

                        },
                        throwable -> {
                            Logger.error(TAG, throwable);
                            waitCardSubscription = null;
                            authBtn.setVisibility(View.VISIBLE);
                            message.setVisibility(View.GONE);
                        });

//        final byte MODE = 0x00;
//        piccReader.deactivate(MODE);
//        piccReader.close();
    }

    private void startWaitCard() {

        Logger.trace(TAG, "Запуск startWaitCard");
        waitCardSubscription = Completable
                .fromAction(() -> scanIndicator.setVisibility(View.VISIBLE))
                .observeOn(SchedulersCPPK.rfid())
                .andThen(Observable
                        .create((Subscriber<? super AuthCardReader> subscriber) -> {

                            subscriber.add(new Subscription() {

                                private boolean unSubscribed;

                                @Override
                                public void unsubscribe() {
                                    if (findCardTask != null) {
                                        findCardTask.cancel();
                                        findCardTask = null;
                                        unSubscribed = true;
                                    }
                                }

                                @Override
                                public boolean isUnsubscribed() {
                                    return unSubscribed;
                                }
                            });
//<<<<<<< HEAD

                            Logger.trace(TAG, "!!! Create ");
//=======
                            Logger.trace(TAG, "Создание findCardTaskFactory");
//>>>>>>> 1b504430a565bfe7febb650362e4bcc135d53708
                            findCardTask = Dagger.appComponent().findCardTaskFactory().create();
                            Logger.trace(TAG, "Поиск карты");

                            Logger.trace(TAG, "!!! Find ");
                            CardReader cardReader = findCardTask.find();

                            Logger.trace(TAG, "!!! Found ");
                            if (cardReader instanceof AuthCardReader) {
                                subscriber.onNext((AuthCardReader) cardReader);
                                subscriber.onCompleted();
                            } else {
                                subscriber.onError(new Exception("Card not found"));
                            }
                        }))
                .timeout(AUTH_CARD_SEARCH_TIME, TimeUnit.MILLISECONDS, SchedulersCPPK.background())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(authCardReader -> scanIndicator.setVisibility(View.GONE))
                .doOnError(throwable -> scanIndicator.setVisibility(View.GONE))
                .flatMap((AuthCardReader authCardReader) -> Observable
                        .fromCallable(() -> {

                            Logger.trace(TAG, "!!! Oper1 ");
                            ReadCardResult<OuterNumber> outerNumberResult = authCardReader.readOuterNumber();
                            if (outerNumberResult.isSuccess()) {

                                OuterNumber outerNumber = outerNumberResult.getData();
                                BscInformation legacyBscInformation = new OuterNumberMapper().toLegacyBscInformation(
                                        outerNumber,
                                        authCardReader.getCardInfo().getCardUid()
                                );

                                ReadCardResult<byte[]> rawServiceDataResult = authCardReader.readRawAuthServiceData();
                                if (rawServiceDataResult.isSuccess()) {
                                    ReadCardResult<AuthCardData> authCardDataResult = authCardReader.readAuthCardData();
                                    if (authCardDataResult.isSuccess()) {
                                        AuthCardData authCardData = authCardDataResult.getData();
                                        AuthCard authCard = new AuthCardDataMapper().toLegacyAuthCard(
                                                authCardData,
                                                legacyBscInformation,
                                                authCardReader.getCardInfo().getCardUid(),
                                                rawServiceDataResult.getData()
                                        );
                                        return authCard;
                                    }
                                }
                            }
                            throw new Exception("Error read auth data");
                        })
                        .subscribeOn(SchedulersCPPK.rfid()))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe(authCard -> {
                    Logger.trace(TAG, "Auth data: " + authCard.toString());
                    waitCardSubscription = null;
                    if (isActivityResumed()) {
                        Logger.trace(TAG, "Start EnterPinActivity from " + SplashActivity.this.toString());
                        Navigator.navigateToEnterPinActivity(SplashActivity.this, authCard);
                        finish();
                    }
                }, throwable -> {
                    Logger.error(TAG, throwable);
                    waitCardSubscription = null;
                    authBtn.setVisibility(View.VISIBLE);
                    message.setVisibility(View.GONE);
                });
    }

    /**
     * Запускает проверку возможности авторизоваться на ПТК по времени
     */
    private void startCheckAccessLockedLimitationPeriod() {

        cancelLockTimer();

        //время с которого разрешен доступ после неверных попыток ввода пина
        Date dateAccessFailedPin = getAccessDate();

        //время последнего события в локальной БД - нельзя давать авторизоваться если каким-то чудом переводили время назад
        Date dateAccessLastEvent = new Date(getLocalDaoSession().getEventDao().getLastEventTimeStamp());

        //берем то время что побольше
        Date startDate = dateAccessFailedPin.after(dateAccessLastEvent) ? dateAccessFailedPin : dateAccessLastEvent;

        long timeForLock = startDate.getTime() - System.currentTimeMillis();

        lockTimer = new CountDownTimer(timeForLock, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                showTimeLockMessage(millisUntilFinished);
            }

            @Override
            public void onFinish() {
                cancelLockTimer();
                message.setVisibility(View.GONE);
                errorMessage.setVisibility(View.GONE);
                updateCache();
            }
        }.start();

    }

    private synchronized void cancelLockTimer() {
        if (lockTimer != null) {
            lockTimer.cancel();
            lockTimer = null;
        }
    }

    /**
     * Показать ошибку инициализации
     */
    private void showInitError(InitErrorType errorType) {
        Logger.trace(TAG, "showInitError(errorType=" + errorType + ")");
        String msg = getString(R.string.splash_msg_default_init_error);
        switch (errorType) {
            case EDS:
                msg = getString(R.string.splash_msg_init_eds_error);
                break;
            case NSI:
                msg = getString(R.string.splash_msg_invalid_nsi);
                break;
            case SECURITY:
                msg = getString(R.string.splash_msg_invalid_security);
                break;
            case DEVICE_ID:
                msg = getString(R.string.splash_msg_invalid_device_id);
                break;
            case TIMEZONE:
                msg = getString(R.string.splash_msg_invalid_time_zone);
                break;
        }
// временная отмена !!!!!
        message.setVisibility(View.GONE);
        errorMessage.setVisibility(View.VISIBLE);
        errorMessage.setText(msg);
        authBtn.setVisibility(View.GONE);
    }

    /**
     * Выведет сообщение о том, что ПТК заблокирован
     */
    private void showTimeLockMessage(long millisToUnlock) {
        String stringTime = getString(R.string.splash_msg_lock_more_than_one_day);
        if (millisToUnlock <= GlobalConstants.MILLISECOND_IN_DAY) {
            int secondsToUnlock = (int) (millisToUnlock / 1000);
            int seconds = secondsToUnlock % 60;
            int minutes = (secondsToUnlock / 60) % 60;
            int hours = (secondsToUnlock / 60 / 60) % 60;
            stringTime = String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds);
        }
        message.setVisibility(View.GONE);
        errorMessage.setVisibility(View.VISIBLE);
        errorMessage.setText(getString(R.string.splash_msg_time_to_unlock, stringTime));
    }

    /**
     * Запускает кеширование данных, нужно для ускорения запросов в дальнейшем
     */
    private void updateCache() {
        updateCacheSubscription = Completable
                .fromAction(() -> di().uiThread().post(() -> {
                    errorMessage.setVisibility(View.GONE);
                    message.setVisibility(View.VISIBLE);
                    message.setText(R.string.splash_msg_update_cache_progress);
                    repeatBtn.setVisibility(View.GONE);
                }))
                .andThen(Completable.fromAction(() -> {
                    if (!BuildConfig.DEBUG) {
                        // Отключаем кеширование при запуске, чтобы не мешалось при разработке
                        Dagger.appComponent().cacheUpdater().updateCache();
                    }
                }))
                .subscribeOn(SchedulersCPPK.background())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> {
                    message.setVisibility(View.GONE);
                    errorMessage.setVisibility(View.GONE);
                    authBtn.setVisibility(View.VISIBLE);
                }, EmergencyModeHelper::startEmergencyMode);

    }

    /**
     * Слазит в секьюрити и вернет время с которого доступ будет разрешен
     */
    private Date getAccessDate() {
        Date accessDate = new Date(System.currentTimeMillis());
        Date date = SharedPreferencesUtils.getTimeLockedAccess(getApplicationContext());
        if (date != null) {
            SecuritySettings securitySettings = getSecurityDaoSession().getSettingDao().getSecuritySettings();
            int min = securitySettings.getTimeLockAccess();

            //время когда доступ будет разрешен
            accessDate = new Date(date.getTime() + min * 60 * 1000);
        }
        return accessDate;
    }

    /**
     * Запускает проверку таймзоны
     */
    private boolean checkTimeZone() {
        int settingsOffset = Dagger.appComponent().commonSettingsStorage().get().getTimeZoneOffset();
        int currentOffset = TimeZone.getDefault().getRawOffset();
        return settingsOffset == currentOffset;
    }

    private DeviceIdChecker.Result validateDeviceId() {
        DeviceIdChecker deviceIdChecker = new DeviceIdChecker(di().getEdsManagerWrapper(), di().getPrivateSettings());
        DeviceIdChecker.Result out = deviceIdChecker.check();
        Logger.trace(TAG, "validateDeviceId() result = " + out);
        return out;
    }

    /**
     * Запускает проверку версии датаконтрактов НСИ
     */
    private boolean checkNsiDataContractVersion() {
        boolean out = Di.INSTANCE.nsiDataContractsVersionChecker().isDataContractVersionValid();
        Logger.trace(TAG, "checkNsiDataContractVersion() result = " + out);
        return out;
    }

    /**
     * Запускает проверку версии датаконтрактов Security
     */
    private boolean checkSecurityDataContractVersion() {
        boolean out = Di.INSTANCE.securityDataContractsVersionChecker().isDataContractVersionValid();
        Logger.trace(TAG, "checkSecurityDataContractVersion() result = " + out);
        return out;
    }

    /**
     * Функция проверяет, задан ли этому ПТК его номер
     *
     * @return
     */
    private boolean checkPtkNumbers() {
        long userId = privateSettings.getTerminalNumber();
        return userId > 0;
    }

    /**
     * Функция проверяет, привязан ли принтер к устройству
     *
     * @return
     */
    private boolean checkPrinterBinding() {
        String macAddress = di().printerManager().getPrinterMacAddress();
        CashRegister cashRegister = di().printerManager().getCashRegister();
        return !TextUtils.isEmpty(macAddress) && new CashRegisterValidityChecker().isValid(cashRegister);
    }

    /**
     * Функция проверяет, привязан ли IPos-терминал к устройству
     *
     * @return
     */
    private boolean checkPosBinding() {
        if (!privateSettings.isSaleEnabled())
            return true; //https://aj.srvdev.ru/browse/CPPKPP-25792
        String macAddress = SharedPreferencesUtils.getPosMacAddress(this);
        return macAddress != null;
    }

    private void resetCurrentUser() {
        //если этого не сделать то возможна ситуация запуска приложения без авторизации под последним активным юзером, при закрытии приложения через системное меню (смахиванием)
        LocalUser localUser = new LocalUser();
        Di.INSTANCE.getUserSessionInfo().setCurrentUser(localUser);
    }

    private RootAccessRequestConditionsChecker rootAccessRequestConditionsChecker = new RootAccessRequestConditionsChecker(new RootAccessRequestConditionsChecker.Callback() {
        @Override
        public void onConditionsMet() {
            Logger.info(TAG, "Запускаем RootAccessRequestActivity...");
            Navigator.navigateToRootAccessRequestActivity(SplashActivity.this);
            finish();
        }

        @Override
        public void onConditionsNotMet() {
            // nop
        }
    });

    private BackupAccessRequestConditionsChecker backupAccessRequestConditionsChecker = new BackupAccessRequestConditionsChecker(new BackupAccessRequestConditionsChecker.Callback() {
        @Override
        public void onConditionsMet() {
            Logger.info(TAG, "Запускаем диалог создания бэкапа...");
            splashViewManager.setState(SplashViewManager.State.ATTENTION_DIALOG);
            showBackupAttentionDialog();
        }

        @Override
        public void onConditionsNotMet() {
            // nop
        }
    });

    private void showBackupAttentionDialog() {
        Logger.trace(TAG, "showBackupAttentionDialog()");
        if (backupAttentionDialog == null) {
            backupAttentionDialog = SimpleDialog.newInstance(null,
                    getString(R.string.common_menu_dialog_backup_attention_message),
                    getString(R.string.common_menu_dialog_backup_attention_yes),
                    getString(R.string.common_menu_dialog_backup_attention_no),
                    LinearLayout.HORIZONTAL, BACKUP_ATTENTION_DIALOG_ID);
            backupAttentionDialog.setDialogPositiveBtnClickListener((dialog, dialogId) -> makeBackup());
            backupAttentionDialog.setDialogNegativeBtnClickListener((dialog, dialogId) -> {
                Dagger.appComponent().splashViewManager().setState(SplashViewManager.State.DEFAULT);
                hideBackupAttentionDialog();
            });
            backupAttentionDialog.setCancelable(false);
            backupAttentionDialog.show(getFragmentManager(), BACKUP_ATTENTION_DIALOG_TAG);
        }
    }

    private void showBackupCompletedDialog() {
        Logger.trace(TAG, "showBackupCompletedDialog()");
        if (backupCompletedDialog == null) {
            Logger.trace(TAG, "showBackupCompletedDialog - create new");
            backupCompletedDialog = SimpleDialog.newInstance(null,
                    splashViewManager.getSuccessBackup() ? getString(R.string.common_menu_dialog_backup_completed_message, splashViewManager.getBackupPath()) : "Ошибка при резервном копировании.",
                    getString(R.string.common_menu_dialog_backup_completed_ok),
                    null,
                    LinearLayout.HORIZONTAL, BACKUP_COMPLETED_DIALOG_ID);
            backupCompletedDialog.setDialogPositiveBtnClickListener((dialog, dialogId) -> {
                Dagger.appComponent().splashViewManager().setState(SplashViewManager.State.DEFAULT);
                hideBackupCompletedDialog();
            });
            backupCompletedDialog.setCancelable(false);
            backupCompletedDialog.show(getFragmentManager(), BACKUP_COMPLETED_DIALOG_TAG);
        }
    }

    private void hideBackupCompletedDialog() {
        if (backupCompletedDialog != null) {
            backupCompletedDialog.dismiss();
            backupCompletedDialog = null;
        }
    }

    private void hideBackupAttentionDialog() {
        if (backupAttentionDialog != null) {
            backupAttentionDialog.dismiss();
            backupAttentionDialog = null;
        }
    }

    private void makeBackup() {
        Logger.trace(TAG, "makeBackup() START");

        splashViewManager.setState(SplashViewManager.State.IN_PROGRESS);
        showBackupProgress();

        Observable
                .fromCallable(() -> Dagger.appComponent().fullBackupCreator().start())
                .subscribeOn(SchedulersCPPK.background())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result -> {
                    Logger.trace(TAG, "makeBackup() FINISH");
                    makeBackupFinished(result);
                }, throwable -> {
                    Logger.trace(TAG, throwable);
                    makeBackupFinished(new Pair<>(false, null));
                });
    }

    private void makeBackupFinished(Pair<Boolean, String> result) {
        Logger.info(TAG, "makeBackupFinished() Завершено создание бекапа: " + (result.first ? "Успешно - " + result.second : "Ошибка"));
        fileCleaner.clearDir(filePathProvider.getBackupsDir(), SharedPreferencesUtils.getMaxFileCountInBackupDir(SplashActivity.this));
        Dagger.appComponent().splashViewManager().setSuccessBackup(result.first);
        Dagger.appComponent().splashViewManager().setBackupPath(result.second);
        Dagger.appComponent().splashViewManager().setState(SplashViewManager.State.BACKUP_COMPLETED_DIALOG);
        if (isActivityResumed()) {
            hideBackupAttentionDialog();
            hideBackupProgress();
            showBackupCompletedDialog();
        }
    }

    /**
     * Метод для отображения диалога при создании backup.
     */
    private void showBackupProgress() {
        if (progressDialog == null || !progressDialog.isShowing()) {
            progressDialog = new FeedbackProgressDialog(SplashActivity.this);
            progressDialog.setTitle("Создание резервной копии");
            progressDialog.setMessage("Выполняется, подождите...");
            progressDialog.setCancelable(false);
            progressDialog.show();
        }
    }

    /**
     * Метод для скрытия диалога после создания backup.
     */
    private void hideBackupProgress() {
        if ((progressDialog != null) && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        progressDialog = null;
    }

}
