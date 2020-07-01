package ru.ppr.cppk.settings;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.TimePicker;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import ru.ppr.core.ui.widget.SimpleLseView;
import ru.ppr.cppk.FragmentParent;
import ru.ppr.cppk.Globals;
import ru.ppr.cppk.R;
import ru.ppr.cppk.di.Dagger;
import ru.ppr.cppk.di.Di;
import ru.ppr.cppk.dialogs.DateTimePickerDialog;
import ru.ppr.cppk.entity.event.base34.TerminalDay;
import ru.ppr.cppk.exceptions.PrettyException;
import ru.ppr.cppk.helpers.SchedulersCPPK;
import ru.ppr.cppk.localdb.model.ShiftEvent;
import ru.ppr.cppk.logic.DocumentTestPd;
import ru.ppr.cppk.logic.ShiftManager;
import ru.ppr.cppk.logic.TicketTapeChecker;
import ru.ppr.cppk.managers.PosManager;
import ru.ppr.cppk.model.PosOperationResult;
import ru.ppr.cppk.printer.exception.IncorrectEKLZNumberException;
import ru.ppr.cppk.printer.rx.operation.auditTrail.PrintAuditTrailOperation;
import ru.ppr.cppk.printer.rx.operation.bankSlip.PrinterPrintBankSlipOperation;
import ru.ppr.cppk.printer.rx.operation.discountShiftSheet.PrintDiscountShiftSheetOperation;
import ru.ppr.cppk.printer.rx.operation.printerPrintSalesForEttLog.PrintSalesForEttLogOperation;
import ru.ppr.cppk.printer.rx.operation.shiftOrMonthSheet.PrintShiftOrMonthSheetOperation;
import ru.ppr.cppk.reports.ReportAuditTrail;
import ru.ppr.cppk.reports.ReportDiscountShiftSheet;
import ru.ppr.cppk.reports.ReportSalesForEttLog;
import ru.ppr.cppk.reports.ReportShiftOrMonthlySheet;
import ru.ppr.cppk.service.ServiceTerminalMonitor;
import ru.ppr.cppk.systembar.FeedbackProgressDialog;
import ru.ppr.cppk.ui.activity.base.Navigator;
import ru.ppr.cppk.ui.adapter.simple.ShiftEventsAdapter;
import ru.ppr.cppk.ui.dialog.SimpleDialog;
import ru.ppr.cppk.ui.widget.FilterEditText;
import ru.ppr.cppk.ui.widget.textWatchers.NumberTextWatcher;
import ru.ppr.cppk.utils.SlipConverter;
import ru.ppr.ikkm.exception.PrinterIsNotConnectedException;
import ru.ppr.ipos.model.TransactionResult;
import ru.ppr.logger.Logger;
import ru.ppr.nsi.repository.FineRepository;
import ru.ppr.security.entity.PermissionDvc;
import ru.ppr.security.entity.RoleDvc;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;

/**
 * Экран формирования отчетности (вкладка "Смена").
 */
public class ReportsShiftFragment extends FragmentParent {

    private static final String TAG = Logger.makeLogTag(ReportsShiftFragment.class);

    private final FineRepository fineRepository;

    private static final int OPERATION_PRINT_TEST_PD = 1;
    private static final int OPERATION_PRINT_TEST_SHIFT_SHEET = 2;
    private static final int OPERATION_PRINT_SHIFT_SHEET = 3;
    private static final int OPERATION_PRINT_DISCOUNT_SHIFT_SHEET = 4;
    private static final int OPERATION_PRINT_TERMINAL_OPERATIONS = 5;
    private static final int OPERATION_PRINT_TERMINAL_DAY_CLOSE = 6;
    private static final int OPERATION_PRINT_AUDIT_TRAIL = 7;
    private static final int OPERATION_PRINT_SALES_FOR_ETT_LOG = 8;

    private static final int REQUEST_CODE_SET_TICKET_TAPE = 101;
    private static final int REQUEST_CODE_ACTIVATE_EKLZ = 102;

    /**
     * Время задержки между тапами
     */
    private static final int TAP_LOCK_DELAY = 400;

    /**
     * Флаг заблокированности возможности тапа
     */
    private boolean isTapLocked = false;

    private ViewHolder viewHolder;
    private ShiftEvent selectedShift;
    private TerminalDay selectedTerminalDay;
    private boolean isShiftOpened;
    private ShiftEvent currentShift;
    private int currentOperation;

    private FeedbackProgressDialog progressDialog;

    Date auditTrailPeriodStart = null;
    Date auditTrailPeriodEnd = null;

    private Handler handler;

    private OnFragmentInteractionListener onFragmentInteractionListener;

    public ReportsShiftFragment() {
        this.fineRepository = Dagger.appComponent().fineRepository();
    }

    public interface OnFragmentInteractionListener {
        void hideErrorView();

        void showErrorView(SimpleLseView.State errorViewState);
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

        handler = new Handler();

