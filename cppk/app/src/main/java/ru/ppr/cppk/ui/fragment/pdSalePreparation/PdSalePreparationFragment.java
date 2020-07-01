package ru.ppr.cppk.ui.fragment.pdSalePreparation;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import java.math.BigDecimal;
import java.util.List;

import ru.ppr.core.ui.mvp.MvpDelegate;
import ru.ppr.cppk.R;
import ru.ppr.cppk.localdb.model.TicketWayType;
import ru.ppr.cppk.localdb.model.PaymentType;
import ru.ppr.cppk.logic.CriticalNsiVersionDialogDelegate;
import ru.ppr.cppk.model.ETicketDataParams;
import ru.ppr.cppk.model.PdSaleData;
import ru.ppr.cppk.model.PdSaleParams;
import ru.ppr.cppk.model.RemoveExemptionParams;
import ru.ppr.cppk.systembar.FeedbackProgressDialog;
import ru.ppr.cppk.ui.activity.base.Navigator;
import ru.ppr.cppk.ui.activity.enterETicketData.EnterETicketDataActivity;
import ru.ppr.cppk.ui.activity.selectExemption.SelectExemptionActivity;
import ru.ppr.cppk.ui.activity.selectExemption.SelectExemptionParams;
import ru.ppr.cppk.ui.activity.selectExemption.SelectExemptionResult;
import ru.ppr.cppk.ui.adapter.autoCompleteTextView.StationsAdapter;
import ru.ppr.cppk.ui.adapter.spinner.TariffPlanForSaleAdapter;
import ru.ppr.cppk.ui.adapter.spinner.TicketTypeAdapter;
import ru.ppr.cppk.ui.dialog.SimpleDialog;
import ru.ppr.cppk.ui.fragment.base.LegacyMvpFragment;
import ru.ppr.cppk.ui.widget.StationEditText;
import ru.ppr.cppk.utils.validators.CyrillicTextWatcher;
import ru.ppr.logger.Logger;
import ru.ppr.nsi.entity.Station;
import ru.ppr.nsi.entity.TariffPlan;
import ru.ppr.nsi.entity.TicketType;

/**
 * Экран оформления доплаты
 *
 * @author Aleksandr Brazhkin
 */
public class PdSalePreparationFragment extends LegacyMvpFragment implements PdSalePreparationView {

    private static final String TAG = Logger.makeLogTag(PdSalePreparationFragment.class);
    public static final String FRAGMENT_TAG = PdSalePreparationFragment.class.getSimpleName();
    public static final String UNHANDLED_ERROR_OCCURRED_DIALOG_TAG = "UNHANDLED_ERROR_OCCURRED_DIALOG_TAG";
    // Dialog tags
    private static final String DIALOG_CRITICAL_NSI_CLOSE = "DIALOG_CRITICAL_NSI_CLOSE";

    private static final int STATION_IN_EDIT_MODE_NONE = 0;
    private static final int STATION_IN_EDIT_MODE_DEP = 1;
    private static final int STATION_IN_EDIT_MODE_DEST = 2;
    // SS
    private String SS_SELECTED_TARIFF_PLAN_POS = "SS_SELECTED_TARIFF_PLAN_POS";
    private String SS_SELECTED_TICKET_TYPE_POS = "SS_SELECTED_TICKET_TYPE_POS";
    // RC
    private static final int RC_E_TICKET = 101;
    private static final int RC_SELECT_EXEMPTION = 102;

    public static PdSalePreparationFragment newInstance() {
        return new PdSalePreparationFragment();
    }

    /**
     * Di
     */
    private PdSalePreparationDi di = new PdSalePreparationDi(di());

    private InteractionListener mInteractionListener;

    // Views
    private ProgressDialog mProgressDialog;
    private TextView title;
    private StationEditText departureStationEditText;
    private StationEditText destinationStationEditText;
    private TextView transitStationName;
    private Spinner tariffPlanSpinner;
    private Button directionBtn;
    private ImageView directionImg;
    private Spinner ticketTypeSpinner;
    private Button incrementPdCountBtn;
    private Button decrementPdCountBtn;
    private TextView pdCount;
    private ViewGroup exemptionGroup;
    private TextView exemptionLabel;
    private TextView exemptionValue;
    private ViewGroup feeGroup;
    private CheckBox feeCheckBox;
    private TextView feeValue;
    private ViewGroup costGroup;
    private TextView onePdCostLabel;
    private TextView onePdCostValue;
    private TextView totalCostValue;
    private RadioGroup paymentTypeGroup;
    private RadioButton paymentTypeCash;
    private RadioButton paymentTypeCard;
    private Button sendETicketBtn;
    private View saleButtonsLayout;
    private Button writeToCardBtn;
    private Button printPdBtn;
    private Button processBtn;
    private View blockingView;
    ////////////////////////////////////////////////
    // Adapters, etc.
    private StationsAdapter departureStationsAdapter;
    private StationsAdapter destinationStationsAdapter;
    private TariffPlanForSaleAdapter mTariffPlansAdapter;
    private TicketTypeAdapter mTicketTypesAdapter;
    ////////////////////////////////////////////////
    private int stationInEditMode = STATION_IN_EDIT_MODE_NONE;
    private int selectedTariffPlanPosition = 0;
    private int selectedTicketTypePosition = 0;
    ////////////////////////////////////////////////
    private boolean incrementPdCountBtnEnabled;
    private boolean decrementPdCountBtnEnabled;
    private boolean pdCountLayoutEnabled;
    ////////////////////////////////////////////////
    //region Other
    private PdSalePreparationPresenter presenter;
    //endregion

