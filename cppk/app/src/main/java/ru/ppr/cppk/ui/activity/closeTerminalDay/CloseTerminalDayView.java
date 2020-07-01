package ru.ppr.cppk.ui.activity.closeTerminalDay;


import ru.ppr.core.ui.mvp.view.MvpView;

/**
 * @author Aleksandr Brazhkin
 */
interface CloseTerminalDayView extends MvpView {
    void hideAnyError();

    void showInitDayEndAskDialog();

    void hideInitDayEndAskDialog();

    void showPreparingDataProgress();

    void showPreparingDataError();

    void showClosingDayProgress();

    void showClosingDayError(String bankResponse);

    void showCloseDaySuccessDialog(long dayNumber);

    void hideAnyProgress();

    void showPrintingSlipProgress();

    void showPrintingSlipError();
}
