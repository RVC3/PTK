package ru.ppr.chit.ui.activity.readbsc;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
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
 * Экран чтения БСК.
 *
 * @author Aleksandr Brazhkin
 */
public class ReadBscActivity extends MvpActivity implements ReadBscView {

    public static Intent getCallingIntent(@NonNull Context context) {
        return new Intent(context, ReadBscActivity.class);
    }

    // region Di
    private ReadBscComponent component;
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
    //region Other
    private ReadBscPresenter presenter;
    //endregion

    @Override
    public void onCreate(Bundle savedInstanceState) {
        component = DaggerReadBscComponent.builder().appComponent(Dagger.appComponent()).activityModule(new ActivityModule(this)).build();
        component.inject(this);
        super.onCreate(savedInstanceState);
        presenter = getMvpDelegate().getPresenter(component::readBscPresenter, ReadBscPresenter.class);
        ///////////////////////////////////////////////////////////////////////////////////////
        setContentView(R.layout.activity_read_bsc);

        statusBarDelegate.init(getMvpDelegate());

        inProcessState = findViewById(R.id.inProcessState);
        timer = (ImageView) findViewById(R.id.timer);
        progress = (ProgressBar) findViewById(R.id.progress);
        simpleLseView = (SimpleLseView) findViewById(R.id.simpleLseView);
        ///////////////////////////////////////////////////////////////////////////////////////
        presenter.setNavigator(readBscNavigator);
        presenter.initialize();
    }

    @Override
    public void onStop() {
        presenter.onScreenClosed();
        super.onStop();
    }

    @Override
    public void setTimerValue(int value) {
        timer.setImageResource(TimerResourceHelper.getTimerImageResource(value));
    }

    @Override
    public void setState(State state) {
        switch (state) {
            case SEARCH_CARD: {
                inProcessState.setVisibility(View.VISIBLE);
                simpleLseView.setVisibility(View.GONE);
                timer.setVisibility(View.VISIBLE);
                progress.setVisibility(View.GONE);
                return;
            }
            case SEARCH_CARD_ERROR: {
                setErrorState(R.string.read_bsc_could_not_read);
                return;
            }
            case READ_CARD: {
                inProcessState.setVisibility(View.VISIBLE);
                simpleLseView.setVisibility(View.GONE);
                timer.setVisibility(View.GONE);
                progress.setVisibility(View.VISIBLE);
                return;
            }
            case PROCESSING_DATA: {
                inProcessState.setVisibility(View.VISIBLE);
                simpleLseView.setVisibility(View.GONE);
                timer.setVisibility(View.GONE);
                progress.setVisibility(View.VISIBLE);
                return;
            }
            case EMPTY_CARD: {
                setErrorState(R.string.read_bsc_empty_card);
                return;
            }
            case UNKNOWN_ERROR: {
                setErrorState(R.string.read_bsc_could_not_read);
            }
        }
    }

    private void setErrorState(@StringRes int msgResId) {
        inProcessState.setVisibility(View.GONE);

        SimpleLseView.State.Builder stateBuilder = new SimpleLseView.State.Builder();
        stateBuilder.setMode(SimpleLseView.State.MODE_ERROR);
        stateBuilder.setTextMessage(msgResId);
        stateBuilder.setButton1(R.string.read_bsc_repeat_btn, v -> presenter.onRepeatBtnClicked());
        stateBuilder.setButton2(R.string.read_bsc_cancel_btn, v -> presenter.onCancelBtnClicked());
        simpleLseView.setState(stateBuilder.build());
        simpleLseView.show();
    }

    @Override
    public void onBackPressed() {
        navigator.navigateBack();
    }

    private final ReadBscPresenter.Navigator readBscNavigator = new ReadBscPresenter.Navigator() {
        @Override
        public void navigateToTicketControl() {
            navigator.navigateToFromBscTicketControl();
        }

        @Override
        public void navigateBack() {
            navigator.navigateBack();
        }
    };
}
