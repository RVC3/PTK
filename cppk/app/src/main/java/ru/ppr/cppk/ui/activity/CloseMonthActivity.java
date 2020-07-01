package ru.ppr.cppk.ui.activity;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Stack;
import java.util.UUID;

import ru.ppr.core.ui.widget.SimpleLseView;
import ru.ppr.cppk.Globals;
import ru.ppr.cppk.Holder;
import ru.ppr.cppk.R;
import ru.ppr.cppk.db.local.BaseEntityDao;
import ru.ppr.cppk.db.local.MonthEventDao;
import ru.ppr.cppk.di.Dagger;
import ru.ppr.cppk.di.Di;
import ru.ppr.cppk.dialogs.CppkDialogFragment;
import ru.ppr.cppk.localdb.model.MonthEvent;
import ru.ppr.cppk.entity.settings.CommonSettings;
import ru.ppr.cppk.entity.settings.PrivateSettings;
import ru.ppr.cppk.entity.settings.ReportType;
import ru.ppr.cppk.helpers.DeferredActionHandler;
import ru.ppr.cppk.helpers.SchedulersCPPK;
import ru.ppr.cppk.logic.TicketTapeChecker;
import ru.ppr.cppk.printer.exception.IncorrectEKLZNumberException;
import ru.ppr.cppk.printer.rx.operation.PrintMonthSheetFooterOperation;
import ru.ppr.cppk.printer.rx.operation.btMonthReport.PrintBtMonthReportOperation;
import ru.ppr.cppk.printer.rx.operation.clearingSheet.PrintClearingSheetOperation;
import ru.ppr.cppk.printer.rx.operation.discountMonthSheet.PrintDiscountMonthSheetOperation;
import ru.ppr.cppk.printer.rx.operation.shiftOrMonthSheet.PrintShiftOrMonthSheetOperation;
import ru.ppr.cppk.reports.ReportBTMonthlySheet;
import ru.ppr.cppk.reports.ReportDiscountMonthSheet;
import ru.ppr.cppk.reports.ReportShiftOrMonthlyClearingSheet;
import ru.ppr.cppk.reports.ReportShiftOrMonthlySheet;
import ru.ppr.cppk.settings.AccountingTicketTapeStartActivity;
import ru.ppr.cppk.systembar.FeedbackProgressDialog;
import ru.ppr.cppk.systembar.SystemBarActivity;
import ru.ppr.cppk.ui.activity.base.Navigator;
import ru.ppr.cppk.ui.activity.settingsPrinter.SettingsPrinterActivity;
import ru.ppr.database.garbage.DBGarbageCollector;
import ru.ppr.database.garbage.base.GCListener;
import ru.ppr.ikkm.exception.PrinterIsNotConnectedException;
import ru.ppr.logger.Logger;
import ru.ppr.nsi.repository.FineRepository;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;

/**
 * Created by Александр on 14.12.2015.
 */
