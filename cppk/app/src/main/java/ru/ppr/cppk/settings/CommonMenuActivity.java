package ru.ppr.cppk.settings;

import android.content.Intent;
import android.os.Bundle;
import android.util.Pair;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import ru.ppr.cppk.R;
import ru.ppr.cppk.debug.Debug;
import ru.ppr.cppk.di.Dagger;
import ru.ppr.cppk.di.Di;
import ru.ppr.cppk.entity.log.Message;
import ru.ppr.cppk.helpers.FilePathProvider;
import ru.ppr.cppk.helpers.PrivateSettingsHolder;
import ru.ppr.cppk.helpers.SchedulersCPPK;
import ru.ppr.cppk.localdb.model.LogActionType;
import ru.ppr.cppk.localdb.model.LogEvent;
import ru.ppr.cppk.logic.PermissionChecker;
import ru.ppr.cppk.logic.TicketTapeChecker;
import ru.ppr.cppk.logic.fiscalDocStateSync.FiscalDocStateSynchronizer;
import ru.ppr.cppk.managers.FileCleaner;
import ru.ppr.cppk.printer.rx.operation.PrinterPrintNotSentDocsReport;
import ru.ppr.cppk.statistics.StatisticsActivity;
import ru.ppr.cppk.systembar.FeedbackProgressDialog;
import ru.ppr.cppk.systembar.SystemBarActivity;
import ru.ppr.cppk.ui.activity.base.Navigator;
import ru.ppr.cppk.ui.dialog.SimpleDialog;
import ru.ppr.logger.Logger;
import ru.ppr.security.entity.PermissionDvc;
import ru.ppr.security.entity.RoleDvc;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;

/**
 * Меню с общими настройками ПТК.
 */
public class CommonMenuActivity extends SystemBarActivity implements View.OnClickListener {

    private static final String TAG = Logger.makeLogTag(CommonMenuActivity.class);

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

    private Button settingsButton;
    private Button additionalSettingsButton;
    private Button setControlDetailButton;
    private Button makeReportButton;
    private Button statisticButton;
    private Button ticketTapeAccountingButton;
    private Button devicesButton;
    private Button fineSaleButton;
    private Button cancelPdButton;
    private Button countChangeButton;
    private Button closeShiftButton;
    private Button printNotSendDocsSheetButton;
    private Button closePOSTerminalDayButton;
    private Button sendDocsToOfdButton;
    private Button closeMonthButton;
    private Button backupButton;
    private Button logoutButton;

