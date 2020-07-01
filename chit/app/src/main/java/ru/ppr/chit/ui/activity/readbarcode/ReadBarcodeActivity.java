package ru.ppr.chit.ui.activity.readbarcode;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
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
 * Экран чтения штрихкода
 *
 * @author Grigoriy Kashka
 */
public class ReadBarcodeActivity extends MvpActivity implements ReadBarcodeView {

    public static Intent getCallingIntent(@NonNull Context context) {
        return new Intent(context, ReadBarcodeActivity.class);
    }

    // region Di
    private ReadBarcodeComponent component;
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
    private ReadBarcodePresenter presenter;
    //endregion

    @Override
    public void onCreate(Bundle savedInstanceState) {
        component = DaggerReadBarcodeComponent.builder().appComponent(Dagger.appComponent()).activityModule(new ActivityModule(this)).build();
        component.inject(this);
        super.onCreate(savedInstanceState);
        presenter = getMvpDelegate().getPresenter(component::readBarcodePresenter, ReadBarcodePresenter.class);
        ///////////////////////////////////////////////////////////////////////////////////////
        setContentView(R.layout.activity_read_barcode);

        statusBarDelegate.init(getMvpDelegate());

        inProcessState = findViewById(R.id.inProcessState);
        timer = (ImageView) findViewById(R.id.timer);
        progress = (ProgressBar) findViewById(R.id.progress);
        simpleLseView = (SimpleLseView) findViewById(R.id.simpleLseView);
        ///////////////////////////////////////////////////////////////////////////////////////
        presenter.setNavigator(ReadBarcodeNavigator);
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
        stateBuilder.setTextMessage(R.string.read_barcode_could_not_read);
        stateBuilder.setButton1(R.string.read_barcode_repeat_btn, v -> presenter.onRepeatBtnClicked());
        stateBuilder.setButton2(R.string.read_barcode_cancel_btn, v -> presenter.onCancelBtnClicked());
        simpleLseView.setState(stateBuilder.build());
        simpleLseView.show();
    }

    private final ReadBarcodePresenter.Navigator ReadBarcodeNavigator = new ReadBarcodePresenter.Navigator() {
        @Override
        public void navigateToTicketControl() {
            navigator.navigateToFromBarcodeTicketControl();
        }

        @Override
        public void navigateBack() {
            navigator.navigateBack();
        }

    };

    @Override
    public void onBackPressed() {
        navigator.navigateBack();
    }
}
