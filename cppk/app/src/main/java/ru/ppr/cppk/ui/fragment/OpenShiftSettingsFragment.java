package ru.ppr.cppk.ui.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import javax.inject.Inject;
import javax.inject.Provider;

import ru.ppr.core.ui.widget.SimpleLseView;
import ru.ppr.cppk.FragmentParent;
import ru.ppr.cppk.R;
import ru.ppr.cppk.di.Dagger;
import ru.ppr.cppk.helpers.SchedulersCPPK;
import ru.ppr.cppk.logic.DocumentTestPd;
import ru.ppr.cppk.logic.ShiftManager;
import ru.ppr.cppk.logic.TicketTapeChecker;
import ru.ppr.cppk.logic.fiscalDocStateSync.FiscalDocStateSynchronizer;
import ru.ppr.cppk.logic.interactor.PrintTestCheckInteractor;
import ru.ppr.cppk.managers.PrinterManager;
import ru.ppr.cppk.printer.exception.IncorrectEKLZNumberException;
import ru.ppr.cppk.printer.rx.operation.shiftOrMonthSheet.PrintShiftOrMonthSheetOperation;
import ru.ppr.cppk.reports.ReportShiftOrMonthlySheet;
import ru.ppr.cppk.settings.AdditionalSettingsFragments.SellAndControlFragment;
import ru.ppr.cppk.systembar.FeedbackProgressDialog;
import ru.ppr.cppk.ui.activity.base.Navigator;
import ru.ppr.cppk.ui.dialog.SimpleDialog;
import ru.ppr.ikkm.exception.PrinterIsNotConnectedException;
import ru.ppr.logger.Logger;
import ru.ppr.nsi.repository.FineRepository;
import ru.ppr.security.entity.PermissionDvc;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;

public class OpenShiftSettingsFragment extends FragmentParent implements OnClickListener {

    private static final String TAG = Logger.makeLogTag(OpenShiftSettingsFragment.class);


    private static final int OPERATION_PRINT_TEST_PD = 1;
    private static final int OPERATION_PRINT_TEST_SHIFT_SHEET = 2;

    private static final int REQUEST_CODE_SET_TICKET_TAPE = 101;
    private static final int REQUEST_CODE_ACTIVATE_EKLZ = 102;

    //region Di
    @Inject
    PrinterManager printerManager;
    @Inject
    TicketTapeChecker ticketTapeChecker;
    @Inject
    FineRepository fineRepository;
    @Inject
    FiscalDocStateSynchronizer fiscalDocStateSynchronizer;
    @Inject
    Provider<PrintTestCheckInteractor> printTestCheckInteractorProvider;
    //endregion

    private FeedbackProgressDialog progressDialog;
    private int currentOperation;
    private SimpleLseView simpleLseView;

    private OnFragmentInteractionListener onFragmentInteractionListener;

    private boolean printTestPdInWork;
    private Subscription printTestShiftSheetSubscription;
    private boolean isNavigationStarted;

    private Button printTestShiftSheetBtn;
    private Button printTestPdBtn;

    public static OpenShiftSettingsFragment newInstance() {
        return new OpenShiftSettingsFragment();
    }

