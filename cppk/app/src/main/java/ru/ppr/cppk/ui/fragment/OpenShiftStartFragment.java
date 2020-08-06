package ru.ppr.cppk.ui.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import ru.ppr.core.ui.widget.SimpleLseView;
import ru.ppr.cppk.FragmentParent;
import ru.ppr.cppk.Globals;
import ru.ppr.cppk.Holder;
import ru.ppr.cppk.R;
import ru.ppr.cppk.db.DateFormatOperations;
import ru.ppr.cppk.di.Dagger;
import ru.ppr.cppk.di.Di;
import ru.ppr.cppk.entity.settings.PrivateSettings;
import ru.ppr.cppk.entity.settings.ReportType;
import ru.ppr.cppk.exceptions.PrettyException;
import ru.ppr.cppk.helpers.CommonSettingsStorage;
import ru.ppr.cppk.helpers.DeferredActionHandler;
import ru.ppr.cppk.helpers.SchedulersCPPK;
import ru.ppr.cppk.localdb.model.MonthEvent;
import ru.ppr.cppk.localdb.model.PaperUsage;
import ru.ppr.cppk.localdb.model.ShiftEvent;
import ru.ppr.cppk.logic.CashRegisterValidityChecker;
import ru.ppr.cppk.logic.DocumentTestPd;
import ru.ppr.cppk.logic.EklzChecker;
import ru.ppr.cppk.logic.ShiftManager;
import ru.ppr.cppk.logic.TicketTapeChecker;
import ru.ppr.cppk.managers.PrinterManager;
import ru.ppr.cppk.printer.exception.IncorrectEKLZNumberException;
import ru.ppr.cppk.printer.rx.operation.openShift.OpenShiftOperation;
import ru.ppr.cppk.printer.rx.operation.shiftOrMonthSheet.PrintShiftOrMonthSheetOperation;
import ru.ppr.cppk.reports.ReportShiftOrMonthlySheet;
import ru.ppr.cppk.settings.AccountingTicketTapeStartActivity;
import ru.ppr.cppk.settings.SetTimeActivity;
import ru.ppr.cppk.systembar.FeedbackProgressDialog;
import ru.ppr.cppk.ui.activity.base.Navigator;
import ru.ppr.cppk.ui.activity.settingsPrinter.SettingsPrinterActivity;
import ru.ppr.ikkm.Printer;
import ru.ppr.ikkm.exception.PrinterIsNotConnectedException;
import ru.ppr.logger.Logger;
import ru.ppr.nsi.repository.FineRepository;
import rx.Observable;
import rx.Observable.OnSubscribe;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;

public class OpenShiftStartFragment extends FragmentParent implements OnClickListener {

    private static final String TAG = Logger.makeLogTag(OpenShiftStartFragment.class);

    private final FineRepository fineRepository;

    private static final int REQUEST_CODE_SET_TICKET_TAPE = 101;
    private static final int REQUEST_CODE_ACTIVATE_EKLZ = 102;

    private OnFragmentInteractionListener onFragmentInteractionListener;

    private boolean autoSyncTime = false;
    /**
     * http://agile.srvdev.ru/browse/CPPKPP-38810
     * Флаг процесса Открытия смены
     */
    private boolean openShiftInProgress = false;
    private ViewHolder viewHolder;
    private FeedbackProgressDialog progressDialog;
    private SimpleLseView simpleLseView;

    private Holder<PrivateSettings> privateSettingsHolder;
    private CommonSettingsStorage commonSettingsStorage;
    private PrinterManager printerManager;

    private final DeferredActionHandler deferredActionHandler = new DeferredActionHandler();

    public OpenShiftStartFragment() {
        fineRepository = Dagger.appComponent().fineRepository();
    }

    public static Fragment newInstance() {
        return new OpenShiftStartFragment();
    }

    public interface OnFragmentInteractionListener {
        void onShiftOpened();

        void onChangeDayCode();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        privateSettingsHolder = Dagger.appComponent().privateSettingsHolder();
        commonSettingsStorage = Dagger.appComponent().commonSettingsStorage();
        printerManager = Dagger.appComponent().printerManager();

