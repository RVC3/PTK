package ru.ppr.cppk.ui.activity;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.google.common.base.Preconditions;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import ru.ppr.core.dataCarrier.pd.PdVersionChecker;
import ru.ppr.core.dataCarrier.pd.base.Pd;
import ru.ppr.cppk.ErrorActivity;
import ru.ppr.cppk.GlobalConstants;
import ru.ppr.cppk.Globals;
import ru.ppr.cppk.R;
import ru.ppr.cppk.dataCarrier.entity.BscInformation;
import ru.ppr.cppk.dataCarrier.entity.PD;
import ru.ppr.cppk.dataCarrier.pd.check.control.PassageResult;
import ru.ppr.cppk.db.local.ShiftEventDao;
import ru.ppr.cppk.di.Dagger;
import ru.ppr.cppk.entity.event.base34.TicketEventBase;
import ru.ppr.cppk.entity.event.model.SmartCard;
import ru.ppr.cppk.entity.settings.PrivateSettings;
import ru.ppr.cppk.entity.utils.builders.events.TicketEventBaseGenerator;
import ru.ppr.cppk.helpers.SchedulersCPPK;
import ru.ppr.cppk.helpers.UiThread;
import ru.ppr.cppk.helpers.controlbarcodestorage.PdControlBarcodeData;
import ru.ppr.cppk.helpers.controlbarcodestorage.PdControlBarcodeDataStorage;
import ru.ppr.cppk.legacy.BscInformationChecker;
import ru.ppr.cppk.legacy.ExtraPaymentParamsBuilder;
import ru.ppr.cppk.legacy.SmartCardBuilder;
import ru.ppr.cppk.localdb.model.ShiftEvent;
import ru.ppr.cppk.localdb.model.TicketWayType;
import ru.ppr.cppk.logic.NeedCreateControlEventChecker;
import ru.ppr.cppk.logic.PermissionChecker;
import ru.ppr.cppk.logic.TransferPdChecker;
import ru.ppr.cppk.logic.TransferSaleButtonDetector;
import ru.ppr.cppk.logic.interactor.PdValidityPeriodCalculator;
import ru.ppr.cppk.logic.utils.DateUtils;
import ru.ppr.cppk.managers.NsiVersionManager;
import ru.ppr.cppk.model.ExtraPaymentParams;
import ru.ppr.cppk.model.PdSaleParams;
import ru.ppr.cppk.pd.utils.PdFragmentCreator;
import ru.ppr.cppk.pd.utils.ValidityPdVariants;
import ru.ppr.cppk.pd.utils.reader.ReaderType;
import ru.ppr.cppk.systembar.SystemBarActivity;
import ru.ppr.cppk.ui.activity.base.Navigator;
import ru.ppr.cppk.ui.activity.readpdfortransfer.model.ReadForTransferParams;
import ru.ppr.cppk.ui.activity.transfersale.model.TransferSaleParams;
import ru.ppr.cppk.ui.dialog.SimpleDialog;
import ru.ppr.cppk.ui.fragment.pd.simple.SimplePdActivityLogic;
import ru.ppr.logger.Logger;
import ru.ppr.nsi.entity.Tariff;
import ru.ppr.nsi.entity.TicketCategory;
import ru.ppr.nsi.entity.TicketType;
import ru.ppr.nsi.repository.SmartCardCancellationReasonRepository;
import ru.ppr.security.entity.PermissionDvc;
import ru.ppr.security.entity.SmartCardStopListItem;
import rx.Single;
import rx.android.schedulers.AndroidSchedulers;

/**
 * Экран с резльтатом чтения ПД с ШК.
 */
public class ResultBarcodeActivity extends SystemBarActivity {

    private static final String TAG = Logger.makeLogTag(ResultBarcodeActivity.class);

    private static final String PD_LIST = "PD_LIST";
    private static final String EXTRA_READ_FOR_TRANSFER_PARAMS = "EXTRA_READ_FOR_TRANSFER_PARAMS";

