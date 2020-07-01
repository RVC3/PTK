package ru.ppr.cppk.ui.fragment.posSale;

import ru.ppr.core.ui.mvp.viewState.BaseMvpViewState;

/**
 * @author Aleksandr Brazhkin
 */
class PosSaleViewState extends BaseMvpViewState<PosSaleView> implements PosSaleView {

    private Consumer<PosSaleView> mStateAction;

    @Override
    protected void onViewAttached(PosSaleView view) {
        if (mStateAction != null) {
            mStateAction.accept(view);
        }
    }

    @Override
    protected void onViewDetached(PosSaleView view) {

    }

    @Override
    public void showConnectingState(long timeout) {
        mStateAction = view -> view.showConnectingState(timeout);
        forEachView(mStateAction);
    }

    @Override
    public void showConnectionTimeoutState() {
        mStateAction = PosSaleView::showConnectionTimeoutState;
        forEachView(mStateAction);
    }

    @Override
    public void showConnectedState() {
        mStateAction = PosSaleView::showConnectedState;
        forEachView(mStateAction);
    }

    @Override
    public void showPrintingFirstSlip(boolean repeat) {
        mStateAction = view -> view.showPrintingFirstSlip(repeat);
        forEachView(mStateAction);

    }

    @Override
    public void showPrintFirstSlipFailState() {
        mStateAction = PosSaleView::showPrintFirstSlipFailState;
        forEachView(mStateAction);
    }

    @Override
    public void showPrintFirstSlipSuccessState() {
        mStateAction = PosSaleView::showPrintFirstSlipSuccessState;
        forEachView(mStateAction);
    }

    @Override
    public void showPrintingSecondSlip() {
        mStateAction = PosSaleView::showPrintingSecondSlip;
        forEachView(mStateAction);
    }

    @Override
    public void showPrintSecondSlipFailState() {
        mStateAction = PosSaleView::showPrintSecondSlipFailState;
        forEachView(mStateAction);
    }

    @Override
    public void showPrintSecondSlipSuccessState() {
        mStateAction = PosSaleView::showPrintSecondSlipSuccessState;
        forEachView(mStateAction);
    }

    @Override
    public void showOperationRejectedWithoutSlip(String bankResponse) {
        mStateAction = view -> view.showOperationRejectedWithoutSlip(bankResponse);
        forEachView(mStateAction);
    }

    @Override
    public void showPrintingRejectSlipState(String bankResponse) {
        mStateAction = view -> view.showPrintingRejectSlipState(bankResponse);
        forEachView(mStateAction);
    }

    @Override
    public void showPrintRejectSlipFailState() {
        mStateAction = PosSaleView::showPrintRejectSlipFailState;
        forEachView(mStateAction);
    }

    @Override
    public void showPrintRejectSlipSuccessState() {
        mStateAction = PosSaleView::showPrintRejectSlipSuccessState;
        forEachView(mStateAction);
    }

    @Override
    public void showCancelConfirmationDialog() {
        forEachView(PosSaleView::showCancelConfirmationDialog);
    }
}
