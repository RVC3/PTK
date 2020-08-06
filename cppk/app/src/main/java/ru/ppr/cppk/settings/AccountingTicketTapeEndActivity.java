package ru.ppr.cppk.settings;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import ru.ppr.core.ui.widget.SimpleLseView;
import ru.ppr.cppk.Globals;
import ru.ppr.cppk.Holder;
import ru.ppr.cppk.R;
import ru.ppr.cppk.di.Dagger;
import ru.ppr.cppk.di.Di;
import ru.ppr.cppk.entity.settings.PrivateSettings;
import ru.ppr.cppk.helpers.SchedulersCPPK;
import ru.ppr.cppk.localdb.model.PaperUsage;
import ru.ppr.cppk.localdb.model.TicketTapeEvent;
import ru.ppr.cppk.logic.ShiftManager;
import ru.ppr.cppk.logic.TicketTapeChecker;
import ru.ppr.cppk.printer.exception.IncorrectEKLZNumberException;
import ru.ppr.cppk.printer.rx.operation.tapeEndReport.PrintTapeEndReportOperation;
import ru.ppr.cppk.reports.ReportTicketTapeEnd;
import ru.ppr.cppk.systembar.FeedbackProgressDialog;
import ru.ppr.cppk.systembar.SystemBarActivity;
import ru.ppr.cppk.ui.activity.base.Navigator;
import ru.ppr.ikkm.exception.PrinterIsNotConnectedException;
import ru.ppr.logger.Logger;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;

/**
 * Окно печати отчета окончания билетной ленты
 */
public class AccountingTicketTapeEndActivity extends SystemBarActivity {

    private static final String TAG = Logger.makeLogTag(AccountingTicketTapeEndActivity.class);

    private static final int REQUEST_CODE_SET_TICKET_TAPE = 101;
    private static final int REQUEST_CODE_ACTIVATE_EKLZ = 102;

    private ViewHolder viewHolder;

    TicketTapeEvent ticketTapeEventStart;
    Globals globals;
    private long paperConsumption;
    private boolean paperCounterRestarted;

    private FeedbackProgressDialog progressDialog;
    private SimpleLseView simpleLseView;

    private Holder<PrivateSettings> privateSettingsHolder;

