package ru.ppr.chit.ui.activity.setdeviceid;

import javax.inject.Inject;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import ru.ppr.chit.domain.model.local.AppProperties;
import ru.ppr.chit.domain.repository.local.AppPropertiesRepository;
import ru.ppr.chit.helpers.AppSchedulers;
import ru.ppr.core.ui.mvp.presenter.BaseMvpViewStatePresenter;
import ru.ppr.logger.Logger;

/**
 * @author Aleksandr Brazhkin
 */
public class SetDeviceIdPresenter extends BaseMvpViewStatePresenter<SetDeviceIdView, SetDeviceIdViewState> {

    private static final String TAG = Logger.makeLogTag(SetDeviceIdPresenter.class);

    //region Common fields
    private boolean initialized = false;
    //endregion
    //region Di
    private final AppPropertiesRepository appPropertiesRepository;
    //endregion
    //region Other
    private Navigator navigator;
    private String deviceIdText = "";
    private boolean blockUserActions;
    //endregion

    @Inject
    SetDeviceIdPresenter(SetDeviceIdViewState setDeviceIdViewState,
                         AppPropertiesRepository appPropertiesRepository) {
        super(setDeviceIdViewState);
        this.appPropertiesRepository = appPropertiesRepository;
    }

    public void setNavigator(Navigator navigator) {
        this.navigator = navigator;
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

    @Override
    public void destroy() {
        super.destroy();
    }

    void onDoneBtnClicked() {
        Logger.trace(TAG, "onDoneBtnClicked");
        if (blockUserActions) {
            return;
        }
        blockUserActions = true;
        Single
                .fromCallable(() -> {
                    long deviceId = parseDeviceId();
                    if (deviceId == -1) {
                        return false;
                    } else {
                        AppProperties appProperties = new AppProperties();
                        appProperties.setDeviceId(deviceId);
                        appPropertiesRepository.merge(appProperties);
                        return true;
                    }
                })
                .subscribeOn(AppSchedulers.background())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(success -> {
                    if (success) {
                        navigator.navigateToSplash();
                    } else {
                        view.setInvalidDataErrorVisible(true);
                        blockUserActions = false;
                    }
                }, throwable -> Logger.error(TAG, throwable));
    }

    private long parseDeviceId() {
        String deviceId = deviceIdText.trim();
        Logger.trace(TAG, "parseDeviceId(): " + deviceId);
        if (deviceId.matches("^[0-9]+$")) {
            long longValue;
            try {
                longValue = Long.valueOf(deviceId);
            } catch (NumberFormatException e) {
                return -1;
            }
            if (longValue >> 32 != 0) {
                // Значение выходит за границы 4-х байтов
                return -1;
            }
            return longValue;
        } else {
            return -1;
        }
    }

    void onDeviceIdTextChanged(String deviceIdText) {
        Logger.trace(TAG, "onDeviceIdTextChanged(deviceIdText): " + deviceIdText);
        this.deviceIdText = deviceIdText;
    }

    public void onBackPressed() {
        Logger.trace(TAG, "onBackPressed");
        if (blockUserActions) {
            return;
        }
        navigator.navigateBack();
    }

    interface Navigator {
        void navigateBack();

        void navigateToSplash();
    }
}
