package ru.ppr.chit.ui.widget.batterylevel;

import ru.ppr.core.ui.mvp.view.MvpView;

/**
 * @author Dmitry Nevolin
 */
interface BatteryLevelView extends MvpView {

    void setCharging(boolean charging);

    void setChargeLevel(int chargeLevel);

}
