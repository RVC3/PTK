package ru.ppr.cppk.ui.fragment.extraPaymentExecution;

import android.app.Activity;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Filter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import java.math.BigDecimal;
import java.util.List;

import ru.ppr.core.helper.Resources;
import ru.ppr.core.ui.mvp.MvpDelegate;
import ru.ppr.cppk.R;
import ru.ppr.cppk.localdb.model.TicketWayType;
import ru.ppr.cppk.db.DateFormatOperations;
import ru.ppr.cppk.localdb.model.PaymentType;
import ru.ppr.cppk.logic.CriticalNsiVersionDialogDelegate;
import ru.ppr.cppk.model.ETicketDataParams;
import ru.ppr.cppk.model.ExtraPaymentParams;
import ru.ppr.cppk.model.RemoveExemptionParams;
import ru.ppr.cppk.pd.DataSalePD;
import ru.ppr.cppk.ui.activity.base.Navigator;
import ru.ppr.cppk.ui.activity.enterETicketData.EnterETicketDataActivity;
import ru.ppr.cppk.ui.activity.selectExemption.SelectExemptionActivity;
import ru.ppr.cppk.ui.activity.selectExemption.SelectExemptionParams;
import ru.ppr.cppk.ui.activity.selectExemption.SelectExemptionResult;
import ru.ppr.cppk.ui.adapter.autoCompleteTextView.StationsAdapter;
import ru.ppr.cppk.ui.adapter.spinner.TariffPlanForSaleAdapter;
import ru.ppr.cppk.ui.dialog.SimpleDialog;
import ru.ppr.cppk.ui.fragment.base.LegacyMvpFragment;
import ru.ppr.cppk.ui.widget.StationEditText;
import ru.ppr.cppk.utils.validators.CyrillicTextWatcher;
import ru.ppr.logger.Logger;
import ru.ppr.nsi.entity.Station;
import ru.ppr.nsi.entity.TariffPlan;

/**
 * Экран оформления доплаты
 *
 * @author Aleksandr Brazhkin
 */
public class ExtraPaymentExecutionFragment extends LegacyMvpFragment implements ExtraPaymentExecutionView {

    private static final String TAG = Logger.makeLogTag(ExtraPaymentExecutionFragment.class);

    private static final int STATION_IN_EDIT_MODE_NONE = 0;
    private static final int STATION_IN_EDIT_MODE_DEP = 1;
    private static final int STATION_IN_EDIT_MODE_DEST = 2;
    // Dialog tags
    private static final String DIALOG_CRITICAL_NSI_CLOSE = "DIALOG_CRITICAL_NSI_CLOSE";
    private static final String NON_EXEMPTION_PAYMENT_FOR_EXEMPTION_PD_ATTENTION_DIALOG_TAG = "NON_EXEMPTION_PAYMENT_FOR_EXEMPTION_PD_ATTENTION_DIALOG_TAG";
    private static final String UNHANDLED_ERROR_OCCURRED_DIALOG_TAG = "UNHANDLED_ERROR_OCCURRED_DIALOG_TAG";

    // SS
    private String SS_SELECTED_TARIFF_PLAN_POS = "SS_SELECTED_TARIFF_PLAN_POS";
    // RC
    private static final int REQUEST_CODE_SELECT_EXEMPTION = 34;
    private static final int REQUEST_CODE_ETICKET = 35;

    public static final String FRAGMENT_TAG = ExtraPaymentExecutionFragment.class.getSimpleName();

    public static ExtraPaymentExecutionFragment newInstance() {
        return new ExtraPaymentExecutionFragment();
    }

    /**
     * Di
     */
    private final ExtraPaymentExecutionDi di = new ExtraPaymentExecutionDi(di());

    private InteractionListener mInteractionListener;

