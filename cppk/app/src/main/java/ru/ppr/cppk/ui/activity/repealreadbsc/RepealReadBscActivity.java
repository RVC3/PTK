package ru.ppr.cppk.ui.activity.repealreadbsc;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import java.util.List;

import ru.ppr.core.ui.helper.TimerResourceHelper;
import ru.ppr.cppk.R;
import ru.ppr.cppk.dataCarrier.entity.PD;
import ru.ppr.cppk.di.Dagger;
import ru.ppr.cppk.ui.activity.base.ActivityModule;
import ru.ppr.cppk.ui.activity.base.MvpActivity;
import ru.ppr.cppk.ui.activity.base.Navigator;
import ru.ppr.core.ui.widget.SimpleLseView;


/**
 * Экран чтения БСК.
 *
 * @author Aleksandr Brazhkin
 */
public class RepealReadBscActivity extends MvpActivity implements RepealReadBscView {

    public static Intent getCallingIntent(Context context) {
        return new Intent(context, RepealReadBscActivity.class);
    }

    // region Di
    private RepealReadBscComponent component;
    // endregion
    // region Views
    View inProcessState;
    ImageView timer;
    ProgressBar progress;
    SimpleLseView simpleLseView;
    //endregion
    //region Other
    private RepealReadBscPresenter presenter;
    //endregion

    @Override
    public void onCreate(Bundle savedInstanceState) {
        component = DaggerRepealReadBscComponent.builder().appComponent(Dagger.appComponent()).activityModule(new ActivityModule(this)).build();
        component.inject(this);
        super.onCreate(savedInstanceState);
        presenter = getMvpDelegate().getPresenter(component::repealReadBscPresenter, RepealReadBscPresenter.class);
        ///////////////////////////////////////////////////////////////////////////////////////
        setContentView(R.layout.activity_repeal_read_bsc);
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
        stateBuilder.setTextMessage(R.string.repeal_read_bsc_could_not_read);
        stateBuilder.setButton1(R.string.repeal_read_bsc_repeat_btn, v -> presenter.onRepeatBtnClicked());
        simpleLseView.setState(stateBuilder.build());
        simpleLseView.show();
    }

    private final RepealReadBscPresenter.Navigator navigator = new RepealReadBscPresenter.Navigator() {
        @Override
        public void navigateToRepealFinishActivity(List<PD> pdList, long id) {
            Navigator.navigateToRepealFinishActivity(RepealReadBscActivity.this, pdList, id);
        }

        @Override
        public void navigateToRepealBSCReadErrorActivity() {
            Navigator.navigateToRepealBscReadErrorActivity(RepealReadBscActivity.this);
        }
    };
}
