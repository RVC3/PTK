package ru.ppr.chit.ui.activity.ticketcontrol;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import javax.inject.Inject;

import ru.ppr.chit.R;
import ru.ppr.chit.di.Dagger;
import ru.ppr.chit.domain.ticketcontrol.DataCarrierType;
import ru.ppr.chit.ui.activity.ActivityNavigator;
import ru.ppr.chit.ui.activity.base.ActivityModule;
import ru.ppr.chit.ui.activity.base.MvpActivity;
import ru.ppr.chit.ui.activity.base.delegate.StatusBarDelegate;
import ru.ppr.chit.ui.activity.base.delegate.TripServiceInfoDelegate;
import ru.ppr.chit.ui.activity.ticketcontrol.model.TicketValidationResult;
import ru.ppr.core.ui.widget.SimpleLseView;


/**
 * Экран контроля ПД.
 *
 * @author Aleksandr Brazhkin
 */
public class TicketControlActivity extends MvpActivity implements TicketControlView {

    private static final String EXTRA_TICKET_ID = "EXTRA_TICKET_ID";
    private static final String EXTRA_FROM_BSC = "EXTRA_FROM_BSC";
    private static final String EXTRA_FROM_BARCODE = "EXTRA_FROM_BARCODE";

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault());

    public static Intent getFromListCallingIntent(@NonNull Context context, long ticketId) {
        Intent intent = new Intent(context, TicketControlActivity.class);
        intent.putExtra(EXTRA_TICKET_ID, ticketId);
        return intent;
    }

    public static Intent getFromBscCallingIntent(@NonNull Context context) {
        Intent intent = new Intent(context, TicketControlActivity.class);
        intent.putExtra(EXTRA_FROM_BSC, true);
        return intent;
    }

    public static Intent getFromBarcodeCallingIntent(@NonNull Context context) {
        Intent intent = new Intent(context, TicketControlActivity.class);
        intent.putExtra(EXTRA_FROM_BARCODE, true);
        return intent;
    }

    // region Di
    private TicketControlComponent component;
    @Inject
    StatusBarDelegate statusBarDelegate;
    @Inject
    TripServiceInfoDelegate tripServiceInfoDelegate;
    @Inject
    ActivityNavigator navigator;
    // endregion
    // region Views
    TextView ticketNumber;
    TextView ticketType;
    TextView dataCarrier;
    TextView date;
    TextView trainNumber;
    TextView depStation;
    TextView destStation;
    TextView exemption;
    TextView actualCarNumber;
    TextView actualSeatNumber;
    TextView rebookingLabel;
    TextView newCarLabel;
    TextView newSeatLabel;
    TextView passenger;
    TextView documentType;
    TextView documentNumber;
    TextView validationResult;
    Button approveBtn;
    Button denyBtn;
    Button backBtn;
    View ticketData;
    SimpleLseView simpleLseView;
    //endregion
    //region Other
    private TicketControlPresenter presenter;
    //endregion

    @Override
    public void onCreate(Bundle savedInstanceState) {
        component = DaggerTicketControlComponent
                .builder()
                .appComponent(Dagger.appComponent())
                .activityModule(new ActivityModule(this))
                .ticketId(getIntent().getLongExtra(EXTRA_TICKET_ID, 0))
                .fromBsc(getIntent().getBooleanExtra(EXTRA_FROM_BSC, false))
                .fromBarcode(getIntent().getBooleanExtra(EXTRA_FROM_BARCODE, false))
                .build();
        component.inject(this);
        super.onCreate(savedInstanceState);
        presenter = getMvpDelegate().getPresenter(component::ticketControlPresenter, TicketControlPresenter.class);
        ///////////////////////////////////////////////////////////////////////////////////////
        setContentView(R.layout.activity_ticket_control);

        statusBarDelegate.init(getMvpDelegate());
        tripServiceInfoDelegate.init(getMvpDelegate());

        ticketNumber = (TextView) findViewById(R.id.ticketNumber);
        ticketType = (TextView) findViewById(R.id.ticketType);
        dataCarrier = (TextView) findViewById(R.id.dataCarrier);
        date = (TextView) findViewById(R.id.date);
        trainNumber = (TextView) findViewById(R.id.trainNumber);
        depStation = (TextView) findViewById(R.id.depStation);
        destStation = (TextView) findViewById(R.id.destStation);
        exemption = (TextView) findViewById(R.id.exemption);
        actualCarNumber = (TextView) findViewById(R.id.actualCarNumber);
        actualSeatNumber = (TextView) findViewById(R.id.actualSeatNumber);
        rebookingLabel = (TextView) findViewById(R.id.rebookingLabel);
        newCarLabel = (TextView) findViewById(R.id.newCarLabel);
        newSeatLabel = (TextView) findViewById(R.id.newSeatLabel);
        passenger = (TextView) findViewById(R.id.passenger);
        documentType = (TextView) findViewById(R.id.documentType);
        documentNumber = (TextView) findViewById(R.id.documentNumber);
        validationResult = (TextView) findViewById(R.id.validationResult);
        approveBtn = (Button) findViewById(R.id.approveBtn);
        approveBtn.setOnClickListener(view -> presenter.onApproveBtnClicked());
        denyBtn = (Button) findViewById(R.id.denyBtn);
        denyBtn.setOnClickListener(view -> presenter.onDenyBtnClicked());
        backBtn = (Button) findViewById(R.id.backBtn);
        backBtn.setOnClickListener(view -> onBackPressed());
        ticketData = findViewById(R.id.ticketData);
        simpleLseView = (SimpleLseView) findViewById(R.id.simpleLseView);
        ///////////////////////////////////////////////////////////////////////////////////////
        presenter.setNavigator(ticketControlNavigator);
        presenter.initialize();
    }

    @Override
    public void onBackPressed() {
        navigator.navigateBack();
    }

    @Override
    public void setTicketNumber(long ticketNumber) {
        this.ticketNumber.setText(String.valueOf(ticketNumber));
    }

    @Override
    public void setTicketType(String ticketType) {
        this.ticketType.setText(ticketType);
    }

    @Override
    public void setDataCarrierType(DataCarrierType dataCarrierType) {
        if (dataCarrierType == null) {
            dataCarrier.setText("");
            return;
        }
        switch (dataCarrierType) {
            case BARCODE: {
                dataCarrier.setText(R.string.ticket_control_data_carrier_type_barcode);
                return;
            }
            case SMART_CARD: {
                dataCarrier.setText(R.string.ticket_control_data_carrier_type_bsc);
                return;
            }
            case TICKET_LIST: {
                dataCarrier.setText(R.string.ticket_control_data_carrier_type_ticket_list);
            }
        }
    }

    @Override
    public void setDate(Date date) {
        this.date.setText(date == null ? "" : DATE_FORMAT.format(date));
    }

    @Override
    public void setTrainNumber(String trainNumber) {
        this.trainNumber.setText(String.valueOf(trainNumber));
    }

    @Override
    public void setDepStationName(String depStationName) {
        depStation.setText(depStationName);
    }

    @Override
    public void setDestStationName(String destStationName) {
        destStation.setText(destStationName);
    }

    @Override
    public void setExemptionExpressCode(Integer expressCode) {
        if (expressCode == null) {
            exemption.setText(R.string.ticket_control_exemption_no_value);
        } else {
            exemption.setText(String.valueOf(expressCode));
        }
    }

    @Override
    public void setCarNumber(String expectedCarNumber, String actualCarNumber) {
        this.actualCarNumber.setText(actualCarNumber);
        String exCarNumber = TextUtils.isEmpty(expectedCarNumber) ? "—" : expectedCarNumber;
        String acCarNumber = TextUtils.isEmpty(actualCarNumber) ? "—" : actualCarNumber;
        newCarLabel.setText(getString(R.string.ticket_control_new_car, exCarNumber, acCarNumber));
    }

    @Override
    public void setNewCarInfoVisible(boolean visible) {
        newCarLabel.setVisibility(visible ? View.VISIBLE : View.GONE);
        updateRebookingLabelVisibility();
    }

    @Override
    public void setSeatNumber(String expectedSeatNumber, String actualSeatNumber) {
        this.actualSeatNumber.setText(actualSeatNumber);
        String exSeatNumber = TextUtils.isEmpty(expectedSeatNumber) ? "—" : expectedSeatNumber;
        String acSeatNumber = TextUtils.isEmpty(actualSeatNumber) ? "—" : actualSeatNumber;
        newSeatLabel.setText(getString(R.string.ticket_control_new_seat, exSeatNumber, acSeatNumber));
    }

    @Override
    public void setNewSeatInfoVisible(boolean visible) {
        newSeatLabel.setVisibility(visible ? View.VISIBLE : View.GONE);
        updateRebookingLabelVisibility();
    }

    @Override
    public void setPassengerFio(String fio) {
        passenger.setText(fio);
    }

    @Override
    public void setDocumentType(String documentType) {
        this.documentType.setText(documentType);
    }

    @Override
    public void setDocumentNumber(String documentNumber) {
        this.documentNumber.setText(documentNumber);
    }

    @Override
    public void setDateValid(@NonNull DateValidity dateValidity) {
        setTextViewValidTextColor(date, dateValidity == DateValidity.FULLY_VALID);
    }

    @Override
    public void setTrainValid(boolean valid) {
        setTextViewValidTextColor(trainNumber, valid);
    }

    @Override
    public void setDepStationValid(boolean valid) {
        setTextViewValidTextColor(depStation, valid);
    }

    @Override
    public void setDestinationStationValid(boolean valid) {
        setTextViewValidTextColor(destStation, valid);
    }

    @Override
    public void setValidationResult(TicketValidationResult validationResult) {
        if (validationResult == null) {
            this.validationResult.setVisibility(View.GONE);
            return;
        }
        switch (validationResult) {
            case SUCCESS: {
                this.validationResult.setVisibility(View.GONE);
                return;
            }
            case PROBABLY_SUCCESS_BY_DATE: {
                this.validationResult.setVisibility(View.VISIBLE);
                this.validationResult.setTextColor(ContextCompat.getColor(this, R.color.app_warn));
                this.validationResult.setText(R.string.ticket_control_validation_res_probably_success_by_date);
                return;
            }
            case INVALID_DATA: {
                this.validationResult.setVisibility(View.VISIBLE);
                this.validationResult.setTextColor(ContextCompat.getColor(this, R.color.app_error));
                this.validationResult.setText(R.string.ticket_control_validation_res_invalid_data);
                return;
            }
            case INVALID_EDS_KEY: {
                this.validationResult.setVisibility(View.VISIBLE);
                this.validationResult.setTextColor(ContextCompat.getColor(this, R.color.app_error));
                this.validationResult.setText(R.string.ticket_control_validation_res_invalid_eds_key);
                return;
            }
            case REVOKED_EDS_KEY: {
                this.validationResult.setVisibility(View.VISIBLE);
                this.validationResult.setTextColor(ContextCompat.getColor(this, R.color.app_error));
                this.validationResult.setText(R.string.ticket_control_validation_res_revoked_eds_key);
                return;
            }
            case CANCELLED: {
                this.validationResult.setVisibility(View.VISIBLE);
                this.validationResult.setTextColor(ContextCompat.getColor(this, R.color.app_error));
                this.validationResult.setText(R.string.ticket_control_validation_res_cancelled);
                return;
            }
            case RETURNED: {
                this.validationResult.setVisibility(View.VISIBLE);
                this.validationResult.setTextColor(ContextCompat.getColor(this, R.color.app_error));
                this.validationResult.setText(R.string.ticket_control_validation_res_returned);
            }
        }
    }

    @Override
    public void setApproveBtnVisible(boolean visible) {
        approveBtn.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    @Override
    public void setDenyBtnVisible(boolean visible) {
        denyBtn.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    @Override
    public void setState(State state) {
        switch (state) {
            case PREPARING: {
                ticketData.setVisibility(View.GONE);
                SimpleLseView.State.Builder stateBuilder = new SimpleLseView.State.Builder();
                stateBuilder.setMode(SimpleLseView.State.MODE_LOADING);
                stateBuilder.setTextMessage(R.string.ticket_control_checking_progress);
                simpleLseView.setState(stateBuilder.build());
                simpleLseView.show();
                return;
            }
            case DATA: {
                ticketData.setVisibility(View.VISIBLE);
                simpleLseView.hide();
                return;
            }
            case NO_DATA: {
                ticketData.setVisibility(View.GONE);
                SimpleLseView.State.Builder stateBuilder = new SimpleLseView.State.Builder();
                stateBuilder.setMode(SimpleLseView.State.MODE_ERROR);
                stateBuilder.setTextMessage(R.string.ticket_control_no_data);
                simpleLseView.setState(stateBuilder.build());
                simpleLseView.show();
            }
        }
    }

    private void updateRebookingLabelVisibility() {
        if (newCarLabel.getVisibility() == View.VISIBLE
                || newSeatLabel.getVisibility() == View.VISIBLE) {
            rebookingLabel.setVisibility(View.VISIBLE);
        } else {
            rebookingLabel.setVisibility(View.GONE);
        }
    }

    private void setTextViewValidTextColor(TextView textView, boolean valid) {
        if (valid) {
            textView.setTextColor(getResources().getColor(R.color.app_normal));
        } else {
            textView.setTextColor(getResources().getColor(R.color.app_error));
        }
    }

    private final TicketControlPresenter.Navigator ticketControlNavigator = new TicketControlPresenter.Navigator() {
        @Override
        public void navigateBack() {
            navigator.navigateBack();
        }
    };
}
