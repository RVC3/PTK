package ru.ppr.chit.ui.activity.readbsqrcode;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import javax.inject.Inject;

import ru.ppr.chit.R;
import ru.ppr.chit.di.Dagger;
import ru.ppr.chit.ui.activity.ActivityNavigator;
import ru.ppr.chit.ui.activity.base.ActivityModule;
import ru.ppr.chit.ui.activity.base.MvpActivity;
import ru.ppr.chit.ui.activity.base.delegate.StatusBarDelegate;
import ru.ppr.core.ui.helper.TimerResourceHelper;
import ru.ppr.core.ui.widget.SimpleLseView;

/**
 * "ПЕРВЫЙ ЭКРАН" с макетов
 *
 * @author Dmitry Nevolin
 */
public class ReadBsQrCodeActivity extends MvpActivity implements ReadBsQrCodeView {

    // region Extras
    private static final String EXTRA_RESULT_READ_QR_CODE = "EXTRA_RESULT_READ_QR_CODE";
    // endregion

    public static Intent getCallingIntent(Context context) {
        return new Intent(context, ReadBsQrCodeActivity.class);
    }


    /**
     * Формирует из данных {@link #onActivityResult(int, int, Intent)}
     * результат работы данной активити
     *
     * @param resultCode из {@link #onActivityResult(int, int, Intent)}
     * @param data       из {@link #onActivityResult(int, int, Intent)}
     * @return id {@link ru.ppr.chit.domain.model.local.AuthInfo}
     */
    @Nullable
    public static Long getResultFromIntent(int resultCode, @Nullable final Intent data) {
        Long authInfoId = null;
        if (resultCode == Activity.RESULT_OK && data != null) {
            authInfoId = (Long) data.getSerializableExtra(EXTRA_RESULT_READ_QR_CODE);
        }
        return authInfoId;
    }

    // region Di
    private ReadBsQrCodeComponent component;
    @Inject
    StatusBarDelegate statusBarDelegate;
    @Inject
    ActivityNavigator navigator;
    // endregion
    // region Views
    View inProcessState;
    ImageView timer;
    ProgressBar progress;
    SimpleLseView simpleLseView;
    //endregion
    // region Other
    private ReadBsQrCodePresenter presenter;
    // endregion

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        component = DaggerReadBsQrCodeComponent
                .builder()
                .appComponent(Dagger.appComponent())
                .activityModule(new ActivityModule(this))
                .build();
        component.inject(this);
        super.onCreate(savedInstanceState);
        presenter = getMvpDelegate().getPresenter(component::readBsQrCodePresenter, ReadBsQrCodePresenter.class);

        setContentView(R.layout.activity_read_bs_qr_code);

        statusBarDelegate.init(getMvpDelegate());

        inProcessState = findViewById(R.id.inProcessState);
        timer = (ImageView) findViewById(R.id.timer);
        progress = (ProgressBar) findViewById(R.id.progress);
        simpleLseView = (SimpleLseView) findViewById(R.id.simpleLseView);

        presenter.setNavigator(readBsQrCodeNavigator);
        presenter.initialize();
    }

    @Override
    public void onStop() {
        presenter.onScreenClosed();
        super.onStop();
    }

    @Override
    public void onBackPressed() {
        navigator.navigateBack();
    }

    @Override
    public void setTimerValue(int value) {
        timer.setImageResource(TimerResourceHelper.getTimerImageResource(value));
    }

    @Override
    public void setState(State state) {
        switch (state) {
            case SEARCH_BARCODE: {
                inProcessState.setVisibility(View.VISIBLE);
                simpleLseView.setVisibility(View.GONE);
                timer.setVisibility(View.VISIBLE);
                progress.setVisibility(View.GONE);
                break;
            }
            case SEARCH_BARCODE_ERROR: {
                setErrorState();
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
        stateBuilder.setTextMessage(R.string.read_bs_qr_code_could_not_read);
        stateBuilder.setButton1(R.string.read_bs_qr_code_repeat_btn, v -> presenter.onRepeatBtnClicked());
        stateBuilder.setButton2(R.string.read_bs_qr_code_cancel_btn, v -> presenter.onCancelBtnClicked());
        simpleLseView.setState(stateBuilder.build());
        simpleLseView.show();
    }

    private final ReadBsQrCodePresenter.Navigator readBsQrCodeNavigator = new ReadBsQrCodePresenter.Navigator() {

        @Override
        public void navigateBack(@Nullable Long authInfoId) {
            setResult(RESULT_OK, new Intent().putExtra(EXTRA_RESULT_READ_QR_CODE, authInfoId));
            navigator.navigateBack();
        }

    };

}
