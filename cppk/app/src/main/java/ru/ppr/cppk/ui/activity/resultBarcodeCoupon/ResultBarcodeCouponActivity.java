package ru.ppr.cppk.ui.activity.resultBarcodeCoupon;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;

import ru.ppr.cppk.R;
import ru.ppr.cppk.di.Dagger;
import ru.ppr.cppk.model.PdSaleParams;
import ru.ppr.cppk.ui.activity.base.MvpActivity;
import ru.ppr.cppk.ui.activity.base.Navigator;
import ru.ppr.cppk.ui.dialog.SimpleDialog;

/**
 * @author Dmitry Nevolin
 */
public class ResultBarcodeCouponActivity extends MvpActivity implements ResultBarcodeCouponView {

    private static final String COUPON_READ_EVENT_ID = "COUPON_READ_EVENT_ID";

    public static Intent getCallingIntent(@NonNull Context context, long couponReadEventId) {
        return new Intent(context, ResultBarcodeCouponActivity.class)
                .putExtra(COUPON_READ_EVENT_ID, couponReadEventId);
    }

    /**
     * Di
     */
    private ResultBarcodeCouponDi di;
    // Views
    private TextView validityStatus;
    private TextView errorMessage;
    private TextView stationValue;
    private TextView printDateTimeValue;
    private TextView moreThanNHours;
    private TextView salePdDisabled;
    private View contentView;
    private View progressView;
    private Button sellPdBtn;
    private Button sellBaggageBtn;
    //region Other
    private ResultBarcodeCouponPresenter presenter;
    //endregion

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_result_barcode_coupon);

        di = new ResultBarcodeCouponDi(di());

        validityStatus = (TextView) findViewById(R.id.validityStatus);
        errorMessage = (TextView) findViewById(R.id.errorMessage);
        stationValue = (TextView) findViewById(R.id.stationValue);
        printDateTimeValue = (TextView) findViewById(R.id.printDateTimeValue);
        moreThanNHours = (TextView) findViewById(R.id.moreThanNHours);
        salePdDisabled = (TextView) findViewById(R.id.salePdDisabled);
        contentView = findViewById(R.id.contentView);
        progressView = findViewById(R.id.progressView);
        sellPdBtn = (Button) findViewById(R.id.sellPdBtn);
        sellPdBtn.setOnClickListener(v -> presenter.onSellPdClicked());
        sellBaggageBtn = (Button) findViewById(R.id.sellBaggageBtn);
        sellBaggageBtn.setOnClickListener(v -> presenter.onSellBaggageClicked());

        presenter = getMvpDelegate().getPresenter(ResultBarcodeCouponPresenter::new, ResultBarcodeCouponPresenter.class);
        presenter.bindInteractionListener(resultBarcodeCouponInteractionListener);
        presenter.initialize(
                getIntent().getLongExtra(COUPON_READ_EVENT_ID, 0),
                di.uiThread(),
                di.localDaoSession(),
                Dagger.appComponent().ptkModeChecker(),
                di.securityDaoSession(),
                di.commonSettings(),
                di.privateSettings(),
                di.nsiVersionManager(),
                di.pdSaleEnvFactory(),
                di.stationRepository(),
                di.pdSaleRestrictionsParamsBuilder(),
                Dagger.appComponent().permissionChecker()
        );
    }

    private ResultBarcodeCouponPresenter.InteractionListener resultBarcodeCouponInteractionListener = new ResultBarcodeCouponPresenter.InteractionListener() {
        @Override
        public void navigateToPdSale(PdSaleParams pdSaleParams) {
            Navigator.navigateToPdSaleActivity(ResultBarcodeCouponActivity.this, pdSaleParams);
            finish();
        }
    };

    @Override
    public void setValidityStatus(boolean valid) {
        if (valid) {
            validityStatus.setText(R.string.result_barcode_coupon_validity_yes);
            validityStatus.setBackgroundColor(getResources().getColor(R.color.result_barcode_coupon_success));
        } else {
            validityStatus.setText(R.string.result_barcode_coupon_validity_no);
            validityStatus.setBackgroundColor(getResources().getColor(R.color.result_barcode_coupon_error));
        }
    }

    @Override
    public void setErrorMessage(ErrorMessage errorMessage) {
        if (errorMessage == ErrorMessage.NONE) {
            this.errorMessage.setText(null);
            this.errorMessage.setVisibility(View.GONE);
        } else if (errorMessage == ErrorMessage.ALREADY_USED) {
            this.errorMessage.setText(R.string.result_barcode_coupon_msg_already_used);
            this.errorMessage.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void setStationName(String name) {
        if (name == null) {
            stationValue.setText(R.string.result_barcode_coupon_station_not_found);
        } else {
            stationValue.setText(name);
        }
    }

    @Override
    public void setStationValid(boolean valid) {
        if (valid) {
            stationValue.setTextColor(getResources().getColor(R.color.result_barcode_coupon_normal));
        } else {
            stationValue.setTextColor(getResources().getColor(R.color.result_barcode_coupon_error));
        }
    }

    @Override
    public void setPrintDateTime(Date printDateTime) {
        if (printDateTime == null) {
            printDateTimeValue.setText(null);
        } else {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm");
            String formattedDate = dateFormat.format(printDateTime);
            printDateTimeValue.setText(formattedDate);
        }
    }

    @Override
    public void setPrintDateTimeValid(boolean valid) {
        if (valid) {
            printDateTimeValue.setTextColor(getResources().getColor(R.color.result_barcode_coupon_normal));
        } else {
            printDateTimeValue.setTextColor(getResources().getColor(R.color.result_barcode_coupon_error));
        }
    }

    @Override
    public void showMoreThanNHoursError(int hours) {
        moreThanNHours.setVisibility(View.VISIBLE);
        moreThanNHours.setText(getResources().getQuantityString(R.plurals.result_barcode_coupon_more_than_n_hours, hours, hours));
    }

    @Override
    public void hideMoreThanNHoursError() {
        moreThanNHours.setVisibility(View.GONE);
    }

    @Override
    public void showSalePdDisabledError() {
        salePdDisabled.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideSalePdDisabledError() {
        salePdDisabled.setVisibility(View.GONE);
    }

    @Override
    public void showProgress() {
        contentView.setVisibility(View.GONE);
        progressView.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideProgress() {
        progressView.setVisibility(View.GONE);
        contentView.setVisibility(View.VISIBLE);
    }

    @Override
    public void showErrorTariffNotFound(String name) {
        SimpleDialog simpleDialog = SimpleDialog.newInstance(null,
                getString(R.string.result_barcode_coupon_tariff_not_found_msg, name),
                getString(R.string.result_barcode_coupon_tariff_not_found_continue),
                getString(R.string.result_barcode_coupon_tariff_not_found_cancel),
                LinearLayout.VERTICAL,
                0);
        simpleDialog.show(getFragmentManager(), SimpleDialog.FRAGMENT_TAG);
        simpleDialog.setDialogPositiveBtnClickListener((dialog, dialogId) -> {
            presenter.onContinueWithoutTariffClicked();
        });
    }

    @Override
    public void setSalePdBtnVisible(boolean visible) {
        sellPdBtn.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    @Override
    public void setBaggagePdBtnVisible(boolean visible) {
        sellBaggageBtn.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    @Override
    public void setSalePdBtnEnabled(boolean enable) {
        sellPdBtn.setEnabled(enable);
    }

    @Override
    public void setBaggagePdBtnEnabled(boolean enable) {
        sellBaggageBtn.setEnabled(enable);
    }

}
