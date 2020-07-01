package ru.ppr.chit.ui.widget.batterylevel;

import javax.inject.Inject;

import ru.ppr.core.manager.BatteryManager;
import ru.ppr.core.ui.mvp.presenter.BaseMvpViewStatePresenter;

/**
 * @author Dmitry Nevolin
 */
class BatteryLevelPresenter extends BaseMvpViewStatePresenter<BatteryLevelView, BatteryLevelViewState> {

    private boolean initialized;

    private final BatteryManager batteryManager;

    @Inject
    BatteryLevelPresenter(BatteryLevelViewState batteryLevelViewState,
                          BatteryManager batteryManager) {
        super(batteryLevelViewState);
        this.batteryManager = batteryManager;
    }

    void initialize() {
        if (!initialized) {
            initialized = true;
            onInitialize();
        }
    }

    private void onInitialize() {
        batteryManager.addBatteryStateListener(batteryStateListener);
    }

    @Override
    public void destroy() {
        batteryManager.removeBatteryStateListener(batteryStateListener);

        super.destroy();
    }

    private BatteryManager.BatteryStateListener batteryStateListener = new BatteryManager.BatteryStateListener() {

        @Override
        public void onChargeLevelChanged(int chargeLevel) {
            view.setChargeLevel(chargeLevel);
        }

        @Override
        public void onPowerConnectedStateChanged(boolean powerConnected) {
            view.setCharging(powerConnected);
        }

    };

}
