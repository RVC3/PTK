package ru.ppr.cppk;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

import ru.ppr.cppk.di.Dagger;
import ru.ppr.cppk.di.Di;
import ru.ppr.cppk.entity.event.base34.CashRegisterEvent;
import ru.ppr.cppk.entity.event.model.Cashier;
import ru.ppr.cppk.entity.settings.LocalUser;
import ru.ppr.cppk.entity.settings.PrivateSettings;
import ru.ppr.cppk.localdb.model.CashRegister;
import ru.ppr.cppk.localdb.model.MonthEvent;
import ru.ppr.cppk.logic.ShiftManager;
import ru.ppr.cppk.settings.CommonMenuActivity;
import ru.ppr.cppk.settings.SharedPreferencesUtils;
import ru.ppr.cppk.systembar.SystemBarActivity;
import ru.ppr.cppk.ui.activity.base.Navigator;
import ru.ppr.logger.Logger;
import ru.ppr.nsi.entity.ProductionSection;
import ru.ppr.nsi.entity.Station;
import ru.ppr.nsi.repository.ProductionSectionRepository;
import ru.ppr.security.entity.PermissionDvc;
import ru.ppr.security.entity.RoleDvc;
import ru.ppr.security.entity.UserDvc;


/**
 * Активити которое показывает кнопку "Начать работу" либо "Открыть смену".
 */
public class WelcomeActivity extends SystemBarActivity implements View.OnClickListener {

    private static final String TAG = WelcomeActivity.class.getSimpleName();
    private static final String RUN_OPEN_SHIFT = "RUN_OPEN_SHIFT";

    private Globals globals;
    private RoleDvc roleId;

    private TextView lastName, firstName, midleName, roleTextView, rdsError;
    private Button beginWorkBtn, openShiftBtn, menuBtn;

    private IntentReceiver disconnectReceiver;

    private Holder<PrivateSettings> privateSettingsHolder;
    private ProductionSectionRepository productionSectionRepository;