    public static Intent getCallingIntent(Context context) {
        Intent intent = new Intent(context, AccountingTicketTapeEndActivity.class);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        privateSettingsHolder = Globals.getInstance().getPrivateSettingsHolder();

        setContentView(R.layout.accounting_ticket_tape_end);
        viewHolder = new ViewHolder();
        viewHolder.ticketTapeSeries = (TextView) findViewById(R.id.ticket_tape_series);
        viewHolder.ticketTapeNumber = (TextView) findViewById(R.id.ticket_tape_number);
        viewHolder.openShift = (Button) findViewById(R.id.openShift);
        viewHolder.beginWork = (Button) findViewById(R.id.beginWork);

        globals = (Globals) getApplication();
        ticketTapeEventStart = getLocalDaoSession().getTicketTapeEventDao().getInstalledTicketTape();


        if (ShiftManager.getInstance().isShiftClosed()) {
            viewHolder.openShift.setVisibility(View.VISIBLE);
            viewHolder.beginWork.setVisibility(View.GONE);
        } else {
            viewHolder.openShift.setVisibility(View.GONE);
            viewHolder.beginWork.setVisibility(View.VISIBLE);
        }

        setSeriesAndNumber(ticketTapeEventStart.getSeries(), ticketTapeEventStart.getNumber());

        simpleLseView = (SimpleLseView) findViewById(R.id.simpleLseView);

        progressDialog = new FeedbackProgressDialog(this);
        progressDialog.setCancelable(false);
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.openShift:
                openShift();
                break;

            case R.id.beginWork:
                beginWork();
                break;

            case R.id.print_tape_end:
                printTapeEnd();
                break;

            default:
                break;
        }
    }

    private void openShift() {
        Navigator.navigateToWelcomeActivity(this, true);
    }

    private void beginWork() {
        Navigator.navigateToMenuActivity(this);
    }

    private void printTapeEnd() {

        Logger.info(TAG, "print 1...");
        final FeedbackProgressDialog progressDialog = new FeedbackProgressDialog(this);
        progressDialog.setMessage(getString(R.string.printer_PrintingNow));
        progressDialog.setCancelable(false);
        progressDialog.show();
        Logger.info(TAG, "print 2...");

        Di.INSTANCE.printerManager().getOperationFactory().getGetOdometerValue()
                .call()
                .flatMap(result -> {
                    Globals.getInstance().getPaperUsageCounter().setCurrentOdometerValueBeforePrinting(result.getOdometerValue());
                    PaperUsage paperUsage = Globals.getInstance().getPaperUsageCounter().getPaperUsage(PaperUsage.ID_TAPE);
                    paperConsumption = paperUsage.getPaperLength();
                    paperCounterRestarted = paperUsage.isRestarted();
                    return new ReportTicketTapeEnd()
                            .setTicketTapeId(ticketTapeEventStart.getTicketTapeId())
                            .setPaperConsumption(paperConsumption)
                            .buildAndPrintObservable(Di.INSTANCE.printerManager().getPrinter());
                })
                .flatMap(result -> Observable.create((Subscriber<? super PrintTapeEndReportOperation.Result> subscriber) -> {
                    Dagger.appComponent().ticketTapeEventCreator()
                            .setTicketTapeId(ticketTapeEventStart.getTicketTapeId())
                            .setSeries(ticketTapeEventStart.getSeries())
                            .setNumber(ticketTapeEventStart.getNumber())
                            .setExpectedFirstDocNumber(ticketTapeEventStart.getExpectedFirstDocNumber())
                            .setPaperConsumption(paperConsumption)
                            .setPaperCounterRestarted(paperCounterRestarted)
                            .setStartTime(ticketTapeEventStart.getStartTime())
                            .setEndTime(result.getOperationTime())
                            .create();
                    subscriber.onNext(null);
                    subscriber.onCompleted();
                }))
                .subscribeOn(SchedulersCPPK.printer())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<PrintTapeEndReportOperation.Result>() {
                    @Override
                    public void onCompleted() {
                        progressDialog.dismiss();
                        Navigator.navigateToAccountingTicketTapeStartActivity(AccountingTicketTapeEndActivity.this, false);
                        finish();
                    }

                    @Override
                    public void onError(Throwable e) {
                        Logger.error(TAG, e);
                        progressDialog.dismiss();

                        SimpleLseView.State.Builder stateBuilder = new SimpleLseView.State.Builder();
                        stateBuilder.setMode(SimpleLseView.State.MODE_ERROR);

                        if (e instanceof PrinterIsNotConnectedException) {
                            stateBuilder.setTextMessage(R.string.printer_not_found_msg);
                            stateBuilder.setButton1(R.string.printer_repeat_connection, v -> printTapeEnd());
                        } else if (e instanceof TicketTapeChecker.TicketTapeIsNotSetException) {
                            stateBuilder.setTextMessage(R.string.error_msg_ticket_tape_is_not_set);
                            stateBuilder.setButton1(R.string.ticket_tape_is_not_set_view_set_ticket_tape_btn,
                                    v -> Navigator.navigateToAccountingTicketTapeStartActivity(AccountingTicketTapeEndActivity.this, null, REQUEST_CODE_SET_TICKET_TAPE, true));
                        } else if (e instanceof IncorrectEKLZNumberException) {
                            stateBuilder.setTextMessage(R.string.printer_eklz_activation_required_msg);
                            stateBuilder.setButton1(R.string.printer_eklz_activation_required_activate,
                                    v -> Navigator.navigateToSettingsPrinterActivity(AccountingTicketTapeEndActivity.this, null, REQUEST_CODE_ACTIVATE_EKLZ));
                        } else {
                            stateBuilder.setTextMessage(R.string.unknown_error);
                            stateBuilder.setButton1(R.string.sev_btn_repeat, v -> printTapeEnd());
                        }
                        stateBuilder.setButton2(R.string.sev_btn_cancel, v -> simpleLseView.hide());

                        simpleLseView.setState(stateBuilder.build());
                        simpleLseView.show();
                    }

                    @Override
                    public void onNext(PrintTapeEndReportOperation.Result result) {
                        /*NOP*/
                    }
                });
    }

    private void setSeriesAndNumber(String series, int number) {
        viewHolder.ticketTapeSeries.setText(series);
        viewHolder.ticketTapeNumber.setText(String.valueOf(number));
    }

    class ViewHolder {
        TextView ticketTapeSeries;
        TextView ticketTapeNumber;
        Button openShift;
        Button beginWork;
    }

}
