package ru.ppr.chit.ui.activity.welcome;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import javax.inject.Inject;

import ru.ppr.chit.R;
import ru.ppr.chit.di.Dagger;
import ru.ppr.chit.ui.activity.ActivityNavigator;
import ru.ppr.chit.ui.activity.base.ActivityModule;
import ru.ppr.chit.ui.activity.base.MvpActivity;
import ru.ppr.chit.ui.activity.base.delegate.StatusBarDelegate;
import ru.ppr.chit.ui.activity.base.delegate.TripServiceInfoDelegate;

/**
 * Экран начала работы
 *
 * @author Dmitry Nevolin
 */
public class WelcomeActivity extends MvpActivity implements WelcomeView {

    public static Intent getCallingIntent(Context context) {
        return new Intent(context, WelcomeActivity.class);
    }

    // region Di
    private WelcomeComponent component;
    @Inject
    ActivityNavigator navigator;
    @Inject
    StatusBarDelegate statusBarDelegate;
    @Inject
    TripServiceInfoDelegate tripServiceInfoDelegate;
    // endregion
    // region Views
    private View startTripService;
    // endregion
    // region Other
    private WelcomePresenter presenter;
    // endregion

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        component = DaggerWelcomeComponent
                .builder()
                .appComponent(Dagger.appComponent())
                .activityModule(new ActivityModule(this))
                .build();
        component.inject(this);
        super.onCreate(savedInstanceState);
        presenter = getMvpDelegate().getPresenter(component::welcomePresenter, WelcomePresenter.class);

        setContentView(R.layout.activity_welcome);

        statusBarDelegate.init(getMvpDelegate());
        tripServiceInfoDelegate.init(getMvpDelegate());

        startTripService = findViewById(R.id.start_trip_service);
        startTripService.setOnClickListener(v -> presenter.onStartTripServiceBtnClicked());
        findViewById(R.id.menu).setOnClickListener(v -> presenter.onMenuBtnClicked());

        presenter.setNavigator(welcomeNavigator);
        presenter.initialize();
    }

    private final WelcomePresenter.Navigator welcomeNavigator = new WelcomePresenter.Navigator() {

        @Override
        public void navigateToSetUser() {
            navigator.navigateToSetUser();
        }

        @Override
        public void navigateToMenu() {
            navigator.navigateToMenu();
        }

        @Override
        public void navigateToMain() {
            navigator.navigateToMain(false);
        }

    };

    @Override
    public void setStartTripServiceVisible(boolean visible) {
        startTripService.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

}
