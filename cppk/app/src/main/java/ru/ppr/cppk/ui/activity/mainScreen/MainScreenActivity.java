package ru.ppr.cppk.ui.activity.mainScreen;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import javax.inject.Inject;

import ru.ppr.cppk.BuildConfig;
import ru.ppr.cppk.R;
import ru.ppr.cppk.di.Dagger;
import ru.ppr.cppk.helpers.CommonSettingsStorage;
import ru.ppr.cppk.helpers.PrivateSettingsHolder;
import ru.ppr.cppk.helpers.TicketTapeRestChecker;
import ru.ppr.cppk.localdb.model.TicketWayType;
import ru.ppr.cppk.logic.PermissionChecker;
import ru.ppr.cppk.logic.PtkModeChecker;
import ru.ppr.cppk.logic.pd.checker.TransferRouteChecker;
import ru.ppr.cppk.managers.NsiVersionManager;
import ru.ppr.cppk.model.PdSaleParams;
import ru.ppr.cppk.pd.utils.reader.ReaderType;
import ru.ppr.cppk.service.ServiceTerminalMonitor;
import ru.ppr.cppk.settings.CommonMenuActivity;
import ru.ppr.cppk.systembar.SystemBarActivity;
import ru.ppr.cppk.ui.activity.base.ActivityModule;
import ru.ppr.cppk.ui.activity.base.Navigator;
import ru.ppr.cppk.ui.activity.pdSale.PdSaleActivity;
import ru.ppr.cppk.ui.dialog.SimpleDialog;
import ru.ppr.logger.Logger;
import ru.ppr.nsi.entity.Station;
import ru.ppr.nsi.entity.TicketCategory;
import ru.ppr.nsi.repository.StationRepository;
import ru.ppr.security.entity.PermissionDvc;

/**
 * Экран меню с 4-мя синими кнопками.
 */
public class MainScreenActivity extends SystemBarActivity {

    private static final String TAG = Logger.makeLogTag(MainScreenActivity.class);

    public static Intent getCallingIntent(Context context) {
        return new Intent(context, MainScreenActivity.class);
    }

    // region Di
    private MainScreenComponent component;
    @Inject
    PermissionChecker permissionChecker;
    @Inject
    PrivateSettingsHolder privateSettingsHolder;
    @Inject
    PtkModeChecker ptkModeChecker;
    @Inject
    TransferRouteChecker transferRouteChecker;
    @Inject
    StationRepository stationRepository;
    @Inject
    NsiVersionManager nsiVersionManager;
    @Inject
    TicketTapeRestChecker ticketTapeRestChecker;
    @Inject
    CommonSettingsStorage commonSettingsStorage;

    // endregion

    //region Views
    private Button readBscBtn;
    private Button readBarcodeBtn;
    private TextView saleLabel;
    private Button sellPdBtn;
    private Button sellBaggageBtn;
    private Button sellTransferBtn;
    private View sellSpace;
    private View transferOnlySpace;
    private Button menuBtn;
    private TextView transferDirectionLabel;
    //endregion

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        component = DaggerMainScreenComponent.builder().appComponent(Dagger.appComponent()).activityModule(new ActivityModule(this)).build();
        component.inject(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_screen);

        transferDirectionLabel = (TextView) findViewById(R.id.transferDirectionLabel);
        saleLabel = (TextView) findViewById(R.id.saleLabel);
        sellSpace = findViewById(R.id.sellSpace);
        transferOnlySpace = findViewById(R.id.transferOnlySpace);

        readBscBtn = (Button) findViewById(R.id.readBscBtn);
        readBscBtn.setOnClickListener(v -> {
            Logger.info(TAG, "Нажали на кнопку БСК");
            if (isActivityResumed())
                startReadPd(ReaderType.TYPE_BSC, null);
        });

        readBarcodeBtn = (Button) findViewById(R.id.readBarcodeBtn);
        readBarcodeBtn.setOnClickListener(v -> {
            Logger.info(TAG, "Нажали на кнопку ШК");
            if (isActivityResumed())
                startReadPd(ReaderType.TYPE_BARCODE, null);
        });

        sellPdBtn = (Button) findViewById(R.id.sellPdBtn);
        sellPdBtn.setOnClickListener(v -> {
            Logger.info(TAG, "Нажали на кнопку ПРОДАЖА");
            checkTicketTapeAndExecute(() -> startSellActivity((int) TicketCategory.Code.SINGLE));
        });

        sellBaggageBtn = (Button) findViewById(R.id.sellBaggageBtn);
        sellBaggageBtn.setOnClickListener(v -> {
            Logger.info(TAG, "Нажали на кнопку БАГАЖ");
            checkTicketTapeAndExecute(() -> startSellActivity((int) TicketCategory.Code.BAGGAGE));
        });

        sellTransferBtn = (Button) findViewById(R.id.sellTransferBtn);
        sellTransferBtn.setOnClickListener(v -> {
            Logger.info(TAG, "Нажали на кнопку Трансфер");
            checkTicketTapeAndExecute(() -> Navigator.navigateToTransferSaleStartActivity(MainScreenActivity.this));
        });

        menuBtn = (Button) findViewById(R.id.menuBtn);
        menuBtn.setOnClickListener(v -> {
            Logger.info(TAG, "Нажали на кнопку НАСТРОЙКИ");
            startActivity(new Intent(MainScreenActivity.this, CommonMenuActivity.class));
        });

        canUserHardwareButton();
        openOptionsMenu();

