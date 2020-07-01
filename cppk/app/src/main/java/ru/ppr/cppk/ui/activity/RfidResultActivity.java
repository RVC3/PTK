package ru.ppr.cppk.ui.activity;

import android.annotation.SuppressLint;
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
import android.widget.TabHost;
import android.widget.TextView;

import com.google.common.base.Preconditions;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import ru.ppr.core.dataCarrier.pd.PdVersion;
import ru.ppr.core.dataCarrier.pd.PdVersionChecker;
import ru.ppr.core.dataCarrier.pd.base.Pd;
import ru.ppr.core.dataCarrier.smartCard.passageMark.base.PassageMark;
import ru.ppr.core.ui.helper.CoppernicKeyEvent;
import ru.ppr.cppk.GlobalConstants;
import ru.ppr.cppk.Globals;
import ru.ppr.cppk.R;
import ru.ppr.cppk.dataCarrier.PdFromLegacyMapper;
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
import ru.ppr.cppk.helpers.controlbscstorage.PdControlCardData;
import ru.ppr.cppk.helpers.controlbscstorage.PdControlCardDataStorage;
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
import ru.ppr.cppk.logic.pd.checker.ValidAndControlNeededChecker;
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
import ru.ppr.cppk.ui.activity.serviceticketcontrol.cardinfo.CardInfoFragment;
import ru.ppr.cppk.ui.activity.transfersale.model.TransferSaleParams;
import ru.ppr.cppk.ui.dialog.SimpleDialog;
import ru.ppr.cppk.ui.fragment.pd.countrips.CountTripsPdActivityLogic;
import ru.ppr.cppk.ui.fragment.pd.countrips.model.CountTripsPdControlData;
import ru.ppr.cppk.ui.fragment.pd.invalid.ErrorFragment;
import ru.ppr.cppk.ui.fragment.pd.invalid.ErrorFragment.Errors;
import ru.ppr.cppk.ui.fragment.pd.servicefee.ServiceFeePdFragment;
import ru.ppr.cppk.ui.fragment.pd.simple.SimplePdActivityLogic;
import ru.ppr.logger.Logger;
import ru.ppr.nsi.entity.Tariff;
import ru.ppr.nsi.entity.TicketCategory;
import ru.ppr.nsi.entity.TicketType;
import ru.ppr.nsi.repository.SmartCardCancellationReasonRepository;
import ru.ppr.security.entity.PermissionDvc;
import ru.ppr.security.entity.SmartCardStopListItem;
import rx.Completable;
import rx.android.schedulers.AndroidSchedulers;

/**
 * Выводит результат чтения БСК с билетами.
 *
 * @author А.Ушаков
 */
public class RfidResultActivity extends SystemBarActivity {

    private static final String TAG = Logger.makeLogTag(RfidResultActivity.class);

    private static final String PD_LIST = "PD_LIST";
    private static final String BSC_INFO = "BSC_INFO";
    private static final String EXTRA_READ_FOR_TRANSFER_PARAMS = "EXTRA_READ_FOR_TRANSFER_PARAMS";

    public static Intent getCallingIntent(@NonNull Context context,
                                          @Nullable ArrayList<PD> legacyPdList,
                                          @Nullable BscInformation bscInformation,
                                          @Nullable ReadForTransferParams readForTransferParams) {
        Intent intent = new Intent(context, RfidResultActivity.class);
        intent.putExtra(PD_LIST, legacyPdList);
        intent.putExtra(BSC_INFO, bscInformation);
        intent.putExtra(EXTRA_READ_FOR_TRANSFER_PARAMS, readForTransferParams);
        return intent;
    }

    private static final String PD_DATA = "pd";
    private static final String BSC_DATA = "bsc";
    private static final String NO_TRANSFER_CAN_BE_SOLD_DIALOG_TAG = "NO_TRANSFER_CAN_BE_SOLD_DIALOG_TAG";

    // region Di
    @Inject
    Globals globals;
    @Inject
    ValidAndControlNeededChecker validAndControlNeededChecker;
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