    private static final String NO_TRANSFER_CAN_BE_SOLD_DIALOG_TAG = "NO_TRANSFER_CAN_BE_SOLD_DIALOG_TAG";

    public static Intent getCallingIntent(@NonNull Context context,
                                          @Nullable ArrayList<PD> pdList,
                                          @Nullable ReadForTransferParams readForTransferParams) {
        Intent intent = new Intent(context, ResultBarcodeActivity.class);
        intent.putExtra(PD_LIST, pdList);
        intent.putExtra(EXTRA_READ_FOR_TRANSFER_PARAMS, readForTransferParams);
        return intent;
    }

    // region Di
    @Inject
    NsiVersionManager nsiVersionManager;
    @Inject
    PdValidityPeriodCalculator pdValidityPeriodCalculator;
    @Inject
    NeedCreateControlEventChecker needCreateControlEventChecker;
    @Inject
    UiThread uiThread;
    @Inject
    PrivateSettings privateSettings;
    @Inject
    PermissionChecker permissionChecker;
    @Inject
    SmartCardCancellationReasonRepository smartCardCancellationReasonRepository;
    @Inject
    TransferSaleButtonDetector transferSaleButtonDetector;
    @Inject
    TransferPdChecker transferPdChecker;
    @Inject
    PdVersionChecker pdVersionChecker;
    //endregion

    /**
     * Кнопка "Оформить новый ПД"
     */
    private Button sellPdButton;

    private PD legacyPd;

    /**
     * Флаг добавленности события контроля в БД
     */
    private boolean isEventAdded = false;
    /**
     * Данные для оформления трансфера по считанному ПД
     */
    private ReadForTransferParams readForTransferParams;

    private boolean pdWithPlace;

