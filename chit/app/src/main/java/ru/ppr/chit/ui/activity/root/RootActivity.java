package ru.ppr.chit.ui.activity.root;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;

import java.util.List;

import javax.inject.Inject;

import ru.ppr.chit.R;
import ru.ppr.chit.di.Dagger;
import ru.ppr.chit.ui.activity.ActivityNavigator;
import ru.ppr.chit.ui.activity.base.ActivityModule;
import ru.ppr.chit.ui.activity.base.MvpActivity;
import ru.ppr.chit.ui.activity.base.delegate.StatusBarDelegate;
import ru.ppr.core.domain.model.BarcodeType;
import ru.ppr.core.domain.model.EdsType;
import ru.ppr.core.domain.model.RfidType;

/**
 * Экран рут-меню.
 *
 * @author Dmitry Nevolin
 */
public class RootActivity extends MvpActivity implements RootView {

    private static final String EXTRA_BACK_TO_SPLASH = "EXTRA_BACK_TO_SPLASH";

    public static Intent getCallingIntent(Context context, boolean backToSplash) {
        return new Intent(context, RootActivity.class)
                .putExtra(EXTRA_BACK_TO_SPLASH, backToSplash);
    }

    // region Di
    private RootComponent component;
    @Inject
    ActivityNavigator navigator;
    @Inject
    StatusBarDelegate statusBarDelegate;
    // endregion
    // region Views
    private ProgressDialog backupProgress;
    private Spinner edsTypeSpinner;
    private Spinner barcodeTypeSpinner;
    private Spinner rfidTypeSpinner;
    // endregion
    // region Other
    private RootPresenter presenter;
    // endregion

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        component = DaggerRootComponent
                .builder()
                .appComponent(Dagger.appComponent())
                .activityModule(new ActivityModule(this))
                .build();
        component.inject(this);
        super.onCreate(savedInstanceState);
        presenter = getMvpDelegate().getPresenter(component::rootPresenter, RootPresenter.class);

        setContentView(R.layout.activity_root);

        statusBarDelegate.init(getMvpDelegate());

        findViewById(R.id.create_full_btn).setOnClickListener(v -> presenter.onCreateFullBtnClicked());
        findViewById(R.id.create_logs_btn).setOnClickListener(v -> presenter.onCreateLogsBtnClicked());
        findViewById(R.id.create_dbs_btn).setOnClickListener(v -> presenter.onCreateDbsBtnClicked());
        findViewById(R.id.restore_full_btn).setOnClickListener(v -> presenter.onRestoreFullBtnClicked());
        findViewById(R.id.restore_dbs_btn).setOnClickListener(v -> presenter.onRestoreDbsBtnClicked());

        edsTypeSpinner = (Spinner) findViewById(R.id.eds_type_spinner);
        edsTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                presenter.onEdsTypeSelected(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        barcodeTypeSpinner = (Spinner) findViewById(R.id.barcode_type_spinner);
        barcodeTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                presenter.onBarcodeTypeSelected(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        rfidTypeSpinner = (Spinner) findViewById(R.id.rfid_type_spinner);
        rfidTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                presenter.onRfidTypeSelected(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        backupProgress = new ProgressDialog(this);
        backupProgress.setCancelable(false);
        backupProgress.setCanceledOnTouchOutside(false);

        presenter.setNavigator(rootNavigator);
        presenter.setBackToSplash(getIntent().getBooleanExtra(EXTRA_BACK_TO_SPLASH, true));
        presenter.initialize();
    }

    @Override
    public void onBackPressed() {
        presenter.onBackPressed();
    }

    @Override
    public void setCreateFullBackupProgressVisible(boolean visible) {
        if (visible) {
            if (!backupProgress.isShowing()) {
                backupProgress.setMessage(getString(R.string.root_create_full_progress));
                backupProgress.show();
            }
        } else {
            if (backupProgress.isShowing()) {
                backupProgress.dismiss();
            }
        }
    }

    @Override
    public void setCreateLogsBackupProgressVisible(boolean visible) {
        if (visible) {
            if (!backupProgress.isShowing()) {
                backupProgress.setMessage(getString(R.string.root_create_logs_progress));
                backupProgress.show();
            }
        } else {
            if (backupProgress.isShowing()) {
                backupProgress.dismiss();
            }
        }
    }

    @Override
    public void setCreateDbsBackupProgressVisible(boolean visible) {
        if (visible) {
            if (!backupProgress.isShowing()) {
                backupProgress.setMessage(getString(R.string.root_create_dbs_progress));
                backupProgress.show();
            }
        } else {
            if (backupProgress.isShowing()) {
                backupProgress.dismiss();
            }
        }
    }

    @Override
    public void setRestoreFullBackupProgressVisible(boolean visible) {
        if (visible) {
            if (!backupProgress.isShowing()) {
                backupProgress.setMessage(getString(R.string.root_restore_full_progress));
                backupProgress.show();
            }
        } else {
            if (backupProgress.isShowing()) {
                backupProgress.dismiss();
            }
        }
    }

    @Override
    public void setRestoreDbsBackupProgressVisible(boolean visible) {
        if (visible) {
            if (!backupProgress.isShowing()) {
                backupProgress.setMessage(getString(R.string.root_restore_dbs_progress));
                backupProgress.show();
            }
        } else {
            if (backupProgress.isShowing()) {
                backupProgress.dismiss();
            }
        }
    }

    @Override
    public void setEdsTypeList(List<EdsType> edsTypeList) {
        SpinnerAdapter edsTypeSpinnerAdapter = new ArrayAdapter<>(this, R.layout.item_root_spinner, edsTypeList);
        edsTypeSpinner.setAdapter(edsTypeSpinnerAdapter);
    }

    @Override
    public void setEdsTypeListSelection(int selection) {
        edsTypeSpinner.setSelection(selection);
    }

    @Override
    public void setBarcodeTypeList(List<BarcodeType> barcodeTypeList) {
        SpinnerAdapter barcodeTypeSpinnerAdapter = new ArrayAdapter<>(this, R.layout.item_root_spinner, barcodeTypeList);
        barcodeTypeSpinner.setAdapter(barcodeTypeSpinnerAdapter);
    }

    @Override
    public void setBarcodeTypeListSelection(int selection) {
        barcodeTypeSpinner.setSelection(selection);
    }

    @Override
    public void setRfidTypeList(List<RfidType> rfidTypeList) {
        SpinnerAdapter rfidTypeSpinnerAdapter = new ArrayAdapter<>(this, R.layout.item_root_spinner, rfidTypeList);
        rfidTypeSpinner.setAdapter(rfidTypeSpinnerAdapter);
    }

    @Override
    public void setRfidTypeListSelection(int selection) {
        rfidTypeSpinner.setSelection(selection);
    }

    private final RootPresenter.Navigator rootNavigator = new RootPresenter.Navigator() {

        @Override
        public void navigateBack() {
            navigator.navigateBack();
        }

        @Override
        public void navigateToSplash() {
            navigator.navigateToSplash(true);
        }

    };

}
