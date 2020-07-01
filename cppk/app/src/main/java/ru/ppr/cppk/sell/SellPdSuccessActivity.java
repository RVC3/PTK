package ru.ppr.cppk.sell;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import ru.ppr.core.dataCarrier.pd.PdVersion;
import ru.ppr.cppk.R;
import ru.ppr.cppk.dataCarrier.entity.PD;
import ru.ppr.cppk.db.local.CppkTicketSaleDao;
import ru.ppr.cppk.di.Dagger;
import ru.ppr.cppk.entity.event.base34.CPPKTicketSales;
import ru.ppr.cppk.entity.event.base34.TicketEventBase;
import ru.ppr.cppk.entity.event.base34.TicketSaleReturnEventBase;
import ru.ppr.cppk.entity.event.model.Check;
import ru.ppr.cppk.helpers.DeferredActionHandler;
import ru.ppr.cppk.localdb.model.TicketWayType;
import ru.ppr.cppk.model.PdSaleParams;
import ru.ppr.cppk.model.PdSaleSuccessParams;
import ru.ppr.cppk.model.SaleType;
import ru.ppr.cppk.pd.utils.PdFragmentCreator;
import ru.ppr.cppk.pd.utils.ValidityPdVariants;
import ru.ppr.cppk.pd.utils.reader.ReadRfidData;
import ru.ppr.cppk.systembar.SystemBarActivity;
import ru.ppr.cppk.ui.activity.base.Navigator;
import ru.ppr.cppk.ui.fragment.pd.simple.SimplePdActivityLogic;
import ru.ppr.logger.Logger;
import ru.ppr.nsi.entity.TicketCategory;
import ru.ppr.security.entity.PermissionDvc;

/**
 * Экран с резултатом успешного оформления ПД.
 */
public class SellPdSuccessActivity extends SystemBarActivity {

    public static final String TAG = Logger.makeLogTag(SellPdSuccessActivity.class);

    // EXTRAS
    private static final String EXTRA_PD_SALE_SUCCESS_PARAMS = "EXTRA_PD_SALE_SUCCESS_PARAMS";

    private ReadRfidData readRfidData;
    private int newPdNumber;
    private View readBScProgressBar;
    private View printPdSuccessView;
    private Button calculateDelivery;
    private PdSaleSuccessParams pdSaleSuccessParams;
    private List<Fragment> fragments;
    private TicketEventBase newTicketEventBase;
    private TextView message;
    private AtomicBoolean isReadCard = new AtomicBoolean(false);
    private Handler handler;

    // UI
    /**
     * Кнопка "докупить багаж"
     */
    private Button sellPdBuyBaggageBtn;
    /**
     * Кнопка "Оформить новый ПД"
     */
    private Button sellNewPdBtn;

    private final DeferredActionHandler deferredActionHandler = new DeferredActionHandler();

    public static Intent getCallingIntent(Context context, PdSaleSuccessParams pdSaleSuccessParams) {
        Intent intent = new Intent(context, SellPdSuccessActivity.class);
        intent.putExtra(EXTRA_PD_SALE_SUCCESS_PARAMS, pdSaleSuccessParams);
        return intent;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sell_pd_success);

        handler = new Handler();

        fragments = new ArrayList<>();
        readRfidData = new ReadRfidData(Dagger.appComponent().findCardTaskFactory(),
                Dagger.appComponent().beepPlayer(),
                (pdList, bscInformation) -> SellPdSuccessActivity.this.readCompleted(pdList));

        pdSaleSuccessParams = getIntent().getParcelableExtra(EXTRA_PD_SALE_SUCCESS_PARAMS);

        CppkTicketSaleDao saleDao = getLocalDaoSession().getCppkTicketSaleDao();
        CPPKTicketSales newPD = saleDao.load(pdSaleSuccessParams.getNewPDId());

        TicketSaleReturnEventBase ticketSaleReturnEventBase = getLocalDaoSession().getTicketSaleReturnEventBaseDao().load(newPD.getTicketSaleReturnEventBaseId());
        Check check = getLocalDaoSession().getCheckDao().load(ticketSaleReturnEventBase.getCheckId());
        newPdNumber = check.getOrderNumber();

