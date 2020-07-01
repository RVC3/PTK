package ru.ppr.chit.ui.activity.menu;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import javax.inject.Inject;

import ru.ppr.chit.R;
import ru.ppr.chit.di.Dagger;
import ru.ppr.chit.ui.activity.ActivityNavigator;
import ru.ppr.chit.ui.activity.base.ActivityModule;
import ru.ppr.chit.ui.activity.base.MvpActivity;
import ru.ppr.chit.ui.activity.base.delegate.StatusBarDelegate;
import ru.ppr.chit.ui.activity.base.delegate.TripServiceInfoDelegate;

/**
 * Экран меню.
 *
 * @author Dmitry Nevolin
 */
public class MenuActivity extends MvpActivity implements MenuView {

    public static Intent getCallingIntent(Context context) {
        return new Intent(context, MenuActivity.class);
    }

    // region Di
    private MenuComponent component;
    @Inject
    ActivityNavigator navigator;
    @Inject
    StatusBarDelegate statusBarDelegate;
    @Inject
    TripServiceInfoDelegate tripServiceInfoDelegate;
    // endregion
    // region Views
    private Button rootBtn;
    private Button workingStateBtn;
    // endregion
    // region Other
    private MenuPresenter presenter;
    // endregion

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        component = DaggerMenuComponent
                .builder()
                .appComponent(Dagger.appComponent())
                .activityModule(new ActivityModule(this))
                .build();
        component.inject(this);
        super.onCreate(savedInstanceState);
        presenter = getMvpDelegate().getPresenter(component::menuPresenter, MenuPresenter.class);

        setContentView(R.layout.activity_menu);

        statusBarDelegate.init(getMvpDelegate());
        tripServiceInfoDelegate.init(getMvpDelegate());

        rootBtn = (Button) findViewById(R.id.rootBtn);
        rootBtn.setOnClickListener(v -> presenter.onRootBtnClicked());

        workingStateBtn = (Button) findViewById(R.id.workingStateBtn);
        workingStateBtn.setOnClickListener(v -> presenter.onWorkingStateBtnClicked());

        presenter.setNavigator(menuNavigator);
        presenter.initialize();
    }

    @Override
    public void onBackPressed() {
        navigator.navigateBack();
    }

    @Override
    public void setRootBtnVisibility(boolean visible) {
        rootBtn.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    private final MenuPresenter.Navigator menuNavigator = new MenuPresenter.Navigator() {

        @Override
        public void navigateToRoot() {
            navigator.navigateToRoot(false);
        }

        @Override
        public void navigateToWorkingState() {
            navigator.navigateToWorkingState();
        }

    };
}
