package ru.ppr.cppk.settings;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import java.util.UUID;

import ru.ppr.core.ui.widget.SimpleLseView;
import ru.ppr.cppk.BuildConfig;
import ru.ppr.cppk.Globals;
import ru.ppr.cppk.Holder;
import ru.ppr.cppk.R;
import ru.ppr.cppk.di.Dagger;
import ru.ppr.cppk.di.Di;
import ru.ppr.cppk.entity.settings.PrivateSettings;
import ru.ppr.cppk.helpers.SchedulersCPPK;
import ru.ppr.cppk.localdb.model.PaperUsage;
import ru.ppr.cppk.localdb.model.ShiftEvent;
import ru.ppr.cppk.logic.DocumentTestPd;
import ru.ppr.cppk.logic.ShiftManager;
import ru.ppr.cppk.printer.exception.IncorrectEKLZNumberException;
import ru.ppr.cppk.reports.ReportTicketTapeStart;
import ru.ppr.cppk.systembar.FeedbackProgressDialog;
import ru.ppr.cppk.systembar.SystemBarActivity;
import ru.ppr.cppk.ui.activity.base.Navigator;
import ru.ppr.cppk.ui.dialog.SimpleDialog;
import ru.ppr.ikkm.exception.PrinterIsNotConnectedException;
import ru.ppr.logger.Logger;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;

public class AccountingTicketTapeStartActivity extends SystemBarActivity {

    private static final String TAG = Logger.makeLogTag(AccountingTicketTapeStartActivity.class);

    private static final int OPERATION_PRINT_TEST_PD = 1;
    private static final int OPERATION_PRINT_TAPE_START_SHEET = 2;

    private static final int REQUEST_CODE_SET_TICKET_TAPE = 101;
    private static final int REQUEST_CODE_ACTIVATE_EKLZ = 102;

    // EXTRAS
    private static final String EXTRA_BREAKING_MODE = "EXTRA_BREAKING_MODE";
    public static final String EXTRA_IS_TICKET_TAPE_SET = "EXTRA_IS_TICKET_TAPE_SET";

    private ViewHolder viewHolder;

    Globals globals;
    private boolean breakingMode = false;

    private FeedbackProgressDialog progressDialog;
    private int currentOperation;
    private SimpleLseView simpleLseView;

    private Holder<PrivateSettings> privateSettingsHolder;

    public static Intent getCallingIntent(Context context, boolean breakingMode) {
        return new Intent(context, AccountingTicketTapeStartActivity.class).putExtra(EXTRA_BREAKING_MODE, breakingMode);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        privateSettingsHolder = Globals.getInstance().getPrivateSettingsHolder();

        setContentView(R.layout.accounting_ticket_tape_start);
        resetRegisterReceiver();

        breakingMode = getIntent().getBooleanExtra(EXTRA_BREAKING_MODE, false);

        globals = (Globals) getApplication();

        viewHolder = new ViewHolder();
        viewHolder.buttonsLayout = (ViewGroup) findViewById(R.id.buttons);
        viewHolder.ticketTapeSeries = (EditText) findViewById(R.id.ticket_tape_series);
        viewHolder.ticketTapeNumber = (EditText) findViewById(R.id.ticket_tape_number);
        viewHolder.printTapeStart = (Button) findViewById(R.id.print_tape_start);
        viewHolder.openShift = (Button) findViewById(R.id.openShift);
        viewHolder.beginWork = (Button) findViewById(R.id.beginWork);
        viewHolder.continue_work = (Button) findViewById(R.id.continue_work);
        viewHolder.printPD = (Button) findViewById(R.id.print_pd);

        //для релизной прошивки сотрем хинты: https://aj.srvdev.ru/browse/CPPKPP-25759
        if (!BuildConfig.DEBUG) {
            viewHolder.ticketTapeSeries.setHint("");
            viewHolder.ticketTapeNumber.setHint("");
        }

        if (breakingMode) {
            viewHolder.openShift.setVisibility(View.GONE);
            viewHolder.beginWork.setVisibility(View.GONE);
            viewHolder.continue_work.setVisibility(View.GONE);
            viewHolder.printPD.setVisibility(View.GONE);
        } else {
            if (ShiftManager.getInstance().isShiftClosed()) {
                // смена закрыта
                viewHolder.openShift.setVisibility(View.GONE);
                viewHolder.beginWork.setVisibility(View.GONE);
                viewHolder.continue_work.setVisibility(View.GONE);
                viewHolder.printPD.setVisibility(View.GONE);
            } else {
                // смена открыта
                viewHolder.openShift.setVisibility(View.GONE);
                viewHolder.beginWork.setVisibility(View.GONE);
                viewHolder.continue_work.setVisibility(View.GONE);
                viewHolder.printPD.setVisibility(View.GONE);
            }
        }

        simpleLseView = (SimpleLseView) findViewById(R.id.simpleLseView);

        progressDialog = new FeedbackProgressDialog(this);
        progressDialog.setCancelable(false);
    }