        newTicketEventBase = getLocalDaoSession().getTicketEventBaseDao().load(ticketSaleReturnEventBase.getTicketEventBaseId());

        setupView(pdSaleSuccessParams.getSaleType());
    }

    private void setupView(SaleType saleType) {

        calculateDelivery = (Button) findViewById(R.id.sellPd_calculate_delivery);
        calculateDelivery.setOnClickListener(v -> onCalculateDeliveryBtnClicked());
        calculateDelivery.setVisibility(pdSaleSuccessParams.isHideDeliveryButton() ? View.GONE : View.VISIBLE);

        sellPdBuyBaggageBtn = (Button) findViewById(R.id.sellPd_bay_baggage);
        sellPdBuyBaggageBtn.setOnClickListener(v -> onSellBaggageBtnClicked());
        sellNewPdBtn = (Button) findViewById(R.id.sellPd_sell_new_pd);
        sellNewPdBtn.setOnClickListener(v -> onSellNewPdBtnClicked());
        printPdSuccessView = findViewById(R.id.sell_pd_success_pring_pd_is_success);
        message = (TextView) findViewById(R.id.sell_pd_success_message);

        if (saleType == SaleType.SMART_CARD) {
            isReadCard.set(true);
            View sellPdSuccessWritePdTitle = findViewById(R.id.sell_pd_success_write_pd_title);
            sellPdSuccessWritePdTitle.setVisibility(View.VISIBLE);
            printPdSuccessView.setVisibility(View.GONE);
            readBScProgressBar = findViewById(R.id.selL_pd_success_layout_progress_bar);
            readBScProgressBar.setVisibility(View.VISIBLE);
            readRfidData.startRfidRead();
        } else {
            printPdSuccessView.setVisibility(View.VISIBLE);
            message.setText(R.string.print_pd_success);
        }

        configAccess();
    }


    @Override
    protected void onResume() {
        super.onResume();
        deferredActionHandler.resume();
    }

    @Override
    protected void onPause() {
        deferredActionHandler.pause();
        super.onPause();
    }

    @Override
    protected void onStart() {
        super.onStart();
        //https://aj.srvdev.ru/browse/CPPKPP-27923 https://aj.srvdev.ru/browse/CPPKPP-28025
        handler.postDelayed(autoCloseRunnable, TimeUnit.SECONDS.toMillis(Dagger.appComponent().commonSettings().getAutoCloseTime()));
    }

    @Override
    protected void onStop() {
        handler.removeCallbacks(autoCloseRunnable);
        super.onStop();
    }

    private Runnable autoCloseRunnable = () -> Navigator.navigateToMenuActivity(this);

    private void onCalculateDeliveryBtnClicked() {
        Navigator.navigateToCalculateDeliveryFixedCostActivity(this, pdSaleSuccessParams.getPdCost());
    }

    private void onSellBaggageBtnClicked() {
        PdSaleParams pdSaleParams = new PdSaleParams();
        pdSaleParams.setTicketCategoryCode((int) TicketCategory.Code.BAGGAGE);
        pdSaleParams.setDirectionCode(newTicketEventBase.getWayType().getCode());
        pdSaleParams.setDepartureStationCode(pdSaleSuccessParams.getDepartureStationCode());
        pdSaleParams.setDestinationStationCode(pdSaleSuccessParams.getDestinationStationCode());
        Navigator.navigateToPdSaleActivity(this, pdSaleParams);
        finish();
    }

    private void onSellNewPdBtnClicked() {
        PdSaleParams pdSaleParams = new PdSaleParams();
        pdSaleParams.setTicketCategoryCode((int) TicketCategory.Code.SINGLE);
        pdSaleParams.setDirectionCode(TicketWayType.OneWay.getCode());
        Navigator.navigateToPdSaleActivity(this, pdSaleParams);
        finish();
    }

    @Override
    public void onClickSettings() {
        // если карта еще читается, то перекрываем нажатие
        if (!isReadCard.get()) {
            super.onClickSettings();
        }
    }

    @Override
    public void onClickBarcode() {
        // если карта еще читается, то перекрываем нажатие
        if (!isReadCard.get()) {
            super.onClickBarcode();
        }
    }

    @Override
    public void onClickRfrid() {
        // если карта еще читается, то перекрываем нажатие
        if (!isReadCard.get()) {
            super.onClickRfrid();
        }
    }

    @Override
    public void onBackPressed() {
        // если карта еще читается, то перекрываем нажатие
        if (!isReadCard.get()) {
            Navigator.navigateToMenuActivity(this);
        }
    }

    private void readCompleted(List<PD> pdList) {
        deferredActionHandler.post(() -> {
            if (!isDestroyed()) {
                isReadCard.set(false);

                runOnUiThread(() -> {
                    if (readBScProgressBar != null) {
                        readBScProgressBar.setVisibility(View.GONE);
                    }
                });

                if (pdList == null || pdList.isEmpty()) {
                    runOnUiThread(() -> {
                        printPdSuccessView.setVisibility(View.VISIBLE);
                        message.setText(R.string.write_pd_success);
                    });
                } else {
                    FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();

                    int newIndex = 0;
                    long saleDate = 0;
                    //высчитываем доп. критерий определения какой билет мы только что записали
                    //записанным считается билет с самым позднем временем продажи
                    for (int i = 0; i < pdList.size(); i++) {
                        PD pd = pdList.get(i);

                        PdVersion pdVersion = pd == null ? null : PdVersion.getByCode(pd.versionPD);

                        if ((pdVersion == PdVersion.V5
                                || pdVersion == PdVersion.V13
                                || pdVersion == PdVersion.V3
                                || pdVersion == PdVersion.V11)
                                && saleDate < pd.getSaleDateInSecond()) {
                            newIndex = i;

                            saleDate = pd.getSaleDateInSecond();
                        }
                    }

                    for (int i = 0; i < pdList.size(); i++) {
                        PD pd = pdList.get(i);

                        PdVersion pdVersion = pd == null ? null : PdVersion.getByCode(pd.versionPD);

                        if (pdVersion == PdVersion.V5
                                || pdVersion == PdVersion.V13
                                || pdVersion == PdVersion.V3
                                || pdVersion == PdVersion.V11) {
                            // Показываем только записываемый ПД
                            // http://agile.srvdev.ru/browse/CPPKPP-42845
                            boolean isNewPd = newPdNumber == pd.numberPD && newIndex == i;
                            if (isNewPd) {
                                Fragment fragment = PdFragmentCreator.createFragment(SellPdSuccessActivity.this, pd, ValidityPdVariants.ONE_OF_TWO_PD_IS_VALID, true,
                                        false, i, false, false, false, false,
                                        simplePdFragmentCallback, null, null);
                                fragmentTransaction.add(R.id.sell_pd_success_container, fragment);
                                fragments.add(fragment);
                            }
                        }
                    }

                    fragmentTransaction.commit();
                }
            }
        });
    }

    /**
     * Настраивает доступность функций для разный ролей пользователей
     */
    private void configAccess() {
        // запрещаем или показываем кнопку продажи ПД
       sellNewPdBtn.setVisibility(Dagger.appComponent().privateSettings().isSaleEnabled()
                && Dagger.appComponent().permissionChecker().checkPermission(PermissionDvc.SalePd)
                ? View.VISIBLE : View.GONE);
        // запрещаем или показываем кнопку Продажи багажа
       sellPdBuyBaggageBtn.setVisibility(Dagger.appComponent().permissionChecker().checkPermission(PermissionDvc.SaleBaggage)
                ? View.VISIBLE : View.GONE);
    }

    private final SimplePdActivityLogic.Callback simplePdFragmentCallback = new SimplePdActivityLogic.Callback() {
        @Override
        public void onSaleSurchargeBtnClicked(@NonNull PD legacyPd) {
            /* NOP */
        }

        @Override
        public void setHardwareButtonsEnabled(boolean value) {
            /* NOP */
        }

        @Override
        public void onSaleTransferBtnClicked(@NonNull PD legacyPd) {
            /* NOP */
        }

        @Override
        public void onPdValidBtnClicked(@NonNull PD legacyPd) {
            /* NOP */
        }

        @Override
        public void onPdNotValidBtnClicked(@NonNull PD legacyPd) {
            /* NOP */
        }
    };

}
