package ru.ppr.cppk.ui.activity.closeshift;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.LinearLayout;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Stack;

import ru.ppr.core.helper.Toaster;
import ru.ppr.core.ui.widget.SimpleLseView;
import ru.ppr.cppk.R;
import ru.ppr.cppk.di.Dagger;
import ru.ppr.cppk.di.Di;
import ru.ppr.cppk.dialogs.CppkDialogFragment;
import ru.ppr.cppk.entity.settings.ReportType;
import ru.ppr.cppk.helpers.CommonSettingsStorage;
import ru.ppr.cppk.helpers.DeferredActionHandler;
import ru.ppr.cppk.helpers.EmergencyModeHelper;
import ru.ppr.cppk.helpers.PaperUsageCounter;
import ru.ppr.cppk.helpers.SchedulersCPPK;
import ru.ppr.cppk.helpers.TicketTapeRestChecker;
import ru.ppr.cppk.localdb.model.PaperUsage;
import ru.ppr.cppk.localdb.model.ShiftEvent;
import ru.ppr.cppk.logic.ShiftManager;
import ru.ppr.cppk.logic.TicketTapeChecker;
import ru.ppr.cppk.managers.PosManager;
import ru.ppr.cppk.managers.PrinterManager;
import ru.ppr.cppk.model.PosOperationResult;
import ru.ppr.cppk.printer.exception.IncorrectEKLZNumberException;
import ru.ppr.cppk.printer.rx.operation.PrinterGetCashInFR;
import ru.ppr.cppk.printer.rx.operation.PrinterGetOdometerValue;
import ru.ppr.cppk.printer.rx.operation.bankSlip.PrinterPrintBankSlipOperation;
import ru.ppr.cppk.printer.rx.operation.clearingSheet.PrintClearingSheetOperation;
import ru.ppr.cppk.printer.rx.operation.closeShift.CloseShiftOperation;
import ru.ppr.cppk.printer.rx.operation.discountShiftSheet.PrintDiscountShiftSheetOperation;
import ru.ppr.cppk.printer.rx.operation.shiftOrMonthSheet.PrintShiftOrMonthSheetOperation;
import ru.ppr.cppk.reports.ReportDiscountShiftSheet;
import ru.ppr.cppk.reports.ReportShiftOrMonthlyClearingSheet;
import ru.ppr.cppk.reports.ReportShiftOrMonthlySheet;
import ru.ppr.cppk.settings.AccountingTicketTapeStartActivity;
import ru.ppr.cppk.systembar.FeedbackProgressDialog;
import ru.ppr.cppk.systembar.SystemBarActivity;
import ru.ppr.cppk.ui.activity.base.Navigator;
import ru.ppr.cppk.ui.dialog.SimpleDialog;
import ru.ppr.ikkm.exception.PrinterIsNotConnectedException;
import ru.ppr.ikkm.model.OfdDocsState;
import ru.ppr.ipos.model.TransactionResult;
import ru.ppr.logger.Logger;
import ru.ppr.nsi.repository.FineRepository;
import ru.ppr.security.entity.PermissionDvc;
import ru.ppr.security.entity.RoleDvc;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;

/**
 * Окно закрытия смены
 * <p>
 * Created by Александр on 14.12.2015.
 */