    private PrivateSettingsHolder privateSettings;
    private PermissionChecker permissionChecker;
    private CommonMenuViewManager commonMenuViewManager;
    private FiscalDocStateSynchronizer fiscalDocStateSynchronizer;
    private FilePathProvider filePathProvider;
    private FileCleaner fileCleaner;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_menu);
        privateSettings = Dagger.appComponent().privateSettingsHolder();
        permissionChecker = Dagger.appComponent().permissionChecker();
        commonMenuViewManager = Dagger.appComponent().commonMenuViewManager();
        fiscalDocStateSynchronizer = Dagger.appComponent().fiscalDocStateSynchronizer();
        filePathProvider = Dagger.appComponent().filePathProvider();
        fileCleaner = Dagger.appComponent().fileCleaner();
        findViews();
    }

    @Override
    protected void onResume() {
        super.onResume();

        switch (commonMenuViewManager.getState()) {
            case DEFAULT:
                configMenuItems();
                hideBackupProgress();
                break;
            case ATTENTION_DIALOG:
                configMenuItems();
                hideBackupProgress();
                showBackupAttentionDialog();
                break;
            case IN_PROGRESS:
                //не вызываем configMenuItems() поскольку в данный момент НСИ может быть не готово
                showBackupProgress();
                break;
            case BACKUP_COMPLETED_DIALOG:
                configMenuItems();
                hideBackupProgress();
                showBackupCompletedDialog();
                break;
        }

    }

    /**
     * Настроить видимость, текст и доступность пунктов меню.
     */
    private void configMenuItems() {
        // в рамках бага (http://agile.srvdev.ru/browse/CPPKPP-34891) нужна эта проверка,
        // чтобы методы ниже не вызывались в случае когда приложение не должно быть на этом
        // экране в принципе, и не случился NPE на getCurrentRole (мы ведь еще даже не авторизовались,
        // соответсвенно текущую роль авторизованного пользоватедля получить не можем)
        if (isCurrentScreenValid()) {
            configVisibility();
            configAccess();
            configText();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        hideBackupAttentionDialog();
        hideProgressDialog();
        hideBackupCompletedDialog();
    }

    private void configText() {
        setControlDetailButton.setText(privateSettings.get().isTransferControlMode() ? R.string.settings_control_detail_config_transfer_route : R.string.settings_control_detail_config_train_category);
    }

    private void configVisibility() {

        boolean isShiftOpened = di().getShiftManager().isShiftOpened();

        //НАСТРОЙКИ
        additionalSettingsButton.setVisibility(isShiftOpened ? View.GONE : View.VISIBLE);
        //Настроить категорию поезда, настроить маршрут трансфера
        setControlDetailButton.setVisibility(isShiftOpened ? View.VISIBLE : View.GONE);
        //Закрыть месяц
        closeMonthButton.setVisibility(isShiftOpened ? View.GONE : View.VISIBLE);
        // Аннулирование
        cancelPdButton.setVisibility(isShiftOpened ? View.VISIBLE : View.GONE);
        // Рассчитать сдачу
        countChangeButton.setVisibility(isShiftOpened ? View.VISIBLE : View.GONE);
        // Закрыть смену
        closeShiftButton.setVisibility(isShiftOpened ? View.VISIBLE : View.GONE);
        // Закрыть день на POS- терминале
        // http://agile.srvdev.ru/browse/CPPKPP-40692
        closePOSTerminalDayButton.setVisibility(di().getPosManager().isDayStarted() && !isShiftOpened ? View.VISIBLE : View.GONE);
        //оформить штраф
        fineSaleButton.setVisibility(di().getShiftManager().isShiftOpenedWithTestPd() ? View.VISIBLE : View.GONE);
        //печать отчета о непереданных в ОФД документах
        // Оставляем печать отчёта доступной только в закрытой смене.
        // Временное решение (hotfix), см. http://agile.srvdev.ru/browse/CPPKPP-39483
        // В будущем убрать hotfix когда будет нормальное решение
        printNotSendDocsSheetButton.setVisibility(
                di().printerManager().getPrinter().isFederalLaw54Supported() &&
                        di().getShiftManager().isShiftClosed() ? View.VISIBLE : View.GONE);
        //Отправка данных в ОФД
        sendDocsToOfdButton.setVisibility(di().printerManager().getPrinter().isFederalLaw54Supported() && privateSettings.get().isSaleEnabled() ? View.VISIBLE : View.GONE);
    }

    /**
     * Настраивает доступность функций для разный ролей пользователей
     */
    private void configAccess() {

        long saleStationCode = privateSettings.get().getSaleStationCode();
        boolean isBindingStationSet = Dagger.appComponent().stationRepository().load(saleStationCode, di().nsiVersionManager().getCurrentNsiVersionId()) != null;

        RoleDvc role = Di.INSTANCE.getUserSessionInfo().getCurrentUser().getRole();
        boolean isRootUser = role.isRoot();

        // настраиваем доступ к ROOT настройкам
        settingsButton.setVisibility(isRootUser ? View.VISIBLE : View.GONE);

        //настраиваем доступ к "Настройка категории поезда контроля" или "Настройка маршрута трансфера"
        setControlDetailButton.setEnabled(permissionChecker.checkPermission(privateSettings.get().isTransferControlMode() ? PermissionDvc.ChangeTransferRoute : PermissionDvc.ChangeTrainCategoryCode));

        //настраиваем доступ к "Формирование отчетности"
        makeReportButton.setEnabled(isBindingStationSet && permissionChecker.checkPermission(PermissionDvc.CreateReporting));

        //настраиваем доступ к "Информационные сирвисы"
        statisticButton.setEnabled(isBindingStationSet && permissionChecker.checkPermission(PermissionDvc.InfoService));

        //настраиваем доступ к "Учет билетной ленты"
        ticketTapeAccountingButton.setEnabled(isBindingStationSet && permissionChecker.checkPermission(PermissionDvc.AccountingTicketTape));

        //настраиваем доступ к "Аннулирование"
        cancelPdButton.setEnabled(isBindingStationSet && permissionChecker.checkPermission(PermissionDvc.Annulment));

        //настраиваем доступ к "Закрытие смены"
        closeShiftButton.setEnabled(permissionChecker.checkPermission(PermissionDvc.CloseShift));

        //настраиваем доступ к "Закрытие дня на терминле"
        closePOSTerminalDayButton.setEnabled(isBindingStationSet && permissionChecker.checkPermission(PermissionDvc.ClosePOSTerminalDay));

        //настраиваем доступ к "Закрытие месяца"
        closeMonthButton.setEnabled(isBindingStationSet && permissionChecker.checkPermission(PermissionDvc.CloseMonth));

        //настраиваем доступ к "Резервное копирование"
        backupButton.setEnabled(permissionChecker.checkPermission(PermissionDvc.CreateFullBackup));

        //настраиваем доступ к "Взимание штрафа"
        fineSaleButton.setEnabled(permissionChecker.checkPermission(PermissionDvc.FineSale));

        //печать отчета о непереданных в ОФД документах
        printNotSendDocsSheetButton.setEnabled(permissionChecker.checkPermission(PermissionDvc.PrintNotSentDocsSheet));

    }

    private void findViews() {
        settingsButton = (Button) findViewById(R.id.settingsGod);
        settingsButton.setOnClickListener(this);

        setControlDetailButton = (Button) findViewById(R.id.settingsControlDetailBtn);
        setControlDetailButton.setOnClickListener(this);
        makeReportButton = (Button) findViewById(R.id.createReport);
        makeReportButton.setOnClickListener(this);

        statisticButton = (Button) findViewById(R.id.InfoService);
        statisticButton.setOnClickListener(this);

        ticketTapeAccountingButton = (Button) findViewById(R.id.uchetLenti);
        ticketTapeAccountingButton.setOnClickListener(this);

        fineSaleButton = (Button) findViewById(R.id.fineSale);
        fineSaleButton.setOnClickListener(v -> Navigator.navigateToFineSaleActivity(this));

        cancelPdButton = (Button) findViewById(R.id.canselPD);
        cancelPdButton.setOnClickListener(this);
        countChangeButton = (Button) findViewById(R.id.countChange);
        countChangeButton.setOnClickListener(this);
        sendDocsToOfdButton = (Button) findViewById(R.id.sendDocsToOfd);
        sendDocsToOfdButton.setOnClickListener(this);
        closeMonthButton = (Button) findViewById(R.id.closeMounth);
        closeMonthButton.setOnClickListener(this);
        additionalSettingsButton = (Button) findViewById(R.id.settingsAdt);
        additionalSettingsButton.setOnClickListener(this);

        closeShiftButton = (Button) findViewById(R.id.closeShift);
        closeShiftButton.setOnClickListener(this);

        printNotSendDocsSheetButton = (Button) findViewById(R.id.printNotSendDocsSheet);
        printNotSendDocsSheetButton.setOnClickListener(this);

        closePOSTerminalDayButton = (Button) findViewById(R.id.closePOSTerminalDay);
        closePOSTerminalDayButton.setOnClickListener(this);

        logoutButton = (Button) findViewById(R.id.logout);
        logoutButton.setOnClickListener(this);

        devicesButton = (Button) findViewById(R.id.devices);
        devicesButton.setOnClickListener(this);

        backupButton = (Button) findViewById(R.id.backup);
        backupButton.setOnClickListener(this);
    }

    /**
     * Метод для перехода в Root меню.
     */
    private void startDebug() {
        startActivity(new Intent(this, Debug.class));
    }

    /**
     * Метод для ображения диалога logout.
     */
    private void logoutDialog() {

        SimpleDialog simpleDialog = SimpleDialog.newInstance(null,
                getString(R.string.really_exit),
                getString(R.string.Yes),
                getString(R.string.No),
                LinearLayout.VERTICAL,
                0);
        simpleDialog.show(getFragmentManager(), SimpleDialog.FRAGMENT_TAG);
        simpleDialog.setDialogPositiveBtnClickListener((dialog, dialogId) -> logout());
    }

    /**
     * Метод для выполнения необходимых операций при logout.
     */
    private void logout() {
        Logger.info(getClass(), "Нажали на кнопку ВЫХОД и согласились выйти");
        Navigator.navigateToSplashActivity(this, false);
        LogEvent logEvent = Dagger.appComponent().logEventBuilder()
                .setLogActionType(LogActionType.STANDARD_MODE_OFF)
                .setMessage(Message.LOGOUT)
                .build();
        Dagger.appComponent().localDaoSession().logEventDao().insertOrThrow(logEvent);
    }

    /**
     * Метод для перехода на экран расчета сдачи.
     */
    private void countChange() {
        Navigator.navigateToCalculateDeliveryActivity(this);
    }

    /**
     * Метод для перехода на экран учета билетно ленты.
     */
    private void accountingTicketTape() {
        if (getLocalDaoSession().getTicketTapeEventDao().isTicketTapeSet()) {
            Navigator.navigateToAccountingTicketTapeEndActivity(this);
        } else {
            Navigator.navigateToAccountingTicketTapeStartActivity(this, false);
        }
    }

    /**
     * Метод для перехода на экран настроек.
     */
    private void startSettingsMaket() {
        Intent intent = new Intent(this, AdditionalSettingsActivity.class);
        startActivity(intent);
    }

    /**
     * Метод для перехода на экран статистики.
     */
    private void startInfoService() {
        Intent intent = new Intent(this, StatisticsActivity.class);
        startActivity(intent);
    }

    /**
     * Метод для перехода на экран аннулирования ПД.
     */
    private void startRepail() {
        if (privateSettings.get().isSaleEnabled()) {
            Navigator.navigateToRepealActivity(this);
        } else {
            di().getApp().getToaster().showToast(R.string.printer_errorFunctionDisabledAtThisMoment);
        }
    }

    /**
     * Метод для перехода на экран настроки категории поезда контоля или марштута движения трансфера
     */
    private void settingsControlDetail() {
        startActivity(SetControlDetailActivity.getNewIntent(this));
    }

    /**
     * Метод для перехода на экран формирования отчестности.
     */
    private void createReport() {
        if (privateSettings.get().isSaleEnabled()) {
            Navigator.navigateToReportsActivity(this);
        } else {
            di().getApp().getToaster().showToast(R.string.action_is_not_avaliable);
        }
    }

    /**
     * Метод для перехода на экран устройств.
     */
    private void startDevicesSettings() {
        Navigator.navigateToDevicesActivity(this);
    }

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
                Dagger.appComponent().commonMenuViewManager().setState(CommonMenuViewManager.State.DEFAULT);
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
                    commonMenuViewManager.getSuccessBackup() ? getString(R.string.common_menu_dialog_backup_completed_message, commonMenuViewManager.getBackupPath()) : "Ошибка при резервном копировании.",
                    getString(R.string.common_menu_dialog_backup_completed_ok),
                    null,
                    LinearLayout.HORIZONTAL, BACKUP_COMPLETED_DIALOG_ID);
            backupCompletedDialog.setDialogPositiveBtnClickListener((dialog, dialogId) -> {
                Dagger.appComponent().commonMenuViewManager().setState(CommonMenuViewManager.State.DEFAULT);
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

        commonMenuViewManager.setState(CommonMenuViewManager.State.IN_PROGRESS);
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
        fileCleaner.clearDir(filePathProvider.getBackupsDir(), SharedPreferencesUtils.getMaxFileCountInBackupDir(CommonMenuActivity.this));
        Dagger.appComponent().commonMenuViewManager().setSuccessBackup(result.first);
        Dagger.appComponent().commonMenuViewManager().setBackupPath(result.second);
        Dagger.appComponent().commonMenuViewManager().setState(CommonMenuViewManager.State.BACKUP_COMPLETED_DIALOG);
        if (isActivityResumed()) {
            configMenuItems();
            hideBackupAttentionDialog();
            hideBackupProgress();
            showBackupCompletedDialog();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.settingsGod:
                startDebug();
                break;

            case R.id.settingsControlDetailBtn:
                settingsControlDetail();
                break;

            case R.id.logout:
                logoutDialog();
                break;

            case R.id.closeShift:
                Logger.info(getClass(), "Нажали на кнопку \"ЗАКРЫТЬ СМЕНУ\"");
                Navigator.navigateToCloseShiftActivity(this, false, false);
                break;

            case R.id.printNotSendDocsSheet:
                Logger.info(getClass(), "Нажали на кнопку \"ПЕЧАТАТЬ ОТВЕТ О НЕ ПЕРЕДАННЫХ В ОФД ДОКУМЕНТАХ\"");
                printNotSentDocsSheet();
                break;

            case R.id.closePOSTerminalDay:
                Logger.info(getClass(), "Нажали на кнопку \"ЗАКРЫТЬ день на терминале\"");
                Navigator.navigateToCloseTerminalDayActivity(this);
                break;

            case R.id.countChange:
                countChange();
                break;

            case R.id.uchetLenti:
                accountingTicketTape();
                break;

            case R.id.settingsAdt:
                startSettingsMaket();
                break;

            case R.id.InfoService:
                startInfoService();
                break;

            case R.id.canselPD:
                startRepail();
                break;

            case R.id.sendDocsToOfd:
                Logger.info(getClass(), "Нажали на кнопку \"Отправка документов в ОФД\"");
                Navigator.navigateToSendDocsToOfdActivity(this, false);
                break;

            case R.id.closeMounth:
                Logger.info(getClass(), "Нажали на кнопку \"ЗАКРЫТЬ МЕСЯЦ\"");
                Navigator.navigateToCloseMonthActivity(this, false);
                break;

            case R.id.createReport:
                createReport();
                break;

            case R.id.devices:
                startDevicesSettings();
                break;

            case R.id.backup:
                commonMenuViewManager.setState(CommonMenuViewManager.State.ATTENTION_DIALOG);
                showBackupAttentionDialog();
                break;

            default:
                break;
        }

    }

    /**
     * Печатать отчет о не переданных в ОФД документах
     */
    private void printNotSentDocsSheet() {
        FeedbackProgressDialog progressDialog = new FeedbackProgressDialog(CommonMenuActivity.this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage(getString(R.string.settings_menu_progress_printing_not_sent_docs));
        progressDialog.show();

        Dagger.appComponent().ticketTapeChecker().checkOrThrow()
                .andThen(fiscalDocStateSynchronizer.rxSyncCheckState())
                .flatMapObservable(syncStateResult -> di().printerManager().getOperationFactory().getPrintNotSentDocsReport().call())
                .subscribeOn(SchedulersCPPK.printer())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<PrinterPrintNotSentDocsReport.Result>() {
                    @Override
                    public void onCompleted() {
                        progressDialog.dismiss();
                    }

                    @Override
                    public void onError(Throwable e) {
                        Logger.error(TAG, e);
                        progressDialog.dismiss();
                        if (e instanceof TicketTapeChecker.TicketTapeIsNotSetException) {
                            Navigator.navigateToActivityTicketTapeIsNotSet(CommonMenuActivity.this);
                        }
                    }

                    @Override
                    public void onNext(PrinterPrintNotSentDocsReport.Result result) {
                        progressDialog.dismiss();
                    }
                });
    }

    /**
     * Метод для отображения диалога при создании backup.
     */
    private void showBackupProgress() {
        if (progressDialog == null || !progressDialog.isShowing()) {
            progressDialog = new FeedbackProgressDialog(CommonMenuActivity.this);
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