    //Views
    private ProgressDialog mProgressDialog;
    private ViewGroup parentPdGroup;
    private ViewGroup formPdGroup;
    private ScrollView scrollViewContainer;
    private TextView departureStationLabel;
    private StationEditText departureStationEditText;
    private TextView destinationStationLabel;
    private StationEditText destinationStationEditText;
    private ImageButton clearStationsBtn;
    private ImageButton swapStationsBtn;
    private Spinner tariffPlanSpinner;
    private ViewGroup exemptionGroup;
    private TextView exemptionLabel;
    private TextView exemptionValue;
    private ViewGroup feeGroup;
    private CheckBox feeCheckBox;
    private TextView feeValue;
    private ViewGroup costGroup;
    private TextView onePdCostValue;
    private TextView totalCostValue;
    private RadioGroup paymentTypeGroup;
    private RadioButton paymentTypeCash;
    private RadioButton paymentTypeCard;
    private Button sendETicketBtn;

    // Other
    private TariffPlanForSaleAdapter mTariffPlansAdapter;
    private StationsAdapter departureStationsAdapter;
    private StationsAdapter destinationStationsAdapter;
    private int stationInEditMode = STATION_IN_EDIT_MODE_NONE;
    //region Other
    private ExtraPaymentExecutionPresenter presenter;
    //endregion

    private int selectedTariffPlanPosition = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mTariffPlansAdapter = new TariffPlanForSaleAdapter(getActivity(), R.layout.item_spinner_extra_payment_tariff);
        departureStationsAdapter = new StationsAdapter(getActivity());
        departureStationsAdapter.setFilter(depStationAdapterFilter);
        destinationStationsAdapter = new StationsAdapter(getActivity());
        destinationStationsAdapter.setFilter(destStationAdapterFilter);

        if (savedInstanceState != null) {
            selectedTariffPlanPosition = savedInstanceState.getInt(SS_SELECTED_TARIFF_PLAN_POS);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_extra_payment_execution, container, false);

        parentPdGroup = (ViewGroup) view.findViewById(R.id.parentPdGroup);
        formPdGroup = (ViewGroup) view.findViewById(R.id.formPdGroup);
        scrollViewContainer = (ScrollView) view.findViewById(R.id.scrollViewContainer);
        //////////////////////////////////////////////////////////////////////////////////////
        departureStationLabel = (TextView) view.findViewById(R.id.departureStationLabel);
        departureStationEditText = (StationEditText) view.findViewById(R.id.departureStationEditText);
        departureStationEditText.addTextChangedListener(new CyrillicTextWatcher(departureStationEditText));
        departureStationEditText.setOnItemClickListener(depStationOnItemClickListener);
        departureStationEditText.setOnBackListener(stationBackListener);
        departureStationEditText.setOnEditorActionListener(depStationEditorListener);
        departureStationEditText.setOnFocusChangeListener(depStationViewFocusChangeListener);
        departureStationEditText.setOnClickListener(stationClickListener);
        //////////////////////////////////////////////////////////////////////////////////////
        destinationStationLabel = (TextView) view.findViewById(R.id.destinationStationLabel);
        destinationStationEditText = (StationEditText) view.findViewById(R.id.destinationStationEditText);
        destinationStationEditText.addTextChangedListener(new CyrillicTextWatcher(destinationStationEditText));
        destinationStationEditText.setOnItemClickListener(destStationOnItemClickListener);
        destinationStationEditText.setOnBackListener(stationBackListener);
        destinationStationEditText.setOnEditorActionListener(destStationEditorListener);
        destinationStationEditText.setOnFocusChangeListener(destStationViewFocusChangeListener);
        destinationStationEditText.setOnClickListener(stationClickListener);
        //////////////////////////////////////////////////////////////////////////////////////
        clearStationsBtn = (ImageButton) view.findViewById(R.id.clearStationsBtn);
        clearStationsBtn.setOnClickListener(v -> {
            cancelEditStation();
            presenter.onClearStationsBtnClick();
        });
        setImageDrawableForButton(clearStationsBtn, R.drawable.ic_clear_stations, R.color.extra_payment_btn_tint_color);
        /////////////
        swapStationsBtn = (ImageButton) view.findViewById(R.id.swapStationsBtn);
        swapStationsBtn.setOnClickListener(v -> {
            cancelEditStation();
            presenter.onSwapStationsBtnClick();
        });
        setImageDrawableForButton(swapStationsBtn, R.drawable.ic_swap_stations, R.color.extra_payment_btn_tint_color);
        //////////////////////////////////////////////////////////////////////////////////////
        tariffPlanSpinner = (Spinner) view.findViewById(R.id.tariffPlanSpinner);
        tariffPlanSpinner.setAdapter(mTariffPlansAdapter);
        tariffPlanSpinner.setOnTouchListener(tariffPlanSpinnerOnTouchListener);
        tariffPlanSpinner.setOnItemSelectedListener(tariffPlanSpinnerOnItemSelectedListener);
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
        onePdCostValue = (TextView) view.findViewById(R.id.onePdCostValue);
        totalCostValue = (TextView) view.findViewById(R.id.totalCostValue);
        //////////////////////////////////////////////////////////////////////////////////////
        paymentTypeGroup = (RadioGroup) view.findViewById(R.id.paymentTypeGroup);
        paymentTypeGroup.setOnCheckedChangeListener((group, checkedId) -> presenter.onPaymentTypeChecked(checkedId));
        paymentTypeCash = (RadioButton) view.findViewById(R.id.paymentTypeCash);
        paymentTypeCard = (RadioButton) view.findViewById(R.id.paymentTypeCard);
        //////////////////////////////////////////////////////////////////////////////////////
        view.findViewById(R.id.formPdBtn).setOnClickListener(v -> presenter.onSellBtnClick());

