package ru.ppr.cppk.debug;

import android.app.AlertDialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.common.base.Preconditions;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import ru.ppr.core.domain.model.BarcodeType;
import ru.ppr.core.domain.model.EdsType;
import ru.ppr.cppk.BuildConfig;
import ru.ppr.cppk.GlobalConstants;
import ru.ppr.cppk.Globals;
import ru.ppr.cppk.Holder;
import ru.ppr.cppk.PathsConstants;
import ru.ppr.cppk.R;
import ru.ppr.cppk.db.DaoSession;
import ru.ppr.cppk.db.LocalDaoSession;
import ru.ppr.cppk.debug.batterytest.ActivityBatteryTest;
import ru.ppr.cppk.debug.batterytest.core.TasksConfig;
import ru.ppr.cppk.di.Dagger;
import ru.ppr.cppk.di.Di;
import ru.ppr.cppk.dialogs.CppkDialogFragment.CppkDialogClickListener;
import ru.ppr.cppk.entity.event.base34.Event;
import ru.ppr.cppk.entity.event.base34.TestTicketEvent;
import ru.ppr.cppk.entity.event.model.Check;
import ru.ppr.cppk.entity.settings.LocalUser;
import ru.ppr.cppk.entity.settings.PrivateSettings;
import ru.ppr.cppk.legacy.SamSlot;
import ru.ppr.cppk.localdb.model.AuditTrailEvent;
import ru.ppr.cppk.localdb.model.AuditTrailEventType;
import ru.ppr.cppk.localdb.model.ShiftEvent;
import ru.ppr.cppk.localdb.model.TicketTapeEvent;
import ru.ppr.cppk.localdb.model.UpdateEventType;
import ru.ppr.cppk.logic.DocumentTestPd;
import ru.ppr.cppk.logic.ShiftManager;
import ru.ppr.cppk.logic.builder.AuditTrailEventBuilder;
import ru.ppr.cppk.logic.builder.CheckBuilder;
import ru.ppr.cppk.managers.PrinterManager;
import ru.ppr.cppk.pos.PosType;
import ru.ppr.cppk.settings.SharedPreferencesUtils;
import ru.ppr.cppk.systembar.FeedbackProgressDialog;
import ru.ppr.cppk.systembar.LoggedActivity;
import ru.ppr.cppk.ui.activity.ActivityTestTerminal;
import ru.ppr.cppk.ui.activity.base.Navigator;
import ru.ppr.cppk.ui.activity.commonSettingsManagement.CommonSettingsManagementActivity;
import ru.ppr.cppk.ui.activity.coupon.CouponTestActivity;
import ru.ppr.cppk.ui.activity.nsiQueryTest.NsiQueryTestActivity;
import ru.ppr.cppk.ui.activity.privateSettingsManagement.PrivateSettingsManagementActivity;
import ru.ppr.cppk.ui.activity.sqlitetransactiontest.SqliteTransactionTestActivity;
import ru.ppr.cppk.utils.UpdateDb;
import ru.ppr.cppkupdater.RootUtils;
import ru.ppr.ipos.IPos;
import ru.ppr.logger.Logger;
import ru.ppr.security.entity.RoleDvc;
import ru.ppr.utils.FileUtils;
import rx.Completable;
import rx.CompletableSubscriber;
import rx.Subscription;

public class Debug extends LoggedActivity implements OnClickListener, CppkDialogClickListener {

    private static final String TAG = Logger.makeLogTag(Debug.class);

    /**
     * В этом классе важно получать {@link DaoSession} через {@link Globals}, т.к. здесь мы можем
     * обновить БД через кнопки, после чего в {@link Globals} должен обновится
     * объект {@link DaoSession}. Если после обновления где то останется старый объект
     * {@link DaoSession} то он будет кидать исключения, т.к. будет пытаться обратиться к
     * закрытой БД
     */


    private static final String EXTRA_START_SPLASH_ON_BACK = "EXTRA_START_SPLASH_ON_BACK";

    public static Intent getCallingIntent(Context context, boolean startSplashOnBack) {
        return new Intent(context, Debug.class)
                .putExtra(EXTRA_START_SPLASH_ON_BACK, startSplashOnBack);
    }

