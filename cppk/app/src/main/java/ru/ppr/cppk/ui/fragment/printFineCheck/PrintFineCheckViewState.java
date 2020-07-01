package ru.ppr.cppk.ui.fragment.printFineCheck;


import javax.inject.Inject;

import ru.ppr.core.ui.mvp.viewState.BaseMvpViewState;

/**
 * @author Aleksandr Brazhkin
 */
class PrintFineCheckViewState extends BaseMvpViewState<PrintFineCheckView> implements PrintFineCheckView {

    private Consumer<PrintFineCheckView> mStateAction;

    @Inject
    PrintFineCheckViewState(){

    }

    @Override
    protected void onViewAttached(PrintFineCheckView view) {
        if (mStateAction != null) {
            mStateAction.accept(view);
        }
    }

    @Override
    protected void onViewDetached(PrintFineCheckView view) {

    }

    @Override
    public void showPrintingState() {
        mStateAction = PrintFineCheckView::showPrintingState;
        forEachView(mStateAction);
    }

    @Override
    public void showPrintSuccessState(boolean withCalculateDeliveryBtn) {
        mStateAction = view -> view.showPrintSuccessState(withCalculateDeliveryBtn);
        forEachView(mStateAction);
    }

    @Override
    public void showPrintFailState() {
        mStateAction = PrintFineCheckView::showPrintFailState;
        forEachView(mStateAction);
    }

    @Override
    public void showNeedActivateEklzState() {
        mStateAction = PrintFineCheckView::showNeedActivateEklzState;
        forEachView(mStateAction);
    }

    @Override
    public void showPrintingFailedAndCheckInFrState() {
        mStateAction = PrintFineCheckView::showPrintingFailedAndCheckInFrState;
        forEachView(mStateAction);
    }

    @Override
    public void showShiftTimeOutError() {
        mStateAction = PrintFineCheckView::showShiftTimeOutError;
        forEachView(mStateAction);
    }

    @Override
    public void showIncorrectFrStateError() {
        mStateAction = PrintFineCheckView::showIncorrectFrStateError;
        forEachView(mStateAction);
    }

    @Override
    public void showReturnMoneyConfirmationDialog() {
        forEachView(PrintFineCheckView::showReturnMoneyConfirmationDialog);
    }
}
