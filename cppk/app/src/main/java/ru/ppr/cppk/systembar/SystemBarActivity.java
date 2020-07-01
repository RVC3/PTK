package ru.ppr.cppk.systembar;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import ru.ppr.core.manager.BatteryManager;
import ru.ppr.cppk.BuildConfig;
import ru.ppr.cppk.Globals;
import ru.ppr.cppk.Holder;
import ru.ppr.cppk.R;
import ru.ppr.cppk.db.LocalDaoSession;
import ru.ppr.cppk.di.Dagger;
import ru.ppr.cppk.di.Di;
import ru.ppr.cppk.entity.settings.PrivateSettings;
import ru.ppr.cppk.localdb.model.ShiftEvent;
import ru.ppr.cppk.logic.CriticalNsiChecker;
import ru.ppr.cppk.logic.CriticalNsiVersionDialogDelegate;
import ru.ppr.cppk.logic.ShiftAlarmManager;
import ru.ppr.cppk.logic.ShiftManager;
import ru.ppr.cppk.managers.MobileSignalManager;
import ru.ppr.cppk.managers.ScreenLockManager;
import ru.ppr.cppk.pd.utils.reader.ReaderType;
import ru.ppr.cppk.ui.activity.base.Navigator;
import ru.ppr.cppk.ui.activity.controlreadbarcode.ControlReadBarcodeActivity;
import ru.ppr.cppk.ui.activity.controlreadbsc.ControlReadBscActivity;
import ru.ppr.cppk.ui.activity.controlreadbsc.model.ControlReadBscParams;
import ru.ppr.cppk.ui.activity.readpdfortransfer.model.ReadForTransferParams;
import ru.ppr.cppk.ui.dialog.SimpleDialog;
import ru.ppr.logger.Logger;
import ru.ppr.nsi.NsiDaoSession;
import ru.ppr.security.SecurityDaoSession;
import ru.ppr.security.entity.PermissionDvc;
import ru.ppr.security.entity.RoleDvc;

/**
 * Базовый класса всех Activity, именно здесь отрисовывается кастомный StatusBar, и выполняются все общие для всех Activity действия
 */
@SuppressLint("InflateParams")
public abstract class SystemBarActivity extends LoggedActivity implements HardButtonsListener {

    private static final String TAG = Logger.makeLogTag(SystemBarActivity.class);

    public static final String DIALOG_ATTENTION_CLOSE_SHIFT = "DIALOG_ATTENTION_CLOSE_SHIFT";
    private static final String DIALOG_CRITICAL_NSI_CLOSE = "DIALOG_CRITICAL_NSI_CLOSE";

    private volatile AtomicBoolean mIsResumed = new AtomicBoolean(false);

    private FeedbackProgressDialog pDialog = null;

    /**
     * Откуда считали последнйи раз ПД( БСК или ШК )
     */
    protected ReaderType _readerType = null;

    /**
     * Флаг указывающий на то, идет ли сейчас считывание или нет Если установлен
     * true - идет считывание ПД, считывать повторно нельзя Если установлен
     * false - считывание завершилось, можно запускать повторное считывание
     * Данный флаг необходимо сбрасывать только в том случае, если считывание
     * завершилось с ошибкой, для повторной попытки считывания Если считывание
     * завершилось успешно, то запуститься новое активити с результатом, где
     * этот флаг уже будет сброшен.
     */
    protected boolean isAlreadyRead = false;

    /**
     * Данный флаг разрешает считывание ПД с использованием боковых кнопок
     */
    private boolean canUseHardwareButton = false;

    /**
     * Данный флаг указывает на необходимость проверки нахождения на определенном экране
     */
    private boolean checkValidForScreen = true;

    /**
     * Данный флаг позволяет зарегистрировать ресивер, который срабатывает при
     * истечении времени закрытия смены
     */
    private boolean registerForShiftAlarm = true;