    private Globals g;
    private boolean startSplashOnBack;

    private Spinner mSpSamNb = null;
    private CheckBox useSAMModule = null;

    private Button posTerminalTestButton;
    private RadioGroup posTerminalGroup;

    private Holder<PrivateSettings> privateSettingsHolder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        privateSettingsHolder = Globals.getInstance().getPrivateSettingsHolder();

        setContentView(R.layout.debug);
        g = (Globals) getApplication();

        startSplashOnBack = getIntent().getBooleanExtra(EXTRA_START_SPLASH_ON_BACK, false);

        Button changeShiftStateBtn = (Button) findViewById(R.id.changeShiftState);

        findViewById(R.id.logoutEmergensyModeBtn).setOnClickListener(this);
        findViewById(R.id.syncControlBtn).setOnClickListener(this);
        findViewById(R.id.backupAndRestoreBtn).setOnClickListener(this);
        findViewById(R.id.startTestSft).setOnClickListener(this);
        findViewById(R.id.rfid2).setOnClickListener(this);
        findViewById(R.id.rfid1).setOnClickListener(this);
        findViewById(R.id.startTestPrinter).setOnClickListener(this);
        findViewById(R.id.start_wifi_configurator).setOnClickListener(this);
        findViewById(R.id.startSystemSettings).setOnClickListener(this);
        findViewById(R.id.enableSystemBar).setOnClickListener(this);
        findViewById(R.id.disableSystemBar).setOnClickListener(this);
        findViewById(R.id.testBarcode).setOnClickListener(this);
        findViewById(R.id.changeWorkMode).setOnClickListener(this);
        findViewById(R.id.addSyncFinishedEvent).setOnClickListener(this);
        findViewById(R.id.start_activity_test_battery).setOnClickListener(this);
        findViewById(R.id.startTppdCouponTest).setOnClickListener(this);
        findViewById(R.id.startNsiQueryTest).setOnClickListener(this);
        findViewById(R.id.startSqliteTransactionTest).setOnClickListener(this);
        findViewById(R.id.manage_common_settings).setOnClickListener(this);
        findViewById(R.id.manage_private_settings).setOnClickListener(this);
        findPosViews();

        changeShiftStateBtn.setOnClickListener(this);
        Button logoutBtn = (Button) findViewById(R.id.logoutBtn);
        logoutBtn.setVisibility(View.GONE);

        CheckBox enableToast = (CheckBox) findViewById(R.id.enableErrorToast);
        enableToast.setChecked(SharedPreferencesUtils.isErrorToastsEnabled(getApplicationContext()));
        enableToast.setOnCheckedChangeListener((buttonView, isChecked) ->
                SharedPreferencesUtils.setErrorToastsEnable(getApplicationContext(), isChecked));

        mSpSamNb = (Spinner) findViewById(R.id.spSamNb);
        useSAMModule = (CheckBox) findViewById(R.id.useSAMModuleCheck);
        configSamBlock();

        Spinner ecpTypeSpinner = (Spinner) findViewById(R.id.ecpType);
        EcpTypeAdapter ecpTypeAdapter = new EcpTypeAdapter(new ArrayList<>(Arrays.asList(EdsType.values())));

        ecpTypeSpinner.setAdapter(ecpTypeAdapter);
        int selectedPosition = ecpTypeAdapter.getPosition(SharedPreferencesUtils.getEdsType(this));
        ecpTypeSpinner.setSelection(selectedPosition);
        ecpTypeSpinner.setOnItemSelectedListener(ecpItemSelectedListener);

        CheckBox useEmergencyMode = (CheckBox) findViewById(R.id.useEmergencyModeCheck);
        useEmergencyMode.setChecked(SharedPreferencesUtils.isEmergencyModeUseEnable(getApplicationContext()));
        useEmergencyMode.setOnCheckedChangeListener((buttonView, isChecked) ->
                SharedPreferencesUtils.setEmergencyModeUseEnable(getApplicationContext(), isChecked));

