package ru.ppr.chit.ui.activity.splash;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;

import javax.inject.Inject;

import ru.ppr.chit.R;
import ru.ppr.chit.di.Dagger;
import ru.ppr.chit.ui.activity.ActivityNavigator;
import ru.ppr.chit.ui.activity.base.ActivityModule;
import ru.ppr.chit.ui.activity.base.MvpActivity;
import ru.ppr.chit.ui.activity.base.delegate.StatusBarDelegate;


/**
 * Сплеш.
 *
 * @author Aleksandr Brazhkin
 */
public class SplashActivity extends MvpActivity implements SplashView {

    public static Intent getCallingIntent(@NonNull Context context) {
        return new Intent(context, SplashActivity.class);
    }

    // region Di
    private SplashComponent component;
    @Inject
    StatusBarDelegate statusBarDelegate;
    @Inject
    ActivityNavigator navigator;
    // endregion
    // region Views
    TextView message;
    TextView errorMessage;
    //endregion
    //region Other
    private SplashPresenter presenter;
    //endregion

    @Override
    public void onCreate(Bundle savedInstanceState) {
        component = DaggerSplashComponent.builder().appComponent(Dagger.appComponent()).activityModule(new ActivityModule(this)).build();
        component.inject(this);
        super.onCreate(savedInstanceState);
        presenter = getMvpDelegate().getPresenter(component::splashPresenter, SplashPresenter.class);
        ///////////////////////////////////////////////////////////////////////////////////////
        setContentView(R.layout.activity_splash);
        statusBarDelegate.init(getMvpDelegate());
        message = (TextView) findViewById(R.id.message);
        errorMessage = (TextView) findViewById(R.id.errorMessage);
        ///////////////////////////////////////////////////////////////////////////////////////
        presenter.setNavigator(splashNavigator);
        presenter.initialize();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return presenter.onKey(keyCode, event) || super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        return presenter.onKey(keyCode, event) || super.onKeyUp(keyCode, event);
    }

    @Override
    public void onBackPressed() {
        navigator.navigateBack();
    }

    @Override
    public void setState(State state) {
        switch (state) {
            case INIT_EDS: {
                message.setVisibility(View.VISIBLE);
                message.setText(R.string.splash_msg_init_eds_progress);
                errorMessage.setVisibility(View.GONE);
                return;
            }
            case INIT_EDS_ERRROR: {
                errorMessage.setVisibility(View.VISIBLE);
                errorMessage.setText(R.string.splash_msg_init_eds_error);
                message.setVisibility(View.GONE);
                return;
            }
            case DEFAULT:
            default: {
                errorMessage.setVisibility(View.GONE);
                message.setVisibility(View.GONE);
            }
        }
    }

    private final SplashPresenter.Navigator splashNavigator = new SplashPresenter.Navigator() {
        @Override
        public void navigateToWelcome() {
            navigator.navigateToWelcome(false);
        }

        @Override
        public void navigateToSetDeviceId() {
            navigator.navigateToSetDeviceId();
        }

        @Override
        public void navigateToWorkingState() {
            navigator.navigateToWorkingState();
        }

        @Override
        public void navigateToRootAccess() {
            navigator.navigateToRootAccess();
        }

    };
}
