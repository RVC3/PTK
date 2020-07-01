package ru.ppr.chit.ui.activity.workingstate;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Locale;

import javax.inject.Inject;

import ru.ppr.chit.R;
import ru.ppr.chit.di.Dagger;
import ru.ppr.chit.domain.model.nsi.Version;
import ru.ppr.chit.helpers.AppDialogHelper;
import ru.ppr.chit.ui.activity.ActivityNavigator;
import ru.ppr.chit.ui.activity.base.ActivityModule;
import ru.ppr.chit.ui.activity.base.MvpActivity;
import ru.ppr.chit.ui.activity.base.delegate.StatusBarDelegate;
import ru.ppr.chit.ui.activity.base.delegate.TripServiceInfoDelegate;
import ru.ppr.chit.ui.activity.readbsqrcode.ReadBsQrCodeActivity;

/**
 * Экран, отображающий системную информацию о текущем состоянии приложения,
 * так же отсюда происходит синхронизация
 *
 * @author Dmitry Nevolin
 */
public class WorkingStateActivity extends MvpActivity implements WorkingStateView {

    // region RC
    private static final int RC_READ_BS_QR_CODE = 100;
    private static final int RC_WIFI_SETTINGS = 101;
    // endregion

    public static Intent getCallingIntent(Context context) {
        return new Intent(context, WorkingStateActivity.class);
    }