        RadioGroup usePrinterGroup = (RadioGroup) findViewById(R.id.usePrinterGroup);
        int printerMode = Di.INSTANCE.printerManager().getPrinterMode();
        switch (printerMode) {
            case PrinterManager.PRINTER_MODE_FILE: {
                usePrinterGroup.check(R.id.usePrinterFilePrinter);
                break;
            }
            case PrinterManager.PRINTER_MODE_MOEBIUS_REAL: {
                usePrinterGroup.check(R.id.usePrinterRealNewSDK);
                break;
            }
            case PrinterManager.PRINTER_MODE_MOEBIUS_VIRTUAL_EKLZ:
                usePrinterGroup.check(R.id.usePrinterRealVirtualEklz);
                break;
            case PrinterManager.PRINTER_MODE_SHTRIH:
                usePrinterGroup.check(R.id.usePrinterShtrih);
                break;
        }

        if (ShiftManager.getInstance().isShiftClosed()) {
            usePrinterGroup.setOnCheckedChangeListener((group, checkedId) -> {
                switch (checkedId) {
                    case R.id.usePrinterFilePrinter:
                        Di.INSTANCE.printerManager().setPrinterMode(PrinterManager.PRINTER_MODE_FILE);
                        break;
                    case R.id.usePrinterRealNewSDK:
                        Di.INSTANCE.printerManager().setPrinterMode(PrinterManager.PRINTER_MODE_MOEBIUS_REAL);
                        break;
                    case R.id.usePrinterRealVirtualEklz:
                        Di.INSTANCE.printerManager().setPrinterMode(PrinterManager.PRINTER_MODE_MOEBIUS_VIRTUAL_EKLZ);
                        break;
                    case R.id.usePrinterShtrih:
                        Di.INSTANCE.printerManager().setPrinterMode(PrinterManager.PRINTER_MODE_SHTRIH);
                        break;
                }

                Di.INSTANCE.printerManager().updatePrinter();
            });
        } else {
            //если смена открыта, то не даем сменить принтер
            final int childCount = usePrinterGroup.getChildCount();
            for (int i = 0; i < childCount; i++) {
                usePrinterGroup.getChildAt(i).setEnabled(false);
            }
        }

        CheckBox cardReadTimeToast = (CheckBox) findViewById(R.id.cardReadTimeToast);
        cardReadTimeToast.setChecked(SharedPreferencesUtils.getCardReadTimeToast(getApplicationContext()));
        cardReadTimeToast.setOnCheckedChangeListener((buttonView, isChecked) ->
                SharedPreferencesUtils.setСardReadTimeToast(getApplicationContext(), isChecked));

