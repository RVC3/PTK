package ru.ppr.cppk.repeal;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import ru.ppr.core.dataCarrier.pd.PdVersion;
import ru.ppr.core.ui.widget.SimpleLseView;
import ru.ppr.cppk.ErrorActivity;
import ru.ppr.cppk.R;
import ru.ppr.cppk.dataCarrier.entity.PD;
import ru.ppr.cppk.dataCarrier.pd.check.control.PassageResult;
import ru.ppr.cppk.db.LocalDaoSession;
import ru.ppr.cppk.di.Dagger;
import ru.ppr.cppk.entity.event.base34.CPPKTicketSales;
import ru.ppr.cppk.entity.event.base34.TicketEventBase;
import ru.ppr.cppk.entity.event.base34.TicketSaleReturnEventBase;
import ru.ppr.cppk.entity.event.model.SmartCard;
import ru.ppr.cppk.entity.settings.PrivateSettings;
import ru.ppr.cppk.helpers.DeferredActionHandler;
import ru.ppr.cppk.localdb.model.BankTransactionEvent;
import ru.ppr.cppk.logic.TicketTapeChecker;
import ru.ppr.cppk.logic.pd.PdSaleSupportedChecker;
import ru.ppr.cppk.managers.NsiVersionManager;
import ru.ppr.cppk.systembar.SystemBarActivity;
import ru.ppr.cppk.ui.activity.base.ActivityModule;
import ru.ppr.cppk.ui.activity.base.Navigator;
import ru.ppr.cppk.ui.activity.pdrepeal.PdRepealParams;
import ru.ppr.cppk.ui.adapter.base.OnItemSelectedListenerAdapter;
import ru.ppr.cppk.ui.dialog.SimpleDialog;
import ru.ppr.cppk.ui.widget.HamburgerSpinner;
import ru.ppr.cppk.utils.adapters.RepealReasonAdapter;
import ru.ppr.logger.Logger;
import ru.ppr.nsi.NsiDaoSession;
import ru.ppr.nsi.entity.RepealReason;

/**
 * Экран подробного представления аннулирования ПД.
 */
public class RepealFinishActivity extends SystemBarActivity implements RepealPdFragment.OnFragmentInteractionListener {

    private static final String TAG = Logger.makeLogTag(RepealFinishActivity.class);

    //region Теги для фрагментов
    private static final String NO_REASONS_F_TAG = "NO_REASONS_F_TAG";
    private static final String NO_POS_F_TAG = "NO_POS_F_TAG";
    private static final String DELETE_FROM_CARD_CONFIRM_F_TAG = "DELETE_FROM_CARD_CONFIRM_F_TAG";
    private static final String NOT_LAST_PD_F_TAG = "NOT_LAST_PD_F_TAG";
    private static final String REPEAL_ERROR_F_TAG = "REPEAL_ERROR_F_TAG";
    private static final String WARNING_TIME_F_TAG = "WARNING_TIME_F_TAG";
    //endregion

    // EXTRAS
    private static final String EXTRA_SALE_EVENT_ID = "EXTRA_SALE_EVENT_ID";
    private static final String EXTRA_PD_LIST = "EXTRA_PD_LIST";

    // region Di
    private RepealFinishComponent component;
    @Inject
    NsiDaoSession nsiDaoSession;
    @Inject
    LocalDaoSession localDaoSession;
    @Inject
    NsiVersionManager nsiVersionManager;
    @Inject
    PdSaleSupportedChecker pdSaleSupportedChecker;
    @Inject
    PrivateSettings privateSettings;
    @Inject
    TicketTapeChecker ticketTapeChecker;
    // endregion
    // region Views
    private HamburgerSpinner reasonRepealSpinner = null;
    private SimpleLseView simpleLseView;
    //endregion
    // region Other
    private RepealReasonAdapter adapter = null;
    /**
     * Событие продажи аннулируемого ПД
     */
    private CPPKTicketSales event = null;
    private final DeferredActionHandler deferredActionHandler = new DeferredActionHandler();
    //endregion

