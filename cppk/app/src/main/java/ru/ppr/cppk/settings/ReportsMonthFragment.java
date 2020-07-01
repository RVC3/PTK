package ru.ppr.cppk.settings;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import ru.ppr.core.ui.widget.SimpleLseView;
import ru.ppr.cppk.FragmentParent;
import ru.ppr.cppk.Globals;
import ru.ppr.cppk.R;
import ru.ppr.cppk.di.Dagger;
import ru.ppr.cppk.di.Di;
import ru.ppr.cppk.exceptions.PrettyException;
import ru.ppr.cppk.helpers.SchedulersCPPK;
import ru.ppr.cppk.localdb.model.MonthEvent;
import ru.ppr.cppk.logic.TicketTapeChecker;
import ru.ppr.cppk.printer.exception.IncorrectEKLZNumberException;
import ru.ppr.cppk.printer.rx.operation.btMonthReport.PrintBtMonthReportOperation;
import ru.ppr.cppk.printer.rx.operation.discountMonthSheet.PrintDiscountMonthSheetOperation;
import ru.ppr.cppk.printer.rx.operation.shiftOrMonthSheet.PrintShiftOrMonthSheetOperation;
import ru.ppr.cppk.reports.ReportBTMonthlySheet;
import ru.ppr.cppk.reports.ReportDiscountMonthSheet;
import ru.ppr.cppk.reports.ReportShiftOrMonthlySheet;
import ru.ppr.cppk.systembar.FeedbackProgressDialog;
import ru.ppr.cppk.ui.activity.base.Navigator;
import ru.ppr.cppk.ui.adapter.simple.MonthEventsAdapter;
import ru.ppr.cppk.ui.dialog.SimpleDialog;
import ru.ppr.cppk.ui.widget.FilterEditText;
import ru.ppr.cppk.ui.widget.textWatchers.NumberTextWatcher;
import ru.ppr.ikkm.exception.PrinterIsNotConnectedException;
import ru.ppr.logger.Logger;
import ru.ppr.nsi.repository.FineRepository;
import ru.ppr.security.entity.PermissionDvc;
import ru.ppr.security.entity.RoleDvc;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;

/**
 * Экран формирования отчетности (вкладка "Месяц").
 */
public class ReportsMonthFragment extends FragmentParent {

    private static final String TAG = Logger.makeLogTag(ReportsMonthFragment.class);

    private final FineRepository fineRepository;

    private static final int OPERATION_PRINT_MONTH_SHEET = 1;
    private static final int OPERATION_PRINT_TEST_MONTH_SHEET = 2;
    private static final int OPERATION_PRINT_DISCOUNT_MONTH_SHEET = 3;
    private static final int OPERATION_PRINT_BT_MONTHLY_SHEET = 4;

    private static final int REQUEST_CODE_SET_TICKET_TAPE = 101;
    private static final int REQUEST_CODE_ACTIVATE_EKLZ = 102;

    private ViewHolder viewHolder;
    private MonthEvent selectedMonth;
    private int currentOperation;

    private boolean isMonthOpened;
    private MonthEvent currentMonth;

    /**
     * Время задержки между тапами
     */
    private static final int TAP_LOCK_DELAY = 400;

    /**
     * Флаг заблокированности возможности тапа
     */
    private boolean isTapLocked = false;

    private Handler handler;


    private FeedbackProgressDialog progressDialog;

    private OnFragmentInteractionListener onFragmentInteractionListener;

    public ReportsMonthFragment() {
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
        currentMonth = getLocalDaoSession().getMonthEventDao().getLastMonthEvent();
        isMonthOpened = currentMonth != null && currentMonth.getStatus() != MonthEvent.Status.CLOSED;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragmnet_reports_month, null);

        viewHolder = new ViewHolder();
        viewHolder.monthNum = (FilterEditText) view.findViewById(R.id.monthNum);
        viewHolder.monthDate = (TextView) view.findViewById(R.id.monthDate);
        viewHolder.testMonthSheet = view.findViewById(R.id.testMonthSheet);
        viewHolder.monthSheet = view.findViewById(R.id.monthSheet);
        viewHolder.discountMonthSheet = view.findViewById(R.id.discountMonthSheet);
        viewHolder.BTMonthlySheet = view.findViewById(R.id.BTMonthlySheet);

        viewHolder.monthNum.addTextChangedListener(new NumberTextWatcher());

        bindListeners();

        progressDialog = new FeedbackProgressDialog(getActivity());
        progressDialog.setMessage(getString(R.string.printer_PrintingNow));
        progressDialog.setCancelable(false);

        setSelectedMonth(currentMonth, false);

