package ru.ppr.chit.ui.activity.welcome;

import javax.inject.Inject;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.disposables.Disposables;
import ru.ppr.chit.bs.RegistrationInformant;
import ru.ppr.chit.bs.RegistrationState;
import ru.ppr.chit.domain.tripservice.TripServiceManager;
import ru.ppr.core.ui.mvp.presenter.BaseMvpViewStatePresenter;
import ru.ppr.logger.Logger;

/**
 * @author Dmitry Nevolin
 */
class WelcomePresenter extends BaseMvpViewStatePresenter<WelcomeView, WelcomeViewState> {

    private static final String TAG = Logger.makeLogTag(WelcomePresenter.class);

    private boolean initialized;
    private Navigator navigator;

    private final RegistrationInformant registrationInformant;
    private final TripServiceManager tripServiceManager;
    private Disposable registrationStatesDisposable = Disposables.disposed();

    @Inject
    WelcomePresenter(WelcomeViewState welcomeViewState,
                     RegistrationInformant registrationInformant,
                     TripServiceManager tripServiceManager) {
        super(welcomeViewState);
        this.registrationInformant = registrationInformant;
        this.tripServiceManager = tripServiceManager;
    }

    void initialize() {
        if (!initialized) {
            initialized = true;
            onInitialize();
        }
    }

    private void onInitialize() {
        Logger.trace(TAG, "onInitialize");
        registrationStatesDisposable = registrationInformant
                .getRegistrationStatePublisher()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(registrationState -> view.setStartTripServiceVisible(registrationState != RegistrationState.NOT_PREPARED));
    }

    void setNavigator(Navigator navigator) {
        this.navigator = navigator;
    }

    void onStartTripServiceBtnClicked() {
        Logger.trace(TAG, "onStartTripServiceBtnClicked");
        if (tripServiceManager.isTripServiceStarted()) {
            navigator.navigateToMain();
        } else {
            navigator.navigateToSetUser();
        }
    }

    void onMenuBtnClicked() {
        Logger.trace(TAG, "onMenuBtnClicked");
        navigator.navigateToMenu();
    }

    @Override
    public void destroy() {
        super.destroy();
        registrationStatesDisposable.dispose();
    }

    interface Navigator {

        void navigateToSetUser();

        void navigateToMenu();

        void navigateToMain();

    }

}
