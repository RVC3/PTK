package ru.ppr.chit.ui.activity.main;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import javax.inject.Inject;

import ru.ppr.chit.R;
import ru.ppr.chit.di.Dagger;
import ru.ppr.chit.helpers.AppDialogHelper;
import ru.ppr.chit.ui.activity.ActivityNavigator;
import ru.ppr.chit.ui.activity.base.ActivityModule;
import ru.ppr.chit.ui.activity.base.MvpActivity;
import ru.ppr.chit.ui.activity.base.delegate.StatusBarDelegate;
import ru.ppr.chit.ui.activity.base.delegate.TripServiceInfoDelegate;

/**
 * Главный экран приложения с синими кнопками
 *
 * @author Dmitry Nevolin
 */
public class MainActivity extends MvpActivity implements MainView {

    public static Intent getCallingIntent(Context context) {
        return new Intent(context, MainActivity.class);
    }

    // region Di
    private MainComponent component;
    @Inject
    ActivityNavigator navigator;
    @Inject
    StatusBarDelegate statusBarDelegate;
    @Inject
    TripServiceInfoDelegate tripServiceInfoDelegate;
    // endregion
    // region Views
    private Button readBscBtn;
    private Button readBarcodeBtn;
    private Button passengerListBtn;
    private Button startBoardingBtn;
    private Button endBoardingBtn;
    private Button endTripServiceBtn;
    private Button menuBtn;
    // endregion
    // region Other
    private MainPresenter presenter;
    // endregion

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        component = DaggerMainComponent
                .builder()
                .appComponent(Dagger.appComponent())
                .activityModule(new ActivityModule(this))
                .build();
        component.inject(this);
        super.onCreate(savedInstanceState);
        presenter = getMvpDelegate().getPresenter(component::mainPresenter, MainPresenter.class);

        setContentView(R.layout.activity_main);

        statusBarDelegate.init(getMvpDelegate());
        tripServiceInfoDelegate.init(getMvpDelegate());

        readBscBtn = (Button) findViewById(R.id.readBscBtn);
        readBscBtn.setOnClickListener(v -> presenter.onReadBscBtnClicked());
        readBarcodeBtn = (Button) findViewById(R.id.readBarcodeBtn);
        readBarcodeBtn.setOnClickListener(v -> presenter.onReadBarcodeBtnClicked());
        passengerListBtn = (Button) findViewById(R.id.passengerListBtn);
        passengerListBtn.setOnClickListener(v -> presenter.onPassengerListBtnClicked());
        menuBtn = (Button) findViewById(R.id.menuBtn);
        menuBtn.setOnClickListener(v -> presenter.onMenuBtnClicked());
        startBoardingBtn = (Button) findViewById(R.id.startBoardingBtn);
        startBoardingBtn.setOnClickListener(v -> presenter.onStartBoardingBtnClicked());
        endBoardingBtn = (Button) findViewById(R.id.endBoardingBtn);
        endBoardingBtn.setOnClickListener(v -> presenter.onEndBoardingBtnClicked());
        endTripServiceBtn = (Button) findViewById(R.id.endTripServiceBtn);
        endTripServiceBtn.setOnClickListener(v -> presenter.onEndTripServiceBtnClicked());

        presenter.setNavigator(mainNavigator);
        presenter.initialize();
    }

    @Override
    public void setStartBoardingBtnVisible(boolean visible) {
        startBoardingBtn.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    @Override
    public void setEndBoardingBtnVisible(boolean visible) {
        endBoardingBtn.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    @Override
    public void setEndTripServiceBtnVisible(boolean visible) {
        endTripServiceBtn.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    @Override
    public void setPassengerListBtnEnabled(boolean enabled) {
        passengerListBtn.setEnabled(enabled);
    }

    @Override
    public void showError(String message){
        AppDialogHelper.showError(this, message);
    }

    private final MainPresenter.Navigator mainNavigator = new MainPresenter.Navigator() {

        @Override
        public void navigateToReadBsc() {
            navigator.navigateToReadBsc();
        }

        @Override
        public void navigateToReadBarcode() {
            navigator.navigateToReadBarcode();
        }

        @Override
        public void navigateToMenu() {
            navigator.navigateToMenu();
        }

        @Override
        public void navigateToPassengerList() {
            navigator.navigateToPassengerList();
        }

        @Override
        public void navigateToWelcome() {
            navigator.navigateToWelcome(true);
        }

    };

}
