package ru.ppr.cppk.ui.activity.pdrepeal.printrepealcheck;


import javax.inject.Inject;

import ru.ppr.core.ui.mvp.viewState.BaseMvpViewState;

/**
 * @author Aleksandr Brazhkin
 */
class PrintRepealCheckViewState extends BaseMvpViewState<PrintRepealCheckView> implements PrintRepealCheckView {

    private Consumer<PrintRepealCheckView> mStateAction;

    @Inject
    PrintRepealCheckViewState() {

    }

    @Override
    protected void onViewAttached(PrintRepealCheckView view) {
        if (mStateAction != null) {
            mStateAction.accept(view);
        }
    }

    @Override
    protected void onViewDetached(PrintRepealCheckView view) {

    }

    @Override
    public void showPrintingState() {
        mStateAction = PrintRepealCheckView::showPrintingState;
        forEachView(mStateAction);
    }

    @Override
    public void showPrintSuccessState() {
        mStateAction = PrintRepealCheckView::showPrintSuccessState;
        forEachView(mStateAction);
    }

    @Override
    public void showPrintFailState() {
        mStateAction = PrintRepealCheckView::showPrintFailState;
        forEachView(mStateAction);
    }

    @Override
    public void showNeedActivateEklzState() {
        mStateAction = PrintRepealCheckView::showNeedActivateEklzState;
        forEachView(mStateAction);
    }

    @Override
    public void showPrintingFailedAndCheckInFrState() {
        mStateAction = PrintRepealCheckView::showPrintingFailedAndCheckInFrState;
        forEachView(mStateAction);
    }

    @Override
    public void showShiftTimeOutError() {
        mStateAction = PrintRepealCheckView::showShiftTimeOutError;
        forEachView(mStateAction);
    }

    @Override
    public void showIncorrectFrStateError() {
        mStateAction = PrintRepealCheckView::showIncorrectFrStateError;
        forEachView(mStateAction);
    }
}
