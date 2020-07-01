package ru.ppr.chit.ui.activity.rootaccess;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import javax.inject.Inject;

import ru.ppr.chit.BuildConfig;
import ru.ppr.chit.domain.repository.local.AppPropertiesRepository;
import ru.ppr.chit.helpers.RootPasswordBuilder;
import ru.ppr.core.ui.mvp.presenter.BaseMvpViewStatePresenter;
import ru.ppr.logger.Logger;

/**
 * @author Dmitry Nevolin
 */
class RootAccessPresenter extends BaseMvpViewStatePresenter<RootAccessView, RootAccessViewState> {

    private static final String TAG = Logger.makeLogTag(RootAccessPresenter.class);

    //region Common fields
    private boolean initialized = false;
    //endregion
    //region Di
    private final RootPasswordBuilder rootPasswordBuilder;
    private final AppPropertiesRepository appPropertiesRepository;
    //endregion
    //region Other
    private Navigator navigator;
    private String rootPassword;
    //endregion

    @Inject
    RootAccessPresenter(RootAccessViewState rootAccessViewState,
                        RootPasswordBuilder rootPasswordBuilder,
                        AppPropertiesRepository appPropertiesRepository) {
        super(rootAccessViewState);
        this.rootPasswordBuilder = rootPasswordBuilder;
        this.appPropertiesRepository = appPropertiesRepository;
    }

    void initialize() {
        if (!initialized) {
            initialized = true;
            onInitialize();
        }
    }

    private void onInitialize() {
        Logger.trace(TAG, "onInitialize");
        Long deviceId = appPropertiesRepository.load().getDeviceId();

        if (deviceId == null) {
            Logger.error(TAG, "deviceId is null");
            navigator.navigateToSplash();
            return;
        }

        if (BuildConfig.DEBUG) {
            rootPassword = rootPasswordBuilder.buildDebugRootPassword();
        } else {
            rootPassword = rootPasswordBuilder.buildReleaseRootPassword(deviceId);
            Logger.error(TAG, "rootPassword is null");
            if (rootPassword == null) {
                navigator.navigateToSplash();
            }
        }
    }

    void setNavigator(Navigator navigator) {
        this.navigator = navigator;
    }

    void onRootPasswordProvided(@NonNull String rootPassword) {
        Logger.trace(TAG, "onRootPasswordProvided");
        if (TextUtils.equals(this.rootPassword, rootPassword)) {
            navigator.navigateToRoot();
        } else {
            Logger.info(TAG, "tried rootPassword: " + rootPassword);
            view.setErrorVisible(true);
        }
    }

    interface Navigator {

        void navigateToRoot();

        void navigateToSplash();

    }

}