    // region Constants
    private static final SimpleDateFormat NSI_VERSION_DATE_FORMAT = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
    private static final SimpleDateFormat LAST_EXCHANGE_DATE_FORMAT = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault());
    // endregion
    // region Di
    private WorkingStateComponent component;
    @Inject
    ActivityNavigator navigator;
    @Inject
    StatusBarDelegate statusBarDelegate;
    @Inject
    TripServiceInfoDelegate tripServiceInfoDelegate;
    // endregion
    // region Views
    private TextView terminalId;
    private TextView wifiSsid;
    private TextView bsId;
    private TextView softwareVersion;
    private TextView nsiVersion;
    private TextView nsiVersionDate;
    private TextView lastExchange;
    private TextView lastExchangeDate;
    private TextView notExported;
    private ProgressDialog syncProgress;
    private View forceSync;
    private View bsConnect;
    // endregion
    // region Other
    private WorkingStatePresenter presenter;
    // endregion

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        component = DaggerWorkingStateComponent
                .builder()
                .appComponent(Dagger.appComponent())
                .activityModule(new ActivityModule(this))
                .build();
        component.inject(this);
        super.onCreate(savedInstanceState);
        presenter = getMvpDelegate().getPresenter(component::workingStatePresenter, WorkingStatePresenter.class);

        setContentView(R.layout.activity_working_state);

        statusBarDelegate.init(getMvpDelegate());
        tripServiceInfoDelegate.init(getMvpDelegate());

        terminalId = (TextView) findViewById(R.id.terminal_id);
        wifiSsid = (TextView) findViewById(R.id.wifi_ssid);
        bsId = (TextView) findViewById(R.id.bs_id);
        softwareVersion = (TextView) findViewById(R.id.software_version);
        nsiVersion = (TextView) findViewById(R.id.nsi_version);
        nsiVersionDate = (TextView) findViewById(R.id.nsi_version_date);
        lastExchange = (TextView) findViewById(R.id.last_exchange);
        lastExchangeDate = (TextView) findViewById(R.id.last_exchange_date);
        notExported = (TextView) findViewById(R.id.not_exported);

        syncProgress = AppDialogHelper.createProgress(this, getString(R.string.working_state_sync_progress), "");

        findViewById(R.id.wifi_settings).setOnClickListener(v -> presenter.onWifiSettingsClicked());
        forceSync = findViewById(R.id.force_sync);
        forceSync.setOnClickListener(v -> presenter.onForceSyncClicked());
        bsConnect = findViewById(R.id.bs_connect);
        bsConnect.setOnClickListener(v -> presenter.onBsConnectClicked());

        presenter.setNavigator(workingStateNavigator);
        presenter.initialize();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case RC_READ_BS_QR_CODE:
                presenter.onAuthInfoIdReceived(ReadBsQrCodeActivity.getResultFromIntent(resultCode, data));
                break;
            case RC_WIFI_SETTINGS:
                presenter.onWifiSettingsChanged();
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void setTerminalId(Long terminalId) {
        if (terminalId != null) {
            this.terminalId.setText(String.valueOf(terminalId));
            this.terminalId.setTextColor(ContextCompat.getColor(this, R.color.defaultGreen));
        } else {
            this.terminalId.setText(noDataString());
            this.terminalId.setTextColor(ContextCompat.getColor(this, R.color.defaultRed));
        }
    }

    @Override
    public void setWifiSsid(String wifiSsid) {
        if (wifiSsid != null) {
            if (wifiSsid.startsWith("\"") && wifiSsid.endsWith("\"")) {
                this.wifiSsid.setText(wifiSsid.substring(1, wifiSsid.length() - 1));
            } else {
                this.wifiSsid.setText(wifiSsid);
            }
            this.wifiSsid.setTextColor(ContextCompat.getColor(this, R.color.defaultGreen));
        } else {
            this.wifiSsid.setText(noDataString());
            this.wifiSsid.setTextColor(ContextCompat.getColor(this, R.color.defaultRed));
        }
    }

    @Override
    public void setBsId(String bsId) {
        if (bsId != null) {
            this.bsId.setText(bsId);
            this.bsId.setTextColor(ContextCompat.getColor(this, R.color.defaultGreen));
        } else {
            this.bsId.setText(noDataString());
            this.bsId.setTextColor(ContextCompat.getColor(this, R.color.defaultRed));
        }
    }

    @Override
    public void setSoftwareVersion(@NonNull String softwareVersion) {
        this.softwareVersion.setText(softwareVersion);
    }

    @Override
    public void setNsiVersion(@Nullable Version version) {
        if (version == null) {
            nsiVersion.setText(noDataString());
            nsiVersion.setTextColor(ContextCompat.getColor(this, R.color.defaultRed));
            nsiVersionDate.setText("");
        } else {
            nsiVersion.setText(String.valueOf(version.getVersionId()));
            nsiVersion.setTextColor(ContextCompat.getColor(this, R.color.defaultGreen));
            nsiVersionDate.setText(NSI_VERSION_DATE_FORMAT.format(version.getStartingDateTime()));
        }
    }

    @Override
    public void setForceSyncVisible(boolean visible) {
        forceSync.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    @Override
    public void setBsConnectVisible(boolean visible) {
        bsConnect.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    @Override
    public void setSyncProgressVisible(boolean visible) {
        if (visible) {
            if (!syncProgress.isShowing()) {
                syncProgress.show();
            }
        } else {
            if (syncProgress.isShowing()) {
                syncProgress.dismiss();
            }
        }
    }

    @Override
    public void setSyncProgressMessage(String message) {
        if (syncProgress.isShowing()) {
            syncProgress.setMessage(message);
        }
    }

    @Override
    public void setSyncErrorMessage(String message) {
        showErrorDialog(getString(R.string.working_state_sync_error_title), message);
    }

    @Override
    public void setBsConnectBrokenError(String message){
        showErrorDialog(getString(R.string.working_state_connect_error_title), message);
    }

    @Override
    public void setLastExchangeInfo(@Nullable LastExchangeInfo lastExchangeEvent) {
        if (lastExchangeEvent == null) {
            lastExchange.setText("");
            lastExchange.setTextColor(ContextCompat.getColor(this, R.color.defaultGreen));
            lastExchangeDate.setText("");
        } else {
            if (lastExchangeEvent.success) {
                lastExchange.setText(getString(R.string.working_state_ok));
                lastExchange.setTextColor(ContextCompat.getColor(this, R.color.defaultGreen));
                lastExchangeDate.setTextColor(ContextCompat.getColor(this, R.color.defaultGreen));
            } else {
                lastExchange.setText(getString(R.string.working_state_error));
                lastExchange.setTextColor(ContextCompat.getColor(this, R.color.defaultRed));
                lastExchangeDate.setTextColor(ContextCompat.getColor(this, R.color.defaultRed));
            }
            lastExchangeDate.setText(LAST_EXCHANGE_DATE_FORMAT.format(lastExchangeEvent.date));
        }
    }

    @Override
    public void setNotExported(int notExported) {
        if (notExported > 0) {
            this.notExported.setText(String.valueOf(notExported));
        } else {
            this.notExported.setText("");
        }
    }

    @Override
    public void onBackPressed() {
        navigator.navigateBack();
    }

    @Override
    public void onDestroy() {
        if (syncProgress.isShowing()) {
            syncProgress.dismiss();
        }
        super.onDestroy();
    }

    private void showErrorDialog(String title, String message){
        AppDialogHelper.showError(this, title, message);
    }

    private String noDataString() {
        return getString(R.string.trip_service_info_no_data);
    }

    private final WorkingStatePresenter.Navigator workingStateNavigator = new WorkingStatePresenter.Navigator() {

        @Override
        public void navigateToWifiSettings() {
            navigator.navigateToWifiSettings(RC_WIFI_SETTINGS);
        }

        @Override
        public void navigateToReadBsQrCode() {
            navigator.navigateToReadBsQrCode(RC_READ_BS_QR_CODE);
        }

    };

}