        if (!ServiceTerminalMonitor.isRunning())
            startService(new Intent(this, ServiceTerminalMonitor.class));
    }

    @Override
    protected void onPause() {
        if (ServiceTerminalMonitor.isRunning())
            stopService(new Intent(this, ServiceTerminalMonitor.class));

        super.onPause();
    }

    @Override
    protected void onResume() {
        configVisibility();
        configAccess();

        if (!ServiceTerminalMonitor.isRunning()) {
            startService(new Intent(this, ServiceTerminalMonitor.class));
        }

        super.onResume();
    }

    private void configVisibility() {
        // запрещаем или разрешаем показывать кнопки оформления ПД
        setSaleLayoutVisible(privateSettingsHolder.get().isSaleEnabled());
        // запрещаем или разрешаем показывать кнопки оформления трансфера
        setSaleTransferVisible(privateSettingsHolder.get().isSaleEnabled(), privateSettingsHolder.get().isTransferSaleEnabled());
        // настраиваем отображение станции отправления трансфера
        Station fromStation = null;
        if (ptkModeChecker.isTransferControlMode() && transferRouteChecker.checkTransferRouteStationValid()) {
            fromStation = stationRepository.load(privateSettingsHolder.get().getTransferRouteStationsCodes()[0], nsiVersionManager.getCurrentNsiVersionId());
        }
        transferDirectionLabel.setText(fromStation != null ? getString(R.string.main_screen_transfer_from, fromStation.getName()) : "");
        transferDirectionLabel.setVisibility(fromStation == null ? View.GONE : View.VISIBLE);
    }

    private void setSaleLayoutVisible(boolean visible) {
        saleLabel.setVisibility(visible ? View.VISIBLE : View.GONE);
        sellPdBtn.setVisibility(visible ? View.VISIBLE : View.GONE);
        sellBaggageBtn.setVisibility(visible ? View.VISIBLE : View.GONE);
        sellSpace.setVisibility(visible ? View.GONE : View.VISIBLE);
        transferOnlySpace.setVisibility(visible ? View.GONE : View.VISIBLE);
    }

    private void setSaleTransferVisible(boolean saleVisible, boolean transferVisible) {
        saleLabel.setVisibility(saleVisible || transferVisible ? View.VISIBLE : View.GONE);
        sellTransferBtn.setVisibility(transferVisible ? View.VISIBLE : View.GONE);
        sellSpace.setVisibility(saleVisible || transferVisible ? View.GONE : View.VISIBLE);
        transferOnlySpace.setVisibility(!saleVisible && transferVisible ? View.VISIBLE : View.GONE);
    }

    /**
     * Настраивает доступность функций для разный ролей пользователей
     */
    private void configAccess() {
        // запрещаем или показываем кнопку контроля ПД на БСК
        readBscBtn.setEnabled((BuildConfig.DEBUG) ? true: isEnabledControlBsc());
        // запрещаем или показываем кнопку контроля ПД на ШК
        readBarcodeBtn.setEnabled((BuildConfig.DEBUG) ? true: isEnabledControlBarcode());
        // запрещаем или показываем кнопку продажи ПД
        sellPdBtn.setEnabled((BuildConfig.DEBUG) ? true: permissionChecker.checkPermission(PermissionDvc.SalePd));
        // запрещаем или показываем кнопку Продажи багажа
        sellBaggageBtn.setEnabled((BuildConfig.DEBUG) ? true: permissionChecker.checkPermission(PermissionDvc.SaleBaggage));
        // запрещаем или показываем кнопку Продажи трансфера
        sellTransferBtn.setEnabled((BuildConfig.DEBUG) ? true: permissionChecker.checkPermission(PermissionDvc.SaleTransfer));

    }

    private void startSellActivity(int sellType) {
        Logger.debug(TAG, "startSellActivity() start");
        PdSaleParams pdSaleParams = new PdSaleParams();
        pdSaleParams.setTicketCategoryCode(sellType);
        pdSaleParams.setDirectionCode(TicketWayType.OneWay.getCode());
        startActivity(PdSaleActivity.getCallingIntent(this, pdSaleParams));
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_HOME || super.onKeyDown(keyCode, event);
    }

    /**
     * Переопределяем обработчик чтобы не запускать с этого активити самого себя
     */
    @Override
    public void onClickSettings() {
        /* NOP */
    }

    /**
     * Покажет диалог о необходимости печати отчета об окончании билетной ленты.
     *
     * @param action - действие, которое нужно выполнить в случае успеха
     */
    private void checkTicketTapeAndExecute(Runnable action) {

        Logger.trace(TAG, "checkTicketTapeAndExecute()");

        if (ticketTapeRestChecker.check()) {
            action.run();
            return;
        }

        SimpleDialog simpleDialog = SimpleDialog.newInstance(null,
                String.format(getString(R.string.main_screen_ask_ticketTape_attention_msg), commonSettingsStorage.get().getTicketTapeAttentionLength()),
                getString(R.string.main_screen_ask_ticketTape_attention_ok),
                getString(R.string.main_screen_ask_ticketTape_attention_no),
                LinearLayout.HORIZONTAL,
                0);
        simpleDialog.show(getFragmentManager(), SimpleDialog.FRAGMENT_TAG);
        simpleDialog.setDialogPositiveBtnClickListener((dialog, dialogId) -> {
            Navigator.navigateToAccountingTicketTapeEndActivity(this);
        });
        simpleDialog.setDialogNegativeBtnClickListener((dialog, dialogId) -> {
            action.run();
        });
        simpleDialog.setOnCancelListener(dialogInterface -> {
            action.run();
        });
    }
}
