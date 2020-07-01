package ru.ppr.cppk.ui.activity.controlreadbsc;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import java.util.ArrayList;

import ru.ppr.core.ui.helper.TimerResourceHelper;
import ru.ppr.core.ui.widget.SimpleLseView;
import ru.ppr.cppk.BuildConfig;
import ru.ppr.cppk.R;
import ru.ppr.cppk.dataCarrier.entity.BscInformation;
import ru.ppr.cppk.dataCarrier.entity.PD;
import ru.ppr.cppk.di.Dagger;
import ru.ppr.cppk.model.PdSaleParams;
import ru.ppr.cppk.ui.activity.base.ActivityModule;
import ru.ppr.cppk.ui.activity.base.MvpActivity;
import ru.ppr.cppk.ui.activity.base.Navigator;
import ru.ppr.cppk.ui.activity.controlreadbsc.model.ControlReadBscParams;
import ru.ppr.cppk.ui.activity.readpdfortransfer.model.ReadForTransferParams;
import ru.ppr.cppk.ui.dialog.SimpleDialog;


/**
 * Экран чтения БСК.
 *
 * @author Aleksandr Brazhkin
 */
public class ControlReadBscActivity extends MvpActivity implements ControlReadBscView {

    private static final String EXTRA_PARAMS = "EXTRA_PARAMS";

    public static Intent getCallingIntent(@NonNull Context context, @NonNull ControlReadBscParams controlReadBscParams) {
        Intent intent = new Intent(context, ControlReadBscActivity.class);
        intent.putExtra(EXTRA_PARAMS, controlReadBscParams);
        return intent;
    }

    // region Di
    private ControlReadBscComponent component;
    // endregion
    // region Views
    View inProcessState;
    ImageView timer;
    ProgressBar progress;
    SimpleLseView simpleLseView;
    //endregion
    //region Other
    private ControlReadBscPresenter presenter;
    private boolean saleNewPdBtnVisible;
    private State state = State.SEARCH_CARD;
    //endregion

    @Override
    public void onCreate(Bundle savedInstanceState) {
        component = DaggerControlReadBscComponent
                .builder()
                .appComponent(Dagger.appComponent())
                .controlReadBscParams(getIntent().getParcelableExtra(EXTRA_PARAMS))
                .activityModule(new ActivityModule(this))
                .build();
        component.inject(this);
        super.onCreate(savedInstanceState);
        presenter = getMvpDelegate().getPresenter(component::controlReadBscPresenter, ControlReadBscPresenter.class);
        ///////////////////////////////////////////////////////////////////////////////////////
        canUserHardwareButton();
        ///////////////////////////////////////////////////////////////////////////////////////
        setContentView(R.layout.activity_control_read_bsc);
        inProcessState = findViewById(R.id.inProcessState);
        timer = (ImageView) findViewById(R.id.timer);
        progress = (ProgressBar) findViewById(R.id.progress);
        simpleLseView = (SimpleLseView) findViewById(R.id.simpleLseView);
        ///////////////////////////////////////////////////////////////////////////////////////
        presenter.setNavigator(navigator);
        presenter.initialize();
    }

    @Override
    public void onStop() {
        presenter.onScreenClosed();
        super.onStop();
    }

    @Override
    public void onClickRfrid() {
        presenter.onRfidBtnClicked();
    }

    @Override
    public void onClickBarcode() {
        // запускался ридер из текущей активити, теперь так делать нельзя
    }

    @Override
    public void onClickSettings() {
        // Переопределяем метод чтобы нельзя было выйти в меню с этого окна
    }

    @Override
    public void setTimerValue(int value) {
        timer.setImageResource(TimerResourceHelper.getTimerImageResource(value));
    }

    @Override
    public void setState(State state) {
        this.state = state;
        switch (state) {
            case SEARCH_CARD: {
                inProcessState.setVisibility(View.VISIBLE);
                simpleLseView.setVisibility(View.GONE);
                timer.setVisibility(View.VISIBLE);
                progress.setVisibility(View.GONE);
                break;
            }
            case SEARCH_CARD_ERROR: {
                setErrorState();
                break;
            }
            case READ_CARD: {
                inProcessState.setVisibility(View.VISIBLE);
                simpleLseView.setVisibility(View.GONE);
                timer.setVisibility(View.GONE);
                progress.setVisibility(View.VISIBLE);
                break;
            }
            case PROCESSING_DATA: {
                inProcessState.setVisibility(View.VISIBLE);
                simpleLseView.setVisibility(View.GONE);
                timer.setVisibility(View.GONE);
                progress.setVisibility(View.VISIBLE);
                break;
            }
            case UNKNOWN_ERROR: {
                setErrorState();
                break;
            }
        }
    }

    private void setErrorState() {
        inProcessState.setVisibility(View.GONE);

        SimpleLseView.State.Builder stateBuilder = new SimpleLseView.State.Builder();
        stateBuilder.setMode(SimpleLseView.State.MODE_ERROR);
        stateBuilder.setTextMessage(R.string.control_read_bsc_could_not_read);
        if (saleNewPdBtnVisible) {
            stateBuilder.setButton1(R.string.control_read_bsc_sale_new_pd_btn, v -> presenter.onSaleNewPdBtnClicked());
        }
        stateBuilder.setButton2(R.string.control_read_bsc_repeat_btn, v -> presenter.onRepeatBtnClicked());
        simpleLseView.setState(stateBuilder.build());
        simpleLseView.show();
    }

    @Override
    public void showSaleNewPdConfirmDialog() {
        SimpleDialog simpleDialog = SimpleDialog.newInstance(null,
                getString(R.string.control_read_bsc_sale_new_pd_dialog_msg),
                getString(R.string.control_read_bsc_sale_new_pd_dialog_yes),
                getString(R.string.control_read_bsc_sale_new_pd_dialog_no),
                LinearLayout.HORIZONTAL,
                0);
        simpleDialog.show(getFragmentManager(), SimpleDialog.FRAGMENT_TAG);
        simpleDialog.setDialogPositiveBtnClickListener((dialog, dialogId) -> presenter.onSaleNewPdDialogYesBtnClicked());
    }

    @Override
    public void setSaleNewPdBtnVisible(boolean visible) {
        saleNewPdBtnVisible = visible;
        if(BuildConfig.DEBUG) {
            setErrorState();
        } else {
            if (state == State.SEARCH_CARD_ERROR || state == State.UNKNOWN_ERROR) {
                setErrorState();
            }
        }

    }

    private final ControlReadBscPresenter.Navigator navigator = new ControlReadBscPresenter.Navigator() {
        @Override
        public void navigateToRfidResultActivity(@Nullable ArrayList<PD> pdList, @Nullable BscInformation bscInformation, @Nullable ReadForTransferParams readForTransferParams) {
            Navigator.navigateToRfidResultActivity(ControlReadBscActivity.this, pdList, bscInformation, readForTransferParams);
            finish();
        }

        @Override
        public void navigateToPdSaleActivity(PdSaleParams pdSaleParams) {
            Navigator.navigateToPdSaleActivity(ControlReadBscActivity.this, pdSaleParams);
            finish();
        }

        @Override
        public void navigateToServiceTicketControlActivity() {
            Navigator.navigateToServiceTicketControlActivity(ControlReadBscActivity.this);
            finish();
        }
    };
}