    public static Intent getCallingIntent(Context context, List<PD> pdList, long id) {
        Intent intent = new Intent(context, RepealFinishActivity.class);
        intent.putParcelableArrayListExtra(EXTRA_PD_LIST, (ArrayList<? extends Parcelable>) pdList);
        intent.putExtra(EXTRA_SALE_EVENT_ID, id);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        component = DaggerRepealFinishComponent
                .builder()
                .appComponent(Dagger.appComponent())
                .activityModule(new ActivityModule(this))
                .build();
        component.inject(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.repeal_finish_activity);

        // Отказываемся от реакции на автозакыртие смены
        resetRegisterReceiver();

        // Получаем список возможных причин аннулирования из НСИ
        List<RepealReason> reasons = nsiDaoSession.getSmartCardCancellationReasonDao().getRepailReasons(nsiVersionManager.getCurrentNsiVersionId());
        if (reasons.isEmpty()) {
            showNoReasonsErrorDialog();
            return;
        }

        reasonRepealSpinner = (HamburgerSpinner) findViewById(R.id.repail_reason_spinner);
        adapter = new RepealReasonAdapter(this, reasons, android.R.layout.simple_dropdown_item_1line);
        reasonRepealSpinner.setAdapter(adapter);
        reasonRepealSpinner.setOnItemSelectedListener(reasonRepealSpinnerOnItemSelectedListener);
        /////////////////////////////////////////////
        simpleLseView = (SimpleLseView) findViewById(R.id.simpleLseView);
        /////////////////////////////////////////////
        List<PD> pdList = getIntent().getParcelableArrayListExtra(EXTRA_PD_LIST);
        if (pdList != null) {
            setupViewByPdList(pdList);
        } else {
            long saleEventId = getIntent().getLongExtra(EXTRA_SALE_EVENT_ID, -1);
            setupViewBySaleEventId(saleEventId);
        }
    }

