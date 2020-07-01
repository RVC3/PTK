package ru.ppr.cppk.ui.activity.decrementtrip;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import ru.ppr.core.ui.helper.TimerResourceHelper;
import ru.ppr.cppk.R;
import ru.ppr.cppk.di.Dagger;
import ru.ppr.cppk.ui.activity.base.ActivityModule;
import ru.ppr.cppk.ui.activity.base.MvpActivity;
import ru.ppr.cppk.ui.activity.decrementtrip.model.DecrementTripParams;
import ru.ppr.cppk.ui.activity.decrementtrip.model.DecrementTripResult;
import ru.ppr.core.ui.widget.SimpleLseView;


/**
 * Экран списания поездки.
 *
 * @author Aleksandr Brazhkin
 */
public class DecrementTripActivity extends MvpActivity implements DecrementTripView {

    // EXTRAS
    private static final String EXTRA_PARAMS = "EXTRA_PARAMS";
    private static final String EXTRA_RESULT = "EXTRA_RESULT";

    public static Intent getCallingIntent(Context context, DecrementTripParams decrementTripParams) {
        Intent intent = new Intent(context, DecrementTripActivity.class);
        intent.putExtra(EXTRA_PARAMS, decrementTripParams);
        return intent;
    }

    @Nullable
    public static DecrementTripResult getResultFromIntent(int resultCode, @Nullable final Intent intent) {
        DecrementTripResult decrementTripResult = null;
        if (resultCode == Activity.RESULT_OK && intent != null) {
            decrementTripResult = intent.getParcelableExtra(EXTRA_RESULT);
        }
        return decrementTripResult;
    }

    // region Di
    private DecrementTripComponent component;
    // endregion
    // region Views
    View inProcessState;
    ImageView timer;
    ProgressBar progress;
    SimpleLseView simpleLseView;
    //endregion
    //region Other
    private DecrementTripPresenter presenter;
    //endregion

    @Override
    public void onCreate(Bundle savedInstanceState) {
        component = DaggerDecrementTripComponent
                .builder()
                .appComponent(Dagger.appComponent())
                .decrementTripParams(getIntent().getParcelableExtra(EXTRA_PARAMS))
                .activityModule(new ActivityModule(this))
                .build();
        component.inject(this);
        super.onCreate(savedInstanceState);
        presenter = getMvpDelegate().getPresenter(component::decrementTripPresenter, DecrementTripPresenter.class);
        ///////////////////////////////////////////////////////////////////////////////////////
        canUserHardwareButton();
        ///////////////////////////////////////////////////////////////////////////////////////
        setContentView(R.layout.activity_decrement_trip);
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
        switch (state) {
            case SEARCH_CARD: {
                inProcessState.setVisibility(View.VISIBLE);
                simpleLseView.setVisibility(View.GONE);
                timer.setVisibility(View.VISIBLE);
                progress.setVisibility(View.GONE);
                break;
            }
            case SEARCH_CARD_ERROR: {
                setErrorState(state);
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
                setErrorState(state);
                break;
            }
        }
    }

    private void setErrorState(State state) {
        inProcessState.setVisibility(View.GONE);

        SimpleLseView.State.Builder stateBuilder = new SimpleLseView.State.Builder();
        stateBuilder.setMode(SimpleLseView.State.MODE_ERROR);
        if (state == State.SEARCH_CARD_ERROR) {
            stateBuilder.setTextMessage(R.string.decrement_trip_card_not_found);
        } else {
            stateBuilder.setTextMessage(R.string.decrement_trip_could_not_decrement);
        }
        stateBuilder.setButton1(R.string.decrement_trip_cancel_btn, v -> presenter.onCancelBtnClicked());
        stateBuilder.setButton2(R.string.decrement_trip_repeat_btn, v -> presenter.onRepeatBtnClicked());
        simpleLseView.setState(stateBuilder.build());
        simpleLseView.show();
    }

    private final DecrementTripPresenter.Navigator navigator = new DecrementTripPresenter.Navigator() {

        @Override
        public void navigateToPreviousScreen(DecrementTripResult decrementTripResult) {
            if (decrementTripResult != null) {
                Intent intent = new Intent();
                intent.putExtra(EXTRA_RESULT, decrementTripResult);
                setResult(RESULT_OK, intent);
            }
            finish();
        }
    };
}
