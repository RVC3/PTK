package ru.ppr.cppk.ui.fragment.printFineCheck;

import ru.ppr.core.ui.mvp.view.MvpView;

/**
 * @author Aleksandr Brazhkin
 */
interface PrintFineCheckView extends MvpView {

    void showPrintingState();

    void showPrintSuccessState(boolean withCalculateDeliveryBtn);

    void showPrintFailState();

    void showNeedActivateEklzState();

    void showPrintingFailedAndCheckInFrState();

    void showShiftTimeOutError();

    void showIncorrectFrStateError();

    void showReturnMoneyConfirmationDialog();
}