    /**
     * View с батарейкой
     */
    private BatteryLevelView batteryLevelView = null;

    /**
     * View с уровнем сигнала мобильных данных
     */
    private MobileDataSignalView mobileDataSignalView = null;


    /**
     * Флаг, говорит о том что сейчас используется одна из кнопок, остальные лочатся
     */
    private AtomicBoolean isLocked = new AtomicBoolean(false);

    /**
     * Текущая залоченная кнопка
     */
    private View currentUsingBtn = null;

    /**
     * блокирует нажатия на кнопки, для этого нужно кнопку добавить командой addLockedView(Button button)
     */
    protected void lockViews() {
        isLocked.set(true);
    }

    /**
     * разброкирует кнопки(вьюхи), для этого нужно кнопку добавить командой addLockedView(Button button)
     */
    protected void unLockViews() {
        isLocked.set(false);
    }

    private Holder<PrivateSettings> privateSettingsHolder;


    private ScreenLockManager screenLockManager;

    /**
     * Флаг, разрешена ли блокировка экрана
     * По умолчанию - разрешена
     */
    private boolean screenLockAllowed = true;

    /**
     * Флаг, указывающий на валидность текущего экрана, устанавливается в onResume,
     * в тех случаях когда {@link #checkValidForScreen} = true может при неудачной проверке
     * быть установлен в false, чтобы дочерние активити смогли проверить необходимость выполнения того или иного кода
     * доп. пояснения находятся в методе этого класса {@link #onResume}
     * добавлено в рамках http://agile.srvdev.ru/browse/CPPKPP-34891
     */
    private boolean isCurrentScreenValid = true;

    private CriticalNsiChecker criticalNsiChecker;
    private CriticalNsiVersionDialogDelegate criticalNsiVersionDialogDelegate;

