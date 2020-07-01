package ru.ppr.cppk.ui.activity.pdrepeal.poscancel;


import ru.ppr.core.ui.mvp.view.MvpView;

/**
 * @author Aleksandr Brazhkin
 */
interface PosCancelView extends MvpView {

    void showConnectingState(long timeout);

    void showConnectionTimeoutState();

    void showConnectedState();
}
