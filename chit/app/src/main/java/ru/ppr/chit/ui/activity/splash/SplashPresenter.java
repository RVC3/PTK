package ru.ppr.chit.ui.activity.splash;

import android.support.annotation.NonNull;
import android.view.KeyEvent;

import javax.inject.Inject;

import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.disposables.Disposables;
import ru.ppr.chit.domain.repository.local.AppPropertiesRepository;
import ru.ppr.chit.helpers.AppSchedulers;
import ru.ppr.chit.helpers.RootAccessConditionsChecker;
import ru.ppr.chit.helpers.UiThread;
import ru.ppr.core.manager.eds.EdsManagerWrapper;
import ru.ppr.core.ui.mvp.presenter.BaseMvpViewStatePresenter;
import ru.ppr.edssft.SftEdsChecker;
import ru.ppr.edssft.model.GetStateResult;
import ru.ppr.logger.Logger;

/**
 * @author Aleksandr Brazhkin
 */
public class SplashPresenter extends BaseMvpViewStatePresenter<SplashView, SplashViewState> {

    private static final String TAG = Logger.makeLogTag(SplashPresenter.class);

    //region Common fields
    private boolean initialized = false;
    //endregion
    //region Di
    private final AppPropertiesRepository appPropertiesRepository;
    private final UiThread uiThread;
    private final EdsManagerWrapper edsManager;
    private final RootAccessConditionsChecker rootAccessConditionsChecker;
    //endregion
    //region Other
    private Navigator navigator;
    private Disposable initializeDisposable = Disposables.disposed();
    //endregion

    @Inject
    SplashPresenter(SplashViewState setDeviceIdViewState,
                    AppPropertiesRepository appPropertiesRepository,
                    UiThread uiThread,
                    EdsManagerWrapper edsManager,
                    RootAccessConditionsChecker rootAccessConditionsChecker) {
        super(setDeviceIdViewState);
        this.appPropertiesRepository = appPropertiesRepository;
        this.uiThread = uiThread;
        this.edsManager = edsManager;
        this.rootAccessConditionsChecker = rootAccessConditionsChecker;
    }

    void initialize() {
        if (!initialized) {
            initialized = true;
            onInitialize();
        }
    }

    private void onInitialize() {
        Logger.trace(TAG, "onInitialize");
        initializeDisposable = Single
                .fromCallable(() -> {
                    uiThread.post(() -> view.setState(SplashView.State.INIT_EDS));
                    return appPropertiesRepository.load().getDeviceId() != null;
                })
                .map(hasDeviceKey -> {
                    if (hasDeviceKey) {
                        return true;
                    } else {
                        uiThread.post(() -> navigator.navigateToSetDeviceId());
                        return false;
                    }
                })
                .filter(Boolean.TRUE::equals)
                .flatMapCompletable(skipSetDeviceId -> Completable.fromAction(rootAccessConditionsChecker::awaitCheck))
                .andThen(rootAccessConditionsChecker.getConditionsMetPublisher())
                .flatMapSingle(conditionsMet -> {
                    if (conditionsMet) {
                        return Single.fromCallable(() -> {
                            uiThread.post(() -> navigator.navigateToRootAccess());
                            return false;
                        });
                    } else {
                        return Single.just(true);
                    }
                })
                .filter(Boolean.TRUE::equals)
                .map(skipRootAccess -> {
                    if (checkEdsState()) {
                        return true;
                    } else {
                        uiThread.post(() -> {
                            view.setState(SplashView.State.INIT_EDS_ERRROR);
                            navigator.navigateToWorkingState();
                        });
                        return false;
                    }
                })
                .filter(Boolean.TRUE::equals)
                .subscribeOn(AppSchedulers.background())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(skipWorkingState -> navigator.navigateToWelcome());
    }

    void setNavigator(Navigator navigator) {
        this.navigator = navigator;
    }

    boolean onKey(int keyCode, @NonNull KeyEvent keyEvent) {
        return rootAccessConditionsChecker.onKey(keyCode, keyEvent);
    }

    private boolean checkEdsState() {
        GetStateResult edsSateRes = edsManager.getStateBlocking();
        Logger.info(TAG, "initEds() edsSateRes = " + edsSateRes);

        return edsSateRes.isSuccessful()
                && (edsSateRes.getState() == SftEdsChecker.SFT_STATE_ONLY_CHECK_LICENSE
                || edsSateRes.getState() == SftEdsChecker.SFT_STATE_ALL_LICENSES);
    }

    @Override
    public void destroy() {
        initializeDisposable.dispose();
        super.destroy();
    }

    interface Navigator {

        void navigateToWelcome();

        void navigateToSetDeviceId();

        void navigateToWorkingState();

        void navigateToRootAccess();

    }

}