    public interface OnFragmentInteractionListener {

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof OnFragmentInteractionListener) {
            onFragmentInteractionListener = (OnFragmentInteractionListener) activity;
        }
    }

    @Override
    public void onDetach() {
        onFragmentInteractionListener = null;
        super.onDetach();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Dagger.appComponent().inject(this);

        progressDialog = new FeedbackProgressDialog(getActivity());
        progressDialog.setCancelable(false);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.open_shift_settings, container, false);

        Button beginWork = (Button) view.findViewById(R.id.beginWork);
        beginWork.setOnClickListener(this);

        printTestPdBtn = (Button) view.findViewById(R.id.printTestPdBtn);
        printTestPdBtn.setOnClickListener(this);

        printTestShiftSheetBtn = (Button) view.findViewById(R.id.printTestShiftSheetBtn);
        printTestShiftSheetBtn.setOnClickListener(this);

        configAccess();

        Fragment sellAndControlFragment = new SellAndControlFragment();
        Bundle bundle = new Bundle();
        bundle.putBoolean(SellAndControlFragment.FROM_OPEN_SHIFT_SETTINGS, true);
        sellAndControlFragment.setArguments(bundle);
        getFragmentManager().beginTransaction().add(R.id.trainCategoryFragment, sellAndControlFragment).commit();

        simpleLseView = (SimpleLseView) view.findViewById(R.id.simpleLseView);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        isNavigationStarted = false;
    }

    private void configAccess() {
        if (!getSecurityDaoSession().getRolePermissionDvcDao().isPermissionEnabled(di().getUserSessionInfo().getCurrentUser().getRole(), PermissionDvc.TestShiftShit)) {
            disableBtn(printTestShiftSheetBtn);
        }
        if (!getSecurityDaoSession().getRolePermissionDvcDao().isPermissionEnabled(di().getUserSessionInfo().getCurrentUser().getRole(), PermissionDvc.TestPd)) {
            disableBtn(printTestPdBtn);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.printTestPdBtn:
                currentOperation = OPERATION_PRINT_TEST_PD;
                startOperation();
                break;

            case R.id.printTestShiftSheetBtn:
                currentOperation = OPERATION_PRINT_TEST_SHIFT_SHEET;
                startOperation();
                break;

            case R.id.beginWork:
                beginWork();
                break;

            default:
                break;
        }

    }

    private void startOperation() {
        simpleLseView.hide();
        switch (currentOperation) {
            case OPERATION_PRINT_TEST_PD: {
                printTestPD();
                break;
            }
            case OPERATION_PRINT_TEST_SHIFT_SHEET: {
                printTestShiftSheet();
                break;
            }
        }
    }

    private void printTestPD() {

        if (isBtnClickDenied()) {
            return;
        }

        progressDialog.setMessage(getString(R.string.shift_open_settings_printing_test_pd));
        progressDialog.show();

        printTestPdInWork = true;
        ticketTapeChecker.checkOrThrow()
                .andThen(fiscalDocStateSynchronizer.rxSyncCheckState())
                .flatMapCompletable(aBoolean -> printTestCheckInteractorProvider.get().print())
                .subscribeOn(SchedulersCPPK.printer())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<DocumentTestPd>() {
                               @Override
                               public void onCompleted() {
                                   progressDialog.dismiss();
                                   printTestPdInWork = false;
                               }

                               @Override
                               public void onError(Throwable throwable) {
                                   onOperationFailed(throwable);
                                   printTestPdInWork = false;
                               }

                               @Override
                               public void onNext(DocumentTestPd documentTestPd) {
                                   // NOP
                               }
                           }

                );
    }

    private void disableBtn(Button btn) {
        btn.setEnabled(false);
        btn.setBackgroundColor(Color.parseColor("#D1D1D1"));
        btn.setTextColor(Color.parseColor("#B1B1B1"));
    }

    private void printTestShiftSheet() {

        if (isBtnClickDenied()) {
            return;
        }

        progressDialog.setMessage(getString(R.string.printer_PrintingNow));
        progressDialog.show();

        printTestShiftSheetSubscription = ticketTapeChecker.checkOrThrow()
                .andThen(new ReportShiftOrMonthlySheet(fineRepository)
                        .setBuildForLastShift()
                        .setSheetTypeTestAtShiftStart()
                        .buildAndPrintObservable(printerManager.getPrinter())
                )
                .subscribeOn(SchedulersCPPK.printer())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<PrintShiftOrMonthSheetOperation.Result>() {
                    @Override
                    public void onCompleted() {
                        progressDialog.dismiss();
                        disableBtn(printTestShiftSheetBtn);
                        printTestShiftSheetSubscription = null;
                    }

                    @Override
                    public void onError(Throwable e) {
                        onOperationFailed(e);
                        printTestShiftSheetSubscription = null;
                    }

                    @Override
                    public void onNext(PrintShiftOrMonthSheetOperation.Result result) {

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
        } else if (e instanceof TicketTapeChecker.TicketTapeIsNotSetException) {
            stateBuilder.setTextMessage(R.string.error_msg_ticket_tape_is_not_set);
            stateBuilder.setButton1(R.string.ticket_tape_is_not_set_view_set_ticket_tape_btn, v -> Navigator.navigateToAccountingTicketTapeStartActivity(getActivity(), this, REQUEST_CODE_SET_TICKET_TAPE, true));
        } else if (e instanceof IncorrectEKLZNumberException) {
            stateBuilder.setTextMessage(R.string.printer_eklz_activation_required_msg);
            stateBuilder.setButton1(R.string.printer_eklz_activation_required_activate, v -> Navigator.navigateToSettingsPrinterActivity(getActivity(), this, REQUEST_CODE_ACTIVATE_EKLZ));
        } else {
            stateBuilder.setTextMessage(R.string.open_shift_settings_test_pd_fail_msg);
            stateBuilder.setButton1(R.string.sev_btn_repeat, v -> startOperation());
        }
        stateBuilder.setButton2(R.string.sev_btn_cancel, v -> simpleLseView.hide());

        simpleLseView.setState(stateBuilder.build());
        simpleLseView.show();
    }

    private void beginWork() {

        if (isBtnClickDenied()) {
            return;
        }

        if (ShiftManager.getInstance().isShiftOpenedWithTestPd()) {
            Logger.info(TAG, "начинаем работу");
            isNavigationStarted = true;
            Navigator.navigateToMenuActivity(getActivity());
        } else {
            SimpleDialog simpleDialog = SimpleDialog.newInstance(null,
                    getString(R.string.open_shift_settings_test_pd_not_printed_msg),
                    getString(R.string.open_shift_settings_test_pd_not_printed_ok),
                    getString(R.string.open_shift_settings_test_pd_not_printed_cancel),
                    LinearLayout.HORIZONTAL,
                    0);
            simpleDialog.show(getFragmentManager(), SimpleDialog.FRAGMENT_TAG);
            simpleDialog.setDialogPositiveBtnClickListener((dialog, dialogId) -> {
                currentOperation = OPERATION_PRINT_TEST_PD;
                startOperation();
            });
        }
    }

    private boolean isBtnClickDenied() {
        return isNavigationStarted || printTestPdInWork || printTestShiftSheetSubscription != null;
    }
}
