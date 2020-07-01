package ru.ppr.cppk.ui.activity.pdrepeal.poscancelprintslip;


import ru.ppr.core.ui.mvp.view.MvpView;

/**
 * @author Aleksandr Brazhkin
 */
interface PosCancelPrintSlipView extends MvpView {

    void showPrintingState();

    void showFailState();

    void showSuccessState();
}