        currentShift = ShiftManager.getInstance().getCurrentShiftEvent();
        isShiftOpened = ShiftManager.getInstance().isShiftOpened();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragmnet_reports_shift, container, false);

        viewHolder = new ViewHolder();
        viewHolder.shiftNum = (FilterEditText) view.findViewById(R.id.shiftNum);
        viewHolder.shiftDate = (TextView) view.findViewById(R.id.shiftDate);
        viewHolder.testPD = view.findViewById(R.id.testPD);
        viewHolder.testShiftSheet = view.findViewById(R.id.testShiftSheet);
        viewHolder.shiftSheet = view.findViewById(R.id.shiftSheet);
        viewHolder.auditTrail = (TextView) view.findViewById(R.id.auditTrail);
        viewHolder.auditTrailDetailLayout = view.findViewById(R.id.auditTrailDetailLayout);
        viewHolder.discountShiftSheet = view.findViewById(R.id.discountShiftSheet);
        viewHolder.reportsTerminalOperations = view.findViewById(R.id.reportsTerminalOperations);
        viewHolder.reportsTerminalDayClose = view.findViewById(R.id.reportsTerminalDayClose);
        viewHolder.salesForEttLog = view.findViewById(R.id.salesForEttLog);
        viewHolder.auditTrailPeriodStart = (TextView) view.findViewById(R.id.auditTrailPeriodStart);
        viewHolder.auditTrailPeriodEnd = (TextView) view.findViewById(R.id.auditTrailPeriodEnd);
        viewHolder.auditTrailPeriodStartPrefix = (TextView) view.findViewById(R.id.auditTrailPeriodStartPrefix);
        viewHolder.auditTrailPeriodEndPrefix = (TextView) view.findViewById(R.id.auditTrailPeriodEndPrefix);
        viewHolder.printAuditTrail = (Button) view.findViewById(R.id.printAuditTrail);
        viewHolder.auditTrailPeriodType = (RadioGroup) view.findViewById(R.id.auditTrailPeriodType);

        viewHolder.shiftNum.addTextChangedListener(new NumberTextWatcher());

        bindListeners();

        progressDialog = new FeedbackProgressDialog(getActivity());
        progressDialog.setMessage(getString(R.string.printing_reports_progress_printing));
        progressDialog.setCancelable(false);

        setSelectedShift(currentShift, false);