    /**
     * добавляет кнопку которая учавствует в системе лока кнопок, т.е. одновременно тапать можно только одну
     */
    public void addLockedView(View view) {
        view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (isLocked.get() && currentUsingBtn != v)
                    return true; // не будем реагировать если кнопка залочена
                int action = event.getAction();
                if (action == MotionEvent.ACTION_DOWN) { //нажали
                    if (isLocked.compareAndSet(false, true)) {
                        currentUsingBtn = v;
                    } else return true;
                } else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) { //отпустили
                    if (isLocked.compareAndSet(true, false)) {
                        currentUsingBtn = null;
                    } else return true;
                }
                if (isActivityResumed()) return false;
                return true;
            }
        });
    }

    @Override
    public void setContentView(int layoutResID) {
        View userView = getLayoutInflater().inflate(layoutResID, null);
        ViewGroup globalView = getGlobalView();
        globalView.addView(userView, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        super.setContentView(globalView);
    }

    @Override
    public void setContentView(View view, LayoutParams params) {
        ViewGroup globalView = getGlobalView();
        globalView.addView(view, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        super.setContentView(globalView, params);
    }

    private ViewGroup getGlobalView() {
        LayoutInflater inf = getLayoutInflater();
        ViewGroup vg = (ViewGroup) inf.inflate(R.layout.global_view, null);
        View sbl = vg.findViewById(R.id.systemBarLayout);
        batteryLevelView = (BatteryLevelView) vg.findViewById(R.id.batteryLevelView);
        mobileDataSignalView = (MobileDataSignalView) vg.findViewById(R.id.mobileDataSignalView);
        CustomDigitalClock cdc = (CustomDigitalClock) vg.findViewById(R.id.customDigitalClock);
        boolean isSplash = SystemBarUtils.isSplash(SystemBarActivity.this);
        sbl.setBackgroundResource((isSplash) ? android.R.color.white : android.R.color.black);
        batteryLevelView.setWhiteThemeEnable(isSplash);
        cdc.setWhiteThemeEnable(isSplash);
        return vg;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setBackgroundDrawable(null);
        super.onCreate(savedInstanceState);
        privateSettingsHolder = Globals.getInstance().getPrivateSettingsHolder();
        screenLockManager = Globals.getInstance().getScreenLockManager();
        criticalNsiVersionDialogDelegate = new CriticalNsiVersionDialogDelegate(
                criticalNsiChecker = new CriticalNsiChecker(di().nsiVersionManager(), di().getUserSessionInfo(), di().permissionChecker()),
                getFragmentManager(),
                getResources(),
                DIALOG_CRITICAL_NSI_CLOSE);

    }


    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        updateLastActionTimestamp();
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        updateLastActionTimestamp();
        return super.dispatchKeyEvent(event);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mIsResumed.set(false);
        setAlreadyRead(false);
        Dagger.appComponent().batteryManager().removeBatteryStateListener(batteryStateListener);
        screenLockManager.removeScreenLockListener(screenLockListener);
        if (registerForShiftAlarm) {
            Globals.getInstance().getShiftAlarmManager().removeAlarmListener(alarmListener);
        }
        //уровень сигнала
        mobileDataSignalView.setVisibility(privateSettingsHolder.get().isUseMobileDataEnabled() ? View.VISIBLE : View.GONE);
        Di.INSTANCE.mobileSignalManager().removeListener(mobileSignalManagerListener);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Dagger.appComponent().batteryManager().addBatteryStateListener(batteryStateListener);
        screenLockManager.addScreenLockListener(screenLockListener);
        // такой костыль используется из-за того что запуск интента на переход в
        // окно ввода пина вызывается в ui потоке, а запуск окна синхронизации
        // происходит из любого места с флагом NO_HISTORY, из-за этого возникает
        // ситуация, что окно ввода пина может вызваться после перехода на окно
        // синхронизации
        if (((Globals) getApplication()).getIsSyncNow() && SystemBarUtils.isEnterPinActivity(SystemBarActivity.this)) {
            finish();
        } else {
            ((Globals) getApplication()).setIsSyncNow(false);
        }

        logActivitysStack();

        mIsResumed.set(true);

        if (checkValidForScreen && !SystemBarUtils.isValid(SystemBarActivity.this)) {
            // недостаточно просто запускать активити и делать return, когда дочерняя активити,
            // в случае с багом это CommonMenuActivity, в onResume обращается к super.onResume
            // т.к. активити могла еще не умереть, соответствено код из onResume после вызовы super выполниться,
            // что и произошло в рамках бага (http://agile.srvdev.ru/browse/CPPKPP-34891),
            // в результате был получен NPE и аварийный режим
            isCurrentScreenValid = false;

            Navigator.navigateToSplashActivity(SystemBarActivity.this, false);
            return;
        }
        isCurrentScreenValid = true;
        // проверяем, и если нужно запускаем сервис синхронизации с АРМ
        SystemBarUtils.checkAndStartArmService(SystemBarActivity.this);
        if (registerForShiftAlarm) {
            Globals.getInstance().getShiftAlarmManager().addAlarmListener(alarmListener);
        }


        final SimpleDialog fragment = (SimpleDialog) getFragmentManager().findFragmentByTag(DIALOG_ATTENTION_CLOSE_SHIFT);
        if (fragment != null) {
            fragment.setDialogNegativeBtnClickListener(attentionDialogNegativeListener);
            fragment.setDialogPositiveBtnClickListener(attentionDialogPositiveListener);
        }

        //уровень сигнала`
        mobileDataSignalView.refreshCurrentSignalStrength();
        mobileDataSignalView.setVisibility(privateSettingsHolder.get().isUseMobileDataEnabled() ? View.VISIBLE : View.GONE);
        Di.INSTANCE.mobileSignalManager().addListener(mobileSignalManagerListener);

    }

    protected Di di() {
        return Di.INSTANCE;
    }

    private ShiftAlarmManager.AlarmListener alarmListener = new ShiftAlarmManager.AlarmListener() {
        @Override
        public void onAttentionDialogShouldBeShown() {
            Logger.trace(TAG, "onAttentionDialogShouldBeShown() START");

            // Если у пользователя нет разрешения, пропустим https://aj.srvdev.ru/browse/CPPKPP-28017
            RoleDvc role = di().getUserSessionInfo().getCurrentUser().getRole();
            //CPPKPP-30922
            if (role.isRoot()) {
                Logger.info(TAG, "onAttentionDialogShouldBeShown() Запрещаем автозакрытие смены для Root пользователя");
            } else if (getSecurityDaoSession().getRolePermissionDvcDao().isPermissionEnabled(role, PermissionDvc.CloseShift)) {
                Logger.info(TAG, "onAttentionDialogShouldBeShown() role = " + role);

                showAttentionCloseShiftDialog();
                Globals.getInstance().getShiftAlarmManager().markActualActionAsHandled();
            }
            Logger.trace(TAG, "onAttentionDialogShouldBeShown() FINISH");
        }

        @Override
        public void onShiftShouldBeClosedNow() {
            Logger.trace(TAG, "onShiftShouldBeClosedNow() START");
            // Если у пользователя нет разрешения, пропустим https://aj.srvdev.ru/browse/CPPKPP-28017
            RoleDvc role = di().getUserSessionInfo().getCurrentUser().getRole();

            //CPPKPP-30922
            if (role.isRoot()) {
                Logger.info(TAG, "onShiftShouldBeClosedNow() Запрещаем автозакрытие смены для Root пользователя");
            } else if (getSecurityDaoSession().getRolePermissionDvcDao().isPermissionEnabled(role, PermissionDvc.CloseShift)) {
                Logger.info(TAG, "onShiftShouldBeClosedNow() role = " + role);

                DialogFragment simpleDialog = (DialogFragment) getFragmentManager().findFragmentByTag(DIALOG_ATTENTION_CLOSE_SHIFT);

                if (simpleDialog != null) {
                    Logger.trace(TAG, "onShiftShouldBeClosedNow() Необходимо скрыть диалоговое окно showAttentionCloseShiftDialog");

                    simpleDialog.dismiss();
                }

                closeShift();
                Globals.getInstance().getShiftAlarmManager().markActualActionAsHandled();
            }
            Logger.trace(TAG, "onShiftShouldBeClosedNow() FINISH");
        }

        @Override
        public void onCriticalNsiCloseDialogShouldBeShown() {
            Logger.trace(TAG, "onCriticalNsiCloseDialogShouldBeShown() START");
            // Если у пользователя нет разрешения, пропустим https://aj.srvdev.ru/browse/CPPKPP-28017
            RoleDvc role = di().getUserSessionInfo().getCurrentUser().getRole();

            if (getSecurityDaoSession().getRolePermissionDvcDao().isPermissionEnabled(role, PermissionDvc.CloseShift)) {
                Logger.info(TAG, "onCriticalNsiCloseDialogShouldBeShown() role = " + role);

                DialogFragment simpleDialog = (DialogFragment) getFragmentManager().findFragmentByTag(DIALOG_ATTENTION_CLOSE_SHIFT);
                DialogFragment criticalNsiCloseDialog = (DialogFragment) getFragmentManager().findFragmentByTag(DIALOG_CRITICAL_NSI_CLOSE);

                if (simpleDialog != null) {
                    Logger.trace(TAG, "onShiftShouldBeClosedNow() Необходимо скрыть диалоговое окно showAttentionCloseShiftDialog");

                    simpleDialog.dismiss();
                }

                if (criticalNsiCloseDialog != null) {
                    Logger.trace(TAG, "onCriticalNsiCloseDialogShouldBeShown() Необходимо скрыть диалоговое окно criticalNsiCloseDialog");

                    criticalNsiCloseDialog.dismiss();
                }

                SystemBarActivity.this.onCriticalNsiCloseDialogShouldBeShown();
                Globals.getInstance().getShiftAlarmManager().markActualActionAsHandled();
            }

            Logger.trace(TAG, "onCriticalNsiCloseDialogShouldBeShown() FINISH");
        }
    };

    private void onCriticalNsiCloseDialogShouldBeShown() {
        Logger.info(TAG, "onCriticalNsiCloseDialogShouldBeShown()");

        ShiftEvent lastShiftEvent = ShiftManager.getInstance().getCurrentShiftEvent();

        if (lastShiftEvent == null) {
            throw new IllegalArgumentException("Shift is null, but alarm manager for close shift was fired");
        }

        Date criticalNsiChangeDate = di().nsiVersionManager().getCriticalNsiChangeDate();

        if (criticalNsiChangeDate == null) {
            throw new IllegalArgumentException("criticalNsiChangeDate is null, but alarm manager for critical nsi attention was fired");
        }

        criticalNsiVersionDialogDelegate.showCriticalNsiCloseDialogIfNeeded(onCriticalNsiDialogShownListener);
    }

    private final SimpleDialog.DialogBtnClickListener onCriticalNsiDialogShownListener = (dialog, dialogId) -> {
        // Проверяем права
        if (criticalNsiChecker.checkCriticalNsiCloseShiftPermissions()) {
            Logger.info(TAG, "onCriticalNsiDialogShownListener() -> ok button clicked");
            // закрываем смену
            closeShift();
        }
    };

    /**
     * Вернет статус - находится ли активити в активном состоянии
     */
    public boolean isActivityResumed() {
        return mIsResumed.get();
    }

    /**
     * Покажет крутилку с заданным текстом, спрятав предудущую
     */
    public void showProgressDialog(int stringResourse) {
        showProgressDialog(getResources().getString(stringResourse));
    }

    /**
     * Покажет крутилку с заданным текстом, спрятав предудущую
     */
    public void showProgressDialog(final String text) {
        runOnUiThread(() -> {
            if (pDialog != null && pDialog.isShowing())
                pDialog.dismiss();
            pDialog = null;
            pDialog = new FeedbackProgressDialog(SystemBarActivity.this);
            pDialog.setMessage(text);
            pDialog.setCancelable(false);
            pDialog.show();
        });
    }

    /**
     * Спрячет текущую крутилку
     */
    public void hideProgressDialog() {
        if (pDialog != null && pDialog.isShowing()) {
            runOnUiThread(() -> {
                pDialog.dismiss();
                pDialog = null;
            });
        }
    }

    /**
     * Выводит в лог ActivitysStack
     */
    public void logActivitysStack() {
        if (BuildConfig.DEBUG) {
            ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
            List<RunningTaskInfo> list = am.getRunningTasks(10);
            for (RunningTaskInfo task : list) {
                if (task.baseActivity.flattenToShortString().startsWith("ru.ppr.cppk")) {
                    Logger.trace("stack", "------------------");
                    Logger.trace("stack", "Count: " + task.numActivities);
                    Logger.trace("stack", "Root: " + task.baseActivity.flattenToShortString());
                    Logger.trace("stack", "Top: " + task.topActivity.flattenToShortString());
                }
            }
        }
    }

    /**
     * Определяет находится ли данная Activity в топе стека
     */
    public boolean isTopActivity() {
        logActivitysStack();
        ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        List<RunningTaskInfo> list = am.getRunningTasks(10);
        for (RunningTaskInfo task : list) {
            if (task.baseActivity.flattenToShortString().startsWith("ru.ppr.cppk")) {
                boolean res = task.topActivity.getClassName().equalsIgnoreCase(this.getClass().getName());
                if (res) return true;
            }
        }
        return false;
    }

    protected boolean isCurrentScreenValid() {
        return isCurrentScreenValid;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == ru.ppr.core.ui.helper.CoppernicKeyEvent.getRfidKeyCode() && canUseHardwareButton) {
            onClickRfrid();
            return true;
        }
        if (keyCode == ru.ppr.core.ui.helper.CoppernicKeyEvent.getBarcodeKeyCode() && canUseHardwareButton) {
            onClickBarcode();
            return true;
        }
        // если кнопку back нажали в процессе считывания запрещаем обработку.
        if (keyCode == KeyEvent.KEYCODE_BACK && isAlreadyRead()) {
            return true;
        }

        if (keyCode == KeyEvent.KEYCODE_POWER) {
            Logger.trace(TAG, "onKeyDown(): keyCode == KeyEvent.KEYCODE_POWER");

            event.startTracking(); // Needed to track long presses

            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_MENU) {
            onClickSettings();
            return true;
        }

        return super.onKeyUp(keyCode, event);
    }

    @Override
    public boolean onKeyLongPress(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_POWER) {
            Logger.trace(TAG, "onKeyLongPress(): keyCode == KeyEvent.KEYCODE_POWER");

            return true;
        }

        return super.onKeyLongPress(keyCode, event);
    }

    @Override
    public void onClickRfrid() {
        Logger.info(TAG, "onClickRfid() Нажали на голубую кнопку RFID");
        if (isActivityResumed()) {
            startReadPd(ReaderType.TYPE_BSC, null);
        }
    }

    @Override
    public void onClickBarcode() {
        Logger.info(TAG, "onClickBarcode() Нажали на голубую кнопку BARCODE");
        if (isActivityResumed()) {
            startReadPd(ReaderType.TYPE_BARCODE, null);
        }
    }

    public void canUserHardwareButton() {
        canUseHardwareButton = true;
    }

    public void disableCheckForScreen() {
        checkValidForScreen = false;
    }

    @Override
    public void onClickSettings() {
        // делаем проверку на корректность версий стоплистов и НСИ, чтобы быть
        // уверенным, что пользователь может перейти на главное окно.
        if (
                Di.INSTANCE.nsiVersionManager().checkCurrentVersionIdValid()
                        && Di.INSTANCE.nsiDataContractsVersionChecker().isDataContractVersionValid()
                        && Di.INSTANCE.securityDataContractsVersionChecker().isDataContractVersionValid()
                        && Dagger.appComponent().updateEventRepository().isStopListVersionValid(di().getPrivateSettings().get().getStopListValidTime())
        ) {
            if (di().getShiftManager().isShiftOpenedWithTestPd()) {
                Logger.info(TAG, "onClickSettings() Нажали на хардварную кнопку SETTINGS");
                Navigator.navigateToMenuActivity(this);
            }
        }
    }

    public boolean isAlreadyRead() {
        Logger.trace(TAG, "isAlreadyRead(" + isAlreadyRead + ")");
        return this.isAlreadyRead;
    }

    public void setAlreadyRead(boolean alreadyRead) {
        Logger.trace(TAG, "setAlreadyRead(" + alreadyRead + ")");
        this.isAlreadyRead = alreadyRead;
    }

    /**
     * Запускает считывания билета с типа носителя readerType
     *
     * @param readerType            тип носителя, откуда необходимо считать ПД
     * @param readForTransferParams Данные для оформления трансфера по считанному ПД
     */
    // В будущем в дальнейшем придумать другой способ, флажки - самое быстрое, но не самое красивое решение
    public void startReadPd(ReaderType readerType, ReadForTransferParams readForTransferParams) {
        if (BuildConfig.DEBUG) {
            if (!isAlreadyRead()) {
                setAlreadyRead(true);
                this._readerType = readerType;
                if (_readerType == ReaderType.TYPE_BSC) {
                    Logger.info(TAG, "startReadPd() - запускаем " + ControlReadBscActivity.class.getSimpleName() + " " + ReaderType.TYPE_BSC.toString());
                    ControlReadBscParams controlReadBscParams = new ControlReadBscParams();
                    controlReadBscParams.setReadForTransferParams(readForTransferParams);
                    controlReadBscParams.setIncrementPmHwUsageCounter(readForTransferParams == null);
                    Navigator.navigateToControlReadBscActivity(this, controlReadBscParams);
                } else {
                    Logger.info(TAG, "startReadPd() - запускаем " + ControlReadBarcodeActivity.class.getSimpleName() + " " + ReaderType.TYPE_BARCODE.toString());
                    Navigator.navigateToControlReadBarcodeActivity(this, readForTransferParams);
                }
            }
        } else {
            if (readForTransferParams == null && (readerType == ReaderType.TYPE_BARCODE && !isEnabledControlBarcode()))
                Logger.warning(TAG, "Недоступен контроль ШК для текущей роли");
            else if (readForTransferParams == null && (readerType == ReaderType.TYPE_BSC && !isEnabledControlBsc()))
                Logger.warning(TAG, "Недоступен контроль БСК для текущей роли");
            else if (!isAlreadyRead()) {
                setAlreadyRead(true);
                this._readerType = readerType;

                if (_readerType == ReaderType.TYPE_BSC) {
                    Logger.info(TAG, "startReadPd() - запускаем " + ControlReadBscActivity.class.getSimpleName() + " " + ReaderType.TYPE_BSC.toString());
                    ControlReadBscParams controlReadBscParams = new ControlReadBscParams();
                    controlReadBscParams.setReadForTransferParams(readForTransferParams);
                    controlReadBscParams.setIncrementPmHwUsageCounter(readForTransferParams == null);
                    Navigator.navigateToControlReadBscActivity(this, controlReadBscParams);
                } else {
                    Logger.info(TAG, "startReadPd() - запускаем " + ControlReadBarcodeActivity.class.getSimpleName() + " " + ReaderType.TYPE_BARCODE.toString());
                    Navigator.navigateToControlReadBarcodeActivity(this, readForTransferParams);
                }
            }
        }
    }

    /**
     * Вернет разрешение на контроль БСК
     *
     * @return
     */
    protected boolean isEnabledControlBsc() {
        RoleDvc role = di().getUserSessionInfo().getCurrentUser().getRole();
        return getSecurityDaoSession().getRolePermissionDvcDao().isPermissionEnabled(role, PermissionDvc.ReedBskNfc);
    }

    /**
     * Вернет разрешение на контроль ШК
     *
     * @return
     */
    protected boolean isEnabledControlBarcode() {
        RoleDvc role = di().getUserSessionInfo().getCurrentUser().getRole();
        return getSecurityDaoSession().getRolePermissionDvcDao().isPermissionEnabled(role, PermissionDvc.ReedPdBarcode);
    }

    /**
     * Показывает собщение вида "Функция недоступна для пользователя "Кассир"
     */
    public void makeErrorAccessToast() {
        Globals.getInstance().getToaster().showToast(getResources().getString(R.string.accessFunctionError) + " \"" +
                Di.INSTANCE.getUserSessionInfo().getCurrentUser().getRole().getName()
                + "\"");
    }

    /**
     * Сбрасывает флаг регистрации ресиверов, для принятия сообщений о закрытии
     * смены
     */
    public void resetRegisterReceiver() {
        registerForShiftAlarm = false;
    }

    private void showAttentionCloseShiftDialog() {
        Logger.info(TAG, "showAttentionCloseShiftDialog()");

        final ShiftEvent lastShiftEvent = ShiftManager.getInstance().getCurrentShiftEvent();

        if (lastShiftEvent == null) {
            throw new IllegalArgumentException("Shift is null, but alarm manager for close shift was fired");
        }

        final Calendar shiftCloseTimeLocal = Calendar.getInstance();
        shiftCloseTimeLocal.setTime(lastShiftEvent.getStartTime());
        shiftCloseTimeLocal.add(Calendar.HOUR_OF_DAY, 24);

        final Calendar currentTime = Calendar.getInstance();

        // вычислим реальное количество минут, которое осталось до закрытия смены
        long diff = shiftCloseTimeLocal.getTimeInMillis() - currentTime.getTimeInMillis();
        int minute = (int) (diff / (1000 * 60));

        // прибавляем 1 минуту, т.к. при делении результат округлится в меньшею сторону
        SimpleDialog simpleDialog = SimpleDialog.newInstance(getString(R.string.attention),
                String.format(getString(R.string.attention_close_shit), minute + 1),
                getString(R.string.Yes),
                getString(R.string.No),
                LinearLayout.HORIZONTAL,
                0);

        simpleDialog.setCancelable(false);
        simpleDialog.setDialogPositiveBtnClickListener(attentionDialogPositiveListener);
        simpleDialog.setDialogNegativeBtnClickListener(attentionDialogNegativeListener);
        simpleDialog.show(getFragmentManager(), DIALOG_ATTENTION_CLOSE_SHIFT);
    }

    private SimpleDialog.DialogBtnClickListener attentionDialogPositiveListener = (dialog, dialogId) -> {
        Logger.info(TAG, "showAttentionCloseShiftDialog() -> positive button clicked");
        // закрываем смену
        closeShift();
    };

    private SimpleDialog.DialogBtnClickListener attentionDialogNegativeListener = (dialog, dialogId) -> {
        Logger.info(TAG, "showAttentionCloseShiftDialog() -> negative button clicked");
    };

    /**
     * Инициирует закрытие смены
     */
    private void closeShift() {
        Logger.info(TAG, "closeShift() Запускаем Автоматическое закрытие смены");
        Navigator.navigateToCloseShiftActivity(this, true, true);
    }

    protected LocalDaoSession getLocalDaoSession() {
        return Globals.getInstance().getLocalDaoSession();
    }

    protected NsiDaoSession getNsiDaoSession() {
        return Globals.getInstance().getNsiDaoSession();
    }

    protected SecurityDaoSession getSecurityDaoSession() {
        return Globals.getInstance().getSecurityDaoSession();
    }

    protected void hideKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    //<editor-fold desc="Screen lock">
    public void denyScreenLock() {
        screenLockAllowed = false;
    }

    public void allowScreenLock() {
        screenLockAllowed = true;
        updateLastActionTimestamp();
    }

    protected void updateLastActionTimestamp() {
        screenLockManager.updateLastActionTimestamp();
    }

    protected boolean isScreenShouldBeLocked() {
        return screenLockManager.isScreenShouldBeLocked();
    }
    //</editor-fold>

    private BatteryManager.BatteryStateListener batteryStateListener = new BatteryManager.BatteryStateListener() {
        @Override
        public void onChargeLevelChanged(int chargeLevel) {
            batteryLevelView.setChargeLevel(chargeLevel);
        }

        @Override
        public void onPowerConnectedStateChanged(boolean powerConnected) {
            batteryLevelView.setPowerConnectedState(powerConnected);
        }
    };

    private ScreenLockManager.ScreenLockListener screenLockListener = screenShouldBeLocked -> {
        if (screenLockAllowed && screenShouldBeLocked) {
            Navigator.navigateToLockScreenActivity(this);
        }
    };

    private MobileSignalManager.Listener mobileSignalManagerListener = signalLevel -> {
        if (!privateSettingsHolder.get().isUseMobileDataEnabled()) {
            mobileDataSignalView.setVisibility(View.GONE);
        } else {
            mobileDataSignalView.setVisibility(View.VISIBLE);
            mobileDataSignalView.setCurrentSignalStrength(MobileDataSignalView.SignalStrength.fromSignalLevel(signalLevel));
        }
    };

}
