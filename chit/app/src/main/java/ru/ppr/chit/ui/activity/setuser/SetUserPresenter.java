package ru.ppr.chit.ui.activity.setuser;

import javax.inject.Inject;

import io.reactivex.Completable;
import io.reactivex.disposables.Disposable;
import io.reactivex.disposables.Disposables;
import ru.ppr.chit.domain.tripservice.TripServiceInfoStorage;
import ru.ppr.chit.domain.boarding.BoardingManager;
import ru.ppr.chit.domain.controlstation.ControlStationManager;
import ru.ppr.chit.domain.model.local.User;
import ru.ppr.chit.domain.tripservice.TripServiceMode;
import ru.ppr.chit.domain.tripservice.TripServiceModeManager;
import ru.ppr.chit.helpers.AppSchedulers;
import ru.ppr.chit.domain.tripservice.TripServiceManager;
import ru.ppr.chit.helpers.UiThread;
import ru.ppr.core.exceptions.UserException;
import ru.ppr.core.ui.mvp.presenter.BaseMvpViewStatePresenter;
import ru.ppr.logger.Logger;

/**
 * @author Dmitry Nevolin
 */
class SetUserPresenter extends BaseMvpViewStatePresenter<SetUserView, SetUserViewState> {

    private static final String TAG = Logger.makeLogTag(SetUserPresenter.class);

    private boolean initialized;
    private Navigator navigator;

    private final TripServiceManager tripServiceManager;
    private final TripServiceInfoStorage tripServiceInfoStorage;
    private final UiThread uiThread;
    private final ControlStationManager controlStationManager;
    private final BoardingManager boardingManager;
    private final TripServiceModeManager tripServiceModeManager;
    private Disposable userNameProvidedDisposable = Disposables.disposed();

    @Inject
    SetUserPresenter(SetUserViewState setUserViewState,
                     TripServiceManager tripServiceManager,
                     TripServiceInfoStorage tripServiceInfoStorage,
                     ControlStationManager controlStationManager,
                     UiThread uiThread,
                     BoardingManager boardingManager,
                     TripServiceModeManager tripServiceModeManager) {
        super(setUserViewState);
        this.tripServiceManager = tripServiceManager;
        this.tripServiceInfoStorage = tripServiceInfoStorage;
        this.uiThread = uiThread;
        this.controlStationManager = controlStationManager;
        this.boardingManager = boardingManager;
        this.tripServiceModeManager = tripServiceModeManager;
    }

    void initialize() {
        if (!initialized) {
            initialized = true;
            onInitialize();
        }
    }

    private void onInitialize() {
        Logger.trace(TAG, "onInitialize");
    }

    void setNavigator(Navigator navigator) {
        this.navigator = navigator;
    }

    void onUserNameProvided(String userName) {
        Logger.trace(TAG, "onUserNameProvided(userName): " + userName);
        if (userName == null || userName.isEmpty()) {
            uiThread.post(() -> view.setUserNameEmptyErrorVisible(true));
        } else {
            userNameProvidedDisposable = Completable
                    .fromAction(() -> {
                        User user = new User();
                        user.setName(userName);
                        // Задаем пользователя
                        tripServiceInfoStorage.updateUser(user);
                        // Если мы внутри обслуживания поездки - значит передаём её
                        if (tripServiceManager.isTripServiceStarted()) {
                            tripServiceManager.transferTripService();
                            navigator.navigateBack();
                        } else {
                            // Иначе начинаем обслуживание, посадку
                            tripServiceManager.startTripService();
                            boardingManager.startBoarding();
                            navigator.navigateForwardToMain();
                        }
                    })
                    .subscribeOn(AppSchedulers.background())
                    .subscribe(
                            () -> Logger.info(TAG, String.format("onUserNameProvided() set user [%s]", userName)),
                            error -> {
                                Logger.error(TAG, "onUserNameProvided() error of setting user", error);
                                uiThread.post(() -> view.showError(UserException.wrap(error).getMessage()));
                            });
        }
    }

    @Override
    public void destroy() {
        userNameProvidedDisposable.dispose();

        super.destroy();
    }

    interface Navigator {

        void navigateBack();

        void navigateBackToMain();

        void navigateForwardToMain();

    }

}