        progressDialog = new FeedbackProgressDialog(getActivity());
        progressDialog.setCancelable(false);

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
    public void onResume() {
        super.onResume();
        deferredActionHandler.resume();
        viewHolder.dayCodeView.setText(getDayCode());
        viewHolder.timeView.setText(DateFormatOperations.getTime(new Date()));
        String curStringDate = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(System.currentTimeMillis());
        viewHolder.dateView.setText(curStringDate);

    }

    @Override
    public void onPause() {
        deferredActionHandler.pause();
        super.onPause();
    }

    @Override
    public void onStop() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
        super.onStop();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.start_open_shift_fragment, container, false);

        if (view == null)
            return super.onCreateView(inflater, container, savedInstanceState);

        viewHolder = new ViewHolder();
        viewHolder.dayCodeView = (TextView) view.findViewById(R.id.day_code);
        viewHolder.dateView = (TextView) view.findViewById(R.id.startOpenShiftDateValue);
        viewHolder.timeView = (TextView) view.findViewById(R.id.time_value);

        Button setTime = (Button) view.findViewById(R.id.change_date_and_time);
        setTime.setOnClickListener(this);
        setTime.setEnabled(!autoSyncTime);

        Button continueBtn = (Button) view.findViewById(R.id.continueBtn);
        continueBtn.setOnClickListener(this);

        Button setDayCode = (Button) view.findViewById(R.id.change_day_code);
        setDayCode.setOnClickListener(this);

        simpleLseView = (SimpleLseView) view.findViewById(R.id.simpleLseView);

