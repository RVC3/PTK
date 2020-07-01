package ru.ppr.cppk.ui.activity.transfersale.preparation;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import ru.ppr.cppk.R;
import ru.ppr.cppk.db.DateFormatOperations;
import ru.ppr.cppk.localdb.model.PaymentType;
import ru.ppr.cppk.localdb.model.TicketWayType;
import ru.ppr.cppk.ui.activity.transfersale.TransferSaleActivity;
import ru.ppr.cppk.ui.activity.transfersale.TransferSaleComponent;
import ru.ppr.cppk.ui.adapter.spinner.TicketTypeAdapter;
import ru.ppr.cppk.ui.dialog.SimpleDialog;
import ru.ppr.cppk.ui.fragment.FragmentOnBackPressed;
import ru.ppr.cppk.ui.fragment.base.MvpFragment;
import ru.ppr.logger.Logger;
import ru.ppr.nsi.entity.FeeType;
import ru.ppr.nsi.entity.TicketType;

/**
 * @author Dmitry Nevolin
 */
public class PreparationFragment extends MvpFragment implements PreparationView, FragmentOnBackPressed {

    public static PreparationFragment newInstance() {
        return new PreparationFragment();
    }

    private static final String TAG = Logger.makeLogTag(PreparationFragment.class);
    // Dialog tags
    private static final String DIALOG_EDS_FAILED_ERROR_TAG = "DIALOG_EDS_FAILED_ERROR_TAG";
    // Dependencies
    private PreparationComponent component;
    private PreparationPresenter presenter;
    // Views
    private ProgressDialog loadingDialog;
    private ViewGroup parentPdGroup;
    private TextView parentPdTicketType;
    private TextView parentPdNumber;
    private TextView parentPdDepartureStation;
    private ImageView parentPdDirectionImage;
    private TextView parentPdDestinationStation;
    private TextView parentPdExemptionValue;
    private TextView parentPdTrainCategory;
    private TextView parentPdValidityDates;
    private TextView transferValidityDates;
    private TextView transferRoute;
    private Spinner ticketTypeSpinner;
    private ViewGroup feeGroup;
    private TextView feeValue;
    private CheckBox feeCheckBox;
    private TextView transferCostValue;
    private TextView totalCostValue;
    private RadioGroup paymentTypeGroup;
    private RadioButton paymentTypeCash;
    private RadioButton paymentTypeCard;
    private Button writeToCardBtn;
    private Button printPdBtn;
    // Other
    private TicketTypeAdapter ticketTypeAdapter;
    private int selectedTicketTypePosition = 0;
    private InteractionListener interactionListener;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        TransferSaleComponent transferSaleComponent = ((TransferSaleActivity) getActivity()).getTransferSaleComponent();
        component = transferSaleComponent.preparationComponent();
        super.onCreate(savedInstanceState);
        presenter = getMvpDelegate().getPresenter(component::preparationPresenter, PreparationPresenter.class);
        presenter.setInteractionListener(preparationPresenterInteractionListener);
        presenter.initialize2();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_transfer_sale_preparation, container, false);

        parentPdGroup = (ViewGroup) view.findViewById(R.id.parent_pd_group);
        parentPdTicketType = (TextView) view.findViewById(R.id.parent_pd_ticket_type);
        parentPdNumber = (TextView) view.findViewById(R.id.parent_pd_number);
        parentPdDepartureStation = (TextView) view.findViewById(R.id.parent_pd_departure_station);
        parentPdDirectionImage = (ImageView) view.findViewById(R.id.parent_pd_direction_image);
        parentPdDestinationStation = (TextView) view.findViewById(R.id.parent_pd_destination_station);
        parentPdTrainCategory = (TextView) view.findViewById(R.id.parent_pd_train_category);
        parentPdExemptionValue = (TextView) view.findViewById(R.id.parent_pd_exemption_value);
        parentPdValidityDates = (TextView) view.findViewById(R.id.parent_pd_validity_dates);
        transferValidityDates = (TextView) view.findViewById(R.id.transferValidityDates);
        transferRoute = (TextView) view.findViewById(R.id.transferRoute);

        ticketTypeSpinner = (Spinner) view.findViewById(R.id.ticketTypeSpinner);
        ticketTypeAdapter = new TicketTypeAdapter(getActivity());
        ticketTypeSpinner.setAdapter(ticketTypeAdapter);
        ticketTypeSpinner.setOnTouchListener(ticketTypeSpinnerOnTouchListener);
        ticketTypeSpinner.setOnItemSelectedListener(ticketTypeSpinnerOnItemSelectedListener);
        feeGroup = (ViewGroup) view.findViewById(R.id.fee_group);
        feeValue = (TextView) view.findViewById(R.id.fee_value);
        feeCheckBox = (CheckBox) view.findViewById(R.id.fee_check_box);
        feeCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> presenter.onFeeCheckedChanged(isChecked));
        transferCostValue = (TextView) view.findViewById(R.id.transfer_cost_value);
        totalCostValue = (TextView) view.findViewById(R.id.total_cost_value);
        paymentTypeGroup = (RadioGroup) view.findViewById(R.id.payment_type_group);
        paymentTypeGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.payment_type_card) {
                presenter.onPaymentTypeSelected(PaymentType.INDIVIDUAL_BANK_CARD);
            } else {
                presenter.onPaymentTypeSelected(PaymentType.INDIVIDUAL_CASH);
            }
        });
        paymentTypeCash = (RadioButton) view.findViewById(R.id.payment_type_cash);
        paymentTypeCard = (RadioButton) view.findViewById(R.id.payment_type_card);
        writeToCardBtn = (Button) view.findViewById(R.id.writeToCardBtn);
        writeToCardBtn.setOnClickListener(v -> presenter.onWriteToCardBtnClicked());
        printPdBtn = (Button) view.findViewById(R.id.printPdBtn);
        printPdBtn.setOnClickListener(v -> presenter.onPrintPdBtnClicked());

        return view;
    }

    @Override
    public void showLoadingDialog() {
        if (loadingDialog == null) {
            loadingDialog = new ProgressDialog(getActivity());
            loadingDialog.setCancelable(false);
            loadingDialog.setCanceledOnTouchOutside(false);
            loadingDialog.setMessage(getString(R.string.transfer_sale_preparation_dialog_loading));
        }

        loadingDialog.show();
    }

    @Override
    public void hideLoadingDialog() {
        if (loadingDialog != null) {
            loadingDialog.dismiss();
        }
    }

    @Override
    public void setParentPdInfo(@Nullable ParentPdInfo parentPdInfo) {
        if (parentPdInfo == null) {
            parentPdGroup.setVisibility(View.GONE);
        } else {
            parentPdGroup.setVisibility(View.VISIBLE);
            parentPdTicketType.setText(parentPdInfo.ticketType);
            parentPdNumber.setText(String.format(getString(R.string.transfer_sale_preparation_pd_number), parentPdInfo.pdNumber));
            parentPdDepartureStation.setText(parentPdInfo.departureStation);
            parentPdDirectionImage.setImageResource(parentPdInfo.direction == TicketWayType.OneWay ? R.drawable.there_direction : R.drawable.there_back_direction);
            parentPdDestinationStation.setText(parentPdInfo.destinationStation);
            parentPdTrainCategory.setText(parentPdInfo.trainCategory);

            if (parentPdInfo.exemptionCode == 0) {
                parentPdExemptionValue.setText(R.string.No);
                parentPdExemptionValue.setTextColor(ActivityCompat.getColor(getActivity(), R.color.white));
            } else {
                parentPdExemptionValue.setText(String.valueOf(parentPdInfo.exemptionCode));
                parentPdExemptionValue.setTextColor(ActivityCompat.getColor(getActivity(), R.color.green));
            }

            if (parentPdInfo.startDate == null || parentPdInfo.endDate == null) {
                parentPdValidityDates.setText("");
            } else if (parentPdInfo.startDate.equals(parentPdInfo.endDate)) {
                parentPdValidityDates.setText(DateFormatOperations.getOutDate(parentPdInfo.startDate));
            } else {
                parentPdValidityDates.setText(getString(R.string.transfer_sale_preparation_validity_dates_period,
                        DateFormatOperations.getOutDate(parentPdInfo.startDate),
                        DateFormatOperations.getOutDate(parentPdInfo.endDate)));
            }
        }
    }

    @Override
    public void setTransferValidityDates(Date startDate, Date endDate) {
        if (startDate == null || endDate == null) {
            transferValidityDates.setText("");
        } else if (startDate.equals(endDate)) {
            transferValidityDates.setText(DateFormatOperations.getOutDate(startDate));
        } else {
            transferValidityDates.setText(getString(R.string.transfer_sale_preparation_validity_dates_period,
                    DateFormatOperations.getOutDate(startDate),
                    DateFormatOperations.getOutDate(endDate)));
        }
    }

    @Override
    public void setStations(String departureStation, String destinationStation) {
        transferRoute.setText(getString(R.string.transfer_sale_preparation_route_value, departureStation, destinationStation));
    }

    @Override
    public void setTicketTypes(@NonNull List<TicketType> ticketTypes) {
        Logger.trace(TAG, "setTicketTypes, size = " + ticketTypes.size());
        ticketTypeAdapter.setItems(ticketTypes);
    }

    @Override
    public void setSelectedTicketTypePosition(int position) {
        // В нулевой позиции мнимый элемент
        // https://aj.srvdev.ru/browse/CPPKPP-28012
        int uiPosition = position + 1;
        Logger.trace(TAG, "setSelectedTicketTypePosition, uiPosition = " + uiPosition);
        ticketTypeAdapter.setSelectedPosition(uiPosition);
        ticketTypeSpinner.setSelection(uiPosition);
        selectedTicketTypePosition = uiPosition;
    }

    @Override
    public void setTicketTypeSelectionEnabled(boolean enabled) {
        ticketTypeSpinner.setEnabled(enabled);
    }

    public void setInteractionListener(InteractionListener interactionListener) {
        this.interactionListener = interactionListener;
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
            this.feeValue.setText(R.string.transfer_sale_preparation_fee_value_no);
        } else {
            this.feeValue.setText(getString(R.string.transfer_sale_preparation_fee_value, feeValue));
        }
    }

    @Override
    public void setFeeType(FeeType feeType) {
        feeCheckBox.setText(getString(feeType == FeeType.TRANSFER_IN_BUS ?
                R.string.transfer_sale_preparation_fee_type_in_bus : R.string.transfer_sale_preparation_fee_type_in_train));
    }

    @Override
    public void setTransferCost(BigDecimal cost) {
        transferCostValue.setText(getString(R.string.transfer_sale_preparation_transfer_cost_value, cost));
    }

    @Override
    public void setTotalCost(BigDecimal cost) {
        totalCostValue.setText(getString(R.string.transfer_sale_preparation_total_cost_value, cost));
    }

    @Override
    public void setPaymentType(PaymentType paymentType) {
        paymentTypeGroup.check(paymentType == PaymentType.INDIVIDUAL_BANK_CARD ? R.id.payment_type_card : R.id.payment_type_cash);
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
    public void setEdsFailedErrorDialogVisible(boolean visible) {
        SimpleDialog edsFailedErrorDialog = (SimpleDialog) getFragmentManager().findFragmentByTag(DIALOG_EDS_FAILED_ERROR_TAG);

        if (visible) {
            if (edsFailedErrorDialog == null) {
                edsFailedErrorDialog = SimpleDialog.newInstance(null,
                        getString(R.string.transfer_sale_preparation_sft_error_dialog_message),
                        getString(R.string.transfer_sale_preparation_sft_close_dialog_positive),
                        null,
                        LinearLayout.VERTICAL,
                        0);
                edsFailedErrorDialog.setOnDismissListener(dialog -> presenter.onCheckEdsFailedDialogClosed());
                edsFailedErrorDialog.show(getFragmentManager(), DIALOG_EDS_FAILED_ERROR_TAG);
            } else {
                edsFailedErrorDialog.setOnDismissListener(dialog -> presenter.onCheckEdsFailedDialogClosed());
            }
        } else {
            if (edsFailedErrorDialog != null) {
                edsFailedErrorDialog.dismiss();
            }
        }
    }

    @Override
    public void setSaleButtonsState(SaleButtonsState saleButtonsState) {
        switch (saleButtonsState) {
            case WRITE_AND_PRINT: {
                writeToCardBtn.setVisibility(View.VISIBLE);
                printPdBtn.setVisibility(View.VISIBLE);
                break;
            }
            case WRITE_ONLY: {
                writeToCardBtn.setVisibility(View.VISIBLE);
                printPdBtn.setVisibility(View.GONE);
                break;
            }
        }
    }

    /**
     * Блокировщик открытия выпадающего списка типов ПД, если в нем нет иных вариантов.
     */
    private final View.OnTouchListener ticketTypeSpinnerOnTouchListener = (v, event) -> {
        for (int i = 0; i < ticketTypeAdapter.getItems().size(); i++) {
            if (ticketTypeAdapter.getSelectedPosition() - 1 != i) {
                return false;
            }
        }
        return true;
    };

    /**
     * Обработчик типа билета.
     */
    private final AdapterView.OnItemSelectedListener ticketTypeSpinnerOnItemSelectedListener = new AdapterView.OnItemSelectedListener() {

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            Logger.trace(TAG, "onItemSelected, uiPosition = " + position);
            if (selectedTicketTypePosition == position) {
                Logger.trace(TAG, "onItemSelected skipped");
            } else {
                // В нулевой позиции мнимый элемент
                // https://aj.srvdev.ru/browse/CPPKPP-28012
                ticketTypeAdapter.setSelectedPosition(position);
                selectedTicketTypePosition = position;
                presenter.onTicketTypeSelected(position - 1);
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
            /* NOP */
        }
    };

    private final PreparationPresenter.InteractionListener preparationPresenterInteractionListener = new PreparationPresenter.InteractionListener() {
        @Override
        public void writePd() {
            interactionListener.writePd();
        }

        @Override
        public void printPd() {
            interactionListener.printPd();
        }
    };

    @Override
    public boolean onBackPress() {
        interactionListener.navigateBack();
        return true;
    }

    public interface InteractionListener {

        void navigateBack();

        void writePd();

        void printPd();

        void navigateToCloseShiftActivity();
    }

}