    /**
     * @param context
     * @param runOpenShift true - после настройки нитерфейса запуститься поцесс открытия смены если она закрыта
     * @return
     */
    public static Intent getCallingIntent(Context context, boolean runOpenShift) {
        Intent intent = new Intent(context, WelcomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra(RUN_OPEN_SHIFT, runOpenShift);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        privateSettingsHolder = Dagger.appComponent().privateSettingsHolder();
        productionSectionRepository = Dagger.appComponent().productionSectionRepository();

        setContentView(R.layout.welcome_fragment);
        globals = (Globals) getApplication();

        lastName = (TextView) findViewById(R.id.welcome_fragment_last_name);      // фамилия
        firstName = (TextView) findViewById(R.id.welcome_fragment_first_name);    // имя
        midleName = (TextView) findViewById(R.id.welcome_fragment_middle_name);   // отчество
        roleTextView = (TextView) findViewById(R.id.tvRole);
        beginWorkBtn = (Button) findViewById(R.id.beginWork);
        openShiftBtn = (Button) findViewById(R.id.openShift);
        menuBtn = (Button) findViewById(R.id.menu);
        rdsError = (TextView) findViewById(R.id.rds_not_load);

        beginWorkBtn.setOnClickListener(this);
        openShiftBtn.setOnClickListener(this);
        menuBtn.setOnClickListener(this);
    }

    /**
     * Настраивает доступность функций для разный ролей пользователей
     */
    private void configAccess() {
        // запрещаем или показываем кнопку "Открыть смену"
        openShiftBtn.setEnabled(getSecurityDaoSession().getRolePermissionDvcDao().isPermissionEnabled(di().getUserSessionInfo().getCurrentUser().getRole(), PermissionDvc.OpenShift));
    }

    private void configInterface() {

        // устанавливаем роль
        roleId = Di.INSTANCE.getUserSessionInfo().getCurrentUser().getRole();
        String roleString = roleId.getName();
        roleTextView.setText(roleString);

        // получаем имя учетной записи залогиневшегося пользователя
        LocalUser currentUser = di().getUserSessionInfo().getCurrentUser();
        // устанавливаем ФИО
        UserDvc userDvc = getSecurityDaoSession().getUserDvcDao().getUserFromUserDvc(currentUser.getLogin());
        if (userDvc == null) {
            //Устанавливаем то, что считанно с карты
            lastName.setText(currentUser.getName());
        } else {
            lastName.setText(userDvc.getLastName());
            firstName.setText(userDvc.getFirstName());
            midleName.setText(userDvc.getMiddleName());
        }

        // Обновляем информацию о Cashier
        Cashier currentCashier = new Cashier();
        currentCashier.setFio(currentUser.getName());
        currentCashier.setLogin(currentUser.getLogin());
        di().getCashierSessionInfo().setCurrentCashier(currentCashier);

        CashRegisterEvent lastCashRegisterEvent = getLocalDaoSession().getCashRegisterEventDao().getLastCashRegisterEvent();
        Cashier previousCashier = lastCashRegisterEvent == null ? null : getLocalDaoSession().cashierDao().load(lastCashRegisterEvent.getCashierId());
        String previousCashierLogin = previousCashier == null ? null : previousCashier.getLogin();

        boolean shiftMustBeTransferred = false;
        if (!TextUtils.equals(currentCashier.getLogin(), previousCashierLogin)) {
            // Новый кассир
            if (di().getShiftManager().isShiftOpened()) {
                currentCashier.setOfficialCode(String.valueOf(Integer.valueOf(previousCashier.getOfficialCode()) + 1));
                shiftMustBeTransferred = true;
            }
            CashRegister cashRegister = Di.INSTANCE.printerManager().getCashRegister();
            Dagger.appComponent().cashRegisterEventCreator()
                    .setCashRegister(cashRegister)
                    .create();
        }

        // Открываем месяц, если нужно
        MonthEvent monthEvent = getLocalDaoSession().getMonthEventDao().getLastMonthEvent();

        if (monthEvent == null || monthEvent.getStatus() == MonthEvent.Status.CLOSED) {
            //либо нет месяца, либо он закрыт, надо открыть
            monthEvent = openMonth(monthEvent == null ? 1 : monthEvent.getMonthNumber() + 1);
            if (monthEvent == null) {
                throw new IllegalStateException("Month is not opened");
            }
        }

        // Записываем событие передачи смены
        if (shiftMustBeTransferred) {
            try {
                ShiftManager.getInstance().transferShift();
            } catch (Exception e) {
                throw new IllegalStateException("Could not transfer shift");
            }
        }

        openShiftBtn.setText(R.string.open_shift);
        menuBtn.setVisibility(View.VISIBLE);

        if (ShiftManager.getInstance().isShiftOpenedWithoutTestPd()) {
            openShiftBtn.setText(R.string.begin_work);
        }

        beginWorkBtn.setVisibility(ShiftManager.getInstance().isShiftOpenedWithTestPd() ? View.VISIBLE : View.GONE);
        openShiftBtn.setVisibility(!ShiftManager.getInstance().isShiftOpenedWithTestPd() ? View.VISIBLE : View.GONE);

        configAccess();

        beginWorkBtn.setClickable(true);
        openShiftBtn.setClickable(true);
        if (!Di.INSTANCE.nsiVersionManager().checkCurrentVersionIdValid()
                || !Di.INSTANCE.nsiDataContractsVersionChecker().isDataContractVersionValid()) {
            showErrorOpenShift(getString(R.string.rds_not_load));
        } else {
            rdsError.setVisibility(View.GONE);
        }

        //проверим флаг запуска открытия смены, и если надо, то запустим открытие
        if (!ShiftManager.getInstance().isShiftOpenedWithTestPd() && getIntent().getBooleanExtra(RUN_OPEN_SHIFT, false)) {
            if (getSecurityDaoSession().getRolePermissionDvcDao().isPermissionEnabled(di().getUserSessionInfo().getCurrentUser().getRole(), PermissionDvc.OpenShift)) {
                startOpenShift();
            }
            getIntent().removeExtra(RUN_OPEN_SHIFT);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // в данном месте воспроизвелось в рамках бага (http://agile.srvdev.ru/browse/CPPKPP-35471),
        // уже фиксилось в рамках другого аналогичного бага для экрана CommonMenuActivity,
        // см. CommonMenuActivity.onResume()
        if (isCurrentScreenValid()) {
            regDisconnectReciever();
            configInterface();
        }
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.beginWork:
                beginWork();
                break;

            case R.id.menu:
                openMenu();
                break;

            case R.id.openShift:
                startOpenShift();
                break;

            default:
                break;
        }

    }

    private void beginWork() {
        Navigator.navigateToMenuActivity(this);
    }

    private void openMenu() {
        startActivity(new Intent(this, CommonMenuActivity.class));
    }

    private void startOpenShift() {
        boolean isBus = privateSettingsHolder.get().isTransferControlMode();
        if (isBus) {
            // http://agile.srvdev.ru/browse/CPPKPP-42649
            // Обновляем дату и время трансфера каждый раз при открытии смены
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            Date transferDepartureDateTime = calendar.getTime();
            SharedPreferencesUtils.setTransferDepartureDateTime(globals, transferDepartureDateTime);
        }

        if (getCurrentProductionSection() == null) {
            showErrorOpenShift(getString(R.string.choose_section));
        } else if (getCurrentBindingStation() == null) {
            showErrorOpenShift(getString(R.string.choose_binding_station));
        } else if (privateSettingsHolder.get().isMobileCashRegister() && getCurrentWorkStation() == null) {
            showErrorOpenShift(getString(R.string.choose_work_station));
        } else if (!getLocalDaoSession().getTicketTapeEventDao().isTicketTapeSet()) {
            showErrorOpenShift(getString(R.string.set_ticket_tape));
        } else if (!isTransferStationsOk()) {
            showErrorOpenShift(getString(R.string.welcome_activity_shift_cant_be_opened_set_transfer_route));
        } else if (Dagger.appComponent().updateEventRepository().isStopListVersionValid(privateSettingsHolder.get().getStopListValidTime())) {
            Navigator.navigateToOpenShiftActivity(this);
        } else {
            showErrorOpenShift(getString(R.string.stop_list_is_overdue));
        }
    }

    /**
     * Показывает сообщение о невозможности открыть смену из-за устаревших
     * стоплистов или если не выбран участок
     */
    private void showErrorOpenShift(String message) {
        if (message != null) {
            openShiftBtn.setClickable(false);
            beginWorkBtn.setClickable(false);
            rdsError.setVisibility(View.VISIBLE);
            rdsError.setText(message);
        }
    }

    private boolean isTransferStationsOk() {
        if (!privateSettingsHolder.get().isTransferControlMode())
            return true;
        return Dagger.appComponent().transferRouteChecker().checkTransferRouteStationValid();
    }

    private ProductionSection getCurrentProductionSection() {
        PrivateSettings privateSettings = privateSettingsHolder.get();
        int pSectionId = privateSettings.getProductionSectionId();

        return productionSectionRepository.load((long) pSectionId, Di.INSTANCE.nsiVersionManager().getCurrentNsiVersionId());
    }

    /**
     * Вернет текущую станцию работы ПТК в режиме мобильной кассы на выход
     *
     * @return
     */
    private Station getCurrentWorkStation() {
        return Dagger.appComponent().stationRepository().load(
                (long) privateSettingsHolder.get().getCurrentStationCode(),
                Di.INSTANCE.nsiVersionManager().getCurrentNsiVersionId());
    }

    private Station getCurrentBindingStation() {
        return Dagger.appComponent().stationRepository().load(
                (long) Globals.getInstance().getPrivateSettingsHolder().get().getSaleStationCode(),
                Di.INSTANCE.nsiVersionManager().getCurrentNsiVersionId());
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK) {
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    /**
     * Регистрирует слушателя отключения от АРМ
     */
    private void regDisconnectReciever() {
        disconnectReceiver = new IntentReceiver();
        IntentFilter disconnectedFilter = new IntentFilter(Intent.ACTION_POWER_DISCONNECTED);
        globals.registerReceiver(disconnectReceiver, disconnectedFilter);
    }

    /**
     * Отменяет регистрацию слушателя
     *
     * @param receiver
     */
    private void unregResivers(BroadcastReceiver receiver) {
        try {
            if (receiver != null)
                globals.unregisterReceiver(receiver);
        } catch (Exception e) {
            Logger.error(TAG, e);
        }
    }

    @Override
    protected void onPause() {
        unregResivers(disconnectReceiver);
        super.onPause();
    }

    public class IntentReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equalsIgnoreCase(Intent.ACTION_POWER_DISCONNECTED)) {
                Logger.trace(TAG, "Arm - ACTION_POWER_DISCONNECTED");
                configInterface();
            }
        }
    }

    @Override
    /** Переопределяем обработчик чтобы запретить переход на главное меню по нажатию на кнопку Settings*/
    public void onClickSettings() {
        /* NOP */
    }

    /**
     * Открывает новый месяц
     *
     * @param number номер нового месяца
     * @return событие нового открытого месяца, либо null если месяц не удалось открыть
     */
    @NonNull
    private MonthEvent openMonth(int number) {
        return Dagger.appComponent().monthEventCreator()
                .setStatus(MonthEvent.Status.OPENED)
                .setMonthNumber(number)
                .setOpenDate(new Date())
                .setMonthId(UUID.randomUUID().toString())
                .create();
    }
}