        Spinner spinner = (Spinner) findViewById(R.id.barcodeType);
        BarcodeTypeAdapter adapter = new BarcodeTypeAdapter();
        adapter.setItems(new ArrayList<>(Arrays.asList(BarcodeType.values())));
        spinner.setAdapter(adapter);
        spinner.setSelection(adapter.getPosition(SharedPreferencesUtils.getBarcodeType(this)));
        spinner.setOnItemSelectedListener(barcodeSelectedListener);
    }

    private void configSamBlock() {
        Logger.trace(TAG, "configSamBlock");
        boolean useSam = SharedPreferencesUtils.isSamModuleUseEnable(getApplicationContext());
        mSpSamNb.setEnabled(useSam);
        mSpSamNb.setSelection(SharedPreferencesUtils.getSamModuleSlot(getApplicationContext()).getRealNumber() - 1);
        useSAMModule.setChecked(useSam);

        useSAMModule.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SharedPreferencesUtils.setSamModuleUseEnable(getApplicationContext(), isChecked);
            configSamBlock();
        });
        mSpSamNb.setOnItemSelectedListener(new OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                SharedPreferencesUtils.setSamModuleSlot(getApplicationContext(), SamSlot.getByRealNumber(position + 1));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        setupSpinners();
        boolean enablePosViewsRadioGroup = ShiftManager.getInstance().isShiftClosed() && privateSettingsHolder.get().isPosEnabled();
        enablePosViews(enablePosViewsRadioGroup);
    }

    @Override
    public void onClick(View v) {
        Logger.trace(TAG, "onClick");
        switch (v.getId()) {

            case R.id.syncControlBtn:
                Logger.trace(TAG, "case R.id.syncControlBtn");
                startActivity(new Intent(Debug.this, DebugSyncActivity.class));
                break;

            case R.id.backupAndRestoreBtn:
                Logger.trace(TAG, "case R.id.backupAndRestoreBtn");
                startActivity(new Intent(Debug.this, DebugBackupActivity.class));
                break;

            case R.id.startTestSft:
                Logger.trace(TAG, "case R.id.startTestSft");
                startActivity(new Intent(Debug.this, SftTestActivity.class));
                break;

            case R.id.startTestPrinter:
                Logger.trace(TAG, "case R.id.startTestPrinter");
                startActivity(new Intent(Debug.this, PrinterDebugActivity.class));
                break;

            case R.id.start_wifi_configurator:
                Logger.trace(TAG, "case R.id.start_wifi_configurator");
                startWiFiConfigurator();
                break;

            case R.id.startSystemSettings:
                Logger.trace(TAG, "case R.id.startSystemSettings");
                startActivity(new Intent(Settings.ACTION_SETTINGS));
                break;

            case R.id.enableSystemBar:
                Logger.trace(TAG, "case R.id.enableSystemBar");
                configStatusBar(true);
                break;

            case R.id.disableSystemBar:
                Logger.trace(TAG, "case R.id.disableSystemBar");
                configStatusBar(false);
                break;

            case R.id.rfid2:
                Logger.trace(TAG, "case R.id.rfid2");
                startActivity(new Intent(getApplicationContext(), DebugRfid2Activity.class));
                break;

            case R.id.rfid1:
                Logger.trace(TAG, "case R.id.rfid1");
                startActivity(new Intent(Debug.this, DebugRfid1Activity.class));
                break;

            case R.id.changeShiftState:
                Logger.trace(TAG, "case R.id.changeShiftState");
                changeShiftState();
                break;

            case R.id.deleteApp:
                Logger.trace(TAG, "case R.id.deleteApp");
                startActivity(new Intent(Intent.ACTION_DELETE, Uri.fromParts("package", g.getPackageName(), null)));
                break;

            case R.id.startFileManager:
                Logger.trace(TAG, "case R.id.startFileManager");
                startFileManager();
                break;
            case R.id.startCpcHdkCone:
                Logger.trace(TAG, "case R.id.startCpcHdkCone");
                startApkFromAssetsInstall(GlobalConstants.CpcHdkConeSampleApkName);
                break;

            case R.id.printAuditTrail:
                Logger.trace(TAG, "case R.id.printAuditTrail");
                //new ReportAuditTrail().print(null);
                break;
            case R.id.printTestShiftSheet:
                Logger.trace(TAG, "case R.id.printTestShiftSheet");
                //new ReportShiftOrMonthlySheet().setSheetTypeTestAtShiftMiddle().print(null);
                break;
            case R.id.printShiftSheet:
                Logger.trace(TAG, "case R.id.printShiftSheet");
                //new ReportShiftOrMonthlySheet().setSheetTypeShift().print(null);
                break;

            case R.id.printMonthlySheet:
                Logger.trace(TAG, "case R.id.printMonthlySheet");
                //new ReportShiftOrMonthlySheet().setSheetTypeMonth().print(null);
                break;

            case R.id.testBarcode:
                Logger.trace(TAG, "case R.id.testBarcode");
                startActivity(new Intent(Debug.this, BarcodeTestActivity.class));
                break;

            case R.id.addTestPDtoDB: {
                Logger.trace(TAG, "case R.id.addTestPDtoDB");

                LocalDaoSession localDaoSession = Dagger.appComponent().localDaoSession();
                if (!ShiftManager.getInstance().isShiftOpened()) {
                    Dagger.appComponent().toaster().showToast("Смена закрыта");
                    return;
                }

                TicketTapeEvent ticketTapeEvent = localDaoSession.getTicketTapeEventDao().getInstalledTicketTape();

                if (ticketTapeEvent == null || ticketTapeEvent.getEndTime() != null) {
                    Dagger.appComponent().toaster().showToast("Установите билетную ленту!");
                    return;
                }

                DocumentTestPd documentTestPd = Dagger.appComponent().testPdSaleDocumentFactory().create();
                documentTestPd.createTestSaleEvent();
                documentTestPd.updateStatusPrePrinting();
                documentTestPd.updateStatusPrinted();
                documentTestPd.updateStatusCompleted();

                Dagger.appComponent().toaster().showToast("Пробный № " + documentTestPd.getPdNumber());
                break;
            }

            case R.id.changeWorkMode:
                Logger.trace(TAG, "case R.id.changeWorkMode");
                break;

            case R.id.logoutBtn:
                Logger.trace(TAG, "case R.id.logoutBtn");
                logout(false);
                break;

            case R.id.logoutEmergensyModeBtn:
                Logger.trace(TAG, "case R.id.logoutEmergensyModeBtn");
                logout(true);
                break;

            case R.id.addSyncFinishedEvent:
                Logger.trace(TAG, "case R.id.addSyncFinishedEvent");
                addSyncFinishedEvent();
                break;

            case R.id.start_activity_test_battery:
                Logger.trace(TAG, "case R.id.start_activity_test_battery");
                startActivity(ActivityBatteryTest.getCallingIntent(this, new TasksConfig.Builder().build()));
                break;

            case R.id.startTppdCouponTest:
                Logger.trace(TAG, "case R.id.startTppdCouponTest");
                startActivity(new Intent(Debug.this, CouponTestActivity.class));
                break;

            case R.id.startNsiQueryTest:
                Logger.trace(TAG, "case R.id.startNsiQueryTest");
                startActivity(new Intent(Debug.this, NsiQueryTestActivity.class));
                break;

            case R.id.startSqliteTransactionTest:
                Logger.trace(TAG, "case R.id.startSqliteTransactionTest");
                startActivity(SqliteTransactionTestActivity.getCallingIntent(Debug.this));
                break;

            case R.id.manage_common_settings:
                Logger.trace(TAG, "case R.id.manage_common_settings");
                startActivity(new Intent(Debug.this, CommonSettingsManagementActivity.class));
                break;

            case R.id.manage_private_settings:
                Logger.trace(TAG, "case R.id.manage_private_settings");
                startActivity(new Intent(Debug.this, PrivateSettingsManagementActivity.class));
                break;

            default:
                break;
        }

    }


    //Добавит событие успешной синхронизации, чтобы можно было открыть смену на ПТК
    private void addSyncFinishedEvent() {
        Logger.trace(TAG, "addSyncFinishedEvent");
        Dagger.appComponent().updateEventCreator().setType(UpdateEventType.ALL).create();
        //добавим событие обновления НСИ, чтобы можно было авторизоваться
        Dagger.appComponent().updateEventCreator().setType(UpdateEventType.NSI).create();
        //добавим событие обновления Базы безопасности, чтобы можно было авторизоваться
        Dagger.appComponent().updateEventCreator().setType(UpdateEventType.SECURITY).create();
    }

    private void startTestTerminal() {
        Logger.trace(TAG, "startTestTerminal");
        startActivity(new Intent(this, ActivityTestTerminal.class));
    }

    private void startFileManager() {
        Logger.trace(TAG, "startFileManager");
        if (isPackageInstalled(GlobalConstants.FILE_MANAGER_PACKAGE, getApplicationContext())) {
            Intent LaunchIntent = getPackageManager().getLaunchIntentForPackage(GlobalConstants.FILE_MANAGER_PACKAGE);
            startActivity(LaunchIntent);
        } else {
            startApkFromAssetsInstall(GlobalConstants.FILE_MANAGER_APK_NAME);
        }
    }

    private boolean isPackageInstalled(String packagename, Context context) {
        Logger.trace(TAG, "isPackageInstalled");
        PackageManager pm = context.getPackageManager();
        try {
            pm.getPackageInfo(packagename, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (NameNotFoundException e) {
            return false;
        }
    }

    private void logout(boolean emergensyMode) {
        Logger.trace(TAG, "logout()");
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setPositiveButton(getResources().getString(R.string.Yes), (dialog, which) -> {
            Navigator.navigateToSplashActivity(Debug.this, emergensyMode);
        });
        alertDialogBuilder.setNegativeButton(getResources().getString(R.string.No), null);
        alertDialogBuilder.setMessage((emergensyMode) ? R.string.really_exit_to_emergensy_mode : R.string.really_exit);
        alertDialogBuilder.setTitle(R.string.Exit);
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    private void configStatusBar(boolean enable) {
        Logger.trace(TAG, "configStatusBar");
        boolean res = (enable) ? RootUtils.enableSystemBar() : RootUtils.disableSystemBar();
        String toast = getResources().getString(R.string.toast_need_root);
        if (res)
            toast = getResources().getString((enable) ? R.string.system_bar_enabled : R.string.system_bar_disabled);
        Globals.getInstance().getToaster().showToast(toast);
    }

    private void startWiFiConfigurator() {
        Logger.trace(TAG, "startWiFiConfigurator");
        startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
    }

    private void changeShiftState() {
        Logger.trace(TAG, "changeShiftState()");
        Intent intent = new Intent(this, ShiftManagerActivity.class);
        startActivity(intent);
    }

    /**
     * Копирует дистрибутив в доступное место и запускает установку
     *
     * @return
     */
    private void startApkFromAssetsInstall(String apkFileName) {
        Logger.trace(TAG, "startApkFromAssetsInstall(" + apkFileName + ")");
        String tcFile = "/data/data/" + getPackageName() + "/" + apkFileName;
        File dst = new File(PathsConstants.TEMP + "/" + apkFileName);

        if (!(new File(tcFile).exists())) {
            UpdateDb.copyFileOrDirFromAssets(g, apkFileName);
        }

        if (!dst.exists()) {
            FileUtils.copy(g, new File(tcFile), dst);
        }
        startActivity(new Intent(Intent.ACTION_VIEW).setDataAndType(Uri.parse("file:///" + dst.getAbsolutePath()), "application/vnd.android.package-archive"));
    }

    /**
     * Настраивает спиннер, и устанавливает текущий элемент в соответствии с
     * настройками
     */
    private void setupSpinners() {
        Logger.trace(TAG, "setupSpinners()");

        // настраиваем спиннер выбора скорости работы сканера ШК
        SharedPreferences preferences = getSharedPreferences(GlobalConstants.SHARED_PREFERENCE, Context.MODE_PRIVATE);

        Spinner spinnerSpeed = (Spinner) findViewById(R.id.spBaudrate);
        ArrayAdapter<CharSequence> adapterBaudrate = ArrayAdapter.createFromResource(this, R.array.baudrate, android.R.layout.simple_spinner_item);
        adapterBaudrate.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSpeed.setAdapter(adapterBaudrate);

        String currentBaudrate = preferences.getString(GlobalConstants.OPTICON_BAUDRATE_SETTING_NAME, GlobalConstants.OPTICON_BAUDRATE_DEFAULT);
        int currentPosition = adapterBaudrate.getPosition(currentBaudrate);
        spinnerSpeed.setSelection(currentPosition);

        spinnerSpeed.setOnItemSelectedListener(new SpinnerListener());

        // настраиваем спиннер выбора роли
        Spinner spinnerRoles = (Spinner) findViewById(R.id.spRole);
        ArrayList<String> roleStrings = new ArrayList<String>();
        final ArrayList<RoleDvc> all = Di.INSTANCE.getDbManager().getSecurityDaoSession().get().getRoleDvcDao().getRoleDvcList();
        all.add(RoleDvc.getRootRole());
        for (RoleDvc ur : all) {
            roleStrings.add(ur.getName());
        }

        // selected item will look like a spinner set from XML
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, roleStrings);
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRoles.setAdapter(spinnerArrayAdapter);

        RoleDvc roleDvc = Di.INSTANCE.getUserSessionInfo().getCurrentUser().getRole();
        int currentPositionRole = spinnerArrayAdapter.getPosition(roleDvc.getName());
        spinnerRoles.setSelection(currentPositionRole);
        spinnerRoles.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                LocalUser user = Di.INSTANCE.getUserSessionInfo().getCurrentUser();
                user.setRole(all.get(position));
                Di.INSTANCE.getUserSessionInfo().setCurrentUser(user);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    /**
     * Класс, обрабатывающий изменение текущего элемента в спинере
     *
     * @author Владелец
     */
    private class SpinnerListener implements OnItemSelectedListener {

        SharedPreferences preferences = getSharedPreferences(GlobalConstants.SHARED_PREFERENCE, Context.MODE_PRIVATE);

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            Logger.trace(TAG, "SpinnerListener - onItemSelected");
            String selectedItem = parent.getItemAtPosition(position).toString();
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString(GlobalConstants.OPTICON_BAUDRATE_SETTING_NAME, selectedItem);
            editor.apply();
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) { /* NOP */ }
    }

    private OnItemSelectedListener ecpItemSelectedListener = new OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            Logger.trace(TAG, "ecpItemSelectedListener - onItemSelected");
            EdsType currentType = SharedPreferencesUtils.getEdsType(getApplicationContext());
            EdsType newType = (EdsType) parent.getAdapter().getItem(position);
            if (!currentType.equals(newType)) {
                setupNewEcpType(newType);
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) { /* NOP */ }
    };

    private OnItemSelectedListener barcodeSelectedListener = new OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            BarcodeType currentType = SharedPreferencesUtils.getBarcodeType(getApplicationContext());
            BarcodeType newType = (BarcodeType) parent.getAdapter().getItem(position);
            if (!currentType.equals(newType)) {
                SharedPreferencesUtils.setBarcodeType(getApplicationContext(), newType);
                Dagger.appComponent().barcodeManager().updateBarcode(newType);
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    };

    /**
     * Метод для инициализации ЭЦП.
     *
     * @param edsType тип ЭЦП.
     */
    private void setupNewEcpType(@NonNull final EdsType edsType) {
        Logger.trace(TAG, "setupNewEdsType " + edsType);
        final FeedbackProgressDialog progressDialog = new FeedbackProgressDialog(this);
        progressDialog.setMessage(String.format(Locale.getDefault(), "Инициализация EDS. Новый тип: %s - %s", edsType, edsType.name()));
        progressDialog.setCancelable(false);
        progressDialog.show();

        Completable
                .fromAction(() -> {
                    SharedPreferencesUtils.setEdsType(getApplicationContext(), edsType);
                    Dagger.appComponent().edsManagerConfigSyncronizer().sync();
                })
                .subscribe(new CompletableSubscriber() {
                    @Override
                    public void onCompleted() {
                        progressDialog.dismiss();
                    }

                    @Override
                    public void onError(Throwable e) {
                        Logger.trace(TAG, e);
                        progressDialog.dismiss();
                    }

                    @Override
                    public void onSubscribe(Subscription d) {

                    }
                });
    }

    private class EcpTypeAdapter extends BaseAdapter {

        private final List<EdsType> ecpTypes;
        private final int size;

        public EcpTypeAdapter(List<EdsType> ecpTypes) {
            this.ecpTypes = ecpTypes;
            size = ecpTypes.size();
        }

        @Override
        public int getCount() {
            return size;
        }

        @Override
        public EdsType getItem(int position) {
            return ecpTypes.get(position);
        }

        @Override
        public long getItemId(int position) {
            return getItem(position).getCode();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            TextView textView;
            if (convertView != null) {
                textView = (TextView) convertView;
            } else {
                textView = (TextView) getLayoutInflater()
                        .inflate(android.R.layout.simple_spinner_dropdown_item, parent, false);
            }

            textView.setText(getItem(position).name());
            return textView;
        }

        public int getPosition(@NonNull final EdsType type) {
            int position = 0;
            for (EdsType currentType : ecpTypes) {
                if (currentType == type) {
                    return position;
                }
                position++;
            }
            return 0;
        }
    }

    private class BarcodeTypeAdapter extends ru.ppr.cppk.ui.adapter.base.BaseAdapter<BarcodeType> {

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TextView textView;
            if (convertView != null) {
                textView = (TextView) convertView;
            } else {
                textView = (TextView) getLayoutInflater()
                        .inflate(android.R.layout.simple_spinner_dropdown_item, parent, false);
            }

            textView.setText(getItem(position).name());
            return textView;
        }

        public int getPosition(BarcodeType type) {
            int position = 0;
            for (BarcodeType currentType : getItems()) {
                if (currentType.equals(type)) {
                    return position;
                }
                position++;
            }
            return 0;
        }
    }

    @Override
    public void onPositiveClick(DialogFragment dialog, int dialogId) {
        Logger.trace(TAG, "onPositiveClick");
    }

    @Override
    public void onNegativeClick(DialogFragment dialog, int dialogId) {
        Logger.trace(TAG, "onNegativeClick");
    }

    @Override
    public void onBackPressed() {
        if (startSplashOnBack) {
            Navigator.navigateToSplashActivity(this, false);
        } else {
            super.onBackPressed();
        }
    }

    private OnClickListener posTerminalTestClickListener = v -> {
        final IPos terminal = Globals.getPosTerminal();

        if (terminal != null) {
            if (terminal.isReady()) {
                startTestTerminal();
            } else {
                Globals.getInstance().getToaster().showToast(getString(R.string.terminal_is_busy));
            }
        } else {
            Globals.getInstance().getToaster().showToast("Банковский терминал не инициализирован.");
        }
    };

    private OnClickListener posTerminalChangeClickListener = v -> {
        final IPos terminal = Globals.getPosTerminal();

        if (terminal != null) {
            if (terminal.isReady()) {
                PosType posType = SharedPreferencesUtils.getPosTerminalType(Debug.this);

                switch (v.getId()) {
                    case R.id.use_simulator: {
                        posType = PosType.DEFAULT;
                    }
                    break;

                    case R.id.use_ingenico: {
                        posType = PosType.INGENICO;
                    }
                    break;

                    case R.id.use_inpas: {
                        posType = PosType.INPAS;
                    }
                    break;
                }

                SharedPreferencesUtils.setPosTerminalType(this, posType);
                Globals.updatePosTerminalWithoutCallback(this, SharedPreferencesUtils.getPosMacAddress(this), SharedPreferencesUtils.getPosPort(this), SharedPreferencesUtils.getPosTerminalType(this));
            } else {
                Globals.getInstance().getToaster().showToast(getString(R.string.terminal_is_busy));
            }
        } else {
            Globals.getInstance().getToaster().showToast("Банковский терминал не инициализирован.");
        }
    };

    private void findPosViews() {
        posTerminalTestButton = (Button) findViewById(R.id.test_terminal);
        posTerminalTestButton.setEnabled(Di.INSTANCE.getPrivateSettings().get().isPosEnabled());
        posTerminalTestButton.setOnClickListener(posTerminalTestClickListener);

        posTerminalGroup = (RadioGroup) findViewById(R.id.use_pos_terminal_group);

        RadioButton useSimulator = (RadioButton) findViewById(R.id.use_simulator);
        useSimulator.setVisibility(BuildConfig.DISABLE_STUB_POS ? View.GONE : View.VISIBLE);
        useSimulator.setOnClickListener(posTerminalChangeClickListener);

        RadioButton useIngenico = (RadioButton) findViewById(R.id.use_ingenico);
        useIngenico.setOnClickListener(posTerminalChangeClickListener);

        RadioButton useInpas = (RadioButton) findViewById(R.id.use_inpas);
        useInpas.setOnClickListener(posTerminalChangeClickListener);

        PosType posType = SharedPreferencesUtils.getPosTerminalType(Globals.getInstance());

        switch (posType) {
            case DEFAULT: {
                useSimulator.setChecked(true);
            }
            break;

            case INGENICO: {
                useIngenico.setChecked(true);
            }
            break;

            case INPAS: {
                useInpas.setChecked(true);
            }
            break;
        }
    }

    private void enablePosViews(final boolean enable) {
        for (int i = 0; i < posTerminalGroup.getChildCount(); i++) {
            final View view = posTerminalGroup.getChildAt(i);
            view.setEnabled(enable);
        }
    }

}