    @Inject
    PdControlBarcodeDataStorage pdControlBarcodeDataStorage;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.result_barcode_activity);
        Dagger.appComponent().inject(this);

        isEventAdded = false;

        canUserHardwareButton();

        sellPdButton = (Button) findViewById(R.id.sale_pd);

        readForTransferParams = getIntent().getParcelableExtra(EXTRA_READ_FOR_TRANSFER_PARAMS);
        List<PD> pdList = getIntent().getParcelableArrayListExtra(PD_LIST);

        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage(getString(readForTransferParams == null ?
                R.string.result_barcode_activity_handle_progress :
                R.string.result_barcode_activity_detecting_transfer_sale_progress
        ));
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        handlePdList(pdList);
    }

    @Override
    protected void onDestroy() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }

        super.onDestroy();
    }

    private void handlePdList(List<PD> pdList) {
        PdControlBarcodeData pdControlBarcodeData = pdControlBarcodeDataStorage.getLastBarcodeData();
        Pd pd = pdControlBarcodeData == null ? null : pdControlBarcodeData.getPd();
        pdWithPlace = false;
        if (pd != null) {
            if (pdVersionChecker.isPdWithPlace(pd.getVersion())) {
                pdWithPlace = true;
            }
        }

        if ((pdList != null && pdList.size() == 1) || pdWithPlace) {
            Single
                    .fromCallable(() -> {
                        if (!pdWithPlace) {
                            // Т.к. с ШК можно считать только 1 ПД, то забираем его с 0 позиции
                            legacyPd = pdList.get(0);
                            List<PassageResult> errors = legacyPd.errors;
                            if (errors.isEmpty()) {
                                uiThread.post(() -> sellPdButton.setVisibility(View.GONE));
                            }
                        }

                        //Определяем статус ПД
                        ValidityPdVariants variants;

                        if (!pdWithPlace) {
                            if (legacyPd.isValid()) {
                                variants = ValidityPdVariants.ONE_PD_VALID;
                            } else {
                                uiThread.post(() -> sellPdButton.setVisibility(
                                        privateSettings.isSaleEnabled()
                                                && permissionChecker.checkPermission(PermissionDvc.SalePd)
                                                ? View.VISIBLE : View.GONE));
                                sellPdButton.setOnClickListener(v -> onSellNewPdBtnClicked());
                                variants = ValidityPdVariants.ONE_PD_INVALID;
                            }
                        } else {
                            variants = ValidityPdVariants.ONE_PD_INVALID;
                        }
                        // Экраны контроля в поезде и оформления трансфера отличаются в поведении:
                        // пока мы не можем при обычном контроле в поезде выводить кнопку оформления трансфера.
                        // На экране оформления трансфера мы скрываем кнопку "оформить новый ПД" за ненадобностью,
                        // остальное поведение оставляем как есть.
                        // см. http://agile.srvdev.ru/browse/CPPKPP-37099
                        // Также скрываем кнопку для ПД с местом
                        if (readForTransferParams != null || pdWithPlace) {
                            uiThread.post(() -> sellPdButton.setVisibility(View.GONE));
                            sellPdButton.setOnClickListener(null);
                        }

                        InitResult initResult = new InitResult();
                        initResult.variants = variants;
                        initResult.transferSaleButtonCanBeShown = pdWithPlace ? false : detectIfTransferSaleButtonCanBeShown(legacyPd, pd);

                        return initResult;
                    })
                    .subscribeOn(SchedulersCPPK.background())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(initResult -> {
                        progressDialog.dismiss();

                        Fragment fragment = PdFragmentCreator.createFragment(ResultBarcodeActivity.this, legacyPd, initResult.variants,
                                false, true, 0, false,
                                initResult.transferSaleButtonCanBeShown, readForTransferParams != null, pdWithPlace,
                                simplePdFragmentCallback,
                                null, null);

                        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                        fragmentTransaction.replace(R.id.fragment_container, fragment);
                        fragmentTransaction.commit();

                        if (readForTransferParams != null && !initResult.transferSaleButtonCanBeShown) {
                            uiThread.post(this::showNoTransferCanBeSoldDialog);
                        }

                    }, throwable -> {
                        Logger.error(TAG, throwable);
                        progressDialog.dismiss();
                    });
        } else {
            Logger.error(TAG, "Error read pd from barcode - " + (pdList == null ? "pdList is null"
                    : "Wrong pd list size - " + pdList.size()));
            startErrorActivity();
        }
    }

    private void onSellNewPdBtnClicked() {
        Logger.info(TAG, "onSellNewPdBtnClicked");
        lockViews();
        //добавим событие в БД
        addControlEvent(PassageResult.RouteNotFound);
        showSellNewPdApproveDialog();
    }

    private void showSellNewPdApproveDialog() {
        SimpleDialog simpleDialog = SimpleDialog.newInstance(null,
                getString(R.string.question_sell_new_pd),
                getString(R.string.Yes),
                getString(R.string.No),
                LinearLayout.HORIZONTAL,
                0);
        simpleDialog.setCancelable(false);
        simpleDialog.show(getFragmentManager(), SimpleDialog.FRAGMENT_TAG);
        simpleDialog.setDialogPositiveBtnClickListener((dialog, dialogId) -> {
            PdSaleParams pdSaleParams = new PdSaleParams();
            pdSaleParams.setTicketCategoryCode((int) TicketCategory.Code.SINGLE);
            pdSaleParams.setDirectionCode(TicketWayType.OneWay.getCode());
            Navigator.navigateToPdSaleActivity(this, pdSaleParams);
            finish();
        });
        simpleDialog.setDialogNegativeBtnClickListener((dialog, dialogId) -> {
            finish();
        });
    }

    private boolean detectIfTransferSaleButtonCanBeShown(@NonNull PD legacyPd, @NonNull Pd pd) {

        boolean validForTransferSale = true;
        for (PassageResult passageResult : legacyPd.errors) {
            switch (passageResult) {
                // ПД не начал действовать - допустимо
                case TooEarly:
                    // http://agile.srvdev.ru/browse/CPPKPP-44118
                    // Некорректная категория поезда - допустимо
                case BannedTrainType: {
                    continue;
                }
                default: {
                    validForTransferSale = false;
                    break;
                }
            }
        }

        return readForTransferParams != null &&
                validForTransferSale &&
                !transferPdChecker.check(pd) &&
                privateSettings.isTransferSaleEnabled() &&
                transferSaleButtonDetector.detect(legacyPd, readForTransferParams);
    }

    private void showNoTransferCanBeSoldDialog() {
        SimpleDialog noTransferCanBeSoldDialog = SimpleDialog.newInstance(null,
                getString(R.string.result_barcode_no_ticket_for_transfer_msg),
                getString(R.string.result_barcode_no_ticket_for_transfer_ok_btn),
                null,
                LinearLayout.HORIZONTAL,
                0);
        noTransferCanBeSoldDialog.show(getFragmentManager(), NO_TRANSFER_CAN_BE_SOLD_DIALOG_TAG);
    }

    private void startErrorActivity() {
        finish();
        Intent intent = new Intent(this, ErrorActivity.class);
        intent.putExtra(ErrorActivity.TYPE_ERROR, ErrorActivity.ERROR_TYPE_UNKNOWN);
        intent.putExtra(ErrorActivity.LAST_ACTIVITY, GlobalConstants.READ_BARCODE_ACTIVITY);
        startActivity(intent);
    }

    /**
     * Добавляет событие контроля в БД
     */
    private void addControlEvent(PassageResult passageResult) {
        PD localPd = legacyPd;
        if (localPd == null) {
            Logger.error(TAG, "Local pd is null");
            return;
        }
        if (isEventAdded) {
            Logger.error(TAG, "isEventAdded: true");
            return;
        }
        if (!legacyPd.isReadyToAddControlEvent()) {
            Logger.info(TAG, "Пропускаем создание события контроля");
            return;
        }
        if (readForTransferParams != null) {
            // Если активити запущена с флагом transfer значит не надо создавать событие в любом случае
            Logger.info(TAG, "Пропускаем создание события контроля, transfer: true");
            return;
        }
        if (!needCreateControlEventChecker.check(legacyPd)) {
            // Проверяем необходимость создания события контроля
            Logger.info(TAG, "Пропускаем создание события контроля, needCreateControlEvent: false");
            return;
        }

        // добавим событие контроля в бд
        int smartCardStopListReasonCode = 0;
        BscInformation bscInformation = localPd.getBscInformation();
        SmartCard smartCard = null;
        if (bscInformation != null) {
            smartCard = new SmartCardBuilder().setBscInformation(bscInformation).build();
            if (localPd.getPassageMark() != null)
                smartCard.setUsageCount(localPd.getPassageMark().getCounterCard());
            android.util.Pair<SmartCardStopListItem, String> stopItemResult = new BscInformationChecker(
                    bscInformation,
                    nsiVersionManager,
                    smartCardCancellationReasonRepository).getStopListItem(true);
            if (stopItemResult != null)
                smartCardStopListReasonCode = stopItemResult.first.getReasonCode();
        }

        Globals globals = Globals.getInstance();

        ShiftEventDao shiftDao = Globals.getInstance().getLocalDaoSession()
                .getShiftEventDao();
        ShiftEvent shift = shiftDao.getLastShiftEvent(ShiftEvent.ShiftProgressStatus.FINISHED_STATUSES);
        if (shift == null || ShiftEvent.Status.ENDED.equals(shift.getStatus())) {
            throw new IllegalStateException("Shift is null or closed");
        }

        Tariff tariff = legacyPd.getTariff();
        Preconditions.checkNotNull(tariff, "Tariff is null");

        // дата начала действия ПД должна быть с начала дня
        Calendar calendar = DateUtils.getStartOfDay(new Date((legacyPd.getSaleDateInSecond() + legacyPd.term * GlobalConstants.SECOND_IN_DAY) * 1000));
        Date startPdTime = calendar.getTime();

        // дата окончания = дата начала + количество дней действия
        final TicketType ticketType = tariff.getTicketType(globals.getNsiDaoSession());
        int validityDays = pdValidityPeriodCalculator.calcValidityPeriod(startPdTime, legacyPd.wayType, ticketType, nsiVersionManager.getCurrentNsiVersionId());

        calendar.add(Calendar.DAY_OF_MONTH, validityDays);
        calendar.add(Calendar.SECOND, -1); // вычитаем 1 секунду, т.к. действует до 23:25:59
        Date endTimePD = calendar.getTime();

        getLocalDaoSession().beginTransaction();
        try {
            if (smartCard != null) {
                getLocalDaoSession().getSmartCardDao().save(smartCard);
            }

            TicketEventBase ticketEventBase = new TicketEventBaseGenerator()
                    .setSmartCard(smartCard)
                    .setCurrentShift(shift)
                    .setWayType(legacyPd.wayType)
                    .setSaleTime(legacyPd.getSaleDate())
                    .setTicketTypeCode(tariff.getTicketTypeCode())
                    .setTicketCategoryCode(ticketType.getTicketCategoryCode())
                    .setStartDayOffset(legacyPd.term)
                    .setValidFromDate(startPdTime)
                    .setValidTillDate(endTimePD)
                    .setType(ticketType.getExpressTicketTypeCode())
                    .setDepartureStationCode(tariff.getStationDepartureCode())
                    .setDestinationStationCode(tariff.getStationDestinationCode())
                    .setTicketTypeShortName(ticketType.getShortName())
                    .setTariffCode(Long.valueOf(tariff.getCode()))
                    .build();

            Dagger.appComponent().ticketControlEventCreator()
                    .setPd(localPd)
                    .setPassageResult(passageResult)
                    .setTicketEventBase(ticketEventBase)
                    .setSmartCardStopListReasonCode(smartCardStopListReasonCode)
                    .create();

            getLocalDaoSession().setTransactionSuccessful();
        } finally {
            getLocalDaoSession().endTransaction();
        }

        isEventAdded = true;
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (!pdWithPlace) {
            PassageResult passageResult = PassageResult.SuccesPassage;
            if (!legacyPd.isValid()) {
                passageResult = legacyPd.errors.get(0);
            }
            addControlEvent(passageResult);
        }
        return super.onKeyUp(keyCode, event);
    }

    // Вызов startReadPd с этого экрана в итоге плодить еще одну такую же активити
    // поэтому в стеке получается много ненужных одинаковых активити, вызываем финишь
    // чтобы предотвратить эту ситуацию, см. http://agile.srvdev.ru/browse/CPPKPP-34869
    @Override
    public void startReadPd(ReaderType readerType, ReadForTransferParams readForTransferParams) {
        super.startReadPd(readerType, this.readForTransferParams);
        finish();
    }

    private static class InitResult {

        private ValidityPdVariants variants;
        private boolean transferSaleButtonCanBeShown;

    }

    private final SimplePdActivityLogic.Callback simplePdFragmentCallback = new SimplePdActivityLogic.Callback() {
        @Override
        public void onSaleSurchargeBtnClicked(@NonNull PD legacyPd) {
            // Добавляем событие контроля в БД
            lockViews();
            addControlEvent(PassageResult.BannedTrainType);
            // Запускаем экран оформления доплаты
            ExtraPaymentParams extraPaymentParams = ExtraPaymentParamsBuilder.from(legacyPd);
            Navigator.navigateToExtraPaymentActivity(ResultBarcodeActivity.this, extraPaymentParams);
            // Закрываем экран с ПД
            finish();
        }

        @Override
        public void setHardwareButtonsEnabled(boolean value) {
            /* NOP */
        }

        @Override
        public void onSaleTransferBtnClicked(@NonNull PD legacyPd) {
            Navigator.navigateToTransferSaleActivity(ResultBarcodeActivity.this, TransferSaleParams.Builder.fromPD(legacyPd, readForTransferParams));
            finish();
        }

        @Override
        public void onPdValidBtnClicked(@NonNull PD legacyPd) {
            addControlEvent(PassageResult.SuccesPassage);
        }

        @Override
        public void onPdNotValidBtnClicked(@NonNull PD legacyPd) {
            addControlEvent(PassageResult.RouteNotFound);
        }
    };

}