public class CloseMonthActivity extends SystemBarActivity
        implements CppkDialogFragment.CppkDialogClickListener {

    private static final String TAG = Logger.makeLogTag(CloseMonthActivity.class);

    private final FineRepository fineRepository;

    // EXTRAS
    private static final String EXTRA_CILENT_MODE = "EXTRA_CILENT_MODE";


    private static final int DIALOG_ID_ASK_INIT_CLOSE_MONTH = 1;
    private static final int DIALOG_ID_ASK_PRINT_REPORT = 2;
    private static final int DIALOG_ID_CLOSE_MONTH_SUCCESS = 3;

    private static final int STEP_ID_AFTER_MONTH_SHEET_NOT_PRINTED = 10001;
    private static final int STEP_ID_PRINT_MONTH_SHEET_FOOTER = 10002;
    private static final int STEP_ID_CHECK_MONTH_SHEET_FOOTER_PRINTED = 10003;
    private static final int STEP_ID_WRITE_EVENT_CLOSE_MONTH_TO_DB = 10005;

    private static final int REQUEST_CODE_SET_TICKET_TAPE = 101;
    private static final int REQUEST_CODE_ACTIVATE_EKLZ = 102;

    private Stack<Integer> stepsStack;
    private boolean silentMode;
    private FeedbackProgressDialog progressDialog;
    private SimpleLseView simpleLseView;
    private final DeferredActionHandler deferredActionHandler = new DeferredActionHandler();
    private final Handler mainHandler = new Handler();

    private Holder<PrivateSettings> privateSettingsHolder;

    public CloseMonthActivity() {
        fineRepository = Dagger.appComponent().fineRepository();
    }

    public static Intent getCallingIntent(Context context, boolean silentMode) {
        Intent intent = new Intent(context, CloseMonthActivity.class);
        intent.putExtra(EXTRA_CILENT_MODE, silentMode);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        privateSettingsHolder = Globals.getInstance().getPrivateSettingsHolder();

        setContentView(R.layout.activity_close_month);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            silentMode = extras.getBoolean(EXTRA_CILENT_MODE);
        }

        simpleLseView = (SimpleLseView) findViewById(R.id.simpleLseView);

        ReportType[] reportTypes = getReportTypes();

        stepsStack = new Stack<>();
        stepsStack.push(STEP_ID_WRITE_EVENT_CLOSE_MONTH_TO_DB);
        for (int i = reportTypes.length - 1; i >= 0; i--) {
            if (reportTypes[i] == ReportType.MonthlySheet) {
                stepsStack.push(STEP_ID_CHECK_MONTH_SHEET_FOOTER_PRINTED);
                stepsStack.push(STEP_ID_PRINT_MONTH_SHEET_FOOTER);
                stepsStack.push(STEP_ID_AFTER_MONTH_SHEET_NOT_PRINTED);
            }
            stepsStack.push(reportTypes[i].getCode());
        }

        progressDialog = new FeedbackProgressDialog(this);
        progressDialog.setCancelable(false);

        denyScreenLock();
        startClosing(false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        deferredActionHandler.resume();
    }

    @Override
    protected void onPause() {
        deferredActionHandler.pause();
        super.onPause();
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case REQUEST_CODE_SET_TICKET_TAPE: {
                    boolean isTicketTapeSet = data.getBooleanExtra(AccountingTicketTapeStartActivity.EXTRA_IS_TICKET_TAPE_SET, false);
                    if (isTicketTapeSet) {
                        startClosing(true);
                    }
                    break;
                }
                case REQUEST_CODE_ACTIVATE_EKLZ: {
                    boolean isEKLZActivated = data.getBooleanExtra(SettingsPrinterActivity.EXTRA_IS_EKLZ_ACTIVATED, false);
                    if (isEKLZActivated) {
                        startClosing(true);
                    }
                    break;
                }
            }
        }
    }

    @Override
    public void onBackPressed() { /* NOP */ }

    private ReportType[] getReportTypes() {

        List<ReportType> fullReportTypesList = new ArrayList<>(Arrays.asList(Dagger.appComponent().commonSettingsStorage().get().getReportCloseMonth()));
        fullReportTypesList.remove(ReportType.EttShit);

        ReportType[] reportTypes = new ReportType[fullReportTypesList.size()];
        return fullReportTypesList.toArray(reportTypes);
    }

    private void startClosing(boolean continueClosing) {
        simpleLseView.setVisibility(View.GONE);
        if (continueClosing) {
            doStep(true, false);
        } else {
            showInitCloseMonthAskDialog();
        }
    }

    private void showInitCloseMonthAskDialog() {
        CppkDialogFragment cppkDialogFragment = CppkDialogFragment.getInstance(null,
                getString(R.string.month_close_ask_init),
                getString(R.string.Yes),
                getString(R.string.No),
                DIALOG_ID_ASK_INIT_CLOSE_MONTH,
                CppkDialogFragment.CppkDialogButtonStyle.HORIZONTAL);
        cppkDialogFragment.setCancelable(false);
        cppkDialogFragment.show(getFragmentManager(), null);
    }

    private void showCloseMonthSuccessDialog(int monthNumber) {
        CppkDialogFragment cppkDialogFragment = CppkDialogFragment.getInstance(null,
                String.format(getString(R.string.month_close_success_msg), monthNumber),
                getString(R.string.btnOk),
                null,
                DIALOG_ID_CLOSE_MONTH_SUCCESS,
                CppkDialogFragment.CppkDialogButtonStyle.VERTICAL);
        cppkDialogFragment.setCancelable(false);
        cppkDialogFragment.show(getFragmentManager(), null);
    }

    private void showPrintReportAskDialog(ReportType reportType) {

        String message = "";

        switch (reportType) {
            case DiscountedMonthlySheet:
                message = getString(R.string.month_close_ask_print_discount_month_sheet);
                break;

            case SheetBlankingMonth:
                message = getString(R.string.month_close_ask_print_clearing_month_sheet);
                break;

            case MonthlySheet:
                message = getString(R.string.month_close_ask_print_month_sheet);
                break;

            case BTMonthlySheet:
                message = getString(R.string.month_close_ask_print_bt_month_sheet);
                break;

            default:
                break;
        }

        CppkDialogFragment cppkDialogFragment = CppkDialogFragment.getInstance(null,
                message,
                getString(R.string.Yes),
                getString(R.string.No),
                DIALOG_ID_ASK_PRINT_REPORT,
                CppkDialogFragment.CppkDialogButtonStyle.HORIZONTAL);
        cppkDialogFragment.setCancelable(false);
        cppkDialogFragment.show(getFragmentManager(), null);
    }

    @Override
    public void onPositiveClick(DialogFragment dialog, int idDialog) {
        switch (idDialog) {
            case DIALOG_ID_ASK_INIT_CLOSE_MONTH: {
                doStep(false, false);
                break;
            }
            case DIALOG_ID_ASK_PRINT_REPORT: {
                doStep(true, false);
                break;
            }
            case DIALOG_ID_CLOSE_MONTH_SUCCESS: {
                Navigator.navigateToWelcomeActivity(this, false);
                finish();
                break;
            }

            default:
                break;
        }
    }

    @Override
    public void onNegativeClick(DialogFragment dialog, int idDialog) {
        switch (idDialog) {
            case DIALOG_ID_ASK_INIT_CLOSE_MONTH: {
                onCloseCanceled();
                break;
            }
            case DIALOG_ID_ASK_PRINT_REPORT: {
                doStep(false, true);
                break;
            }
            default:
                break;
        }
    }

    private void incrementCurrentStep() {
        stepsStack.pop();
    }

    protected boolean isRequiredByDefault(ReportType reportType) {

        boolean required;

        CommonSettings commonSettings = Dagger.appComponent().commonSettingsStorage().get();

        switch (reportType) {
            case SheetBlankingMonth:
                required = commonSettings.isSheetBlankingMonthClosingMonthReq();
                break;

            case DiscountedMonthlySheet:
                required = commonSettings.isDiscountMonthShiftSheetClosingMonthReq();
                break;

            case MonthlySheet:
                required = commonSettings.isMonthSheetClosingMonthReq();
                break;

            case BTMonthlySheet:
                required = commonSettings.isBtMonthlySheetClosingMonthReq();
                break;

            default:
                throw new IllegalArgumentException("reportType is not supported");
        }

        return required;
    }

    private void doStep(boolean isRequiredByUser, boolean next) {

        if (next) {
            incrementCurrentStep();
        }

        if (stepsStack.isEmpty()) {
            onCloseCanceled();
            return;
        }

        int currentStep = stepsStack.peek();

        switch (currentStep) {
            case STEP_ID_WRITE_EVENT_CLOSE_MONTH_TO_DB: {
                progressDialog.setMessage(getString(R.string.month_close_progress_closing_at_ptk));
                progressDialog.show();

                writeEventCloseMonthToDb();
                break;
            }
            case STEP_ID_AFTER_MONTH_SHEET_NOT_PRINTED: {
                // Если на шаге печати MonthlySheet нажать "Нет", то мы автоматически переходим к следующему шагу
                // Это как-бы костылек, что-бы удалить печать футера из стека, если ведомость не напечатали
                // Если раннее нажать "Да", то данный шаг будет удален по завершении printMonthSheet
                stepsStack.remove(Integer.valueOf(STEP_ID_PRINT_MONTH_SHEET_FOOTER)); // Пропускаем печать футера
                stepsStack.remove(Integer.valueOf(STEP_ID_CHECK_MONTH_SHEET_FOOTER_PRINTED)); // Пропускаем проверку, напечатан ли футер
                doStep(false, true);
                break;
            }
            case STEP_ID_PRINT_MONTH_SHEET_FOOTER: {
                progressDialog.setMessage(getString(R.string.month_close_progress_printing_month_sheet_footer));
                progressDialog.show();

                if (isRequiredByUser || isRequiredByDefault(ReportType.SheetBlankingMonth)) {
                    printMonthSheetFooter(true);
                } else if (silentMode) {
                    // включен режим тихого закрытия месяца, пропускаем печать данного отчета
                    printMonthSheetFooter(false);
                } else {
                    progressDialog.dismiss();

                    //запрашиваем разрешение
                    showPrintReportAskDialog(ReportType.SheetBlankingMonth);
                }
                break;
            }
            case STEP_ID_CHECK_MONTH_SHEET_FOOTER_PRINTED: {
                // Если на шаге STEP_ID_PRINT_MONTH_SHEET_FOOTER нажать "Нет", то мы автоматически переходим к следующему шагу
                // Это как-бы костылек, что-бы вызвать таки печать футера
                // Если раннее нажать "Да", то данный шаг будет удален по завершении printMonthSheetFooter
                progressDialog.setMessage(getString(R.string.month_close_progress_printing_month_sheet_footer));
                progressDialog.show();

                printMonthSheetFooter(false);
                break;
            }
            default: {
                ReportType currentReportType = ReportType.getByCode(currentStep);
                if (currentReportType == null || isRequiredByUser || isRequiredByDefault(currentReportType)) {
                    //сразу печатаем
                    printReport(currentReportType);
                } else if (silentMode) {
                    // включен режим тихого закрытия смены, пропускаем печать данного отчета
                    doStep(false, true);
                } else {
                    progressDialog.dismiss();

                    //запрашиваем разрешение
                    showPrintReportAskDialog(currentReportType);
                }
                break;
            }
        }
    }

    private void onCloseCompleted(int monthNumber) {
        Logger.info(TAG, "onCloseCompleted");
        showCloseMonthSuccessDialog(monthNumber);
        progressDialog.dismiss();
        allowScreenLock();
    }

    private void onCloseCanceled() {
        Logger.info(TAG, "onCloseCanceled");
        progressDialog.dismiss();
        finish();
    }

    private void onCloseFailed(Throwable e) {
        Logger.error(TAG, e.getMessage(), e);
        progressDialog.dismiss();

        SimpleLseView.State.Builder stateBuilder = new SimpleLseView.State.Builder();
        stateBuilder.setMode(SimpleLseView.State.MODE_ERROR);

        if (e instanceof PrinterIsNotConnectedException) {
            stateBuilder.setTextMessage(R.string.printer_not_found_msg);
            stateBuilder.setButton1(R.string.printer_repeat_connection, v -> startClosing(true));
        } else if (e instanceof TicketTapeChecker.TicketTapeIsNotSetException) {
            stateBuilder.setTextMessage(R.string.error_msg_ticket_tape_is_not_set);
            stateBuilder.setButton1(R.string.ticket_tape_is_not_set_view_set_ticket_tape_btn, v -> Navigator.navigateToAccountingTicketTapeStartActivity(this, null, REQUEST_CODE_SET_TICKET_TAPE, true));
        } else if (e instanceof IncorrectEKLZNumberException) {
            stateBuilder.setTextMessage(R.string.printer_eklz_activation_required_msg);
            stateBuilder.setButton1(R.string.printer_eklz_activation_required_activate, v -> Navigator.navigateToSettingsPrinterActivity(this, null, REQUEST_CODE_ACTIVATE_EKLZ));
        } else {
            stateBuilder.setTextMessage(R.string.month_close_failed_msg);
            stateBuilder.setButton1(R.string.sev_btn_repeat, v -> startClosing(true));
        }
        stateBuilder.setButton2(R.string.sev_btn_cancel, v -> finish());

        simpleLseView.setState(stateBuilder.build());
        simpleLseView.show();
    }

    private void printReport(ReportType reportType) {
        if (reportType == null) {
            throw new IllegalArgumentException("reportType is not supported");
        }
        switch (reportType) {
            case SheetBlankingMonth:
                progressDialog.setMessage(getString(R.string.month_close_progress_printing_clearing_month_sheet));
                printClearingMonthSheet();
                break;

            case DiscountedMonthlySheet:
                progressDialog.setMessage(getString(R.string.month_close_progress_printing_discount_month_sheet));
                printDiscountMonthSheet();
                break;

            case MonthlySheet:
                progressDialog.setMessage(getString(R.string.month_close_progress_printing_month_sheet));
                printMonthSheet();
                break;

            case BTMonthlySheet:
                progressDialog.setMessage(getString(R.string.month_close_progress_printing_bt_monthly_sheet));
                printBTMonthlySheet();
                break;

            default:
                throw new IllegalArgumentException("reportType is not supported");
        }

        progressDialog.show();

    }

    private void printMonthSheet() {
        new ReportShiftOrMonthlySheet(fineRepository)
                .setSheetTypeMonth()
                .setBuildForLastMonth()
                .buildAndPrintObservable(Di.INSTANCE.printerManager().getPrinter())
                .subscribeOn(SchedulersCPPK.printer())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<PrintShiftOrMonthSheetOperation.Result>() {
                    @Override
                    public void onCompleted() {
                        stepsStack.remove(Integer.valueOf(STEP_ID_AFTER_MONTH_SHEET_NOT_PRINTED)); // Пропускаем шаг удаления команд для печати футера
                        deferredActionHandler.post(() -> doStep(false, true));
                    }

                    @Override
                    public void onError(Throwable e) {
                        deferredActionHandler.post(() -> onCloseFailed(e));
                    }

                    @Override
                    public void onNext(PrintShiftOrMonthSheetOperation.Result result) {

                    }
                });
    }

    private void printClearingMonthSheet() {
        new ReportShiftOrMonthlyClearingSheet()
                .setBuildForLastMonth()
                .setSheetTypeMonth()
                .buildAndPrintObservable(Di.INSTANCE.printerManager().getPrinter())
                .subscribeOn(SchedulersCPPK.printer())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<PrintClearingSheetOperation.Result>() {
                    @Override
                    public void onCompleted() {
                        deferredActionHandler.post(() -> doStep(false, true));
                    }

                    @Override
                    public void onError(Throwable e) {
                        deferredActionHandler.post(() -> onCloseFailed(e));
                    }

                    @Override
                    public void onNext(PrintClearingSheetOperation.Result result) {

                    }
                });
    }

    private void printDiscountMonthSheet() {
        new ReportDiscountMonthSheet()
                .setBuildForLastMonth()
                .buildAndPrintObservable(Di.INSTANCE.printerManager().getPrinter())
                .subscribeOn(SchedulersCPPK.printer())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<PrintDiscountMonthSheetOperation.Result>() {
                    @Override
                    public void onCompleted() {
                        deferredActionHandler.post(() -> doStep(false, true));
                    }

                    @Override
                    public void onError(Throwable e) {
                        deferredActionHandler.post(() -> onCloseFailed(e));
                    }

                    @Override
                    public void onNext(PrintDiscountMonthSheetOperation.Result result) {

                    }
                });
    }

    private void printMonthSheetFooter(boolean isNextStepRequired) {
        PrintMonthSheetFooterOperation.Params params = new PrintMonthSheetFooterOperation.Params();
        params.isMonthClosed = isNextStepRequired;
        Dagger.appComponent().ticketTapeChecker().checkOrThrow()
                .andThen(Di.INSTANCE.printerManager().getOperationFactory()
                        .getPrintMonthSheetFooterOperation(params)
                        .call()
                )
                .subscribeOn(SchedulersCPPK.printer())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<PrintMonthSheetFooterOperation.Result>() {
                    @Override
                    public void onCompleted() {
                        stepsStack.remove(Integer.valueOf(STEP_ID_CHECK_MONTH_SHEET_FOOTER_PRINTED)); // Удаляем повторную печать футера
                        if (isNextStepRequired) {
                            deferredActionHandler.post(() -> doStep(true, true));
                        } else {
                            stepsStack.remove(Integer.valueOf(ReportType.SheetBlankingMonth.getCode())); // Пропускаем ведомость гашения месяца
                            stepsStack.remove(Integer.valueOf(STEP_ID_WRITE_EVENT_CLOSE_MONTH_TO_DB)); // Пропускаем запись события в БД
                            deferredActionHandler.post(() -> doStep(false, true));
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        deferredActionHandler.post(() -> onCloseFailed(e));
                    }

                    @Override
                    public void onNext(PrintMonthSheetFooterOperation.Result result) {

                    }
                });
    }

    private void printBTMonthlySheet() {
        new ReportBTMonthlySheet()
                .buildForLastMonth()
                .buildAndPrintObservable(Di.INSTANCE.printerManager().getPrinter())
                .subscribeOn(SchedulersCPPK.printer())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<PrintBtMonthReportOperation.Result>() {
                    @Override
                    public void onCompleted() {
                        deferredActionHandler.post(() -> doStep(false, true));
                    }

                    @Override
                    public void onError(Throwable error) {
                        deferredActionHandler.post(() -> onCloseFailed(error));
                    }

                    @Override
                    public void onNext(PrintBtMonthReportOperation.Result result) {
                    }
                });
    }


    /**
     * Запуск сборщика мусора, который удаляет из базы данных неактуальные данные после зкрытия месяца
     */
    private void startDatabaseGarbageCollector(Integer closedMonthNumber){
        Observable.create((Observable.OnSubscribe<Integer>) subscriber -> {
            try {

                // Количество месяцев от текущей даты, когда данные считаются не актуальными и их можно удалять
                int monthCount = Dagger.appComponent().commonSettings().getTermStoragePd();

                Calendar dateStart = Calendar.getInstance();
                dateStart.add(Calendar.MONTH, monthCount * -1);
                // получаем дату последнего закрытого месаца от даты dateStart
                Date archiveCloseMonthDate = new Date(Dagger.appComponent().localDaoSession().getMonthEventDao().getLastCloseMonthEventDate(dateStart.getTime()).getTime());

                final GCListener gcListener = message -> mainHandler.post(() -> {
                    progressDialog.setMessage(message);
                    progressDialog.show();
                });

                // вызываем сборщик мусора в базе данных (удаляет все данные, дата которых меньше firstCloseMonthDate и все не актуальные записи)
                new DBGarbageCollector<BaseEntityDao>(Dagger.appComponent().context(),
                        Dagger.appComponent().localDaoSession().getLocalDb(),
                        Dagger.appComponent().localDaoSession().getEntities(),
                        Dagger.appComponent().localDaoSession().getReferences(),
                        gcListener).execute(archiveCloseMonthDate);
                subscriber.onNext(closedMonthNumber);
            } catch (Throwable e){
                subscriber.onError(e);
            }
            subscriber.onCompleted();
        })
        .subscribeOn(SchedulersCPPK.background())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(new Subscriber<Integer>() {
            @Override
            public void onCompleted() {
            }

            @Override
            public void onError(Throwable e) {
                deferredActionHandler.post(() -> onCloseFailed(e));
            }

            @Override
            public void onNext(Integer monthNumber) {
                deferredActionHandler.post(() -> onCloseCompleted(monthNumber));
            }
        });
    }

    /**
     * Запись в БД события о закрытии месяца
     */
    private void writeEventCloseMonthToDb() {
        Di.INSTANCE.printerManager().getOperationFactory().getGetStateOperation()
                .call()
                .flatMap(result -> Observable.create(new Observable.OnSubscribe<Integer>() {
                    @Override
                    public void call(Subscriber<? super Integer> subscriber) {

                        MonthEventDao monthEventDao = getLocalDaoSession().getMonthEventDao();
                        MonthEvent month = monthEventDao.getLastMonthEvent();

                        if (month == null || month.getStatus() == MonthEvent.Status.CLOSED) {
                            throw new IllegalStateException("MonthEvent is null or already closed");
                        }

                        try {
                            Dagger.appComponent().localDbTransaction().runInTx(() -> {
                                Dagger.appComponent().monthEventCreator()
                                        .setStatus(MonthEvent.Status.CLOSED)
                                        .setMonthNumber(month.getMonthNumber())
                                        .setOpenDate(month.getOpenDate())
                                        .setCloseDate(result.getOperationTime())
                                        .setMonthId(month.getMonthId())
                                        .create();

                                Dagger.appComponent().monthEventCreator()
                                        .setStatus(MonthEvent.Status.OPENED)
                                        .setMonthNumber(month.getMonthNumber() + 1)
                                        .setOpenDate(result.getOperationTime())
                                        .setMonthId(UUID.randomUUID().toString())
                                        .create();
                            });
                            subscriber.onNext(month.getMonthNumber());
                        } catch (Throwable e) {
                            subscriber.onError(new Exception("monthEventDao.saveEvents(monthCloseEvent, openMonthEvent) = Exception: " + e.getMessage()));
                        }
                        subscriber.onCompleted();
                    }
                }))
                .subscribeOn(SchedulersCPPK.printer())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Integer>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        deferredActionHandler.post(() -> onCloseFailed(e));
                    }

                    @Override
                    public void onNext(Integer monthNumber) {
                        deferredActionHandler.post(() -> startDatabaseGarbageCollector(monthNumber));
                    }
                });
    }
}