        //принудительно задаем высоты синей области размером с экран
        scrollViewContainer.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (scrollViewContainer.getMeasuredHeight() != 0) {
                    scrollViewContainer.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    ViewGroup.LayoutParams layoutParams = formPdGroup.getLayoutParams();
                    layoutParams.height = scrollViewContainer.getMeasuredHeight();
                    formPdGroup.setLayoutParams(layoutParams);
                }
            }
        });

        //настраиваем кнопку Электронный билет
        sendETicketBtn = (Button) view.findViewById(R.id.saleExtraPayment_sendETicketBtn);
        sendETicketBtn.setOnClickListener(v -> presenter.onSendETicketBtnClick());
        sendETicketBtn.setVisibility(di().printerManager().getPrinter().isFederalLaw54Supported() ? View.VISIBLE : View.INVISIBLE);

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
            case REQUEST_CODE_SELECT_EXEMPTION:
                SelectExemptionResult selectExemptionResult = SelectExemptionActivity.getResultFromIntent(resultCode, data);
                if (selectExemptionResult != null) {
                    presenter.onExemptionSelected(selectExemptionResult);
                }
                break;
            case REQUEST_CODE_ETICKET:
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
    }

    @Override
    public void onDestroy() {
        Fragment existingFragment = getFragmentManager().findFragmentByTag(SimpleDialog.FRAGMENT_TAG);
        if (existingFragment != null) {
            SimpleDialog exemptionUsageDialog = (SimpleDialog) existingFragment;
            // Чтобы не вызывать у презентера onExemptionUsageDialogDismissed() при повороте экрана
            exemptionUsageDialog.setOnDismissListener(null);
        }
        super.onDestroy();
    }

    public void setInteractionListener(InteractionListener interactionListener) {
        mInteractionListener = interactionListener;
    }

    @Override
    public void init(MvpDelegate parent, String id) {
        super.init(parent, id);
        presenter = getMvpDelegate().getPresenter(ExtraPaymentExecutionPresenter::new, ExtraPaymentExecutionPresenter.class);
    }

    public void initialize(Resources resources, ExtraPaymentParams extraPaymentParams, DataSalePD dataSalePd) {
        presenter.bindInteractionListener(extraPaymentExecutionInteractionListener);
        presenter.initialize(di.uiThread(),
                di.privateSettings(),
                di.commonSettings(),
                di.nsiDaoSession(),
                resources,
                extraPaymentParams,
                dataSalePd,
                di.nsiVersionManager(),
                di.criticalNsiVersionChecker(),
                di.tariffRepository(),
                di.tariffPlanRepository(),
                di.trainCategoryRepository(),
                di.ticketTypeRepository(),
                di.exemptionRepository(),
                di.exemptionGroupRepository(),
                di.pdSaleEnvFactory(),
                di.pdSaleRestrictionsParamsBuilder());
    }

    public void onExemptionRemoved() {
        presenter.onExemptionRemoved();
    }

    private void setImageDrawableForButton(ImageButton view, @DrawableRes int imageResId, @ColorRes int colorResId) {
        final Drawable originalDrawable;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            originalDrawable = getResources().getDrawable(imageResId, getActivity().getTheme());
        } else {
            originalDrawable = getResources().getDrawable(imageResId);
        }
        ColorStateList colorStateList;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            colorStateList = getResources().getColorStateList(colorResId, getActivity().getTheme());
        } else {
            colorStateList = getResources().getColorStateList(colorResId);
        }
        final Drawable wrappedDrawable = DrawableCompat.wrap(originalDrawable);
        DrawableCompat.setTintList(wrappedDrawable, colorStateList);
        view.setImageDrawable(wrappedDrawable);
    }

    @Override
    public void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(getActivity());
            mProgressDialog.setCancelable(false);
            mProgressDialog.setMessage(getString(R.string.please_wait));
        }
        mProgressDialog.show();
    }

    @Override
    public void hideProgressDialog() {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
        }
    }

    @Override
    public void setParentPdInfo(ParentPdInfo parentPdInfo) {
        ((TextView) getView().findViewById(R.id.parentPdTicketType)).setText(parentPdInfo.ticketType);
        ((TextView) getView().findViewById(R.id.parentPdNumber)).setText(String.format(getString(R.string.number_for_pd), parentPdInfo.pdNumber));
        ((TextView) getView().findViewById(R.id.parentPdDepartureStation)).setText(parentPdInfo.departureStation);
        ((TextView) getView().findViewById(R.id.parentPdDestinationStation)).setText(parentPdInfo.destinationStation);
        ((TextView) getView().findViewById(R.id.parentPdTrainCategory)).setText(parentPdInfo.trainCategory);
        ((TextView) getView().findViewById(R.id.parentPdStartDate)).setText(parentPdInfo.startDateTime == null ? "" : DateFormatOperations.getOutDate(parentPdInfo.startDateTime));
        ((TextView) getView().findViewById(R.id.parentPdTerminalNumber)).setText(String.valueOf(parentPdInfo.terminalNumber));

        int directionImageResourceId = parentPdInfo.direction == TicketWayType.OneWay ? R.drawable.there_direction : R.drawable.there_back_direction;
        ((ImageView) getView().findViewById(R.id.parentPdDirectionImage)).setImageResource(directionImageResourceId);

        if (parentPdInfo.exemptionCode == 0) {
            ((TextView) getView().findViewById(R.id.parentPdExemptionValue)).setText(R.string.No);
            ((TextView) getView().findViewById(R.id.parentPdExemptionValue)).setTextColor(getResources().getColor(R.color.white));
        } else {
            ((TextView) getView().findViewById(R.id.parentPdExemptionValue)).setText(String.valueOf(parentPdInfo.exemptionCode));
            ((TextView) getView().findViewById(R.id.parentPdExemptionValue)).setTextColor(getResources().getColor(R.color.green));
        }
    }

    @Override
    public void setStationFieldsEnabled(boolean enabled) {
        departureStationLabel.setEnabled(enabled);
        departureStationEditText.setEnabled(enabled);
        destinationStationLabel.setEnabled(enabled);
        destinationStationEditText.setEnabled(enabled);
    }

    @Override
    public void setClearStationsBtnEnabled(boolean enabled) {
        clearStationsBtn.setEnabled(enabled);
    }

    @Override
    public void setSwapStationsBtnEnabled(boolean enabled) {
        swapStationsBtn.setEnabled(enabled);
    }

    @Override
    public void setTariffPlans(List<TariffPlan> tariffPlans) {
        Logger.trace(TAG, "setTariffPlans, size = " + tariffPlans.size());
        mTariffPlansAdapter.setItems(tariffPlans);
    }

    @Override
    public void setDepartureStationName(String departureStationName) {
        departureStationEditText.setText(departureStationName, false);
    }

    @Override
    public void setDestinationStationName(String destinationStationName) {
        destinationStationEditText.setText(destinationStationName, false);
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
    public void setPaymentType(PaymentType paymentType) {
        paymentTypeGroup.check(paymentType == PaymentType.INDIVIDUAL_BANK_CARD ? R.id.paymentTypeCard : R.id.paymentTypeCash);
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
            this.feeValue.setText(R.string.extra_payment_execution_fee_no);
        } else {
            this.feeValue.setText(String.format(getString(R.string.rub_cent_as_single), feeValue));
        }
    }

    @Override
    public void setCostGroupVisible(boolean visible) {
        costGroup.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    @Override
    public void setOpePdCost(BigDecimal cost) {
        onePdCostValue.setText(String.format(getString(R.string.rub_cent_as_single), cost));
    }

    @Override
    public void setTotalCost(BigDecimal cost) {
        totalCostValue.setText(String.format(getString(R.string.rub_cent_as_single), cost));
    }

    @Override
    public void setExemptionEnabled(boolean enabled) {
        exemptionGroup.setEnabled(enabled);
        exemptionLabel.setEnabled(enabled);
        exemptionValue.setEnabled(enabled);
    }

    @Override
    public void setExemptionValue(int exemptionCode, int percentage) {
        if (exemptionCode > 0) {
            exemptionValue.setText(String.format(
                    getString(R.string.sale_exemption_code_and_percentage),
                    exemptionCode,
                    percentage));
        } else {
            exemptionValue.setText(getString(R.string.extra_payment_execution_exemption_no));
        }
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
    public void setFeeLabel(String feeLabel) {
        feeCheckBox.setText(feeLabel);
    }


    @Override
    public void setNonExemptionPaymentForExemptionPdAttentionDialogVisible(boolean visible) {
        SimpleDialog simpleDialog = (SimpleDialog) getFragmentManager().findFragmentByTag(NON_EXEMPTION_PAYMENT_FOR_EXEMPTION_PD_ATTENTION_DIALOG_TAG);

        if (visible) {
            if (simpleDialog == null) {
                simpleDialog = SimpleDialog.newInstance(null,
                        getString(R.string.extra_payment_execution_non_exemption_payment_for_exemption_pd_attention_dialog_message),
                        getString(R.string.extra_payment_execution_non_exemption_payment_for_exemption_pd_attention_dialog_positive),
                        getString(R.string.extra_payment_execution_non_exemption_payment_for_exemption_pd_attention_dialog_negative),
                        LinearLayout.HORIZONTAL,
                        0);
                simpleDialog.setDialogPositiveBtnClickListener((dialog, dialogId) -> extraPaymentExecutionInteractionListener.onSellBtnClick());
                simpleDialog.setOnDismissListener(dialog -> presenter.onNonExemptionPaymentForExemptionPdAttentionDialogDismiss());
                simpleDialog.show(getFragmentManager(), NON_EXEMPTION_PAYMENT_FOR_EXEMPTION_PD_ATTENTION_DIALOG_TAG);
            } else {
                simpleDialog.setDialogPositiveBtnClickListener((dialog, dialogId) -> extraPaymentExecutionInteractionListener.onSellBtnClick());
                simpleDialog.setOnDismissListener(dialog -> presenter.onNonExemptionPaymentForExemptionPdAttentionDialogDismiss());
            }
        } else {
            if (simpleDialog != null) {
                simpleDialog.dismiss();
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

    @Override
    public void setUnhandledErrorOccurredDialogVisible(boolean visible) {
        SimpleDialog unhandledErrorOccurredDialog = (SimpleDialog) getFragmentManager().findFragmentByTag(UNHANDLED_ERROR_OCCURRED_DIALOG_TAG);

        if (visible) {
            if (unhandledErrorOccurredDialog == null) {
                unhandledErrorOccurredDialog = SimpleDialog.newInstance(null,
                        getString(R.string.extra_payment_execution_unhandled_error_occurred_dialog_message),
                        getString(R.string.extra_payment_execution_unhandled_error_occurred_dialog_positive),
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

    /**
     * Выполняет скролл экрана, чтобы поля ввода станци оказались наверху.
     *
     * @param afterScroll Операция, которую следует выполнить после скролла
     */
    private void scrollScreen(Runnable afterScroll) {
        if (scrollViewContainer.getScrollY() != parentPdGroup.getMeasuredHeight()) {
            scrollViewContainer.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
                @Override
                public void onScrollChanged() {
                    if (scrollViewContainer.getScrollY() == parentPdGroup.getMeasuredHeight()) {
                        scrollViewContainer.getViewTreeObserver().removeOnScrollChangedListener(this);
                        afterScroll.run();
                    }
                }
            });
            scrollViewContainer.smoothScrollTo(0, parentPdGroup.getMeasuredHeight());
        } else {
            afterScroll.run();
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
     * Обработчик выбора тарифного плана.
     */
    private final AdapterView.OnItemSelectedListener tariffPlanSpinnerOnItemSelectedListener = new AdapterView.OnItemSelectedListener() {

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
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
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
            /* NOP */
        }
    };

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

    private final SimpleDialog.DialogBtnClickListener onCriticalNsiBackDialogShownListener = (dialog, dialogId) -> {
        presenter.onCriticalNsiBackDialogRead();
    };

    private final SimpleDialog.DialogBtnClickListener onCriticalNsiCloseShiftDialogShownListener = (dialog, dialogId) -> {
        presenter.onCriticalNsiCloseShiftDialogRead();
    };

    private ExtraPaymentExecutionPresenter.InteractionListener extraPaymentExecutionInteractionListener = new ExtraPaymentExecutionPresenter.InteractionListener() {
        @Override
        public void onSellBtnClick() {
            mInteractionListener.onSellBtnClick();
        }

        @Override
        public void onSendETicketBtnClick(ETicketDataParams eTicketDataParams) {
            Navigator.navigateToEnterETicketDataActivity(REQUEST_CODE_ETICKET, getActivity(), ExtraPaymentExecutionFragment.this, eTicketDataParams);
        }

        @Override
        public void navigateToSelectExemption(SelectExemptionParams selectExemptionParams) {
            Navigator.navigateToSelectExemptionActivity(REQUEST_CODE_SELECT_EXEMPTION, getActivity(), ExtraPaymentExecutionFragment.this, selectExemptionParams);
        }

        @Override
        public void navigateToRemoveExemption(RemoveExemptionParams removeExemptionParams) {
            mInteractionListener.navigateToRemoveExemption(removeExemptionParams);
        }

        @Override
        public void navigateToCloseShiftActivity() {
            mInteractionListener.navigateToCloseShiftActivity();
        }

        @Override
        public void navigateBack() {
            mInteractionListener.navigateBack();
        }
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
     * Обработчик смены фокуса станции отправления.
     */
    private View.OnFocusChangeListener depStationViewFocusChangeListener = (v, hasFocus) -> {
        if (hasFocus) {
            scrollScreen(() -> {
                if (stationInEditMode == STATION_IN_EDIT_MODE_DEST) {
                    presenter.onDestinationStationEditCanceled();
                }
                stationInEditMode = STATION_IN_EDIT_MODE_DEP;
                departureStationEditText.setText("", false);
                departureStationEditText.showDropDown();
            });
        }
    };

    /**
     * Обработчик смены фокуса станции назнчения.
     */
    private View.OnFocusChangeListener destStationViewFocusChangeListener = (v, hasFocus) -> {
        if (hasFocus) {
            scrollScreen(() -> {
                if (stationInEditMode == STATION_IN_EDIT_MODE_DEP) {
                    presenter.onDepartureStationEditCanceled();
                }
                stationInEditMode = STATION_IN_EDIT_MODE_DEST;
                destinationStationEditText.setText("", false);
                destinationStationEditText.showDropDown();
            });
        }
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
     * Скрывает клавиатуру для view
     */
    private void hideKeyboard() {
        InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(getView().getWindowToken(), 0);
        getView().requestFocus();
    }

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
     * Фильтр станций отправления
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
     * Фильтр станций назначения
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

    /**
     * Интерфейс обработки событий.
     */
    public interface InteractionListener {
        void onSellBtnClick();

        void navigateToRemoveExemption(RemoveExemptionParams removeExemptionParams);

        void navigateToCloseShiftActivity();

        void navigateBack();
    }
}