    private boolean isValidSeries() {
        String regEx = "^(?:15|05|02)0(?:(?:1[4-9])|(?:[2-9][0-9]))(?:ЯН|ФВ|МР|АП|МА|ИН|ИЛ|АВ|СН|ОК|НР|ДР)$";
        String text = viewHolder.ticketTapeSeries.getText().toString();
        return text.matches(regEx);
    }

    private boolean isValidNumber() {
        String regEx = "^\\d{6}$";
        String text = viewHolder.ticketTapeNumber.getText().toString();
        return text.matches(regEx) && Integer.valueOf(text) > 0;
    }

    private void showAlertDialog(int msgResId) {
        SimpleDialog simpleDialog = SimpleDialog.newInstance(null, getString(msgResId), getString(R.string.dialog_close), null, LinearLayout.VERTICAL, 0);
        simpleDialog.show(getFragmentManager(), SimpleDialog.FRAGMENT_TAG);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_ENTER:
                checkNumberAndSeries();
                break;
            default:
                return super.onKeyUp(keyCode, event);
        }
        return true;
    }

    private void checkNumberAndSeries() {
        boolean isValidSeries = isValidSeries();
        boolean isValidNumber = isValidNumber();

        if (isValidSeries && isValidNumber) {
            viewHolder.buttonsLayout.setVisibility(View.VISIBLE);
        } else {
            if (!isValidSeries) {
                showAlertDialog(R.string.invalid_series);
            } else {
                showAlertDialog(R.string.invalid_number);
            }
        }
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.openShift:
                openShift();
                break;

            case R.id.beginWork:
                beginWork();
                break;

            case R.id.continue_work:
                continueWork();
                break;

            case R.id.print_pd:
                currentOperation = OPERATION_PRINT_TEST_PD;
                startOperation();
                break;

            case R.id.print_tape_start:
                currentOperation = OPERATION_PRINT_TAPE_START_SHEET;
                startOperation();
                break;

            default:
                break;
        }
    }

    private void startOperation() {
        simpleLseView.hide();
        switch (currentOperation) {
            case OPERATION_PRINT_TEST_PD: {
                printPd();
                break;
            }
            case OPERATION_PRINT_TAPE_START_SHEET: {
                printTapeStart();
                break;
            }
        }
    }

    private void openShift() {
        Navigator.navigateToWelcomeActivity(this, true);
    }

    private void beginWork() {
        Navigator.navigateToMenuActivity(this);
    }

    private void continueWork() {
        finish();
    }

    private void printPd() {

        progressDialog.show();

        Dagger.appComponent().ticketTapeChecker().checkOrThrow()
                .andThen(Dagger.appComponent().fiscalDocStateSynchronizer().rxSyncCheckState())
                .flatMapCompletable(aBoolean -> Dagger.appComponent().printTestCheckInteractor().print())
                .subscribeOn(SchedulersCPPK.printer())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<DocumentTestPd>() {
                               @Override
                               public void onCompleted() {
                                   progressDialog.dismiss();
                               }

                               @Override
                               public void onError(Throwable e) {
                                   onOperationFailed(e);
                               }

                               @Override
                               public void onNext(DocumentTestPd documentTestPd) {
                                   // NOP
                               }
                           }

                );
    }

    private void printTapeStart() {

        final int number = Integer.valueOf(viewHolder.ticketTapeNumber.getText().toString());
        final String series = viewHolder.ticketTapeSeries.getText().toString();

        progressDialog.show();

        Di.INSTANCE.printerManager().getOperationFactory().getGetOdometerValue()
                .call()
                .flatMap(result -> {
                    Globals.getInstance().getPaperUsageCounter().setCurrentOdometerValueBeforePrinting(result.getOdometerValue());
                    Globals.getInstance().getPaperUsageCounter().resetPaperUsage(PaperUsage.ID_TAPE);
                    PaperUsage paperUsage = Globals.getInstance().getPaperUsageCounter().getPaperUsage(PaperUsage.ID_TAPE);
                    long paperConsumption = paperUsage.getPaperLength();
                    boolean paperCounterRestarted = paperUsage.isRestarted();

                    int firstDocNumber = Di.INSTANCE.documentNumberProvider().getNextDocumentNumber();

                    Integer shiftNum = null;
                    ShiftEvent shiftEvent = getLocalDaoSession().getShiftEventDao().getLastShiftEvent(ShiftEvent.ShiftProgressStatus.FINISHED_STATUSES);
                    if (shiftEvent != null) {
                        if (shiftEvent.getStatus() == ShiftEvent.Status.STARTED || shiftEvent.getStatus() == ShiftEvent.Status.TRANSFERRED) {
                            // Устанавливаем номер смены только если смена открыта
                            shiftNum = shiftEvent.getShiftNumber();
                        }
                    }

                    return new ReportTicketTapeStart()
                            .setShiftNum(shiftNum)
                            .setFirstDocNumber(firstDocNumber)
                            .setTapeSeries(series)
                            .setTapeNumber(number)
                            .buildAndPrintObservable(Di.INSTANCE.printerManager().getPrinter())
                            .doOnNext(result1 -> Dagger.appComponent().ticketTapeEventCreator()
                                    .setTicketTapeId(UUID.randomUUID().toString())
                                    .setSeries(series)
                                    .setNumber(number)
                                    .setExpectedFirstDocNumber(firstDocNumber)
                                    .setPaperConsumption(paperConsumption)
                                    .setPaperCounterRestarted(paperCounterRestarted)
                                    .setStartTime(result1.getOperationTime())
                                    .create())
                            .flatMap(result1 -> Observable.just((Void) null));
                })
                .subscribeOn(SchedulersCPPK.printer())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Void>() {
                    @Override
                    public void onCompleted() {

                        if (breakingMode) {
                            viewHolder.continue_work.setVisibility(View.VISIBLE);
                        } else {
                            if (ShiftManager.getInstance().isShiftClosed()) {
                                viewHolder.openShift.setVisibility(View.VISIBLE);
                            } else {
                                viewHolder.beginWork.setVisibility(View.VISIBLE);
                                viewHolder.printPD.setVisibility(View.VISIBLE);
                            }
                        }

                        viewHolder.ticketTapeSeries.setEnabled(false);
                        viewHolder.ticketTapeSeries.setHint(series);
                        viewHolder.ticketTapeNumber.setEnabled(false);
                        viewHolder.ticketTapeNumber.setHint(String.valueOf(number));

                        progressDialog.dismiss();

                        viewHolder.printTapeStart.setVisibility(View.GONE);

                        Intent intent = new Intent();
                        intent.putExtra(EXTRA_IS_TICKET_TAPE_SET, true);
                        setResult(RESULT_OK, intent);
                    }

                    @Override
                    public void onError(Throwable e) {
                        onOperationFailed(e);
                    }

                    @Override
                    public void onNext(Void aVoid) {
                        /*NOP*/
                    }
                });
    }

    private void onOperationFailed(Throwable e) {
        Logger.error(TAG, e);
        progressDialog.dismiss();

        SimpleLseView.State.Builder stateBuilder = new SimpleLseView.State.Builder();
        stateBuilder.setMode(SimpleLseView.State.MODE_ERROR);

        if (e instanceof PrinterIsNotConnectedException) {
            stateBuilder.setTextMessage(R.string.printer_not_found_msg);
            stateBuilder.setButton1(R.string.printer_repeat_connection, v -> startOperation());
        } else if (e instanceof IncorrectEKLZNumberException) {
            stateBuilder.setTextMessage(R.string.printer_eklz_activation_required_msg);
            stateBuilder.setButton1(R.string.printer_eklz_activation_required_activate,
                    v -> Navigator.navigateToSettingsPrinterActivity(this, null, REQUEST_CODE_ACTIVATE_EKLZ));
        } else {
            if (currentOperation == OPERATION_PRINT_TAPE_START_SHEET) {
                stateBuilder.setTextMessage(R.string.accounting_ticket_tape_start_print_tape_start_sheet_error);
                stateBuilder.setButton1(R.string.sev_btn_repeat, v -> startOperation());
            } else {
                stateBuilder.setTextMessage(R.string.shift_open_failed_msg);
                stateBuilder.setButton1(R.string.sev_btn_repeat, v -> startOperation());
            }
        }
        stateBuilder.setButton2(R.string.sev_btn_cancel, v -> simpleLseView.hide());

        simpleLseView.setState(stateBuilder.build());
        simpleLseView.show();
    }

    class ViewHolder {
        ViewGroup buttonsLayout;
        EditText ticketTapeSeries;
        EditText ticketTapeNumber;
        Button openShift;
        Button beginWork;
        Button continue_work;
        Button printPD;
        Button printTapeStart;
    }

}