        return view;
    }

    /**
     * Привязка слушаталей событий к UI-элементам.
     */
    private void bindListeners() {

        viewHolder.auditTrailPeriodType.setOnCheckedChangeListener((group, checkedId) -> {

            if (checkedId == R.id.auditTrailForShift) {
                viewHolder.auditTrailPeriodStart.setEnabled(false);
                viewHolder.auditTrailPeriodEnd.setEnabled(false);
                viewHolder.auditTrailPeriodStartPrefix.setEnabled(false);
                viewHolder.auditTrailPeriodEndPrefix.setEnabled(false);
            } else {
                viewHolder.auditTrailPeriodStart.setEnabled(true);
                viewHolder.auditTrailPeriodEnd.setEnabled(true);
                viewHolder.auditTrailPeriodStartPrefix.setEnabled(true);
                viewHolder.auditTrailPeriodEndPrefix.setEnabled(true);
            }

        });

        viewHolder.shiftDate.setOnClickListener(v -> {
            if (!isTapLocked) {
                isTapLocked = true;
                DatePickerFragment datePickerFragment = new DatePickerFragment();
                datePickerFragment.show(getFragmentManager(), "datePickerFragment");
                handler.postDelayed(() -> isTapLocked = false, TAP_LOCK_DELAY);
            }
        });

        viewHolder.auditTrailPeriodStart.setOnClickListener(auditTrailFromOnClickListener);
        viewHolder.auditTrailPeriodStartPrefix.setOnClickListener(auditTrailFromOnClickListener);
        viewHolder.auditTrailPeriodEnd.setOnClickListener(auditTrailToOnClickListener);
        viewHolder.auditTrailPeriodEndPrefix.setOnClickListener(auditTrailToOnClickListener);

        viewHolder.shiftNum.setOnBackListener(() -> {
            setSelectedShift(selectedShift, false);
            return false;
        });


        viewHolder.shiftNum.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                int shiftNumber = -1;
                String valueString = viewHolder.shiftNum.getText().toString().trim().replaceAll("[^\\d]", "");
                if (!valueString.isEmpty()) {
                    shiftNumber = Integer.valueOf(valueString);
                }
                List<ShiftEvent> workingShiftsWithNumber = getLocalDaoSession().getShiftEventDao().getAllShiftsByNumber(shiftNumber, ShiftEvent.ShiftProgressStatus.FINISHED_STATUSES);
                if (workingShiftsWithNumber.isEmpty()) {
                    setSelectedShift(selectedShift, true);
                } else if (workingShiftsWithNumber.size() == 1) {
                    setSelectedShift(workingShiftsWithNumber.get(0), false);
                } else {
                    ShiftListFragment shiftListFragment = new ShiftListFragment();
                    shiftListFragment.setShifts(workingShiftsWithNumber);
                    shiftListFragment.show(getFragmentManager(), "shiftListFragment");
                }
            }
            return false;
        });

        // Кнопки печати отчетов
        viewHolder.testPD.setOnClickListener(printReportBtnClickListener);
        viewHolder.testShiftSheet.setOnClickListener(printReportBtnClickListener);
        viewHolder.shiftSheet.setOnClickListener(printReportBtnClickListener);
        viewHolder.discountShiftSheet.setOnClickListener(printReportBtnClickListener);
        viewHolder.reportsTerminalOperations.setOnClickListener(printReportBtnClickListener);
        viewHolder.reportsTerminalDayClose.setOnClickListener(printReportBtnClickListener);
        viewHolder.printAuditTrail.setOnClickListener(printReportBtnClickListener);
        viewHolder.salesForEttLog.setOnClickListener(printReportBtnClickListener);
    }

    /**
     * Слушатель тапа на выбор даты начала контрольного журнала
     */
    private View.OnClickListener auditTrailFromOnClickListener = v -> {
        if (!isTapLocked) {
            isTapLocked = true;
            DateTimePickerFragment dateTimePickerFragment = new DateTimePickerFragment();
            dateTimePickerFragment.modeFromDate = true;
            dateTimePickerFragment.show(ReportsShiftFragment.this.getFragmentManager(), "dateTimePickerFragment");
            handler.postDelayed(() -> isTapLocked = false, TAP_LOCK_DELAY);
        }
    };

    /**
     * Слушатель тапа на выбор даты конца контрольного журнала
     */
    private View.OnClickListener auditTrailToOnClickListener = v -> {
        if (!isTapLocked) {
            isTapLocked = true;
            DateTimePickerFragment dateTimePickerFragment = new DateTimePickerFragment();
            dateTimePickerFragment.modeFromDate = false;
            dateTimePickerFragment.show(getFragmentManager(), "dateTimePickerFragment");
            handler.postDelayed(() -> isTapLocked = false, TAP_LOCK_DELAY);
        }
    };


    private View.OnClickListener printReportBtnClickListener = v -> {
        if (!isTapLocked) {
            isTapLocked = true;
            switch (v.getId()) {
                case R.id.testPD: {
                    currentOperation = OPERATION_PRINT_TEST_PD;
                    break;
                }
                case R.id.testShiftSheet: {
                    currentOperation = OPERATION_PRINT_TEST_SHIFT_SHEET;
                    break;
                }
                case R.id.shiftSheet: {
                    currentOperation = OPERATION_PRINT_SHIFT_SHEET;
                    break;
                }
                case R.id.discountShiftSheet: {
                    currentOperation = OPERATION_PRINT_DISCOUNT_SHIFT_SHEET;
                    break;
                }
                case R.id.reportsTerminalOperations: {
                    currentOperation = OPERATION_PRINT_TERMINAL_OPERATIONS;
                    break;
                }
                case R.id.reportsTerminalDayClose: {
                    currentOperation = OPERATION_PRINT_TERMINAL_DAY_CLOSE;
                    break;
                }
                case R.id.printAuditTrail: {
                    currentOperation = OPERATION_PRINT_AUDIT_TRAIL;
                    break;
                }
                case R.id.salesForEttLog: {
                    currentOperation = OPERATION_PRINT_SALES_FOR_ETT_LOG;
                    break;
                }
            }
            startOperation();
            handler.postDelayed(() -> isTapLocked = false, TAP_LOCK_DELAY);
        }
    };

    /**
     * Стартует операцию печати отчета.
     */
    private void startOperation() {
        if (onFragmentInteractionListener != null) {
            onFragmentInteractionListener.hideErrorView();
        }
        switch (currentOperation) {
            case OPERATION_PRINT_TEST_PD: {
                printTestPD();
                break;
            }
            case OPERATION_PRINT_TEST_SHIFT_SHEET: {
                printTestShiftSheet();
                break;
            }
            case OPERATION_PRINT_SHIFT_SHEET: {
                printShiftSheet();
                break;
            }
            case OPERATION_PRINT_DISCOUNT_SHIFT_SHEET: {
                printDiscountShiftSheet();
                break;
            }
            case OPERATION_PRINT_TERMINAL_OPERATIONS: {
                printReportTerminalOperations();
                break;
            }
            case OPERATION_PRINT_TERMINAL_DAY_CLOSE: {
                printReportsTerminalDayClose();
                break;
            }
            case OPERATION_PRINT_AUDIT_TRAIL: {
                printAuditTrail();
                break;
            }
            case OPERATION_PRINT_SALES_FOR_ETT_LOG: {
                printSalesForEttLog();
                break;
            }
        }
    }

    /**
     * Устаналивает смену, по которой следует формировать отчетность.
     *
     * @param workingShift Событие смены.
     * @param showErrorMsg {@code true}, если нужно показать сообщение об ошибке при отсутствии смены,
     *                     [@code false} иначе.
     */
    private void setSelectedShift(ShiftEvent workingShift, boolean showErrorMsg) {
        if (workingShift == null) {
            if (showErrorMsg) {
                SimpleDialog simpleDialog = SimpleDialog.newInstance(
                        null,
                        getString(R.string.reports_no_shifts_with_number_msg),
                        getString(R.string.dialog_close),
                        null,
                        LinearLayout.VERTICAL,
                        -1
                );
                simpleDialog.show(getFragmentManager(), SimpleDialog.FRAGMENT_TAG);
            }
        } else {
            ShiftEvent lastWorkingShift = getLocalDaoSession().getShiftEventDao().getLastCashRegisterWorkingShiftByShiftId(workingShift.getShiftId(), ShiftEvent.ShiftProgressStatus.FINISHED_STATUSES);
            selectedShift = lastWorkingShift;
        }
        ////////////////////////////////////////////
        if (selectedShift == null) {
            viewHolder.shiftDate.setText("");
            viewHolder.shiftNum.setText("");
            viewHolder.auditTrailPeriodStart.setText("");
            viewHolder.auditTrailPeriodEnd.setText("");
        } else {
            String format = "dd.MM.yyyy";
            SimpleDateFormat dateFormat = new SimpleDateFormat(format, Locale.getDefault());
            String date = dateFormat.format(selectedShift.getStartTime());
            viewHolder.shiftDate.setText(date);
            viewHolder.shiftNum.setText(String.valueOf(selectedShift.getShiftNumber()));
            ////////////////////////////////////////////
            auditTrailPeriodStart = selectedShift.getStartTime();
            setPeriodValue(viewHolder.auditTrailPeriodStart, auditTrailPeriodStart);
            if (selectedShift.getCloseTime() != null) {
                auditTrailPeriodEnd = selectedShift.getCloseTime();
            } else {
                auditTrailPeriodEnd = new Date();
            }
            setPeriodValue(viewHolder.auditTrailPeriodEnd, auditTrailPeriodEnd);
            ////////////////////////////////////////////
            selectedTerminalDay = getLocalDaoSession().getTerminalDayDao().getTerminalDayForShiftId(selectedShift.getId());
            ////////////////////////////////////////////
        }

        updateViewState();
    }

    /**
     * Настраивает доступность кнопок в UI.
     */
    private void updateViewState() {
        RoleDvc role = di().getUserSessionInfo().getCurrentUser().getRole();
        boolean currentShiftIsSelected = selectedShift != null && currentShift != null && selectedShift.getShiftId().equals(currentShift.getShiftId()) && isShiftOpened;
        viewHolder.testPD.setEnabled(currentShiftIsSelected && getSecurityDaoSession().getRolePermissionDvcDao().isPermissionEnabled(role, PermissionDvc.TestPd));
        viewHolder.testShiftSheet.setEnabled(currentShiftIsSelected && getSecurityDaoSession().getRolePermissionDvcDao().isPermissionEnabled(role, PermissionDvc.TestShiftShit));
        viewHolder.reportsTerminalOperations.setEnabled(currentShiftIsSelected && getSecurityDaoSession().getRolePermissionDvcDao().isPermissionEnabled(role, PermissionDvc.ReportPOSOperations));
        viewHolder.reportsTerminalDayClose.setEnabled(selectedTerminalDay != null && getSecurityDaoSession().getRolePermissionDvcDao().isPermissionEnabled(role, PermissionDvc.ReportPOSCloseDay));
        viewHolder.auditTrail.setCompoundDrawablesWithIntrinsicBounds(0, 0, getSecurityDaoSession().getRolePermissionDvcDao().isPermissionEnabled(role, PermissionDvc.PrintControlJournal) ? R.drawable.arrow_bottom_black : R.drawable.arrow_gray_white, 0);
        viewHolder.auditTrail.setBackgroundColor(getResources().getColor(getSecurityDaoSession().getRolePermissionDvcDao().isPermissionEnabled(role, PermissionDvc.PrintControlJournal) ? R.color.reports_background_color : R.color.gray_inactive));
        viewHolder.auditTrailDetailLayout.setVisibility(getSecurityDaoSession().getRolePermissionDvcDao().isPermissionEnabled(role, PermissionDvc.PrintControlJournal) ? View.VISIBLE : View.GONE);
        viewHolder.printAuditTrail.setEnabled(selectedShift != null && getSecurityDaoSession().getRolePermissionDvcDao().isPermissionEnabled(role, PermissionDvc.PrintControlJournal));
        viewHolder.salesForEttLog.setEnabled(selectedShift != null && getSecurityDaoSession().getRolePermissionDvcDao().isPermissionEnabled(role, PermissionDvc.JournalETT));
        viewHolder.shiftSheet.setEnabled(selectedShift != null && !currentShiftIsSelected && getSecurityDaoSession().getRolePermissionDvcDao().isPermissionEnabled(role, PermissionDvc.ShiftSheet));
        viewHolder.discountShiftSheet.setEnabled(selectedShift != null && getSecurityDaoSession().getRolePermissionDvcDao().isPermissionEnabled(role, PermissionDvc.DiscountedShiftShit));
    }

    /**
     * Диалоговое окно с выбором даты смены.
     */
    @SuppressLint("ValidFragment")
    public class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current date as the default date in the picker
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            // Create a new instance of DatePickerDialog and return it
            DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(), this, year, month, day) {
                @Override
                protected void onStop() {
                    // Родительский метод вызывает onDateSet при нажатии Back
                }
            };
            datePickerDialog.setTitle(R.string.reports_select_date_title);
            return datePickerDialog;
        }

        public void onDateSet(DatePicker datePicker, int year, int month, int day) {

            Calendar calendar = Calendar.getInstance();
            calendar.set(year, month, day);

            List<ShiftEvent> shifts = getLocalDaoSession().getShiftEventDao().getShiftsAtDate(calendar.getTime());

            if (shifts.isEmpty()) {
                SimpleDialog simpleDialog = SimpleDialog.newInstance(
                        null,
                        getString(R.string.reports_no_shifts_at_date_msg),
                        getString(R.string.dialog_close),
                        null,
                        LinearLayout.VERTICAL,
                        -1
                );
                simpleDialog.show(getFragmentManager(), SimpleDialog.FRAGMENT_TAG);
            } else {
                ShiftListFragment shiftListFragment = new ShiftListFragment();
                shiftListFragment.setShifts(shifts);
                shiftListFragment.show(getFragmentManager(), "shiftListFragment");
            }
        }
    }

    /**
     * Диалоговое окно с выбором времени от/до для границ печати КЖ.
     */
    @SuppressLint("ValidFragment")
    public class DateTimePickerFragment extends DialogFragment implements DateTimePickerDialog.OnDateTimeSetListener {

        private boolean modeFromDate;

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current date as the default date in the picker

            Date time = new Date();
            if (modeFromDate && auditTrailPeriodStart != null) {
                time = auditTrailPeriodStart;
            }
            if (!modeFromDate && auditTrailPeriodEnd != null) {
                time = auditTrailPeriodEnd;
            }
            final Calendar c = Calendar.getInstance();
            c.setTime(time);

            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);
            int hourOfDay = c.get(Calendar.HOUR_OF_DAY);
            int minute = c.get(Calendar.MINUTE);
            // Create a new instance of DatePickerDialog and return it
            DateTimePickerDialog datePickerDialog = new DateTimePickerDialog(getActivity(), this, year, month, day, hourOfDay, minute, true);
            datePickerDialog.setTitle(R.string.reports_select_date_title);
            return datePickerDialog;
        }

        public void onDateTimeSet(DatePicker datePicker, TimePicker timePicker, int year, int month, int day, int hour, int minute) {

            Calendar calendar = Calendar.getInstance();
            calendar.set(year, month, day, hour, minute);

            TextView textView = null;
            if (modeFromDate) {
                textView = viewHolder.auditTrailPeriodStart;
                auditTrailPeriodStart = calendar.getTime();
            } else {
                textView = viewHolder.auditTrailPeriodEnd;
                auditTrailPeriodEnd = calendar.getTime();
            }

            setPeriodValue(textView, calendar.getTime());
        }
    }

    /**
     * Диалоговое окно со списком смен.
     */
    @SuppressLint("ValidFragment")
    public class ShiftListFragment extends DialogFragment implements AdapterView.OnItemClickListener {

        private List<ShiftEvent> shiftList;
        private ShiftEventsAdapter adapter;

        public void setShifts(List<ShiftEvent> shifts) {
            this.shiftList = shifts;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(R.string.reports_shifts_list_title);
            ListView list = new ListView(getActivity());

            adapter = new ShiftEventsAdapter(getActivity());
            adapter.setItems(shiftList);

            list.setAdapter(adapter);
            list.setOnItemClickListener(this);

            builder.setView(list);

            return builder.create();

        }

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            setSelectedShift(adapter.getItem(position), false);
            dismiss();
        }
    }

    /**
     * Печатает пробный ПД.
     */
    private void printTestPD() {
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
                        Globals.getInstance().getToaster().showToast(R.string.printing_reports_msg_success);
                    }

                    @Override
                    public void onError(Throwable e) {
                        onOperationFailed(e);
                    }

                    @Override
                    public void onNext(DocumentTestPd documentTestPd) {

                    }
                });
    }

    /**
     * Печатает пробную сменную ведомость.
     */
    private void printTestShiftSheet() {
        progressDialog.show();
        ///////////////////////////////
        new ReportShiftOrMonthlySheet(fineRepository)
                .setBuildForLastShift()
                .setSheetTypeTestAtShiftMiddle()
                .buildAndPrintObservable(Di.INSTANCE.printerManager().getPrinter())
                .subscribeOn(SchedulersCPPK.printer())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<PrintShiftOrMonthSheetOperation.Result>() {
                    @Override
                    public void onCompleted() {
                        progressDialog.dismiss();
                        Globals.getInstance().getToaster().showToast(R.string.printing_reports_msg_success);
                    }

                    @Override
                    public void onError(Throwable e) {
                        onOperationFailed(e);
                    }

                    @Override
                    public void onNext(PrintShiftOrMonthSheetOperation.Result result) {

                    }
                });
    }

    /**
     * Печатает сменную ведомость.
     */
    private void printShiftSheet() {
        progressDialog.show();
        ///////////////////////////////
        new ReportShiftOrMonthlySheet(fineRepository)
                .setShiftId(selectedShift.getShiftId())
                .setSheetTypeShift()
                .buildAndPrintObservable(Di.INSTANCE.printerManager().getPrinter())
                .subscribeOn(SchedulersCPPK.printer())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<PrintShiftOrMonthSheetOperation.Result>() {
                    @Override
                    public void onCompleted() {
                        progressDialog.dismiss();
                        Globals.getInstance().getToaster().showToast(R.string.printing_reports_msg_success);
                    }

                    @Override
                    public void onError(Throwable e) {
                        onOperationFailed(e);
                    }

                    @Override
                    public void onNext(PrintShiftOrMonthSheetOperation.Result result) {

                    }
                });
    }

    /**
     * Печатает льготную сменную ведомость.
     */
    private void printDiscountShiftSheet() {
        progressDialog.show();
        ///////////////////////////////
        new ReportDiscountShiftSheet()
                .setShiftId(selectedShift.getShiftId())
                .buildAndPrintObservable(Di.INSTANCE.printerManager().getPrinter())
                .subscribeOn(SchedulersCPPK.printer())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<PrintDiscountShiftSheetOperation.Result>() {
                    @Override
                    public void onCompleted() {
                        progressDialog.dismiss();
                        Globals.getInstance().getToaster().showToast(R.string.printing_reports_msg_success);
                    }

                    @Override
                    public void onError(Throwable e) {
                        onOperationFailed(e);
                    }

                    @Override
                    public void onNext(PrintDiscountShiftSheetOperation.Result result) {

                    }
                });
    }

    /**
     * Печатает журнал оформления по ЭТТ.
     */
    private void printSalesForEttLog() {
        progressDialog.show();
        ///////////////////////////////
        new ReportSalesForEttLog()
                .setShiftId(selectedShift.getShiftId())
                .buildAndPrintObservable(Di.INSTANCE.printerManager().getPrinter())
                .subscribeOn(SchedulersCPPK.printer())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<PrintSalesForEttLogOperation.Result>() {
                    @Override
                    public void onCompleted() {
                        progressDialog.dismiss();
                        Globals.getInstance().getToaster().showToast(R.string.printing_reports_msg_success);
                    }

                    @Override
                    public void onError(Throwable e) {
                        onOperationFailed(e);
                    }

                    @Override
                    public void onNext(PrintSalesForEttLogOperation.Result result) {

                    }
                });
    }

    /**
     * Печатает отчёт по операциям POS-терминала.
     */
    private void printReportTerminalOperations() {
        //https://aj.srvdev.ru/browse/CPPKPP-26074
        if (Globals.getInstance().getPosManager().isDayMustBeReopened()) {
            getActivity().runOnUiThread(() -> {
                final SimpleDialog simpleDialog = SimpleDialog.newInstance(
                        null,
                        getString(R.string.reports_terminal_operations_day_not_opened),
                        getString(R.string.btnOk),
                        null,
                        LinearLayout.HORIZONTAL,
                        -1);
                simpleDialog.setCancelable(false);
                simpleDialog.show(getFragmentManager(), SimpleDialog.FRAGMENT_TAG);
            });

            return;
        }

        if (!Globals.getInstance().getPosManager().isReady() || ServiceTerminalMonitor.isBusy()) {
            getActivity().runOnUiThread(() -> Globals.getInstance().getToaster().showToast(R.string.terminal_is_busy));

            return;
        }

        progressDialog.show();

        Dagger.appComponent().ticketTapeChecker().checkOrThrow()
                .andThen(Observable.create(new Observable.OnSubscribe<List<String>>() {
                    @Override
                    public void call(Subscriber<? super List<String>> subscriber) {
                        if (!Globals.getInstance().getPosManager().isDayStarted()) {
                            List<String> receipt = new ArrayList<String>() {{
                                add("В ТЕЧЕНИЕ ДНЯ НЕ БЫЛО");
                                add("ОПЛАТ ПО БАНК . КАРТАМ");
                            }};

                            subscriber.onNext(receipt);
                            subscriber.onCompleted();

                            return;
                        }

                        Globals.getInstance().getPosManager().getTransactionsJournal(new PosManager.AbstractTransactionListener() {

                            @Override
                            public void onConnectionTimeout() {
                                subscriber.onError(new Exception("Connection timeout"));
                            }

                            @Override
                            public void onResult(@NonNull PosOperationResult<TransactionResult> operationResult) {
                                TransactionResult result = operationResult.getTransactionResult();
                                if (result != null) {
                                    List<String> receipt = result.getReceipt();

                                    //https://aj.srvdev.ru/browse/CPPKPP-27155
                                    if (receipt == null || receipt.isEmpty()) {
                                        receipt = new ArrayList<String>() {{
                                            add("В ТЕЧЕНИЕ ДНЯ НЕ БЫЛО");
                                            add("ОПЛАТ ПО БАНК . КАРТАМ");
                                        }};
                                    }

                                    subscriber.onNext(receipt);
                                    subscriber.onCompleted();
                                }
                                subscriber.onError(new Exception("bad receipt"));
                            }
                        });
                    }
                }).observeOn(SchedulersCPPK.printer()))
                .flatMap(receipt -> {
                    PrinterPrintBankSlipOperation.Params params = new PrinterPrintBankSlipOperation.Params();

                    params.tplParams.slipLines = receipt;

                    return Observable.just(params);
                })
                .flatMap(params -> Di.INSTANCE.printerManager().getOperationFactory()
                        .getPrintBankSlipOperation(params)
                        .call())
                .subscribeOn(SchedulersCPPK.printer())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Object>() {
                               @Override
                               public void onCompleted() {
                                   progressDialog.dismiss();
                                   Globals.getInstance().getToaster().showToast(R.string.printing_reports_msg_success);
                               }

                               @Override
                               public void onError(Throwable e) {
                                   onOperationFailed(e);
                               }

                               @Override
                               public void onNext(Object object) {

                               }
                           }
                );
    }

    /**
     * Печатает отчёт о закрытии дня на POS-терминале.
     */
    private void printReportsTerminalDayClose() {

        Dagger.appComponent().ticketTapeChecker().checkOrThrow()
                .andThen(Observable
                        .just(getLocalDaoSession().getTicketTapeEventDao().isTicketTapeSet())
                        .flatMap(isTicketTapeSet -> {
                            if (isTicketTapeSet) {
                                if (selectedTerminalDay.getReport() != null) {
                                    List<String> receipt = SlipConverter.fromImage(selectedTerminalDay.getReport());

                                    if (!receipt.isEmpty()) {
                                        return Observable.just(receipt);
                                    } else {
                                        return Observable.error(new Exception("bad receipt"));
                                    }
                                } else {
                                    return Observable.error(new Exception("bad receipt"));
                                }

                            } else {
                                return Observable.error(new PrettyException(getString(R.string.error_msg_ticket_tape_is_not_set)));
                            }
                        })
                        .flatMap(receipt -> {
                            PrinterPrintBankSlipOperation.Params params = new PrinterPrintBankSlipOperation.Params();

                            params.tplParams.slipLines = receipt;

                            return Observable.just(params);
                        })
                        .flatMap(params -> Di.INSTANCE.printerManager().getOperationFactory()
                                .getPrintBankSlipOperation(params)
                                .call())
                )
                .subscribeOn(SchedulersCPPK.printer())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Object>() {
                               @Override
                               public void onCompleted() {
                                   progressDialog.dismiss();

                                   Globals.getInstance().getToaster().showToast(R.string.printing_reports_msg_success);
                               }

                               @Override
                               public void onError(Throwable e) {
                                   onOperationFailed(e);
                               }

                               @Override
                               public void onNext(Object object) {

                               }
                           }
                );
    }

    /**
     * Печатает КЖ.
     */
    private void printAuditTrail() {
        Date toDate = null;
        Date fromDate = null;
        int checkedId = viewHolder.auditTrailPeriodType.getCheckedRadioButtonId();
        if (checkedId == R.id.auditTrailForPeriod) {

            // Всё непонятное на счет "начала минут" сделано ввиду того, что
            // пользователь вводит время в минутах
            // И если пользователь написал 15:01, а фактически смена открыта
            // в 15:01:30, то происходит преобразование времени
            // в точное по границам интервала (15:01:00 -> 15:01:30)
            auditTrailPeriodStart = startOfMinute(auditTrailPeriodStart);
            auditTrailPeriodEnd = startOfMinute(auditTrailPeriodEnd);

            Date startReal = selectedShift.getStartTime();
            if (isDateInRange(auditTrailPeriodStart)) {
                fromDate = auditTrailPeriodStart;
            } else {
                fromDate = startReal;
            }

            Date endReal = selectedShift.getCloseTime() == null ? new Date() : selectedShift.getCloseTime();
            if (isDateInRange(auditTrailPeriodEnd)) {
                toDate = auditTrailPeriodEnd;
            } else {
                toDate = endReal;
            }
            if (!auditTrailPeriodEnd.after(auditTrailPeriodStart)) {
                Globals.getInstance().getToaster().showToast(R.string.reports_audit_taril_period_invalid);
                return;
            }
            if (!startOfMinute(fromDate).equals(auditTrailPeriodStart) || !startOfMinute(toDate).equals(auditTrailPeriodEnd)) {
                String msg = getActivity().getResources().getString(R.string.reports_audit_taril_period_real);
                msg = msg.replace("{1}", fromDate.toString());
                msg = msg.replace("{2}", toDate.toString());
                Globals.getInstance().getToaster().showToast(msg);
            }
        }
        progressDialog.show();

        final Date fFromDate = fromDate;
        final Date fToDate = toDate;

        new ReportAuditTrail()
                .setShiftId(selectedShift.getShiftId())
                .setPeriod(fFromDate, fToDate)
                .setBuildForPeriod()
                .buildAndPrintObservable(Di.INSTANCE.printerManager().getPrinter())
                .subscribeOn(SchedulersCPPK.printer())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<PrintAuditTrailOperation.Result>() {
                    @Override
                    public void onCompleted() {
                        progressDialog.dismiss();
                        Globals.getInstance().getToaster().showToast(R.string.printing_reports_msg_success);
                    }

                    @Override
                    public void onError(Throwable e) {
                        onOperationFailed(e);
                    }

                    @Override
                    public void onNext(PrintAuditTrailOperation.Result result) {

                    }
                });
    }

    /**
     * Обрабытывает ошибку печати отчета.
     *
     * @param e Ошибка
     */
    void onOperationFailed(Throwable e) {
        Logger.error(TAG, e);
        progressDialog.dismiss();
        if (onFragmentInteractionListener != null) {

            SimpleLseView.State.Builder stateBuilder = new SimpleLseView.State.Builder();
            stateBuilder.setMode(SimpleLseView.State.MODE_ERROR);

            if (e instanceof PrinterIsNotConnectedException) {
                stateBuilder.setTextMessage(getString(R.string.printer_not_found_msg));
                stateBuilder.setButton1(getString(R.string.printer_repeat_connection), v -> startOperation());
            } else if (e instanceof TicketTapeChecker.TicketTapeIsNotSetException) {
                stateBuilder.setTextMessage(getString(R.string.error_msg_ticket_tape_is_not_set));
                stateBuilder.setButton1(getString(R.string.ticket_tape_is_not_set_view_set_ticket_tape_btn), v -> Navigator.navigateToAccountingTicketTapeStartActivity(getActivity(), this, REQUEST_CODE_SET_TICKET_TAPE, true));
            } else if (e instanceof IncorrectEKLZNumberException) {
                stateBuilder.setTextMessage(getString(R.string.printer_eklz_activation_required_msg));
                stateBuilder.setButton1(getString(R.string.printer_eklz_activation_required_activate), v -> Navigator.navigateToSettingsPrinterActivity(getActivity(), this, REQUEST_CODE_ACTIVATE_EKLZ));
            } else if (e instanceof PrettyException) {
                stateBuilder.setTextMessage(e.getMessage());
                stateBuilder.setButton1(getString(R.string.sev_btn_repeat), v -> startOperation());
            } else {
                stateBuilder.setTextMessage(getString(R.string.printing_reports_msg_failed));
                stateBuilder.setButton1(getString(R.string.sev_btn_repeat), v -> startOperation());
            }
            stateBuilder.setButton2(getString(R.string.sev_btn_cancel), v -> onFragmentInteractionListener.hideErrorView());

            onFragmentInteractionListener.showErrorView(stateBuilder.build());
        }
    }

    /**
     * Проверяет, что дата входит в интервал открытости смены.
     *
     * @param date Дата
     * @return {@code true}, если входит, {@code false} иначе.
     */
    private boolean isDateInRange(Date date) {

        if (date.before(startOfMinute(selectedShift.getStartTime()))) {
            return false;
        } else {
            if (selectedShift.getCloseTime() != null) {
                if (date.after(startOfMinute(selectedShift.getCloseTime()))) {
                    return false;
                } else {
                    return true;
                }
            } else {
                if (date.after(startOfMinute(new Date()))) {
                    return false;
                } else {
                    return true;
                }
            }
        }
    }

    /**
     * Возвращает начало минуты от указанной даты.
     */
    private Date startOfMinute(Date date) {
        return new Date(date.getTime() / 60000L * 60000L);
    }

    /**
     * Отображает в UI границу формирования КЖ.
     *
     * @param textView Поле, в котором отображается граница
     * @param time     Временная граница
     */
    private void setPeriodValue(TextView textView, Date time) {
        String date = "";
        String format = "dd.MM.yyyy HH:mm";
        SimpleDateFormat dateFormat = new SimpleDateFormat(format, Locale.getDefault());
        date = dateFormat.format(time);
        textView.setText(date);
    }

    class ViewHolder {
        FilterEditText shiftNum;
        TextView shiftDate;
        View discountShiftSheet;
        View reportsTerminalOperations;
        View reportsTerminalDayClose;
        View salesForEttLog;
        View testPD;
        View testShiftSheet;
        View shiftSheet;
        TextView auditTrail;
        View auditTrailDetailLayout;
        TextView auditTrailPeriodStart;
        TextView auditTrailPeriodEnd;
        TextView auditTrailPeriodStartPrefix;
        TextView auditTrailPeriodEndPrefix;
        Button printAuditTrail;
        RadioGroup auditTrailPeriodType;
    }
}