    /**
     * Слушатель выбора причины невалидности ПД в списке.
     */
    private AdapterView.OnItemSelectedListener reasonRepealSpinnerOnItemSelectedListener = new OnItemSelectedListenerAdapter() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            adapter.setSelectedPosition(position);
        }
    };

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

    /**
     * Отображает диалог об отсутствии причин аннлирования ПД в НСИ.
     */
    private void showNoReasonsErrorDialog() {
        SimpleDialog simpleDialog = SimpleDialog.newInstance(null,
                getString(R.string.repeal_error_no_reason),
                getString(R.string.dialog_close),
                null,
                LinearLayout.HORIZONTAL,
                0);
        simpleDialog.setDialogPositiveBtnClickListener((dialog, dialogId) -> finish());
        simpleDialog.setCancelable(false);
        simpleDialog.show(getFragmentManager(), NO_REASONS_F_TAG);
    }

    /**
     * Устанавливает фрагмент с информацией о ПД.
     * Используется, когда ПД выбран из списка оформленных.
     *
     * @param saleEventId Id события продажи ПД
     */
    private void setupViewBySaleEventId(long saleEventId) {
        if (saleEventId > -1) {
            FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
            Fragment fragment = RepealPdFragment.newInstance(saleEventId, false);
            fragmentTransaction.add(R.id.repail_read_pd_container, fragment);
            fragmentTransaction.commit();
        } else {
            showErrorMessage();
        }
    }

    /**
     * Устанавливает фрагменты с информацией о ПД.
     * Используется, когда данные считаны с карты или с ШК.
     *
     * @param pdList Список ПД с карты/ШК
     */
    private void setupViewByPdList(@NonNull List<PD> pdList) {
        if (pdList.isEmpty()) {
            // Если список ПД пустой, ничего не показываем в UI
            Logger.info(TAG, "SetupViewByPd: Pd list is empty, return");
            return;
        }

        int countPdFoRepeal = 0;
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        for (PD pd : pdList) {
            // проверяем что считанный ПД соответствует версиям, которые мы
            // можем продавать на ПТК
            // а так же то, что билет проходит все проверки удачно
            // если эти условия не выполнены, то мы не можем аннулирвоать билет
            // и соответственно его не отображаем
            PdVersion pdVersion = PdVersion.getByCode(pd.versionPD);

            if (pdVersion != null && pdSaleSupportedChecker.isSaleSupported(pdVersion)) {

                List<PassageResult> errors = pd.errors;

                boolean isError = false;
                for (PassageResult passageResult : errors) {
                    if (
                            !(passageResult == PassageResult.BannedTrainType ||
                                    passageResult == PassageResult.InvalidStation ||
                                    passageResult == PassageResult.TooEarly ||
                                    passageResult == PassageResult.WeekendOnly ||
                                    passageResult == PassageResult.WorkingDayOnly)
                            ) {
                        Logger.info(TAG, "SetupViewByPd: Попытка аннулировать ПД с ошибкой валидации: " + passageResult.toString());
                        isError = true;
                        break;
                    }
                }

                //если билет валиден или невалиден по причине некорректной категории поезда
                if (!isError) {
                    CPPKTicketSales cppkTicketSales = localDaoSession.getCppkTicketSaleDao().findSaleByParam(pd.numberPD, pd.ecpNumberPD, pd.getSaleDate());
                    if (cppkTicketSales != null) {
                        Fragment fragment = RepealPdFragment.newInstance(cppkTicketSales.getId(), pdList.size() == 2);
                        fragmentTransaction.add(R.id.repail_read_pd_container, fragment);
                        countPdFoRepeal++;
                    }
                }
            } else {
                Logger.info(TAG, "SetupViewByPd: Попытка аннулировать билет невозможной версии (" + pd.versionPD + "). Показываем сообщение об ошибке");
            }
        }
        fragmentTransaction.commit();

        if (countPdFoRepeal == 0)
            showErrorMessage();
    }

    /**
     * Показывает сообщение о невозможности аннулирования ПД.
     */
    private void showErrorMessage() {
        Intent intent = new Intent(this, ErrorActivity.class);
        Bundle bundle = new Bundle();
        bundle.putInt(ErrorActivity.TYPE_ERROR, ErrorActivity.PD_FOR_REPAIL_UNCORRECT);
        intent.putExtras(bundle);
        startActivity(intent);
        finish();
    }

    @Override
    public void performRepeal(CPPKTicketSales salesReturnsEvent) {
        event = salesReturnsEvent;

        TicketSaleReturnEventBase ticketSaleReturnEventBase = localDaoSession.getTicketSaleReturnEventBaseDao().load(salesReturnsEvent.getTicketSaleReturnEventBaseId());
        BankTransactionEvent bankTransactionEvent = localDaoSession.getBankTransactionDao().load(ticketSaleReturnEventBase.getBankTransactionEventId());
        if (bankTransactionEvent != null) {
            // Если ПД был продан по безналу
            if (privateSettings.isPosEnabled()) {
                // Если допустима работа с банковским терминалом
                // Запускаем экран аннлирования ПД
                repeal(salesReturnsEvent);
            } else {
                // Отображаем сообщение о невозможности использования POS-теминала
                showDialogPosTerminalNotAvailable();
            }
        } else {
            // Если ПД был продан за наличные
            TicketEventBase ticketEventBase = localDaoSession.getTicketEventBaseDao().load(ticketSaleReturnEventBase.getTicketEventBaseId());
            SmartCard smartCard = localDaoSession.getSmartCardDao().load(ticketEventBase.getSmartCardId());
            if (smartCard != null) {
                // Если ПД был записан на карту
                // Спрашиваем подтверждения у пользовтаеля об удалении ПД с карты
                showDeleteFromCardConfirmDialog();
            } else {
                // Запускаем процедуру аннулирования ПД
                repeal(salesReturnsEvent);
            }
        }
    }

    private void repeal(@NonNull CPPKTicketSales salesReturnsEvent) {
        PdRepealParams pdRepealParams = new PdRepealParams();
        pdRepealParams.setPdSaleEventId(salesReturnsEvent.getId());
        pdRepealParams.setRepealReason(((RepealReason) reasonRepealSpinner.getSelectedItem()).getReasonRepeal());
        Navigator.navigateToPdRepealActivity(this, pdRepealParams);
        finish();
    }

    /**
     * Отображает сообщение о невозможности использования POS-теминала.
     */
    private void showDialogPosTerminalNotAvailable() {
        SimpleDialog simpleDialog = SimpleDialog.newInstance(
                null,
                getString(R.string.terminal_is_not_available),
                getString(R.string.btnOk),
                null,
                LinearLayout.VERTICAL,
                0);
        simpleDialog.setDialogPositiveBtnClickListener((dialog, dialogId) -> finish());
        simpleDialog.setCancelable(false);
        simpleDialog.show(getFragmentManager(), NO_POS_F_TAG);
    }

    /**
     * Отображает диалог подтверждения удаления ПД с БСК.
     */
    private void showDeleteFromCardConfirmDialog() {
        SimpleDialog simpleDialog = SimpleDialog.newInstance(null,
                getString(R.string.delete_pd_from_bsc),
                getString(R.string.Yes),
                getString(R.string.No),
                LinearLayout.HORIZONTAL,
                0);
        simpleDialog.setDialogPositiveBtnClickListener((dialog, dialogId) -> repeal(event));
        simpleDialog.setDialogNegativeBtnClickListener((dialog, dialogId) -> finish());
        simpleDialog.setCancelable(false);
        simpleDialog.show(getFragmentManager(), DELETE_FROM_CARD_CONFIRM_F_TAG);
    }

    @Override
    public void setSmartCardInfo(SmartCard smartCard) {
        TextView bscNumber = (TextView) findViewById(R.id.repail_finish_activity_bsc_num);
        bscNumber.setText(smartCard.getOuterNumber());
        TextView bscType = (TextView) findViewById(R.id.repail_finish_activity_bsc_type);
        bscType.setText(smartCard.getType().getAbbreviation());
        View bscLayout = findViewById(R.id.repail_finish_activity_bsc_layout);
        bscLayout.setVisibility(View.VISIBLE);
        bscNumber.setText(smartCard.getOuterNumber());
    }

    @Override
    public void showNotLastRepealPd(@NonNull CPPKTicketSales event) {
        String msg = getString(R.string.repeal_not_last_pd) + "\n" + getString(R.string.repeal_question);
        SimpleDialog simpleDialog = SimpleDialog.newInstance(null,
                msg,
                getString(R.string.Yes),
                getString(R.string.No),
                LinearLayout.HORIZONTAL,
                0);
        simpleDialog.setDialogPositiveBtnClickListener((dialog, dialogId) -> performRepeal(event));
        simpleDialog.setDialogNegativeBtnClickListener((dialog, dialogId) -> finish());
        simpleDialog.setCancelable(false);
        simpleDialog.show(getFragmentManager(), NOT_LAST_PD_F_TAG);
    }

    @Override
    public void showErrorDialog(String errorMessage) {
        SimpleDialog simpleDialog = SimpleDialog.newInstance(getString((R.string.repeal_is_impossible)),
                errorMessage,
                getString(R.string.dialog_close),
                null,
                LinearLayout.VERTICAL,
                0);
        simpleDialog.setDialogPositiveBtnClickListener((dialog, dialogId) -> finish());
        simpleDialog.setCancelable(false);
        simpleDialog.show(getFragmentManager(), REPEAL_ERROR_F_TAG);
    }

    @Override
    public void showWarningTimeDialog(CPPKTicketSales event) {
        SimpleDialog simpleDialog = SimpleDialog.newInstance(null,
                getString(R.string.repeal_time_end),
                getString(R.string.btnOk),
                null,
                LinearLayout.VERTICAL,
                0);
        simpleDialog.setCancelable(false);
        simpleDialog.show(getFragmentManager(), WARNING_TIME_F_TAG);
    }

    @Override
    public void onBackPressed() {
        if (!simpleLseView.isVisible()) {
            super.onBackPressed();
        }
    }

}
