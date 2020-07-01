package ru.ppr.cppk.ui.activity.pdrepeal.printrepealcheck;

import ru.ppr.core.ui.mvp.view.MvpView;

/**
 * @author Aleksandr Brazhkin
 */
interface PrintRepealCheckView extends MvpView {

    void showPrintingState();

    void showPrintSuccessState();

    void showPrintFailState();

    void showNeedActivateEklzState();

    void showPrintingFailedAndCheckInFrState();

    void showShiftTimeOutError();

    void showIncorrectFrStateError();
}
