package ru.ppr.cppk.ui.fragment.posSale;

import ru.ppr.core.ui.mvp.view.MvpView;

/**
 * @author Aleksandr Brazhkin
 */
interface PosSaleView extends MvpView {

    void showConnectingState(long timeout);

    void showConnectionTimeoutState();

    void showConnectedState();

    void showPrintingFirstSlip(boolean repeat);

    void showPrintFirstSlipFailState();

    void showPrintFirstSlipSuccessState();

    void showPrintingSecondSlip();

    void showPrintSecondSlipFailState();

    void showPrintSecondSlipSuccessState();

    void showOperationRejectedWithoutSlip(String bankResponse);

    void showPrintingRejectSlipState(String bankResponse);

    void showPrintRejectSlipFailState();

    void showPrintRejectSlipSuccessState();

    void showCancelConfirmationDialog();
}