        return view;
    }

    /**
     * Привязка слушаталей событий к UI-элементам.
     */
    private void bindListeners() {

        viewHolder.monthDate.setOnClickListener(v -> {
            if (!isTapLocked) {
                isTapLocked = true;
                DatePickerFragment datePickerFragment = new DatePickerFragment();
                datePickerFragment.show(getFragmentManager(), "datePickerFragment");
                handler.postDelayed(() -> isTapLocked = false, TAP_LOCK_DELAY);
            }
        });

        viewHolder.monthNum.setOnBackListener(() -> {
            setSelectedMonth(selectedMonth, false);
            return false;
        });

        viewHolder.monthNum.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                int monthNumber = -1;
                String valueString = viewHolder.monthNum.getText().toString().trim().replaceAll("[^\\d]", "");
                if (!valueString.isEmpty()) {
                    monthNumber = Integer.valueOf(valueString);
                }
                List<MonthEvent> monthsWithNumber = getLocalDaoSession().getMonthEventDao().getAllMonthsByNumber(monthNumber);
                if (monthsWithNumber.isEmpty()) {
                    setSelectedMonth(selectedMonth, true);
                } else if (monthsWithNumber.size() == 1) {
                    setSelectedMonth(monthsWithNumber.get(0), true);
                } else {
                    MonthListFragment monthListFragment = new MonthListFragment();
                    monthListFragment.setMonths(monthsWithNumber);
                    monthListFragment.show(getFragmentManager(), "monthListFragment");
                }
            }
            return false;
        });

        // Кнопки печати отчетов
        viewHolder.monthSheet.setOnClickListener(printReportBtnClickListener);
        viewHolder.testMonthSheet.setOnClickListener(printReportBtnClickListener);
        viewHolder.discountMonthSheet.setOnClickListener(printReportBtnClickListener);
        viewHolder.BTMonthlySheet.setOnClickListener(printReportBtnClickListener);

    }

    private View.OnClickListener printReportBtnClickListener = v -> {
        if (!isTapLocked) {
            isTapLocked = true;
            switch (v.getId()) {
                case R.id.monthSheet: {
                    currentOperation = OPERATION_PRINT_MONTH_SHEET;
                    break;
                }
                case R.id.testMonthSheet: {
                    currentOperation = OPERATION_PRINT_TEST_MONTH_SHEET;
                    break;
                }
                case R.id.discountMonthSheet: {
                    currentOperation = OPERATION_PRINT_DISCOUNT_MONTH_SHEET;
                    break;
                }
                case R.id.BTMonthlySheet: {
                    currentOperation = OPERATION_PRINT_BT_MONTHLY_SHEET;
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
            case OPERATION_PRINT_MONTH_SHEET: {
                printMonthSheet();
                break;
            }
            case OPERATION_PRINT_TEST_MONTH_SHEET: {
                printTestMonthSheet();
                break;
            }
            case OPERATION_PRINT_DISCOUNT_MONTH_SHEET: {
                printDiscountMonthSheet();
                break;
            }
            case OPERATION_PRINT_BT_MONTHLY_SHEET: {
                printBTMonthlySheet();
                break;
            }
        }
    }

    /**
     * Устаналивает месяц, по которому следует формировать отчетность.
     *
     * @param monthEvent   Событие открытия/закрытия месяца.
     * @param showErrorMsg {@code true}, если нужно показать сообщение об ошибке при отсутствии месяца,
     *                     [@code false} иначе.
     */
    private void setSelectedMonth(MonthEvent monthEvent, boolean showErrorMsg) {
        if (monthEvent == null) {
            if (showErrorMsg) {
                SimpleDialog simpleDialog = SimpleDialog.newInstance(
                        null,
                        getString(R.string.reports_no_months_with_number_msg),
                        getString(R.string.dialog_close),
                        null,
                        LinearLayout.VERTICAL,
                        -1
                );
                simpleDialog.show(getFragmentManager(), SimpleDialog.FRAGMENT_TAG);
            }
        } else {
            MonthEvent lastMonthEvent = getLocalDaoSession().getMonthEventDao().getLastMonthByMonthId(monthEvent.getMonthId());
            selectedMonth = lastMonthEvent;
        }
        ////////////////////////////////////////////
        if (selectedMonth == null) {
            viewHolder.monthDate.setText("");
            viewHolder.monthNum.setText("");
        } else {
            String format = "MM.yyyy";
            SimpleDateFormat dateFormat = new SimpleDateFormat(format, Locale.getDefault());
            String date = dateFormat.format(monthEvent.getOpenDate());
            viewHolder.monthDate.setText(date);
            viewHolder.monthNum.setText(String.valueOf(monthEvent.getMonthNumber()));
        }

        updateViewState();
    }

    /**
     * Настраивает доступность кнопок в UI.
     */
    private void updateViewState() {
        RoleDvc role = di().getUserSessionInfo().getCurrentUser().getRole();
        boolean currentMonthIsSelected = selectedMonth != null && currentMonth != null && selectedMonth.getMonthId().equals(currentMonth.getMonthId()) && isMonthOpened;
        viewHolder.testMonthSheet.setEnabled(selectedMonth != null && currentMonthIsSelected && getSecurityDaoSession().getRolePermissionDvcDao().isPermissionEnabled(role, PermissionDvc.TestMonthlySheet));
        viewHolder.monthSheet.setEnabled(selectedMonth != null && !currentMonthIsSelected && getSecurityDaoSession().getRolePermissionDvcDao().isPermissionEnabled(role, PermissionDvc.MonthlySheet));
        viewHolder.discountMonthSheet.setEnabled(selectedMonth != null && getSecurityDaoSession().getRolePermissionDvcDao().isPermissionEnabled(role, PermissionDvc.DiscountedMonthlySheet));
        viewHolder.BTMonthlySheet.setEnabled(selectedMonth != null && !currentMonthIsSelected && getSecurityDaoSession().getRolePermissionDvcDao().isPermissionEnabled(role, PermissionDvc.ReportPOSMonth));
    }

    /**
     * Диалоговое окно с выбором даты месяца.
     */
    @SuppressLint("ValidFragment")
    public class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {

        private boolean fired = false;

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
                public void onDateChanged(DatePicker view, int year, int month, int day) {
                    c.set(Calendar.YEAR, year);
                    c.set(Calendar.MONTH, month);
                    c.set(Calendar.DAY_OF_MONTH, day);
                }
            };
            datePickerDialog.setTitle(R.string.reports_select_date_title);

            // Костыль, скрываем через рефлексию выбор дня
            DatePicker picker = datePickerDialog.getDatePicker();
            try {
                Field field = picker.getClass().getDeclaredField("mDaySpinner");
                field.setAccessible(true);
                Object mDaySpinner;
                mDaySpinner = field.get(picker);
                ((View) mDaySpinner).setVisibility(View.GONE);
            } catch (Exception e) {
                e.printStackTrace();
            }

            return datePickerDialog;
        }

        public void onDateSet(DatePicker view, int year, int month, int day) {

            // Потому что вызывается дважды
            if (fired) {
                return;
            } else {
                fired = true;
            }

            Calendar calendar = Calendar.getInstance();
            calendar.set(year, month, day);

            List<MonthEvent> months = getLocalDaoSession().getMonthEventDao().getMonthsAtCalendarMonth(calendar.getTime());

            if (months.isEmpty()) {
                SimpleDialog simpleDialog = SimpleDialog.newInstance(
                        null,
                        getString(R.string.reports_no_months_at_date_msg),
                        getString(R.string.dialog_close),
                        null,
                        LinearLayout.VERTICAL,
                        -1
                );
                simpleDialog.show(getFragmentManager(), SimpleDialog.FRAGMENT_TAG);
            } else {
                MonthListFragment monthListFragment = new MonthListFragment();
                monthListFragment.setMonths(months);
                monthListFragment.show(getFragmentManager(), "monthListFragment");
            }
        }
    }

    /**
     * Диалоговое окно со списком месяцев.
     */
    @SuppressLint("ValidFragment")
    public class MonthListFragment extends DialogFragment implements AdapterView.OnItemClickListener {

        private List<MonthEvent> monthList;
        private MonthEventsAdapter adapter;

        public void setMonths(List<MonthEvent> months) {
            this.monthList = months;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(R.string.reports_months_list_title);
            ListView list = new ListView(getActivity());

            adapter = new MonthEventsAdapter(getActivity());
            adapter.setItems(monthList);

            list.setAdapter(adapter);
            list.setOnItemClickListener(this);

            builder.setView(list);

            return builder.create();

        }

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            setSelectedMonth(adapter.getItem(position), false);
            dismiss();
        }
    }

    /**
     * Печатает пробную месячную ведомость.
     */
    private void printTestMonthSheet() {
        progressDialog.show();
        ///////////////////////////////
        new ReportShiftOrMonthlySheet(fineRepository)
                .setBuildForLastMonth()
                .setSheetTypeTestAtMonthMiddle()
                .setPrintFooter()
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
     * Печатает месячную ведомость.
     */
    private void printMonthSheet() {
        progressDialog.show();
        new ReportShiftOrMonthlySheet(fineRepository)
                .setMonthId(selectedMonth.getMonthId())
                .setSheetTypeMonth()
                .setPrintFooter()
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
     * Печатает льготную месячную ведомость.
     */
    private void printDiscountMonthSheet() {
        progressDialog.show();
        new ReportDiscountMonthSheet()
                .setMonthId(selectedMonth.getMonthId())
                .buildAndPrintObservable(Di.INSTANCE.printerManager().getPrinter())
                .subscribeOn(SchedulersCPPK.printer())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<PrintDiscountMonthSheetOperation.Result>() {
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
                    public void onNext(PrintDiscountMonthSheetOperation.Result result) {

                    }
                });
    }

    /**
     * Печатает месячный отчёт по операциям POS-терминала
     */
    private void printBTMonthlySheet() {
        progressDialog.show();

        new ReportBTMonthlySheet()
                .setMonthId(selectedMonth.getMonthId())
                .buildAndPrintObservable(Di.INSTANCE.printerManager().getPrinter())
                .subscribeOn(SchedulersCPPK.printer())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<PrintBtMonthReportOperation.Result>() {
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
                    public void onNext(PrintBtMonthReportOperation.Result result) {

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

    class ViewHolder {
        FilterEditText monthNum;
        TextView monthDate;
        View monthSheet;
        View discountMonthSheet;
        View BTMonthlySheet;
        View testMonthSheet;
    }
}