    //region Views
    private TabHost tabHost;
    private Button sellPdButton;
    private Button pdsIsNoValidButton;
    //endregion
    private ValidityPdVariants variants = null;
    private ArrayList<PD> legacyPdList = null;

    private boolean pdWithPlace;

    @Inject
    PdControlCardDataStorage pdControlCardDataStorage;

    private boolean isHardwareButtonsEnabled = true;
    /**
     * Фрагмент с информацией о карте
     */
    private CardInfoFragment cardInfoFragment;
    /**
     * Флаг, что метка прохода прошла проверку
     */
    private boolean passageMarkChecked;
    /**
     * Данные для оформления трансфера по считанному ПД
     */
    private ReadForTransferParams readForTransferParams;

    private ProgressDialog progressDialog;

    private Map<PD, CountTripsPdControlData> countTripsPdControlData = new HashMap<>();

    @SuppressLint("InflateParams")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bsc_result_activity);
        Dagger.appComponent().inject(this);
        /////////////////////////////////////////////////////////
        tabHost = (TabHost) findViewById(android.R.id.tabhost);
        tabHost.setup();
        TabHost.TabSpec spec;
        spec = tabHost.newTabSpec(PD_DATA);
        View tabHostHeader = getLayoutInflater().inflate(R.layout.tab_host_header, null);
        final TextView title1 = (TextView) tabHostHeader.findViewById(R.id.tvTabHostTitle);
        title1.setText(R.string.PD);
        title1.setBackgroundColor(getResources().getColor(R.color.white));
        title1.setTextColor(getResources().getColor(R.color.black));
        title1.setBackground(getResources().getDrawable(R.drawable.top_corners));

        spec.setIndicator(tabHostHeader);
        spec.setContent(R.id.pd_info);
        tabHost.addTab(spec);

        spec = tabHost.newTabSpec(BSC_DATA);
        View tabHostHeader2 = getLayoutInflater().inflate(R.layout.tab_host_header, null);
        final TextView title2 = (TextView) tabHostHeader2.findViewById(R.id.tvTabHostTitle);
        title2.setText(R.string.BSC);
        title2.setBackgroundColor(getResources().getColor(R.color.black));

        title2.setTextColor(getResources().getColor(R.color.gray_tabhost_title));
        spec.setIndicator(tabHostHeader2);
        spec.setContent(R.id.bsc_info_container);
        tabHost.addTab(spec);
        tabHost.setOnTabChangedListener(tabId -> {
            if (tabId.equals(PD_DATA)) {
                title1.setBackgroundColor(getResources().getColor(R.color.white));
                title1.setTextColor(getResources().getColor(R.color.black));
                title1.setBackground(getResources().getDrawable(R.drawable.top_corners));
                title2.setBackgroundColor(getResources().getColor(R.color.black));
                title2.setTextColor(getResources().getColor(R.color.gray_tabhost_title));
            } else {
                title2.setBackgroundColor(getResources().getColor(R.color.white));
                title2.setTextColor(getResources().getColor(R.color.black));
                title2.setBackground(getResources().getDrawable(R.drawable.top_corners));
                title1.setBackgroundColor(getResources().getColor(R.color.black));
                title1.setTextColor(getResources().getColor(R.color.gray_tabhost_title));

            }
        });
        tabHost.setCurrentTabByTag(PD_DATA);
        /////////////////////////////////////////////////////////
        sellPdButton = (Button) findViewById(R.id.sale_pd);
        sellPdButton.setOnClickListener(v -> showSellNewPdApproveDialog());
        pdsIsNoValidButton = (Button) findViewById(R.id.pd_is_not_valid);
        pdsIsNoValidButton.setOnClickListener(v -> onSellNewPdBtnClicked());
        addLockedView(pdsIsNoValidButton);
        addLockedView(sellPdButton);

        canUserHardwareButton();

        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage(getString(readForTransferParams == null ?
                R.string.rfid_result_activity_handle_progress :
                R.string.rfid_result_activity_detecting_transfer_sale_progress
        ));
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        showResult();
    }

    @Override
    protected void onDestroy() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }

        super.onDestroy();
    }

    private void showResult() {

        legacyPdList = getIntent().getParcelableArrayListExtra(PD_LIST);
        BscInformation bscInformation = getIntent().getParcelableExtra(BSC_INFO);
        readForTransferParams = getIntent().getParcelableExtra(EXTRA_READ_FOR_TRANSFER_PARAMS);

        PdControlCardData pdControlCardData = pdControlCardDataStorage.getLastCardData();
        List<Pd> pdList = pdControlCardData == null ? null : pdControlCardData.getPdList();
        pdWithPlace = false;

        if (pdList != null && pdList.size() == 1) {
            Pd firstPd = pdList.get(0);
            if (firstPd != null) {
                if (pdVersionChecker.isPdWithPlace(firstPd.getVersion())) {
                    pdWithPlace = true;
                }
            }
        }

        if (!pdWithPlace && (legacyPdList == null || bscInformation == null)) {
            startReadPd(ReaderType.TYPE_BSC, readForTransferParams);
            return;
        }

        if (!pdWithPlace) {
            legacyPdList = getIntent().getExtras().getParcelableArrayList(PD_LIST);
            // удаляем из списка билеты заглушки
            Iterator<PD> iterator = legacyPdList.iterator();
            while (iterator.hasNext()) {

                PD pd = iterator.next();
                if (pd.versionPD == PdVersion.V64.getCode()) {
                    iterator.remove();
                }
            }
        }

        Completable
                .fromAction(() -> {

                    boolean canSaleTransfer = false;
                    // если список пд пустой, то показываем слой с сообщением об отсутствии пд
                    if (!pdWithPlace && legacyPdList.isEmpty()) {
                        //если список ПД пустой, то в качестве количества билетов передаем 1, т.к. разделитель между билетами ненужен
                        uiThread.post(() -> {
                            variants = ValidityPdVariants.NO_TICKETS;
                            addFragment(ErrorFragment.newInstance(variants, Errors.NO_TICKET, true), R.id.pd_container);
                        });
                    } else {
                        if (!pdWithPlace) {
                            Collections.sort(legacyPdList, (pd1, pd2) -> {
                                if (needCreateControlEventChecker.check(pd1)) {
                                    return -1;
                                } else if (needCreateControlEventChecker.check(pd2)) {
                                    return 1;
                                } else {
                                    return 0;
                                }
                            });
                            variants = detectPdStatus(legacyPdList);
                        } else {
                            variants = ValidityPdVariants.ONE_PD_INVALID;
                        }

                        if (pdWithPlace) {
                            Fragment fragment = PdFragmentCreator.createFragment(this, null, variants, false, true, 0, false,
                                    false, false, true,
                                    null, null, null);
                            addFragment(fragment, R.id.pd_container);
                        } else {
                            for (int i = 0; i < legacyPdList.size(); i++) {
                                PD legacyPd = legacyPdList.get(i);
                                Pd pd = new PdFromLegacyMapper().fromLegacyPd(legacyPd);
                                boolean transferSaleButtonCanBeShown = detectIfTransferSaleButtonCanBeShown(legacyPd, pd);
                                canSaleTransfer |= transferSaleButtonCanBeShown;
                                int index = i;
                                uiThread.post(() -> {
                                    Fragment fragment = createFragment(legacyPd, variants, index, legacyPdList.size() == 2, transferSaleButtonCanBeShown, readForTransferParams != null);
                                    addFragment(fragment, R.id.pd_container);
                                });
                            }
                        }
                    }
                    uiThread.post(() -> {
                        // создаем фрагмент для отображения информации о БСК
                        cardInfoFragment = CardInfoFragment.newInstance();
                        addFragment(cardInfoFragment, R.id.bsc_info_container);
                    });

                    if (readForTransferParams != null && !canSaleTransfer) {
                        uiThread.post(this::showNoTransferCanBeSoldDialog);
                    }

                    // определяем наличие карты в стоп листе и срок действия карты
                    if (!new BscInformationChecker(
                            bscInformation,
                            nsiVersionManager,
                            smartCardCancellationReasonRepository).isCardValid(true)) {
                        // если карта в стоп листе или срок действия истек, то активным
                        // делаем таб с информацией о карте
                        uiThread.post(() -> tabHost.setCurrentTabByTag(BSC_DATA));
                    }
                    uiThread.post(() -> setupButton(variants));
                })
                .subscribeOn(SchedulersCPPK.background())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> progressDialog.dismiss(), throwable -> {
                    Logger.error(TAG, throwable);
                    progressDialog.dismiss();
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
                permissionChecker.checkPermission(PermissionDvc.SaleTransfer) &&
                transferSaleButtonDetector.detect(legacyPd, readForTransferParams);
    }

    private void showNoTransferCanBeSoldDialog() {
        SimpleDialog noTransferCanBeSoldDialog = SimpleDialog.newInstance(null,
                getString(R.string.rfid_result_no_ticket_for_transfer_msg),
                getString(R.string.rfid_result_no_ticket_for_transfer_ok_btn),
                null,
                LinearLayout.HORIZONTAL,
                0);
        noTransferCanBeSoldDialog.show(getFragmentManager(), NO_TRANSFER_CAN_BE_SOLD_DIALOG_TAG);
    }

    /**
     * Определяет статус валидности билетов
     *
     * @param list
     * @return
     */
    private ValidityPdVariants detectPdStatus(List<PD> list) {
        ValidityPdVariants result;

        if (list.size() == 1) {
            if (list.get(0).isValid())
                result = ValidityPdVariants.ONE_PD_VALID;
            else
                result = ValidityPdVariants.ONE_PD_INVALID;
        } else {
            boolean firstValidAndControlNeeded = validAndControlNeededChecker.isValidAndControlNeeded(list.get(0));
            boolean secondValidAndControlNeeded = validAndControlNeededChecker.isValidAndControlNeeded(list.get(1));

            if (firstValidAndControlNeeded && secondValidAndControlNeeded) {
                // если два билета валидны
                result = ValidityPdVariants.TWO_PD_IS_VALID;
            } else if (firstValidAndControlNeeded || secondValidAndControlNeeded) {
                // если один из билетов валиден
                result = ValidityPdVariants.ONE_OF_TWO_PD_IS_VALID;
            } else {
                // оба билета не валидны
                result = ValidityPdVariants.TWO_PD_IS_INVALID;
            }
        }

        return result;
    }

    /**
     * Настраивает кнопки в соответствии с вариантами валидности/не валидности
     * билетов
     *
     * @param variants
     */
    private void setupButton(ValidityPdVariants variants) {
        switch (variants) {
            case NO_TICKETS:

            case ONE_PD_INVALID:
            case TWO_PD_IS_INVALID:
                // показываем кнопку оформить новый ПД
                //настроим доступ к функционалу продажи в зависимости от пермишшена продажу
                sellPdButton.setVisibility(privateSettings.isSaleEnabled()
                        && permissionChecker.checkPermission(PermissionDvc.SalePd)
                        ? View.VISIBLE : View.GONE);
                pdsIsNoValidButton.setVisibility(View.GONE);
                break;

            case TWO_PD_IS_VALID:
                pdsIsNoValidButton.setVisibility(View.VISIBLE);
                break;

            case ONE_PD_VALID:
            case ONE_OF_TWO_PD_IS_VALID:
            default:
                break;
        }

        // Экраны контроля в поезде и оформления трансфера отличаются в поведении:
        // пока мы не можем при обычном контроле в поезде выводить кнопку оформления трансфера.
        // На экране оформления трансфера мы скрываем кнопку "оформить новый ПД" за ненадобностью,
        // остальное поведение оставляем как есть.
        // см. http://agile.srvdev.ru/browse/CPPKPP-37099
        // Также скрываем кнопки для ПД с местом
        if (readForTransferParams != null || pdWithPlace) {
            pdsIsNoValidButton.setVisibility(View.GONE);
            sellPdButton.setVisibility(View.GONE);
        }
    }

    /**
     * Создает фрагмент для конкретного ПД
     */
    private Fragment createFragment(PD pd, ValidityPdVariants variants, int pdIndex, boolean enableZoom, boolean transferSaleButtonCanBeShown, boolean transfer) {
        return PdFragmentCreator.createFragment(this, pd, variants, false, true,
                pdIndex, enableZoom, transferSaleButtonCanBeShown, transfer, false,
                simplePdFragmentCallback, countTripsFragmentCallback, serviceFeePdFragmentCallback);
    }

    private void addFragment(Fragment fragment, int idContainer) {
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.add(idContainer, fragment);
        fragmentTransaction.commit();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (!isHardwareButtonsEnabled)
            return true;

        if (keyCode == KeyEvent.KEYCODE_BACK || keyCode == CoppernicKeyEvent.getRfidKeyCode() || keyCode == CoppernicKeyEvent.getBarcodeKeyCode()) {
            if (tabHost.getCurrentTabTag().equals(BSC_DATA)) {
                tabHost.setCurrentTabByTag(PD_DATA);
                return true;
            }
            if (checkWarningNeed()) {
                return true;
            }
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_MENU) {
            if (checkWarningNeed()) {
                return true;
            }
        }
        return super.onKeyUp(keyCode, event);
    }


    /**
     * Проверяет нужно ли информировать пользователя при попытке ухода с этого экрана
     * А так же добавляет события контроля ПД если это необходимо
     **/
    private boolean checkWarningNeed() {

        if (readForTransferParams != null) {
            // Если экран открыт для чтения ПД для оформления трансфера,
            // позволяем спокойно уйти назад
            return false;
        }

        if (!isAlreadyRead() && variants != null) {

            if (variants == ValidityPdVariants.TWO_PD_IS_VALID) {

                SimpleDialog simpleDialog = SimpleDialog.newInstance(null,
                        getString(R.string.allert_confirm_PD),
                        getString(R.string.dialog_close),
                        null,
                        LinearLayout.VERTICAL,
                        0);
                simpleDialog.show(getFragmentManager(), null);
                return true;

            } else if (variants == ValidityPdVariants.ONE_OF_TWO_PD_IS_VALID || variants == ValidityPdVariants.ONE_PD_VALID) {
                if (!pdWithPlace) {
                    //добавляем событие контроля для действующего билета
                    for (PD pd : legacyPdList) {
                        if (pd != null && validAndControlNeededChecker.isValidAndControlNeeded(pd)) {
                            // https://aj.srvdev.ru/browse/CPPKPP-27006
                            // Если метка просрочена и пользователь нажимает списать поездку,
                            // то появляется диалог с подтверждением, действия, если пользователь отказывается,
                            // то сохраняем событие со статусом - Метка устарела, поездку не списываем
                            PdVersion pdVersion = PdVersion.getByCode(pd.versionPD);
                            Preconditions.checkNotNull(pdVersion);
                            PassageResult passageResult;
                            if (pdVersionChecker.isCountTripsSeasonTicket(pdVersion)) {
                                passageResult = passageMarkChecked ? PassageResult.SuccesPassage : PassageResult.PassMarkOutOfDate;
                            } else {
                                passageResult = PassageResult.SuccesPassage;
                            }
                            addControlEvent(pd, passageResult);
                            // Прерываем цикл, т.к. должен быть только один билет,
                            // удовлетворяющий данным условиям
                            break;
                        }
                    }
                }
            } else if (variants == ValidityPdVariants.TWO_PD_IS_INVALID || variants == ValidityPdVariants.ONE_PD_INVALID) {
                if (!pdWithPlace) {
                    //добавляем событие контроля для двух не действующих ПД
                    for (PD pd : legacyPdList) {
                        if (pd != null) {
                            //возьмем первую ошибку для билета
                            addControlEvent(pd, pd.isValid() ? PassageResult.SuccesPassage : pd.errors.get(0));
                        }
                    }
                }
            }
        }
        return false;
    }

    /**
     * Добавляет событие контроля в БД
     *
     * @param pd
     * @param passageResult
     */
    private void addControlEvent(@NonNull PD pd, @NonNull PassageResult passageResult) {
        if (!pd.isReadyToAddControlEvent()) {
            Logger.info(TAG, "Пропускаем создание события контроля");
            return;
        }
        if (readForTransferParams != null) {
            // Если активити запущена с флагом transfer значит не надо создавать событие в любом случае
            Logger.info(TAG, "Пропускаем создание события контроля, transfer: true");
            return;
        }
        if (!needCreateControlEventChecker.check(pd)) {
            // Проверяем необходимость создания события контроля
            Logger.info(TAG, "Пропускаем создание события контроля, needCreateControlEvent: false");
            return;
        }
        // добавим событие контроля в бд

        int smartCardStopListReasonCode = 0;
        BscInformation bscInformation = pd.getBscInformation();
        SmartCard smartCard = null;
        if (bscInformation != null) {
            smartCard = new SmartCardBuilder().setBscInformation(bscInformation).build();
            if (pd.getPassageMark() != null)
                smartCard.setUsageCount(pd.getPassageMark().getCounterCard());
            android.util.Pair<SmartCardStopListItem, String> stopItemResult = new BscInformationChecker(
                    bscInformation,
                    nsiVersionManager,
                    smartCardCancellationReasonRepository).getStopListItem(true);
            if (stopItemResult != null)
                smartCardStopListReasonCode = stopItemResult.first.getReasonCode();
        }

        ShiftEventDao shiftDao = Globals.getInstance().getLocalDaoSession()
                .getShiftEventDao();
        ShiftEvent shift = shiftDao.getLastShiftEvent(ShiftEvent.ShiftProgressStatus.FINISHED_STATUSES);
        if (shift == null || ShiftEvent.Status.ENDED.equals(shift.getStatus())) {
            throw new IllegalStateException("Shift is null or closed");
        }

        Tariff tariff = pd.getTariff();
        Preconditions.checkNotNull(tariff, "Tariff is null");

        // дата начала действия ПД должна быть с начала дня
        Calendar calendar = DateUtils.getStartOfDay(new Date((pd.getSaleDateInSecond() + pd.term * GlobalConstants.SECOND_IN_DAY) * 1000));
        Date startPdTime = calendar.getTime();

        // дата окончания = дата начала + количество дней действия

        final TicketType ticketType = tariff.getTicketType(globals.getNsiDaoSession());
        int validityDays = pdValidityPeriodCalculator.calcValidityPeriod(startPdTime, pd.wayType, ticketType, nsiVersionManager.getCurrentNsiVersionId());

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
                    .setWayType(pd.wayType)
                    .setSaleTime(pd.getSaleDate())
                    .setTicketTypeCode(tariff.getTicketTypeCode())
                    .setTicketCategoryCode(ticketType.getTicketCategoryCode())
                    .setStartDayOffset(pd.term)
                    .setValidFromDate(startPdTime)
                    .setValidTillDate(endTimePD)
                    .setType(ticketType.getExpressTicketTypeCode())
                    .setDepartureStationCode(tariff.getStationDepartureCode())
                    .setDestinationStationCode(tariff.getStationDestinationCode())
                    .setTicketTypeShortName(ticketType.getShortName())
                    .setTariffCode(Long.valueOf(tariff.getCode()))
                    .build();

            Dagger.appComponent().ticketControlEventCreator()
                    .setPd(pd)
                    .setPassageResult(passageResult)
                    .setTicketEventBase(ticketEventBase)
                    .setSmartCardStopListReasonCode(smartCardStopListReasonCode)
                    .setCountTripsPdControlData(countTripsPdControlData.get(pd))
                    .create();

            getLocalDaoSession().setTransactionSuccessful();
        } finally {
            getLocalDaoSession().endTransaction();
        }
    }

    private void onSellNewPdBtnClicked() {
        Logger.info(TAG, "onSellNewPdBtnClicked");
        lockViews();
        //добавим событие в БД
        for (PD pd : legacyPdList) {
            if (pd != null) {
                addControlEvent(pd, PassageResult.RouteNotFound);
            }
        }
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

    private void onSaleSurchargeBtnClicked(@NonNull PD legacyPd) {
        // Добавляем событие контроля в БД
        lockViews();
        addControlEvent(legacyPd, PassageResult.BannedTrainType);
        // Запускаем экран оформления доплаты
        ExtraPaymentParams extraPaymentParams = ExtraPaymentParamsBuilder.from(legacyPd);
        Navigator.navigateToExtraPaymentActivity(this, extraPaymentParams);
        // Закрываем экран с ПД
        finish();
    }

    private void setHardwareButtonsEnabled(boolean value) {
        isHardwareButtonsEnabled = value;
    }

    // Вызов startReadPd с этого экрана в итоге плодить еще одну такую же активити
    // поэтому в стеке получается много ненужных одинаковых активити, вызываем финишь
    // чтобы предотвратить эту ситуацию, см. http://agile.srvdev.ru/browse/CPPKPP-34869
    @Override
    public void startReadPd(ReaderType readerType, ReadForTransferParams readForTransferParams) {
        super.startReadPd(readerType, this.readForTransferParams);
        finish();
    }

    private final SimplePdActivityLogic.Callback simplePdFragmentCallback = new SimplePdActivityLogic.Callback() {
        @Override
        public void onSaleSurchargeBtnClicked(@NonNull PD legacyPd) {
            RfidResultActivity.this.onSaleSurchargeBtnClicked(legacyPd);
        }

        @Override
        public void setHardwareButtonsEnabled(boolean value) {
            RfidResultActivity.this.setHardwareButtonsEnabled(value);
        }

        @Override
        public void onSaleTransferBtnClicked(@NonNull PD legacyPd) {
            Navigator.navigateToTransferSaleActivity(RfidResultActivity.this, TransferSaleParams.Builder.fromPD(legacyPd, readForTransferParams));
            finish();
        }

        @Override
        public void onPdValidBtnClicked(@NonNull PD legacyPd) {
            lockViews();
            addControlEvent(legacyPd, PassageResult.SuccesPassage);
        }

        @Override
        public void onPdNotValidBtnClicked(@NonNull PD legacyPd) {
            lockViews();
            addControlEvent(legacyPd, PassageResult.RouteNotFound);
        }
    };

    private final CountTripsPdActivityLogic.Callback countTripsFragmentCallback = new CountTripsPdActivityLogic.Callback() {
        @Override
        public void onSaleSurchargeBtnClicked(@NonNull PD legacyPd) {
            RfidResultActivity.this.onSaleSurchargeBtnClicked(legacyPd);
        }

        @Override
        public void onPassageMarkChanged(PassageMark passageMark) {
            //пробросим данные в фрагмент Информация о БСК
            cardInfoFragment.onPassageMarkChanged(passageMark);
        }

        @Override
        public void onPassageMarkChecked() {
            passageMarkChecked = true;
        }

        @Override
        public void setHardwareButtonsEnabled(boolean value) {
            RfidResultActivity.this.setHardwareButtonsEnabled(value);
        }

        @Override
        public void onPdNotValidBtnClicked(@NonNull PD legacyPd) {
            lockViews();
            addControlEvent(legacyPd, PassageResult.RouteNotFound);
        }

        @Override
        public void onCountTripsPdControlDataChanged(@NonNull PD legacyPd, @NonNull CountTripsPdControlData pdControlData) {
            countTripsPdControlData.put(legacyPd, pdControlData);
        }
    };

    private final ServiceFeePdFragment.Callback serviceFeePdFragmentCallback = new ServiceFeePdFragment.Callback() {
        @Override
        public void setHardwareButtonsEnabled(boolean value) {
            RfidResultActivity.this.setHardwareButtonsEnabled(value);
        }
    };

}