public class CloseShiftActivity extends SystemBarActivity
        implements CppkDialogFragment.CppkDialogClickListener {

    private static final String TAG = Logger.makeLogTag(CloseShiftActivity.class);

    // EXTRAS
    private static final String EXTRA_CLIENT_MODE = "EXTRA_CLIENT_MODE";
    private static final String EXTRA_AUTO_CLOSE_MODE = "EXTRA_AUTO_CLOSE_MODE";

    /**
     * Диалог: "Закрыть смену?"
     */
    private static final int DIALOG_ID_ASK_INIT_CLOSE_SHIFT = 1;
    /**
     * Диалог о необходимости печать отчета
     */
    private static final int DIALOG_ID_ASK_PRINT_REPORT = 2;
    /**
     * Диалог, смена успешно закрыта
     */
    private static final int DIALOG_ID_CLOSE_SHIFT_SUCCESS = 3;
    /**
     * Диалог, смена успешно закрыта, но требуется отправка данных в ОФД
     */
    private static final int DIALOG_ID_CLOSE_SHIFT_SUCCESS_NEED_SEND_DOCS_TO_OFD = 4;

    private static final int STEP_ID_PREPARE_FOR_CLOSE_SHIFT = 10001;
    private static final int STEP_ID_GET_ODOMETER_VALUE = 10002;
    private static final int STEP_ID_GET_CASH_IN_FR = 10003;
    private static final int STEP_ID_PRINT_Z_REPORT = 10004;
    private static final int STEP_ID_CLOSE_DAY_TERMINAL = 10005;
    private static final int STEP_ID_CREATE_CLOSE_SHIFT_EVENT_IN_DB = 10006;
    private static final int STEP_ID_UPDATE_CLOSE_SHIFT_EVENT_TO_COMPLETED = 10011;
    private static final int STEP_ID_REPEAL_MUST_HAVE_TO_SALES = 10007;
    private static final int STEP_ID_SYNC_FISCAL_DOC_STATE = 10008;
    private static final int STEP_ID_SEND_DOCS_TO_OFD_STATE = 10009;
    private static final int STEP_ID_CHECK_SHIFT_STATE = 10010;

    private static final int REQUEST_CODE_SET_TICKET_TAPE = 101;
    private static final int REQUEST_CODE_ACTIVATE_EKLZ = 102;
    private static final int REQUEST_CODE_CHANGE_PRINTER = 103;

    private Stack<Integer> stepsStack;
    private boolean silentMode;
    private boolean autoCloseMode;
    private FeedbackProgressDialog progressDialog;
    private SimpleLseView simpleLseView;
    private final DeferredActionHandler deferredActionHandler = new DeferredActionHandler();

    private CommonSettingsStorage commonSettingsStorage;
    private Toaster toaster;
    private PosManager posManager;
    private ShiftManager shiftManager;
    private PrinterManager printerManager;
    private PaperUsageCounter paperUsageCounter;
    private FineRepository fineRepository;
    private TicketTapeRestChecker ticketTapeRestChecker;

    public static Intent getCallingIntent(Context context, boolean silentMode, boolean autoCloseMode) {
        Intent intent = new Intent(context, CloseShiftActivity.class);
        intent.putExtra(EXTRA_CLIENT_MODE, silentMode);
        intent.putExtra(EXTRA_AUTO_CLOSE_MODE, autoCloseMode);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        commonSettingsStorage = Dagger.appComponent().commonSettingsStorage();
        toaster = Dagger.appComponent().toaster();
        posManager = Dagger.appComponent().posManager();
        shiftManager = Dagger.appComponent().shiftManager();
        printerManager = Dagger.appComponent().printerManager();
        paperUsageCounter = Dagger.appComponent().paperUsageCounter();
        fineRepository = Dagger.appComponent().fineRepository();
        ticketTapeRestChecker = Dagger.appComponent().ticketTapeRestChecker();

        setContentView(R.layout.activity_close_shift);
        resetRegisterReceiver();
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            silentMode = extras.getBoolean(EXTRA_CLIENT_MODE);
            autoCloseMode = extras.getBoolean(EXTRA_AUTO_CLOSE_MODE);
        }

        simpleLseView = (SimpleLseView) findViewById(R.id.simpleLseView);

        ReportType[] reportTypes = getReportTypes();

        boolean clearingSheetEnabled = false;
        for (ReportType reportType : reportTypes) {
            if (reportType == ReportType.SheetShiftBlanking) {
                clearingSheetEnabled = true;
                break;
            }
        }
        stepsStack = new Stack<>();
        stepsStack.push(STEP_ID_SEND_DOCS_TO_OFD_STATE);
        stepsStack.push(STEP_ID_UPDATE_CLOSE_SHIFT_EVENT_TO_COMPLETED);
        stepsStack.push(STEP_ID_PRINT_Z_REPORT);
        stepsStack.push(STEP_ID_CREATE_CLOSE_SHIFT_EVENT_IN_DB);
        stepsStack.push(STEP_ID_CLOSE_DAY_TERMINAL);
        if (!clearingSheetEnabled) {
            stepsStack.push(STEP_ID_GET_ODOMETER_VALUE);
        }
        for (int i = reportTypes.length - 1; i >= 0; i--) {
            stepsStack.push(reportTypes[i].getCode());
            if (reportTypes[i] == ReportType.SheetShiftBlanking) {
                stepsStack.push(STEP_ID_GET_ODOMETER_VALUE);
            }
        }
        if (!stepsStack.contains(STEP_ID_GET_ODOMETER_VALUE)) {
            stepsStack.push(STEP_ID_GET_ODOMETER_VALUE);
        }
        stepsStack.push(STEP_ID_REPEAL_MUST_HAVE_TO_SALES);
        stepsStack.push(STEP_ID_GET_CASH_IN_FR);
        stepsStack.push(STEP_ID_CHECK_SHIFT_STATE);
        stepsStack.push(STEP_ID_SYNC_FISCAL_DOC_STATE);
        stepsStack.push(STEP_ID_PREPARE_FOR_CLOSE_SHIFT);

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
        if (shiftManager.isShiftClosed()) {
            /**
             * Проверим так, ибо проверка REQUEST_CODE_ACTIVATE_EKLZ недостаточна при следующем сценарии:
             * Лента не установлена
             * Уходим её устанавливать
             * Оказывается, сменилась ЭКЛЗ
             * Выполняем активизацию ЭКЛЗ (Всё, смена теперь закрыта)
             * Печатаем отчет установки билетной ленты
             * Возвращаемся сюда с requestCode = REQUEST_CODE_SET_TICKET_TAPE
             */
            onNavigateToNextScreen();
            return;
        }
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case REQUEST_CODE_SET_TICKET_TAPE: {
                    boolean isTicketTapeSet = data.getBooleanExtra(AccountingTicketTapeStartActivity.EXTRA_IS_TICKET_TAPE_SET, false);
                    if (isTicketTapeSet) {
                        startClosing(true);
                    }
                    break;
                }
            }
        }
    }

    @Override
    public void onBackPressed() {/* NOP */ }

    private ReportType[] getReportTypes() {

        List<ReportType> fullReportTypesList = new ArrayList<>(Arrays.asList(commonSettingsStorage.get().getReportCloseShift()));
        fullReportTypesList.remove(ReportType.EttShit);

        ReportType[] reportTypes = new ReportType[fullReportTypesList.size()];
        return fullReportTypesList.toArray(reportTypes);
    }

    private void startClosing(boolean continueClosing) {
        simpleLseView.setVisibility(View.GONE);
        if (continueClosing) {
            doStep(true, false);
        } else if (silentMode) {
            doStep(false, false);
        } else {
            showInitCloseShiftAskDialog();
        }
    }

    private void showInitCloseShiftAskDialog() {
        CppkDialogFragment cppkDialogFragment = CppkDialogFragment.getInstance(null,
                getString(R.string.shift_close_ask_init),
                getString(R.string.Yes),
                getString(R.string.No),
                DIALOG_ID_ASK_INIT_CLOSE_SHIFT,
                CppkDialogFragment.CppkDialogButtonStyle.HORIZONTAL);
        cppkDialogFragment.setCancelable(false);
        cppkDialogFragment.show(getFragmentManager(), null);
    }

    /**
     * Покажет диалог о необходимости печати отчета об окончании билетной ленты.
     *
     * @param action - действие, которое нужно выполнить в случае успеха
     */
    private void checkTicketTapeAndExecute(Runnable action) {

        Logger.trace(TAG, "checkTicketTapeAndExecute()");

        if (ticketTapeRestChecker.check()) {
            deferredActionHandler.post(action);
            return;
        }

        SimpleDialog simpleDialog = SimpleDialog.newInstance(null,
                String.format(getString(R.string.shift_close_ask_ticketTape_attention_msg), commonSettingsStorage.get().getTicketTapeAttentionLength()),
                getString(R.string.shift_close_ask_ticketTape_attention_ok),
                getString(R.string.shift_close_ask_ticketTape_attention_no),
                LinearLayout.HORIZONTAL,
                0);
        simpleDialog.show(getFragmentManager(), SimpleDialog.FRAGMENT_TAG);
        simpleDialog.setDialogPositiveBtnClickListener((dialog, dialogId) -> {
            Navigator.navigateToAccountingTicketTapeEndActivity(this);
            finish();
        });
        simpleDialog.setDialogNegativeBtnClickListener((dialog, dialogId) -> {
            deferredActionHandler.post(action);
        });
        simpleDialog.setOnCancelListener(dialogInterface -> {
            deferredActionHandler.post(action);
        });
    }

    private void showCloseShiftSuccessDialog(int shiftNumber) {
        CppkDialogFragment cppkDialogFragment = CppkDialogFragment.getInstance(null,
                String.format(getString(R.string.shift_close_success_msg), shiftNumber),
                getString(R.string.btnOk),
                null,
                DIALOG_ID_CLOSE_SHIFT_SUCCESS,
                CppkDialogFragment.CppkDialogButtonStyle.VERTICAL);
        cppkDialogFragment.setCancelable(false);
        cppkDialogFragment.show(getFragmentManager(), null);
    }

    /**
     * Покажет диалок успешного закрытия смены, в случае когда есть документы, которые небыли отправлены в ОФД
     *
     * @param shiftNumber     - номер смены
     * @param unsentDocsCount - количество документов, которые небыли отправлены в ОФД
     */
    private void showCloseShiftSuccessWithUnsentDocsDialog(int shiftNumber, int unsentDocsCount) {
        CppkDialogFragment cppkDialogFragment = CppkDialogFragment.getInstance(null,
                String.format(getString(R.string.shift_close_success_with_unsent_docs_msg), shiftNumber, unsentDocsCount),
                getString(R.string.Yes),
                getString(R.string.No),
                DIALOG_ID_CLOSE_SHIFT_SUCCESS_NEED_SEND_DOCS_TO_OFD,
                CppkDialogFragment.CppkDialogButtonStyle.VERTICAL);
        cppkDialogFragment.setCancelable(false);
        cppkDialogFragment.show(getFragmentManager(), null);
    }

    /**
     * Покажет диалог успешного закрытия смены, в случае когда не удалось получить информацию по неотправленным в ОФД документам
     *
     * @param shiftNumber - номер смены
     */
    private void showCloseShiftSuccessWithErrorSendDocsToOfdDialog(int shiftNumber) {
        CppkDialogFragment cppkDialogFragment = CppkDialogFragment.getInstance(null,
                String.format(getString(R.string.shift_close_success_with_error_to_get_ofd_state_msg), shiftNumber),
                getString(R.string.Yes),
                getString(R.string.No),
                DIALOG_ID_CLOSE_SHIFT_SUCCESS_NEED_SEND_DOCS_TO_OFD,
                CppkDialogFragment.CppkDialogButtonStyle.VERTICAL);
        cppkDialogFragment.setCancelable(false);
        cppkDialogFragment.show(getFragmentManager(), null);
    }

    private void showPrintReportAskDialog(ReportType reportType) {

        String message = "";

        switch (reportType) {
            case DiscountedShiftShit:
                message = getString(R.string.shift_close_ask_print_discount_shift_sheet);
                break;

            case SheetShiftBlanking:
                message = getString(R.string.shift_close_ask_print_clearing_shift_sheet);
                break;

            case ShiftShit:
                message = getString(R.string.shift_close_ask_print_shift_sheet);
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
            case DIALOG_ID_ASK_INIT_CLOSE_SHIFT: {
                doStep(false, false);
                break;
            }
            case DIALOG_ID_ASK_PRINT_REPORT: {
                doStep(true, false);
                break;
            }
            case DIALOG_ID_CLOSE_SHIFT_SUCCESS: {
                onNavigateToNextScreen();
                break;
            }
            case DIALOG_ID_CLOSE_SHIFT_SUCCESS_NEED_SEND_DOCS_TO_OFD: {
                Navigator.navigateToSendDocsToOfdActivity(this, true);
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
            case DIALOG_ID_ASK_INIT_CLOSE_SHIFT: {
                onCloseCanceled();
                break;
            }
            case DIALOG_ID_ASK_PRINT_REPORT: {
                doStep(false, true);
                break;
            }
            case DIALOG_ID_CLOSE_SHIFT_SUCCESS_NEED_SEND_DOCS_TO_OFD: {
                onNavigateToNextScreen();
                finish();
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

        switch (reportType) {
            case SheetShiftBlanking:
                required = commonSettingsStorage.get().isSheetBlankingShiftClosingShiftReq();
                break;

            case DiscountedShiftShit:
                required = commonSettingsStorage.get().isDiscountShiftSheetClosingShiftReq();
                break;

            case ShiftShit:
                required = commonSettingsStorage.get().isSheetShiftCloseShiftReq();
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

        int currentStep = stepsStack.peek();

        switch (currentStep) {
            case STEP_ID_PREPARE_FOR_CLOSE_SHIFT: {
                prepareForCloseShift();
                break;
            }
            case STEP_ID_SYNC_FISCAL_DOC_STATE: {
                progressDialog.setMessage(getString(R.string.shift_close_progress_sync_fiscal_doc_state));
                progressDialog.show();

                syncFiscalDocState();
                break;
            }
            case STEP_ID_CHECK_SHIFT_STATE: {
                progressDialog.setMessage(getString(R.string.shift_close_progress_check_shift_state));
                progressDialog.show();

                checkShiftState();
                break;
            }
            case STEP_ID_CREATE_CLOSE_SHIFT_EVENT_IN_DB: {
                progressDialog.setMessage(getString(R.string.shift_close_progress_closing_at_ptk));
                progressDialog.show();

                createCloseShiftEvent();
                break;
            }
            case STEP_ID_UPDATE_CLOSE_SHIFT_EVENT_TO_COMPLETED: {
                progressDialog.setMessage(getString(R.string.shift_close_progress_closing_at_ptk));
                progressDialog.show();

                updateCloseShiftEventToCompleted();
                break;
            }
            case STEP_ID_CLOSE_DAY_TERMINAL: {
                if (di().getPrivateSettings().get().isPosEnabled()) {
                    progressDialog.setMessage(getString(R.string.shift_close_progress_closing_at_terminal));
                    progressDialog.show();

                    endDayOnTerminal();
                } else {
                    doStep(false, true);
                }

                break;
            }
            case STEP_ID_PRINT_Z_REPORT: {
                progressDialog.setMessage(getString(R.string.shift_close_progress_printing_z_report));
                progressDialog.show();

                printZReport();
                break;
            }
            case STEP_ID_SEND_DOCS_TO_OFD_STATE: {
                progressDialog.setMessage(getString(R.string.shift_close_progress_send_docs_to_ofd));
                progressDialog.show();

                sendDocsToOfd();

                break;
            }
            case STEP_ID_GET_ODOMETER_VALUE: {
                progressDialog.setMessage(getString(R.string.shift_close_progress_getting_odometer_value));
                progressDialog.show();

                getOdometerValueFromPrinter();
                break;
            }
            case STEP_ID_GET_CASH_IN_FR: {
                progressDialog.setMessage(getString(R.string.shift_close_progress_getting_cash_in_fr));
                progressDialog.show();

                getCashInFRFromPrinter();
                break;
            }
            case STEP_ID_REPEAL_MUST_HAVE_TO_SALES: {
                progressDialog.setMessage(getString(R.string.shift_close_progress_repealing_must_have_to_sales));
                progressDialog.show();

                repealMustHaveToSales();
                break;
            }
            default: {
                ReportType currentReportType = ReportType.getByCode(currentStep);
                if (isRequiredByUser || isRequiredByDefault(currentReportType)) {
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

    /**
     * Событие смена закрыта, требовалась отправка данных в ОФД
     *
     * @param shiftNumber  - номер смены
     * @param ofdDocsState - состояние отправки данных в ОФД
     */
    private void onCloseCompleted(int shiftNumber, OfdDocsState ofdDocsState) {
        Logger.info(TAG, "onCloseCompleted(shiftNumber=" + shiftNumber + ", ofdDocsState=" + ofdDocsState + ")");
        if (ofdDocsState == null)
            showCloseShiftSuccessWithErrorSendDocsToOfdDialog(shiftNumber);
        else if (ofdDocsState.getUnsentDocumentsCount() == 0)
            showCloseShiftSuccessDialog(shiftNumber);
        else
            showCloseShiftSuccessWithUnsentDocsDialog(shiftNumber, ofdDocsState.getUnsentDocumentsCount());
        progressDialog.dismiss();
        allowScreenLock();
    }

    /**
     * Событие смена закрыта, не требовалась отправка данных в ОФД
     *
     * @param shiftNumber
     */
    private void onCloseCompleted(int shiftNumber) {
        Logger.info(TAG, "onCloseCompleted(shiftNumber=" + shiftNumber + ")");
        showCloseShiftSuccessDialog(shiftNumber);
        progressDialog.dismiss();
        allowScreenLock();
    }

    private void onNavigateToNextScreen() {
        Navigator.navigateToWelcomeActivity(this, false);
        finish();
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

            RoleDvc role = di().getUserSessionInfo().getCurrentUser().getRole();
            if (getSecurityDaoSession().getRolePermissionDvcDao().isPermissionEnabled(role, PermissionDvc.ConfigFiscalRegister)) {
                stateBuilder.setButton3(R.string.shift_close_change_printer_btn, v -> Navigator.navigateToSettingsPrinterActivity(this, null, REQUEST_CODE_CHANGE_PRINTER));
            }
        } else if (e instanceof TicketTapeChecker.TicketTapeIsNotSetException) {
            stateBuilder.setTextMessage(R.string.error_msg_ticket_tape_is_not_set);
            stateBuilder.setButton1(R.string.ticket_tape_is_not_set_view_set_ticket_tape_btn, v -> Navigator.navigateToAccountingTicketTapeStartActivity(this, null, REQUEST_CODE_SET_TICKET_TAPE, true));
        } else if (e instanceof IncorrectEKLZNumberException) {
            stateBuilder.setTextMessage(R.string.printer_eklz_activation_required_msg);
            stateBuilder.setButton1(R.string.printer_eklz_activation_required_activate, v -> Navigator.navigateToSettingsPrinterActivity(this, null, REQUEST_CODE_ACTIVATE_EKLZ));
        } else {
            stateBuilder.setTextMessage(R.string.shift_close_failed_msg);
            stateBuilder.setButton1(R.string.sev_btn_repeat, v -> startClosing(true));
        }
        if (autoCloseMode) {
            stateBuilder.setButton2(R.string.sev_btn_emergency_mode, v -> EmergencyModeHelper.startEmergencyModeDirectly());
        } else {
            stateBuilder.setButton2(R.string.sev_btn_cancel, v -> finish());
        }

        simpleLseView.setState(stateBuilder.build());
        simpleLseView.show();
    }

    private void endDayOnTerminal() {
        Logger.trace(TAG, "endDayOnTerminal() START");
        if (posManager.isReady()) {
            Logger.info(TAG, "All right, listen to terminal and closing day normally");

            posManager.dayEnd(new PosManager.AbstractTransactionListener() {
                @Override
                public void onConnectionTimeout() {
                    runOnUiThread(() -> {
                        toaster.showToast(R.string.shift_close_bt_day_close_failed_msg);
                        doStep(false, true);
                    });
                }

                @Override
                public void onResult(@NonNull PosOperationResult<TransactionResult> operationResult) {
                    runOnUiThread(() -> {
                        TransactionResult result = operationResult.getTransactionResult();
                        if (result != null && result.isApproved() &&
                                result.getReceipt() != null && !result.getReceipt().isEmpty()) {
                            printTerminalDayEndSlip(result.getReceipt());
                        } else {
                            doStep(false, true);
                        }
                    });
                }
            });
        } else {
            Logger.info(TAG, "Terminal is busy, dont listen to terminal and just skip the step");
            toaster.showToast(R.string.shift_close_bt_day_close_failed_msg);

            doStep(false, true);
        }
    }

    private void printReport(ReportType reportType) {
        if (reportType == null) {
            throw new IllegalArgumentException("Null report type");
        }
        switch (reportType) {
            case SheetShiftBlanking:
                progressDialog.setMessage(getString(R.string.shift_close_progress_printing_clearing_shift_sheet));
                printClearingShiftSheet();
                break;

            case DiscountedShiftShit:
                progressDialog.setMessage(getString(R.string.shift_close_progress_printing_discount_shift_sheet));
                printDiscountShiftSheet();
                break;

            case ShiftShit:
                progressDialog.setMessage(getString(R.string.shift_close_progress_printing_shift_sheet));
                printShiftSheet();
                break;
            default:
                throw new IllegalArgumentException("reportType is not supported");
        }
        progressDialog.show();
    }

    private void printTerminalDayEndSlip(List<String> receipt) {
        Logger.trace(TAG, "printTerminalDayEndSlip() START");
        Dagger.appComponent().ticketTapeChecker().checkOrThrow()
                .andThen(Observable.fromCallable(() -> {
                    PrinterPrintBankSlipOperation.Params params = new PrinterPrintBankSlipOperation.Params();

                    params.tplParams.slipLines = receipt;

                    return params;
                }))
                .flatMap(params -> printerManager.getOperationFactory()
                        .getPrintBankSlipOperation(params)
                        .call()
                )
                .subscribeOn(SchedulersCPPK.printer())
                .observeOn(AndroidSchedulers.mainThread())
                .onErrorReturn(throwable -> null)
                .subscribe(new Subscriber<Object>() {
                               @Override
                               public void onCompleted() {
                                   Logger.trace(TAG, "doStep - printTerminalDayEndSlip onCompleted");
                                   checkTicketTapeAndExecute(() -> doStep(false, true));
                               }

                               @Override
                               public void onError(Throwable e) {
                                   deferredActionHandler.post(() -> onCloseFailed(e));
                               }

                               @Override
                               public void onNext(Object o) {

                               }
                           }
                );
    }

    private void printShiftSheet() {
        Logger.trace(TAG, "printShiftSheet() START");
        new ReportShiftOrMonthlySheet(fineRepository)
                .setSheetTypeShift()
                .setBuildForLastShift()
                .buildAndPrintObservable(printerManager.getPrinter())
                .subscribeOn(SchedulersCPPK.printer())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<PrintShiftOrMonthSheetOperation.Result>() {
                    @Override
                    public void onCompleted() {
                        checkTicketTapeAndExecute(() -> doStep(false, true));
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

    /**
     * Запускает проверку текущего состояния смены (открыта/закрыта)
     */
    private void checkShiftState() {
        Logger.trace(TAG, "checkShiftState() START");
        if (shiftManager.isShiftClosed()) {
            Logger.warning(TAG, "checkShiftState() Смена уже закрыта, поэтому пропустим все шаги включая обновление статуса на COMPLETED в БД");
            while (stepsStack.peek() != STEP_ID_UPDATE_CLOSE_SHIFT_EVENT_TO_COMPLETED)
                incrementCurrentStep();
        }
        doStep(false, true);
    }

    private void prepareForCloseShift() {
        Logger.trace(TAG, "prepareForCloseShift() START");
        shiftCloseTempData = new ShiftCloseTempData();

        doStep(false, true);
    }

    private void syncFiscalDocState() {
        Logger.trace(TAG, "syncFiscalDocState() START");
        Dagger.appComponent().fiscalDocStateSynchronizer().rxSyncCheckState()
                .subscribeOn(SchedulersCPPK.printer())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Object>() {
                    @Override
                    public void onCompleted() {
                        deferredActionHandler.post(() -> doStep(false, true));
                    }

                    @Override
                    public void onError(Throwable e) {
                        deferredActionHandler.post(() -> onCloseFailed(e));
                    }

                    @Override
                    public void onNext(Object o) {

                    }
                });
    }

    private void getOdometerValueFromPrinter() {
        Logger.trace(TAG, "getOdometerValueFromPrinter() START");
        printerManager.getOperationFactory().getGetOdometerValue()
                .call()
                .subscribeOn(SchedulersCPPK.printer())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<PrinterGetOdometerValue.Result>() {
                    @Override
                    public void onCompleted() {
                        deferredActionHandler.post(() -> doStep(false, true));
                    }

                    @Override
                    public void onError(Throwable e) {
                        deferredActionHandler.post(() -> onCloseFailed(e));
                    }

                    @Override
                    public void onNext(PrinterGetOdometerValue.Result result) {
                        paperUsageCounter.setCurrentOdometerValueBeforePrinting(result.getOdometerValue());
                        PaperUsage paperUsage = paperUsageCounter.getPaperUsage(PaperUsage.ID_SHIFT);
                        shiftCloseTempData.paperConsumption = paperUsage.getPaperLength();
                        shiftCloseTempData.paperCounterRestarted = paperUsage.isRestarted();
                    }
                });
    }

    private void getCashInFRFromPrinter() {
        Logger.trace(TAG, "getCashInFRFromPrinter() START");
        printerManager.getOperationFactory().getGetCashInFrOperation()
                .call()
                .subscribeOn(SchedulersCPPK.printer())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<PrinterGetCashInFR.Result>() {
                    @Override
                    public void onCompleted() {
                        deferredActionHandler.post(() -> doStep(false, true));
                    }

                    @Override
                    public void onError(Throwable e) {
                        deferredActionHandler.post(() -> onCloseFailed(e));
                    }

                    @Override
                    public void onNext(PrinterGetCashInFR.Result result) {
                        shiftCloseTempData.cashInFR = result.getCashInFR();
                    }
                });
    }

    private void printClearingShiftSheet() {
        Logger.trace(TAG, "printClearingShiftSheet() START");
        new ReportShiftOrMonthlyClearingSheet()
                .setBuildForLastShift()
                .setSheetTypeShift()
                .setPaperConsumption(shiftCloseTempData.paperConsumption)
                .buildAndPrintObservable(printerManager.getPrinter())
                .subscribeOn(SchedulersCPPK.printer())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<PrintClearingSheetOperation.Result>() {
                    @Override
                    public void onCompleted() {
                        checkTicketTapeAndExecute(() -> doStep(false, true));
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

    private void printDiscountShiftSheet() {
        Logger.trace(TAG, "printDiscountShiftSheet() START");
        new ReportDiscountShiftSheet()
                .setBuildForLastShift()
                .buildAndPrintObservable(printerManager.getPrinter())
                .subscribeOn(SchedulersCPPK.printer())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<PrintDiscountShiftSheetOperation.Result>() {
                    @Override
                    public void onCompleted() {
                        checkTicketTapeAndExecute(() -> doStep(false, true));
                    }

                    @Override
                    public void onError(Throwable e) {
                        deferredActionHandler.post(() -> onCloseFailed(e));
                    }

                    @Override
                    public void onNext(PrintDiscountShiftSheetOperation.Result result) {

                    }
                });
    }

    private void printZReport() {
        Logger.trace(TAG, "printZReport() START");
        Dagger.appComponent().ticketTapeChecker().checkOrThrow()
                .andThen(Observable.defer(() -> {
                    CloseShiftOperation.Params params = new CloseShiftOperation.Params(Di.INSTANCE.fiscalHeaderParamsBuilder().build(), true);
                    return printerManager.getOperationFactory().getCloseShiftOperation(params).call();
                }))
                .subscribeOn(SchedulersCPPK.printer())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<CloseShiftOperation.Result>() {
                    @Override
                    public void onCompleted() {
                        zReportPrinted();
                    }

                    @Override
                    public void onError(Throwable e) {
                        deferredActionHandler.post(() -> onCloseFailed(e));
                    }

                    @Override
                    public void onNext(CloseShiftOperation.Result result) {
                        shiftCloseTempData.operationTime = result.getOperationTime();
                        shiftCloseTempData.spndNumber = result.getSpndNumber();
                    }
                });
    }

    private void zReportPrinted() {
        Logger.trace(TAG, "zReportPrinted() START");
        // За данной операцией следует запись события в БД. Это не требует работы в UI,
        // будет лучше, если в этом месте событие попадет сразу в БД
        checkTicketTapeAndExecute(() -> doStep(false, true));
    }

    /**
     * Процесс отправки данных в ОФД
     */
    private void sendDocsToOfd() {
        Logger.trace(TAG, "sendDocsToOfd() START");
        if (printerManager.getPrinter().isFederalLaw54Supported()) {
            printerManager.getOperationFactory().getSendDocsToOfdOperation(30).call()
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(SchedulersCPPK.printer())
                    .subscribe(ofdDocsState -> {
                        deferredActionHandler.post(() -> onCloseCompleted(shiftCloseTempData.shiftNumber, ofdDocsState));
                    }, throwable -> {
                        deferredActionHandler.post(() -> onCloseCompleted(shiftCloseTempData.shiftNumber, null));
                    });
        } else {
            deferredActionHandler.post(() -> onCloseCompleted(shiftCloseTempData.shiftNumber));
        }
    }

    /**
     * Создаст заись в БД события о закрытии смены со статусом PRE_PRINTING
     */
    private void createCloseShiftEvent() {
        Logger.trace(TAG, "createCloseShiftEvent() START");
        clearUncompletedSales()
                .flatMap(aVoid ->
                        Observable.fromCallable(() -> {
                            return shiftManager.createCloseShiftEvent(shiftCloseTempData.paperConsumption, shiftCloseTempData.paperCounterRestarted, shiftCloseTempData.cashInFR);
                        })
                )
                .subscribeOn(SchedulersCPPK.printer())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<ShiftEvent>() {
                    @Override
                    public void onCompleted() {
                        deferredActionHandler.post(() -> doStep(false, true));
                    }

                    @Override
                    public void onError(Throwable e) {
                        deferredActionHandler.post(() -> onCloseFailed(e));
                    }

                    @Override
                    public void onNext(ShiftEvent closeShiftEvent) {
                        shiftCloseTempData.shiftNumber = closeShiftEvent.getShiftNumber();
                        shiftCloseTempData.shiftEventId = closeShiftEvent.getId();
                    }
                });
    }

    /**
     * Обновление статуса о закрытии смены с PRE_PRINTING на COMPLETED
     */
    private void updateCloseShiftEventToCompleted() {
        Logger.trace(TAG, "updateCloseShiftEventToCompleted() START");
        clearUncompletedSales()
                .flatMap(aVoid ->
                        Observable.fromCallable(() -> {
                            shiftManager.closeShiftEventUpdateToComplete(shiftCloseTempData.operationTime, shiftCloseTempData.shiftEventId, shiftCloseTempData.spndNumber);
                            return shiftManager.getCurrentShiftNumber();
                        })
                )
                .subscribeOn(SchedulersCPPK.printer())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Integer>() {
                    @Override
                    public void onCompleted() {
                        deferredActionHandler.post(() -> doStep(false, true));
                    }

                    @Override
                    public void onError(Throwable e) {
                        deferredActionHandler.post(() -> onCloseFailed(e));
                    }

                    @Override
                    public void onNext(Integer shiftNumber) {
                        shiftCloseTempData.shiftNumber = shiftNumber;
                    }
                });
    }

    /**
     * Аннулирует недоаннулированные продажи, совершенные с помощью БТ
     */
    public void repealMustHaveToSales() {
        Logger.trace(TAG, "repealMustHaveToSales() START");
        Dagger.appComponent().completePdRepealEventInteractor().completeEvents()
                .subscribeOn(SchedulersCPPK.printer())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Object>() {
                    @Override
                    public void onCompleted() {
                        deferredActionHandler.post(() -> doStep(false, true));
                    }

                    @Override
                    public void onError(Throwable e) {
                        deferredActionHandler.post(() -> onCloseFailed(e));
                    }

                    @Override
                    public void onNext(Object o) {
                    }
                });
    }

    /**
     * Подчищает недоделанные продажи
     *
     * @return Observable
     */
    private Observable<Void> clearUncompletedSales() {
        Logger.trace(TAG, "clearUncompletedSales() START");
        return Observable.fromCallable(() -> {
            getLocalDaoSession().getCppkTicketSaleDao().clearUncompletedSales();
            return (Void) null;
        }).onErrorReturn(throwable -> null);
    }

    private static class ShiftCloseTempData {
        long paperConsumption;
        boolean paperCounterRestarted;
        BigDecimal cashInFR = BigDecimal.ZERO;
        Date operationTime;
        int shiftNumber;
        long shiftEventId;
        Integer spndNumber;
    }

    private ShiftCloseTempData shiftCloseTempData;
}