        return view;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case REQUEST_CODE_SET_TICKET_TAPE: {
                    boolean isTicketTapeSet = data.getBooleanExtra(AccountingTicketTapeStartActivity.EXTRA_IS_TICKET_TAPE_SET, false);
                    if (isTicketTapeSet) {
                        startOpenShift();
                    }
                    break;
                }
                case REQUEST_CODE_ACTIVATE_EKLZ: {
                    boolean isEKLZActivated = data.getBooleanExtra(SettingsPrinterActivity.EXTRA_IS_EKLZ_ACTIVATED, false);
                    if (isEKLZActivated) {
                        startOpenShift();
                    }
                    break;
                }
            }
        }
    }

    private void startOpenShift() {
        openShiftInProgress = true;
        simpleLseView.hide();
        doStep(false);
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.change_date_and_time:
                startSetTime();
                break;

            case R.id.change_day_code:
                changeDayCode();
                break;

            case R.id.continueBtn:
                if (!openShiftInProgress)
                    startOpenShift();
                break;

            default:
                break;
        }

    }

    private void startSetTime() {
        Intent intentTime = new Intent(getActivity(), SetTimeActivity.class);
        startActivity(intentTime);
    }

    private void changeDayCode() {
        if (onFragmentInteractionListener != null) {
            onFragmentInteractionListener.onChangeDayCode();
        }
    }

    private String getDayCode() {
        PrivateSettings privateSettings = privateSettingsHolder.get();
        int dayCode = privateSettings.getDayCode();
        String s = String.valueOf(dayCode);
        while (s.length() < 4)
            s = "0" + s;
        return s;
    }

    private static class ShiftOpenTempData {
        MonthEvent monthEvent;
        ShiftEvent shiftInfo;
        Date openTime;
        int shiftNum;
        int spndNumber;
        BigDecimal cashInFR = BigDecimal.ZERO;
    }

    private boolean commonSettingsPReportOpenShiftContains(ReportType reportType) {
        for (ReportType tmp : commonSettingsStorage.get().getReportOpenShift()) {
            if (tmp == reportType)
                return true;
        }

        return false;
    }

    private int commonSettingsPReportOpenShiftOrder(ReportType reportType) {
        for (int i = 0; i < commonSettingsStorage.get().getReportOpenShift().length; i++) {
            if (commonSettingsStorage.get().getReportOpenShift()[i] == reportType)
                return i;
        }

        return 0;
    }

    private ShiftOpenTempData shiftOpenTempData;

    /**
     * Состояние окна открытия смены
     */
    private enum OpenShiftState {

        //не ясно в каком состоянии находимся
        UnknownPrinterState(0),

        //состояние принтера получено
        PrepareForOpenShift(1),

        //все готово для открытия смены
        ReadyToOpenShift(2),

        //Смена открыта, тестовый ПД не напечатан
        ShiftOpened(3),

        //Смена открыта, первый отчет напечатан (по умолчанию это тестовый ПД)
        Report1Printed(4),

        //Смена открыта, второй отчет напечатан (по умолчанию это пробная сменная ведомость)
        Report2Priinted(5),

        //Смена открыта, третий отчет напечатан (сейчас не используется сделано на будущее)
        Report3Printed(6);

        int index = 0;

        OpenShiftState(int index) {
            this.index = index;
        }

        public static OpenShiftState get(int index) {
            for (OpenShiftState state : OpenShiftState.values()) {
                if (state.index() == index) {
                    return state;
                }
            }
            return Report3Printed;
        }

        private int index() {
            return index;
        }

        public OpenShiftState getNextStep() {
            return OpenShiftState.get(index + 1);
        }

    }

    private void incrementStateIndex() {
        currentStep = currentStep.getNextStep();
    }

    private OpenShiftState currentStep = OpenShiftState.UnknownPrinterState;

    private void onStepFinished() {
        doStep(true);
    }

    private void onStepFailed(Throwable e) {
        openShiftInProgress = false;
        Logger.error(TAG, e);
        progressDialog.dismiss();
        // Пока бросаем на следующий экран даже если не напечатали ПД
        if (currentStep == OpenShiftState.ShiftOpened) {
            onAllStepsCompleted();
            return;
        }

        SimpleLseView.State.Builder stateBuilder = new SimpleLseView.State.Builder();
        stateBuilder.setMode(SimpleLseView.State.MODE_ERROR);

        if (e instanceof PrinterIsNotConnectedException) {
            stateBuilder.setTextMessage(R.string.printer_not_found_msg);
            stateBuilder.setButton1(R.string.printer_repeat_connection, v -> startOpenShift());
        } else if (e instanceof TicketTapeChecker.TicketTapeIsNotSetException) {
            stateBuilder.setTextMessage(R.string.error_msg_ticket_tape_is_not_set);
            stateBuilder.setButton1(R.string.ticket_tape_is_not_set_view_set_ticket_tape_btn, v -> Navigator.navigateToAccountingTicketTapeStartActivity(getActivity(), this, REQUEST_CODE_SET_TICKET_TAPE, true));
        } else if (e instanceof IncorrectEKLZNumberException) {
            stateBuilder.setTextMessage(R.string.printer_eklz_activation_required_msg);
            stateBuilder.setButton1(R.string.printer_eklz_activation_required_activate, v -> Navigator.navigateToSettingsPrinterActivity(getActivity(), this, REQUEST_CODE_ACTIVATE_EKLZ));
        } else {
            stateBuilder.setTextMessage(R.string.shift_open_failed_msg);
            stateBuilder.setButton1(R.string.sev_btn_repeat, v -> startOpenShift());
        }
        stateBuilder.setButton2(R.string.sev_btn_cancel, v -> simpleLseView.hide());

        simpleLseView.setState(stateBuilder.build());
        simpleLseView.show();
    }

    private void onAllStepsCompleted() {
        Logger.info(TAG, "onAllStepsCompleted");

        openShiftInProgress = false;

        if (di().getPrivateSettings().get().isPosEnabled()) {
            Globals.getInstance().getPosManager().dayStart(null);
        }

        progressDialog.dismiss();

        if (onFragmentInteractionListener != null) {
            onFragmentInteractionListener.onShiftOpened();
        }
    }

    private void doStep(boolean next) {

        if (next) {
            incrementStateIndex();
        }

        if (currentStep == OpenShiftState.ReadyToOpenShift) {
            if (shiftOpenTempData.shiftInfo != null && shiftOpenTempData.shiftInfo.getStatus() != ShiftEvent.Status.ENDED) {
                // Смена уже открыта, начнем с печати
                currentStep = OpenShiftState.ShiftOpened;
            }
        }

        //Если пробный ПД НЕ входит в список того, что нужно напечатать
        //при открытии смены или печать НЕобязательна, то пропускаем шаг
        if (currentStep == OpenShiftState.get(OpenShiftState.ShiftOpened.index() + commonSettingsPReportOpenShiftOrder(ReportType.TestPd)) &&
                (!commonSettingsPReportOpenShiftContains(ReportType.TestPd) ||
                        !commonSettingsStorage.get().isTestPdPrintReq())) {
            currentStep = OpenShiftState.get(OpenShiftState.ShiftOpened.index() + commonSettingsPReportOpenShiftOrder(ReportType.TestShiftShit));
        }

        //Если пробная сменная ведомость НЕ входит в список того, что нужно напечатать
        //при открытии смены или печать НЕобязательна, то пропускаем шаг
        if (currentStep.index() == OpenShiftState.ShiftOpened.index() + commonSettingsPReportOpenShiftOrder(ReportType.TestShiftShit) &&
                (!commonSettingsPReportOpenShiftContains(ReportType.TestShiftShit) ||
                        !commonSettingsStorage.get().isDiscountShiftSheetOpeningShift())) {
            //см. ниже
            currentStep = OpenShiftState.get(OpenShiftState.ShiftOpened.index() + (commonSettingsStorage.get().getReportOpenShift().length > 2 ? 2 : commonSettingsStorage.get().getReportOpenShift().length));
        }

        if (currentStep == OpenShiftState.UnknownPrinterState) {
            updatePrinterState();
        } else if (currentStep == OpenShiftState.PrepareForOpenShift) {
            prepareForOpenShift();
        } else if (currentStep == OpenShiftState.ReadyToOpenShift) {
            openShift();
        } else if (currentStep.index() == OpenShiftState.ShiftOpened.index() + commonSettingsPReportOpenShiftOrder(ReportType.TestPd) && commonSettingsPReportOpenShiftContains(ReportType.TestPd)) {
            printTestPD();
        } else if (currentStep.index() == OpenShiftState.ShiftOpened.index() + commonSettingsPReportOpenShiftOrder(ReportType.TestShiftShit) && commonSettingsPReportOpenShiftContains(ReportType.TestShiftShit)) {
            printTestShiftSheet();
            //проверка на двойку - если через настройки добавят что-то еще, то длина будет 3, а у нас обрабатываются только 2 репорт тайпа
            //без проверки открытие смены в таком случае повиснет, с проверкой - нет. Возможно этого никогда не случатся, но вдруг
        } else if (currentStep.index() == OpenShiftState.ShiftOpened.index() + (commonSettingsStorage.get().getReportOpenShift().length > 2 ? 2 : commonSettingsStorage.get().getReportOpenShift().length)) {
            onAllStepsCompleted();
        }
    }

    private Observable<MonthEvent> getMonthEventForShift() {
        return Observable.create(new OnSubscribe<MonthEvent>() {

            @Override
            public void call(Subscriber<? super MonthEvent> subscriber) {

                //текущий месяц
                MonthEvent monthEvent = getLocalDaoSession().getMonthEventDao().getLastMonthEvent();

                if (monthEvent == null || monthEvent.getStatus() == MonthEvent.Status.CLOSED) {
                    //месяц не открыт, ошибка
                    subscriber.onError(new PrettyException(getString(R.string.shift_open_error_month_is_not_opened)));
                } else {
                    //месяц открылся, можем работать дальше
                    subscriber.onNext(monthEvent);
                    subscriber.onCompleted();
                }
            }
        });
    }

    private void updatePrinterState() {
        deferredActionHandler.post(() -> onStepFinished());
    }

    private Observable<Void> checkCurrentShiftState() {
        return Observable
                .fromCallable(() -> {

                    final ShiftEvent shiftInfo = ShiftManager.getInstance().getCurrentShiftEvent();

                    shiftOpenTempData.shiftInfo = shiftInfo;
                    if (shiftInfo != null && shiftInfo.getStatus() == ShiftEvent.Status.ENDED) {

                        Date date = new Date(System.currentTimeMillis() + Printer.OPEN_SHIFT_MAX_ENABLED_TIME);
                        Date closeShiftDate = shiftInfo.getCloseTime();
                        if (date.before(closeShiftDate)) {

                            // сколько минут прошло с момента закрытия последней смены
                            int minutsAgo = -Math.round(((float) System.currentTimeMillis() - shiftInfo.getCloseTime().getTime()) / 1000 / 60);
                            int hoursAgo = minutsAgo / 60;
                            minutsAgo = minutsAgo - hoursAgo * 60;

                            String msg = getString(R.string.shift_open_error_incorrect_shift_start_time);
                            throw new PrettyException(String.format(msg, hoursAgo, minutsAgo));
                        }

                        //запрет открытия смены в минуте закрытия https://aj.srvdev.ru/browse/CPPKPP-27875
                        Date now = new Date();
                        if (now.after(closeShiftDate) && now.getTime() - closeShiftDate.getTime() < 60 * 1000) {
                            Calendar c = Calendar.getInstance();
                            c.setTime(closeShiftDate);
                            int minuteCloseShift = c.get(Calendar.MINUTE);
                            c.setTime(now);
                            int minuteNow = c.get(Calendar.MINUTE);
                            if (minuteNow == minuteCloseShift) {
                                //String msg = getString(R.string.shift_open_error_incorrect_minute_shift_start_time);
                                int secondsNow = c.get(Calendar.SECOND);
                                int seconds = 60 - secondsNow + 1; // +1, чтоб наверняка
                                //throw new PrettyException(String.format(msg, seconds));
                                return Observable.just((Void) null).delay(seconds, TimeUnit.SECONDS);
                            }
                        }

                    }
                    return Observable.just((Void) null);
                })
                .flatMap(objectObservable -> objectObservable);
    }

    private void prepareForOpenShift() {
        shiftOpenTempData = new ShiftOpenTempData();

        progressDialog.setMessage(getString(R.string.shift_open_progress_preparing_for_open_shift));
        progressDialog.show();

        getMonthEventForShift()
                .doOnNext(monthEvent -> shiftOpenTempData.monthEvent = monthEvent)
                .flatMap(monthEvent -> checkCurrentShiftState())
                .subscribeOn(SchedulersCPPK.background())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Void>() {


                    @Override
                    public void onCompleted() {
                        deferredActionHandler.post(() -> onStepFinished());
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        deferredActionHandler.post(() -> onStepFailed(throwable));
                    }

                    @Override
                    public void onNext(Void aVoid) {
                        // NOP
                    }
                });
    }

    private Observable<Boolean> checkNeedSkipShiftOpening() {
        return Di.INSTANCE.printerManager().getOperationFactory().getGetStateOperation().call()
                .flatMap(state -> Observable.fromCallable(() -> {
                    if (!BigDecimal.ZERO.equals(shiftOpenTempData.cashInFR)) {
                        // В фискальнике лежат деньги, нельзя втихую продолжать
                        Logger.trace(TAG, "checkNeedSkipShiftOpening  = false_1");
                        return false;
                    }

                    EklzChecker eklzChecker = di().printerManager().getEklzChecker();
                    if (!eklzChecker.check(state.getEKLZNumber(), state.getRegNumber())) {
                        // Сменилась ЭКЛЗ, нельзя втихую продолжать
                        Logger.trace(TAG, "checkNeedSkipShiftOpening  = false_2");
                        return false;
                    }

                    if (state.isShiftOpened()) {
                        ShiftEvent shiftEvent = getLocalDaoSession().getShiftEventDao().getLastShiftEvent(ShiftEvent.ShiftProgressStatus.FINISHED_STATUSES);
                        if (shiftEvent != null) {
                            int lastShiftNum = shiftEvent.getShiftNumber();
                            if (lastShiftNum + 1 == state.getShiftNum()) {
                                // Всё норм, можно продолжать втихую
                                Logger.trace(TAG, "checkNeedSkipShiftOpening  = true");
                                return true;
                            }
                        }
                    }
                    Logger.trace(TAG, "checkNeedSkipShiftOpening  = false_3");
                    return false;
                }));
    }

    private void openShift() {
        progressDialog.setMessage(getString(R.string.shift_open_progress_opening_shift));
        progressDialog.show();

        Dagger.appComponent().ticketTapeChecker().checkOrThrow()
                .andThen(Observable
                        .create(new OnSubscribe<OpenShiftOperation.Params>() {

                            @Override
                            public void call(Subscriber<? super OpenShiftOperation.Params> subscriber) {
                                OpenShiftOperation.Params params = new OpenShiftOperation.Params();

                                params.userName = Di.INSTANCE.getUserSessionInfo().getCurrentUser().getName().trim();
                                // порядковый номер кассира в смене
                                params.userId = 1;

                                params.headerParams = Di.INSTANCE.fiscalHeaderParamsBuilder().build();

                                subscriber.onNext(params);
                                subscriber.onCompleted();

                            }
                        })
                        //сверим номер ФН в принтере, если поменялся, то создадим новый CashRegisterEvent
                        //http://agile.srvdev.ru/browse/CPPKPP-44743
                        .flatMap(params -> Di.INSTANCE.printerManager().getCashRegisterFromPrinter(null).toObservable()
                                .doOnNext(cashRegister -> {
                                    if (!printerManager.checkFnSerial(cashRegister.getFNSerial())) {
                                        printerManager.setCashRegister(cashRegister);
                                        Dagger.appComponent().cashRegisterEventCreator()
                                                .setCashRegister(cashRegister)
                                                .create();
                                        Logger.trace(TAG, "CashRegisterEvent created");
                                    }
                                })
                                .map(resultGetStateOperation -> params)
                        )
                        .flatMap(params -> Di.INSTANCE.printerManager().getOperationFactory().getGetOdometerValue()
                                .call()
                                .doOnNext(resultGetOdometerValue -> {
                                    // 1. Получаем показания одометра
                                    Globals.getInstance().getPaperUsageCounter().setCurrentOdometerValueBeforePrinting(resultGetOdometerValue.getOdometerValue());
                                    Globals.getInstance().getPaperUsageCounter().resetPaperUsage(PaperUsage.ID_SHIFT);
                                })
                                .flatMap(resultGetOdometerValue -> Di.INSTANCE.printerManager().getOperationFactory().getGetCashInFrOperation().call())
                                .doOnNext(resultGetCashInFR -> {
                                    // 2. Получаем сумму в ФР
                                    shiftOpenTempData.cashInFR = resultGetCashInFR.getCashInFR();
                                })
                                .flatMap(resultGetCashInFR -> {
                                    // 3. Открываем смену
                                    return checkNeedSkipShiftOpening().flatMap(needSkip -> {
                                        if (needSkip) {
                                            return Di.INSTANCE.printerManager().getOperationFactory().getGetDateOperation().call()
                                                    .doOnNext(date -> {
                                                        Logger.trace(TAG, "Shift opening at printer skipped");
                                                        // 4. Время с принтера
                                                        shiftOpenTempData.openTime = date;
                                                        // 5. Номер открытой смены
                                                        shiftOpenTempData.shiftNum = getLocalDaoSession().getShiftEventDao().getLastShiftEvent(ShiftEvent.ShiftProgressStatus.FINISHED_STATUSES).getShiftNumber() + 1;
                                                    })
                                                    .flatMap(resultGetOdometerValue -> Di.INSTANCE.printerManager().getOperationFactory().getGetLastDocumentInfoOperation().call())
                                                    .doOnNext(lastDocumentInfoResult -> {
                                                        // 6. сквозной номер документа
                                                        shiftOpenTempData.spndNumber = lastDocumentInfoResult.getSpnd();
                                                    })
                                                    .map(result -> null);

                                        } else {
                                            return Di.INSTANCE.printerManager().getOperationFactory().getOpenShiftOperation(params).call()
                                                    .doOnNext(resultOpenShift -> {
                                                        Logger.trace(TAG, "Shift opening at printer executed");
                                                        // 4. Время с принтера
                                                        shiftOpenTempData.openTime = resultOpenShift.getOperationTime();
                                                        // 5. Номер открытой смены
//                                                        shiftOpenTempData.shiftNum = resultOpenShift.getShiftNum();
                                                        shiftOpenTempData.shiftNum = 1;
                                                        // 6. сквозной номер документа
                                                        shiftOpenTempData.spndNumber = resultOpenShift.getSpndNumber();
                                                    })
                                                    .map(result -> null);
                                        }
                                    });
                                })
                        )
                        .flatMap(aVoid -> Observable.create(new OnSubscribe<Void>() {
                            @Override
                            public void call(Subscriber<? super Void> subscriber) {
                                try {
                                    ShiftManager.getInstance().openShift(shiftOpenTempData.shiftNum, shiftOpenTempData.spndNumber, shiftOpenTempData.openTime, shiftOpenTempData.cashInFR);
                                    subscriber.onNext(null);
                                    subscriber.onCompleted();
                                } catch (Exception e) {
                                    subscriber.onError(e);
                                }
                            }
                        }))
                )
                .subscribeOn(SchedulersCPPK.printer())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Void>() {


                    @Override
                    public void onCompleted() {
                        deferredActionHandler.post(() -> onStepFinished());
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        deferredActionHandler.post(() -> onStepFailed(throwable));
                    }

                    @Override
                    public void onNext(Void aVoid) {
                        // NOP
                    }
                });
    }

    private void printTestPD() {
        progressDialog.setMessage(getString(R.string.shift_open_progress_printing_test_pd));
        progressDialog.show();

        Dagger.appComponent().ticketTapeChecker().checkOrThrow()
                .andThen(Dagger.appComponent().fiscalDocStateSynchronizer().rxSyncCheckState())
                .flatMapCompletable(aBoolean -> Dagger.appComponent().printTestCheckInteractor().print())
                .subscribeOn(SchedulersCPPK.printer())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<DocumentTestPd>() {
                               @Override
                               public void onCompleted() {
                                   deferredActionHandler.post(() -> onStepFinished());
                               }

                               @Override
                               public void onError(Throwable throwable) {
                                   deferredActionHandler.post(() -> onStepFailed(throwable));
                               }

                               @Override
                               public void onNext(DocumentTestPd documentTestPd) {
                                   // NOP
                               }
                           }

                );
    }

    private void printTestShiftSheet() {
        progressDialog.setMessage(getString(R.string.shift_open_progress_printing_test_shift_sheet));
        progressDialog.show();

        Dagger.appComponent().ticketTapeChecker().checkOrThrow()
                .andThen(new ReportShiftOrMonthlySheet(fineRepository)
                        .setBuildForLastShift()
                        .setSheetTypeTestAtShiftStart()
                        .buildAndPrintObservable(Di.INSTANCE.printerManager().getPrinter())
                )
                .subscribeOn(SchedulersCPPK.printer())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<PrintShiftOrMonthSheetOperation.Result>() {
                    @Override
                    public void onCompleted() {
                        deferredActionHandler.post(() -> onStepFinished());
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        deferredActionHandler.post(() -> onStepFailed(throwable));
                    }

                    @Override
                    public void onNext(PrintShiftOrMonthSheetOperation.Result result) {
                        //NOP
                    }
                });
    }

    private class ViewHolder {
        TextView dateView;
        TextView timeView;
        TextView dayCodeView;
    }

    public boolean onBackPressed() {
        return (simpleLseView.getVisibility() == View.VISIBLE)
                || (progressDialog != null && progressDialog.isShowing());
    }
}
