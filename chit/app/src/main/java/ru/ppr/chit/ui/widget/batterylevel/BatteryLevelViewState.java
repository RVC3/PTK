package ru.ppr.chit.ui.widget.batterylevel;

import javax.inject.Inject;

import ru.ppr.core.ui.mvp.viewState.BaseMvpViewState;

/**
 * @author Dmitry Nevolin
 */
class BatteryLevelViewState extends BaseMvpViewState<BatteryLevelView> implements BatteryLevelView {

    private boolean charging = false;
    private int chargeLevel = 0;

    @Inject
    BatteryLevelViewState() {

    }

    @Override
    protected void onViewAttached(BatteryLevelView view) {
        view.setCharging(charging);
        view.setChargeLevel(chargeLevel);
    }

    @Override
    protected void onViewDetached(BatteryLevelView view) {

    }

    @Override
    public void setCharging(boolean charging) {
        this.charging = charging;
        forEachView(view -> view.setCharging(this.charging));
    }

    @Override
    public void setChargeLevel(int chargeLevel) {
        this.chargeLevel = chargeLevel;
        forEachView(view -> view.setChargeLevel(this.chargeLevel));
    }

}
