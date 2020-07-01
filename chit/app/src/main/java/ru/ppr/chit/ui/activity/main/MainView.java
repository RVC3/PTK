package ru.ppr.chit.ui.activity.main;

import ru.ppr.core.ui.mvp.view.MvpView;

/**
 * @author Dmitry Nevolin
 */
interface MainView extends MvpView {

    void setStartBoardingBtnVisible(boolean visible);

    void setEndBoardingBtnVisible(boolean visible);

    void setEndTripServiceBtnVisible(boolean visible);

    void setPassengerListBtnEnabled(boolean enabled);

    void showError(String message);

}