    private boolean isProcessing = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        departureStationsAdapter = new StationsAdapter(getActivity());
        departureStationsAdapter.setFilter(depStationAdapterFilter);

        destinationStationsAdapter = new StationsAdapter(getActivity());
        destinationStationsAdapter.setFilter(destStationAdapterFilter);

        mTariffPlansAdapter = new TariffPlanForSaleAdapter(getActivity(), android.R.layout.simple_spinner_dropdown_item);

        mTicketTypesAdapter = new TicketTypeAdapter(getActivity());

        mProgressDialog = new FeedbackProgressDialog(getActivity());
        mProgressDialog.setCancelable(false);

        if (savedInstanceState != null) {
            selectedTariffPlanPosition = savedInstanceState.getInt(SS_SELECTED_TARIFF_PLAN_POS);
            selectedTicketTypePosition = savedInstanceState.getInt(SS_SELECTED_TICKET_TYPE_POS);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_pd_sale_preparation, container, false);
        //////////////////////////////////////////////////////////////////////////////////////
        title = (TextView) view.findViewById(R.id.title);
        //////////////////////////////////////////////////////////////////////////////////////
        departureStationEditText = (StationEditText) view.findViewById(R.id.departureStationEditText);
        departureStationEditText.addTextChangedListener(new CyrillicTextWatcher(departureStationEditText));
        departureStationEditText.setOnItemClickListener(depStationOnItemClickListener);
        departureStationEditText.setOnBackListener(stationBackListener);
        departureStationEditText.setOnEditorActionListener(depStationEditorListener);
        departureStationEditText.setOnFocusChangeListener(depStationViewFocusChangeListener);
        departureStationEditText.setOnClickListener(stationClickListener);
        //////////////////////////////////////////////////////////////////////////////////////
        destinationStationEditText = (StationEditText) view.findViewById(R.id.destinationStationEditText);
        destinationStationEditText.addTextChangedListener(new CyrillicTextWatcher(destinationStationEditText));
        destinationStationEditText.setOnItemClickListener(destStationOnItemClickListener);
        destinationStationEditText.setOnBackListener(stationBackListener);
        destinationStationEditText.setOnEditorActionListener(destStationEditorListener);
        destinationStationEditText.setOnFocusChangeListener(destStationViewFocusChangeListener);
        destinationStationEditText.setOnClickListener(stationClickListener);
        //////////////////////////////////////////////////////////////////////////////////////
        transitStationName = (TextView) view.findViewById(R.id.transitStationName);
        //////////////////////////////////////////////////////////////////////////////////////
        tariffPlanSpinner = (Spinner) view.findViewById(R.id.tariffPlanSpinner);
        tariffPlanSpinner.setAdapter(mTariffPlansAdapter);
        tariffPlanSpinner.setOnTouchListener(tariffPlanSpinnerOnTouchListener);
        tariffPlanSpinner.setOnItemSelectedListener(tariffPlanSpinnerOnItemSelectedListener);
        //////////////////////////////////////////////////////////////////////////////////////
        directionBtn = (Button) view.findViewById(R.id.directionBtn);
        directionBtn.setOnClickListener(v -> presenter.onDirectionBtnClicked());
        directionImg = (ImageView) view.findViewById(R.id.directionImg);
        //////////////////////////////////////////////////////////////////////////////////////
        ticketTypeSpinner = (Spinner) view.findViewById(R.id.ticketTypeSpinner);
        ticketTypeSpinner.setAdapter(mTicketTypesAdapter);
        ticketTypeSpinner.setOnTouchListener(ticketTypeSpinnerOnTouchListener);
        ticketTypeSpinner.setOnItemSelectedListener(ticketTypeSpinnerOnItemSelectedListener);
        //////////////////////////////////////////////////////////////////////////////////////
        incrementPdCountBtn = (Button) view.findViewById(R.id.incrementPdCountBtn);
        incrementPdCountBtn.setOnClickListener(v -> presenter.onIncrementPdCountBtnClicked());
        decrementPdCountBtn = (Button) view.findViewById(R.id.decrementPdCountBtn);
        decrementPdCountBtn.setOnClickListener(v -> presenter.onDecrementPdCountBtnClicked());
        pdCount = (TextView) view.findViewById(R.id.pdCount);
        //////////////////////////////////////////////////////////////////////////////////////
        exemptionGroup = (ViewGroup) view.findViewById(R.id.exemptionGroup);
        exemptionLabel = (TextView) view.findViewById(R.id.exemptionLabel);
        exemptionGroup.setOnClickListener(v -> {
            cancelEditStation();
            presenter.onExemptionClicked();
        });
        exemptionValue = (TextView) view.findViewById(R.id.exemptionValue);
        feeGroup = (ViewGroup) view.findViewById(R.id.feeGroup);
        feeCheckBox = (CheckBox) view.findViewById(R.id.feeCheckBox);
        feeCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            cancelEditStation();
            presenter.onFeeCheckedChanged(isChecked);
        });
        feeValue = (TextView) view.findViewById(R.id.feeValue);
        //////////////////////////////////////////////////////////////////////////////////////
        costGroup = (ViewGroup) view.findViewById(R.id.costGroup);
        onePdCostLabel = (TextView) view.findViewById(R.id.onePdCostLabel);
        onePdCostValue = (TextView) view.findViewById(R.id.onePdCostValue);
        totalCostValue = (TextView) view.findViewById(R.id.totalCostValue);
        //////////////////////////////////////////////////////////////////////////////////////
        paymentTypeGroup = (RadioGroup) view.findViewById(R.id.paymentTypeGroup);
        paymentTypeGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.paymentTypeCard) {
                presenter.onPaymentTypeChecked(PaymentType.INDIVIDUAL_BANK_CARD);
            } else {
                presenter.onPaymentTypeChecked(PaymentType.INDIVIDUAL_CASH);
            }
        });
        paymentTypeCash = (RadioButton) view.findViewById(R.id.paymentTypeCash);
        paymentTypeCard = (RadioButton) view.findViewById(R.id.paymentTypeCard);
        //////////////////////////////////////////////////////////////////////////////////////
        sendETicketBtn = (Button) view.findViewById(R.id.sendETicketBtn);
        sendETicketBtn.setOnClickListener(v -> presenter.onSendETicketBtnClick());
        //////////////////////////////////////////////////////////////////////////////////////
        saleButtonsLayout = view.findViewById(R.id.saleButtonsLayout);
        writeToCardBtn = (Button) view.findViewById(R.id.writeToCardBtn);
        writeToCardBtn.setOnClickListener(v -> presenter.onWriteToCardBtnClicked());
        printPdBtn = (Button) view.findViewById(R.id.printPdBtn);
        printPdBtn.setOnClickListener(v -> presenter.onPrintPdBtnClicked());
        processBtn = (Button) view.findViewById(R.id.processBtn);
        processBtn.setOnClickListener(v -> presenter.onProcessBtnClicked());
        //////////////////////////////////////////////////////////////////////////////////////
        blockingView = view.findViewById(R.id.blockingView);
        //////////////////////////////////////////////////////////////////////////////////////

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Устанавливаем адаптер в onResume, чтобы избежать срабатывания Filter.performFiltering() в onRestoreInstanceState()
        departureStationEditText.setAdapter(departureStationsAdapter);
        destinationStationEditText.setAdapter(destinationStationsAdapter);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case RC_SELECT_EXEMPTION:
                SelectExemptionResult selectExemptionResult = SelectExemptionActivity.getResultFromIntent(resultCode, data);
                if (selectExemptionResult != null) {
                    presenter.onExemptionSelected(selectExemptionResult);
                }
                break;
            case RC_E_TICKET:
                if (resultCode == Activity.RESULT_OK && data != null) {
                    presenter.onETicketDataSelected(data.getParcelableExtra(EnterETicketDataActivity.EXTRA_E_TICKET_PARAMS_RESULT));
                }
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);

        }
    }

    @Override
    public void onPause() {
        super.onPause();
        departureStationEditText.setAdapter(null);
        destinationStationEditText.setAdapter(null);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(SS_SELECTED_TARIFF_PLAN_POS, selectedTariffPlanPosition);
        outState.putInt(SS_SELECTED_TICKET_TYPE_POS, selectedTicketTypePosition);
    }

    public void setInteractionListener(InteractionListener interactionListener) {
        mInteractionListener = interactionListener;
    }

    @Override
    public void init(MvpDelegate parent, String id) {
        super.init(parent, id);
        presenter = getMvpDelegate().getPresenter(PdSalePreparationPresenter::new, PdSalePreparationPresenter.class);
    }

    public void initialize(PdSaleParams pdSaleParams, PdSaleData pdSaleData) {
        presenter.bindInteractionListener(fineSalePreparationInteractionListener);
        presenter.initialize(pdSaleParams,
                pdSaleData,
                di.uiThread(),
                di.nsiDaoSession(),
                di.printerManager(),
                di.privateSettings(),
                di.pdSaleRestrictionsParamsBuilder(),
                di.ticketStorageTypeToTicketTypeChecker(),
                di.pdSaleEnvFactory(),
                di.nsiVersionManager(),
                di.criticalNsiVersionChecker(),
                di.trainCategoryRepository(),
                di.stationRepository(),
                di.exemptionRepository(),
                di.exemptionGroupRepository()
        );
    }

    @Override
    public void setCostGroupVisible(boolean visible) {
        costGroup.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    @Override
    public void setPaymentTypeEnabled(boolean enabled) {
        paymentTypeCash.setEnabled(enabled);
        paymentTypeCard.setEnabled(enabled);
    }

    @Override
    public void setPaymentTypeVisible(boolean visible) {
        paymentTypeGroup.setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
    }

    @Override
    public void setSendETicketBtnVisible(boolean visible) {
        sendETicketBtn.setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
    }

    @Override
    public void setFeeLabel(FeeLabel feeLabel) {
        if (feeLabel == FeeLabel.AT_DESTINATION_STATION) {
            feeCheckBox.setText(R.string.pd_sale_preparation_fee_type_at_destination_station);
        } else {
            feeCheckBox.setText(R.string.pd_sale_preparation_fee_type_in_train);
        }
    }

    @Override
    public void setDirection(TicketWayType ticketWayType) {
        switch (ticketWayType) {
            case OneWay:
                directionBtn.setText(R.string.pd_sale_preparation_direction_btn_there);
                directionImg.setImageResource(R.drawable.there_direction);
                break;

            case TwoWay:
                directionBtn.setText(R.string.pd_sale_preparation_direction_btn_there_back);
                directionImg.setImageResource(R.drawable.there_back_direction);
                break;

            default:
                break;
        }
    }

    @Override
    public void showProgress() {
        isProcessing = true;
        blockingView.setVisibility(View.VISIBLE);
        mProgressDialog.show();
    }

    @Override
    public void hideProgress() {
        blockingView.setVisibility(View.GONE);
        mProgressDialog.dismiss();
        // Костыль, чтоб успели за это время сработать асинхронные AdapterView.OnItemSelectedListener
        getView().postDelayed(() -> isProcessing = false, 64);
    }

    @Override
    public void setTariffPlans(List<TariffPlan> tariffPlans) {
        mTariffPlansAdapter.setItems(tariffPlans);
    }

    @Override
    public void setTicketTypes(List<TicketType> ticketTypes) {
        mTicketTypesAdapter.setItems(ticketTypes);
    }

    @Override
    public void setSelectedTariffPlanPosition(int position) {
        // В нулевой позиции мнимый элемент
        // https://aj.srvdev.ru/browse/CPPKPP-28012
        int uiPosition = position + 1;
        Logger.trace(TAG, "setSelectedTariffPlanPosition, uiPosition = " + uiPosition);
        mTariffPlansAdapter.setSelectedPosition(uiPosition);
        tariffPlanSpinner.setSelection(uiPosition);
        selectedTariffPlanPosition = uiPosition;
    }

    @Override
    public void setSelectedTicketTypePosition(int position) {
        // В нулевой позиции мнимый элемент
        // https://aj.srvdev.ru/browse/CPPKPP-28012
        int uiPosition = position + 1;
        Logger.trace(TAG, "setSelectedTicketTypePosition, uiPosition = " + uiPosition);
        mTicketTypesAdapter.setSelectedPosition(uiPosition);
        ticketTypeSpinner.setSelection(uiPosition);
        selectedTicketTypePosition = uiPosition;
    }

    @Override
    public void setPaymentType(PaymentType paymentType) {
        paymentTypeGroup.check(paymentType == PaymentType.INDIVIDUAL_BANK_CARD ? R.id.paymentTypeCard : R.id.paymentTypeCash);
    }

    @Override
    public void setOnePdCost(BigDecimal cost) {
        onePdCostValue.setText(getString(R.string.pd_sale_preparation_one_pd_cost_value, cost));
    }

    @Override
    public void setTotalCost(BigDecimal cost) {
        totalCostValue.setText(getString(R.string.pd_sale_preparation_total_pd_cost_value, cost));
    }

    @Override
    public void setFeeChecked(boolean checked) {
        feeCheckBox.setChecked(checked);
    }

    @Override
    public void setFeeEnabled(boolean enabled) {
        feeGroup.setEnabled(enabled);
        feeCheckBox.setEnabled(enabled);
        feeValue.setEnabled(enabled);
    }

    @Override
    public void setFeeValue(@Nullable BigDecimal feeValue) {
        if (feeValue == null) {
            this.feeValue.setText(R.string.pd_sale_preparation_fee_value_no);
        } else {
            this.feeValue.setText(getString(R.string.pd_sale_preparation_fee_value, feeValue));
        }
    }

    @Override
    public void setOnePdCostLabel(TicketCategoryLabel label) {
        if (label == TicketCategoryLabel.BAGGAGE) {
            onePdCostLabel.setText(R.string.pd_sale_preparation_one_pd_cost_baggage);
        } else {
            onePdCostLabel.setText(R.string.pd_sale_preparation_one_pd_cost_single);
        }
    }

    @Override
    public void setTitle(TicketCategoryLabel label) {
        if (label == TicketCategoryLabel.BAGGAGE) {
            title.setText(R.string.pd_sale_preparation_baggage_title);
        } else {
            title.setText(R.string.pd_sale_preparation_single_title);
        }
    }

    @Override
    public void setPdCount(int count) {
        pdCount.setText(String.valueOf(count));
    }

    @Override
    public void setPdCountLayoutEnabled(boolean enabled) {
        pdCountLayoutEnabled = enabled;
        pdCount.setEnabled(pdCountLayoutEnabled);
        decrementPdCountBtn.setEnabled(pdCountLayoutEnabled && decrementPdCountBtnEnabled);
        incrementPdCountBtn.setEnabled(pdCountLayoutEnabled && incrementPdCountBtnEnabled);
    }

    @Override
    public void setExemptionValue(int exemptionCode, int percentage) {
        if (exemptionCode > 0) {
            exemptionValue.setText(getString(R.string.pd_sale_preparation_exemption_value, exemptionCode, percentage));
        } else {
            exemptionValue.setText(getString(R.string.pd_sale_preparation_exemption_value_no));
        }
    }

    @Override
    public void showEdsFailedError() {
        SimpleDialog simpleDialog = SimpleDialog.newInstance(
                null,
                getString(R.string.pd_sale_preparation_sft_error_msg),
                getString(R.string.pd_sale_preparation_sft_close_btn),
                null,
                LinearLayout.VERTICAL,
                -1
        );
        simpleDialog.setDialogPositiveBtnClickListener((dialog, dialogId) -> {
            presenter.onCheckEdsFailedDialogClosed();
        });
        simpleDialog.setOnCancelListener(dialogInterface -> {
            presenter.onCheckEdsFailedDialogClosed();
        });
        simpleDialog.show(getFragmentManager(), SimpleDialog.FRAGMENT_TAG);
    }

    @Override
    public void setDepartureStationName(String departureStationName) {
        departureStationEditText.setText(departureStationName, false);
    }

    @Override
    public void setDepartureStationEnabled(boolean enabled) {
        departureStationEditText.setEnabled(enabled);
    }

    @Override
    public void setDestinationStationName(String destinationStationName) {
        destinationStationEditText.setText(destinationStationName, false);
    }

    @Override
    public void setDestinationStationEnabled(boolean enabled) {
        destinationStationEditText.setEnabled(enabled);
    }

    @Override
    public void setDepartureStations(List<Station> stations) {
        departureStationsAdapter.setItems(stations);
    }

    @Override
    public void setDestinationStations(List<Station> stations) {
        destinationStationsAdapter.setItems(stations);
    }

    @Override
    public void setSaleButtonsState(SaleButtonsState saleButtonsState) {
        switch (saleButtonsState) {
            case WRITE_AND_PRINT: {
                writeToCardBtn.setVisibility(View.VISIBLE);
                printPdBtn.setVisibility(View.VISIBLE);
                processBtn.setVisibility(View.GONE);
                break;
            }
            case PRINT_ONLY: {
                writeToCardBtn.setVisibility(View.GONE);
                printPdBtn.setVisibility(View.VISIBLE);
                processBtn.setVisibility(View.GONE);
                break;
            }
            case WRITE_ONLY: {
                writeToCardBtn.setVisibility(View.VISIBLE);
                printPdBtn.setVisibility(View.GONE);
                processBtn.setVisibility(View.GONE);
                break;
            }
            case PROCESS: {
                writeToCardBtn.setVisibility(View.GONE);
                printPdBtn.setVisibility(View.GONE);
                processBtn.setVisibility(View.VISIBLE);
                break;
            }
        }
    }

    @Override
    public void setDirectionBtnEnabled(boolean enabled) {
        directionBtn.setEnabled(enabled);
    }

    @Override
    public void setDecrementPdCountBtnEnabled(boolean enabled) {
        decrementPdCountBtnEnabled = enabled;
        decrementPdCountBtn.setEnabled(pdCountLayoutEnabled && decrementPdCountBtnEnabled);
    }

    @Override
    public void setIncrementPdCountBtnEnabled(boolean enabled) {
        incrementPdCountBtnEnabled = enabled;
        incrementPdCountBtn.setEnabled(pdCountLayoutEnabled && incrementPdCountBtnEnabled);
    }

    @Override
    public void setTransitStationName(String stationName) {
        if (stationName == null) {
            transitStationName.setText("");
        } else {
            transitStationName.setText(getString(R.string.pd_sale_preparation_transit_station_value, stationName));
        }
    }

    @Override
    public void showExemptionInDifferentRegionsDeniedError() {
        SimpleDialog simpleDialog = SimpleDialog.newInstance(
                null,
                getString(R.string.pd_sale_preparation_exemption_in_different_regions_denied_error_msg),
                getString(R.string.pd_sale_preparation_exemption_in_different_regions_denied_close_btn),
                null,
                LinearLayout.VERTICAL,
                -1
        );
        simpleDialog.show(getFragmentManager(), SimpleDialog.FRAGMENT_TAG);
    }

    public void showDeniedForSaleOnTicketStorageTypeError(String ticketStorageTypeName) {
        SimpleDialog simpleDialog = SimpleDialog.newInstance(
                null,
                String.format(getString(R.string.pd_sale_preparation_denied_for_print_error_msg), ticketStorageTypeName),
                getString(R.string.pd_sale_preparation_denied_for_print_close_btn),
                null,
                LinearLayout.VERTICAL,
                -1
        );
        simpleDialog.show(getFragmentManager(), SimpleDialog.FRAGMENT_TAG);
    }

    @Override
    public void setUnhandledErrorOccurredDialogVisible(boolean visible) {
        SimpleDialog unhandledErrorOccurredDialog = (SimpleDialog) getFragmentManager().findFragmentByTag(UNHANDLED_ERROR_OCCURRED_DIALOG_TAG);

        if (visible) {
            if (unhandledErrorOccurredDialog == null) {
                unhandledErrorOccurredDialog = SimpleDialog.newInstance(null,
                        getString(R.string.pd_sale_preparation_unhandled_error_occurred_dialog_message),
                        getString(R.string.pd_sale_preparation_unhandled_error_occurred_dialog_positive),
                        null,
                        LinearLayout.HORIZONTAL,
                        0);
                unhandledErrorOccurredDialog.setOnDismissListener(dialog -> presenter.onUnhandledErrorOccurredDialogDismiss());
                unhandledErrorOccurredDialog.show(getFragmentManager(), UNHANDLED_ERROR_OCCURRED_DIALOG_TAG);
            } else {
                unhandledErrorOccurredDialog.setOnDismissListener(dialog -> presenter.onUnhandledErrorOccurredDialogDismiss());
            }
        } else {
            if (unhandledErrorOccurredDialog != null) {
                unhandledErrorOccurredDialog.dismiss();
            }
        }
    }

    @Override
    public void setCriticalNsiBackDialogVisible(boolean visible) {
        SimpleDialog criticalNsiVersionDialog = (SimpleDialog) getFragmentManager().findFragmentByTag(DIALOG_CRITICAL_NSI_CLOSE);

        if (visible) {
            // http://agile.srvdev.ru/browse/CPPKPP-35280
            // (Сделать проверки при разблокировке ПТК и при открытии экрана продажи.)
            // ShiftAlarmManager тут не работает
            CriticalNsiVersionDialogDelegate criticalNsiVersionDialogDelegate = new CriticalNsiVersionDialogDelegate(
                    di.criticalNsiVersionChecker(),
                    getFragmentManager(),
                    getResources(),
                    DIALOG_CRITICAL_NSI_CLOSE);

            if (criticalNsiVersionDialog == null) {
                criticalNsiVersionDialogDelegate.showCriticalNsiCloseDialogIfNeeded(onCriticalNsiBackDialogShownListener);
            } else {
                criticalNsiVersionDialog.setDialogPositiveBtnClickListener(onCriticalNsiBackDialogShownListener);
            }
        } else {
            if (criticalNsiVersionDialog != null) {
                criticalNsiVersionDialog.dismiss();
            }
        }
    }

    @Override
    public void setCriticalNsiCloseShiftDialogVisible(boolean visible) {
        SimpleDialog criticalNsiVersionDialog = (SimpleDialog) getFragmentManager().findFragmentByTag(DIALOG_CRITICAL_NSI_CLOSE);

        if (visible) {
            // http://agile.srvdev.ru/browse/CPPKPP-35280
            // (Сделать проверки при разблокировке ПТК и при открытии экрана продажи.)
            // ShiftAlarmManager тут не работает
            CriticalNsiVersionDialogDelegate criticalNsiVersionDialogDelegate = new CriticalNsiVersionDialogDelegate(
                    di.criticalNsiVersionChecker(),
                    getFragmentManager(),
                    getResources(),
                    DIALOG_CRITICAL_NSI_CLOSE);

            if (criticalNsiVersionDialog == null) {
                criticalNsiVersionDialogDelegate.showCriticalNsiCloseDialogIfNeeded(onCriticalNsiCloseShiftDialogShownListener);
            } else {
                criticalNsiVersionDialog.setDialogPositiveBtnClickListener(onCriticalNsiCloseShiftDialogShownListener);
            }
        } else {
            if (criticalNsiVersionDialog != null) {
                criticalNsiVersionDialog.dismiss();
            }
        }
    }

    private void cancelEditStation() {
        if (stationInEditMode == STATION_IN_EDIT_MODE_NONE) {
            return;
        }
        hideKeyboard();
        if (stationInEditMode == STATION_IN_EDIT_MODE_DEP) {
            presenter.onDepartureStationEditCanceled();
        } else if (stationInEditMode == STATION_IN_EDIT_MODE_DEST) {
            presenter.onDestinationStationEditCanceled();
        }
    }

    /**
     * Скрывает клавиатуру для view
     */
    private void hideKeyboard() {
        InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(getView().getWindowToken(), 0);
        getView().requestFocus();
    }

    public void onExemptionRemoved() {
        presenter.onExemptionRemoved();
    }

    /**
     * Блокировщик открытия выпадающего списка тарифных планов, если в нем нет иных вариантов.
     */
    View.OnTouchListener tariffPlanSpinnerOnTouchListener = new View.OnTouchListener() {

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            for (int i = 0; i < mTariffPlansAdapter.getItems().size(); i++) {
                if (mTariffPlansAdapter.getSelectedPosition() - 1 != i) {
                    return false;
                }
            }
            return true;
        }
    };

    /**
     * Блокировщик открытия выпадающего списка типов ПД, если в нем нет иных вариантов.
     */
    View.OnTouchListener ticketTypeSpinnerOnTouchListener = new View.OnTouchListener() {

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            for (int i = 0; i < mTicketTypesAdapter.getItems().size(); i++) {
                if (mTicketTypesAdapter.getSelectedPosition() - 1 != i) {
                    return false;
                }
            }
            return true;
        }
    };

    /**
     * Обработчик типа билета.
     */
    private final AdapterView.OnItemSelectedListener ticketTypeSpinnerOnItemSelectedListener = new AdapterView.OnItemSelectedListener() {

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            if (!isProcessing) {
                Logger.trace(TAG, "onItemSelected, uiPosition = " + position);
                if (selectedTicketTypePosition == position) {
                    Logger.trace(TAG, "onItemSelected skipped");
                } else {
                    // В нулевой позиции мнимый элемент
                    // https://aj.srvdev.ru/browse/CPPKPP-28012
                    mTicketTypesAdapter.setSelectedPosition(position);
                    selectedTicketTypePosition = position;
                    presenter.onTicketTypeSelected(position - 1);
                }
            } else {
                Logger.trace(TAG, "ticketTypeSpinnerOnItemSelectedListener while processing");
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
            /* NOP */
        }
    };

    /**
     * Обработчик тарифного плана.
     */
    private final AdapterView.OnItemSelectedListener tariffPlanSpinnerOnItemSelectedListener = new AdapterView.OnItemSelectedListener() {

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            if (!isProcessing) {
                Logger.trace(TAG, "onItemSelected, uiPosition = " + position);
                if (selectedTariffPlanPosition == position) {
                    Logger.trace(TAG, "onItemSelected skipped");
                } else {
                    // В нулевой позиции мнимый элемент
                    // https://aj.srvdev.ru/browse/CPPKPP-28012
                    mTariffPlansAdapter.setSelectedPosition(position);
                    selectedTariffPlanPosition = position;
                    presenter.onTariffPlanSelected(position - 1);
                }
            } else {
                Logger.trace(TAG, "tariffPlanSpinnerOnItemSelectedListener while processing");
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
            /* NOP */
        }
    };

    /**
     * Обработчик выбора станции отправления.
     */
    private final AdapterView.OnItemClickListener depStationOnItemClickListener = (parent, view, position, id) -> {
        presenter.onDepartureStationSelected(position);
        hideKeyboard();
    };

    /**
     * Обработчик выбора станции прибытия.
     */
    private final AdapterView.OnItemClickListener destStationOnItemClickListener = (parent, view, position, id) -> {
        presenter.onDestinationStationSelected(position);
        hideKeyboard();
    };


    /**
     * Обработчик нажатия.
     */
    private View.OnClickListener stationClickListener = (v -> {
        ((AutoCompleteTextView) v).showDropDown();
    });

    /**
     * Обработчик отмены выбора станции.
     */
    private final StationEditText.OnBackListener stationBackListener = () -> {
        cancelEditStation();
        return true;
    };

    /**
     * Обработчик набора текста станции отправления.
     */
    private final TextView.OnEditorActionListener depStationEditorListener = (v, actionId, event) -> {
        if (actionId == EditorInfo.IME_ACTION_DONE) {
            // Не реагируем на кнопку "Done"
            return true;
        }
        return false;
    };

    /**
     * Обработчик смены фокуса станции отправления.
     */
    private View.OnFocusChangeListener depStationViewFocusChangeListener = (v, hasFocus) -> {
        if (hasFocus) {
            if (stationInEditMode == STATION_IN_EDIT_MODE_DEST) {
                presenter.onDestinationStationEditCanceled();
            }
            stationInEditMode = STATION_IN_EDIT_MODE_DEP;
            departureStationEditText.setText("", false);
            departureStationEditText.showDropDown();
        }
    };

    /**
     * Обработчик смены фокуса станции назнчения.
     */
    private View.OnFocusChangeListener destStationViewFocusChangeListener = (v, hasFocus) -> {
        if (hasFocus) {
            if (stationInEditMode == STATION_IN_EDIT_MODE_DEP) {
                presenter.onDepartureStationEditCanceled();
            }
            stationInEditMode = STATION_IN_EDIT_MODE_DEST;
            destinationStationEditText.setText("", false);
            destinationStationEditText.showDropDown();
        }
    };

    /**
     * Обработчик набора текста станции прибытия.
     */
    private final TextView.OnEditorActionListener destStationEditorListener = (v, actionId, event) -> {
        if (actionId == EditorInfo.IME_ACTION_DONE) {
            // Не реагируем на кнопку "Done"
            return true;
        }
        return false;
    };

    /**
     * Фильтр для станций отправления.
     *
     * @see Filter
     */
    private final Filter depStationAdapterFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            String text = constraint == null ? "" : constraint.toString();
            List<Station> stations = presenter.onDepartureStationTextChanged(text);
            final FilterResults filterResults = new FilterResults();
            filterResults.values = stations;
            filterResults.count = stations.size();
            return filterResults;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            // nop
            // Презентер напрямую пробрасывает результаты в адаптер
        }
    };

    /**
     * Фильтр для станций прибытия.
     *
     * @see Filter
     */
    private final Filter destStationAdapterFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            String text = constraint == null ? "" : constraint.toString();
            List<Station> stations = presenter.onDestinationStationTextChanged(text);
            final FilterResults filterResults = new FilterResults();
            filterResults.values = stations;
            filterResults.count = stations.size();
            return filterResults;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            // nop
            // Презентер напрямую пробрасывает результаты в адаптер
        }
    };

    private final SimpleDialog.DialogBtnClickListener onCriticalNsiBackDialogShownListener = (dialog, dialogId) -> {
        presenter.onCriticalNsiBackDialogRead();
    };

    private final SimpleDialog.DialogBtnClickListener onCriticalNsiCloseShiftDialogShownListener = (dialog, dialogId) -> {
        presenter.onCriticalNsiCloseShiftDialogRead();
    };

    private PdSalePreparationPresenter.InteractionListener fineSalePreparationInteractionListener = new PdSalePreparationPresenter.InteractionListener() {

        @Override
        public void onSendETicketBtnClick(ETicketDataParams eTicketDataParams) {
            Navigator.navigateToEnterETicketDataActivity(RC_E_TICKET, getActivity(), PdSalePreparationFragment.this, eTicketDataParams);
        }

        @Override
        public void onCheckingECPFailed() {

        }

        @Override
        public void navigateToSelectExemption(SelectExemptionParams selectExemptionParams) {
            Navigator.navigateToSelectExemptionActivity(RC_SELECT_EXEMPTION, getActivity(), PdSalePreparationFragment.this, selectExemptionParams);
        }

        @Override
        public void navigateToRemoveExemption(RemoveExemptionParams removeExemptionParams) {
            mInteractionListener.navigateToRemoveExemption(removeExemptionParams);
        }

        @Override
        public void writePd() {
            mInteractionListener.writePd();
        }

        @Override
        public void printPd() {
            mInteractionListener.printPd();
        }

        @Override
        public void navigateBack() {
            mInteractionListener.navigateBack();
        }

        @Override
        public void navigateToCloseShiftActivity() {
            mInteractionListener.navigateToCloseShiftActivity();
        }
    };

    /**
     * Интерфейс обработки событий.
     */
    public interface InteractionListener {
        void navigateBack();

        void navigateToCloseShiftActivity();

        void navigateToRemoveExemption(RemoveExemptionParams removeExemptionParams);

        void writePd();

        void printPd();
    }
}
