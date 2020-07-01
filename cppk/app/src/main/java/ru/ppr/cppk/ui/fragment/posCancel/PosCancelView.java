package ru.ppr.cppk.ui.fragment.posCancel;


import ru.ppr.core.ui.mvp.view.MvpView;

/**
 * @author Aleksandr Brazhkin
 */
interface PosCancelView extends MvpView {

    void showConnectingState(long timeout);

    void showConnectionTimeoutState();

    void showConnectedState();

    void showPrintingFirstSlip(boolean repeat);

    void showPrintFirstSlipFailState();

    void showPrintFirstSlipSuccessState();

    void showOperationRejectedWithoutSlip(String bankResponse);

    void showPrintingRejectSlipState(String bankResponse);

    void showPrintRejectSlipSuccessState();

    void showPrintRejectSlipFailState();
}
